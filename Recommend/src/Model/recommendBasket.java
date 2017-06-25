/**
 * 
 */
package Model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import dataStuct.SortPairDouble;
import edu.ruc.FileReader;

/**
 * @author wenhui zhang
 *
 */
public class recommendBasket {
	BufferedWriter bwResult_;
	int topicNumS_;
	int topicNumI_;
	int minTestAC_;
	int maxTestAC_;
	int questionNum_;
	int userNum_;
	double alpha_;
	double semanticMaxWindow_;
//	double eta_;
	double[][] thetaI_;//estimated impression mode user-volumn distribution
	double[][] thetaS_;//estimated semantic mode user-topic distribution 
	double[][] phiI_;//estimated impression topic-word distribution phiI[v][w]
	double[][] phiS_;//estimated semantic topic-word distribution phiS[z][w]
	double[][] userTopicDistribution_;
////	double[][] userQuestionScore_;
////	double[][] userQuestionDiff_;
//	double[][] userQuestionInterest_;
//	double[][] questionQuestionDiff_;
//	double[][] userQuestionInterest_;
//	
	double[] weight_;
	double[] userBase_;
	double[][] userTopicExpertise_;
	double[] questionBase_;
	double[][] questionTopicExpertise_;
	int[] indexToTerm_;//index to word
	HashMap<String,Integer> termToIndex_;
	double[][] questionTopicDistribution_;
	int[][] dataTrain_;
	int[][] dataTest_;
	int[][] dataTrainAC_;
	int[][] dataTestAC_;

	
	public void initialize(
			int topicNumS,
			int topicNumI,
			int minTestAC,
			int maxTestAC,
			double alpha,
			double maxWindow,
			String fileThetaI,
			String fileThetaS,
			String filePhiS,
			String filePhiI,
			String fileWordMap,
			String fileTestData,
			String fileTrainData,
			String fileTestACData,
			String fileTrainACData,
			String fileUserBase,
			String fileUserTopicExpert,
			String fileQuestionBase,
			String fileQuestionTopicExpert,
			String fileWeight,
			String fileQuestionTopicDistriFile,
			String fileTopicAssign,
			BufferedWriter fileOutputResult) throws IOException{
		this.bwResult_=fileOutputResult;
		this.topicNumS_=topicNumS;
		this.topicNumI_=topicNumI;
		this.minTestAC_=minTestAC;
		this.maxTestAC_=maxTestAC;
		this.alpha_=alpha;
		this.userNum_=getUserNum(fileTrainData);
		this.semanticMaxWindow_=maxWindow;
//		this.eta_=eta;
		this.termToIndex_=new HashMap<>();
		constructIndexToWord(fileWordMap, 
				 this.termToIndex_);
		
		
		
		this.userBase_=new double[userNum_];
		this.userTopicExpertise_=new double[userNum_][topicNumS_];
		this.questionBase_=new double[questionNum_];
		this.questionTopicExpertise_=new double[questionNum_][topicNumS_];
		this.weight_=new double[topicNumS_];
		this.questionTopicDistribution_=new double[questionNum_][topicNumS_];
		this.thetaI_=new double[userNum_][topicNumI_];
		this.thetaS_=new double[userNum_][topicNumS_];
		this.phiI_=new double[topicNumI][questionNum_];
		this.phiS_=new double[topicNumS][questionNum_];
		this.userTopicDistribution_=new double[userNum_][topicNumS_];
		this.dataTest_=new int[userNum_][];
		this.dataTrain_=new int[userNum_][];
		this.dataTestAC_=new int[userNum_][];
		this.dataTrainAC_=new int[userNum_][];
		
		readPhiI(filePhiI, this.phiI_);
		readPhiS(filePhiS, this.phiS_);
		readThetaI(fileThetaI, this.thetaI_);
		readThetaS(fileThetaS, this.thetaS_);
		readTopicAssign(fileTopicAssign,this.userTopicDistribution_);
		
		constuctData(fileTestData, 
				this.dataTest_, this.termToIndex_);
		constuctData(fileTrainData, 
				this.dataTrain_, this.termToIndex_);
		constuctACData(fileTrainACData, 
				this.dataTrainAC_, this.termToIndex_);
		constuctACData(fileTestACData, 
				this.dataTestAC_, this.termToIndex_);
		readQuestionBase(fileQuestionBase, questionBase_);
		readUserBase(fileUserBase, userBase_);
		readQuestionTopicExpert(fileQuestionTopicExpert, questionTopicExpertise_);
		readUserTopicExpert(fileUserTopicExpert, userTopicExpertise_);
		readWeight(fileWeight, weight_);
		readQuestionTopicDistribution(fileQuestionTopicDistriFile);
	}
	public void recommendQuestion() throws IOException{
		int topN=100;
		double thresholdEta=0.7;
		double thresholdSemantic=0.3;
		long sumP=0;
		long sumAll=0;
		double pTop1=0.0;
		double pTop3=0.0;
		double pTop5=0.0;
		double pTop10=0.0;
		double rTop1=0.0;
		double rTop3=0.0;
		double rTop5=0.0;
		double rTop10=0.0;
		double MAPTop1=0.0;
		double MAPTop3=0.0;
		double MAPTop5=0.0;
		double MAPTop10=0.0;
		int startUser=0;
		int endUser=this.userNum_;
		int userNum=0;
		double eta=0.0;
		for(int u=startUser;u<endUser;u++){
			if(u%1000==1){
				System.out.println(u);
			}
			eta=calculateEta(this.dataTrain_[u],this.indexToTerm_);
			
//			double eta=calculateEta(this.dataTrain_[u],this.indexToTerm_);
//			eta=0.0;
			this.bwResult_.write(eta+"\n");
			HashSet<Integer> setTrainAC=new HashSet<>();
			for(int trainData:this.dataTrainAC_[u]){
				setTrainAC.add(trainData);
			}
			HashSet<Integer> setTestEva=new HashSet<>();
			for(int testData:this.dataTestAC_[u]){
				setTestEva.add(testData);
			}
//			for(int testData:this.dataTest_[u]){
//				setTestEva.add(testData);
//			}
//			System.out.println(setTestEva.size());
			if(setTestEva.size()<this.minTestAC_
					||setTestEva.size()>this.maxTestAC_){
				continue;
			}
			userNum++;
			double[] volumnDistri=calculateVolumnDistri(dataTrain_[u], indexToTerm_);
			ArrayList<SortPairDouble> arrayRank=new ArrayList<>();
			for(int q=0;q<this.questionNum_;q++){
				if(!setTrainAC.contains(q)){
					double valueInterest=0.0;
					double valueI=0.0;
					double valueS=0.0;
					for(int v=0;v<this.topicNumI_;v++){
//		 				valueI+=(1.0*this.thetaI_[u][v]*this.phiI_[v][q]);
		 				valueI+=(1.0*volumnDistri[v]*this.phiI_[v][q]);
						
//						if(eta>thresholdEta){
//							double[] result=calculateVolumnLast(this.dataTrain_[u],this.indexToTerm_,5);
////							valueI+=(1.0*result[v]*this.phiI_[v][q]);
//							valueI+=(1.0*result[v]*this.thetaI_[u][v]*this.phiI_[v][q]);
//							
////							valueI+=(1.0*this.thetaI_[u][v]*this.phiI_[v][q]);
//						}else{
//							valueI+=(1.0*this.thetaI_[u][v]*this.phiI_[v][q]);
//						}
//						
					}
					for(int z=0;z<this.topicNumS_;z++){
//						double[] result =calculateSemanticLast(dataTrain_[u], 5);
//						valueS+=this.thetaS_[u][z]*this.phiS_[z][q];
						valueS+=(this.userTopicDistribution_[u][z])*this.phiS_[z][q];
						
//						if(eta<thresholdSemantic){
//							valueS+=result[z]*this.thetaS_[u][z]*this.phiS_[z][q];
//						}else{
//							valueS+=result[z]*this.thetaS_[u][z]*this.phiS_[z][q];
//						}
					}
					
					valueInterest=valueI*eta+valueS*(1.0-eta);
					double valueExpert=calculate(u, q);
					
//					arrayRank.add(new SortPairDouble(q,valueExpert));
//					arrayRank.add(new SortPairDouble(q,valueInterest));
//					
					arrayRank.add(new SortPairDouble(q,valueInterest*valueExpert));
				}
			}
			Collections.sort(arrayRank);
			int numP=0;
			int numAll=0;
			int topOrder=0;
			ArrayList<Integer> arrayResult=new ArrayList<>();
			for(SortPairDouble pair:arrayRank){
				topOrder++;
				if(topOrder>topN){
					break;
				}
				this.bwResult_.write(indexToTerm_[pair.key]+":");
					
				
				if(setTestEva.contains(pair.key)){
					
					bwResult_.write("1\t");
					
					arrayResult.add(1);
				}else{
					bwResult_.write("0\t");
					arrayResult.add(0);
				}
					
			}
			
			bwResult_.write("\n");
//			if(eta>thresholdEta){
				
			pTop1+=calculatePreTopK(arrayResult, 1);
			pTop3+=calculatePreTopK(arrayResult, 3);
			pTop5+=calculatePreTopK(arrayResult, 5);
			pTop10+=calculatePreTopK(arrayResult, 10);
			
			rTop1+=calculateRecallTopK(arrayResult, 1,setTestEva.size());
			rTop3+=calculateRecallTopK(arrayResult, 3,setTestEva.size());
			rTop5+=calculateRecallTopK(arrayResult, 5,setTestEva.size());
			rTop10+=calculateRecallTopK(arrayResult, 10,setTestEva.size());
			
			MAPTop1+=calculateMAPTopK(arrayResult, 1);
			MAPTop3+=calculateMAPTopK(arrayResult, 3);
			MAPTop5+=calculateMAPTopK(arrayResult, 5);
			MAPTop10+=calculateMAPTopK(arrayResult, 10);
//			}

			//			System.out.println(u+":"+numP*1.0/numAll);
			sumP+=numP;
			sumAll+=numAll;
			
		}
		bwResult_.close();
		System.out.println(userNum);
		System.out.println(pTop1/userNum);
		System.out.println(pTop3/userNum);
		System.out.println(pTop5/userNum);
		System.out.println(pTop10/userNum);
		System.out.println();
		System.out.println(rTop1/userNum);
		System.out.println(rTop3/userNum);
		System.out.println(rTop5/userNum);
		System.out.println(rTop10/userNum);
		System.out.println();
		System.out.println(MAPTop1/userNum);
		System.out.println(MAPTop3/userNum);
		System.out.println(MAPTop5/userNum);
		System.out.println(MAPTop10/userNum);
//		System.out.println("userNum:"+userNum);
//		System.out.println("p@1"+":"+pTop1/userNum);
//		System.out.println("p@3"+":"+pTop3/userNum);
//		System.out.println("p@5"+":"+pTop5/userNum);
//		System.out.println("p@10"+":"+pTop10/userNum);
//		System.out.println("MAP@1"+":"+MAPTop1/userNum);
//		System.out.println("MAP@3"+":"+MAPTop3/userNum);
//		System.out.println("MAP@5"+":"+MAPTop5/userNum);
//		System.out.println("MAP@10"+":"+MAPTop10/userNum);
		
	}
	private double calculateEta(int[] data,int[] indexToTerm){
		int length=data.length;
		int numV=0;
		int[] problemList=new int[length];
		for(int i=0;i<length;i++){
			int value=indexToTerm[data[i]];
			problemList[i]=value;
		}
		for(int i=1;i<length;i++){
			if(Math.abs(problemList[i]-problemList[i-1])<semanticMaxWindow_){
				numV++;
				continue;
			}
			if((i!=(length-1))&&Math.abs(problemList[i]-problemList[i+1])<semanticMaxWindow_){
				numV++;
				continue;
			}
		}
		return 1.0*numV/(length-1);
	}
	
