import scala.actors.Actor
import scala.actors.Actor._
import scala.math
import scala.actors.remote.Node
import scala.actors.TIMEOUT
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer


object project3{
  def main(args: Array[String]) : Unit = {
    var numNodes:Double = 256
    var numRequests:Int = 256
    if(args.length >1){
      numNodes = args(0).toInt
      numRequests = args(1).toInt
      if(numNodes < 3){
        numNodes = 256
        numRequests = 256
        println("Opps, number of nodes has to be more than 2. Default input is Number of Nodes: " + numNodes.toInt + ", Number of Requests: "+ numRequests)
      }
      else    println("Number of Nodes: " + numNodes.toInt + ", Number of Requests: "+ numRequests)
    }
    else{println("Input format incorrect, default input is " + "Number of Nodes: " + 256 + ", Number of Requests: "+ 256)}
    
    var temp: Double = 0
    temp = math.log(numNodes)/math.log(2)
    temp = temp.ceil
    numNodes = math.pow(2, temp)
 
   var Builder = new NetworkBuilder(numNodes.toInt,numRequests)
   Builder.start()
   Builder ! "ready"
  }
}

class PastryNode(forcounter:Counter,binarylength:Int) extends Actor {

  var LeafSet = new ArrayBuffer[Int]
  var connList= new ArrayBuffer[Int] 
  var conn = new ArrayBuffer[PastryNode] 
  type Row = ArrayBuffer[Int]
  val RoutingTable = new Array[Row](binarylength)
  var totNumPeers:Int = 0
  var myBinary:String =""
  var maxLeaf = -1
  var minLeaf = -1
  var curPeers:Int = 0
  var curID:Int = 0;
  var myPeerID:Int = 0;
  var puppet:Int = 0;


  def numofMatched(binary1: String,binary2: String):Int = { // calculate the length of common prefix
        var i:Int = 0
        var length:Int = binary1.length()
        while(i<length){
        try{
        if(binary1(i) == binary2(i)){
          i += 1;
        } else {
          return i
        }
        } catch{
          case e: Exception =>
        }
      }
      return i
  }

  def ifInLeafs(target:Int) : Int = {  // test if request ID is in the leafset
        var nearest = Integer.MAX_VALUE
        var diff = Integer.MAX_VALUE
        i = 0
        while(i<4) { 
          if(LeafSet(i) == target){
            return LeafSet(i);
          } else {
            if(math.abs(target - LeafSet(i)) < diff){
              nearest = (i)
              diff = math.abs(target - LeafSet(i))
            }
          }
          i += 1
        }
        if (diff >= 2) {
          return -1
        } else {
          return LeafSet(nearest)
        }
        return -1
  }

  var i=0
  while (i<binarylength){
    RoutingTable(i) = new ArrayBuffer[Int]
    i += 1
  }

  def tobinary(x: Int): String = {    // convert to binary string
   var binaryNum = Integer.toBinaryString(x)
          if(binaryNum.length()<binarylength) {
            var diff = binarylength - binaryNum.length();
            while(diff >0){
              binaryNum = '0' + binaryNum;
              diff -= 1;
            }
          }
        return binaryNum
  }

  def toflip(binaryNum: String,at:Int): String = {  //1. keep first (at - 1) digits 2. flip the (at)th digits 3. set the digits after (at) to 0
        var length = binaryNum.length()
        var modBinary:String =""
        var atFlag = 0
        var inc = 0
        while(inc<length){
          if(at == inc + 1){
            if(binaryNum(inc) == '1'){
              modBinary = modBinary + '0'
            } else {
              modBinary = modBinary + '1'
            }
              atFlag = 1
          } else {
              if(atFlag == 0){
                modBinary = modBinary + binaryNum(inc)
              } else {
                modBinary = modBinary + '0'
              }
          }
          inc += 1
        }
        return modBinary
  }

