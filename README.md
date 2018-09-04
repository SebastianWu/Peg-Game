# Peg Game

Author: Yuanxu Wu

# Description

Implement the Davis-Putnam algorithm and use it to solve versions of the peg game.  

The peg game is a well-known puzzle. You have a board with holes in a geometric pattern and a collection of pegs. Initially, there are pegs in all but one of the holes. The rule is that if there are three holes, A, B, C in a row, C is empty, and there are pegs in A and B, then you can jump the peg in A over B to C, and take out the peg in B. The puzzle is to find a way of carrying out jumps so that there is one peg left on the board.  

## Three part of this program.

1. An implementation of the Davis-Putnam procedure, which takes as input a set of clauses and outputs either a satisfying valuation, or a statement that the clauses cannot be satisfied.  
2. A front end, which takes as input a geometric layout of a board and a starting state and outputs a set of clauses that can be input to (1).  
3. A back end, which takes as input the output of (1) and translates it into a solution to the original problem.  

## Compiling the peg puzzle to propositional logic

The peg game can be expressed propositionally as follows: There are two kinds of atoms:  

	Peg(H,I) means that hole H has a peg in it at time I. For instance Peg(5,2) means that hole 5 has a peg at time 2. (Note: in propositional logic, a construction like "Peg(5,2)" is considered a single symbol of 7 characters. The parentheses and comma are not punctuation, they are just characters in the symbol, to make it easier for the human reader.) 
	
	Jump(A,B,C,I) means that at time I, the peg in A is jumped to C over B.  
	
The number of time points is necessarily equal to the number of holes minus one, since you start with one hole and end with one peg. Let N be the number of holes. Then for each hole H and I=1..N-1 there should be an atom Peg(H,I) and for each triple of holes in a row A,B,C and I=1..N-2 there should be an atom Jump(A,B,C,I).

## Propositional Encoding 
There are eight or nine kinds of propositions.  

1. Precondition axioms. If, at time I, you jump a peg from A to C over B, then A and B must have pegs at time I and C must not have a peg at time I.
	For instance, with the above puzzle,   
	Jump(8,5,3,4) => Peg(8,4) ^ Peg(5,4) ^ ~Peg(3,4) . 

2. Causal axioms. If you jump from A to C over B at time I, then, at time I+1, A and B are empty and C has a peg.  
For instance, Jump(8,5,3,4) => ~Peg(8,5) ^ ~Peg(5,5) ^ Peg(3,5) . 

3. Frame axioms. (Frame axioms assert that state can change only if a relevant action occurs.)  
  A. If Peg(H,I) and ~Peg(H,I+1) then either Jump(X,H,Y) or Jump(H,X,Y) for some holes X and Y.   
	For instance, Peg(6,4) ^ ~Peg(6,5) => Jump(10,6,3,4) V Jump(3,6,10,4) V Jump(6,3,1,4) V Jump(6,5,4,4).  
  B. If ~Peg(H,I) and Peg(H,I+1) then Jump(X,Y,H) for some holes X and Y.   
	For instance ~Peg(6,4) ^ Peg(6,5) => Jump(1,3,6,4) V Jump(4,5,6,4).  

4. One action at a time  
  A. No two actions can be executed at the same time.   
	For instance ~(Jump(1,2,4,4) ^ Jump(10,6,3,4))  
  B. (Optional) At least one action is executed at each step.   
	For instance Jump(1,2,4,4) V Jump(4,2,1,4) V ... V Jump(10,9,8,4) (going through all 18 possible jumps).   
	(Adding additional constraints that you know are valid never costs much in performance, unless you add an enormous 	   number, and can hugely improve performance, depending on the specifics.)  

5. Starting state . Specify the truth value of Peg(H,1) for each hole H.  

