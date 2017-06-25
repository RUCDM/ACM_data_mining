/**
 * 
 */
package Main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import DataStruct.Documents;
import FileIO.FileWriter;
import Model.zwhModel;

/**
 * @author wenhui zhang
 *
 */
public class TldaTrainDebug {
	public static HashMap<String,Integer> constructMap(){
		HashMap<String,Integer> map=new HashMap<>();
		for(int i=1;i<13;i++){
			map.put(i+"",(i-1)/4);
		}
		return map;
	}
	
	public static void main(String[] args) throws IOException{
//		String inputDoc="data/lda/test.dat";
		System.out.println(args.length);
		if(args.length!=11){
			System.err.println("error");
			System.exit(0);
		}
		String inputDoc=args[0];
		String delim=" ";
		int K1=Integer.parseInt(args[1]);
		int K2=Integer.parseInt(args[2]);
//		int topicNum=40;
		double alpha=Double.parseDouble(args[3]);
		double betaI=Double.parseDouble(args[4]);
		double betaS=Double.parseDouble(args[5]);
		double gamma=Double.parseDouble(args[6]);
		double psi=Double.parseDouble(args[7]);
		int iterations=Integer.parseInt(args[8]);
		int saveNum=Integer.parseInt(args[9]);
//		int saveStep=Integer.parseInt(args[8]);
//		int beginSaveIters=Integer.parseInt(args[9]);
		String resultPath=args[10];
//		int mode=Integer.parseInt(args[11]);
		String version="test";
		String filename="_K1_"+K1+
						"_K2_"+K2+
						"_a_"+alpha+
						"_bI_"+betaI+
						"_bS_"+betaS+
						"_eps_"+gamma+
						"_rho_"+psi+
						"_it_"+iterations;
		BufferedWriter bw=FileWriter.createWriter(resultPath+filename+"/test.txt");
		bw.close();
		zwhModel nm=new zwhModel();
		Documents doc=new Documents();
		doc.readDocs(inputDoc,delim);
		System.out.println("start init");
////		nm.initModel(K1, K2, 
//				iterations,saveNum, alpha, betaS, betaI, 
//				gamma,psi,doc,
//				constructMap(),
//				resultPath+filename+"/lda");
		nm.infer();
		
//		String resultPath="data/lda/ldaResult71/";
		
////		TLdaModel lm=new TLdaModel(
//				K1,K2, alpha, betaI,
//				betaS,
//				gamma,
//				iterations, saveStep,
//				beginSaveIters, resultPath+filename+"/");
//		lm.estimate(mode,doc,constructMap(),constructArray(K1));
	}

}
