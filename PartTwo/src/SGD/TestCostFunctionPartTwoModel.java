/**
 * 
 */
package SGD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import DataStruct.TriplePairIntIntInt;
import DataStruct.TwoInt;

/**
 * @author wenhui zhang
 *
 */
public class TestCostFunctionPartTwoModel implements CostFunction{
	private int size_;
	
	private int miniBatchNum_;
	private int parameterNum_;
	private int playerNum_;
	private int topicNum_;
	private ArrayList<TriplePairIntIntInt> arrayAllData_;
	private double[][] questionTopicDistribution_;
	private double[] priorPlayerMean_;
	private double[] priorPlayerSigma_;
	private double lamda_;
	private double alpha_;
	private double beta1_;
	private double beta2_;
	public TestCostFunctionPartTwoModel(int size,
			int miniBatchNum,
			int playerNum,
			int parameterNum,
			int topicNum,
			ArrayList<TriplePairIntIntInt> arrayAllData,
			double[][] questionTopicDistribution,
			double[] priorPlayerMean,
			double[] priorPlayerSigma,
			double alpha,
			double lamda,
			double beta1,
			double beta2
			) {
		// TODO Auto-generated constructor stub
		this.beta1_=beta1;
		this.beta2_=beta2;
		this.size_=size;
		this.arrayAllData_=arrayAllData;
		this.lamda_=lamda;
		this.miniBatchNum_=miniBatchNum;
		this.alpha_=alpha;
		this.parameterNum_=parameterNum;
		this.playerNum_=playerNum;
		this.priorPlayerMean_=priorPlayerMean;
		this.priorPlayerSigma_=priorPlayerSigma;
		this.questionTopicDistribution_=questionTopicDistribution;
		this.topicNum_=topicNum;
	
		
	}
	public ArrayList<TriplePairIntIntInt> getBatchData(int part){
//		System.out.println(part+":"+this.size_);
		int startIndex=(int)(((long)part)*this.size_/this.miniBatchNum_);
//		System.out.println(startIndex);
		int endIndex=-1;
		if(part==this.miniBatchNum_-1){
			endIndex=this.size_;
		}else{
			endIndex=(int)(((long)part+1)*this.size_/this.miniBatchNum_);
		}
//		System.out.println(endIndex);
		ArrayList<TriplePairIntIntInt> array=new ArrayList<>();
		for(int i=startIndex;i<endIndex;i++){
			array.add(this.arrayAllData_.get(i));
		}
		return array;
		
	}
	public double calculatePrecisionAfterUpdate(int iteration,double[] last){
//		if(iteration==0){
//			System.out.println("last none");
//			return ;
//		}
		ArrayList<TriplePairIntIntInt> arraySample=
				getBatchData((iteration)%miniBatchNum_);
		int baseStartIndex=this.playerNum_*this.topicNum_;
		int weightStartIndex=this.playerNum_*(this.topicNum_+1);
		int numAll=arraySample.size();
		int numP=0;
		for(TriplePairIntIntInt pair:arraySample){
			int win=pair.a_;
			int lose=pair.b_;
			int context=pair.c_;
			double value=0.0;
//			value+=(alpha_*(last[baseStartIndex+win]-last[baseStartIndex+lose])
//					);
			for(int t=0;t<topicNum_;t++){
				double topicValue=(last[weightStartIndex+t]
						*questionTopicDistribution_[context][t]
						*(last[win*topicNum_+t]-last[lose*topicNum_+t]));
				value+=topicValue;
			}
//			System.out.println(value);
			if(value>0.0){
				numP++;
			}
		}
		return 1.0*numP/numAll;
		
	}
	
	
	/* (non-Javadoc)
	 * @see SGD.CostFunction#evaluateCost(SGD.DoubleVector)
	 */
	@Override
	public CostGradientTuple evaluateCost(DoubleVector initial,int iteration) {
		// TODO Auto-generated method stub
//		if(iteration==0){
//			System.out.println("last none");
//		}else{
//			double value1=calculatePrecisionAfterUpdate(iteration-1, initial.vector);
//			System.out.println((iteration-1)+"after \tprecison:"+value1+"\n-------------");
//			
//		}
//		double value2=calculatePrecisionAfterUpdate(iteration, initial.vector);
//		System.out.println((iteration)+"before \tprecison:"+value2);
		
//		if(iteration%miniBatchNum_==0){
//			Collections.shuffle(this.arrayAllData_);
//		}
		ArrayList<TriplePairIntIntInt> arraySample=getBatchData(iteration%miniBatchNum_);
		int sampleSize=arraySample.size();
//		System.out.println("sampleSize:"+sampleSize);
		double[] valueM=new double[sampleSize];
		double[] valueLast=new double[sampleSize];
		int sampleOrder=0;
		double[] last=initial.vector;
		int baseStartIndex=this.playerNum_*this.topicNum_;
		int weightStartIndex=this.playerNum_*(this.topicNum_+1);
		HashSet<Integer> setPlayer=new HashSet<>();
		//		System.out.println("initial");
		for(TriplePairIntIntInt pair:arraySample){
			
			int win=pair.a_;
			int lose=pair.b_;
			setPlayer.add(win);
			setPlayer.add(lose);
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
			valueM[sampleOrder]=value;
//			System.out.println(value);
//			System.out.println(Math.exp(-value));
//			System.out.println(1.0/1+Math.exp(-value));
//			System.out.println(1-(1.0/(1+Math.exp(-value))));
			valueLast[sampleOrder]=1-(1.0/(1+Math.exp(-value)));
			sampleOrder++;
		}
//		
		double[] gradient=new double[this.parameterNum_];//add bias term
		sampleOrder=0;
		for(TriplePairIntIntInt pair:arraySample){
			int win=pair.a_;
			int lose=pair.b_;
			int context=pair.c_;
			//win base
			gradient[baseStartIndex+win]+=(valueLast[sampleOrder]*alpha_);
			//lose base
			gradient[baseStartIndex+lose]-=(valueLast[sampleOrder]*alpha_);
		
		
			for(int t=0;t<topicNum_;t++){
				
				//win topic
				gradient[win*topicNum_+t]+=(valueLast[sampleOrder]
						*last[weightStartIndex+t]
						*questionTopicDistribution_[context][t]);
				//lose topic
				gradient[lose*topicNum_+t]-=(valueLast[sampleOrder]
						*last[weightStartIndex+t]
						*questionTopicDistribution_[context][t]);
			}
			sampleOrder++;
		}
		int num=0;
		for(int p=0;p<this.playerNum_;p++){
			for(int t=0;t<topicNum_;t++){
				if(Math.abs(gradient[p*topicNum_+t])>0.0){
					num++;
					gradient[p*topicNum_+t]=-(gradient[p*topicNum_+t]-beta1_*(last[p*topicNum_+t]-priorPlayerMean_[p])
							/priorPlayerSigma_[p]);
				}
			}
			if(Math.abs(gradient[baseStartIndex+p])>0.0){
				gradient[baseStartIndex+p]=-(gradient[baseStartIndex+p]-
						beta2_*(last[baseStartIndex+p]-priorPlayerMean_[p])
						/(priorPlayerSigma_[p]));
			}
		}
//		System.out.println(iteration+":"+num);
		
		//w_z
//		for(int t=0;t<topicNum_;t++){
//			double value=0.0;
//			sampleOrder=0;
//			for(TriplePairIntIntInt pair:arraySample){
//				int win=pair.a_;
//				int lose=pair.b_;
//				int context=pair.c_;
//				value+=(valueLast[sampleOrder]
//						*(last[win*topicNum_+t]-last[lose*topicNum_+t])
//						*questionTopicDistribution_[context][t]);
//				
//				sampleOrder++;
//			}
//			gradient[weightStartIndex+t]=-(
//					value-last[weightStartIndex+t]/lamda_);
//		}
		double cost=iteration*0.1;
//		for(double v:valueM){
//			cost+=(Math.log(1+Math.exp(-v)));
//		}
//		cost*=(-1);
//		for(int p=0;p<playerNum_;p++){
//			for(int t=0;t<topicNum_;t++){
//				cost-=(beta1_/2*(last[p*topicNum_+t]-priorPlayerMean_[p])*
//						(last[p*topicNum_+t]-priorPlayerMean_[p])
//						/(priorPlayerSigma_[p]));
//			}
//			cost-=(beta2_/2*(last[baseStartIndex+p]-priorPlayerMean_[p])*
//					(last[baseStartIndex+p]-priorPlayerMean_[p])
//					/(priorPlayerSigma_[p]));
//		}
//		for(int t=0;t<topicNum_;t++){
//			cost-=(1.0/(2*lamda_)*last[weightStartIndex+t]
//					*last[weightStartIndex+t]);
//		}
		CostGradientTuple cgt=new 
				CostGradientTuple(-cost, new DoubleVector(gradient));
		
		return cgt;
	}

	/* (non-Javadoc)
	 * @see SGD.CostFunction#evaluateCost(SGD.DoubleVector)
	 */
	@Override
	public CostGradientTuple evaluateCost(DoubleVector input) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
