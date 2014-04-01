import akka.actor.{ActorRef, Actor}
import scala.collection.mutable.ListBuffer


object pushNode {
  case class sendPair(s: Double, w: Double)

}

class pushNode(Index: Int, TopoMap: Array[Int]) extends Node(Index, TopoMap) {

  var s = this.Index.toDouble
  var w = 1.0
  var last = s / w
  var count = 0
  var stopped = false
  val endCount = 3

  var NeighborDone = ListBuffer[Int]()

  def receive = {

    case Node.start => {
      if(!this.stopped)
        this.Send()
    }

    case pushNode.sendPair(s_in: Double, w_in: Double) => {
      if(stopped) {
        sender ! Node.done(this.Index)
      } else {
        this.s += s_in
        this.w += w_in
        //println("Node"+this.Index+" :" + this.s/this.w)

        if (math.abs(this.s / this.w - this.last) < 1E-10) {
          this.count += 1
        } else {
          this.count = 0
        }
        this.last = this.s / this.w
        if (this.count == endCount) {
          //println("Node"+this.Index+" :" + this.s/this.w)
          val boss = context.actorSelection("/user/BossInstance")
          boss ! Node.report(this.s / this.w)
          boss ! Node.done(this.Index)
          this.stopped = true
          sender ! Node.done(this.Index)
        } else {

        }
        self ! Node.start
      }
    }

    //If receive message from an Node, remove this Node from its neighbor dictionary AND RESEND TO ANOTHER NEIGHBOR
    case Node.done(index: Int) => {
      //The actor become useless if all its neighbors are stopped!
      this.Neighbors -= index
      if(this.Neighbors.isEmpty) {
        //If a neighbor get isolated, message ping will die here.
        context.actorSelection("/user/BossInstance") ! Node.dead(this.Index)
        this.stopped = true
      } else {
        self ! Node.start
      }

    }
    case _ => {
      println(s"[${this.Index}] Unknown message")
    }
  }

  def Send() {

    val target = GetRandomNeighbor()
    //Holds last
    this.s = this.s / 2.0
    this.w = this.w / 2.0
    context.actorSelection(s"/user/NodeInstance$target") ! pushNode.sendPair(this.s, this.w)
  }

}
