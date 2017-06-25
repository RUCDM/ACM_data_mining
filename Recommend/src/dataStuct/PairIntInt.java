package dataStuct;

public class PairIntInt implements Comparable<PairIntInt>{
	public int key;
	public int value;
	public PairIntInt(int key,int value) {
		this.key=key;
		this.value=value;
	}
	@Override
	public int compareTo(PairIntInt o) {
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