  def act(){
    loop {
      react{
        case "exit" =>
          exit()
   
        case input :(ArrayBuffer[PastryNode],Int) =>
          curID = input._2
          conn = input._1
        case input: (Int,ArrayBuffer[Int],String) =>
          myPeerID = input._1
          connList = input._2
          puppet = 1
          myBinary = tobinary(input._1)

        case input : (Int,Int,String,String,String,Int,Int) =>
        
        case input: (Int,ArrayBuffer[Int],String,Int,Int) =>// receiving Peer ID
          if(input._3 == "iniTable"){ //create iniTable 
            curPeers = input._4
            //Build Routing Table 
            var thisBit = 0
            while(thisBit<binarylength){
              RoutingTable(0) += Integer.parseInt(toflip(myBinary,(thisBit + 1)),2)  //add to the table
              thisBit += 1
            }
            //Routing Table End 

            //Build Leaf Build
            totNumPeers = 7
            if (myPeerID + 1 > totNumPeers - 1) {
              LeafSet += 0;LeafSet += 1
            } else if(myPeerID + 2 > totNumPeers -1){
              LeafSet += (myPeerID + 1);LeafSet += 0
            } else {
              LeafSet += (myPeerID + 1);LeafSet += (myPeerID + 2)
              maxLeaf = myPeerID + 2;
            }

            if (myPeerID - 1 < 0) {
              LeafSet += totNumPeers - 2;LeafSet += totNumPeers - 1
            } else if(myPeerID - 2 < 0){
              LeafSet += (myPeerID - 1);LeafSet += totNumPeers -1
            } else {
              LeafSet += (myPeerID - 1);LeafSet += (myPeerID - 2)
              minLeaf = myPeerID - 2;
            }
            // Leaf Build End

            sender ! "ready"
            forcounter ! "Received"
          } else { // Joining node by node after default 2 nodes.(joinTable) //send everyone in the current peer list to update their leafset and routing table accordingly.
            curPeers = input._4
            /* Routing Table */
            if( input._1 == input._5){// For joinTable do this only if this is the new node getting added to network.
              var thisBit = 0
              while(thisBit<binarylength){
                RoutingTable(0) += Integer.parseInt(toflip(myBinary,(thisBit + 1)),2)
                thisBit += 1
              }
            }
            // Routing Table End 

            // Build Leaf Build 
            totNumPeers = curPeers 
            LeafSet.clear
            if (myPeerID + 1 > totNumPeers - 1) {
              LeafSet += 0;LeafSet += 1
            } else if(myPeerID + 2 > totNumPeers -1){
              LeafSet += (myPeerID + 1);LeafSet += 0
            } else {
              LeafSet += (myPeerID + 1);LeafSet += (myPeerID + 2)
              maxLeaf = myPeerID + 2;
            }

            if (myPeerID - 1 < 0) {
              LeafSet += totNumPeers - 2;LeafSet += totNumPeers - 1
            } else if(myPeerID - 2 < 0){
              LeafSet += (myPeerID - 1);LeafSet += totNumPeers -1
            } else {
              LeafSet += (myPeerID - 1);LeafSet += (myPeerID - 2)
              minLeaf = myPeerID - 2;
            }
            // Leaf Build End

            if( input._1 == input._5){
              sender ! "ready"
              forcounter ! "Received"
            }
          }



        case input : (Int,Int,String,String,String,Int) =>
          if(input._3  == "PassingMessage"){
            if (input._1 == myPeerID) {   // check if the current node id match the request id
              forcounter ! (input._6,input._2)
            } else {
              var nextPeer = ifInLeafs(input._1) // check if the request id is in the leafset
              if (nextPeer != -1){ // case that not in the leaf set
              conn(connList(nextPeer)) ! (input._1,input._2 + 1,"PassingMessage","hello","hello",input._6)
              } else {
              /* check routing table */
                var len = RoutingTable(0).size
                var i=0
                var flag =0
                var curr = (tobinary(myPeerID))
                i = numofMatched(curr,tobinary(input._1))
                if (i >= len) {
                } else {                                                  
                  conn(connList(RoutingTable(0)(i))) ! (input._1,input._2 + 1,"PassingMessage","hello","hello",input._6)
                }
              }
            }
          }
        }
      }
    }
}

