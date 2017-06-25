/**
 * 
 */
package Model;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import DataStruct.Documents;
import DataStruct.SortPairDouble;
import DataStruct.TwoInt;
import DataStruct.doubleDecimal;
import FileIO.FileWriter;
import SGD.CostFunctionZwhModel;
import SGD.DoubleVector;
import SGD.GradientDescent;
import SGD.GradientDescent.GradientDescentBuilder;

/**
 * @author wenhui zhang
 *
 */
public class zwhModel {
	public zwhModel(){
		
	}
	private void calculateModeProb(int d,double[] prob){
		int length=doc_[d].length;
		for(int n=0;n<length;n++){
			double sum=0.0;
			for(int f=0;f<this.featureNum_;f++){
				sum+=(this.epsilon_[f]*this.docFeatureDouble_[d][n][f]);
			}
			sum+=this.epsilon_[this.weightNum_-1];
			double value=1.0/(1.0+Math.exp(-1.0*sum));
			prob[n]=(value+this.Omega_)/(1.0+2*this.Omega_);
		}
	}
	private void constructFeatureLastAndNext(
			int iFeature,int sFeature){
		for(int d=0;d<this.docNum_;d++){
			int nd=doc_[d].length;
			for(int n=0;n<nd;n++){
				if(nd==1){
					this.docFeatureDouble_[d][n][iFeature]=0.0;
					this.docFeatureDouble_[d][n][sFeature]=0.0;
					continue;
				}
				if(n==0){
					if(isImpressionFearuteBySide(
							doc_[d][n],doc_[d][n+1])){
						this.docFeatureDouble_[d][n][iFeature]=1.0;
					}else{
						this.docFeatureDouble_[d][n][iFeature]=0.0;
					}
					if(isSemanticFearuteBySide(
							doc_[d][n],doc_[d][n+1])){
						this.docFeatureDouble_[d][n][sFeature]=1.0;
					}else{
						this.docFeatureDouble_[d][n][sFeature]=0.0;
					}
				}else{
					if(n==nd-1){
						if(isImpressionFearuteBySide(
								doc_[d][n-1],doc_[d][n])){
							this.docFeatureDouble_[d][n][iFeature]=1.0;
						}else{
							this.docFeatureDouble_[d][n][iFeature]=0.0;
						}
						if(isSemanticFearuteBySide(
								doc_[d][n-1],doc_[d][n])){
							this.docFeatureDouble_[d][n][sFeature]=1.0;
						}else{
							this.docFeatureDouble_[d][n][sFeature]=0.0;
						}
					}else{
						if(isImpressionFearuteByLastAndNext(
								doc_[d][n-1],doc_[d][n],doc_[d][n+1])){
							this.docFeatureDouble_[d][n][0]=1.0;
						}else{
							this.docFeatureDouble_[d][n][0]=0.0;
						}
						if(isSemanticFearuteByLastAndNext(
								doc_[d][n-1],doc_[d][n],doc_[d][n+1])){
							this.docFeatureDouble_[d][n][1]=1.0;
						}else{
							this.docFeatureDouble_[d][n][1]=0.0;
						}
					}
				}
				
			}
		}
	}
	private void constructFeatureForwardImpression(
			int locFeature,
			int MAXSIZE,
			int WINDOWSIZE){
		
		for(int d=0;d<this.docNum_;d++){
			int nd=doc_[d].length;
			
			for(int n=0;n<nd;n++){
				if(nd==1){
					this.docFeatureDouble_[d][n][locFeature]=0.0;
				}else{
					int now=n-1;
					int last=docProb_[d][n];
					while(now>=0&&n-now<=MAXSIZE){
						int probNow=Integer.parseInt(this.indexToTerm_.get(doc_[d][now]));
						if(Math.abs(last-docProb_[d][now])<=WINDOWSIZE){
							last=docProb_[d][now];
							now--;
						}else{
							break;
						}
					}
					if(now==n-1){
						this.docFeatureDouble_[d][n][locFeature]=0.0;
					}else{
						this.docFeatureDouble_[d][n][locFeature]=1.0*(n-1-now)/MAXSIZE;
					}
				}
			}
		}
	}
	private void constructFeatureBackWardImpression(
			int numFeature,
			int MAXSIZE,
			int WINDOWSIZE){
		
		for(int d=0;d<this.docNum_;d++){
			int nd=doc_[d].length;
			
			for(int n=0;n<nd;n++){
				if(nd==1){
					this.docFeatureDouble_[d][n][numFeature]=0.0;
				}else{
					int now=n+1;
					int last=docProb_[d][n];
					while(now<nd&&now-n<=MAXSIZE){
						if(Math.abs(last-docProb_[d][now])<=WINDOWSIZE){
							last=docProb_[d][now];
							now++;
						}else{
							break;
						}
					}
					if(now==n+1){
						this.docFeatureDouble_[d][n][numFeature]=0.0;
					}else{
						this.docFeatureDouble_[d][n][numFeature]=1.0*(now-n-1)/MAXSIZE;
					}
				}
			}
		}
	}
	private void constructFeatureForwardSemantic(
			int locFeature,
			int MAXSIZE,
			int WINDOWSIZE){
		
		for(int d=0;d<this.docNum_;d++){
			int nd=doc_[d].length;
			
			for(int n=0;n<nd;n++){
				if(nd==1){
					this.docFeatureDouble_[d][n][locFeature]=0.0;
				}else{
					int now=n-1;
					int last=docProb_[d][n];
					while(now>=0&&n-now<=MAXSIZE){
						int probNow=Integer.parseInt(this.indexToTerm_.get(doc_[d][now]));
						if(Math.abs(last-docProb_[d][now])>=WINDOWSIZE){
							last=docProb_[d][now];
							now--;
						}else{
							break;
						}
					}
					if(now==n-1){
						this.docFeatureDouble_[d][n][locFeature]=0.0;
					}else{
						this.docFeatureDouble_[d][n][locFeature]=1.0*(n-1-now)/MAXSIZE;
					}
				}
			}
		}
	}
	private void constructFeatureBackWardSemantic(
			int numFeature,
			int MAXSIZE,
			int WINDOWSIZE){
		
		for(int d=0;d<this.docNum_;d++){
			int nd=doc_[d].length;
			
			for(int n=0;n<nd;n++){
				if(nd==1){
					this.docFeatureDouble_[d][n][numFeature]=0.0;
				}else{
					int now=n+1;
					int last=docProb_[d][n];
					while(now<nd&&now-n<=MAXSIZE){
						if(Math.abs(last-docProb_[d][now])>=WINDOWSIZE){
							last=docProb_[d][now];
							now++;
						}else{
							break;
						}
					}
					if(now==n+1){
						this.docFeatureDouble_[d][n][numFeature]=0.0;
					}else{
						this.docFeatureDouble_[d][n][numFeature]=1.0*(now-n-1)/MAXSIZE;
					}
				}
			}
		}
	}
	
private void constructFeatureRatio(
			int numFeature,
			int windowSize){
		for(int d=0;d<this.docNum_;d++){
			int length=doc_[d].length;
			for(int n=0;n<length;n++){
				int all=0;
				int same=0;
				for(int bias=n-3;bias<n;bias++){
					if(bias>=0){
						all++;
						if(Math.abs(this.docProb_[d][bias]-this.docProb_[d][n])<windowSize){
//							this.docFeatureDouble_[d][n][bias-n+3]=1.0;
//							this.docFeatureDouble_[d][n][bias-n+9]=0.0;
							same++;
						}	
					}
					
				}
				for(int bias=n+1;bias<=n+3;bias++){
					if((bias<length)){
						all++;
						if(Math.abs(this.docProb_[d][bias]-this.docProb_[d][n])<windowSize){
//							this.docFeatureDouble_[d][n][bias-n+8]=0.0;
//							this.docFeatureDouble_[d][n][bias-n+2]=1.0;
							same++;
						}
					}
//						
					
				}
				this.docFeatureDouble_[d][n][numFeature]=((1.0*same/all)-0.5)*2;
			}
		}
	}

private void constructFeatureWeightRatio(
		int numFeature,
		int windowSize){
	int Width=3;
	for(int d=0;d<this.docNum_;d++){
		int length=doc_[d].length;
		for(int n=0;n<length;n++){
			int all=0;
			int same=0;
			for(int bias=n-Width;bias<n;bias++){
				if(bias>=0){
					int weight=Width-(n-bias)+1;
					all+=(weight);
					if(Math.abs(this.docProb_[d][bias]-this.docProb_[d][n])<windowSize){
//						this.docFeatureDouble_[d][n][bias-n+3]=1.0;
//						this.docFeatureDouble_[d][n][bias-n+9]=0.0;
						same+=weight;
					}	
				}
				
			}
			for(int bias=n+1;bias<=n+Width;bias++){
				if((bias<length)){
					int weight=Width-(bias-n-1);
					all+=weight;
					if(Math.abs(this.docProb_[d][bias]-this.docProb_[d][n])<windowSize){
//						this.docFeatureDouble_[d][n][bias-n+8]=0.0;
//						this.docFeatureDouble_[d][n][bias-n+2]=1.0;
						same+=weight;
					}
				}
//					
				
			}
			this.docFeatureDouble_[d][n][numFeature]=((1.0*same/all)-0.5)*2;
		}
	}
}

//	
	private void constructFeature(){
		constructFeatureLastAndNext(0,1);
		constructFeatureForwardImpression(2,5,10);
		constructFeatureBackWardImpression(3,5,10);
		constructFeatureForwardSemantic(4,5,20);
		constructFeatureBackWardSemantic(5,5,20);
		constructFeatureRatio(6,10);
		constructFeatureWeightRatio(7,10);
	}
//	
	private void countImpressionTopicInDoc(int d,double[] cuv){
		for(int v=0;v<this.topicINum_;v++){
			cuv[v]=0.0;
		}
		for(int n=0;n<doc_[d].length;n++){
			for(int v=0;v<this.topicINum_;v++){
				//c=1 ,y=1 s=[1.2...K-1]
				cuv[v]+=(p_zyc_dw_[d][n][v]+p_zyc_dw_[d][n][v+this.topicINum_]);
			}
		}
		
	}
	
