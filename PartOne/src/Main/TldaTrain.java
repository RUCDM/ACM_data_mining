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
public class TldaTrain {
	public static HashMap<String,Integer> constructMap(){
		HashMap<String,Integer> map=new HashMap<>();
//		map.put(1000+"",0);
		for(int i=1000;i<=6014;i++){
			map.put(i+"",(i-1000)/100);
		}
		return map;
	}
	public static int[] constructArray(int K){
		int[] arr=new int[K];
		for(int i=0;i<K;i++){
			arr[i]=100;
		}
		return arr;
	}
	public static void main(String[] args) throws IOException{
//		String inputDoc="data/lda/test.dat";
		System.out.println(args.length);
		if(args.length!=19){
			System.err.println("error");
			System.exit(0);
		}
		String version=args[0];
		String inputDoc=args[1];
		String delim=" ";
		int topicINum=Integer.parseInt(args[2]);
		int topicSNum=Integer.parseInt(args[3]);
		int featureNum=Integer.parseInt(args[4]);
		int iterations=Integer.parseInt(args[5]);
		int saveNum=Integer.parseInt(args[6]);
		double alphaI=Double.parseDouble(args[7]);
		double alphaS=Double.parseDouble(args[8]);
		double betaI=Double.parseDouble(args[9]);
		double betaS=Double.parseDouble(args[10]);
		double omega=Double.parseDouble(args[11]);
		double psiI=Double.parseDouble(args[12]);
		double psiS=Double.parseDouble(args[13]);
		double lamda=Double.parseDouble(args[14]);
		double learningRate=Double.parseDouble(args[15]);
		double limit=Double.parseDouble(args[16]);
		int  maxIteration=Integer.parseInt(args[17]);
		
//		int saveStep=Integer.parseInt(args[8]);
//		int beginSaveIters=Integer.parseInt(args[9]);
		String resultPath=args[18];
//		int mode=Integer.parseInt(args[11]);
		
		String filename=version+"_v_"+topicINum+
						"_z_"+topicSNum+
						"_aI_"+alphaI+
						"_aS_"+alphaS+
						"_bI_"+betaI+
						"_bS_"+betaS+
						"_pI_"+psiI+
						"_pS_"+psiS+
						"_lamda_"+lamda+
						"_lr_"+learningRate+
						"_lim_"+limit+
						"_mIt_"+maxIteration+
						"_it_"+iterations;
		BufferedWriter bw=FileWriter.createWriter(resultPath+filename+"/test.txt");
		bw.close();
		zwhModel nm=new zwhModel();
		Documents doc=new Documents();
		doc.readDocs(inputDoc,delim);
//		nm.initModel(topicNum, volumnNum, 
//				featureNum, iterations, 
//				saveNum, alphaI, alphaS, beta, 
//				omega, psi, lamda, learningRate, 
//				limit, maxIteration, doc, constructMap(), resultPath+filename+"/lda");
		nm.initModel(topicINum, topicSNum, 
				featureNum, iterations, 
				saveNum, alphaI, alphaS, 
				betaI,betaS, omega,psiI,psiS, lamda, learningRate, 
				limit, maxIteration, 
				doc, constructMap(), 
				resultPath+filename+"/lda");
//		nm.initModel(K1, K2, 
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
