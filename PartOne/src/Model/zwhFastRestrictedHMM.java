/**
 * 
 */
package Model;

/**
 * @author wenhui zhang
 *
 */
public class zwhFastRestrictedHMM {
	private int topicI_;
	private int topicS_;
	private int states_;
	private double[][] thetaI_;//estimated impression mode user-volumn distribution
	private double[][] thetaS_;//estimated semantic mode user-topic distribution 
	private double[][] phiI_;//estimated impression topic-word distribution phiI[v][w]
	private double[][] phiS_;//estimated semantic topic-word distribution phiS[z][w]
	//	private double[] omega;//estimated  impression word distribution omega[w]
	private double[] epsilon_;//estimated the feature weight control mode
	private double[] rhoI_;//estimated the value control semantic topic consecutive
	private double[] rhoS_;//estimated the value control semantic topic consecutive
	
	public zwhFastRestrictedHMM(){
		
	}
	
	public double forwardBackWard(
			double[] modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[][] localTwo,
			double[] pi,
			double[][] targetProb,
			double[] hiddenData){//
		
		topicI_=thetaI.length;//kI
		topicS_=thetaS.length;//kS
		states_=topicI_*3+topicS_*3;
		double[] normFactor=new double[localTwo.length];
		double[][] alpha=new double[localTwo.length][states_];
		double[][] beta=new double[localTwo.length][states_];
		normFactor[0]=initAlpha(pi, localTwo[0], alpha[0]);
		computeAllAlphas(localTwo, modeProb, rhoI,rhoS, thetaI, thetaS, normFactor, alpha);
		initBeta(beta[localTwo.length-1]);
		computeAllBeta(localTwo, modeProb, rhoI,rhoS, thetaI, thetaS, beta);
		computeAllProbs(alpha, beta, targetProb,hiddenData);
		double loglike=computeLoglike(normFactor);
		return loglike;
	}
	public double computeLoglike(double[] normFactor){
		double loglike=0.0;
		for(int t=0;t<normFactor.length;t++){
			loglike+=(Math.log(normFactor[t]));
		}
		return loglike;
	}
	public void computeAllProbs(double[][] alpha,
			double[][] beta,
			double[][] probs,
			double[] hiddenData){
		for(int i=0;i<alpha.length;i++){
			combineSingleProb(alpha[i], beta[i], probs[i]);
			hiddenData[i]=0.0;
			for(int k=0;k<this.topicI_*3;k++){
				hiddenData[i]+=(probs[i][k]);
			}
		}
		
	}
	public void computeAllAlphas(
			double[][] localTwo,
			double[] modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[] normFactor,
			double[][] alpha
			){
		for(int n=1;n<localTwo.length;n++){
			normFactor[n]=computeSingleAlpha
					(localTwo[n], modeProb[n], rhoI,rhoS,
					thetaI, thetaS, 
					alpha[n-1], alpha[n]);
		}
		
	}
	public double computeSingleAlpha(
			double[] localTwo,
			double modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[] alpha_t_1,
			double[] alpha_t){
		double norm=0.0;
		for(int v=0;v<this.topicI_;v++){
			//0...v from k-1=z
			double sum1=0.0;
			for(int z_1=this.topicI_*3;z_1<this.states_;z_1++){
				sum1+=alpha_t_1[z_1]*modeProb*thetaI[v]*localTwo[v];
			}
			alpha_t[v]=sum1;
			//v...2v 
			double sum2=0.0;
			for(int v_1=0;v_1<this.topicI_*3;v_1++){
				sum2+=alpha_t_1[v_1]*modeProb*rhoI*thetaI[v]*localTwo[v];		
			}
			alpha_t[topicI_+v]=sum1;
			//2v... 3v k-1=k=v
			alpha_t[topicI_*2+v]=modeProb*(1-rhoI)
					*(alpha_t_1[v]+alpha_t_1[topicI_+v]+alpha_t_1[topicI_*2+v])
					*localTwo[v];
		}
		for(int z=0;z<topicS_;z++){
			//3v...3v+Z
			double sum1=0.0;
			for(int v_1=0;v_1<this.topicI_*3;v_1++){
				sum1+=alpha_t_1[v_1]*(1-modeProb)*thetaS[z]*localTwo[this.topicI_+z];
			}
			alpha_t[z+topicI_*3]=sum1;
			//3V+Z...3V+2Z
			double sum2=0.0;
			for(int z_1=this.topicI_*3;z_1<this.states_;z_1++){
				sum2+=alpha_t_1[z_1]*(1-modeProb)*rhoS*thetaS[z]*localTwo[this.topicI_+z];		
			}
			alpha_t[topicI_*3+z+topicS_]=sum2;
			//3V+2Z...3V+3Z
			alpha_t[topicI_*3+z+2*topicS_]=(1-modeProb)*(1-rhoS)
					*(alpha_t_1[topicI_*3+z]+alpha_t_1[topicI_*3+z+topicS_]+alpha_t_1[topicI_*3+z+2*topicS_])
					*localTwo[z+topicI_];
		}
		for(int s=0;s<this.states_;s++){
			norm+=(alpha_t[s]);
		}
		normalize(alpha_t, norm);
		return norm;
	}
		
public void combineSingleProb(double[] alpha,
			double[] beta,
			double[] probs){
		double norm=0.0;
		for(int s=0;s<states_;s++){
			probs[s]=alpha[s]*beta[s];
			norm+=probs[s];
		}
		normalize(probs, norm);
	}
	public void computeAllBeta(double[][] localTwo,
			double[] modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[][] beta){
		for(int n=localTwo.length-2;n>=0;n--){
			computeSingleBeta(localTwo[n+1], 
					modeProb[n+1], rhoI,rhoS, thetaI,
					thetaS, beta[n], beta[n+1]);
		}
		
	}
	public void computeSingleBeta(
			double[] localTwo_t1,
			double modeProb,
			double rhoI,
			double rhoS,
			double[] thetaI,
			double[] thetaS,
			double[] beta_t,
			double[] beta_t1){
		double trans_sumII=0.0;
		double trans_sumSI=0.0;
		double trans_sumIS=0.0;
		double trans_sumSS=0.0;
		double norm=0.0;
		for(int v=0;v<topicI_;v++){
			trans_sumSI+=(modeProb*thetaI[v]*localTwo_t1[v]*beta_t1[v]);
			trans_sumII+=(modeProb*rhoI*
					thetaI[v]*localTwo_t1[v]*beta_t1[v+topicI_]);
			
		}
		for(int z=0;z<topicS_;z++){
			trans_sumSS+=((1-modeProb)*rhoS*
					thetaS[z]*localTwo_t1[topicI_+z]*beta_t1[z+topicI_*3+topicS_]);
			trans_sumIS+=((1-modeProb)*
					thetaS[z]*localTwo_t1[topicI_+z]*beta_t1[z+topicI_*3]);
		}
		for(int v=0;v<topicI_;v++){
			beta_t[v]=trans_sumII+trans_sumIS
					+modeProb*(1-rhoI)*localTwo_t1[v]*beta_t1[v+topicI_*2];;
			beta_t[v+topicI_]=beta_t[v];
			beta_t[v+topicI_*2]=beta_t[v];
			
			
		}
		for(int z=0;z<topicS_;z++){
			// look this Recall that beta_t1[k+topics] == beta_t1[k+2*topics_]
			
			beta_t[z+topicI_*3]=trans_sumSI+trans_sumSS+
					(1-modeProb)*(1-rhoS)*localTwo_t1[topicI_+z]*beta_t1[z+topicI_*3+2*topicS_];
			beta_t[z+topicI_*3+topicS_]=beta_t[z+topicI_*3];
			beta_t[z+topicI_*3+2*topicS_]=beta_t[z+topicI_*3];
		}
		for(int s=0;s<this.states_;s++){
			norm+=(beta_t[s]);
		}
		normalize(beta_t, norm);
		
	}
	public void initBeta(double[] betaL_1){
		double norm=0.0;
		for(int i=0;i<states_;i++){
			betaL_1[i]=1.0;
			norm+=betaL_1[i];
		}
		normalize(betaL_1, norm);
	}
	public double initAlpha(double[] pi,double[] localTwo,double[] alpha){
		
		double norm=0.0;
		for(int v=0;v<this.topicI_;v++){
			alpha[v]=localTwo[v]*pi[v];
			alpha[v+this.topicI_]=0.0;
			alpha[v+this.topicI_*2]=0.0;
		}
		for(int z=0;z<this.topicS_;z++){
			alpha[this.topicI_*3+z]=localTwo[z+topicI_]*pi[this.topicI_*3+z];
			alpha[this.topicI_*3+z+topicS_]=0.0;
			alpha[this.topicI_*3+z+topicS_*2]=0.0;
		}
		for(int s=0;s<states_;s++){
			
			norm+=(alpha[s]);
		}
		normalize(alpha, norm);
		return norm;
	}
	private void normalize(double[] vector,double norm){
		for(int i=0;i<vector.length;i++){
			vector[i]=(vector[i]/norm);
		}
	}
}
