/**
 * 
 */
package Baseline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import edu.ruc.FileReader;
import edu.ruc.FileWriter;

/**
 * @author wenhui zhang
 *
 */
public class BaselineCalculate {
	public static double calculatePreTopK(
			ArrayList<Integer> array,
			int K){
		int numP=0;
		for(int i=0;i<array.size();i++){
			if(i>=K){
				break;
			}
			if(array.get(i)>0){
				numP++;
			}
		}
		return 1.0*numP/K;
		
	}
	public static double calculateMAPTopK(
			ArrayList<Integer> array,
			int K){
		double value=0.0;
		int numP=0;
		for(int i=0;i<array.size();i++){
			if(i>=K){
				break;
			}
			if(array.get(i)>0){
				numP++;
				value+=(numP*1.0/(i+1));
			}
		}
		return value/K;
		
	}
	
	public static void main(String[] args) throws IOException{
//		String input="data/Record/POJ/t_40/BPR-top-10-items.txt";
//		String inputTest="data/Record/POJ_All5_testAC.txt";
		String dataSource="POJ";
		int topN=100;
		int topicNum=40;
		String baselineModel="lda";
		int userAllNum=20100;
		int minTestACWord=50;
		int maxTestACWord=10000;
		String input="data/Record/"+dataSource+"/t_"+topicNum+"/"+baselineModel+"_"+dataSource+"_"+topicNum;
//		String inputTest="data/Record/POJ_All5_testAC.txt";
		String inputTest="data/Record/"+dataSource+"/"+dataSource+"_All5_testAC.txt";
		String output="data/Record/"+dataSource+"/t_"+topicNum+"/"+baselineModel+"_Log.txt";
		
		
		String inputTrain="data/Record/"+dataSource+"/"+dataSource+"_All5_trainAC.txt";
		BufferedReader br=new FileReader(input).getReader();
		BufferedReader brTest=new FileReader(inputTest).getReader();
		BufferedReader brTrain=new FileReader(inputTrain).getReader();
		BufferedWriter bw=FileWriter.createWriter(output);
		String line;
		double pTop1=0.0;
		double pTop3=0.0;
		double pTop5=0.0;
		double pTop10=0.0;
		double MAPTop1=0.0;
		double MAPTop3=0.0;
		double MAPTop5=0.0;
		double MAPTop10=0.0;
		int userOrder=0;
		int userNum=0;
		
//		int maxOrder=20;
		
		
		int[][] data=new int[userAllNum][topN];
		br.readLine();
		int topLoc=0;
		int lastUID=-1;
		while((line=br.readLine())!=null){
			String[] split=line.split(",");
			int uid=Integer.parseInt(split[0]);
			int problem=Integer.parseInt(split[1]);
			if(uid!=lastUID){
				topLoc=0;
			}
			data[uid-1][topLoc++]=problem;
			lastUID=uid;
			
			
		}
		
		
		String lineTrain;
		int uid=-1;
		while((lineTrain=brTrain.readLine())!=null){
			uid++;
			String[] splitTrain=lineTrain.split("\t");
			HashSet<Integer> setTrainAC=new HashSet<>();
			for(String str:splitTrain){
				if(str.length()>0){
				setTrainAC.add(Integer.parseInt(str));
				}
			}
			String lineTest=brTest.readLine();
			String[] splitTest=lineTest.split("\t");
			HashSet<Integer> setTest=new HashSet<>();
			for(String str:splitTest){
				if(str.length()>0){
				setTest.add(Integer.parseInt(str));
				}
			}
//			System.out.println(setTest.size());
			if(setTest.size()<minTestACWord
					||setTest.size()>maxTestACWord){
				continue;
			}
			
			bw.write(uid+"\t\t");
			userNum++;
			ArrayList<Integer> arrayResult=new ArrayList<>();
			
			for(int i=0;i<topN;i++){
				int word=data[uid][i];
				bw.write(word+":");
				if(setTrainAC.contains(word)){
					bw.write("-1\t");
					continue;
				}
				
				if(setTest.contains(word)){
					bw.write("1\t");
					arrayResult.add(1);
				}else{
					bw.write("0\t");
					arrayResult.add(0);
				}
			}
			
			bw.write("\n");
			
			
			pTop1+=calculatePreTopK(arrayResult, 1);
			pTop3+=calculatePreTopK(arrayResult, 3);
			pTop5+=calculatePreTopK(arrayResult, 5);
			pTop10+=calculatePreTopK(arrayResult, 10);
//			
			MAPTop1+=calculateMAPTopK(arrayResult, 1);
			MAPTop3+=calculateMAPTopK(arrayResult, 3);
			MAPTop5+=calculateMAPTopK(arrayResult, 5);
			MAPTop10+=calculateMAPTopK(arrayResult, 10);

		}
		bw.close();
		System.out.println("userNum:"+userNum);
		System.out.println("p@1"+":"+pTop1/userNum);
		System.out.println("p@3"+":"+pTop3/userNum);
		System.out.println("p@5"+":"+pTop5/userNum);
		System.out.println("p@10"+":"+pTop10/userNum);
		System.out.println("MAP@1"+":"+MAPTop1/userNum);
		System.out.println("MAP@3"+":"+MAPTop3/userNum);
		System.out.println("MAP@5"+":"+MAPTop5/userNum);
		System.out.println("MAP@10"+":"+MAPTop10/userNum);
		
	}
}
