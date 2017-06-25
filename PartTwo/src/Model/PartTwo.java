/**
 * 
 */
package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

import DataStruct.SortPairDouble;
import DataStruct.TriplePairIntIntInt;
import DataStruct.doubleDecimal;
import FileIO.FileReader;
import FileIO.FileWriter;
import Main.SampleUserExpert;
import SGD.CostFunctionPartTwoModel;
import SGD.DoubleVector;
import SGD.GradientDescent;
import SGD.GradientDescent.GradientDescentBuilder;
import SGD.TestCostFunctionPartTwoModel;

/**
 * @author wenhui zhang
 *
 */
public class PartTwo{
	BufferedWriter bw_;
	private int userNum_;
	private int topicNum_;
	private int questionNum_;
	private int playerNum_;
	private int parameterNum_;
	private int maxIteraion_;
	private int miniBatchNum_;
	private double psi_;
	private double alpha_;
	private double beta1_;
	private double beta2_;
	private double lamda_;
	private double learningRate_;
	private double limit_;
	double TSaccuracyMicro_;
	double TSaccuracyUPP_;
	double TSaccuracyUPN_;
	double TSaccuracyUP_;
	double TSaccuracyUU_;
	double TSaccuracyMacro_;
	
	private String[] questionIndexToWord_;
	private HashMap<String,Integer> questionWordToIndex_;
	private String[] playerIndexToWord_;
	private HashMap<String,Integer> playerWordToIndex_;
	private ArrayList<TriplePairIntIntInt> arrayPairTrain_;
	private ArrayList<TriplePairIntIntInt> arrayPairTest_;
	private double[][] paraPlayerTopic_;
	private double[]	paraPlayerBase_;
	private double[][] questionTopicDistribution_;
	private double[] priorPlayerMean_;
	private double[] priorPlayerSigma_;
	