	private void countSemanticTopicWord(double[][] czw){
		for(int z=0;z<this.topicSNum_;z++){
			for(int w=0;w<this.wordNum_;w++){
				czw[z][w]=0.0;
			}
		}
		for(int d=0;d<this.docNum_;d++){
			for(int n=0;n<doc_[d].length;n++){
				int word=doc_[d][n];
				for(int z=0;z<this.topicSNum_;z++){
					czw[z][word]+=(
							p_zyc_dw_[d][n][z+this.topicINum_*3]+
							p_zyc_dw_[d][n][z+this.topicINum_*3+this.topicSNum_]+
							p_zyc_dw_[d][n][z+this.topicINum_*3+2*this.topicSNum_]);
				}
			}
		}
	}
	
	private void countImpressionTopicWord(double[][] cvw){
		for(int v=0;v<this.topicINum_;v++){
			for(int w=0;w<this.wordNum_;w++){
				cvw[v][w]=0.0;
			}
		}
		for(int d=0;d<this.docNum_;d++){
			for(int n=0;n<doc_[d].length;n++){
				int word=doc_[d][n];
				int volumnNum=docVolumn_[d][n];
				for(int v=0;v<this.topicINum_;v++){
					if(volumnNum==v){
						cvw[v][word]+=(
								p_zyc_dw_[d][n][v]+
								p_zyc_dw_[d][n][v+this.topicINum_]+
								p_zyc_dw_[d][n][v+this.topicINum_*2]);
						
					}
			}
			}
		}
	}
	
	
	private void countSemanticTopicsInDoc(int d,double[] cdz){
		for(int z=0;z<this.topicSNum_;z++){
			cdz[z]=0.0;
		}
		for(int n=0;n<doc_[d].length;n++){
			for(int z=0;z<this.topicSNum_;z++){
				//c=1 ,y=1 s=[1.2...K-1]
				cdz[z]+=(p_zyc_dw_[d][n][z+this.topicINum_*3]+p_zyc_dw_[d][n][z+this.topicSNum_+this.topicINum_*3]);
			}
		}
	}
	
