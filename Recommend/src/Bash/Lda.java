/**
 * 
 */
package Bash;

import java.io.BufferedWriter;
import java.io.IOException;

import edu.ruc.FileWriter;

/**
 * @author wenhui zhang
 *
 */
public class Lda {
	public static void main(String[] args) throws IOException{
		int topicNum=40;
		int cv=5;
		String dataSource="POJ";
		String output="data/bash/Random_Lda_"+dataSource+"_"+topicNum+"_cv"+cv+".config";
		BufferedWriter bw=FileWriter.createWriter(output);
		bw.write("dataset.ratings.lins=./demo/Datasets/Zwh/"
				+ "Random_Baseline_"+dataSource+"_cv"+cv+"_trainAC.txt\n"
				+ "ratings.setup=-columns 0 1 -threshold 0\n"
				+ "recommender=LDA\n"
				+"evaluation.setup=test-set -f "
				+ "/demo/Datasets/Zwh/Random_Baseline_"+dataSource+"_cv"+cv+"_testAC.txt\n"
				+ "item.ranking=on -topN 3000 -ignore -1\n"
				+ "num.factors="+topicNum
				+ "\nnum.max.iter=3000\n"
				+ "pgm.setup=-alpha 2 -beta 0.5 -burn-in 1000 -sample-lag 100 -interval 100\n"
				+ "output.setup=on -dir ./demo/Results/"+dataSource+"/t_"+topicNum+"/\n");
		bw.close();
		String linux_Order=
				"nohup java -jar librec.jar -c demo/zwhConfig/"
				+"Random_Lda_"+dataSource+"_"+topicNum+"_cv"+cv+".config"+
						" > myoutLda_"+dataSource+"_"+topicNum+
						".txt 2>&1 &";
		System.out.println(linux_Order);
				
	}
}
