/**
 * 
 */
package old;

import java.io.BufferedWriter;
import java.io.IOException;

import edu.ruc.FileWriter;

/**
 * @author wenhui zhang
 *
 */
public class recommendMain {
	public static void main(String[] args) throws IOException{
		int topicNumS=40;
		int topicNumI=12; 
		int minTestAC=0;
		int maxTestAC=100000;
		double alpha=0.01;
		double maxWindow=1000.0;
		String dataSource="Timus";
		String filebasePartOne="data/Record/"+dataSource+"/t_"+topicNumS+"/";
		String filebaseData="data/Record/"+dataSource+"/"+dataSource+"_All5_";
		String fileThetaI=filebasePartOne+"lda_final.thetaI"; 
		String fileThetaS=filebasePartOne+"lda_final.thetaS";
		String filePhiI=filebasePartOne+"lda_final.phiI";
		String filePhiS=filebasePartOne+"lda_final.phiS";
		
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
		recommend rec=new recommend();
		rec.initialize(topicNumS, topicNumI,
				minTestAC,
				maxTestAC,
				alpha, maxWindow, fileThetaI, 
				fileThetaS, filePhiS, 
				filePhiI, fileWordMap, 
				fileTestData, fileTrainData, 
				fileTestACData, fileTrainACData, 
				fileUserBase, fileUserTopicExpert,
				fileQuestionBase, 
				fileQuestionTopicExpert, 
				fileWeight, 
				fileQuestionTopicDistriFile,
				bw);
		rec.recommendQuestion();
	}
}