	private void eStep(){
		this.logLike_=0.0;
		for(int i=0;i<this.docNum_;i++){
//			System.out.println(":"+this.S);
			double ll=eStepSingleDoc(i);
			logLike_+=ll;
		}
//		System.out.println(this.logLike);
		IncorporatePriorsIntoLikelihood();
		
	}
	private double eStepSingleDoc(int d){
		double[][] localTwo=new double[doc_[d].length][this.topicAllNum_];//impression topic plus semantic topic
		for(int n=0;n<doc_[d].length;n++){
			int word=doc_[d][n];
			//K1=1 impression topic word:omega
			for(int k=0;k<this.topicINum_;k++){
				localTwo[n][k]=this.phiI_[k][word];
			}
			for(int k=0;k<this.topicSNum_;k++){
				localTwo[n][k+this.topicINum_]=this.phiS_[k][word];
			}
		}
		double[] modeprob=new double[doc_[d].length];
		calculateModeProb(d,modeprob);
		//init prob
		
		double[] initProb=new double[this.stateNum_];
		for(int v=0;v<this.topicINum_;v++){//impression
//			System.out.println(d+":"+v);
			initProb[v]=modeprob[0]*thetaI_[d][v];
			initProb[v+topicINum_]=0.0;
			initProb[v+topicINum_*2]=0.0;
		}
		for(int z=0;z<this.topicSNum_;z++){//impression
			initProb[topicINum_*3+z]=(1-modeprob[0])*thetaS_[d][z];
			initProb[topicINum_*3+z+topicSNum_]=0.0;
			initProb[topicINum_*3+z+2*topicSNum_]=0.0;
		}
		
		
		zwhFastRestrictedHMM f=new zwhFastRestrictedHMM();
		double ll=f.forwardBackWard(modeprob, rhoI_[d],rhoS_[d], 
				thetaI_[d], thetaS_[d], localTwo, 
				initProb, p_zyc_dw_[d],p_hidden_[d]);
		
		return ll;
		
	}
		
	
	