	private String outputFileBase_;
	public void optimize() throws IOException{
		double[] input=new double[this.parameterNum_];
		System.out.println("train size:"+this.arrayPairTrain_.size());
		initialParameter(input);
		DoubleVector pInput=new DoubleVector(input);
		GradientDescentBuilder gdBuilder=GradientDescentBuilder.create(this.learningRate_);
//		gdBuilder.boldDriverAverage();
		gdBuilder.breakOnDifference(this.limit_);
		BufferedWriter bw=FileWriter.createWriter(outputFileBase_+".log");
//		GradientDescent gd=new 
//				GradientDescent(gdBuilder,bw);
		double[] arrayAlpha=new double[parameterNum_];
		for(int i=0;i<playerNum_*topicNum_;i++){
			arrayAlpha[i]=this.learningRate_;
		}
		for(int i=playerNum_*topicNum_;i<(playerNum_+1)*topicNum_;i++){
			arrayAlpha[i]=this.learningRate_;
		}
		for(int i=(playerNum_+1)*topicNum_;i<parameterNum_;i++){
			arrayAlpha[i]=this.learningRate_/100;
		}
//		GradientDescent gd=new 
//				GradientDescent(gdBuilder,bw);
		GradientDescent gd=new 
				GradientDescent(gdBuilder,arrayAlpha,bw);
		
		System.out.println("start costFunction");
		CostFunctionPartTwoModel cfz=new 
				CostFunctionPartTwoModel(
//		TestCostFunctionPartTwoModel cfz=new 
//				TestCostFunctionPartTwoModel(
						this.arrayPairTrain_.size(), 
						this.miniBatchNum_,
						this.playerNum_, 
						this.parameterNum_,
						this.topicNum_, 
						this.arrayPairTrain_,
						this.questionTopicDistribution_, 
						priorPlayerMean_,
						priorPlayerSigma_,
						alpha_,
						lamda_, 
						beta1_, 
						beta2_);
		System.out.println("start minimize");
		
		
//		validation(pInput.vector, this.arrayPairTest_, 0.0);
		validationUUTrueSkill(pInput.vector, this.arrayPairTest_, 0.0);
		
		int numbase=1000;
		for(int i=0;i<this.maxIteraion_;i++){
			if(i%numbase==0){
				System.out.println(i);
				gd.minimizePartTwo(cfz, pInput,i,numbase,false);
//				validation(pInput.vector, this.arrayPairTest_, 0.0);
				validationUU(pInput.vector, this.arrayPairTest_, 0.0);
				
			}
			
		}
		bw=gd.bw;
		bw.close();
		validationUU(pInput.vector, this.arrayPairTest_, 0.0);
		BufferedWriter bwUserBase=FileWriter.createWriter(outputFileBase_+"userBase.txt");
		BufferedWriter bwUserE=FileWriter.createWriter(outputFileBase_+"userTopicExpert.txt");
		BufferedWriter bwQBase=FileWriter.createWriter(outputFileBase_+"questionBase.txt");
		BufferedWriter bwQE=FileWriter.createWriter(outputFileBase_+"questionTopicExpert.txt");
		
		BufferedWriter bwW=FileWriter.createWriter(outputFileBase_+"wight.txt");
//		int userNumIndex=this.playerWordToIndex_.get(this.userNum_+"");
		for(int p=0;p<this.userNum_;p++){
//			bw.write(this.playerIndexToWord_[p]+"\t"+this.priorPlayerMean_[p]+
//					"\tbase:"+pInput.get(this.playerNum_*this.topicNum_+p)+"\n");
			bwUserBase.write(pInput.get(this.playerNum_*this.topicNum_+p)+"\n");
			
			ArrayList<SortPairDouble> arraySort=new ArrayList<>();
			for(int t=0;t<this.topicNum_;t++){
//				double value=Math.abs(pInput.get(p*this.topicNum_+t)-this.priorPlayerMean_[p]);
				double value=pInput.get(p*this.topicNum_+t);
				
				arraySort.add(new SortPairDouble(t, value));
			}
//			Collections.sort(arraySort);
			for(int top=0;top<topicNum_;top++){
				int key=arraySort.get(top).key;
				bwUserE.write(pInput.get(p*this.topicNum_+top)+"\t");
			}
			bwUserE.write("\n");
		}
		bwUserBase.close();
		bwUserE.close();
		for(int p=this.userNum_;p<playerNum_;p++){
//			bw.write(this.playerIndexToWord_[p]+"\t"+this.priorPlayerMean_[p]+
//					"\tbase:"+pInput.get(this.playerNum_*this.topicNum_+p)+"\n");
			bwQBase.write((p-this.userNum_+1001)+"\t"+pInput.get(this.playerNum_*this.topicNum_+p)+"\n");
			
			ArrayList<SortPairDouble> arraySort=new ArrayList<>();
			for(int t=0;t<this.topicNum_;t++){
//				double value=Math.abs(pInput.get(p*this.topicNum_+t)-this.priorPlayerMean_[p]);
				double value=pInput.get(p*this.topicNum_+t);
				
				arraySort.add(new SortPairDouble(t, value));
			}
//			Collections.sort(arraySort);
			bwQE.write((p-this.userNum_+1001)+"\t");
			for(int top=0;top<topicNum_;top++){
				int key=arraySort.get(top).key;
				bwQE.write(pInput.get(p*this.topicNum_+top)+" ");
			}
			bwQE.write("\n");
		}
		bwQBase.close();
		bwQE.close();
		for(int t=0;t<topicNum_;t++){
			bwW.write(t+"\t"+pInput.get(this.playerNum_*(this.topicNum_+1)+t)+"\n");
		}
		bwW.close();
		this.bw_.close();
		SampleUserExpert.sample(outputFileBase_+"userTopicExpert.txt", outputFileBase_+"userTopicExpertSample10_.txt");
		SampleUserExpert.sample(outputFileBase_+"userBase.txt", outputFileBase_+"userBaseSample10_.txt");
		
	}
	
