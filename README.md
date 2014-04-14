Distributed_Operating_System
============================

Project1:
===========
1 Problem definition
------------
An interesting problem in arithmetic with deep implications to elliptic curve
theory is the problem of finding perfect squares that are sums of consecutive
squares. A classic example is the Pythagorean identity:
3^2 + 4^2 = 5^2 (1)

that reveals that the sum of squares of 3; 4 is itself a square. A more interesting
example is Lucas` Square Pyramid :
1^2 + 2^2 + ::: + 24^2 = 70^2 (2)

In both of these examples, sums of squares of consecutive integers form the
square of another integer.

The goal of this first project is to use Scala and the actor model to build a
good solution to this problem that runs well on multi-core machines.

2 Requirements
-------------
Input: The input provided (as command line to your project1.scala) will
be two numbers: N and k. The overall goal of your program is to nd all
k consecutive numbers, starting at 1, such that the sum of squares is itself a
perfect square (square of an integer).

Output: Print, on independent lines, the rst number in the sequence for each
solution.
Example 1:
scala project1.scala 3 2

3
indicates that sequences of length 2 with start point between 1 and 3 contain
3,4 as a solution since 32 + 42 = 52.

Example 1:

scala project1.scala 40 24

indicates that sequences of length 24 with start point between 1 and 40 contain 1,2,...,24 as a solution since 1^2 + 2^2 + ::: + 24^2 = 70^2. Actor modeling: In this project you have to use exclusively the actor facility in Scala (projects that do not use multiple actors or use any other form of parallelism will receive no credit). A model similar to the one indicated in class for the problem of adding up a lot of numbers can be used here, in particular dene worker actors that are given a range of problems to solve and a boss that keeps track of all the problems and perform the job assignment.

