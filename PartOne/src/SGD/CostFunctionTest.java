/**
 * 
 */
package SGD;

/**
 * @author wenhui zhang
 *
 */
public class CostFunctionTest implements CostFunction{
	private int size;
	private int dataFeatureNum;
	private int parameterNum;
	private double[][] data;
	
	public CostFunctionTest(int size,int dataFeatureNum,
			int parameterNum,double[][] dataInput) {
		// TODO Auto-generated constructor stub
		this.size=size;
		this.parameterNum=parameterNum;
		this.dataFeatureNum=dataFeatureNum;
		this.data=dataInput;
	}
	
	/* (non-Javadoc)
	 * @see SGD.CostFunction#evaluateCost(SGD.DoubleVector)
	 */
	@Override
	public CostGradientTuple evaluateCost(DoubleVector input) {
		// TODO Auto-generated method stub
		if(input.length!=this.parameterNum){
			System.err.println("error para num");
			System.exit(0);
		}
		double[] dv=input.vector;
		double[] gradient=new double[this.parameterNum];;
		double allSum=0.0;
		for(int p=0;p<this.parameterNum;p++){
			double sum=0.0;
			for(int n=0;n<this.size;n++){
				sum+=2*dv[p]*data[n][p];
				allSum+=(dv[p]*dv[p]*data[n][p]);
			}
			gradient[p]=sum;
		}
		CostGradientTuple cgt=new 
				CostGradientTuple(allSum, new DoubleVector(gradient));
		return cgt;
	}
	
}
