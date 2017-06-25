/**
 * 
 */
package Main;

import java.io.BufferedWriter;
import java.io.IOException;

import Model.recommendBasket;
import Model.recommendNextPosition;
import edu.ruc.FileWriter;
import old.recommend;

/**
 * @author wenhui zhang
 *
 */
public class recommendPositionMain {
	public static void main(String[] args) throws IOException{
		int topicNumS=20;
		String dataSource="Timus";
		int topicNumI=12; 
		int windowSize=5;
		int cv=10;
		double windowMaxValue=10.0;
		double omega=0.01;
//		int minTestAC=1;
//		int maxTestAC=100000;
		double alpha=0.1;
//		double maxWindow=30.0;
		
		String filebasePartOne="data/recommendNextPosition/"+dataSource+"/t_"+topicNumS+"/";
		String filebaseData="data/record/Sequence_"+dataSource+"_cv"+cv+"_";
		String fileThetaI=filebasePartOne+"lda_final.thetaI"; 
		String fileThetaS=filebasePartOne+"lda_final.thetaS";
		String filePhiI=filebasePartOne+"lda_final.phiI";
		String filePhiS=filebasePartOne+"lda_final.phiS";
		String fileTopicAssign=filebasePartOne+"lda_final.assignTopic";
		String fileStateAssign=filebasePartOne+"lda_final.assignStates";
		String fileRho=filebasePartOne+"lda_final.rho";
		
		String fileWordMap=filebasePartOne+"lda_wordmap.txt";
		String fileTestData=filebaseData+"test.txt"; 
		String fileTrainData=filebaseData+"train.txt";
		String fileTestACData=filebaseData+"testAC.txt"; 
		String fileTrainACData=filebaseData+"trainAC.txt";
		String fileBasePartTwo="../PartTwoModel/data/"+dataSource+"ALL/";
//		String fileUserSample="Sample10_";
		String fileUserSample="";
		String fileUserBase= fileBasePartTwo
				+"t_"+topicNumS+"_n_1_userBase"+fileUserSample+".txt";
		String fileUserTopicExpert= fileBasePartTwo
				+"t_"+topicNumS+"_n_1_userTopicExpert"+fileUserSample+".txt";
		String fileQuestionBase= fileBasePartTwo
				+"t_"+topicNumS+"_n_1_questionBase.txt";
		String fileQuestionTopicExpert= fileBasePartTwo
				+"t_"+topicNumS+"_n_1_questionTopicExpert.txt"; 
		String fileWeight= fileBasePartTwo
				+ "t_"+topicNumS+"_n_1_wight.txt";
		
		
		String inputBase="../acm/data/zwhLda/"+dataSource+"/t_"+topicNumS+"/";
		
		String fileQuestionTopicDistriFile=inputBase+"lda_final.qToTopic";
		String fileOutput=filebasePartOne+"rec.result";
		BufferedWriter bw=FileWriter.createWriter(fileOutput);
		recommendNextPosition rec=new recommendNextPosition();
		rec.initModel(topicNumI, 
				topicNumS, windowSize, 
				windowMaxValue, alpha, 
				omega, fileThetaI, 
				fileThetaS, filePhiS, 
				filePhiI, fileWordMap, 
				fileTestData, fileTrainData, 
				fileTestACData, fileTrainACData, 
				fileUserBase, fileUserTopicExpert, 
				fileQuestionBase, fileQuestionTopicExpert,
				fileWeight, fileQuestionTopicDistriFile,
				fileTopicAssign, 				
				fileStateAssign, 
				fileRho,bw);
		
		rec.recommendNextPositionAC();
	}
}
