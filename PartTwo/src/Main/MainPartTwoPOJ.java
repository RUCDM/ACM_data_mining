/**
 * 
 */
package Main;

import java.io.IOException;

import Model.PartTwo;

/**
 * @author wenhui zhang
 *
 */
public class MainPartTwoPOJ {
	public static void main(String[] args) throws IOException{
		for(int topic=0;topic<=40;){
			topic+=10;
			for(int n=5;n<=10;){
				n+=5;
			int numNAC=n;
		PartTwo zwhModel=new PartTwo();
		int userNum=20100;
		int playerNum=23154;
		int topicNum=topic;
		int questionNum=3055;
		int maxIteration=100000;
		int miniBatchNum=10000;
		double psi=10.0;
		double alpha=0.0;
		double beta1=0.00000001;
		double beta2=0.000001;
		double lamda=10000.0;
		double learningRate=0.1;
		double limit=0.00001;
		String dataSource="POJ";
		
		String dataVersion="Triple"+dataSource+"_UU_UPNAC"+numNAC;
		String tsFile="TS"+dataVersion+".dat";
		String comparisonFile=dataVersion+".dat";
//		String dataNeagative="NAC15";
//		String dataNeagative="";
//		String tripleFile="../acm/data/comparison/TriplePOJNAC15Shuffle.dat";
//		String tripleFile="../acm/data/comparison/TriplePOJ_UU_UPShuffle.dat";
		String tripleFile="../acm/data/comparison/"+comparisonFile;
		String priorFile="../acm/data/comparison/"+tsFile;
		
		String inputBase="../acm/data/zwhLda/"+dataSource+"/t_"+topicNum+"/";
		
		String questionTopicDistriFile=inputBase+"lda_final.qToTopic";
		String outputFileBase="data/"+dataSource+"ALL/" ;
		String outputResult="data/result/"+topic+"_result_"+dataVersion+".txt";
		zwhModel.initialize(
				outputResult,
				userNum,
				playerNum,topicNum, 
				questionNum,maxIteration, 
				miniBatchNum,
				psi,alpha, 
				beta1,beta2,lamda, 
				learningRate,limit, 
				tripleFile,questionTopicDistriFile, 
				priorFile,
				outputFileBase);
		zwhModel.optimize();
		}
		}
	}
}
