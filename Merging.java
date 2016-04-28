

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;





public class Merging {
	private DecisionTable originTable;
	private DecisionTable currentTable;
	private BigDecimal allmaxmin[];
	private List<List<BigDecimal>> currentCutpoints;
	
	
	public Merging(DecisionTable orgintb,DecisionTable modtb,List<List<BigDecimal>> cutpoints,BigDecimal[] allmm){
		this.originTable = orgintb;
		this.currentTable = modtb;
		this.allmaxmin = allmm;
		this.currentCutpoints = copyCutpointsFrom(cutpoints);
	}
	
	public Merging(DecisionTable orgintb,DecisionTable modtb,int[] cutPointsNumber,BigDecimal[] allmm){
		this.originTable = orgintb;
		this.currentTable = modtb;
		this.allmaxmin = allmm;
		
		this.currentCutpoints = getcutPointsfromEqualInterval(cutPointsNumber);
	}
	
	public DecisionTable getCurrentTable(){
		return this.currentTable;
	}
	
	private List<List<BigDecimal>> copyCutpointsFrom(List<List<BigDecimal>> origin){
		List<List<BigDecimal>> results = new ArrayList<List<BigDecimal>>();
		
		if(origin != null){
			int attrNo = origin.size();
			int tempSize;
			List<BigDecimal> tempList;
			for(int i=0;i < attrNo;i++){
				List<BigDecimal> newre = new ArrayList<BigDecimal>();
				tempList = origin.get(i);
				tempSize = tempList.size();
				for(int j=0;j<tempSize;j++){
					newre.add(tempList.get(j));
				}
				results.add(newre);
			}
			
		}
		
		return results;
	}
	
	
	//calculate all cut points
	public List<List<BigDecimal>> getcutPointsfromEqualInterval(int[] cutPointsNumber){
		List<List<BigDecimal>> results = new ArrayList<List<BigDecimal>>();
		int cutpointCount;
		BigDecimal interval;
		int attrNum = originTable.getNumberofAttributes();
		for(int i = 0;i < attrNum;i++){
			List<BigDecimal> cplist = new ArrayList<BigDecimal>();
			cutpointCount = cutPointsNumber[i];
			interval = (allmaxmin[i*2].subtract(allmaxmin[i*2+1])).divide(new BigDecimal(cutpointCount+1),3, RoundingMode.HALF_UP);
			for(int j = 0;j < cutpointCount;j++){
				cplist.add(allmaxmin[i*2+1].add(interval.multiply(new BigDecimal(j+1))));
				//System.out.println();
			}
			results.add(cplist);
		}
		return results;
	}
	
	public void doMerging(){
		boolean MergingCompletion = true;
		ConsistencyChecker newchecker;
		int attributeNum = currentCutpoints.size();
		int caseNum = currentTable.getnumberofCases();
		int cutpointlen;
		List<BigDecimal> tempCpList;
		BigDecimal[] threeCutpoints = new BigDecimal[3];
		BigDecimal compareValue;
		//List<BigDecimal> removeList;
		int iteration = 0;
		
		for(int i = 0;i < attributeNum;i++){
			tempCpList = currentCutpoints.get(i);
			//removeList = new ArrayList<BigDecimal>();
			//cutpointlen = tempCpList.size();
			MergingCompletion = true;
			iteration = 0;
			while(MergingCompletion){
				cutpointlen = tempCpList.size();
				if(cutpointlen == iteration){
					break;
				}
				threeCutpoints[1] = tempCpList.get(iteration);
				if(cutpointlen == 1){
					threeCutpoints[0] = this.allmaxmin[i*2+1];						
					threeCutpoints[2] = this.allmaxmin[i*2];
				}
				else{
					if(iteration == 0){
						threeCutpoints[0] = this.allmaxmin[i*2+1];
						threeCutpoints[2] = tempCpList.get(iteration+1);					
					}
					else if(iteration == cutpointlen-1){
						threeCutpoints[0] = tempCpList.get(iteration-1);
						threeCutpoints[2] = this.allmaxmin[i*2];
					}
					else{
						threeCutpoints[0] = tempCpList.get(iteration-1);
						//try{
							threeCutpoints[2] = tempCpList.get(iteration+1);
						//}
						//catch(Exception e){
						//	System.out.println();
						//}
					}
				}				
				for(int k = 0;k < caseNum;k++){
					compareValue = new BigDecimal(originTable.getItemByIndexinTable(k, i));
					if((compareValue.compareTo(threeCutpoints[0]) >= 0
							&&compareValue.compareTo(threeCutpoints[2]) == -1)
							||compareValue.compareTo(allmaxmin[i*2]) == 0){
						currentTable.setItemByIndexinTable(k, i, Utils.Round2(threeCutpoints[0])+".."+Utils.Round2(threeCutpoints[2]));
					}
				}
				//currentTable.printoutDecisionTable();
				//check consistency
				newchecker = new ConsistencyChecker(currentTable);
				if(newchecker.ifConsistency() == true){
					//remove this cut point
					tempCpList.remove(iteration);
					iteration--;
					//removeList.add(tempCpList.get(j));
				}
				else{
					//write back currentTable;
					for(int k = 0;k < caseNum;k++){
						compareValue = new BigDecimal(originTable.getItemByIndexinTable(k, i));
						if(compareValue.compareTo(threeCutpoints[0]) >= 0
								&&compareValue.compareTo(threeCutpoints[1]) == -1){
							currentTable.setItemByIndexinTable(k, i, Utils.Round2(threeCutpoints[0])+".."+Utils.Round2(threeCutpoints[1]));
						}
						else if((compareValue.compareTo(threeCutpoints[1]) >= 0
								&&compareValue.compareTo(threeCutpoints[2]) == -1)
								||compareValue.compareTo(allmaxmin[i*2]) == 0){
							currentTable.setItemByIndexinTable(k, i, Utils.Round2(threeCutpoints[1])+".."+Utils.Round2(threeCutpoints[2]));
						}
					}
					//currentTable.printoutDecisionTable();
					//System.out.println("asdfasdf");
				}
				iteration++;
			}
		}
		currentTable.printoutDecisionTable();
	}
	
	public String getAllCutpointsinString(){
		String ctString = "";
		String[] labelnames = originTable.getAttributeandLabelNames();
		if(currentCutpoints != null){
			int ctsize = currentCutpoints.size();
			int onesize;
			List<BigDecimal> tempList;
			for(int i = 0;i < ctsize;i++){
				ctString += labelnames[i]+ ": ";
				ctString += Utils.Round2(allmaxmin[i*2+1])+"..";
				tempList = currentCutpoints.get(i);
				onesize = tempList.size();
				 
				for(int j = 0;j < onesize;j++){
					ctString += Utils.Round2(tempList.get(j))+", "+Utils.Round2(tempList.get(j))+"..";
				}
				ctString += Utils.Round2(allmaxmin[i*2])+"\n";
			}
		}
		return ctString;
	}
}
