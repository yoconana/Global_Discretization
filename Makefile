target:
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
run:
	java -classpath .:. StartMain
clean:
	rm -rf ./*.class
     
