package dataStuct;

public class SortPairDouble implements Comparable<SortPairDouble>{
	public int key;
	public double value;
	public SortPairDouble(int key,double value) {
		// TODO Auto-generated constructor stub
		this.key=key;
		this.value=value;
	}
	@Override
	public int compareTo(SortPairDouble o) {
		// TODO Auto-generated method stub
		if(o.value-this.value>0){
			return 1;
		}
		if(o.value-this.value<0){
			return -1;
		}
		
		return 0;
	}
}	
