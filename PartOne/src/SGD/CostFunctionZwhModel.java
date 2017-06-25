/**
 * 
 */
package SGD;

import java.util.ArrayList;
import java.util.Random;

import DataStruct.TwoInt;

/**
 * @author wenhui zhang
 *
 */
public class CostFunctionZwhModel implements CostFunction{
	private int size_;
	private int sampleSize_;
	private int parameterNum_;
	private double[][][] dataFeature_;
	private double[][] hiddenData_;
	private double lamda_;
	private ArrayList<TwoInt> lengthToDoc_;
	public CostFunctionZwhModel(int size,
			int parameterNum,double[][] hiddenData,
			double[][][] dataFeature,double lamda,
			ArrayList<TwoInt> lengthToDoc,int sampleSize) {
		// TODO Auto-generated constructor stub
		this.size_=size;
		this.sampleSize_=sampleSize;
		this.parameterNum_=parameterNum;
		this.dataFeature_=dataFeature;
		this.hiddenData_=hiddenData;
		this.lamda_=lamda;
		this.lengthToDoc_=lengthToDoc;
	}
	
	/* (non-Javadoc)
	 * @see SGD.CostFunction#evaluateCost(SGD.DoubleVector)
	 */
	@Override
	public CostGradientTuple evaluateCost(DoubleVector initial) {
		// TODO Auto-generated method stub
		if(initial.length!=this.parameterNum_+1){
			System.err.println("error para num");
			System.exit(0);
		}
		int[] sampleData=new int[sampleSize_];
		Random rand=new Random();
		for(int i=0;i<this.sampleSize_;i++){
			sampleData[i]=rand.nextInt(size_);
		}
		double[] last=initial.vector;
		double[] gradient=new double[this.parameterNum_+1];//add bias term
		//common logistic value
		double[] logisticValue=new double[this.sampleSize_];
		int loc=0;
		for(int sd=0;sd<this.sampleSize_;sd++){
			TwoInt ti=this.lengthToDoc_.get(sampleData[sd]);
			int d=ti.d_;
			int n=ti.n_;
			double sum=0.0;
			for(int f=0;f<this.parameterNum_;f++){
				sum+=(this.dataFeature_[d][n][f]*last[f]);
			}
			sum+=last[this.parameterNum_];//bias term
			logisticValue[loc++]=1.0/(1.0+Math.exp(-1.0*sum));
		}
//		for(int d=0;d<this.dataFeature_.length;d++){
//			for(int n=0;n<this.dataFeature_[d].length;n++){
//				double sum=0.0;
//				for(int f=0;f<this.parameterNum_;f++){
//					sum+=(this.dataFeature_[d][n][f]*last[f]);
//				}
//				sum+=last[this.parameterNum_];//bias term
//				logisticValue[loc++]=1.0/(1.0+Math.exp(-1.0*sum));
//			}
//		}
		if(loc!=this.sampleSize_){
			System.err.println("error data length num");
			System.exit(0);
		}
		
		for(int p=0;p<this.parameterNum_;p++){//
			double sum=0.0;
			for(int si=0;si<this.sampleSize_;si++){
				TwoInt ti=this.lengthToDoc_.get(sampleData[si]);
				int d=ti.d_;
				int n=ti.n_;
				double a=this.hiddenData_[d][n];
				double b=this.dataFeature_[d][n][p];
				double c=(1.0-logisticValue[si]);
				double sumI=this.hiddenData_[d][n]*this.dataFeature_[d][n][p]*(1.0-logisticValue[si]);
				double sumS=(1.0-this.hiddenData_[d][n])*this.dataFeature_[d][n][p]*logisticValue[si];
				sum+=(sumS-sumI);
				
			}
			double a=sum/this.sampleSize_;
			double b=this.lamda_*last[p]/this.sampleSize_;
			gradient[p]=sum/this.sampleSize_+this.lamda_*last[p]/this.sampleSize_;//regularize
		}
		//bias term
		double sum=0.0;
		loc=0;
		for(int si=0;si<this.sampleSize_;si++){
			TwoInt ti=this.lengthToDoc_.get(sampleData[si]);
			int d=ti.d_;
			int n=ti.n_;
			double sumI=this.hiddenData_[d][n]*(1.0-logisticValue[si]);
			double sumS=(1.0-this.hiddenData_[d][n])*logisticValue[si];
			sum+=(sumS-sumI);
		}
		
		gradient[this.parameterNum_]=sum/this.sampleSize_+this.lamda_*last[parameterNum_]/this.sampleSize_;;//regularize
		double allSum=0.0;
		
		for(int si=0;si<this.sampleSize_;si++){
			TwoInt ti=this.lengthToDoc_.get(sampleData[si]);
			int d=ti.d_;
			int n=ti.n_;
			allSum+=(this.hiddenData_[d][n]*Math.log(logisticValue[si])
					+(1.0-this.hiddenData_[d][n])*Math.log(1.0-logisticValue[si]));
			
		}
		
		allSum=(-1.0)*allSum;
//		System.out.println(allSum);
		for(int w=0;w<this.parameterNum_+1;w++){
			allSum+=(0.5*lamda_*last[w]*last[w]/this.sampleSize_);
		}
		
		CostGradientTuple cgt=new 
				CostGradientTuple(allSum, new DoubleVector(gradient));
		
		return cgt;
	}
	
}
