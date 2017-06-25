/**
 * 
 */
package DataStruct;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;




/**
 * @author wenhui zhang
 
 */
public class Documents {

	public ArrayList<Document> docs; 
	public HashMap<String, Integer> termToIndexMap;
	public ArrayList<String> indexToTermMap;
	public HashMap<String,Integer> termCountMap;
	
	public Documents(){
		docs = new ArrayList<Document>();
		termToIndexMap = new HashMap<String, Integer>();
		indexToTermMap = new ArrayList<String>();
		termCountMap = new HashMap<String, Integer>();
	}
	public void readDocs(String inputDoc) throws IOException{
		System.out.println("read docs");
		BufferedReader br=new BufferedReader(
				new InputStreamReader(new FileInputStream(inputDoc),"utf-8"));
		String line;
		while((line=br.readLine())!=null){
			StringTokenizer st=new StringTokenizer(line,"\t");
			ArrayList<String> words=new ArrayList<>();
			while(st.hasMoreTokens()){
				words.add(st.nextToken());
			}
			Document doc = new Document(words, termToIndexMap, words, termCountMap);
			docs.add(doc);
		}
		System.out.println(docs.size()+" docs\t"
				+indexToTermMap.size()+" words");
	}
	public void readDocs(String inputDoc,String delim) throws IOException{
		System.out.println("read docs");
		
		BufferedReader br=new BufferedReader(
				new InputStreamReader(new FileInputStream(inputDoc),"utf-8"));
		String line;
		while((line=br.readLine())!=null){
			StringTokenizer st=new StringTokenizer(line,delim);
			ArrayList<String> words=new ArrayList<>();
			while(st.hasMoreTokens()){
				words.add(st.nextToken());
			}
			Document doc = new Document(words, termToIndexMap,indexToTermMap , termCountMap);
			docs.add(doc);
		}
		br.close();
		System.out.println(docs.size()+" docs\t"
				+indexToTermMap.size()+" words");
	}
}	
