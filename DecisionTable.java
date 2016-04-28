

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class DecisionTable {
	private int numberofAttributes;
	private int numberofCases;
	private String[] attributeandLabelNames;//length: numberofAttributes+1
	private HashSet<String> conceptsValues; //name of concept values, such as "small", "high", etc.
	private HashMap<String,Integer> attributeValuesMap;
	public boolean enablePrint = false;
	
	private Map<BigDecimal,Integer> attrbuteValueMapCount = null;
	
	private List<String[]> allElementArray;
	
	private String[][] originalArray;
	
	public DecisionTable(String[][] inputArray){
		this.originalArray = inputArray;
		this.numberofCases = inputArray.length-2;
		this.numberofAttributes = inputArray[0].length-1;
		
		this.conceptsValues = new HashSet<String>();
		attributeandLabelNames = inputArray[1];
		//allElementArray = new String[numberofCases+2][numberofAttributes+1];
		allElementArray = new ArrayList<String[]>();
		for(int i = 0;i < numberofCases;i++){
			String[] newCase = new String[numberofAttributes+1];
			for(int j = 0;j < numberofAttributes+1;j++){
				newCase[j] = inputArray[i+2][j];
			}
			allElementArray.add(newCase);
			conceptsValues.add(inputArray[i+2][numberofAttributes]);
		}

	}
	
/*	public DecisionTable(int numofAttributes,int numofCases,String[] parts){
		this.numberofAttributes = numofAttributes;
		this.numberofCases = numofCases;
		
		
		attributeandLabelNames = parts[1].replace("[ ","").replace(" ]", "").split(" ");
		casesinTalbe = new ArrayList<OneCaseinTalbe>();
		conceptsValues = new HashSet<String>();
		//attributeValuesMap = new HashMap<String,String>();
		originalStrings = parts;
		for(int i = 0;i < numofCases;i++){
			OneCaseinTalbe temponeCase = new OneCaseinTalbe(parts[i+2]);
			conceptsValues.add(temponeCase.getAtributesandLabels()[numberofAttributes]);
			casesinTalbe.add(temponeCase);
		}
	}*/
	
	public DecisionTable(int numofAttributes){
		this.numberofAttributes = numofAttributes;
		this.numberofCases = 0;
		allElementArray = new ArrayList<String[]>();
		
		//casesinTalbe = new ArrayList<OneCaseinTalbe>();
		conceptsValues = new HashSet<String>();
	}
	
	public void addOnecaseToTable(String[] oneCase){
		StringBuilder builder = new StringBuilder();
		int interation = 0;
		for(String s : oneCase) {
			if(interation == oneCase.length-1){
				builder.append(s);
			}
			else{
				builder.append(s).append(" ");
			}		    
		    interation++;
		}
		conceptsValues.add(oneCase[numberofAttributes]);
		String[] newcase = new String[numberofAttributes+1];
		for(int i = 0;i < numberofAttributes+1;i++){
			newcase[i] = oneCase[i];
		}
		allElementArray.add(newcase);
		numberofCases++;
	}
	
/*	public void addOnecaseToTable(OneCaseinTalbe oneCase){
		conceptsValues.add(oneCase.getAtributesandLabels()[numberofAttributes]);
		casesinTalbe.add(oneCase);
		numberofCases++;
	}*/
	
	public void printoutDecisionTable(){
		if(enablePrint == false){
			return;
		}
		int length = allElementArray.size();
		for(int i =0;i < length;i++){
			for(int j = 0;j < numberofAttributes+1;j++){
				Utils.printLogs(allElementArray.get(i)[j]+" ");
			}
			Utils.printLogs("\n");
		}
	}
	
//	public void printoutDecisionTable(){
//		if(enablePrint == false){
//			return;
//		}
//		//Utils.printlnLogs(attributeandLabelNames);
//		int length = casesinTalbe.size();
//		for(int i =0;i < length;i++){
//			for(int j = 0;j < numberofAttributes+1;j++){
//				Utils.printLogs(casesinTalbe.get(i).getAtributesandLabels()[j]+" ");
//			}
//			Utils.printLogs("\n");
//		}
//	}
	
	public String printDecisionTabletoString(){
		String result = "[ ";
		//String result = originalStrings[0] + "\n" + originalStrings[1] +"\n";
		for(int i = 0;i < numberofAttributes+1;i++){
			result += attributeandLabelNames[i] + " ";
		}
		result += "]\n";
		int length = allElementArray.size();
		for(int i =0;i < length;i++){
			for(int j = 0;j < numberofAttributes+1;j++){
				result += allElementArray.get(i)[j]+" ";
			}
			result += "\n";
		}
		return result;
	}
	
//	public String printDecisionTabletoString(){
//		String result = originalStrings[0] + "\n" + originalStrings[1] +"\n";
//		int length = casesinTalbe.size();
//		for(int i =0;i < length;i++){
//			for(int j = 0;j < numberofAttributes+1;j++){
//				result += casesinTalbe.get(i).getAtributesandLabels()[j]+" ";
//			}
//			result += "\n";
//		}
//		return result;
//	}
	
	
	public BigDecimal[] calcuateAllMaxMin(){
		BigDecimal allmaxmin[] = new BigDecimal[2*numberofAttributes];
		BigDecimal tempmm[] = new BigDecimal[2];
		for(int i = 0 ; i < numberofAttributes; i++){
			tempmm = calcuateMaxandMinAttribute(i);
			allmaxmin[i*2] = tempmm[0];
			allmaxmin[i*2+1] = tempmm[1];
		}
		return allmaxmin;
	}
	
	public BigDecimal[] calcuateMaxandMinAttribute(int attributeNo){
		BigDecimal maxmin[] = new BigDecimal[2];
		maxmin[0] = new BigDecimal(allElementArray.get(0)[attributeNo]);
		maxmin[1] = new BigDecimal(allElementArray.get(0)[attributeNo]);
		
		BigDecimal compare;
		for(int i = 1; i < numberofCases; i++){
			compare = new BigDecimal(allElementArray.get(i)[attributeNo]);
			if((maxmin[0]).compareTo(compare) == -1){
				maxmin[0] = new BigDecimal(0).add(compare);
			}
			if((maxmin[1]).compareTo(compare) == 1){
				maxmin[1] = new BigDecimal(0).add(compare);
			}
		}
		return maxmin;
	}
	
	
	public BigDecimal[] getAttributeValuesandCountsArray(int attrNo){
		Set<BigDecimal> attributeValueSet = this.getAttributeValuesandCounts(attrNo).keySet();
		if(attributeValueSet == null||attributeValueSet.size() == 0){
			return new BigDecimal[0];
		}
		BigDecimal[] newBigArray = attributeValueSet.toArray(new BigDecimal[attributeValueSet.size()]);
		return newBigArray;
	}
	
	public Map<BigDecimal,Integer> getAttributeValuesandCounts(int attrNo){
		if(attrbuteValueMapCount == null){
			HashMap<BigDecimal,Integer> attributeValuesMap = new HashMap<BigDecimal,Integer>();
			BigDecimal tempKey;
			Integer tempValue;
			for(int i = 0;i < numberofCases;i++){
				tempKey = new BigDecimal(this.getItemByIndexinTable(i, attrNo));
				
				if(attributeValuesMap.containsKey(tempKey)){
					tempValue = attributeValuesMap.get(tempKey);
					attributeValuesMap.put(tempKey,tempValue+1);
				}
				else{
					attributeValuesMap.put(tempKey, 1);
				}
			}
			Map<BigDecimal, Integer> sortedMap = new TreeMap<BigDecimal, Integer>(attributeValuesMap);
			
			
			return sortedMap;
		}
		else{
			return attrbuteValueMapCount;
		}		
	}
	
	
	public HashSet<String> getConceptsValues(){
		return this.conceptsValues;
	}
	
	public int getNumberofAttributes(){
		return numberofAttributes;
	}
	
	public int getnumberofCases(){
		return numberofCases;
	}
	
	public String[] getAttributeandLabelNames(){
		return attributeandLabelNames;
	}
	
	public String getItemByIndexinTable(int caseNo,int attrNo){
		return allElementArray.get(caseNo)[attrNo];
	}
	
	
	public String getConceptByIndex(int caseNo){
		return allElementArray.get(caseNo)[numberofAttributes];
	}
	
	
	public void setItemByIndexinTable(int caseNo,int attrNo,String value){
		allElementArray.get(caseNo)[attrNo] = value;
	}
	
	
	public String[] getCasebyIndex(int caseNo){
		
		return allElementArray.get(caseNo);
	}
	
	public DecisionTable cloneDecisionTable(){
		DecisionTable newTable = new DecisionTable(this.originalArray);
		return newTable;		
	}
}
