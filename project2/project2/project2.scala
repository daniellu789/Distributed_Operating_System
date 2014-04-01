

import akka.actor.{Props, ActorSystem}
import com.typesafe.config.ConfigFactory
import scala.collection.mutable

object project2 {

  var failureControl : mutable.HashMap[Int, Int] = new mutable.HashMap[Int, Int]()

  def main(args: Array[String])
  {
    //Parse arguments
    val numNodes = args(0).toInt
    val strTopo = args(1)
    val strAlg  = args(2)
    val config = ConfigFactory.parseString(s"""akka{  	
  	log-dead-letters = 0
    log-dead-letters-during-shutdown = off  
   }""")
   
    //Run
    val actorSystem = ActorSystem("Gossip", ConfigFactory.load(config))
    val bossInstance = actorSystem.actorOf(Props[Boss], "BossInstance")
    val topo = Topology.createTopo(strTopo)
    bossInstance ! Boss.start(numNodes, topo, strAlg)
    actorSystem.awaitTermination
    print("Total time is ")
    println(System.currentTimeMillis() - Boss.startTime+" milliseconds!")
    if(strAlg == "gossip") {
    } else {
      println(s"Average = ${Boss.estimation}")
    }
    sys.exit(0)
  }
}
