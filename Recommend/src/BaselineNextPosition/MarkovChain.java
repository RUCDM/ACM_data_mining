/**
 * 
 */
package BaselineNextPosition;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import dataStuct.SortPairDouble;
import edu.ruc.FileReader;

/**
 * @author wenhui zhang
 *
 */
public class MarkovChain {
	
	public static void main(String[] args) throws IOException{
//		double[][] transProb=new double[4000][4000];
		ArrayList<HashSet<Integer>> arraySet=new ArrayList<>();
		int[] userTrainLast=new int[20100];
		String dataSource="Timus";
		int cv=10;
		String filebaseData="data/record/Sequence_"+dataSource+"_cv"+cv+"_";
		String fileTestData=filebaseData+"test.txt"; 
		String fileTrainData=filebaseData+"train.txt";
		String fileTestACData=filebaseData+"testAC.txt"; 
		String fileTrainACData=filebaseData+"trainAC.txt";
		
		
		calculateTransProb(fileTrainData, userTrainLast,arraySet,4000,10);
		calculateACHitRatio(fileTestACData, userTrainLast, arraySet);
	}
	public static void calculateACHitRatio(String input,
			int[] userTrainLast,
			ArrayList<HashSet<Integer>> arraySet) throws IOException{
		BufferedReader br=new FileReader(input).getReader();
		String line;
		int userOrder=-1;
		long sum=0;
		long sumTrue=0;
		while((line=br.readLine())!=null){
			userOrder++;
			int order=0;
			int last=-1;
			String[] split=line.split("\t");
			for(String str:split){
				
				if(str.length()>0){
					order++;
					int problem=Integer.parseInt(str)-1000;
					sum++;
					if(order==1){
						last=userTrainLast[userOrder];
					}
					HashSet<Integer> set=arraySet.get(last);
					if(set.contains(problem)){
						sumTrue++;
					}
					
				}
			}
		}
		System.out.println("Hit ratio:\n"+sumTrue*1.0/sum);
	}
	public static void calculateTransProb(String input,
			int[] userTrainLast,
			ArrayList<HashSet<Integer>> arraySet,
			int length,int TopN) throws IOException{
		BufferedReader br=new FileReader(input).getReader();
		
		int[][] numTrans=new int[length][length];
		int[] sumTrans=new int[length];
		int prior=1;
		String line;
		int userOrder=-1;
		while((line=br.readLine())!=null){
			userOrder++;
			String[] split=line.split(" ");
			int order=0;
			int last=-1;
			for(String str:split){
				
				if(str.length()>0){
					order++;
					int problem=Integer.parseInt(str)-1000;
					userTrainLast[userOrder]=problem;
					if(order==1){
						last=problem;
					}else{
						numTrans[last][problem]++;
						last=problem;
					}
					
				}
			}
		}
		for(int p1=0;p1<length;p1++){
			ArrayList<SortPairDouble> arrayRank=new ArrayList<>();
			for(int p2=0;p2<length;p2++){
				arrayRank.add(new SortPairDouble(p2,1.0*numTrans[p1][p2]));
			}
			Collections.sort(arrayRank);
			HashSet<Integer> set=new HashSet<>();
			for(int top=0;top<TopN;top++){
				set.add(arrayRank.get(top).key);
			}
			arraySet.add(set);
		}
		br.close();
	}
	
	
}
