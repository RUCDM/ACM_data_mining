/**
 * 
 */
package dataStuct;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import edu.ruc.FileReader;
import edu.ruc.FileWriter;

/**
 * @author wenhui zhang
 *
 */
public class shuffleData {
	public static void shuffle(String inputFile,String outputFile) throws IOException{
		
		BufferedReader br=new FileReader(inputFile).getReader();
		BufferedWriter bw=FileWriter.createWriter(outputFile);
		ArrayList<String> array=new ArrayList<>();
		String line;
		while((line=br.readLine())!=null){
			array.add(line);
		}
		Collections.shuffle(array);
		for(String str:array){
			bw.write(str+"\n");
		}
		br.close();
		bw.close();
	}
}
