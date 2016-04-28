import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConditionalEntropy {
	private int stepNumber = 0;
	private int currentWorstAttribute;
	//private BigDecimal currentbestCutpoint;
	
	private DecisionTable originalTable;
	private DecisionTable currentTable;
	private BigDecimal[] allmaxmin;
	
	private List<List<BigDecimal>> currentCutpoints;

	private BigDecimal[] allEntropyForCurrentTable;
	private BigDecimal[] allPreviousBestCutpoint;
	
	private Merging mergingEngine;
	private String dataFilename;
	
	private boolean checkBit = true;
	
	public ConditionalEntropy(DecisionTable tb,String filename){
		this.originalTable = tb;		
		this.dataFilename = filename;
		this.currentTable = tb.cloneDecisionTable();
		this.allmaxmin = tb.calcuateAllMaxMin();
		
		this.currentCutpoints = new ArrayList<List<BigDecimal>>();
		this.allPreviousBestCutpoint = new BigDecimal[tb.getNumberofAttributes()];
		this.allEntropyForCurrentTable = new BigDecimal[tb.getNumberofAttributes()];
	}
	
	public void doDiscretization(){
		System.out.println("Start Discretization based on Conditional Entropy...");
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
		if(stepNumber == 0){
			//calculate All entropy
			Map<BigDecimal,Integer> tempAttrValuesmapCounts;
			

			int attrNo = originalTable.getNumberofAttributes();
			int caseNo = originalTable.getnumberofCases();
			
			EntropyCalculation entropyCalcul;
						
			BigDecimal previousAttributeValue = new BigDecimal(-1);
			BigDecimal compareResult = new BigDecimal(-1);
			BigDecimal currentEntropy;
			BigDecimal bestCutpointforAttr = null;
			int iteratecount;
			BigDecimal[] cutpoints;
			
			
			for(int i=0;i < attrNo;i++){
				tempAttrValuesmapCounts = originalTable.getAttributeValuesandCounts(i);
				
				iteratecount = 0;		
				bestCutpointforAttr = null;
				compareResult = null;	
				
				cutpoints = new BigDecimal[1];
				
				for (Map.Entry<BigDecimal, Integer> entry : tempAttrValuesmapCounts.entrySet()){
					if(iteratecount != 0){
						cutpoints[0] = (previousAttributeValue.add(entry.getKey())).multiply(new BigDecimal(0.5));
						entropyCalcul = new EntropyCalculation(originalTable, i, cutpoints, allmaxmin[i*2+1], allmaxmin[i*2]);
						entropyCalcul.dispatchCaseMapConceptoDifferentSets();
						currentEntropy = entropyCalcul.calculatetheEntroy(false);
						if(compareResult != null){
							if(currentEntropy.compareTo(compareResult) == -1){
								compareResult = currentEntropy;
								bestCutpointforAttr = cutpoints[0];
								
							}
						}
						else{
							compareResult = currentEntropy;
							bestCutpointforAttr = cutpoints[0];
							
						}
					}	
					previousAttributeValue = entry.getKey();
					iteratecount++;
				}
				allPreviousBestCutpoint[i] = bestCutpointforAttr;
				allEntropyForCurrentTable[i] = compareResult;	//save the worst entropy for attribute i
				for(int j = 0;j < caseNo;j++){
					compareResult = new BigDecimal(originalTable.getItemByIndexinTable(j, i));
					if(compareResult.compareTo(bestCutpointforAttr) == -1){
						currentTable.setItemByIndexinTable(j,i,Utils.Round2(allmaxmin[i*2+1])+".."+Utils.Round2(bestCutpointforAttr));
					}
					else{
						currentTable.setItemByIndexinTable(j,i,bestCutpointforAttr+".."+allmaxmin[i*2]);
					}
				}
				if(bestCutpointforAttr != null){
					List<BigDecimal> cplist = new ArrayList<BigDecimal>();
					cplist.add(bestCutpointforAttr);
					currentCutpoints.add(cplist);
				}
				else{
					List<BigDecimal> cplist = new ArrayList<BigDecimal>();
					currentCutpoints.add(cplist);
				}
				
			}
			//check if consistent
			ConsistencyChecker newchecker = new ConsistencyChecker(currentTable);
			if(newchecker.ifConsistency() == true){
				System.out.println("Discretization Succeed!");
				//currentTable.enablePrint = true;
				currentTable.printoutDecisionTable();
				return true;
			}
			else
			{
				//currentTable.enablePrint = true;
				currentTable.printoutDecisionTable();
				stepNumber++;
				currentWorstAttribute = getWorstAttributeNo();
				return false;
			}
			
		}
		else{
			//Now we have the worst attribute. We need to get some sub tables from original table.
			List<BigDecimal> cplist = currentCutpoints.get(currentWorstAttribute);
			BigDecimal[] subTablecutpoint = new BigDecimal[1];
			subTablecutpoint[0] = allPreviousBestCutpoint[currentWorstAttribute];
			ArrayList<DecisionTable> tableList 
			= divideintoSubtables(originalTable, currentWorstAttribute, 
										subTablecutpoint, 
								  allmaxmin[currentWorstAttribute*2+1], allmaxmin[currentWorstAttribute*2]);
			//calculate best cut point for each sub table,and compare them
			int tableCount = tableList.size();
			BigDecimal[] currentMaxmin;
			int caseNo;
			
			EntropyCalculation entropyCalcul;
						
			BigDecimal compareResult = null;
			BigDecimal currentEntropy;
			BigDecimal bestEntroy = null;
			int bestSubTable = 0;
			BigDecimal[] tempAttributesNames;
			BigDecimal[] cutpoints;
			DecisionTable currentSubTable;
			BigDecimal previousAttributeValue = null;
			
			for(int i = 0;i < tableCount;i++){
				currentSubTable = tableList.get(i);
				currentSubTable.printoutDecisionTable();
				//attrNo = currentSubTable.getNumberofAttributes();
				caseNo = currentSubTable.getnumberofCases();
				
				currentMaxmin = currentSubTable.calcuateMaxandMinAttribute(currentWorstAttribute);
				
				tempAttributesNames = currentSubTable.getAttributeValuesandCountsArray(currentWorstAttribute);
				cutpoints = Utils.getAllcutPointsfromAttributes(tempAttributesNames);

				entropyCalcul = new EntropyCalculation(currentSubTable, currentWorstAttribute, cutpoints, currentMaxmin[1], currentMaxmin[0]);
				entropyCalcul.dispatchCaseMapConceptoDifferentSets();
				currentEntropy = entropyCalcul.calculatetheEntroy(false);
				if(currentEntropy.compareTo(new BigDecimal(0)) == 0){
					continue;
				}
				if(bestEntroy == null){
					bestEntroy = currentEntropy;
					bestSubTable = i;
				}
				else{
					if(bestEntroy.compareTo(currentEntropy) == 1){
						bestEntroy = currentEntropy;
						bestSubTable = i;
					}
				}
			}
			//Now we have the better sub table. So we need to calculate the best cut point for this sub table.
			//just like we did in the first step.
			currentSubTable = tableList.get(bestSubTable);
			//attrNo = currentSubTable.getNumberofAttributes();
			caseNo = currentSubTable.getnumberofCases();
			cutpoints = currentSubTable.getAttributeValuesandCountsArray(currentWorstAttribute);
			BigDecimal[] singleCutpoint = new BigDecimal[1];
			int cutPointsSize = cutpoints.length-1;
			currentMaxmin = currentSubTable.calcuateMaxandMinAttribute(currentWorstAttribute);
			bestEntroy = null;
			BigDecimal bestCutPoint = null;
			compareResult = null;
			int cplistSize = 0;
			for(int i = 0;i < cutPointsSize+1;i++){
				if(i != 0){
					
					singleCutpoint[0] = (previousAttributeValue.add(cutpoints[i])).multiply(new BigDecimal(0.5));
					cplistSize = cplist.size();
					for(int j = 0;j < cplistSize;j++){
						if(singleCutpoint[0].compareTo(cplist.get(j)) == 0){
							continue;
						}
					}
					entropyCalcul = new EntropyCalculation(currentSubTable, currentWorstAttribute, singleCutpoint, currentMaxmin[1], currentMaxmin[0]);
					entropyCalcul.dispatchCaseMapConceptoDifferentSets();
					currentEntropy = entropyCalcul.calculatetheEntroy(false);
					if(bestEntroy != null){
						if(currentEntropy.compareTo(bestEntroy) == -1){
							bestEntroy = currentEntropy;
							bestCutPoint = singleCutpoint[0];
							
						}
					}
					else{
						bestEntroy = currentEntropy;
						bestCutPoint = singleCutpoint[0];	
					}
				}	
				previousAttributeValue = cutpoints[i];
			}
			if(bestCutPoint != null){
				allPreviousBestCutpoint[currentWorstAttribute] = bestCutPoint;
				try{
					cplist.add(bestCutPoint);
					Collections.sort(cplist);
				}
				catch(Exception e){
					//System.out.print("kkkkkkkkkkkkkk");
				}
			}			
			
			//rewrite the currentTable
			caseNo = currentTable.getnumberofCases();
			int blockSize = cplist.size()+1;
			for(int i = 0;i < caseNo;i++){
				compareResult = new BigDecimal(originalTable.getItemByIndexinTable(i, currentWorstAttribute));
				for(int j = 0;j < blockSize;j++){
					if(j == 0){
						if(compareResult.compareTo(cplist.get(j)) == -1){
							currentTable.setItemByIndexinTable(i, currentWorstAttribute, 
									Utils.Round2(allmaxmin[currentWorstAttribute*2+1])+".."+Utils.Round2(cplist.get(j)));
						}
					}
					else if(j == blockSize-1){
						if(compareResult.compareTo(cplist.get(j-1)) >= 0){
							currentTable.setItemByIndexinTable(i, currentWorstAttribute, 
									Utils.Round2(cplist.get(j-1))+".."+Utils.Round2(allmaxmin[currentWorstAttribute*2]));
						}
					}
					else{
						if((compareResult.compareTo(cplist.get(j-1)) >= 0)
								&&(compareResult.compareTo(cplist.get(j)) == -1)){
								currentTable.setItemByIndexinTable(i, currentWorstAttribute, Utils.Round2(cplist.get(j-1))+".."+Utils.Round2(cplist.get(j)));
						}
					}
				}
			}
			//calculate the worst attribute
//			entropyCalcul = new EntropyCalculation(originalTable, currentWorstAttribute, (BigDecimal[]) cplist.toArray(), currentMaxmin[1], currentMaxmin[0]);
//			entropyCalcul.dispatchCaseMapConceptoDifferentSets();
//			allEntropyForCurrentTable[currentWorstAttribute] = entropyCalcul.calculatetheEntroy(true);
			//check if consistent
			ConsistencyChecker newchecker = new ConsistencyChecker(currentTable);
			if(newchecker.ifConsistency() == true){
				System.out.println("Discretization Succeed!");
				//currentTable.enablePrint = true;
				currentTable.printoutDecisionTable();
				return true;
			}
			else
			{
				//currentTable.enablePrint = true;
				currentTable.printoutDecisionTable();
				stepNumber++;
				//currentWorstAttribute = getWrostAttributeNoSimper(currentWorstAttribute);
				currentWorstAttribute = getWorstAttributeNo();
				return false;
			}
		}
	}
	
	public int getWrostAttributeNoSimper(int attrNo){
		int maxCase = 0;
		int attrNum = currentTable.getNumberofAttributes();
		BigDecimal maxValue = new BigDecimal(-1);
		BigDecimal tempresult;
		allEntropyForCurrentTable[attrNo] = entropyfAttributewithCutpoint(attrNo);
		int attrValueNum;
		for(int i = 0;i < attrNum;i++){
			attrValueNum = originalTable.getAttributeValuesandCounts(i).size()-1;
			if(attrValueNum == currentCutpoints.get(i).size()){
				continue;
			}
			if(i == 0){
				maxValue = allEntropyForCurrentTable[i];
				maxCase = 0;
			}
			else{
				tempresult = allEntropyForCurrentTable[i];
				if(tempresult.compareTo(maxValue) == 1){
					maxValue = tempresult;
					maxCase = i;
				}
			}
		}
		return maxCase;
	}
	
	public int getWorstAttributeNo(){
		int attrNum = currentTable.getNumberofAttributes();
		int maxCase = 0;
		BigDecimal tempresult;
		BigDecimal maxValue = new BigDecimal(-1);
		int attrValueNum;
		for(int i = 0;i < attrNum;i++){
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
	
	public ArrayList<DecisionTable> divideintoSubtables(DecisionTable dtable,int attrNo,BigDecimal[] cutpoints,
														BigDecimal startpoint,BigDecimal endpoint){
		int blockCount = cutpoints.length+1;
		ArrayList<DecisionTable> dtList = new ArrayList<DecisionTable>();
		
		int caseNo = dtable.getnumberofCases();
		for(int i = 0;i < blockCount;i++){
			DecisionTable newTable = new DecisionTable(dtable.getNumberofAttributes());
			dtList.add(newTable);
		}
		BigDecimal itemValue;
		for(int i = 0;i < caseNo;i++){
			itemValue = new BigDecimal(dtable.getItemByIndexinTable(i, attrNo));
			for(int j = 0;j < blockCount;j++){
				if(j == 0){
					if(itemValue.compareTo(cutpoints[j]) == -1){
						dtList.get(j).addOnecaseToTable(dtable.getCasebyIndex(i));
					}
				}
				else if(j == blockCount - 1){
					if(itemValue.compareTo(cutpoints[j-1]) >= 0&&itemValue.compareTo(endpoint) <= 0){
						//i drop into j block
						dtList.get(j).addOnecaseToTable(dtable.getCasebyIndex(i));
					}
				}
				else{
					if(itemValue.compareTo(cutpoints[j-1]) >= 0&&itemValue.compareTo(cutpoints[j]) == -1){
						dtList.get(j).addOnecaseToTable(dtable.getCasebyIndex(i));
					}
				}
			}
		}
		
		return dtList;
	}
	
	
}
