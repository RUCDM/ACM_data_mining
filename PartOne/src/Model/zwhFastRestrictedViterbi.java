/**
 * 
 */
package Model;

/**
 * @author wenhui zhang
 *
 */
public class zwhFastRestrictedViterbi {
	public zwhFastRestrictedViterbi(){
		
	}
	public void viterbi(
			double[] modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[][] localTwo,
			int[] bestPath){
		this.length_=localTwo.length;
		this.topicS_=thetaS.length;
		this.topicI_=thetaI.length;
		states_=topicI_*3+topicS_*3;
		double[][] delta=new double[this.length_][this.states_];
		int[][] best=new int[this.length_][this.states_];
		ComputeAllDeltas(modeProb, rhoI,rhoS, thetaI, thetaS, localTwo, delta, best);
		BackTrackBestPath(delta[this.length_-1], best, bestPath);
		
	}
	private void BackTrackBestPath(
			double[] delta_T_1,
			int[][] best,
			int[] bestPath){
		bestPath[this.length_-1]=FindBestInLevel(delta_T_1,0,this.states_);
		for(int i=this.length_-2;i>=0;i--){
			bestPath[i]=best[i+1][bestPath[i+1]];
		}
		
	}
	private void ComputeAllDeltas(
			double[] modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[][] localTwo,
			double[][] delta,
			int[][] best){
		initDelta(modeProb[0], thetaI, localTwo[0], delta[0], thetaS);
		for(int n=1;n<this.length_;n++){
//			System.out.println(i);
			computeSingleDelta(localTwo[n], modeProb[n], thetaI, thetaS, rhoI,rhoS, delta[n-1], delta[n], best[n]);
		}
		
	}
	private void computeSingleDelta(
			double[] localTwo_t,
			double modeProb,
			double[] thetaI,
			double[] thetaS,
			double rhoI,
			double rhoS,
			double[] delta_t_1,
			double[] delta_t,
			int[] best){
		
		
		int prev_bestI=-1;
		prev_bestI=FindBestInLevel(delta_t_1,0,this.topicI_);
		int prev_bestS=-1;
		prev_bestS=FindBestInLevel(delta_t_1,this.topicI_,this.states_);
		if(prev_bestI==-1||prev_bestS==-1){
			System.out.println("error");
		}
		int prev_bestAll=-1;
		if(delta_t_1[prev_bestI]>delta_t_1[prev_bestS]){
			prev_bestAll=prev_bestI;
		}else{
			prev_bestAll=prev_bestS;
		}
//		if(prev_best==-1){
//			System.out.println("no");
//			for(int d=0;d<delta_t_1.length;d++){
//				System.out.println(d+":"+delta_t_1[d]);
//			}
////			System.exit(0);
//		}
		double norm=0.0;
		for(int v=0;v<this.topicI_;v++){
			delta_t[v]=delta_t_1[prev_bestS]*modeProb
					*thetaI[v]*localTwo_t[v];
			best[v]=prev_bestAll;
			//
			delta_t[v+topicI_]=delta_t_1[prev_bestI]*modeProb
					*rhoI*thetaI[v]*localTwo_t[v];
			best[v+topicI_]=prev_bestI;
			//
			int index=FindBestInThree(delta_t_1,
					v, v+topicI_,v+topicI_*2);
			delta_t[v+topicI_*2]=modeProb*(1-rhoI)*delta_t_1[index]*localTwo_t[v];
			best[v+topicI_*2]=index;
			
		}
		for(int z=0;z<this.topicS_;z++){
			//impression mode
		
			delta_t[z+topicI_*3]=delta_t_1[prev_bestI]*(1-modeProb)
					*thetaS[z]*localTwo_t[z+topicI_];
			best[z+topicI_*3]=prev_bestI;
			delta_t[z+topicI_*3+topicS_]=delta_t_1[prev_bestS]*(1-modeProb)
					*rhoS*thetaS[z]*localTwo_t[z+topicI_];
			best[z+topicI_*3+topicS_]=prev_bestS;
			
			int index=FindBestInThree(delta_t_1,
					z+topicI_*3, z+topicI_*3+topicS_,z+topicI_*3+2*topicS_);
			delta_t[z+topicI_*3+2*topicS_]=(1-modeProb)*(1-rhoS)*delta_t_1[index]*localTwo_t[z+topicI_];
			best[z+topicI_*3+2*topicS_]=index;
			
			
		}
		for(int s=0;s<this.states_;s++){
			norm+=(delta_t[s]);
		}
//		System.out.println(norm);
		normalize(delta_t, norm);
//		System.out.println(norm);
		
		
	}
	private int FindBestInLevel(double[] delta,int start,int end){
		int best=-1;
		double score=-1.0;
		for(int i=start;i<end;i++){
			if(delta[i]>=score){
				best=i;
				score=delta[i];
			}
		}
		return best;
	}
	private int FindBestInThree(double[] delta,int first,int second,int third){
		double a=delta[first];
		double b=delta[second];
		double c=delta[third];
		if(a>=b&&a>=c){
			return first;
		}
		if(b>=a&&b>=c){
			return second;
		}
		if(c>=b&&c>=a){
			return third;
		}
		System.out.println("three error");
		System.exit(0);
		return -1;
	}
	private void initDelta(
			double modeProb0,
			double[] thetaI,
			double[] local0,
			double[] delta0,
			double[] thetaS){
		double norm=0.0;
		for(int v=0;v<this.topicI_;v++){
			delta0[v]=modeProb0*thetaI[v]*local0[v];
			delta0[v+this.topicI_]=0.0;
			delta0[v+this.topicI_*2]=0.0;
		}
		for(int z=0;z<this.topicS_;z++){
			delta0[z+this.topicI_*3]=(1-modeProb0)*thetaS[z]*local0[z+topicI_];
			delta0[z+this.topicI_*3+this.topicS_]=0.0;
			delta0[z+this.topicI_*3+2*this.topicS_]=0.0;
		}
		for(int s=0;s<this.states_;s++){
			norm+=(delta0[s]);
		}
		normalize(delta0, norm);
		
	}
	private void normalize(double[] vector,double norm){
		for(int i=0;i<vector.length;i++){
			vector[i]=(vector[i]/norm);
		}
	}
	private int length_;
	private int topicI_;
	private int topicS_;
	private int states_;
}