	private void findEpsilon() throws IOException{
		Random rand=new Random();
		
		double[] input=new double[this.weightNum_];
		initialWeight(input);
//		for(int w=0;w<this.weightNum_;w++){
//			input[w]=this.epsilon_[w];
//		}
//		for(int f=0;f<this.featureNum_;f++){
//				input[f]=rand.nextDouble();
//			if(rand.nextDouble()>0.5){
//				input[f]=input[f]*(-1.0);
//			}
//		}
			
			DoubleVector pInput=new DoubleVector(input);
			GradientDescentBuilder gdBuilder=GradientDescentBuilder.create(this.learningRate_);
//			gdBuilder.boldDriver();
			gdBuilder.breakOnDifference(this.limit_);
			BufferedWriter bw=FileWriter.createWriter(outputFilebase_+itNow_+".it");
			GradientDescent gd=new 
					GradientDescent(gdBuilder,bw);
			
			
			CostFunctionZwhModel cfz=new 
					CostFunctionZwhModel(this.recordNum_, 
							featureNum_, this.p_hidden_,
							this.docFeatureDouble_,this.LAMDA_,this.lengthToDocLoc_,256);
			gd.minimize(cfz, pInput,this.maxIteration_,false);
			for(int w=0;w<this.weightNum_;w++){
				this.epsilon_[w]=pInput.get(w);
			}
		
		
	}
	
	
	
	private void findPhiS(){
		double[][] czw=new double[this.topicSNum_][this.wordNum_];//
		countSemanticTopicWord(czw);
		for(int z=0;z<this.topicSNum_;z++){
			double norm=0.0;
			for(int w=0;w<this.wordNum_;w++){
				this.phiS_[z][w]=czw[z][w]+BETAS_-1;
				norm+=this.phiS_[z][w];
			}
			normalize(this.phiS_[z], norm);
		}
	}
	private void findPhiI(){
		double[][] cvw=new double[this.topicINum_][this.wordNum_];//
		countImpressionTopicWord(cvw);
		for(int v=0;v<this.topicINum_;v++){
			double norm=0.0;
			for(int w=0;w<this.wordNum_;w++){
//				System.out.println(cvw[v][w]);
				this.phiI_[v][w]=cvw[v][w]+BETAI_-1;
				norm+=this.phiI_[v][w];
			}
			normalize(this.phiI_[v], norm);
		}
	}
	
	private void findRho(){
		for(int d=0;d<this.docNum_;d++){
			double lotRhoI=0.0;
			double lotAllI=0.0;
			double lotRhoS=0.0;
			double lotAllS=0.0;
			for(int n=0;n<doc_[d].length;n++){
				for(int s=this.topicINum_;s<this.topicINum_*2;s++){
					//c=1
					lotRhoI+=p_zyc_dw_[d][n][s];
					lotAllI+=p_zyc_dw_[d][n][s];
				}
				for(int s=this.topicINum_*2;s<this.topicINum_*3;s++){
					lotAllI+=p_zyc_dw_[d][n][s];
				}
				for(int s=this.topicINum_*3+this.topicSNum_;s<this.topicINum_*3+this.topicSNum_*2;s++){
					//c=1
					lotRhoS+=p_zyc_dw_[d][n][s];
					lotAllS+=p_zyc_dw_[d][n][s];
				}
				for(int s=this.topicINum_*3+this.topicSNum_*2;s<this.stateNum_;s++){
					lotAllS+=p_zyc_dw_[d][n][s];
				}
			}
//			System.out.println(lotRhoI+":"+lotAllI);
			this.rhoI_[d]=(lotRhoI+this.PSII_)/(lotAllI+2*this.PSII_);
			this.rhoS_[d]=(lotRhoS+this.PSIS_)/(lotAllS+2*this.PSIS_);
			
		}
		
	}
	
	private void findSingleThetaS(int d){
		double norm=0.0;
		double[] cdz=new double[this.topicSNum_];
		countSemanticTopicsInDoc(d, cdz);
		for(int z=0;z<this.topicSNum_;z++){
			thetaS_[d][z]=cdz[z]+ALPHAS_-1;
			norm+=thetaS_[d][z];
		}
		normalize(thetaS_[d], norm);
		
	}
	private void findSingleThetaI(int d){
		double norm=0.0;
		double[] cdv=new double[this.topicINum_];
		countImpressionTopicInDoc(d, cdv);
		for(int v=0;v<this.topicINum_;v++){
			thetaI_[d][v]=cdv[v]+ALPHAI_-1;
			norm+=thetaI_[d][v];
		}
		normalize(thetaI_[d], norm);
		
	}
	
	private void findThetaI(){
		for(int d=0;d<this.docNum_;d++){
			findSingleThetaI(d);
		}
	}
	private void findThetaS(){
		for(int d=0;d<this.docNum_;d++){
			findSingleThetaS(d);
		}
	}
	private void IncorporatePriorsIntoLikelihood(){
		for(int d=0;d<this.docNum_;d++){
			for(int z=0;z<this.topicSNum_;z++){
				this.logLike_+=(this.ALPHAS_-1)*Math.log(thetaS_[d][z]);
			}
			for(int v=0;v<this.topicINum_;v++){
				this.logLike_+=(this.ALPHAI_-1)*Math.log(thetaI_[d][v]);
			}
			this.logLike_+=(this.PSII_*Math.log(rhoI_[d]*(1-rhoI_[d])));//beta 
			this.logLike_+=(this.PSIS_*Math.log(rhoS_[d]*(1-rhoS_[d])));//beta 
		}
		
		for(int z=0;z<this.topicSNum_;z++){
			for(int w=0;w<this.wordNum_;w++){
				this.logLike_+=(BETAS_-1)*Math.log(phiS_[z][w]);
			}
		}
		for(int v=0;v<this.topicINum_;v++){
			for(int w=0;w<this.wordNum_;w++){
				this.logLike_+=(BETAI_-1)*Math.log(phiI_[v][w]);
			}
		}
		
		
	}
	
