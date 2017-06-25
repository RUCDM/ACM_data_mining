/**
 * 
 */
package SGD;

/**
 * @author wenhui zhang
 *
 */
public class DoubleVector {
	public double[] vector;
	public int length;
	public DoubleVector(double[] array){
		this.length=array.length;
		vector=new double[length];
		for(int i=0;i<length;i++){
			vector[i]=array[i];
		}
	}
	public double get(int i){
		return this.vector[i];
	}
	public DoubleVector multiply(double alpha){
		for(int i=0;i<this.length;i++){
			vector[i]*=(alpha);
		}
		return this;
	}
	public DoubleVector subtract(DoubleVector dv){
		if(this.length!=dv.length){
			System.err.println("doublevector length error");
			System.exit(0);
		}
		for(int i=0;i<this.length;i++){
			vector[i]-=dv.get(i);
		}
		return this;
	}
	public DoubleVector add(DoubleVector dv){
		if(this.length!=dv.length){
			System.err.println("doublevector length error");
			System.exit(0);
		}
		for(int i=0;i<this.length;i++){
			vector[i]+=dv.get(i);
		}
		return this;
	}
	public void print(){
		for(int i=0;i<this.length;i++){
			System.out.println(i+":"+vector[i]);
		}
	}
}