	private double[] calculateVolumnDistri(int[] data,int[] indexToTerm){
		int length=data.length;
		int numV=0;
		int[] volDistri=new int[this.topicNumI_];
		double[] volDistriPro=new double[this.topicNumI_];
		int prior=1;
		
		int[] problemList=new int[length];
		for(int i=0;i<length;i++){
			int value=indexToTerm[data[i]];
			volDistri[(value-1000)/100]++;
		}
		int sum=0;
		for(int v=0;v<this.topicNumI_;v++){
			volDistri[v]+=prior;
			sum+=volDistri[v];
		}
		for(int v=0;v<this.topicNumI_;v++){
			volDistriPro[v]=1.0*volDistri[v]/sum;
		}
		
		return volDistriPro;
	}
	
	
	private double[] calculateVolumnLast(int[] data,int[] indexToTerm,int last){
		int length=data.length;
		double prior=0.1;
		int[] problemList=new int[length];
		for(int i=0;i<length;i++){
			int value=indexToTerm[data[i]];
			problemList[i]=value;
		}
		int start=1;
		if(length-last>1){
			start=length-last;
		}
		int numV=0;
		int lengthAll=0;
		double[] result=new double[this.topicNumI_];
		if(problemList[length-2]/100
				==problemList[length-1]/100){
			
			int volumn=(problemList[length-1]-1000)/100;
			result[volumn]=1.0;
			for(int v=volumn;v<this.topicNumI_;v++){
				result[volumn]=0.5;
			}
		}else{
			int volumn=(problemList[length-1]-1000)/100;
			result[volumn]=0.5;
			for(int v=volumn;v<this.topicNumI_;v++){
				result[volumn]=0.5;
			}
		}
		return result;
	}
	
