import akka.actor.{Props, ActorRef, Actor}
import scala.collection._
import scala.collection.mutable._


object Boss {

  case class start(numNodes: Int, topo: Topology, algorithm: String)
  case class fail(failCode: Int, failNodes: mutable.HashMap[Int, Int])
  var Nodes: Array[ActorRef] = _
  var endMark: Array[Boolean] = _
  var topo: Topology = _
  var numNodes: Int = 0
  var startTime: Long = 0
  var deadCount = 0
  var estimation: Double = 0.0
  var printmap = false
}

class Boss extends Actor {

  var algorithm: String = _


  def receive() = {

    case Boss.start(numNodes: Int, topo: Topology, algorithm: String) => {
      this.algorithm = algorithm
      
      Boss.numNodes = numNodes



      //Init actors
      //println("Boss started")
      Boss.Nodes = new Array[ActorRef](numNodes)
      Boss.endMark = (1 to numNodes).map(_ => false).toArray

      //Apply topology on actors
      Boss.topo = topo
      val topoMap = Boss.topo.BuildTopo(numNodes - 1)

      //Generate actors
      if(algorithm == "gossip") {
        for(i <- 0 to numNodes - 1) {
          Boss.Nodes(i) = context.system.actorOf(Props(new gossipNode(i, topoMap(i))), s"NodeInstance$i")
        }
      }
      else if(algorithm == "pushsum") {
        for(i <- 0 to numNodes - 1) {
          Boss.Nodes(i) = context.system.actorOf(Props(new pushNode(i, topoMap(i))), s"NodeInstance$i")
        }
      }
      else{println("Please enter correct format: \n run numNodes Topology Algorithm \n Topology: full, line, 2D, imp2D \n Algorithm: gossip, pushsum\n")
      sys.exit(0)}
      //println("Game started")
      Boss.startTime = System.currentTimeMillis()
      //Trigger a Node
      if(algorithm == "gossip") {
        //-1 is the code for boss
        Boss.Nodes((numNodes + 1) / 2) ! gossipNode.fact(-1)
      } else {
        Boss.Nodes((numNodes + 1) / 2) ! Node.start
      }
    }


    case Node.dead(index: Int) => {
      Boss.deadCount += 1
      //Did everyone stop? => not reliable, set a threshold there
      if(this.algorithm != "gossip") {
        Boss.Nodes.foreach(p => context.stop(p))
        context.system.shutdown()
      }
    }

    case Node.done(index: Int) => {
      if(!Boss.endMark(index))
      {
        Boss.endMark(index) = true
        
        val numEnds = Boss.endMark.count(p => p)
        if(numEnds == Boss.numNodes){
          Boss.Nodes.foreach(p => context.stop(p))
          context.system.shutdown()
        }
      }
    }

    case Node.report(sum: Double) => {
      Boss.estimation = sum
    }
  }

}