	public void initialize(
			String outputResult,
			int userNum,
			int playerNum,
			int topicNum,
			int questionNum,
			int maxIteration,
			int miniBatchNum,
			double psi,
			double alpha,
			double beta1,
			double beta2,
			double lamda,
			double learningRate,
			double limit,
			String tripleFile,
			String questionTopicDistriFile,
			String priorFile,
			String outputFileBase) throws IOException{
		this.bw_=FileWriter.createWriter(outputResult);
		this.userNum_=userNum;
		this.playerNum_=playerNum;
		this.topicNum_=topicNum;
		this.parameterNum_=this.playerNum_*(this.topicNum_+1)+topicNum_;
		this.questionNum_=questionNum;
		this.maxIteraion_=maxIteration;
		this.miniBatchNum_=miniBatchNum;
		this.psi_=psi;
		this.alpha_=alpha;
		this.beta1_=beta1;
		this.beta2_=beta2;
		this.lamda_=lamda;
		this.learningRate_=learningRate;
		this.limit_=limit;
		this.outputFileBase_=outputFileBase;
		this.paraPlayerBase_=new double[this.playerNum_];
		this.paraPlayerTopic_=new double[this.playerNum_][this.topicNum_];
		this.priorPlayerMean_=new double[this.playerNum_];
		this.priorPlayerSigma_=new double[this.playerNum_];
		this.questionIndexToWord_=new String[questionNum_];
		this.questionWordToIndex_=new HashMap<>();
		this.playerIndexToWord_=new String[playerNum_];
		this.playerWordToIndex_=new HashMap<>();
		this.arrayPairTrain_=new ArrayList<>();
		this.arrayPairTest_=new ArrayList<>();
		this.questionTopicDistribution_=new double[this.questionNum_][topicNum_];
		//player dic
		constrcutPlayerDic();
		//question dic and topic distri
		readQuestionTopicDistribution(questionTopicDistriFile);
		readTripleData(tripleFile);
		readPriorData(priorFile);
		
	}
	public void validation(
			double[] last,
			ArrayList<TriplePairIntIntInt> arrayTest,
			double threshold){
		int baseStartIndex=this.playerNum_*this.topicNum_;
		int weightStartIndex=this.playerNum_*(this.topicNum_+1);
		int numAll=arrayTest.size();
		int numN=0;
		int numP=0;
		int numTrue=0;
		int numTrueN=0;
		int numTrueP=0;
		int numFalseN=0;
		int numFalseP=0;
		for(TriplePairIntIntInt pair:arrayTest){
			int win=pair.a_;
			int lose=pair.b_;
			int context=pair.c_;
			double value=0.0;
			value+=(alpha_*(last[baseStartIndex+win]-last[baseStartIndex+lose])
					);
			for(int t=0;t<topicNum_;t++){
				double topicValue=(last[weightStartIndex+t]
						*questionTopicDistribution_[context][t]
						*(last[win*topicNum_+t]-last[lose*topicNum_+t]));
				value+=topicValue;
			}
			if(value>0.0){
				numTrue++;
			}
			if(win>lose){//NAC
				numN++;
				if(value>0.0){
					numTrueN++;
				}else{
					numFalseN++;
					
				}
			}else{//AC
				numP++;
				if(value>0){
					numTrueP++;
				}else{
					numFalseP++;
				}
			}
		}
		System.out.println("all presion:"+numTrue*1.0/numAll);
		
		System.out.println("negative presion:"+numTrueN*1.0/numN);
		System.out.println("positive presion:"+numTrueP*1.0/numP);
		System.out.println(numTrueP+":"+(numP-numTrueP)
				+":"+(numN-numTrueN)+":"+(numTrueN));
	
	}
	public void validationUUTrueSkill(
			double[] last,
			ArrayList<TriplePairIntIntInt> arrayTest,
			double threshold) throws IOException{
		int baseStartIndex=this.playerNum_*this.topicNum_;
		int weightStartIndex=this.playerNum_*(this.topicNum_+1);
		int numAll=arrayTest.size();
		int numN=0;
		int numP=0;
		int numUU=0;
		int numUP=0;
		int numUUTrue=0;
		int numUPTrue=0;
		int numTrue=0;
		int numTrueN=0;
		int numTrueP=0;
		int numFalseN=0;
		int numFalseP=0;
		for(TriplePairIntIntInt pair:arrayTest){
			int win=pair.a_;
			int lose=pair.b_;
			int context=pair.c_;
			double value=0.0;
			value=(priorPlayerMean_[win]-3*(priorPlayerSigma_[win]*priorPlayerSigma_[win]))
					-(priorPlayerMean_[lose]-3*(priorPlayerSigma_[lose]*priorPlayerSigma_[lose]));
			if(win>(this.userNum_-1)||lose>(this.userNum_-1)){
				numUP++;
				if(value>0.0){
					numUPTrue++;
				}
				if(win>lose){//NAC
					numN++;
					if(value>0.0){
						numTrueN++;
					}else{
						numFalseN++;
						
					}
				}else{//AC
					numP++;
					if(value>0){
						numTrueP++;
					}else{
						numFalseP++;
					}
				}
			}else{
				numUU++;
				if(value>0.0){
					numUUTrue++;
				}
			}
			if(value>0.0){
				numTrue++;
			}
//			
		}
		TSaccuracyMicro_=numTrue*1.0/numAll;
		TSaccuracyUPP_=numTrueP*1.0/numP;
		TSaccuracyUPN_=numTrueN*1.0/numN;
		TSaccuracyUP_=numUPTrue*1.0/numUP;
		TSaccuracyUU_=numUUTrue*1.0/numUU;
		TSaccuracyMacro_=(TSaccuracyUPP_+TSaccuracyUPN_+TSaccuracyUU_)/3.0;
		bw_.write("ALL pair:"+numAll);
		bw_.write("\n");
		bw_.write("TrueSkill 微平均\t宏平均\tUU\tUPP\tUPN\tUP");
		bw_.write("\n");
		bw_.write("value\t"
				+doubleDecimal.getString(TSaccuracyMicro_)+"\t"
				+doubleDecimal.getString(TSaccuracyMacro_)+"\t"
				+doubleDecimal.getString(TSaccuracyUU_)+"\t"
				+doubleDecimal.getString(TSaccuracyUPP_)+"\t"
				+doubleDecimal.getString(TSaccuracyUPN_)+"\t"
				+doubleDecimal.getString(TSaccuracyUP_)+"\t"
				);
		bw_.write("\n");
		bw_.write("-----------------\n");
//		System.out.println("uu:"+numUU+"\tup:"+numUP);
//		
//		System.out.println("all precison:"+numTrue*1.0/numAll);
//		System.out.println("up precison:"+numUPTrue*1.0/numUP);
//		System.out.println("uu precison:"+numUUTrue*1.0/numUU);
		
//		System.out.println("all presion:"+numTrue*1.0/numAll);
//		
//		System.out.println("negative presion:"+numTrueN*1.0/numN);
//		System.out.println("positive presion:"+numTrueP*1.0/numP);
//		System.out.println(numTrueP+":"+(numP-numTrueP)
//				+":"+(numN-numTrueN)+":"+(numTrueN));
	
	}
	