	public void infer() throws IOException{
		System.out.println("start infer:");
		saveAll(this.outputFilebase_+"_"+(0));
		saveWordMap(this.outputFilebase_+"_wordmap");
		for(int i=0;i<this.iteration_;i++){
			this.itNow_=i;
//			if(i>15){
//				System.out.println("l");
//			}
			eStep();
//			saveHiddenData(this.outputFilebase_+"_"+(i+1)+"test");
			mStep();
			
//			System.out.println(this.epsilon+":"+this.rho);
			System.out.println("it:"+i+"\tloglikelyhood:"+logLike_);
//			saveAll(this.outputFilebase+"_"+i);
			if((i+1)%(this.iteration_/this.saveNum_)==0){
				saveAll(this.outputFilebase_+"_"+(i+1));
			}
		}
		saveAll(this.outputFilebase_+"_final");
	}
	public void initModel(int topicI,
			int topicS,
			int featureNum,
			int iterations,
			int saveNum,
			double alphaI,double alphaS,
			double betaI,double betaS,
			double omega,
			double psiI,
			double psiS,
			double lamda,
			double learningRate,
			double limit,
			int maxIteration,
			Documents documents,
			HashMap<String,Integer> dicTermToVolumn,
			String outputBase) throws IOException{
		
		this.lengthToDocLoc_=new ArrayList<>();
		this.outputFilebase_=outputBase;
		this.topicINum_=topicI;
		this.topicSNum_=topicS;
		this.topicAllNum_=this.topicINum_+this.topicSNum_;
		
		this.featureNum_=featureNum;
		this.weightNum_=featureNum+1;
		this.iteration_=iterations;
		this.saveNum_=saveNum;
		this.ALPHAI_=alphaI;
		this.ALPHAS_=alphaS;
		this.BETAI_=betaI;
		this.BETAS_=betaS;
		this.Omega_=omega;
		this.PSII_=psiI;
		this.PSIS_=psiS;
		this.LAMDA_=lamda;
		this.learningRate_=learningRate;
		this.limit_=limit;
		this.maxIteration_=maxIteration;
		this.wordNum_=documents.termCountMap.size();
		this.docNum_=documents.docs.size();
		this.stateNum_=3*this.topicINum_+3*this.topicSNum_;//the hidden state 
		//read doc
		this.doc_=new int[this.docNum_][];
		this.docProb_=new int[this.docNum_][];
		this.docFeatureDouble_=new double[this.docNum_][][];
		this.docVolumn_=new int[this.docNum_][];
		this.recordNum_=0;
		
		this.indexToImpressionVolumn_=new int[this.wordNum_];
		this.indexToTerm_=new ArrayList<>();
		int loc=0;
		for(String val:documents.indexToTermMap){
			this.indexToTerm_.add(val);
			int volumnValue=dicTermToVolumn.get(val);
			if(volumnValue>=this.topicINum_){
				System.err.println("impression topic error");
				System.exit(0);
			}
			indexToImpressionVolumn_[loc++]=volumnValue;
			
		}
		for(int d=0;d<this.docNum_;d++){
			int nd=documents.docs.get(d).docWords.length;
			this.recordNum_+=nd;
			doc_[d]=new int[nd];
			docProb_[d]=new int[nd];
			docFeatureDouble_[d]=new double[nd][featureNum];
			docVolumn_[d]=new int[nd];
			for(int n=0;n<nd;n++){
				doc_[d][n]=documents.docs.get(d).docWords[n];
				docProb_[d][n]=Integer.parseInt(this.indexToTerm_.get(doc_[d][n]));
				this.lengthToDocLoc_.add(new TwoInt(d, n));
			}
		}
		if(this.lengthToDocLoc_.size()!=this.recordNum_){
			System.out.println("length to Doc loc error");
			System.exit(0);
		}
		
		
		for(int d=0;d<this.docNum_;d++){
			for(int n=0;n<this.doc_[d].length;n++){
				docVolumn_[d][n]=dicTermToVolumn.get(indexToTerm_.get(doc_[d][n]));
				
			}
		}
		constructFeature();
		saveFeature(this.outputFilebase_);
		//parameter
		this.thetaS_=new double[this.docNum_][this.topicSNum_];//user-topic
		this.thetaI_=new double[this.docNum_][this.topicINum_];//volumn-topic
		this.phiI_=new double[this.topicINum_][this.wordNum_];//
		this.phiS_=new double[this.topicSNum_][this.wordNum_];//
		
		this.rhoS_=new double[this.docNum_];
		this.rhoI_=new double[this.docNum_];
		this.epsilon_=new double[this.weightNum_];
		//initialize value
		//
		initialWeight(epsilon_);
		for(int i=0;i<this.docNum_;i++){
			randProb(thetaS_[i]);
			randProb(thetaI_[i]);
			rhoI_[i]=0.2;
			rhoS_[i]=0.2;
			
			
		}
		
		for(int i=0;i<this.topicINum_;i++){
			randProbPhiI(phiI_[i],i);
//			randProb(phiI_[i]);
		}
		for(int i=0;i<this.topicSNum_;i++){
			randProb(phiS_[i]);
		}
//		this.rho=1.0-new Random().nextDouble()/30;
		//do not need initial because first e-step
		p_zyc_dw_=new double[this.docNum_][][];
		p_hidden_=new double[this.docNum_][];
		for(int i=0;i<this.docNum_;i++){
			p_zyc_dw_[i]=new double[doc_[i].length][];
			p_hidden_[i]=new double[doc_[i].length];
			for(int j=0;j<doc_[i].length;j++){
				p_zyc_dw_[i][j]=new double[this.stateNum_];
			}
		}
	}
	private void initialWeight(double[] vector){
	
	
		
		vector[0]=1.0;
		vector[1]=-5.0;
		vector[2]=1.0;
		vector[3]=1.0;
		vector[4]=-1.0;
		vector[5]=-1.0;
		vector[6]=1.0;
		vector[7]=1.0;
		vector[weightNum_-1]=0.0;
//		vector[2]=1.0;
//		vector[3]=0.1;
//		for(int i=0;i<this.weightNum_;i++){
//			norm+=vector[i];
//		}
//		normalize(vector, norm);
		
	}
	
