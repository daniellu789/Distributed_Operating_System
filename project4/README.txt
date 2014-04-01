This is team©\work assignment, team members include:
Yuezhou Teng, UFID: 3676-2017
Danjie Lu, UFID: 3231©\1202
-----------------------------------------------------------------------------------------------------------------
What is working:
Support creating a Log file tp record each message communication between nodes for each node within the project 1.
-----------------------------------------------------------------------------------------------------------------
How you integrated logging with the actors:
Logging is dealt with by simply inheriting from a class, without the need to litter the code with logging code.
We wrapped the actor with actorlog and override the message send and receive function to implement the function that write sender and receiver information along with message to log before sending message and upon receiving message.
-----------------------------------------------------------------------------------------------------------------
What log files are produced by the example.scala and how to interpret them:
Boss(node ID 1)'s log
TimeStamp: 1385261038309   Sender: (1)    Receiver: (2)    Message: (1,10,24,BossActor@58a07808)
TimeStamp: 1385261038312   Sender: (1)    Receiver: (3)    Message: (11,10,24,BossActor@58a07808)
TimeStamp: 1385261038315   Sender: (1)    Receiver: (4)    Message: (21,10,24,BossActor@58a07808)
TimeStamp: 1385261038317   Sender: (1)    Receiver: (5)    Message: (31,10,24,BossActor@58a07808)
TimeStamp: 1385261038338   Receiver: (1)    Sender: (4)    Message: 25
TimeStamp: 1385261038341   Receiver: (1)    Sender: (2)    Message: 1
TimeStamp: 1385261038343   Receiver: (1)    Sender: (3)    Message: 20
TimeStamp: 1385261038345   Receiver: (1)    Sender: (5)    Message: Finished
TimeStamp: 1385261038347   Receiver: (1)    Sender: (4)    Message: Finished
TimeStamp: 1385261038349   Receiver: (1)    Sender: (3)    Message: Finished
TimeStamp: 1385261038351   Receiver: (1)    Sender: (2)    Message: 9
TimeStamp: 1385261038352   Receiver: (1)    Sender: (2)    Message: Finished


One of Worker(node ID 2)'s log
TimeStamp: 1385261038314   Receiver: (2)    Sender: (1)    Message: (1,10,24,BossActor@58a07808)
TimeStamp: 1385261038335   Sender: (2)    Receiver: (1)    Message: 1
TimeStamp: 1385261038339   Sender: (2)    Receiver: (1)    Message: 9
TimeStamp: 1385261038342   Sender: (2)    Receiver: (1)    Message: Finished

Explanation: 
For both boss and worker, the first attribute is TimeStamp, it's system's currentTimeMillis. The second and third attribute is the receiver or sender with their ID, and the last attribute is Message information. For example, (1) the message (1,10,24,BossActor@4ee1f3cb) means begin number is 1, and gives 10 numbers to calculate and to calculate 24's sum of their square; the last one is the message information, (2)the message as an integer , it is the solution of the calculation (3) the message Finished tell the boss its works have been done.