	public void validationUU(
			double[] last,
			ArrayList<TriplePairIntIntInt> arrayTest,
			double threshold) throws IOException{
		int baseStartIndex=this.playerNum_*this.topicNum_;
		int weightStartIndex=this.playerNum_*(this.topicNum_+1);
		int numAll=arrayTest.size();
		int numN=0;
		int numP=0;
		int numUU=0;
		int numUP=0;
		int numUUTrue=0;
		int numUPTrue=0;
		int numTrue=0;
		int numTrueN=0;
		int numTrueP=0;
		int numFalseN=0;
		int numFalseP=0;
		for(TriplePairIntIntInt pair:arrayTest){
			int win=pair.a_;
			int lose=pair.b_;
			int context=pair.c_;
			double value=0.0;
			value+=(alpha_*(last[baseStartIndex+win]-last[baseStartIndex+lose])
					);
			for(int t=0;t<topicNum_;t++){
				double topicValue=(last[weightStartIndex+t]
						*questionTopicDistribution_[context][t]
						*(last[win*topicNum_+t]-last[lose*topicNum_+t]));
				value+=topicValue;
			}
			if(win>(this.userNum_-1)||lose>(this.userNum_-1)){
				numUP++;
				if(value>0.0){
					numUPTrue++;
				}
				if(win>lose){//NAC
					numN++;
					if(value>0.0){
						numTrueN++;
					}else{
						numFalseN++;
						
					}
				}else{//AC
					numP++;
					if(value>0){
						numTrueP++;
					}else{
						numFalseP++;
					}
				}
			}else{
				numUU++;
				if(value>0.0){
					numUUTrue++;
				}
			}
			if(value>0.0){
				numTrue++;
			}
//			
		}
		double accuracyMicro=numTrue*1.0/numAll;
		double accuracyUPP=numTrueP*1.0/numP;
		double accuracyUPN=numTrueN*1.0/numN;
		double accuracyUP=numUPTrue*1.0/numUP;
		double accuracyUU=numUUTrue*1.0/numUU;
		double accuracyMacro=(accuracyUPP+accuracyUPN+accuracyUU)/3.0;
		this.bw_.write("ALL pair:"+numAll);
		this.bw_.write("\n");
		this.bw_.write("TrueSkill 微平均\t宏平均\tUU\tUPP\tUPN\tUP");
		this.bw_.write("\n");
		this.bw_.write("value\t"
				+doubleDecimal.getString(TSaccuracyMicro_)+"\t"
				+doubleDecimal.getString(TSaccuracyMacro_)+"\t"
				+doubleDecimal.getString(TSaccuracyUU_)+"\t"
				+doubleDecimal.getString(TSaccuracyUPP_)+"\t"
				+doubleDecimal.getString(TSaccuracyUPN_)+"\t"
				+doubleDecimal.getString(TSaccuracyUP_)+"\t"
				);
		this.bw_.write("\n");
		this.bw_.write("ZwhModel 微平均\t宏平均\tUU\tUPP\tUPN\tUP");
		this.bw_.write("\n");
		this.bw_.write("value\t"
				+doubleDecimal.getString(accuracyMicro)+"\t"
				+doubleDecimal.getString(accuracyMacro)+"\t"
				+doubleDecimal.getString(accuracyUU)+"\t"
				+doubleDecimal.getString(accuracyUPP)+"\t"
				+doubleDecimal.getString(accuracyUPN)+"\t"
				+doubleDecimal.getString(accuracyUP)+"\t"
				);
		this.bw_.write("\n");
		this.bw_.write("-----------------\n");
//		
//		System.out.println("ALL pair:"+numAll);
//		
//		System.out.println("uu:"+numUU+"\tup:"+numUP);
//		
//		System.out.println("all precison:"+numTrue*1.0/numAll);
//		System.out.println("up precison:"+numUPTrue*1.0/numUP);
//		System.out.println("uu precison:"+numUUTrue*1.0/numUU);
//		
////		System.out.println("all presion:"+numTrue*1.0/numAll);
////		
//		System.out.println("negative presion:"+numTrueN*1.0/numN);
//		System.out.println("positive presion:"+numTrueP*1.0/numP);
//		System.out.println(numTrueP+":"+(numP-numTrueP)
//				+":"+(numN-numTrueN)+":"+(numTrueN));
	
	}
	