	private boolean isSemanticFearuteByLastAndNext(
			int indexLast,int indexNow,int indexNext){
		final int windowSize=10;
		int numLast=Integer.parseInt(this.indexToTerm_.get(indexLast));
		int numNow=Integer.parseInt(this.indexToTerm_.get(indexNow));
		int numNext=Integer.parseInt(this.indexToTerm_.get(indexNext));
		if((Math.abs(numLast-numNow)>windowSize)&&(Math.abs(numNow-numNext)>windowSize)){
			return true;
		}else{
			return false;
		}
		
		
	}
	private boolean isImpressionFearuteByLastAndNext(
			int indexLast,int indexNow,int indexNext){
		final int windowSize=10;
		int numLast=Integer.parseInt(this.indexToTerm_.get(indexLast));
		int numNow=Integer.parseInt(this.indexToTerm_.get(indexNow));
		int numNext=Integer.parseInt(this.indexToTerm_.get(indexNext));
		if((Math.abs(numLast-numNow)<=windowSize)
				&&(Math.abs(numNow-numNext)<=windowSize)
				&&(Math.abs(numLast-numNext)<=windowSize*2)){
			return true;
		}else{
			return false;
		}
		
		
	}
	private boolean isSemanticFearuteBySide(
			int indexLast,int indexNow){
		final int windowSize=10;
		int numLast=Integer.parseInt(this.indexToTerm_.get(indexLast));
		int numNow=Integer.parseInt(this.indexToTerm_.get(indexNow));
		if((Math.abs(numLast-numNow)>windowSize)){
			return true;
		}else{
			return false;
		}
		
		
	}
	private boolean isImpressionFearuteBySide(
			int indexLast,int indexNow){
		final int windowSize=10;
		int numLast=Integer.parseInt(this.indexToTerm_.get(indexLast));
		int numNow=Integer.parseInt(this.indexToTerm_.get(indexNow));
		if((Math.abs(numLast-numNow)<=windowSize)){
			return true;
		}else{
			return false;
		}
		
		
	}
	