6. Ending state: Exactly one peg remains. This involves two kinds of axioms.  
  A. At least one peg remains at time N-1. Peg(1,N-1) V Peg(2,N-1) V ... V Peg(N,N-1).  
  B. No two holes have a peg. For each pair of holes H,J, assert that ~(Peg(H,N-1) ^ Peg(J,N-1).  

# How to run this program

# Prerequisites
1. Java 1.8.0

# How to compile and run the three programs:
1. Put FrontEnd.java, DavisPutman.java, BackEnd.java And the input txt file in the same folder.
2. You have to compile all three java file before run them.
3. javac FrontEnd.java
4. javac DavisPutman.java
5. javac BackEnd.java
6. java FrontEnd <input txt filename> , e.g. java FrontEnd FrontEndInput.txt
7. java DavisPutman FrontEndOutput.txt
8. java BackEnd DavisPutmanOutput.txt

# Input txt file format:  
10 1  
1 2 4  
2 4 7  
3 5 8  
4 5 6  
7 8 9  
8 9 10  
1 3 6  
3 6 10  
2 5 9  

## Example:  
$ javac FrontEnd.java  
$ javac DavisPutman.java  
$ javac BackEnd.java  
$ Java FrontEnd FrontEndInput.txt  
The specification of peg game is shown as a set of triples as follow:  
1 2 3  
2 3 4  
3 4 1  
4 1 2  
Preposition Encoding:  
-1 17   
-1 20   
-1 -23   
-2 18   
-2 21   
-2 -24   
-3 23   
-3 20   
-3 -17   
-4 24   
-4 21   
-4 -18   
-5 20   
-5 23   
-5 -26   
-6 21   
-6 24   
-6 -27   
-7 26   
-7 23   
-7 -20   
-8 27   
-8 24   
-8 -21   
-9 23   
-9 26   
-9 -17   
-10 24   
-10 27   
-10 -18   
-11 17   
-11 26   
-11 -23   
-12 18   
-12 27   
-12 -24   
-13 26   
-13 17   
-13 -20   
-14 27   
-14 18   
-14 -21   
-15 20   
-15 17   
-15 -26   
-16 21   
-16 18   
-16 -27   
-1 -18   
-1 -21   
-1 24   
-2 -19   
-2 -22   
-2 25   
-3 -24   
-3 -21   
-3 18   
-4 -25   
-4 -22   
-4 19   
-5 -21   
-5 -24   
-5 27   
-6 -22   
-6 -25   
-6 28   
-7 -27   
-7 -24   
-7 21   
-8 -28   
-8 -25   
-8 22   
-9 -24   
-9 -27   
-9 18   
-10 -25   
-10 -28   
-10 19   
-11 -18   
-11 -27   
-11 24   
-12 -19   
-12 -28   
-12 25   
-13 -27   
-13 -18   
-13 21   
-14 -28   
-14 -19   
-14 22   
-15 -21   
-15 -18   
-15 27   
-16 -22   
-16 -19   
-16 28   
-17 18 1 11 13 15   
-18 19 2 12 14 16   
-20 21 1 3 5 15   
-21 22 2 4 6 16   
-23 24 3 5 7 9   
-24 25 4 6 8 10   
-26 27 7 9 11 13   
-27 28 8 10 12 14   
17 -18 3 9   
18 -19 4 10   
20 -21 7 13   
21 -22 8 14   
23 -24 1 11   
24 -25 2 12   
26 -27 5 15   
27 -28 6 16   
-1 -3   
-1 -5   
-2 -4   
-1 -7   
-2 -6   
-3 -5   
-1 -9   
-2 -8   
-3 -7   
-4 -6   
-1 -11   
-2 -10   
-3 -9   
-4 -8   
-5 -7   
-1 -13   
-2 -12   
-3 -11   
-4 -10   
-5 -9   
-6 -8   
-1 -15   
-2 -14   
-3 -13   
-4 -12   
-5 -11   
-6 -10   
-7 -9   
-16 -2   
-3 -15   
-4 -14   
-5 -13   
-6 -12   
-7 -11   
-8 -10   
-16 -4   
-5 -15   
-6 -14   
-7 -13   
-8 -12   
-9 -11   
-16 -6   
-7 -15   
-8 -14   
-9 -13   
-10 -12   
-16 -8   
-9 -15   
-10 -14   
-11 -13   
-16 -10   
-11 -15   
-12 -14   
-16 -12   
-13 -15   
-16 -14   
-17   
20   
23   
26   
19 22 25 28   
-22 -28   
-25 -28   
-19 -22   
-19 -25   
-19 -28   
-22 -25   
0  
1 Jump(1,2,3,1)   
2 Jump(1,2,3,2)   
3 Jump(3,2,1,1)   
4 Jump(3,2,1,2)   
5 Jump(2,3,4,1)   
6 Jump(2,3,4,2)   
7 Jump(4,3,2,1)   
8 Jump(4,3,2,2)   
9 Jump(3,4,1,1)   
10 Jump(3,4,1,2)   
11 Jump(1,4,3,1)   
12 Jump(1,4,3,2)   
13 Jump(4,1,2,1)   
14 Jump(4,1,2,2)   
15 Jump(2,1,4,1)   
16 Jump(2,1,4,2)   
17 Peg(1,1)   
18 Peg(1,2)   
19 Peg(1,3)   
20 Peg(2,1)   
21 Peg(2,2)   
22 Peg(2,3)   
23 Peg(3,1)   
24 Peg(3,2)   
25 Peg(3,3)   
26 Peg(4,1)   
27 Peg(4,2)   
28 Peg(4,3)   
Encoded Preposition was wrote into FrontEndOutput.txt  

$ java DavisPutman FrontEndOutput.txt  
1 Jump(1,2,3,1) false  
2 Jump(1,2,3,2) true  
3 Jump(3,2,1,1) false  
4 Jump(3,2,1,2) false  
5 Jump(2,3,4,1) false  
6 Jump(2,3,4,2) false  
7 Jump(4,3,2,1) false  
8 Jump(4,3,2,2) false  
9 Jump(3,4,1,1) true  
10 Jump(3,4,1,2) false  
11 Jump(1,4,3,1) false  
12 Jump(1,4,3,2) false  
13 Jump(4,1,2,1) false  
14 Jump(4,1,2,2) false  
15 Jump(2,1,4,1) false  
16 Jump(2,1,4,2) false  
17 Peg(1,1) false  
18 Peg(1,2) true  
19 Peg(1,3) false  
20 Peg(2,1) true  
21 Peg(2,2) true  
22 Peg(2,3) false  
23 Peg(3,1) true  
24 Peg(3,2) false  
25 Peg(3,3) true  
26 Peg(4,1) true  
27 Peg(4,2) false  
28 Peg(4,3) false  
Value Map was wrote into DavisPutmanOutput.txt  

$ java BackEnd DavisPutmanOutput.txt  
At time 1  
	Hole 2 Hole 3 Hole 4 has peg  
	Jump from hole 3 across hole 4 to hole 1  
At time 2  
	Hole 1 Hole 2 has peg  
	Jump from hole 1 across hole 2 to hole 3  
at time 3  
	Hole3 has peg  
Accomplished!  


