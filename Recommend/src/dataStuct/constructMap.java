package dataStuct;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import edu.ruc.FileReader;

public class constructMap {
	public static HashMap<Integer,Double> getMapIntDouble(String input) throws IOException{
		HashMap<Integer,Double> map=new HashMap<>();
		BufferedReader br=new FileReader(input).getReader();
		String line;
		while((line=br.readLine())!=null){
			StringTokenizer st=new StringTokenizer(line,"\t");
			String pro=st.nextToken();
			String score=st.nextToken();
			map.put(Integer.parseInt(pro), Double.parseDouble(score));
		}
		return map;
	}
	public static HashMap<Integer,String> getMapIntStr(String input) throws IOException{
		HashMap<Integer,String> map=new HashMap<>();
		BufferedReader br=new FileReader(input).getReader();
		String line;
		while((line=br.readLine())!=null){
			StringTokenizer st=new StringTokenizer(line,"\t");
			String pro=st.nextToken();
			String score=st.nextToken();
			map.put(Integer.parseInt(pro),score);
		}
		return map;
	}
	public static HashMap<String,String> getMapStrStr(String input) throws IOException{
		HashMap<String,String> map=new HashMap<>();
		BufferedReader br=new FileReader(input).getReader();
		String line;
		while((line=br.readLine())!=null){
			StringTokenizer st=new StringTokenizer(line,"\t");
			String key=st.nextToken();
			String value=st.nextToken();
			map.put(key, value);
		}
		return map;
	}
}
