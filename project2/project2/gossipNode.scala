import akka.actor.ActorRef
import java.util.concurrent.TimeoutException
import java.util.{TimerTask, Timer}
import scala.collection.mutable.ListBuffer

import scala.concurrent.Await
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext.Implicits.global


object gossipNode {
  case class fact(from: Int)
}

class gossipNode(Index: Int, TopoMap: Array[Int]) extends Node(Index, TopoMap) {

  var count = 0
  var dead = false
  var NeighborDone = ListBuffer[Int]()

  def Send() = {

    val target = GetRandomNeighbor()

    context.actorSelection(s"/user/NodeInstance$target") ! gossipNode.fact(this.Index)

  }

  def receive() = {
    
     case result: Node.pong => {
        self ! Node.start
      }

     case result: Node.done => {
        //We receive a done
        val index = result.index
        if(!this.NeighborDone.contains(index))
          this.NeighborDone += index
        //The actor become useless if all its neighbors are stopped!
        if(this.Neighbors.keySet.toList.sorted == this.NeighborDone.toSet.toList.sorted) {
          this.dead = true
          context.actorSelection("/user/BossInstance") ! Node.done(this.Index)
          context.actorSelection("/user/BossInstance") ! Node.dead(this.Index)
        } else {
          self ! Node.start
        }
      }

    case gossipNode.fact(from: Int) => {

      //If we heard fact less than 10 times, pong sender back and trigger a new send
      if(this.count < 10) {
        this.count += 1
        sender ! Node.pong(this.Index)
      }

      //If count == 1 then we trigger the send
      if(this.count == 1) {
        self ! Node.start
      }

      //If we heard fact 10 more times. Tell boss we stopped and Pong sender back as done
      if(this.count >= 10) {
        context.actorSelection("/user/BossInstance") ! Node.done(this.Index)

        sender ! Node.done(this.Index)
      }
    }

    case Node.start => {
      if(this.count < 10 && (!this.dead)) {
        this.Send
      }
    }

    case _ => {
      println(s"[${this.Index}] Unknown message")
    }

  }

}