	private void mStep() throws IOException{
		findThetaS();
		findThetaI();
		findPhiI();
		findPhiS();
		
		findRho();
		findEpsilon();
		
		
	}
	private void normalize(double[] vector,double norm){
		for(int i=0;i<vector.length;i++){
			vector[i]=(vector[i]/norm);
		}
	}
	private void randProb(double[] vector,int start){
		Random rand=new Random();
		double norm=0;
		for(int i=start;i<vector.length;i++){
			vector[i]=rand.nextDouble();
			norm+=vector[i];
		}
		normalize(vector, norm);
	}
	private void randProb(double[] vector){
		Random rand=new Random();
		double norm=0;
		for(int i=0;i<vector.length;i++){
			vector[i]=rand.nextDouble();
			norm+=vector[i];
		}
		normalize(vector, norm);
	}
	private void randProbPhiI(double[] vector,int volumn){
		Random rand=new Random();
		double norm=0;
		for(int i=0;i<vector.length;i++){
			if(indexToImpressionVolumn_[i]==volumn){
				vector[i]=rand.nextDouble();
			}else{
				vector[i]=0.0;
			}
			
			norm+=vector[i];
		}
		normalize(vector, norm);
	}
	private void randProb(double[] vector,HashSet<Integer> set){
		Random rand=new Random();
		double norm=0;
		for(int i=0;i<vector.length;i++){
			if(set.contains(i)){
				vector[i]=rand.nextDouble();
				norm+=vector[i];
			}else{
				vector[i]=0.0;
			}
		}
		normalize(vector, norm);
	}
	private void saveAll(String filename) throws IOException{
		savePhiS(filename);
		savePhiI(filename);
		saveEpsilon(filename);
		saveRho(filename);
		saveThetaI(filename);
		saveThetaS(filename);
		saveHiddenData(filename);
		saveTopicAssign(filename);
		
	}
	public void saveTopicAssign(String filename) throws IOException{
		BufferedWriter bwS=FileWriter.createWriter(filename+".assignStates");
		BufferedWriter bwT=FileWriter.createWriter(filename+".assignTopic");
		zwhFastRestrictedViterbi frv=new zwhFastRestrictedViterbi();
		
		for(int d=0;d<this.docNum_;d++){
			double[][] localTwo=new double[doc_[d].length][this.topicAllNum_];
			for(int n=0;n<doc_[d].length;n++){
				int word=doc_[d][n];
				//K1=1 impression topic word:omega
				for(int k=0;k<this.topicINum_;k++){
					localTwo[n][k]=this.phiI_[k][word];
				}
				for(int k=0;k<this.topicSNum_;k++){
					localTwo[n][k+this.topicINum_]=this.phiS_[k][word];
				}
			}
			double[] modeProb=new double[doc_[d].length];
			calculateModeProb(d,modeProb);
			int[] bestPath=new int[doc_[d].length];
			frv.viterbi(modeProb, 
					rhoI_[d],rhoS_[d],thetaI_[d], thetaS_[d], localTwo, bestPath);
			int[] printPath=new int[doc_[d].length];
			for(int i=0;i<doc_[d].length;i++){
//				System.out.println(doc[d][i]);
				if(bestPath[i]<this.topicINum_){
					printPath[i]=bestPath[i];
					continue;
				}
				if(bestPath[i]<this.topicINum_*2){
					printPath[i]=bestPath[i]-this.topicINum_;
					continue;
				}
				if(bestPath[i]<this.topicINum_*3){
					printPath[i]=bestPath[i]-this.topicINum_*2;
					continue;
				}
				if(bestPath[i]<this.topicINum_*3+this.topicSNum_){
					printPath[i]=bestPath[i]-this.topicINum_*2;
					continue;
				}
				if(bestPath[i]<this.topicINum_*3+this.topicSNum_*2){
					printPath[i]=bestPath[i]-this.topicINum_*2-this.topicSNum_;
					continue;
				}
				if(bestPath[i]<this.topicINum_*3+this.topicSNum_*3){
					printPath[i]=bestPath[i]-this.topicINum_*2-this.topicSNum_*2;
					continue;
				}
				
			}
			for(int n=0;n<doc_[d].length;n++){
				bwS.write(this.indexToTerm_.get(doc_[d][n])+":"+bestPath[n]+" ");
				bwT.write(this.indexToTerm_.get(doc_[d][n])+":"+printPath[n]+" ");	
			}
			bwS.write("\n");
			bwT.write("\n");
		}
		bwS.close();
		bwT.close();
	}
	private void saveFeature(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".feature");
		
		
		for(int d=0;d<this.docNum_;d++){
			for(int n=0;n<this.doc_[d].length;n++){
				int word=this.doc_[d][n];
				bw.write(this.indexToTerm_.get(doc_[d][n])+":");
				for(int f=0;f<this.featureNum_;f++){
					bw.write(doubleDecimal.getString(this.docFeatureDouble_[d][n][f])+",");
				}
				bw.write("\t");
			}
			bw.write("\n");
		}
		bw.close();
		
	}
	
	private void saveHiddenData(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".hidden");
		
		
		for(int d=0;d<this.docNum_;d++){
			for(int n=0;n<this.doc_[d].length;n++){
				int word=this.doc_[d][n];
				bw.write(this.indexToTerm_.get(doc_[d][n])+":");
				bw.write(doubleDecimal.getString(this.p_hidden_[d][n])+",");
				
				bw.write("\t");
			}
			bw.write("\n");
		}
		bw.close();
		
	}
	
	
	private void saveEpsilon(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".eps");
		BufferedWriter bwPro=FileWriter.createWriter(filename+".epsProb");
		
		for(int w=0;w<this.weightNum_;w++){
			bw.write(doubleDecimal.getString(this.epsilon_[w])+"\t");
		}
		
		bw.close();
		for(int d=0;d<this.docNum_;d++){
			double[] prob=new double[doc_[d].length];
			calculateModeProb(d, prob);
			for(int n=0;n<doc_[d].length;n++){
				bwPro.write(this.indexToTerm_.get(doc_[d][n])+":"+
			doubleDecimal.getString(prob[n])+" ");
			}
			bwPro.write("\n");
		}
		
		
		bwPro.close();
	}
	private void saveThetaI(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".thetaI");
		for(int d=0;d<this.docNum_;d++){
			for(int v=0;v<this.topicINum_;v++){
				bw.write(doubleDecimal.getString(this.thetaI_[d][v])+"\t");
			}
			bw.write("\n");
		}
		bw.close();
	}
	private void saveThetaS(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".thetaS");
		for(int d=0;d<this.docNum_;d++){
			for(int k=0;k<this.topicSNum_;k++){
				bw.write(doubleDecimal.getString(this.thetaS_[d][k])+"\t");
			}
			bw.write("\n");
		}
		bw.close();
	}
	
	
	private void saveRho(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".rho");
		double sumI=0.0;
		double sumS=0.0;
		for(int i=0;i<this.docNum_;i++){
			bw.write("user "+(i+1)+":"+this.rhoI_[i]+"\t"+this.rhoS_[i]+"\n");
			sumI+=this.rhoI_[i];
			sumS+=this.rhoS_[i];
		}
		bw.write("average I:\t"+sumI/this.docNum_);
		bw.write("average S:\t"+sumS/this.docNum_);
		bw.close();
	}
	private void savePhiI(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".phiI");
	
		for(int v=0;v<this.topicINum_;v++){
			
			for(int w=0;w<this.wordNum_;w++){
				bw.write(w+":"+this.phiI_[v][w]+"\t");
			}
			bw.write("\n");
			
		}
		bw.close();
	}
	
	private void saveWordMap(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".txt");
		for(int i=0;i<this.wordNum_;i++){
			bw.write(i+"\t"+this.indexToTerm_.get(i)+"\n");
		}
		bw.close();
		
	}
	
	private void savePhiS(String filename) throws IOException{
		BufferedWriter bw=FileWriter.createWriter(filename+".phiS");
		BufferedWriter bwTopic=FileWriter.createWriter(filename+".topic");
		
		for(int z=0;z<this.topicSNum_;z++){
			int topicwords=20;
			ArrayList<SortPairDouble> array=new ArrayList<>();
			for(int w=0;w<this.wordNum_;w++){
				array.add(new SortPairDouble(w,phiS_[z][w]));
			}
			Collections.sort(array);
			if(array.size()<20){
				topicwords=array.size();
			}
			bwTopic.write("topic "+z+":\n");
			for(int t=0;t<topicwords;t++){
				bwTopic.write("\t\t"+this.indexToTerm_.get(array.get(t).key)+"\t"+array.get(t).value+"\n");
			}
			for(int w=0;w<this.wordNum_;w++){
				bw.write(w+":"+this.phiS_[z][w]+"\t");
			}
			bw.write("\n");
			
		}
		bw.close();
		bwTopic.close();
	}
	
	private int topicINum_;//number of topic
	private int topicSNum_;//number of topic
	private int topicAllNum_;//number of topic
	private int wordNum_;//number of words
	private int docNum_;//number of doc
	private int recordNum_;//number of all record
	private int stateNum_;//number of hidden states
	private int featureNum_;//number of feature controling mode
	private int weightNum_;//feature plus bias
