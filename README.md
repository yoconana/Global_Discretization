EECS837 Final Project

Description
==============

Given input data file in LERS format, this project uses Java on Linux System to simulate three different kinds of global discretization algorithms in data mining: global discretization based on equal interval width, equal frequency and conditional entropy(Information Gain).

Instruction
==============
1.This project is written in Java.

2.Here are steps of building and running this project.
   1)Open the Terminal on EECS linux platform,and go to the folder which contains all source codes(xxx.java) and the Makefile.
   2)Type in "make" to make the program. 
   *3)Before running the project, please make sure the test data files are put into the "res" folder.
   *4)Make sure those files are of "xxx.d/.txt/.lers" format, or it will not be accepted by the project(for exmaple,"xxx.abc" will not be accepted).
   5)Type in "make run" to run the program.
   6)After program completes running, you can find result files in the "results" folder.

3.Here is a example of making and running the project.

[source code path]$ make

javac ./Utils.java -d .
javac ./ConditionalEntropy.java -d .
javac ./ConsistencyChecker.java -d .
javac ./DecisionTable.java -d .
javac ./EntropyCalculation.java -d .
javac ./EqualFrequencyPerInterval.java -d .
javac ./Equialintervalwidth.java -d .
javac ./FileManager.java -d .
javac ./Merging.java -d .
javac ./StartMain.java -d . -classpath .

[source code path]$ make run

java -classpath .:. StartMain
Please make sure you put your input data files in res folder.
Please intput the name of your input data file(for example, test.d): 
test.d
Starting Reading the File...
Reading Completed.
Please intput the number of the method: 
1. equal interval width
2. equal frequency per interval
3. conditional entropy
4. exit
1
Start Discretization based on Equal Interval Width...
Discretization Succeed!
Start Merging...
End Merging! Start to write results to files...
End Writing.
[source code path]$ 
