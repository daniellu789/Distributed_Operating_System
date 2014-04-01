/** 
 * 9/13/2013 4:00 PM 
 * Change Int to Long to avoid overflow
 * 9/12/2013 12:17 AM
 * redistributed the work load to improve the performance while nrOfElements is small
 **/

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration
import scala.concurrent.duration._

object sqrcal extends App {
  var arg0:Int = 0
  var arg1:Int = 0
 
  arg0 = args(0).toInt 
  arg1 = args(1).toInt
  




   calculate(nrOfWorkers = 4, nrOfElements = arg1, nrOfMessages = arg0)   
sealed trait SqrMessage
case object Calculate extends SqrMessage
case class Work(start: Long, interval: Long, nrOfElements: Long) extends SqrMessage
case class Result(value: String)extends SqrMessage
case class SqrRslt(sqrNums: String, duration: Duration)

class Worker extends Actor {
  // calculateSqr
  def calculateSqr(start: Long, interval: Long, nrOfElements: Long): String = {
  var acc:Long  = 0
  var Rslt:String = ""
  //println("This is start: "+start)
  for (j <- start until (start + interval)){
    //println(j)
    acc = 0
    for (i <- j until (j + nrOfElements)){
      acc += i*i
    }
   //println(acc)
    
    
    if (acc == math.sqrt(acc).toLong*math.sqrt(acc).toLong){
      Rslt +=  j + "|"
      //println(Rslt)
    }
  }
    Rslt
  
}
  def receive = {
    case Work(start,interval, nrOfElements) =>
      sender ! Result(calculateSqr(start,interval, nrOfElements)) // perform the work
  }
  
}




class Master(nrOfWorkers: Int, nrOfMessages: Int, nrOfElements: Int, listener: ActorRef)
extends Actor {
  var sqrNums: String = ""
  var nrOfResults: Int = _
  val start: Long = System.currentTimeMillis
  var msg: Int = _
  
  val workerRouter = context.actorOf(
    Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")
 
  def receive = {
    // handle messages ...
    case Calculate =>
      msg=nrOfMessages/nrOfWorkers 
      //println(msg)
      if ((nrOfWorkers * msg) == nrOfMessages){
         for (i <- 0 until nrOfWorkers) {
           //println(i)
           workerRouter ! Work(i * msg + 1, msg , nrOfElements)
         }  
      }
      else {
        msg += 1
        //println(msg)
          for (i <- 0 until (nrOfWorkers - 1)) {
           //println(i)
           workerRouter ! Work(i * msg + 1, msg, nrOfElements)
          }  
          workerRouter ! Work((nrOfWorkers - 1) * msg+1, nrOfMessages - (nrOfWorkers - 1) * msg, nrOfElements)
      }
      
    case Result(value) =>
      if (value != ""){
         sqrNums += value
         
      } 
      nrOfResults += 1
      if (nrOfResults == nrOfWorkers) {
        //Send the result to the listener
        listener ! SqrRslt(sqrNums, duration = (System.currentTimeMillis - start).millis)
        // Stops this actor and all its supervised children
        context.stop(self)
      }
  }
}

class Listener extends Actor {
 
  def receive = {
    case SqrRslt(sqrNums, duration) =>
      //println(sqrNums)
      println("Square Numbers: "+sqrNums +"\nDuration: "+duration)
           //   .format(sqrNums, duration))
      context.system.shutdown()
        
  }
}

  def calculate(nrOfWorkers: Int, nrOfElements: Int, nrOfMessages: Int) {
    // Create an Akka system
    val system = ActorSystem("SqrCalSystem")
 
    // create the result listener, which will print the result and shutdown the system
    val listener = system.actorOf(Props[Listener], name = "listener")
 
    // create the master
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfMessages, nrOfElements, listener)),
      name = "master")
 
    // start the calculation
      if(arg0 == 0 || arg1 == 0)
      {
        println("Please enter valid worker number, N, k")
        system.shutdown()
      }
      else{
        master ! Calculate
      }
  }
}