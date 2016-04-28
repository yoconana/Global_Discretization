import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;



public class Utils {
	
	private static boolean enableDebug = false;
	
	public static void printlnLogs(String logs){
		if(enableDebug){
			System.out.println(logs);
		}		
	}
	
	public static BigDecimal getVarianceofBlocksCountswithCutpoints
				(BigDecimal[] cutpoints,Map<BigDecimal,Integer> valueCountMap){
		int len = cutpoints.length;
		int[] countsArray = new int[len+1];
		for(int i = 0;i < len+1;i++){
			countsArray[i] = 0;
			for(Entry<BigDecimal, Integer> entry: valueCountMap.entrySet()){
				if(i == 0){
					if(entry.getKey().compareTo(cutpoints[i]) == -1){
						countsArray[i] += entry.getValue();
					}
				}
				else if(i == len){
					if(entry.getKey().compareTo(cutpoints[i-1]) >= 0){
						countsArray[i] += entry.getValue();
					}
				}
				else{
					if(entry.getKey().compareTo(cutpoints[i]) == -1&&entry.getKey().compareTo(cutpoints[i-1]) >= 0){
						countsArray[i] += entry.getValue();
					}
				}
			}
		}
		
		return getVariance(countsArray);
	}
	
	public static BigDecimal[] getAllcutPointsfromAttributes(BigDecimal[] inputString){
		int cutPointsSize = inputString.length-1;
		if(cutPointsSize == 0){
			return inputString;
		}
		BigDecimal[] result = new BigDecimal[cutPointsSize];
		BigDecimal previousValue = null;
		for(int i = 0;i <= cutPointsSize;i++){
			if(i != 0){
				result[i-1] = (inputString[i].add(previousValue)).multiply(new BigDecimal(0.5));
				previousValue = inputString[i];
			}
			else{
				previousValue = inputString[i];
			}
		}

		return result;
	}
	
	public static BigDecimal[] getAllkeysfromMap(Map<BigDecimal,Integer> valueCountMap){
		Set<BigDecimal> attributeValueSet = valueCountMap.keySet();
		if(attributeValueSet == null||attributeValueSet.size() == 0){
			return new BigDecimal[0];
		}
		BigDecimal[] newBigArray = attributeValueSet.toArray(new BigDecimal[attributeValueSet.size()]);
		return newBigArray;
	}
	
	public static int[] getAllvaluesfromMap(Map<BigDecimal,Integer> valueCountMap){
		int len = valueCountMap.size();
		int[] results = new int[len];
		int i = 0;
		for(Entry<BigDecimal, Integer> entry: valueCountMap.entrySet()){
			results[i] = entry.getValue();
			i++;
		}
		return results;
	}
	
	public BigDecimal[] getBestFrequcyCutpoints(Map<BigDecimal,Integer> valueCountMap,int cutpointsNumber){
		BigDecimal[] result = new BigDecimal[cutpointsNumber];
		
		return result;
	}
	
	public static BigDecimal[] getBestFrequencyCutpointCombination(Map<BigDecimal,Integer> valueCountMap, int K){
		BigDecimal[]  elements = getAllcutPointsfromAttributes(getAllkeysfromMap(valueCountMap));

		int N = elements.length;
		
		if(K > N){
			System.out.println("Invalid input, K > N");
			return null;
		}
		// show the possible combinations
		System.out.println("C("+N+","+K+")");
		
		int combination[] = new int[K];
		int r = 0;		
		int index = 0;
		
		BigDecimal[] currentCombination;
		BigDecimal[] bestCombination = null;
		BigDecimal currentVar;
		BigDecimal bestVar = null;
		
		while(r >= 0){
			
			if(index <= (N + (r - K))){
					combination[r] = index;
				if(r == K-1){				
					currentCombination = Utils.getCurrentCombination(combination, elements);
					currentVar = getVarianceofBlocksCountswithCutpoints(currentCombination, valueCountMap);
					if(bestVar != null){						
						if(currentVar.compareTo(bestVar) == -1){
							bestVar = currentVar;
							bestCombination = currentCombination;
						}
					}
					else{
						bestVar = currentVar;
						bestCombination = currentCombination;
					}
					index++;				
				}
				else{
					index = combination[r]+1;
					r++;										
				}
			}
			else{
				r--;
				if(r > 0)
					index = combination[r]+1;
				else
					index = combination[0]+1;	
			}			
		}
		return bestCombination;
	}
	
	
	public static BigDecimal[] getCurrentCombination(int[] combination, BigDecimal[] elements){

		//String output = "";
		int len = combination.length;
		BigDecimal[] output = new BigDecimal[len];
		for(int z = 0 ; z < len;z++){
			output[z] = elements[combination[z]];
		}
		return output;
	}
	
	public static void printLogs(String logs){
		if(enableDebug){
			System.out.print(logs);
		}
		
	}
	
	public static BigDecimal getSum(int[] inputData) {
		  if (inputData == null || inputData.length == 0)
		   return null;
		  int len = inputData.length;
		  BigDecimal sum = new BigDecimal(0);
		  for (int i = 0; i < len; i++) {
			  sum = sum.add(new BigDecimal(inputData[i]));
		  }
		  
		  return sum;

		 }
	
	public static BigDecimal getAverage(int[] inputData) {
		  if (inputData == null || inputData.length == 0)
		   return null;
		  int len = inputData.length;
		  BigDecimal result;
		  result = getSum(inputData).divide(new BigDecimal(len),6, RoundingMode.HALF_UP);
		  
		  return result;
	}
	
	public static BigDecimal getSquareSum(int[] inputData) {
		  if(inputData==null||inputData.length==0)
		      return null;
		  int len=inputData.length;
		  BigDecimal sqrsum = new BigDecimal(0);
		  for (int i = 0; i <len; i++) {
			  sqrsum = sqrsum.add(new BigDecimal(inputData[i]).multiply(new BigDecimal(inputData[i])));
		  }
		  return sqrsum;
	}
	
	public static BigDecimal getVariance(int[] inputData) {
		  BigDecimal count = new BigDecimal(inputData.length);
		  BigDecimal sqrsum = getSquareSum(inputData);
		  BigDecimal average = getAverage(inputData);
		  BigDecimal result;
		  
		  result = (sqrsum.subtract(count.multiply(average).multiply(average))).divide(count,6, RoundingMode.HALF_UP);

		     return result;
	}
	
	public static BigDecimal Round2(BigDecimal value){
		return value.setScale(2, RoundingMode.CEILING);
	}
	
	public static boolean isInteger(String s) {
	    return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
}
