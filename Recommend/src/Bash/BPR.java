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
public class BPR {
	public static void main(String[] args) throws IOException{
		int topicNum=40;
		String dataSource="POJ";
		String output="data/bash/BPR_"+dataSource+"_"+topicNum+".config";
		BufferedWriter bw=FileWriter.createWriter(output);
		bw.write("dataset.ratings.lins=./demo/Datasets/Zwh/"
				+ "Baseline_"+dataSource+"_All5_trainAC.txt\n"
				+ "ratings.setup=-columns 0 1 -threshold 0\n"
				+ "recommender=BPR\n"
				+"evaluation.setup=test-set -f "
				+ "/demo/Datasets/Zwh/Baseline_"+dataSource+"_All5_testAC.txt\n"
				+ "item.ranking=on -topN 3000 -ignore -1\n"
				+ "num.factors="+topicNum
				+ "\nnum.max.iter=30\n"
				+ "learn.rate=0.01 -max -1 -bold-driver\n"
				+"reg.lambda=0.1 -u 0.1 -i 0.1 -b 0.1\n"
				+ "output.setup=on -dir ./demo/Results/"+dataSource+"/t_"+topicNum+"/\n");
		bw.close();
	}
}
