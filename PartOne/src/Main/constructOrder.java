/**
 * 
 */
package Main;

/**
 * @author wenhui zhang
 *
 */
public class constructOrder {
	public static void constructEclipseOrder(){
		String version="test";
		String input="data/lda/"+version+".dat";
		int mode=2;
		int K1=31;
		int K2=40;
		double alpha=2.0;
		double betaI=0.1;
		double betaS=0.1;
		double gamma=10.0;
		int it=1;
		int begin=0;
		int saveStep=1;
		String resultPath="data/lda/";
		String order=input+" "+
					 K1+" "+
					 K2+" "+
					 alpha+" "+
					 betaI+" "+
					 betaS+" "+
					 gamma+" "+
					 it+" "+
					 saveStep+" "+
					 begin+" "+
					 resultPath+" "+
					 mode;
		System.out.println(order);		 
	}
	public static void constructLinuxOrder(){
		String version="test";
		String input="data/lda/"+version+".dat";
		int mode=2;
		int K1=31;
		int K2=40;
		double alpha=2.0;
		double betaI=0.1;
		double betaS=0.1;
		double gamma=10.0;
		int it=500;
		int begin=0;
		int saveStep=10;
		String resultPath="data/lda/";
		String filename="mode_"+mode+
				"_K1_"+K1+
				"_K2_"+K2+
				"_a_"+alpha+
				"_bI_"+betaI+
				"_bS_"+betaS+
				"_g_"+gamma+
				"_it_"+it;
		String order="java -cp bin Main.TldaTrain "+
					 input+" "+
					 K1+" "+
					 K2+" "+
					 alpha+" "+
					 betaI+" "+
					 betaS+" "+
					 gamma+" "+
					 it+" "+
					 saveStep+" "+
					 begin+" "+
					 resultPath+" "+
					 mode+" >"+resultPath+filename+"/myout.txt "
					 +"2>&1 &";
		System.out.println(order);		 
	}
	
	public static void main(String[] args){
//		constructEclipseOrder();
		constructLinuxOrder();
	}
}
