/**
 * 
 */
package evaluationBaseline;

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
	private static double calculateRecallTopK(
			ArrayList<Integer> array,
			int K,int allSize){
		int numP=0;
		for(int i=0;i<array.size();i++){
			if(i>=K){
				break;
			}
			if(array.get(i)>0){
				numP++;
			}
		}
		return 1.0*numP/allSize;
		
	}
	
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
		int maxWord=2000;
		int topicNum=40;
		String baselineModel="LDA";
		int userAllNum=20100;
		int minTestACWord=1;
		int maxTestACWord=1000000;
		int cv=5;
		String input="data/recommendBasket/"+dataSource+"/t_"+topicNum+"/"+baselineModel+"-top-10-items.txt";
//		String inputTest="data/Record/POJ_All5_testAC.txt";
		String fileBase="data/Record/Random_Baseline_"+dataSource+"_cv"+cv+"_";
//		String fileBase="data/Record/Random_"+dataSource+"_cv"+cv+"_";
		
		String inputTrain=fileBase+"trainAC.txt";
		
		String inputTest=fileBase+"testAC.txt";
		String output="data/recommendBasket/"+dataSource+"/t_"+topicNum+"/"+baselineModel+"_baselineResult.txt";
		
		
		BufferedReader br=new FileReader(input).getReader();
		BufferedReader brTest=new FileReader(inputTest).getReader();
		BufferedReader brTrain=new FileReader(inputTrain).getReader();
		BufferedWriter bw=FileWriter.createWriter(output);
		String line;
		double pTop1=0.0;
		double pTop3=0.0;
		double pTop5=0.0;
		double pTop10=0.0;
		double rTop1=0.0;
		double rTop3=0.0;
		double rTop5=0.0;
		double rTop10=0.0;
		double MAPTop1=0.0;
		double MAPTop3=0.0;
		double MAPTop5=0.0;
		double MAPTop10=0.0;
		int userOrder=0;
		int userNum=0;
		
//		int maxOrder=20;
		
		
		int[][] data=new int[userAllNum][maxWord];
		br.readLine();
		
		while((line=br.readLine())!=null){
			int locOrder=0;
			int numIndex=line.indexOf(":");
//			System.out.println(line);
			int uid=Integer.parseInt(line.substring(0,numIndex));
			
			while(true){
				if(locOrder>=maxWord){
					break;
				}
				int index=line.indexOf("(");
				if(index<0){
					break;
				}
				
				int word=Integer.parseInt(line.substring(index+1, index+5));
				line=line.substring(index+5);
				data[uid-1][locOrder++]=word;
//				
				
				
				
			}
		}
		
		
		String lineTrain;
//		int uid=-1;
		int lastid=-1;
		int[][] dataTrainAC=new int[userAllNum][maxWord];
		int[][] dataTestAC=new int[userAllNum][maxWord];
		
		int nowLoc=0;
		while((lineTrain=brTrain.readLine())!=null){
			
			String[] splitTrain=lineTrain.split(" ");
			int nowId=Integer.parseInt(splitTrain[0]);
			int qid=Integer.parseInt(splitTrain[1]);
			if(nowId==lastid){
				dataTrainAC[nowId-1][nowLoc++]=qid;
			}else{
				nowLoc=0;
				dataTrainAC[nowId-1][nowLoc++]=qid;
				
			}
			lastid=nowId;
		}
		String lineTest;
		while((lineTest=brTest.readLine())!=null){
			
			String[] splitTest=lineTest.split(" ");
			int nowId=Integer.parseInt(splitTest[0]);
			int qid=Integer.parseInt(splitTest[1]);
			if(nowId==lastid){
				dataTestAC[nowId-1][nowLoc++]=qid;
			}else{
				nowLoc=0;
				dataTestAC[nowId-1][nowLoc++]=qid;
				
			}
			lastid=nowId;
		}
		userNum=0;
		for(int u=0;u<userAllNum;u++){
			if(data[u][0]>0&&dataTestAC[u][0]>0){
				
				HashSet<Integer> setTrainAC=new HashSet<>();
				for(int top=0;top<maxWord;top++){
					if(dataTrainAC[u][top]<=0){
						break;
					}
					setTrainAC.add(dataTrainAC[u][top]);
				}
				HashSet<Integer> setTest=new HashSet<>();
				for(int top=0;top<maxWord;top++){
					if(dataTestAC[u][top]<=0){
						break;
					}
					setTest.add(dataTestAC[u][top]);
				}
				if(setTest.size()<minTestACWord
						||setTest.size()>maxTestACWord){
					continue;
				}
				bw.write((u+1)+"\t\t");
				userNum++;
				ArrayList<Integer> arrayResult=new ArrayList<>();
				
				for(int i=0;i<maxWord;i++){
					int word=data[u][i];
					if(setTrainAC.contains(word)){
						continue;
					}
					bw.write(word+":");
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
				rTop1+=calculateRecallTopK(arrayResult, 1,setTest.size());
				rTop3+=calculateRecallTopK(arrayResult, 3,setTest.size());
				rTop5+=calculateRecallTopK(arrayResult, 5,setTest.size());
				rTop10+=calculateRecallTopK(arrayResult, 10,setTest.size());
				
				MAPTop1+=calculateMAPTopK(arrayResult, 1);
				MAPTop3+=calculateMAPTopK(arrayResult, 3);
				MAPTop5+=calculateMAPTopK(arrayResult, 5);
				MAPTop10+=calculateMAPTopK(arrayResult, 10);

				
			}
				
		}
			
			
			
			
		
		bw.close();
		System.out.println(userNum);
		System.out.println(pTop1/userNum);
		System.out.println(pTop3/userNum);
		System.out.println(pTop5/userNum);
		System.out.println(pTop10/userNum);
		System.out.println();
		System.out.println(rTop1/userNum);
		System.out.println(rTop3/userNum);
		System.out.println(rTop5/userNum);
		System.out.println(rTop10/userNum);
		System.out.println();
		System.out.println(MAPTop1/userNum);
		System.out.println(MAPTop3/userNum);
		System.out.println(MAPTop5/userNum);
		System.out.println(MAPTop10/userNum);
//		System.out.println("userNum:"+userNum);
//		System.out.println("p@1"+":"+pTop1/userNum);
//		System.out.println("p@3"+":"+pTop3/userNum);
//		System.out.println("p@5"+":"+pTop5/userNum);
//		System.out.println("p@10"+":"+pTop10/userNum);
//		System.out.println("MAP@1"+":"+MAPTop1/userNum);
//		System.out.println("MAP@3"+":"+MAPTop3/userNum);
//		System.out.println("MAP@5"+":"+MAPTop5/userNum);
//		System.out.println("MAP@10"+":"+MAPTop10/userNum);
		
	}
}
