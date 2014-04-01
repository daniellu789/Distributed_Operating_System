
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util._;
import java.io._
import scala.actors._
import scala.actors.Actor._

object actorlog {
  private var logId = 0
  private def inclogId = { 
    logId+=1; 
    //print("%%%%%%%")
    logId ;
  } 
}

trait actorlog extends Actor{
  
  final case class supermessage(message: Any, id: Int)


    private def writeToFile(filename: String, data: String, append: Boolean = true) ={
    try{
      var writer = new FileWriter(filename, append);
      //println("filename: " + filename + "  append: " + append)
      writer.write(data);
      writer.close;
    }
  }
  
  val id = actorlog.inclogId;
  val logName = id +".log";
  writeToFile(logName, "", false);
  
private def default[A](in: actorlog => A, default: A): A = self match
     {
        case x: actorlog => in(x) ;
        case _ => default ;
      }
  
  
private def LogChange(data: String) = {
    default(login => writeToFile(login.logName, data),() ) ;
  }
  
override def !(message: Any) = {
       val inputID = inId;
       //println("Sender: (" +inputID+ ")    Receiver: (" +id+ ")    Message: " +message+ "\n")
       LogChange("TimeStamp: " +System.currentTimeMillis()+ "   Sender: (" +inputID+ ")    Receiver: (" +id+ ")    Message: " +message+ "\n");
       super.!(supermessage(message, inputID))
  }

override def react(handler: PartialFunction[Any, Unit]) = {
    super.react {
      case supermessage(message:Any, inputID:Int) =>{
        /*
        var writer = new FileWriter(logName, true)
        writer.write("TimeStamp: " +System.currentTimeMillis()+ "   Receiver: (" +id+ ")    Sender: (" +inputID+ ")    Message: " +message+ "\n");
        writer.close;
        */
        //println("Receiver: (" +id+ ")    Sender: (" +inputID+ ")    Message: " +message+ "\n")
        LogChange("TimeStamp: " +System.currentTimeMillis()+ "   Receiver: (" +id+ ")    Sender: (" +inputID+ ")    Message: " +message+ "\n");
        handler.apply(message);
      }
    }
  }
  
private def inId = {
    default(_.id, 0);
  }



}




