package old;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import edu.ruc.FileReader;
import edu.ruc.FileWriter;

public class BaselineHDUPrepareNC {
	public static long ALLSUM;
	public static void prepare(String inputRank
			,String filebase,String outputBase,int PART) throws IOException{
		BufferedReader br=new FileReader(inputRank).getReader();
		BufferedWriter bwTrain=FileWriter.createWriter(outputBase+"train.txt");
		BufferedWriter bwTest=FileWriter.createWriter(outputBase+"test.txt");
		BufferedWriter bwTrainAC=FileWriter.createWriter(outputBase+"trainAC.txt");
		BufferedWriter bwTestAC=FileWriter.createWriter(outputBase+"testAC.txt");
		
		JSONArray jarray=JSONArray.parseArray(br.readLine());
		int order=0;
		int userOrder=0;
		int maxorder=jarray.size()*PART;
//		bw.write(maxorder+"\n");
		for(int i=jarray.size()-1;i>=0;i--){
			order++;
//			if(order>10){
//				break;
//			}
//			if(order%10!=1){
//				continue;
//			}
			if(order%1000==1){
				System.out.println(order);
			}
			JSONObject jobj=jarray.getJSONObject(i);
			String id=jobj.getString("userID");
			BufferedReader br2=new FileReader(filebase+id+".txt").getReader();
			if(isSatisfied(br2)){
				userOrder++;
				
				br2=new FileReader(filebase+id+".txt").getReader();
				doneRecData(br2,userOrder,bwTrain,bwTest,bwTrainAC,bwTestAC);
			}
			br2.close();
			
		}
		br.close();
		bwTrain.close();
		bwTest.close();
		bwTrainAC.close();
		bwTestAC.close();
	}
	public static boolean isSatisfied(
			BufferedReader br) throws IOException{
		JSONArray jarray=JSONArray.parseArray(br.readLine());
		br.close();
		String returnLine="";
		HashSet<Integer> set=new HashSet<>();
		ArrayList<Integer> arrayRecord=new ArrayList<>();
		int last=-1;
		for(int i=jarray.size()-1;i>=0;i--){
			
			
			JSONObject jobj=jarray.getJSONObject(i);
			int problem=Integer.parseInt(jobj.getString("problem"));
			if(problem==1000||problem>=3000){
				continue;
			}
			
			if(problem!=last){
				
				arrayRecord.add(problem);
				
			}
			last=problem;
		}
		
		
		if(arrayRecord.size()<10){
			System.out.println("no");
			return false;
		}
		return true;
	}
	private static void doneRecData(
			BufferedReader br,
			int userID,
			BufferedWriter bwTrain,
			BufferedWriter bwTest,
			BufferedWriter bwTrainAC,
			BufferedWriter bwTestAC) throws IOException{
		JSONArray jarray=JSONArray.parseArray(br.readLine());
		String returnLine="";
//		HashSet<Integer> set=new HashSet<>();
//		HashSet<Integer> setNAC=new HashSet<>();
//		HashSet<Integer> setAC=new HashSet<>();
		ArrayList<Integer> arrayRecord=new ArrayList<>();
		int last=-1;
		int order=0;
		int[] resultArray=new int[200000];
		HashSet<Integer> setTrainAC=new HashSet<>();
		HashSet<Integer> setTestAC=new HashSet<>();
		
		for(int i=jarray.size()-1;i>=0;i--){
			order++;

			JSONObject jobj=jarray.getJSONObject(i);
			int problem=Integer.parseInt(jobj.getString("problem"));
			String result=jobj.getString("result");
			if(problem==1000||problem>=3000){
				continue;
			}
			if(problem!=last){
				if(result.equals("Accepted")){	
					resultArray[arrayRecord.size()]=1;
				}
				arrayRecord.add(problem);
				
			}
			last=problem;	
			
		}
		int length=arrayRecord.size();
		order=0;
		
		for(int arr:arrayRecord){
			order++;
			if(order<=length*4/5){
				bwTrain.write(userID+" "+arr+" 1.0\n");
				if(resultArray[order-1]>0){
					setTrainAC.add(arr);
				}
			}else{
				bwTest.write(userID+" "+arr+" 1.0\n");
				if(resultArray[order-1]>0){
					if(!setTrainAC.contains(arr)){
						setTestAC.add(arr);
					}
					
				}
			}
		}
		Iterator<Integer> it=setTrainAC.iterator();
		while(it.hasNext()){
			bwTrainAC.write(userID+" "+it.next()+" 1.0\n");
		}
		 it=setTestAC.iterator();
		while(it.hasNext()){
			bwTestAC.write(userID+" "+it.next()+" 1.0\n");
		}
//		bwTrain.write("\n");
//		bwTest.write("\n");
//		bwTrainAC.write("\n");
//		bwTestAC.write("\n");
		
	
		
	}
	public static void construct(
			String inputRankJson,
			String filebase,
			String outputbase,
			String dataSource) throws IOException{
		String dataVersion="Baseline_"+dataSource+"_All5_";
//		String dataVersion="noConsecutive";
		int PART=1;
		
		
		String output=outputbase+"/"+dataVersion;
		String dfile=dataVersion+".dat";
		
		prepare(inputRankJson,
				filebase,output,PART);
	}
	public static void main(String[] args) throws IOException{
		String dataSource="HDU";
		String inputRankJson="../acm/data/authorRankList"+dataSource+".json";
		String filebase="../acm/data/html/submit"+dataSource+"/";
		String outputbase="data/Record/";
		String outputLinuxOrder="data/linux_order.txt";
//		prepare(inputRankJson,
//				filebase,outputbase);
		ALLSUM=0;
		construct(inputRankJson, filebase, outputbase,dataSource);
//		System.out.println(ALLSUM);
	}
}
