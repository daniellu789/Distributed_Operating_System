import scala.actors._
import scala.actors.Actor._
import scala.math._

case object Finished

class example extends Actor with actorlog{
  def act() {
    react {
      case (begin: Long, tasknumber: Long, length: Long, boss: Actor) =>

/*
        var acc:Long  = 0
  for (j <- b until (b + k)){
    //println(j)
    acc = 0
    for (i <- j until (j + l)){
      acc += i*i
    }

val root = round(sqrt(acc))
    if (acc == math.sqrt(acc).toLong*math.sqrt(acc).toLong)
      boss ! (root, i) 
      //Rslt +=  j + "|"
      //println(Rslt)
    
}
*/     //println("begin: "+begin+" number of task :  "+tasknumber+" length:"+length)
        var acc:Long = 0l

        for (i <- begin to (begin+tasknumber-1)) {
          //println("~~i:"+i)
          acc = 0
          for(j <-i to (i+length-1) ){
           // println("--j:"+j)
            acc = acc + j*j
          }
          //acc=acc+1
          //println("||acc: "+acc+" root*root: "+ round(sqrt(acc))*round(sqrt(acc)))

          val root = round(sqrt(acc))
          if(acc == root*root)
            boss ! (i) // get a solution
        }
        boss ! Finished 
    }
  }
}

class BossActor(N: Long, k: Long, np: Int) extends Actor with actorlog{
  
  private val numberOfWorkers = np  // # of subproblems
  private def format(root: Long): String =  root+"|"

  def act() {
    var done: Int = 0
    var msg = N/numberOfWorkers
    if(msg*numberOfWorkers == N){
          for (j <- 1 to numberOfWorkers) { // initializing numberOfWorkers and assign subtasks
      val worker = new example
      worker.start
      worker ! (1+(j-1)*N/numberOfWorkers ,msg, k, self)
     }
    }
    else{
     for (j <- 1 to (numberOfWorkers) ) { // initializing numberOfWorkers and assign subtasks
      val worker = new example
      worker.start
      if(j < numberOfWorkers){
      worker ! (1+(j-1)*N/numberOfWorkers ,msg, k, self)
      }
      else{
        worker ! ( 1+(j-1)*N/numberOfWorkers ,N - (numberOfWorkers - 1)*msg, k, self)
      }
     }
     
    }

    loop {
      react {
        // get a solution
        case (root: Long) =>  
          print(format(root))
        // exit when get all worker's Finished Feedback
        case Finished =>   
          done += 1
          if (done >= numberOfWorkers)
            exit()
      }
      println("")
    } 

  }
}

object example {
  def main(args: Array[String]) {
    var N = 40l
    var k = 24l
    var workers = 4
    print("Result: ")
    var boss = new BossActor(N,k,workers)
    boss.start
  }
}
