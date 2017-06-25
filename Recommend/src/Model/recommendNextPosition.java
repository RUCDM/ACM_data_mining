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

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

import dataStuct.SortPairDouble;
import edu.ruc.FileReader;

/**
 * @author wenhui zhang
 *
 */
public class recommendNextPosition {
	public void initModel(
			int topicI,
			int topicS,
			int windowSize,
			double windowMaxValue,
			double alpha,
			double omega,
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
			String fileStateAssign,
			String fileRho,
			BufferedWriter fileOutputResult) throws IOException{
		this.bwResult_=fileOutputResult;
		this.alpha_=alpha;
		this.omega_=omega;
		this.windowMaxValue_=windowMaxValue;
		this.windowSize_=windowSize;
		this.topicNumI_=topicI;
		this.topicNumS_=topicS;
		this.states_=3*this.topicNumI_+3*this.topicNumS_;//the hidden state 
		//read doc
		this.userNum_=getUserNum(fileTrainData);
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
		this.phiI_=new double[topicNumI_][questionNum_];
		
		this.phiS_=new double[topicNumS_][questionNum_];
		this.userTopicDistribution_=new double[userNum_][topicNumS_];
		this.lastState_=new int[userNum_];
		this.dataTest_=new int[userNum_][];
		this.dataTrain_=new int[userNum_][];
		this.dataTestAC_=new int[userNum_][];
		this.dataTrainAC_=new int[userNum_][];
		this.modeProb_=new double[userNum_][];
		this.modeProbAC_=new double[userNum_][];
		readPhiI(filePhiI, this.phiI_);
		readPhiS(filePhiS, this.phiS_);
		readThetaI(fileThetaI, this.thetaI_);
		readThetaS(fileThetaS, this.thetaS_);
		readTopicAssign(fileTopicAssign,this.userTopicDistribution_);
		readStateAssign(fileStateAssign, lastState_);
		constuctData(fileTestData, 
				this.dataTest_, this.termToIndex_);
		constuctData(fileTrainData, 
				this.dataTrain_, this.termToIndex_);
		constuctACData(fileTrainACData, 
				this.dataTrainAC_, this.termToIndex_);
		constuctACData(fileTestACData, 
				this.dataTestAC_, this.termToIndex_);
		initialFeature(dataTest_,modeProb_ );
		initialFeature(dataTestAC_,modeProbAC_ );
		constructModeProb(dataTest_, dataTrain_, 
				modeProb_, indexToTerm_, 
				windowSize_, windowMaxValue_);
		constructModeProb(dataTestAC_, dataTrain_, 
				modeProbAC_, indexToTerm_, 
				windowSize_, windowMaxValue_);
		readQuestionBase(fileQuestionBase, questionBase_);
		readUserBase(fileUserBase, userBase_);
		readQuestionTopicExpert(fileQuestionTopicExpert, questionTopicExpertise_);
		readUserTopicExpert(fileUserTopicExpert, userTopicExpertise_);
		readWeight(fileWeight, weight_);
		readQuestionTopicDistribution(fileQuestionTopicDistriFile);
	
		
		this.rhoS_=new double[this.userNum_];
		this.rhoI_=new double[this.userNum_];
		readFileRho(fileRho);
		
		
		
	}
	public void recommendNextPosition() throws IOException{
		int startUser=0;
		int endUser=this.userNum_;
		long sumPosition=0;
		long sumPositionTrue=0;
		for(int u=startUser;u<endUser;u++){
			
			if(u%100==1){
//				System.out.println(u);
				System.out.println(u+":hit ratio:"+1.0*sumPositionTrue/sumPosition);
				
			}else{
				continue;
			}
			
			HashSet<Integer> setDone=new HashSet<>();
			for(int trainData:this.dataTrain_[u]){
				setDone.add(trainData);
			}
			
			for(int l=0;l<this.dataTest_[u].length;l++){
				sumPosition++;
				ArrayList<SortPairDouble> arrayRank=new ArrayList<>();
				int[] questionMaxIndex=new int[questionNum_];
				for(int q=0;q<this.questionNum_;q++){
					if(!setDone.contains(q)){
						double valueInterest=0.0;
						double[] localTwo=new double[states_];
						for(int s=0;s<this.topicNumI_;s++){
							localTwo[s]=this.phiI_[s][q];
							localTwo[s+this.topicNumI_]=this.phiI_[s][q];
							localTwo[s+2*topicNumI_]=this.phiI_[s][q];
						}
						for(int s=0;s<this.topicNumS_;s++){
							localTwo[3*topicNumI_+s]=this.phiS_[s][q];
							localTwo[3*topicNumI_+s+this.topicNumS_]=this.phiS_[s][q];
							localTwo[3*topicNumI_+s+this.topicNumS_*2]=this.phiS_[s][q];
						}
						double[] alpha_t_1=new double[this.states_];
						double[] alpha_t=new double[this.states_];
						//initial 
						for(int s=0;s<this.states_;s++){
							if(s==lastState_[u]){
								alpha_t_1[s]=(1.0+this.omega_)/(1.0+this.omega_*this.states_);
							}else{
								alpha_t_1[s]=(this.omega_)/(1.0+this.omega_*this.states_);
								
							}
						}
						normalize(alpha_t_1,1.0+this.omega_*this.states_);
						computeSingleAlpha(localTwo, 
								modeProb_[u][l], rhoI_[u], rhoS_[u], 
								thetaI_[u], thetaS_[u], alpha_t_1, alpha_t);
						double max=-0.1;
						int index=-1;
						for(int s=0;s<this.states_;s++){
							if(alpha_t[s]>max){
								index=s;
								max=alpha_t[s];
							}
						}
						questionMaxIndex[q]=index;
						arrayRank.add(new SortPairDouble(q, max));
					}
				}
				Collections.sort(arrayRank);
				if(calculateHitRatio(arrayRank, dataTest_[u][l], 10,questionMaxIndex)){
					sumPositionTrue++;
				}
				setDone.add(this.dataTest_[u][l]);
//				lastState_[u]=questionMaxIndex[arrayRank.get(0).key];
				lastState_[u]=questionMaxIndex[this.dataTest_[u][l]];
				
			}
		}
		System.out.println("hit ratio:"+1.0*sumPositionTrue/sumPosition);
	}
	public void recommendNextPositionAC() throws IOException{
		int startUser=0;
//		int endUser=9500;
		int endUser=this.userNum_;
		long sumPosition=0;
		long sumPositionTrue=0;
		for(int u=startUser;u<endUser;u++){
			
			if(u%10==1){
//				System.out.println(u);
				System.out.println((u)+":hit ratio:"+1.0*sumPositionTrue/sumPosition);
				
			}else{
//				continue;
			}
//			System.out.println((u)+":hit ratio:"+1.0*sumPositionTrue/sumPosition);
			
			this.bwResult_.write((u+1)+"-------------\n");
			HashSet<Integer> setDone=new HashSet<>();
			for(int trainData:this.dataTrainAC_[u]){
				setDone.add(trainData);
			}
			double[][] thetaILast=new double[this.dataTestAC_[u].length][this.topicNumI_];
			double[][] thetaSLast=new double[this.dataTestAC_[u].length][this.topicNumS_];
			
			double[] lastDistribution=new double[states_];
			constructThetaILast(dataTestAC_[u], dataTrain_[u], thetaILast, indexToTerm_, windowSize_);
			constructThetaSLast(dataTestAC_[u], dataTrain_[u], thetaSLast, indexToTerm_, windowSize_);
			
			//			int maxLeng=1;
//			if(this.dataTestAC_[u].length<1){
//				maxLeng=this.dataTestAC_[u].length;
//			}
//			for(int l=0;l<maxLeng;l++){
				
			for(int l=0;l<this.dataTestAC_[u].length;l++){
//				if(modeProbAC_[u][l]<0.5){
//					
//				}else{
//					continue;
//				}
				sumPosition++;
				ArrayList<SortPairDouble> arrayRank=new ArrayList<>();
				int[] questionMaxIndex=new int[questionNum_];
				double[][] questionMaxState=new double[questionNum_][states_]; 
				
				boolean signal=false;
				if(modeProbAC_[u][l]>0.5){
					if(ifAdd(dataTestAC_[u], dataTrain_[u], indexToTerm_, l)){
						signal=true;
					}
				}
				int lastProblem;
				if(l==0){
					lastProblem=indexToTerm_[dataTrain_[u][dataTrain_[u].length-1]];
				}else{
					lastProblem=indexToTerm_[dataTestAC_[u][l-1]];
					
				}
				if(l>=0){
					for(int s=0;s<this.states_;s++){
						if(s==lastState_[u]){
							lastDistribution[s]=(1.0+this.omega_)/(1.0+this.omega_*this.states_);
						}else{
							lastDistribution[s]=(this.omega_)/(1.0+this.omega_*this.states_);
							
						}
					}
					normalize(lastDistribution,1.0+this.omega_*this.states_);
					
				}
				for(int q=0;q<this.questionNum_;q++){
					if(!setDone.contains(q)){
						if(signal){
							int thisProblem=indexToTerm_[q];
							if(thisProblem<lastProblem||(thisProblem-lastProblem)>windowMaxValue_){
								continue;
							}
						}
						double valueInterest=0.0;
						double[] localTwo=new double[states_];
						for(int s=0;s<this.topicNumI_;s++){
							localTwo[s]=this.phiI_[s][q];
							localTwo[s+this.topicNumI_]=this.phiI_[s][q];
							localTwo[s+2*topicNumI_]=this.phiI_[s][q];
						}
						for(int s=0;s<this.topicNumS_;s++){
							localTwo[3*topicNumI_+s]=this.phiS_[s][q];
							localTwo[3*topicNumI_+s+this.topicNumS_]=this.phiS_[s][q];
							localTwo[3*topicNumI_+s+this.topicNumS_*2]=this.phiS_[s][q];
						}
						double[] alpha_t_1=new double[this.states_];
						double[] alpha_t=new double[this.states_];
						//initial 
						for(int s=0;s<this.states_;s++){
							alpha_t_1[s]=lastDistribution[s];
						}
							computeSingleAlpha(localTwo, 
						
								modeProbAC_[u][l], rhoI_[u], rhoS_[u], 
//								thetaILast[l],
								thetaI_[u],
								thetaSLast[l],
//								thetaS_[u],
								alpha_t_1, alpha_t);
						double max=-0.1;
						int index=-1;
						for(int s=0;s<this.states_;s++){
							if(alpha_t[s]>max){
								index=s;
								max=alpha_t[s];
							}
						}
						questionMaxIndex[q]=index;
						double norm=0.0;
						for(int s=0;s<this.states_;s++){
							norm+=(alpha_t[s]);
						}
						normalize(alpha_t, norm);
						for(int s=0;s<this.states_;s++){
							questionMaxState[q][s]=alpha_t[s];
						}
						double valueExpert=calculate(u, q);
						arrayRank.add(new SortPairDouble(q, max*valueExpert));
//						arrayRank.add(new SortPairDouble(q, max));
						
					}
				}
				Collections.sort(arrayRank);
				this.bwResult_.write(this.indexToTerm_[dataTestAC_[u][l]]+":"+modeProbAC_[u][l]+"\t");
				
				if(calculateHitRatio(arrayRank, dataTestAC_[u][l], 10,questionMaxIndex)){
					sumPositionTrue++;
				}
				setDone.add(this.dataTestAC_[u][l]);
//				lastState_[u]=questionMaxIndex[arrayRank.get(0).key];
				lastState_[u]=questionMaxIndex[this.dataTestAC_[u][l]];
				for(int s=0;s<this.states_;s++){
					lastDistribution[s]=questionMaxState[this.dataTestAC_[u][l]][s];
				}
			}
		}
		this.bwResult_.close();
		System.out.println("hit ratio:"+1.0*sumPositionTrue/sumPosition);
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
	
	private boolean calculateHitRatio(ArrayList<SortPairDouble> arrayRank,
			int trueProblem,int N,
			int[] qToIndex) throws IOException{
		int length=N;
		if(arrayRank.size()<N){
			length=arrayRank.size();
		}
		for(int i=0;i<length;i++){
			this.bwResult_.write(this.indexToTerm_[arrayRank.get(i).key]+":"+qToIndex[arrayRank.get(i).key]+"//");
			if(arrayRank.get(i).key==trueProblem){
				this.bwResult_.write("\n");
				return true;
			}
		}
		this.bwResult_.write("\n");
		return false;
	}
	private void constructModeProb(int[][] dataTest,
			int[][] dataTrain,
			double[][] modeProb,int[] indexToTerm,
			int windowSize,double windowMaxValue){
		double prior=0.1;
		for(int u=0;u<dataTrain.length;u++){
			for(int l=0;l<modeProb[u].length;l++){
				int trainLength=dataTrain[u].length;
				int[] problemWindow=new int[windowSize];
				int trainWindowSize=windowSize-l;
				int testWindowSize=l;
				if(trainWindowSize<=0){
					testWindowSize=windowSize;
				}
				int order=0;
				for(int i=0;i<trainWindowSize;i++){
					problemWindow[order++]=indexToTerm[dataTrain[u][trainLength-trainWindowSize+i]];
				}
				for(int i=0;i<testWindowSize;i++){
					problemWindow[order++]=indexToTerm[dataTest[u][l-testWindowSize+i]];
				}
				int volumeSize=0;
				for(int i=0;i<windowSize-1;i++){
					if(Math.abs(problemWindow[i]-problemWindow[i+1])<windowMaxValue){
						volumeSize++;
					}
				}
				modeProb[u][l]=(1.0*volumeSize+prior)/(windowSize+2*prior);
			
			}
		}
		
		
	}
	
	private boolean ifAdd(int[] dataTest,
			int[] dataTrain,
			int[] indexToTerm,
			int l){
		int first=0;
		int second=0;
		int third=0;
	
			if(l==0){
				first=indexToTerm[dataTrain[dataTrain.length-3]];
				second=indexToTerm[dataTrain[dataTrain.length-2]];
				third=indexToTerm[dataTrain[dataTrain.length-1]];
			}
			if(l==1){
				 first=indexToTerm[dataTrain[dataTrain.length-2]];
				second=indexToTerm[dataTrain[dataTrain.length-1]];
				third=indexToTerm[dataTest[0]];
			
			}
			if(l==2){
				first=indexToTerm[dataTrain[dataTrain.length-1]];
				second=indexToTerm[dataTest[0]];
				third=indexToTerm[dataTest[1]];
			}
			if(l>2){
				first=indexToTerm[dataTest[l-3]];
				second=indexToTerm[dataTest[l-2]];
				third=indexToTerm[dataTest[l-1]];
			}
			if(first<second&&second<third){
				return true;
			}else{
				return false;
			}
		
		
		
	}
	
	private void constructThetaILast(int[] dataTest,
			int[] dataTrain,
			double[][] thetaILast,int[] indexToTerm,
			int windowSize){
		double prior=0.1;
		for(int l=0;l<dataTest.length;l++){
				int trainLength=dataTrain.length;
				int[] problemWindow=new int[windowSize];
				int[] thetaINum=new int[this.topicNumI_];
				int trainWindowSize=windowSize-l;
				int testWindowSize=l;
				if(trainWindowSize<=0){
					testWindowSize=windowSize;
				}
				int order=0;
				for(int i=0;i<trainWindowSize;i++){
					thetaINum[(indexToTerm[dataTrain[trainLength-trainWindowSize+i]]-1000)/100]++;
				}
				for(int i=0;i<testWindowSize;i++){
					thetaINum[(indexToTerm[dataTest[l-testWindowSize+i]]-1000)/100]++;
					
				}
				for(int v=0;v<this.topicNumI_;v++){
					thetaILast[l][v]=(thetaINum[v]+prior)/(windowSize+windowSize*prior);
				}
				
			}
		
		
		
	}
	
	
	private void constructThetaSLast(int[] dataTest,
			int[] dataTrain,
			double[][] thetaSLast,int[] indexToTerm,
			int windowSize){
		double prior=0.1;
		for(int l=0;l<dataTest.length;l++){
				int trainLength=dataTrain.length;
				int[] problemWindow=new int[windowSize];
				int[] thetaINum=new int[this.topicNumI_];
				int trainWindowSize=windowSize-l;
				int testWindowSize=l;
				if(trainWindowSize<=0){
					testWindowSize=windowSize;
				}
				int order=0;
				for(int i=0;i<trainWindowSize;i++){
					int pro=dataTrain[trainLength-trainWindowSize+i];
					for(int z=0;z<this.topicNumS_;z++){
						thetaSLast[l][z]+=this.questionTopicDistribution_[pro][z];
					}
				}
				for(int i=0;i<testWindowSize;i++){
					int pro=dataTest[l-testWindowSize+i];
					for(int z=0;z<this.topicNumS_;z++){
						thetaSLast[l][z]+=this.questionTopicDistribution_[pro][z];
					}
				}
				normalize(thetaSLast[l]);
				
				
			}
		
		
		
	}
	
	public void readFileRho(String inputFile) throws IOException{
		BufferedReader br=new FileReader(inputFile).getReader();
		String line;
		int order=-1;
		while((line=br.readLine())!=null){
			order++;
			if(order>=this.userNum_){
				break;
			}
			int index=line.indexOf(":");
			line=line.substring(index+1);
			String[] split=line.split("\t");
			double rhoI=Double.parseDouble(split[0]);
			double rhoS=Double.parseDouble(split[1]);
			this.rhoI_[order]=rhoI;
			this.rhoS_[order]=rhoS;
		}
		br.close();
		
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
	
	private void initialFeature(int[][] input,
			double[][] output){
		for(int i=0;i<input.length;i++){
			output[i]=new double[input[i].length];
		}
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
	
	private void readStateAssign(
			String fileStateAssign,
			int[] lastState) throws IOException{
		
		BufferedReader br=new FileReader(fileStateAssign).getReader();
		String line;
		
		int userOrder=-1;
		while((line=br.readLine())!=null){
			userOrder++;
			String[] split=line.split(" ");
			
			for(String str:split){
				if(str.length()>0){
					String[] split2=str.split(":");
					int index=Integer.parseInt(split2[0]);
					int state=Integer.parseInt(split2[1]);
					lastState[userOrder]=state;
					
				}
				
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
	
	private void computeSingleAlpha(
			double[] localTwo,
			double modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[] alpha_t_1,
			double[] alpha_t){
		double norm=0.0;
		for(int v=0;v<this.topicNumI_;v++){
			//0...v from k-1=z
			double sum1=0.0;
			for(int z_1=this.topicNumI_*3;z_1<this.states_;z_1++){
				sum1+=alpha_t_1[z_1]*modeProb*thetaI[v]*localTwo[v];
			}
			alpha_t[v]=sum1;
			//v...2v 
			double sum2=0.0;
			for(int v_1=0;v_1<this.topicNumI_*3;v_1++){
				sum2+=alpha_t_1[v_1]*modeProb*rhoI*thetaI[v]*localTwo[v];		
			}
			alpha_t[topicNumI_+v]=sum1;
			//2v... 3v k-1=k=v
			alpha_t[topicNumI_*2+v]=modeProb*(1-rhoI)
					*(alpha_t_1[v]+alpha_t_1[topicNumI_+v]+alpha_t_1[topicNumI_*2+v])
					*localTwo[v];
		}
		for(int z=0;z<topicNumS_;z++){
			//3v...3v+Z
			double sum1=0.0;
			for(int v_1=0;v_1<this.topicNumI_*3;v_1++){
				sum1+=alpha_t_1[v_1]*(1-modeProb)*thetaS[z]*localTwo[this.topicNumI_+z];
			}
			alpha_t[z+topicNumI_*3]=sum1;
			//3V+Z...3V+2Z
			double sum2=0.0;
			for(int z_1=this.topicNumI_*3;z_1<this.states_;z_1++){
				sum2+=alpha_t_1[z_1]*(1-modeProb)*rhoS*thetaS[z]*localTwo[this.topicNumI_+z];		
			}
			alpha_t[topicNumI_*3+z+topicNumS_]=sum2;
			//3V+2Z...3V+3Z
			alpha_t[topicNumI_*3+z+2*topicNumS_]=(1-modeProb)*(1-rhoS)
					*(alpha_t_1[topicNumI_*3+z]+alpha_t_1[topicNumI_*3+z+topicNumS_]+alpha_t_1[topicNumI_*3+z+2*topicNumS_])
					*localTwo[z+topicNumI_];
		}
//		for(int s=0;s<this.states_;s++){
//			norm+=(alpha_t[s]);
//		}
//		normalize(alpha_t, norm);
		
	}
	private void normalize(double[] vector,double norm){
		for(int i=0;i<vector.length;i++){
			vector[i]=(vector[i]/norm);
		}
	}
	private void normalize(double[] vector){
		double norm=0.0;
		for(int i=0;i<vector.length;i++){
			norm+=vector[i];
		}
		for(int i=0;i<vector.length;i++){
			vector[i]=(vector[i]/norm);
		}
	}
	BufferedWriter bwResult_;
	int topicNumI_;
	int topicNumS_;
	int states_;
	int userNum_;
	int questionNum_;
	int windowSize_;
	double windowMaxValue_;
	double alpha_;
	double omega_;
	double[] weight_;
	double[] userBase_;
	double[][] userTopicExpertise_;
	double[] questionBase_;
	double[][] questionTopicExpertise_;
	int[] indexToTerm_;//index to word
	HashMap<String,Integer> termToIndex_;
	double[][] questionTopicDistribution_;
	
	int[] lastState_;
	
	int[][] dataTrain_;
	int[][] dataTest_;
	int[][] dataTrainAC_;
	int[][] dataTestAC_;
	
	double[][] modeProb_;
	double[][] modeProbAC_;
	
	double[][] userTopicDistribution_;
	double[][] thetaI_;//estimated impression mode user-volumn distribution
	
	double[][] thetaS_;//estimated semantic mode user-topic distribution 
	double[][] phiI_;//estimated impression topic-word distribution phiI[v][w]
	
	double[][] phiS_;//estimated semantic topic-word distribution phiS[z][w]
	//	private double[] omega;//estimated  impression word distribution omega[w]
	double[] epsilon_;//estimated the feature weight control mode
	double[] rhoI_;//estimated the value control semantic topic consecutive
	double[] rhoS_;//estimated the value control semantic topic consecutive
	

}
