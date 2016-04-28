


import java.math.BigDecimal;
import java.math.RoundingMode;



public class Equialintervalwidth {
	private int stepNumber = 2;
	private int[] cutPointsNumber;
	
	private int currentWorstAttribute;
	
	private DecisionTable originalTable;
	private DecisionTable currentTable;
	
	private Merging mergingEngine;
	
	private String dataFilename;
	
	
	
	BigDecimal[] allmaxmin;
	
	private boolean checkBit = true;
	
	public Equialintervalwidth(DecisionTable tb,String filename){
		this.originalTable = tb;		
		this.dataFilename = filename;
		this.currentTable = tb.cloneDecisionTable();
			
		cutPointsNumber = new int[currentTable.getNumberofAttributes()];
	}
	
	public void doDiscretization(){
		System.out.println("Start Discretization based on Equal Interval Width...");
		Thread doDThread = new Thread(){
			public void run() {
				while(checkBit){
					if(stepDivision() == true){
						System.out.println("Start Merging...");
						mergingEngine = new Merging(originalTable, currentTable, cutPointsNumber,allmaxmin);
						mergingEngine.doMerging();
						System.out.println("End Merging! Start to write results to files...");
						FileManager.writeIntervalstoFile(dataFilename, mergingEngine.getAllCutpointsinString());
						//System.out.print(mergingEngine.getAllCutpointsinString());
						FileManager.writeTabletoFile(dataFilename, mergingEngine.getCurrentTable().printDecisionTabletoString());
						//System.out.print(mergingEngine.getCurrentTable().printDecisionTabletoString());
						System.out.println("End Writing.");
						checkBit = false;
					}
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		doDThread.start();
	}
	
	//false: need further work true: can stop
	private boolean stepDivision(){
		if(stepNumber == 2){//the first step, k = 2
			//do division for all attributes on currentTable
			int attrNum = currentTable.getNumberofAttributes();
			int caseNum = currentTable.getnumberofCases();
			allmaxmin = currentTable.calcuateAllMaxMin();
			BigDecimal tempcutPoint;
			BigDecimal tempcompare;
			for(int i = 0; i < attrNum; i++){
				tempcutPoint = allmaxmin[i*2].add(allmaxmin[i*2+1]).multiply(new BigDecimal(0.5));
				for(int j = 0;j < caseNum;j++){
					tempcompare = new BigDecimal(originalTable.getItemByIndexinTable(j, i));
					if(tempcompare.compareTo(tempcutPoint) == -1){
						currentTable.setItemByIndexinTable(j,i,Utils.Round2(allmaxmin[i*2+1])+".."+Utils.Round2(tempcutPoint));
					}
					else{
						currentTable.setItemByIndexinTable(j,i,Utils.Round2(tempcutPoint)+".."+Utils.Round2(allmaxmin[i*2]));
					}
				}
				cutPointsNumber[i] = 1; //at first, only one cut point for each attribute
			}
		}
		else{//only divide the worst case
			cutPointsNumber[currentWorstAttribute]++;
			int cutSize = cutPointsNumber[currentWorstAttribute];
			BigDecimal[] maxmin = originalTable.calcuateMaxandMinAttribute(currentWorstAttribute);
			BigDecimal interval = (maxmin[0].subtract(maxmin[1])).divide(new BigDecimal(cutSize+1),3, RoundingMode.HALF_UP);
			if(interval.compareTo(new BigDecimal(0)) == 0){
				//System.out.println();
			}
			BigDecimal intervalstart,intervalend;
			int caseNum = currentTable.getnumberofCases();
			BigDecimal tempcompare;
			for(int i = 0;i < caseNum;i++){
				for(int j = 0;j < cutSize+1;j++){
					tempcompare = new BigDecimal(originalTable.getItemByIndexinTable(i, currentWorstAttribute));
					intervalstart = maxmin[1].add(interval.multiply(new BigDecimal(j)));
					if(j == cutSize){
						intervalend = maxmin[0];
						if(tempcompare.compareTo(intervalstart) >= 0&&tempcompare.compareTo(intervalend) <= 0){
							currentTable.setItemByIndexinTable(i, currentWorstAttribute, Utils.Round2(intervalstart)+".."+Utils.Round2(intervalend));
						}
					}
					else{
						intervalend = intervalstart.add(interval);
						if(tempcompare.compareTo(intervalstart) >= 0&&tempcompare.compareTo(intervalend) == -1){
							currentTable.setItemByIndexinTable(i, currentWorstAttribute, Utils.Round2(intervalstart)+".."+Utils.Round2(intervalend));					
						}
					}
					
				}				
			}	
			currentTable.printoutDecisionTable();
			//System.out.println();
		}
		ConsistencyChecker newchecker = new ConsistencyChecker(currentTable);
		if(newchecker.ifConsistency() == true){
			System.out.println("Discretization Succeed!");
			//currentTable.enablePrint = true;
			currentTable.printoutDecisionTable();
			return true;
		}
		else{
			stepNumber++;
			currentTable.printoutDecisionTable();
			currentWorstAttribute = getWorstAttributeNo();
			return false;
		}
	}
	
	public int getWorstAttributeNo(){
		int attrNum = currentTable.getNumberofAttributes();
		int maxCase = 0;
		BigDecimal tempresult;
		BigDecimal maxValue = new BigDecimal(-1);
		for(int i = 0; i < attrNum;i++){
			if(cutPointsNumber[i]+1 == originalTable.getAttributeValuesandCounts(i).size()){
				continue;
			}
			if(i == 0){
				maxValue = entropyfAttributewithCutpoint(i);
				maxCase = 0;
			}
			else{
				tempresult = entropyfAttributewithCutpoint(i);
				if(tempresult.compareTo(maxValue) == 1){
					maxValue = tempresult;
					maxCase = i;
				}
			}
		}
		Utils.printlnLogs("WorstAttribute = "+currentTable.getAttributeandLabelNames()[maxCase]);
		return maxCase;
	}
	
	//calculate entropy for one attribute with cut points
	public BigDecimal entropyfAttributewithCutpoint(int attrNo){
		int tempCutpoint = cutPointsNumber[attrNo];
		BigDecimal[] blocks = new BigDecimal[tempCutpoint];
		BigDecimal startpoint,endpoint;
		BigDecimal[] maxmin = originalTable.calcuateMaxandMinAttribute(attrNo);
		startpoint = maxmin[1];
		endpoint = maxmin[0];
		BigDecimal interval = maxmin[0].subtract(maxmin[1]).divide(new BigDecimal(tempCutpoint+1),3, RoundingMode.HALF_UP);
		for(int i = 0;i < tempCutpoint;i++){
			blocks[i] =  maxmin[1].add(interval.multiply(new BigDecimal(i+1)));
		}
		EntropyCalculation entropyCalcul = new EntropyCalculation(originalTable, attrNo, blocks, startpoint, endpoint);
		entropyCalcul.dispatchCaseMapConceptoDifferentSets();
		BigDecimal result = entropyCalcul.calculatetheEntroy(true);
		return result;
	}
	
	public int getStepNumber(){
		return stepNumber;
	}
}
