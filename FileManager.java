import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class FileManager {
	
	public String[][] ReadFileBytebyByte(String filepath){
		File file = new File(filepath);
		if(file == null||!file.isFile()){
			return null;
		}
		//String[] Parts = null;
		FileReader fileReader;
		
		//String[] results = null;
		String[][] newresults = null;
		
		try {
			System.out.println("Starting Reading the File...");
			fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			int tempindex;
			//int lineNo = 1;
			while ((line = bufferedReader.readLine()) != null){
				if(line.contains("!")){
			    	//check comment
					tempindex = line.indexOf("!");
					if(tempindex == 0){
						continue;
					}
					else{
				    	line = line.substring(0,tempindex);
				    	
				    	if(line == null||line.length() == 0){
				    		continue;
				    	}
					}
			    }
				if(line.length() == 0){
					continue;
				}
				if(line.endsWith("\t")){
					line = line.replaceAll("\t", " ").replaceAll("\\s+", " ");
					stringBuffer.append(line);
					//stringBuffer.append(" ");
				}
				else if(line.endsWith(" ")){
					line = line.replaceAll("\t", " ").replaceAll("\\s+", " ");
					stringBuffer.append(line);
				}
				else{
					line = line.replaceAll("\t", " ").replaceAll("\\s+", " ");
					stringBuffer.append(line);
					stringBuffer.append(" ");
				}
//				line = line.replaceAll("\t", " ").replaceAll("\\s+", " ");
//				stringBuffer.append(line);
//				stringBuffer.append(" ");		
				//lineNo++;
			}
			fileReader.close();	
			String longBuffer = stringBuffer.toString();
			String title = longBuffer.substring(longBuffer.indexOf('<')+2, longBuffer.indexOf('>')-1);
			String titleArray[] = title.split(" ");
			int attrConcepCount = titleArray.length;
			String[] splitValues = longBuffer.split(" ");
			
			//writeStringtoFile("./pppp.data",longBuffer.replaceAll(" ", "\n"));
			int splitsize = splitValues.length;
			int caseNo = 0;
			int startindex = attrConcepCount*2+4;
			String[] tempString = new String[attrConcepCount];
			if((splitsize-4)%attrConcepCount == 0){
				
				caseNo = (splitsize-4)/attrConcepCount-2;
				newresults = new String[caseNo+2][attrConcepCount];
				newresults[0] = title.split(" ");
				newresults[1] = longBuffer.substring(longBuffer.indexOf('[')+2, longBuffer.indexOf(']')-1).split(" ");
				
				for(int i = 0;i < caseNo;i++){
					for(int j = 0;j < attrConcepCount;j++){
						newresults[i+2][j] = splitValues[startindex+i*attrConcepCount+j];
					}
				}
				//System.out.println();
//				results = new String[caseNo+2];
//				results[0] = "< "+title+" >";
//				results[1] = longBuffer.substring(longBuffer.indexOf('['), longBuffer.indexOf(']')+1);
//				for(int i = 0;i < caseNo;i++){
//					tempString = Arrays.copyOfRange(splitValues, startindex+i*attrConcepCount, startindex+(i+1)*attrConcepCount);
//					results[i+2] = "";
//					for(int j = 0;j < attrConcepCount;j++){
//						if(j == attrConcepCount-1){
//							results[i+2] += tempString[j];
//						}
//						else{
//							results[i+2] += tempString[j]+" ";
//						}
//						
//					}
//				}
			}
			else{
				return null;
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Reading Completed.");
		return newresults;
	}
	
	
	
	public String[] ReadLinebyLine(String filepath){
		File file = new File(filepath);
		if(file == null||!file.isFile()){
			return null;
		}
		String[] Parts = null;
		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			StringBuffer stringBuffer = new StringBuffer();
			String line;
			int tempindex;

			while ((line = bufferedReader.readLine()) != null) {
//				if(line.contains(">")){
//					System.out.println(line);
//				}
				if(line.contains("!")){
			    	//check comment
					tempindex = line.indexOf("!");
					if(tempindex == 0){
						continue;
					}
					else{
				    	line = line.substring(0,tempindex);
				    	
				    	if(line == null||line.length() == 0){
				    		continue;
				    	}
					}
			    }
				
				stringBuffer.append(line);
				
				stringBuffer.append(" ");
			}
			fileReader.close();			
			Parts = stringBuffer.toString().split("\n");
			//Utils.printlnLogs(stringBuffer.toString());
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("File not found!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//System.out.println("File not found!");
		}
		return Parts;
		
	}
	
	public static void writeIntervalstoFile(String filename,String content){
		writeStringtoFile(filename+".int",content);
		
	}
	
	public static void writeTabletoFile(String filename,String content){
		writeStringtoFile(filename+".data",content);
	}
	
	
	public static void writeStringtoFile(String filename,String content){

		String path = "./results/"+filename;
		
		File f = new File(path);
		//(works for both Windows and Linux)
		if(f.isFile()){
			//fine.rewrite
		}
		else{
			f.getParentFile().mkdirs(); 
			try {
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			Files.write(Paths.get(path), content.getBytes());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