class NetworkBuilder(numNodes1:Int,numRequests:Int) extends Actor {
  var numNodes = numNodes1-1
  var numPeers = numNodes  
  var k:Int = numNodes-1     
  val binarylength = (math.log(numPeers + 1)/math.log(2)).toInt
  var curPeers:Int = 0
  var list = new ArrayBuffer[Int]()
  var full_buff = new ArrayBuffer[Int]()
  var m:Int = 0
  for(i <- 0 to k){
    list += i
  }
// build random array
  var r = new scala.util.Random
  var randNum = r.nextInt(k);
  var range = 0 to (k);
  while(k >= 0){
    if (k != 0){
      randNum = r.nextInt(k)
    } else {
      randNum = 0
    }
    full_buff += list(randNum)
    list.remove(randNum)
    k -= 1
  }

  var conn = new ArrayBuffer[PastryNode](numNodes)
  k = 0
  val Counter = new Counter(numPeers,full_buff,conn,numRequests)
  Counter.start()

  // initialize all the nodes
  while(k<numNodes){
    var newNode:PastryNode = null
    newNode = new PastryNode(Counter,binarylength)
    newNode.start()
    conn += newNode
    k += 1
  }
  Counter ! conn
  k = 0
  while(k<numNodes){
    conn(k)!(conn,k) //sending node Id
    k += 1
  }
    k =0
  while(k<numNodes){
    conn(full_buff(k)) ! (k,full_buff,"sending peer id and full rand array")
    k +=1
  }

  k=0
  def act(){
    loop{
      react{
        case "ready" =>
          if (k<8) {//build 8 actors 
            conn(full_buff(k)) ! (k,full_buff,"iniTable",curPeers,curPeers) 
            k += 1
            curPeers += 1
          } else if (k<numNodes) { //now do the join
            randNum = r.nextInt(k-1)
            m = 0
            while(m<=curPeers){
              conn(full_buff(m)) ! (m,full_buff,"joinTable",curPeers,k) 
              m +=1
            }
            k += 1
            curPeers += 1
          }
          if(k == numNodes){
            exit()
          }
      }
    }
  }
}

class Counter(numPeers:Int,full_buff:ArrayBuffer[Int],conn:ArrayBuffer[PastryNode],numRequests:Int)extends Actor{
  var readyPeers:Int = 0
  var totNumPeers:Int = numPeers
  var i:Int = 0
  var sum = Array[Int](numPeers+1); sum = Array.fill(numPeers+1)(0)
  var r = new scala.util.Random
  var randNum = r.nextInt(totNumPeers)
  var totalmessages:Int = numRequests*(numPeers)
  var rxmessages:Int = 0
  var j:Int = 0
  var count:Double = 0

  def act(){
    loop {
      react{
        case "Received" =>
        readyPeers += 1
        if(readyPeers == totNumPeers) {
          j = 0
          while(j<numRequests){
            i = 0
            while(i<totNumPeers){
              conn(full_buff(i)) ! (full_buff(randNum),1,"PassingMessage","hello","hello",i)
              randNum = r.nextInt(totNumPeers-1)
              i += 1
            }
            j +=1
          }
        }
        
        case input:(Int,Int) =>
          sum(input._1) = sum(input._1) + input._2
          rxmessages += 1
          if(rxmessages == totalmessages){
            j = 0
            while(j<totNumPeers+1){
              count = count + sum(j)
              j = j+1
            }
            println("Average number of hops (node connections) that have to be traversed to deliever a message: "+ count/((totNumPeers+1)*numRequests))
            j= 0
            while(j<totNumPeers){
              conn(full_buff(j)) ! "exit"
              j += 1
            }
            exit()
          }
        }
      }
    }
}




