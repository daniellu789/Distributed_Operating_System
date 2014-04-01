import akka.actor.ActorRef
import scala.collection.JavaConversions._
import scala.util.Random


object Topology {

  def createTopo(topoStr: String) : Topology = {
    new Topology(topoStr)
  }
}

class Topology private (val TopoName: String) {

  //This function randomly destroy ONE connection between actors
     
      if(this.TopoName != "full" && this.TopoName != "2D" && this.TopoName != "imp2D" && this.TopoName != "line"){
          println("Please enter correct format: \n run numNodes Topology Algorithm \n Topology: full, line, 2D, imp2D \n Algorithm: gossip, pushsum\n")
          sys.exit(0)
      }

  def BuildTopo(size: Int) : Map[Int, Array[Int]] = this.TopoName match {
    case "full" => {
      (0 to size).map(i => (i, (0 to size).filter(j => j != i).toArray)).toMap
    }
    case "2D" => {
      val row = math.sqrt(size+1).toInt + 1
      (0 to size).map(i => (i, {
        val x = i % row
        val y = i / row
        //if located on up/down/left/right, true, otherwise false
        (0 to size).filter(j => {
          val xx = j % row
          val yy = j / row
          (xx == x && yy == y - 1) || (xx == x && yy == y + 1) || 
          (xx == x - 1 && yy == y) || (xx == x + 1 && yy == y)
        }).toArray
      })
      ).toMap
    }
    case "line" => {
      (0 to size).map(i => (i,
        (0 to size).filter(j => (j == i - 1) || (j == i + 1)).toArray
      )).toMap
    }
    case "imp2D" => {
      val rand = new Random(1)
      val row = math.sqrt(size + 1).toInt + 1
      (0 to size).map(i => (i, {
        val x = i % row
        val y = i / row
        //if located on up/down/left/right, true, otherwise false
        ((0 to size).filter(j => {
          val xx = j % row
          val yy = j / row
          (xx == x && yy == y - 1) || (xx == x && yy == y + 1) || 
          (xx == x - 1 && yy == y) || (xx == x + 1 && yy == y)
        //:+ random node
        }).toList :+ rand.shuffle((0 to size).filter(j=>j!=i)).get(0)).toArray
      })).toMap
    }
  }
}