

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class EqualFrequencyPerInterval {
	private int stepNumber = 2;
	private int currentWorstAttribute;
	
	private List<List<BigDecimal>> currentCutpoints;
	
	private DecisionTable originalTable;
	private DecisionTable currentTable;
	
	private Merging mergingEngine;
	
	BigDecimal[] allmaxmin;
	
	private boolean checkBit = true;
	
	private String dataFilename;
	
	public EqualFrequencyPerInterval(DecisionTable tb,String filename){
		this.originalTable = tb;		
		this.dataFilename = filename;
		this.currentTable = tb.cloneDecisionTable();
		this.currentCutpoints = new ArrayList<List<BigDecimal>>();
		
		this.allmaxmin = tb.calcuateAllMaxMin();
	}
	
	public void doDiscretization(){
		System.out.println("Start Discretization based on Equal Frequency Per Interval...");
		Thread doDThread = new Thread(){
			public void run() {
				while(checkBit){
					if(stepDivision() == true){
						System.out.println("Start Merging...");
						mergingEngine = new Merging(originalTable, currentTable, currentCutpoints,allmaxmin);
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
	
	private boolean stepDivision(){
		System.out.println("step...");
		if(stepNumber == 2){//the first step, k = 2
			//do division for all attributes on currentTable
			int attrNum = currentTable.getNumberofAttributes();
			int caseNum = currentTable.getnumberofCases();
			BigDecimal[] bestCutPoints;
			
			Map<BigDecimal,Integer> tempAttrValuesmapCounts;
			BigDecimal tempcompare;
			
			for(int i = 0;i < attrNum;i++){
				tempAttrValuesmapCounts = originalTable.getAttributeValuesandCounts(i);
				//divide into two parts with equal frequency
				if(tempAttrValuesmapCounts.size() == 1)continue;
				bestCutPoints = Utils.getBestFrequencyCutpointCombination(tempAttrValuesmapCounts, 1);
				if(bestCutPoints != null){
					List<BigDecimal> cplist = new ArrayList<BigDecimal>();		
					cplist.add(bestCutPoints[0]);
					this.currentCutpoints.add(cplist);
					for(int j = 0;j < caseNum;j++){
						tempcompare = new BigDecimal(originalTable.getItemByIndexinTable(j, i));
						if(tempcompare.compareTo(bestCutPoints[0]) == -1){
							currentTable.setItemByIndexinTable(j,i,Utils.Round2(allmaxmin[i*2+1])+".."+Utils.Round2(bestCutPoints[0]));
						}
						else{
							currentTable.setItemByIndexinTable(j,i,Utils.Round2(bestCutPoints[0])+".."+Utils.Round2(allmaxmin[i*2]));
						}
					}
				}				
				
			}	
		}
		else{
			Map<BigDecimal,Integer> tempAttrValuesmapCounts;
			BigDecimal[] bestCutPoints;
			
			int caseNum = originalTable.getnumberofCases();
			int worstStep = currentCutpoints.get(currentWorstAttribute).size()+1;
			tempAttrValuesmapCounts = originalTable.getAttributeValuesandCounts(currentWorstAttribute);
			bestCutPoints = Utils.getBestFrequencyCutpointCombination(tempAttrValuesmapCounts, worstStep);
			if(bestCutPoints != null){
				List<BigDecimal> cplist = Arrays.asList(bestCutPoints);
				currentCutpoints.set(currentWorstAttribute, cplist);

				Collections.sort(cplist);
				//rewrite currentTable
				int cutPointsCount = bestCutPoints.length;
				BigDecimal tempItemValue;
				for(int i = 0;i < caseNum;i++){
					tempItemValue = new BigDecimal(originalTable.getItemByIndexinTable(i, currentWorstAttribute));
					for(int j = 0;j < cutPointsCount+1;j++){
						
						if(j == 0){
							if(tempItemValue.compareTo(bestCutPoints[j]) == -1){
								currentTable.setItemByIndexinTable(i, currentWorstAttribute, 
										Utils.Round2(allmaxmin[currentWorstAttribute*2+1])+".."+Utils.Round2(bestCutPoints[j]));
							}
						}
						else if(j == cutPointsCount){
							if(tempItemValue.compareTo(bestCutPoints[j-1]) >= 0){
								currentTable.setItemByIndexinTable(i, currentWorstAttribute, 
										Utils.Round2(bestCutPoints[j-1])+".."+Utils.Round2(allmaxmin[currentWorstAttribute*2]));
							}
						}
						else{
							if(tempItemValue.compareTo(bestCutPoints[j-1]) >= 0
									&&tempItemValue.compareTo(bestCutPoints[j]) == -1){
								currentTable.setItemByIndexinTable(i, currentWorstAttribute, 
										Utils.Round2(bestCutPoints[j-1])+".."+Utils.Round2(bestCutPoints[j]));
							}
						}
					}
				}
			}			
		}
		//check consistency
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
	
	private int getWorstAttributeNo(){
		int attrNum = currentTable.getNumberofAttributes();
		int maxCase = 0;
		BigDecimal tempresult;
		BigDecimal maxValue = new BigDecimal(-1);
		int attrValueNum;
		for(int i = 0; i < attrNum;i++){
			attrValueNum = originalTable.getAttributeValuesandCounts(i).size()-1;
			if(attrValueNum == currentCutpoints.get(i).size()){
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
		List<BigDecimal> cutpointlist = currentCutpoints.get(attrNo);
		BigDecimal[] blocks = cutpointlist.toArray(new BigDecimal[cutpointlist.size()]);
		
		EntropyCalculation entropyCalcul = new EntropyCalculation(originalTable, attrNo, blocks, allmaxmin[attrNo*2+1], allmaxmin[attrNo*2]);
		entropyCalcul.dispatchCaseMapConceptoDifferentSets();
		BigDecimal result = entropyCalcul.calculatetheEntroy(true);
		return result;
	}
}