	private double[] calculateSemanticLast(int[] data,int last){
		int length=data.length;
		double prior=0.1;
//		int[] problemList=new int[length];
//		for(int i=0;i<length;i++){
//			int value=indexToTerm[data[i]];
//			problemList[i]=value;
//		}
		int start=1;
		if(length-last>1){
			start=length-last;
		}
		int numV=0;
		int lengthAll=0;
		double[] result=new double[this.topicNumS_];
		int sum=0;
		for(int i=start;i<length;i++){
			sum++;
			for(int z=0;z<this.topicNumS_;z++){
				result[z]+=this.questionTopicDistribution_[data[i]][z];
			}
			
		}
		for(int z=0;z<this.topicNumS_;z++){
			result[z]/=sum;
		}
		return result;
	}
	
	private double calculateMAPTopK(
			ArrayList<Integer> array,
			int K){
		double value=0.0;
		int numP=0;
		for(int i=0;i<array.size();i++){
			if(i>=K){
				break;
			}
			if(array.get(i)>0){
				numP++;
				value+=(numP*1.0/(i+1));
			}
		}
		return value/K;
		
	}

	private  double calculatePreTopK(
			ArrayList<Integer> array,
			int K){
		int numP=0;
		for(int i=0;i<array.size();i++){
			if(i>=K){
				break;
			}
			if(array.get(i)>0){
				numP++;
			}
		}
		return 1.0*numP/K;
		
	}
	
