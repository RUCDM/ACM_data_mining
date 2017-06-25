package edu.ruc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileReader {
	private BufferedReader br;
	public FileReader(String filename) throws IOException{
		br=new BufferedReader(new InputStreamReader(new 
				FileInputStream(filename),"utf-8"));
	}
	public FileReader(String filename,String code) throws IOException{
		br=new BufferedReader(new InputStreamReader(new 
				FileInputStream(filename),code));
	}
	public BufferedReader getReader(){
		return this.br;
	}
}
