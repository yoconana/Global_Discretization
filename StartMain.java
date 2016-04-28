import java.util.Scanner;



public class StartMain {
	
	private final static int EqualIntervalwidth_Way = 1;
	private final static int EqualFrequencyPerInterval_Way = 2;
	private final static int ConditionalEntroy_Way = 3;
	
	private static int ChoosenMethod;
	
	
	public static void main(String[] args){
		FileManager fileManager = new FileManager();
		
		//fileManager.ReadAdvanced("res/common_combined_lers.d");
		
		   Scanner in = new Scanner(System.in);
		   boolean state_checker = true;
		   
		   int erroNum = 0;
		   String filenameshort = "";
		   String filename = "";
		   String[][] filecontent = null;
		   String interString = "";
		   while(state_checker){
			// Reads a single line from the console 
		       // and stores into name variable
			   
			   if(erroNum == 0){
				   System.out.println("Please make sure you put your input data files in res folder.");
				   System.out.println("Please intput the name of your input data file(for example, test.d): ");
				   filenameshort = in.nextLine();
//				   if(!filenameshort.contains(".d")){
//					   System.out.println("Error! Please input a file name like test.d.");
//					   erroNum = 0;
//					   continue;
//				   }
				   if(filenameshort.endsWith(".d")||filenameshort.endsWith(".txt")||filenameshort.endsWith(".lers")){
					   filename = "./res/"+filenameshort;
					   //debug
					   filecontent = fileManager.ReadFileBytebyByte(filename);
					   //debug
					   if(filecontent == null){
							System.out.println("ReadFile Failed, please try again.");
							erroNum = 0;
							continue;
					   }
				   }
				   else{
					   System.out.println("Error! Please input a file name like test.d.");
					   erroNum = 0;
					   continue;
				   }
			   }
			   
			   System.out.println("Please intput the number of the method: ");
			   System.out.println("1. equal interval width");
			   System.out.println("2. equal frequency per interval");
			   System.out.println("3. conditional entropy");
			   System.out.println("4. exit");

			       // Reads a integer from the console
			       // and stores into age variable
			   interString = in.nextLine();
			   if(!Utils.isInteger(interString)){
				   System.out.println("Please choose the correct method.");
					erroNum = 1;
					continue;
			   }
			   ChoosenMethod = Integer.valueOf(interString);
			   if(ChoosenMethod != 1&&ChoosenMethod != 2&&ChoosenMethod != 3&&ChoosenMethod != 4){
			    	   System.out.println("Please choose the correct method.");
			    	   erroNum = 1;
			    	   continue;
			   }
			   in.close(); 
				
			   //String[] attributes = filecontent[1].replaceAll("\\s+", " ").split("[ |\t]");
				
				
			   DecisionTable dTable = new DecisionTable(filecontent);
				
			   String cutFileName = filenameshort.replaceAll(".d", "").replaceAll(".txt", "").replaceAll(".lers", "");
				switch(ChoosenMethod){
				case EqualIntervalwidth_Way:
					Equialintervalwidth widthDiscre = new Equialintervalwidth(dTable,cutFileName);
					widthDiscre.doDiscretization();
					break;
				case EqualFrequencyPerInterval_Way:
					EqualFrequencyPerInterval freqDiscre = new EqualFrequencyPerInterval(dTable,cutFileName);
					freqDiscre.doDiscretization();
					break;
				case ConditionalEntroy_Way:
					ConditionalEntropy condDiscre = new ConditionalEntropy(dTable,cutFileName);
					condDiscre.doDiscretization();
					break;
				case 4:
					break;
				default:
					System.out.println("Please Choose the Correct Method, thank you!");
					break;
				}	
				state_checker = false;
		   }		
	}
}