	private  double calculateRecallTopK(
			ArrayList<Integer> array,
			int K,int allSize){
		int numP=0;
		for(int i=0;i<array.size();i++){
			if(i>=K){
				break;
			}
			if(array.get(i)>0){
				numP++;
			}
		}
		return 1.0*numP/allSize;
		
	}
	
	
	
	private double calculate(int user,int question){
		double value=0.0;
		double valueBase=this.userBase_[user]-this.questionBase_[question];
		double valueTopic=0.0;
		for(int z=0;z<this.topicNumS_;z++){
			valueTopic+=(this.weight_[z]*
					(this.userTopicExpertise_[user][z]
					-this.questionTopicExpertise_[question][z])
					*(this.questionTopicDistribution_[question][z]));
		}
		value=valueBase*alpha_+valueTopic;
		return value;
	}
	public void readQuestionTopicDistribution(String inputFile) throws IOException{
		BufferedReader br=new FileReader(inputFile).getReader();
		String line;
		int order=0;
		while((line=br.readLine())!=null){
			order++;
			StringTokenizer st=new StringTokenizer(line, "\t");
			String question=st.nextToken();
			String token=st.nextToken();
			int topicOrder=0;
			if(!this.termToIndex_.containsKey(question)){
				continue;
			}
			int index=this.termToIndex_.get(question);
			StringTokenizer sst=new StringTokenizer(token," ");
			while(sst.hasMoreTokens()){
				double value=Double.parseDouble(sst.nextToken());
				questionTopicDistribution_[index][topicOrder++]=value;
			}
			if(topicOrder!=this.topicNumS_){
				System.out.println("quesion-topic-distri number error");
				System.exit(0);
			}
		}
//		if(order!=questionNum_){
//			System.out.println("question number error");
//			System.exit(0);
//		}
		
	}