//	private int volumnNum;//number of volumn
	private int iteration_;//EM iteration
	private int saveNum_;//for save parameter
	private int itNow_;//the now Itration
	private double[][] thetaI_;//estimated impression mode user-volumn distribution
	private double[][] thetaS_;//estimated semantic mode user-topic distribution 
	private double[][] phiI_;//estimated impression topic-word distribution phiI[v][w]
	private double[][] phiS_;//estimated semantic topic-word distribution phiS[z][w]
	//	private double[] omega;//estimated  impression word distribution omega[w]
	private double[] epsilon_;//estimated the feature weight control mode
	private double[] rhoI_;//estimated the value control semantic topic consecutive
	private double[] rhoS_;//estimated the value control semantic topic consecutive
	
	private double[][][] p_zyc_dw_;      //the state probability Pr（z,y,c|d,w)
	private double[][] p_hidden_;//the mode probe
	private double logLike_;//loglikelihood
	private int[][] doc_;//the word index
	private int[][] docProb_;
	private int[][] docVolumn_;
	private double[][][] docFeatureDouble_;
	private double ALPHAI_;//dirichlet prior of semantic topic
	private double ALPHAS_;
	private double BETAI_;//dirichlet prior of impression 
	private double BETAS_;//dirichlet prior of semantic 
	private double PSII_;//rho prior
	private double PSIS_;//rho prior
	private double Omega_;// y prior
	private double LAMDA_;//sgd parameter
	private double learningRate_;//sgd parameter
	private double limit_;//sgd parameter
	private int maxIteration_;//sgd parameter
	private ArrayList<String> indexToTerm_;//index to word
	private int[] indexToImpressionVolumn_;
//	private int[] impressionTopicCount;
	private String outputFilebase_;
	private ArrayList<TwoInt> lengthToDocLoc_;
	
}