	public void initialParameter(double[] initial){
		for(int p=0;p<this.playerNum_;p++){
			for(int t=0;t<topicNum_;t++){
				initial[p*topicNum_+t]=this.priorPlayerMean_[p];
			}
		}
		for(int p=0;p<this.playerNum_;p++){
			initial[playerNum_*topicNum_+p]=this.priorPlayerMean_[p];	
		}
		for(int t=0;t<topicNum_;t++){
			initial[playerNum_*(topicNum_+1)+t]=1.0;
		}
	}
	public void readPriorData(String inputFile) throws IOException{
		BufferedReader br=new FileReader(inputFile).getReader();
		String line;
		int order=0;
		br.readLine();//
		while((line=br.readLine())!=null){
			if(order>=this.playerNum_){
				break;
			}
			order++;
			StringTokenizer st=new StringTokenizer(line, "\t");
			String token=st.nextToken();
			
			int player=this.playerWordToIndex_.get(token);
			double mu=Double.parseDouble(st.nextToken());
			double sigma=Double.parseDouble(st.nextToken());
			this.priorPlayerMean_[player]=mu/psi_;
			this.priorPlayerSigma_[player]=sigma*sigma/(psi_*psi_);
		}
		br.close();
	}
	
	public void readTripleData(String inputFile) throws IOException{
		BufferedReader br=new FileReader(inputFile).getReader();
		String line;
		int order=0;
		ArrayList<TriplePairIntIntInt> array=new ArrayList<>();
		while((line=br.readLine())!=null){
			order++;
			StringTokenizer st=new StringTokenizer(line, "\t");
			
			int winPlayer=this.playerWordToIndex_.get(st.nextToken());
			int losePlayer=this.playerWordToIndex_.get(st.nextToken());
			int context=this.questionWordToIndex_.get(st.nextToken());
			array.add(new TriplePairIntIntInt(winPlayer, losePlayer, context));
		}
		Collections.shuffle(array);
		int size=array.size();
		for(int i=0;i<size*4/5;i++){
			arrayPairTrain_.add(array.get(i));
		}
		for(int i=size*4/5;i<size;i++){
			arrayPairTest_.add(array.get(i));
		}
		br.close();
	}
	public void constrcutPlayerDic(){
		for(int p=0;p<playerNum_;p++){
			this.playerIndexToWord_[p]=(p+1)+"";
			this.playerWordToIndex_.put((p+1)+"", p);
		}
	}
	public void readQuestionTopicDistribution(String inputFile) throws IOException{
		BufferedReader br=new FileReader(inputFile).getReader();
		String line;
		int order=0;
		while((line=br.readLine())!=null){
			order++;
			StringTokenizer st=new StringTokenizer(line, "\t");
			String question=st.nextToken();
			int index=questionWordToIndex_.size();
			this.questionIndexToWord_[index]=question;
			questionWordToIndex_.put(question, index);
			String token=st.nextToken();
			int topicOrder=0;
			StringTokenizer sst=new StringTokenizer(token," ");
			while(sst.hasMoreTokens()){
				double value=Double.parseDouble(sst.nextToken());
				questionTopicDistribution_[index][topicOrder++]=value;
			}
			if(topicOrder!=topicNum_){
				System.out.println("quesion-topic-distri number error");
				System.exit(0);
			}
		}
//		if(order!=questionNum_){
//			System.out.println("question number error");
//			System.exit(0);
//		}
		
	}
	
}
