/**
 * 
 */
package SGD;

/**
 * @author wenhui zhang
 *
 */
public class test {
	public static void main(String[] args){
		double[] x=new double[2];
		x[0]=2.0;
		x[1]=1.0;
		DoubleVector dv=new DoubleVector(x);
		dv.add(dv);
		dv.multiply(2.0);
		dv.subtract(dv);
		dv.print();
	}
}
