/**
 * 
 */
package SGD;

import java.util.Arrays;

/**
 * @author wenhui zhang
 *
 */
public class GDTest {
	public static void main(String[] args){
		GradientDescent gd=new 
				GradientDescent(0.1,0.0001);
		double[][] dataInput=new double[1][1];
		dataInput[0][0]=1.0;
		double[] pinput=new double[1];
		Arrays.fill(pinput,10.0);
		DoubleVector pInput=new DoubleVector(pinput);
		pInput.print();
		CostFunctionTest cft=
				new CostFunctionTest(1,1,1,dataInput);
//		gd.minimize(cft, pInput, 
//				50, true);
		pInput.print();
	}
}
