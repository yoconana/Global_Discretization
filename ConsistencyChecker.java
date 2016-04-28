import java.util.ArrayList;

//This class is used for checking consistency for decision tables.
public class ConsistencyChecker {
	private DecisionTable checkedTable;
	private ArrayList<setofCases> setsofCases;
	private ArrayList<setofCases> setsofConcepts;
	
	public ConsistencyChecker(DecisionTable ctable){
		this.checkedTable = ctable;
		initsetsofCasesandConcepts();
	}
	
	public void initsetsofCasesandConcepts(){
		setsofCases = new ArrayList<setofCases>();
		setsofConcepts = new ArrayList<setofCases>();
		int caseNumber = checkedTable.getnumberofCases();
		int attrNumber = checkedTable.getNumberofAttributes();
		int tempSize;
		int tempSizeC;
		int compareBit = -1;
		String itemvalue;
		String conceptvalue;
		for(int i = 0;i< caseNumber;i++){
			//get value of attributes
			itemvalue = "";
			for(int j = 0;j < attrNumber;j++){
				itemvalue += checkedTable.getItemByIndexinTable(i, j) + ",";
			}
			conceptvalue = checkedTable.getItemByIndexinTable(i, attrNumber);
			tempSize = setsofCases.size();
			tempSizeC = setsofConcepts.size();
			if(tempSize == 0){
				setofCases newset = new setofCases(itemvalue,String.valueOf(i));
				setsofCases.add(newset);
			}
			else{
				//search among sets
				compareBit = -1;
				for(int j = 0;j < tempSize;j++){
					if(itemvalue.equals(setsofCases.get(j).getItemValue())){
						setsofCases.get(j).addItemtoset(itemvalue, String.valueOf(i));
						compareBit = 0;
						break;
					}
					else{
						//
					}
				}
				if(compareBit == -1){
					//not belong to any subset, thus create a new one.
					setofCases newset = new setofCases(itemvalue,String.valueOf(i));
					setsofCases.add(newset);
				}
			}
			if(tempSizeC == 0){
				setofCases newset = new setofCases(conceptvalue,String.valueOf(i));
				setsofConcepts.add(newset);
			}
			else{
				compareBit = -1;
				for(int j = 0;j < tempSizeC;j++){
					if(conceptvalue.equals(setsofConcepts.get(j).getItemValue())){
						setsofConcepts.get(j).addItemtoset(conceptvalue, String.valueOf(i));
						compareBit = 0;
						break;
					}
				}
				if(compareBit == -1){
					setofCases newset = new setofCases(conceptvalue,String.valueOf(i));
					setsofConcepts.add(newset);
				}
			}
		}
	}
	
	
	//check if consistency holds
	public boolean ifConsistency(){
		//compare setsofCases and setsofConcepts
		int sizeofConcept = setsofConcepts.size();
		int sizeofCases = setsofCases.size();
		int compareNo = -1;
		for(int i = 0;i < sizeofCases;i++){
			compareNo = -1;
			for(int j = 0;j < sizeofConcept;j++){
				if(setsofConcepts.get(j).getCaseNos().containsAll(setsofCases.get(i).getCaseNos())){
					compareNo = 0;
					break;
				}
			}
			if(compareNo == -1){
				return false;
			}
		}
		return true;
	}
	
	
	
	class setofCases{
		private ArrayList<String> caseNos;
		private String itemValue;
		
		public setofCases(String value,String caseNo){
			this.itemValue = value;
			caseNos = new ArrayList<String>();
			caseNos.add(caseNo);
		}
		
		//true: succeed false: fail
		public boolean addItemtoset(String value,String caseNo){
			if(this.itemValue.equals(value)){
				caseNos.add(caseNo);
				return true;
			}
			return false;
		}
		
		public ArrayList<String> getCaseNos(){
			return this.caseNos;
		}
		
		public String getItemValue(){
			return this.itemValue;
		}
	}
}
