package FileIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileWriter {
	private BufferedWriter bw;
	public FileWriter(String filename) throws IOException{
		this.bw=new BufferedWriter(new OutputStreamWriter(new 
				FileOutputStream(filename)));
		
	}
	public FileWriter(String filename,boolean plus) throws IOException{
		this.bw=new BufferedWriter(new OutputStreamWriter(new 
				FileOutputStream(filename,plus)));
	}
	public BufferedWriter getWriter(){
		return this.bw;
	}
	public static BufferedWriter createWriter(String filename) throws IOException{
		File file=new File(filename);
		if(!file.getParentFile().exists()) {  
			
			if(!file.getParentFile().mkdirs()) {  
	            System.out.println("创建目标文件所在目录失败！");  
	            return null;
			} 
		}
		BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(new 
				FileOutputStream(filename)));
		return bw;
	}
	
}
