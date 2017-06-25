/**
 * 
 */
package Main;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import java.io.IOException;

import FileIO.FileReader;
import FileIO.FileWriter;

/**
 * @author wenhui zhang
 *
 */
public class SampleUserExpert {
	public static void sample(String input,
			String output) throws IOException{
//		String input="data/POJALL/t_40_n_1_userTopicExpert.txt";
//		String output="data/POJALL/Sample10_t_40_n_1_userTopicExpert.txt";
		BufferedReader br=new FileReader(input).getReader();
		BufferedWriter bw=FileWriter.createWriter(output);
		int order=0;
		String line;
		while((line=br.readLine())!=null){
			order++;
			if(order%10!=1){
				continue;
			}
			bw.write(line+"\n");
		}
		br.close();
		bw.close();
	}
}
