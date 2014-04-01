import akka.actor.{Actor, ActorRef}
import scala.collection._
import scala.util.Random


object Node {
  case object start

  //Status
  case class done(val index: Int)
  case class dead(val index: Int)

  //Normal
  case class report(sum: Double)
  case class pong(val index: Int)

}

abstract class Node(val Index: Int, neighbors: Array[Int]) extends Actor {

  //Store neighbors of current actor as a dictionary
  var Neighbors: mutable.HashMap[Int, Null] = new mutable.HashMap[Int, Null]()
  neighbors.foreach(p => {
    Neighbors += (p -> null)
  })

  //Get a random neighbor from the neighbor list
  def GetRandomNeighbor() : Int = {
      val keys = this.Neighbors.keySet.toArray
      keys(Random.nextInt(keys.length))
  }

}

