

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



public class EntropyCalculation {
	private DecisionTable calculateTable;//this should be the original table
	private int calculateAttrNo;
	BigDecimal[] calculateCutpoints;
	private HashMap<String,String> caseMapContent;
	private ArrayList<EntroyCasesSet> enSets;
	private BigDecimal startpoint,endpoint;
	
	
	public EntropyCalculation(DecisionTable tb, int attrNo, BigDecimal[] cutpoints,BigDecimal spoint,BigDecimal epoint){
		this.calculateTable = tb;
		this.calculateAttrNo = attrNo;
		this.calculateCutpoints = cutpoints;
		this.startpoint = spoint;
		this.endpoint = epoint;
		initCaseMapContent();
	}
	
	public void initCaseMapContent(){
		caseMapContent = new HashMap<String, String>();
		int caseNumber = calculateTable.getnumberofCases();
		for(int i = 0;i < caseNumber;i++){
			//caseMapContent.put(calculateTable.getItemByIndexinTable(i, calculateAttrNo), calculateTable.getConceptByIndex(i));
			caseMapContent.put(String.valueOf(i), calculateTable.getConceptByIndex(i));
		}
	}
	
	public BigDecimal calculatetheEntroy(boolean ifdivide){
		BigDecimal result = new BigDecimal(0);
		int setSize = enSets.size();
		int caseNumber = calculateTable.getnumberofCases();
		HashSet<String> conceptValueNames = calculateTable.getConceptsValues();
		int conceptsSize = conceptValueNames.size();
		String[] conceptString = conceptValueNames.toArray(new String[conceptsSize]);
		
		//EntroyCasesSet tempcaseSet;
		HashMap<String,String> tempsubSetMap;
		BigDecimal tempValue;
		BigDecimal tempValue2;
		BigDecimal tempValue3;
		int subsetCount;
		for(int i = 0;i < setSize;i++){
			//tempcaseSet = enSets.get(i);
			int[] conceptCounts = new int[conceptsSize];
			tempValue3 = new BigDecimal(0);
			tempsubSetMap = enSets.get(i).getSubCaseMapContent();
			subsetCount = tempsubSetMap.size();
			if(subsetCount == 0){
				Utils.printlnLogs("subsetCount == 0!");
				continue;
			}
			 Set<Map.Entry<String, String>> entrySet1 = tempsubSetMap.entrySet();
			 Iterator<Entry<String, String>> entrySetIterator = entrySet1.iterator();
			 while (entrySetIterator.hasNext()) {
				 Entry<String, String> entry = entrySetIterator.next();
			    for(int j = 0;j < conceptsSize;j++){
			    	if(entry.getValue().equals(conceptString[j])){
			    		conceptCounts[j]++;
			    	}
			    }
			 }
			 for(int j = 0;j < conceptsSize;j++){
				 tempValue = (new BigDecimal(conceptCounts[j])).divide(new BigDecimal(subsetCount),6, RoundingMode.HALF_UP);
				 if(tempValue.floatValue() > 0){
					 tempValue2 = new BigDecimal(Math.log(tempValue.doubleValue())/Math.log(2));
					 tempValue3 = tempValue3.subtract(
							 tempValue.multiply(tempValue2
									 ));
				 }
				 
			 }
			 //result = result*(BigDecimal)subsetCount/(BigDecimal)caseNumber;
			 //Utils.printlnLogs("result="+result);
			 tempValue3 = tempValue3.multiply(new BigDecimal(subsetCount)).divide(new BigDecimal(caseNumber),6, RoundingMode.HALF_UP);
			 result = tempValue3.add(result);
		}
		//result = result/(BigDecimal)setSize;
		if(ifdivide == true){
			result = result.divide(new BigDecimal(setSize),6, RoundingMode.HALF_UP);
		}		
		Utils.printlnLogs("result="+result);
		return result;
	}
	
	public void dispatchCaseMapConceptoDifferentSets(){
		//first create several sets
		enSets = new ArrayList<EntroyCasesSet>();
		int numberofSets = calculateCutpoints.length+1;
		if(numberofSets <= 1){
			EntroyCasesSet startset = new EntroyCasesSet(startpoint,endpoint);
			enSets.add(startset);
			//return;
		}
		else{
			EntroyCasesSet startset = new EntroyCasesSet(startpoint,calculateCutpoints[0]);	
			enSets.add(startset);
			
			for(int i = 0;i < numberofSets-2;i++){
				EntroyCasesSet newset = new EntroyCasesSet(calculateCutpoints[i],calculateCutpoints[i+1]);
				enSets.add(newset);
			}
			EntroyCasesSet endset = new EntroyCasesSet(calculateCutpoints[numberofSets-2],endpoint);
			enSets.add(endset);
		}		
		//then dispatch items into different sets
		int caseNumber = calculateTable.getnumberofCases();
		//iterate upon caseMapContent
		int tempIndex;
		for(int i = 0; i < caseNumber;i++){
			//drop to No. tempIndex set.
			tempIndex = checkJumptoWithDivision(calculateTable.getItemByIndexinTable(i, calculateAttrNo));
			if(tempIndex == -1){
				Utils.printlnLogs("tempIndex == -1!"+calculateTable.getItemByIndexinTable(i, calculateAttrNo));
			}
			enSets.get(tempIndex).putIntoHashMap(String.valueOf(i), caseMapContent.get(String.valueOf(i)));
		}
	}
	
	private int  checkJumptoWithDivision(String itemValue)
	{
		int numberofSets = enSets.size();
		//BigDecimal BigDecimalItemValue = BigDecimal.parseBigDecimal(itemValue);
		BigDecimal BigDecimalItemValue = new BigDecimal(itemValue);
		for(int i = 0;i < numberofSets;i++){
			//if(BigDecimalItemValue < enSets.get(i).getEndpoint()&&BigDecimalItemValue >= enSets.get(i).getStartpoint()){
			if(i == numberofSets-1){
				if(BigDecimalItemValue.compareTo(enSets.get(i).getEndpoint()) <= 0
						&&BigDecimalItemValue.compareTo(enSets.get(i).getStartpoint()) >= 0){
					//hit. drop in this division
					return i;
				}
			}
			else{
				if(BigDecimalItemValue.compareTo(enSets.get(i).getEndpoint()) == -1
						&&BigDecimalItemValue.compareTo(enSets.get(i).getStartpoint()) >= 0){
					//hit. drop in this division
					return i;
				}
			}
			
		}
		return -1;
	}
	
	class EntroyCasesSet{
		//private HashMap caseMapConcept;
		private HashMap<String,String> subCaseMapContent;
		private String nameValue;
		private BigDecimal startpoint;
		private BigDecimal endpoint;
		
		public EntroyCasesSet(BigDecimal startpoint,BigDecimal endpoint){
			//caseMapConcept = new HashMap();
			this.startpoint = startpoint;
			this.endpoint = endpoint;
			nameValue = Utils.Round2(startpoint) + ".." + Utils.Round2(endpoint);
			subCaseMapContent = new HashMap<String,String>();
		}
		
		public HashMap<String,String> getSubCaseMapContent(){
			return this.subCaseMapContent;
		}
		
		public BigDecimal getStartpoint(){
			return this.startpoint;
		}
		
		public BigDecimal getEndpoint(){
			return this.endpoint;
		}
		
		public void putIntoHashMap(String key,String value){
			subCaseMapContent.put(key, value);
		}
		
		public String getNameValue(){
			return this.nameValue;
		}
	}
}
