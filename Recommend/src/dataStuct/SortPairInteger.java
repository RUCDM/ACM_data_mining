package dataStuct;

public class SortPairInteger implements Comparable<SortPairInteger>{
	public int key;
	public int value;
	public SortPairInteger(int key,int value) {
		this.key=key;
		this.value=value;
	}
	@Override
	public int compareTo(SortPairInteger o) {
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