	private void constuctACData(
			String file,
			int[][] dataAC,
			HashMap<String,Integer> termToIndex) throws IOException{
		BufferedReader br=new FileReader(file).getReader();
		String line;
		int userOrder=0;
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			int length=0;
			for(String str:split){
				if(str.length()>0){
					length++;
				}
			}
			
			dataAC[userOrder]=new int[length];
			int itemOrder=0;
			for(String str:split){
				if(str.length()<1){
					continue;
				}
				if(termToIndex.containsKey(str)){
					int index=termToIndex.get(str);
					dataAC[userOrder][itemOrder]=index;
					itemOrder++;
				}
				
			}
			userOrder++;
			
		}
		br.close();
	}
	
	
	private void constuctData(
			String file,
			int[][] data,
			HashMap<String,Integer> termToIndex) throws IOException{
		BufferedReader br=new FileReader(file).getReader();
		String line;
		int userOrder=0;
		while((line=br.readLine())!=null){
			String[] split=line.split(" ");
			int length=0;
			for(String str:split){
				if(str.length()>0){
					length++;
				}
			}
			
			data[userOrder]=new int[length];
			int itemOrder=0;
			for(String str:split){
//				System.out.println(str);
				if(termToIndex.containsKey(str)){
					int index=termToIndex.get(str);
					data[userOrder][itemOrder]=index;
					itemOrder++;
				}
				
			}
			userOrder++;
			
		}
		br.close();
	}
	
	private  void constructIndexToWord
	(String input,HashMap<String,Integer> map) throws IOException{
		BufferedReader br=new FileReader(input).getReader();
		String line;
		ArrayList<Integer> array=new ArrayList<>();
		while((line=br.readLine())!=null){
			StringTokenizer st=new StringTokenizer(line, "\t");
			int index=Integer.parseInt(st.nextToken());
			String key=st.nextToken();
			int pro=Integer.parseInt(key);
			array.add(pro);
			
			map.put(key, index);
			
		}
		int length=array.size();
		this.questionNum_=length;
		this.indexToTerm_=new int[this.questionNum_];
		for(int i=0;i<length;i++){
			this.indexToTerm_[i]=array.get(i);
		}
		br.close();
		
	}
	
	private void readThetaI(
			String fileThetaI,
			double[][] thetaI) throws IOException{
		int userOrder=0;
		BufferedReader br=new FileReader(fileThetaI).getReader();
		String line;
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			int topicOrder=0;
			for(String str:split){
				double value=Double.parseDouble(str);
				thetaI[userOrder][topicOrder]=value;
				topicOrder++;
			}
			if(topicOrder!=this.topicNumI_){
				System.out.println("thetaI topic error");
				System.exit(0);
			}
			userOrder++;
		}
		if(userOrder!=this.userNum_){
			System.out.println("thetaI user error");
			System.exit(0);
		}
		br.close();
		
	}
	
	private void readThetaS(
			String fileThetaS,
			double[][] thetaS) throws IOException{
		int userOrder=0;
		BufferedReader br=new FileReader(fileThetaS).getReader();
		String line;
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			int topicOrder=0;
			for(String str:split){
				double value=Double.parseDouble(str);
				thetaS[userOrder][topicOrder]=value;
				topicOrder++;
			}
			if(topicOrder!=this.topicNumS_){
				System.out.println("thetaS topic error");
				System.exit(0);
			}
			userOrder++;
		}
		if(userOrder!=this.userNum_){
			System.out.println("thetaS user error");
			System.exit(0);
		}
		br.close();
		
	}
	
	private void readPhiI(
			String filePhiI,
			double[][] PhiI) throws IOException{
		int topicOrder=0;
		BufferedReader br=new FileReader(filePhiI).getReader();
		String line;
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			
			for(String str:split){
				String[] split2=str.split(":");
				int index=Integer.parseInt(split2[0]);
				double value=Double.parseDouble(split2[1]);
				PhiI[topicOrder][index]=value;
				
			}
			
			topicOrder++;
		}
		if(topicOrder!=this.topicNumI_){
			System.out.println("phiI user error");
			System.exit(0);
		}
		br.close();
		
	}
	
	private void readTopicAssign(
			String fileTopicAssign,
			double[][] userTopicDistribution) throws IOException{
		
		BufferedReader br=new FileReader(fileTopicAssign).getReader();
		String line;
		int prior=1;
		int userOrder=-1;
		while((line=br.readLine())!=null){
			userOrder++;
			String[] split=line.split(" ");
			int[] userTopicNum=new int[this.topicNumS_];
			for(String str:split){
				if(str.length()>0){
					String[] split2=str.split(":");
					int index=Integer.parseInt(split2[0]);
					int topic=Integer.parseInt(split2[1]);
					if(topic>=topicNumI_){
						userTopicNum[topic-topicNumI_]++;
					}
				}
				
			}
			int sum=0;
			for(int z=0;z<this.topicNumS_;z++){
				userTopicNum[z]+=prior;
				sum+=userTopicNum[z];
			}
			for(int z=0;z<this.topicNumS_;z++){
				userTopicDistribution[userOrder][z]=1.0*userTopicNum[z]/sum;
			}
			
		}
		
		
		br.close();
		
	}
	
	private void readPhiS(
			String filePhiS,
			double[][] PhiS) throws IOException{
		int topicOrder=0;
		BufferedReader br=new FileReader(filePhiS).getReader();
		String line;
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			
			for(String str:split){
				String[] split2=str.split(":");
				int index=Integer.parseInt(split2[0]);
				double value=Double.parseDouble(split2[1]);
				PhiS[topicOrder][index]=value;
				
			}
			
			topicOrder++;
		}
		if(topicOrder!=this.topicNumS_){
			System.out.println("phiS user error");
			System.exit(0);
		}
		br.close();
		
	}
	private void readUserBase(String fileBase
			,double[] dataBase) throws IOException{
		BufferedReader br=new FileReader(fileBase).getReader();
		String line;
		int userOrder=0;
		while((line=br.readLine())!=null){
//			System.out.println(userOrder);
			if(userOrder>=this.userNum_){
				break;
			}
			double value=Double.parseDouble(line);
			dataBase[userOrder++]=value;
		}
		br.close();
	}
	private void readQuestionBase(String fileBase
			,double[] dataBase) throws IOException{
		BufferedReader br=new FileReader(fileBase).getReader();
		String line;
		
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			String indexStr=split[0];
			if(this.termToIndex_.containsKey(indexStr)){
				int index=this.termToIndex_.get(indexStr);
				double value=Double.parseDouble(split[1]);
				dataBase[index]=value;
			}
			
		}
		br.close();
	}
	
	private void readUserTopicExpert(String fileUserTopicExpert
			,double[][] userTopicExpert) throws IOException{
		BufferedReader br=new FileReader(fileUserTopicExpert).getReader();
		String line;
		int userOrder=0;
		while((line=br.readLine())!=null){
			if(userOrder>=this.userNum_){
				break;
			}
			String[] split=line.split("\t");
			int topicOrder=0;
			for(String str:split){
				double value=Double.parseDouble(str);
				userTopicExpert[userOrder][topicOrder++]=value;
			}
			
			userOrder++;
			
		}
		br.close();
	}
	
	private void readQuestionTopicExpert(String fileQuestionTopicExpert
			,double[][] questionTopicExpert) throws IOException{
		BufferedReader br=new FileReader(fileQuestionTopicExpert).getReader();
		String line;
		
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			String indexStr=split[0];
			if(!this.termToIndex_.containsKey(indexStr)){
				continue;
			}
			int index=this.termToIndex_.get(indexStr);
			int topicOrder=0;
			String[] split2=split[1].split(" ");

			for(String str:split2){
				if(str.equals("")){
					continue;
				}
				double value=Double.parseDouble(str);
				questionTopicExpert[index][topicOrder++]=value;
			}
			
			
			
		}
		br.close();
	}
	private void readWeight(String inputWeight
			,double[] weight) throws IOException{
		BufferedReader br=new FileReader(inputWeight).getReader();
		String line;
		int order=0;
		while((line=br.readLine())!=null){
			String[] split=line.split("\t");
			double value=Double.parseDouble(split[1]);
			weight[order++]=value;
		}
		br.close();
	}
	
	private int getUserNum(String input) throws IOException{
		BufferedReader br=new FileReader(input).getReader();
		int order=0;
		String line;
		while((line=br.readLine())!=null){
			order++;
		}
		br.close();
		return order;
	}
	
	
}
