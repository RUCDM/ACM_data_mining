package SGD;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.google.common.base.Preconditions;

import DataStruct.SortPairDouble;
import DataStruct.doubleDecimal;



/**
 * Gradient descent implementation with some neat features like momentum,
 * divergence detection, delta breaks and bold driver and scheduled annealing
 * adaptive learning rates. For more sophisticated configuration use the
 * {@link GradientDescentBuilder}.
 * 
 * @author thomas.jungblut
 * 
 */
public final class GradientDescent extends AbstractMinimizer {

//  private static final Logger LOG = LogManager.getLogger(GradientDescent.class);

  private static final int COST_HISTORY = 3;

  public static class GradientDescentBuilder {

    private final double alpha;
    private double breakDifference;
    private double momentum;
    private boolean breakOnDivergence;
    private boolean boldDriver;
    private double boldIncreasePercentage;
    private double boldDecreasePercentage;
    private int annealingIteration = -1;

    private GradientDescentBuilder(double alpha) {
      this.alpha = alpha;
    }

    public GradientDescent build() {
      return new GradientDescent(this);
    }

    /**
     * Add momentum to this gradient descent minimizer.
     * 
     * @param momentum the momentum to use. Between 0 and 1.
     * @return the builder again.
     */
    public GradientDescentBuilder momentum(double momentum) {
      Preconditions.checkArgument(momentum >= 0d && momentum <= 1d,
          "Momentum must be between 0 and 1.");
      this.momentum = momentum;
      return this;
    }

    /**
     * BoldDriver will change the learning rate over time by observing the cost
     * of the costfunction. If the cost decreases, it will increase the learning
     * rate by 5%. If the cost increases it will cut the learning rate in half.
     * 
     * @return the builder again.
     */
    public GradientDescentBuilder boldDriver() {
      return boldDriver(0.5, 0.05);
    }
    public GradientDescentBuilder boldDriverAverage() {
        return boldDriver(0.5, 0.5);
      }

    /**
     * BoldDriver will change the learning rate over time by observing the cost
     * of the costfunction. If the cost decreases, it will increase the learning
     * rate (typically by 5%). If the cost increases it will (typically) cut the
     * learning rate in half.
     * 
     * @param increasedCostPercentage the percentage of the learning rate that
     *          will be used when cost increases.
     * @param decreasedCostPercentage the percentage of the learning rate that
     *          will be used when cost decreases.
     * @return the builder again.
     */
    public GradientDescentBuilder boldDriver(double increasedCostPercentage,
        double decreasedCostPercentage) {
      Preconditions.checkArgument(increasedCostPercentage >= 0d
          && increasedCostPercentage <= 1d,
          "increasedCostPercentage must be between 0 and 1.");
      Preconditions.checkArgument(decreasedCostPercentage >= 0d
          && decreasedCostPercentage <= 1d,
          "decreasedCostPercentage must be between 0 and 1.");
      this.boldDriver = true;
      this.boldIncreasePercentage = increasedCostPercentage;
      this.boldDecreasePercentage = decreasedCostPercentage;
      return this;
    }

    /**
     * If called, this breaks when the gradient descent minimizer starts to
     * diverge (costs are growing).
     * 
     * @return the builder again.
     */
    public GradientDescentBuilder breakOnDivergence() {
      this.breakOnDivergence = true;
      return this;
    }

    /**
     * Breaks minimization process when the given delta in costs have been
     * archieved. Usually a quite low value of 1e-4 to 1e-8.
     * 
     * @param delta the delta to break in difference between two costs.
     * @return the builder again.
     */
    public GradientDescentBuilder breakOnDifference(double delta) {
      this.breakDifference = delta;
      return this;
    }

    /**
     * Sets a simple annealing (alpha / (1+current_iteration / phi)) where phi
     * is the given parameter here. This will gradually lower the global
     * learning rate after the given amount of iterations.
     * 
     * @param iteration the iteration to start annealing.
     * @return the builder again.
     */
    public GradientDescentBuilder annealingAfter(int iteration) {
      Preconditions.checkArgument(iteration > 0,
          "Annealing can only kick in after the first iteration! Given: "
              + iteration);
      this.annealingIteration = iteration;
      return this;
    }

    /**
     * Creates a new builder.
     * 
     * @param alpha the learning rate to set.
     * @return a new builder.
     */
    public static GradientDescentBuilder create(double alpha) {
      return new GradientDescentBuilder(alpha);
    }

  }
  public BufferedWriter bw;
  private double[] alphaArray_;
  private final boolean breakOnDivergence;
  private final double breakDifference;
  private final double momentum;
  private final double alpha;
  
  private final boolean boldDriver;
  private final double boldIncreasePercentage;
  private final double boldDecreasePercentage;
  private final int annealingIteration;
  public GradientDescent(
		  GradientDescentBuilder builder,BufferedWriter outFile) {
    this.alpha = builder.alpha;
    this.breakDifference = builder.breakDifference;
    this.momentum = builder.momentum;
    this.breakOnDivergence = builder.breakOnDivergence;
    this.boldDriver = builder.boldDriver;
    this.boldIncreasePercentage = builder.boldIncreasePercentage;
    this.boldDecreasePercentage = builder.boldDecreasePercentage;
    this.annealingIteration = builder.annealingIteration;
    this.bw=outFile;
  }
 
  public GradientDescent(
		  GradientDescentBuilder builder,
		  double[] alphaArray,BufferedWriter outFile) {
    this.alphaArray_=alphaArray;
	 this.alpha = builder.alpha;
    this.breakDifference = builder.breakDifference;
    this.momentum = builder.momentum;
    this.breakOnDivergence = builder.breakOnDivergence;
    this.boldDriver = builder.boldDriver;
    this.boldIncreasePercentage = builder.boldIncreasePercentage;
    this.boldDecreasePercentage = builder.boldDecreasePercentage;
    this.annealingIteration = builder.annealingIteration;
    this.bw=outFile;
  }
  public GradientDescent(
		  GradientDescentBuilder builder) {
    this.alpha = builder.alpha;
    this.breakDifference = builder.breakDifference;
    this.momentum = builder.momentum;
    this.breakOnDivergence = builder.breakOnDivergence;
    this.boldDriver = builder.boldDriver;
    this.boldIncreasePercentage = builder.boldIncreasePercentage;
    this.boldDecreasePercentage = builder.boldDecreasePercentage;
    this.annealingIteration = builder.annealingIteration;
    
  }

  /**
   * @param alpha the learning rate.
   * @param limit the delta in cost to archieve to break the iterations.
   */
  public GradientDescent(double alpha, double limit) {
    this(GradientDescentBuilder.create(alpha).breakOnDifference(limit));
    
  }

  public DoubleVector minimizePartTwo(CostFunction f, DoubleVector pInput,
	       int startIteration,
		  final int Iterations, boolean verbose) throws IOException {

    double[] lastCosts = new double[COST_HISTORY];
    Arrays.fill(lastCosts, Double.MAX_VALUE);
    final int lastIndex = lastCosts.length - 1;
    DoubleVector lastTheta = null;
    DoubleVector lastGradient = null;
    DoubleVector theta = pInput;
    double alpha = this.alpha;
    int topicNum=10;
    for (int iteration = startIteration; iteration < startIteration+Iterations; iteration++) {
//    	System.out.println("it:"+iteration+" "+alpha+"\n");
    	CostGradientTuple evaluateCost = f.evaluateCost(theta,iteration);
//      if(iteration>10){
//    	  break;
//      }
    	
      DoubleVector dv=evaluateCost.getGradient();
      if(iteration==startIteration){
//          bw.write("it:"+iteration+" "+alpha+"\n");
//
//	       ArrayList<SortPairDouble> arrayDouble1=new ArrayList<>();
//	      ArrayList<SortPairDouble> arrayDouble2=new ArrayList<>();
//	      ArrayList<SortPairDouble> arrayDouble3=new ArrayList<>();
//	      
//	      for(int l=0;l<23154*topicNum;l++){
//	    	  if(Math.abs(dv.get(l))>0.0){
//	    		  arrayDouble1.add(new SortPairDouble(l,Math.abs(dv.get(l))));
//		    	  
//	    	  }
//	    	  
//	      }
//	      for(int l=23154*topicNum;l<23154*(topicNum+1);l++){
//	    	  arrayDouble2.add(new SortPairDouble(l,Math.abs(dv.get(l))));
//	    	  
//	      }
//	      for(int l=23154*(topicNum+1);l<dv.length;l++){
//	    	  arrayDouble3.add(new SortPairDouble(l,Math.abs(dv.get(l))));
//	    	  
//	      }
//	    
//	      Collections.sort(arrayDouble1);
//	      Collections.sort(arrayDouble2);
//	      Collections.sort(arrayDouble3);
//	      for(int top=0;top<10;top++){
//	    	  int key1=arrayDouble1.get(top).key;
//	    	  double value1=dv.get(key1);
//////	    	  
////	    	  double value1=arrayDouble1.get(top).value;
//	    	  int key2=arrayDouble2.get(top).key;
//	    	  double value2=arrayDouble2.get(top).value;
////	    	  int key3=arrayDouble3.get(top).key;
////	    	  double value3=arrayDouble3.get(top).value;
//	    	 
//	    	  bw.write("\t"+key1+":\t"+doubleDecimal.getString(value1)+"\t"+doubleDecimal.getString(theta.get(key1))+"\t"
////	    			  +"\t"+doubleDecimal.getString(value2)+"\t"+doubleDecimal.getString(theta.get(key2))+"\t"
////	    			  +"\t"+doubleDecimal.getString(value3)+"\t"+doubleDecimal.getString(theta.get(key3))
//	    			  +"\n");
//	     
//	      }		
      }
//      bw.write("\nupdateNum:"+updateNum+"\n");
//      bw.write("\tcost"+evaluateCost.getCost()+"\n**********\n");
      
//        System.out.println("Iteration " + iteration + " | Cost: "
//            + evaluateCost.getCost()+"  alpha:"+alpha);
//        theta.print();
      
      shiftLeft(lastCosts);
      lastCosts[lastIndex] = evaluateCost.getCost();
      // break if we converged below the limit
      if (converged(lastCosts, breakDifference)) {
        break;
      }
      // break if we are going in the wrong direction
      if (breakOnDivergence && ascending(lastCosts)) {
        break;
      }

      DoubleVector gradient = evaluateCost.getGradient();
      // check the bold driver
      if (boldDriver) {
        if (lastGradient != null) {
          double costDifference = getCostDifference(lastCosts);
          if (costDifference < 0) {
            // we can increase, because cost decreased
            alpha += (alpha * boldDecreasePercentage);
          } else {
            // we decrease, because cost increased
            // we undo the last theta change
            theta = lastTheta;
            gradient = lastGradient;
            alpha -= (alpha * boldIncreasePercentage);
          }
//          if (verbose) {
//            LOG.info("Iteration " + iteration + " | Alpha: " + alpha + "\n");
//          }
        }
        lastGradient = gradient;
      }
      // check annealing
      if (annealingIteration > 0) {
        // always pick the initial learning rate
        alpha = this.alpha / (1d + iteration / annealingIteration);
      }
      // save our last parameter
      lastTheta = theta;
      // basically subtract the gradient multiplied with the learning rate
      
      
      theta = theta.subtract(gradient.multiply(alpha));
      if (lastTheta != null && momentum != 0d) {
        // we add momentum as the parameter "m" multiplied by the difference of
        // both theta vectorxs
        theta = theta.add((lastTheta.subtract(theta)).multiply(momentum));
      }
      onIterationFinished(iteration, evaluateCost.getCost(), theta);
    }
   
    return theta;

  }

  @Override
  public final DoubleVector minimize(CostFunction f, DoubleVector pInput,
       
		  final int maxIterations, boolean verbose) throws IOException {

    double[] lastCosts = new double[COST_HISTORY];
    Arrays.fill(lastCosts, Double.MAX_VALUE);
    final int lastIndex = lastCosts.length - 1;
    DoubleVector lastTheta = null;
    DoubleVector lastGradient = null;
    DoubleVector theta = pInput;
    double alpha = this.alpha;
    for (int iteration = 0; iteration < maxIterations; iteration++) {
//    	System.out.println("it:"+iteration+" "+alpha+"\n");
    	CostGradientTuple evaluateCost = f.evaluateCost(theta,iteration);
//      if(iteration>10){
//    	  break;
//      }
//      bw.write("it:"+iteration+" "+alpha+"\n");
//      bw.write("\t para:\t");
//      for(int l=0;l<theta.length;l++){
//    	  if(Math.abs(theta.get(l))>0.0){
//    	  bw.write(l+":"+theta.get(l)+" ");
//    	  }
//      }
//      bw.write("\n\tgradient:\t");
      DoubleVector dv=evaluateCost.getGradient();
      int updateNum=0;
      ArrayList<SortPairDouble> arrayDouble1=new ArrayList<>();
      ArrayList<SortPairDouble> arrayDouble2=new ArrayList<>();
      ArrayList<SortPairDouble> arrayDouble3=new ArrayList<>();
      
      for(int l=0;l<23154*40;l++){
    	  arrayDouble1.add(new SortPairDouble(l,Math.abs(dv.get(l))));
    	  
      }
      for(int l=23154*40;l<23154*41;l++){
    	  arrayDouble2.add(new SortPairDouble(l,Math.abs(dv.get(l))));
    	  
      }
      for(int l=23154*41;l<dv.length;l++){
    	  arrayDouble3.add(new SortPairDouble(l,Math.abs(dv.get(l))));
    	  
      }
    
      Collections.sort(arrayDouble1);
      Collections.sort(arrayDouble2);
      Collections.sort(arrayDouble3);
      for(int top=0;top<10;top++){
    	  int key1=arrayDouble1.get(top).key;
    	  double value1=arrayDouble1.get(top).value;
    	  int key2=arrayDouble2.get(top).key;
    	  double value2=arrayDouble2.get(top).value;
    	  int key3=arrayDouble3.get(top).key;
    	  double value3=arrayDouble3.get(top).value;
    	 
//    	  bw.write("\t"+key1+":"+value1+"\t"+theta.get(key1)+"\t"
//    			  +"\t"+key2+":"+value2+"\t"+theta.get(key2)+"\t"
//    			  +"\t"+key3+":"+value3+"\t"+theta.get(key3)+"\n");
    	  bw.write("\t"+value1+"\t"+theta.get(key1)+"\t"
    			  +"\t"+value2+"\t"+theta.get(key2)+"\t"
    			  +"\t"+value3+"\t"+theta.get(key3)+"\n");
     
      }
      bw.write("\nupdateNum:"+updateNum+"\n");
      bw.write("\tcost"+evaluateCost.getCost()+"\n**********\n");
      
//        System.out.println("Iteration " + iteration + " | Cost: "
//            + evaluateCost.getCost()+"  alpha:"+alpha);
//        theta.print();
      
      shiftLeft(lastCosts);
      lastCosts[lastIndex] = evaluateCost.getCost();
      // break if we converged below the limit
      if (converged(lastCosts, breakDifference)) {
        break;
      }
      // break if we are going in the wrong direction
      if (breakOnDivergence && ascending(lastCosts)) {
        break;
      }

      DoubleVector gradient = evaluateCost.getGradient();
      // check the bold driver
      if (boldDriver) {
        if (lastGradient != null) {
          double costDifference = getCostDifference(lastCosts);
          if (costDifference < 0) {
            // we can increase, because cost decreased
            alpha += (alpha * boldDecreasePercentage);
          } else {
            // we decrease, because cost increased
            // we undo the last theta change
            theta = lastTheta;
            gradient = lastGradient;
            alpha -= (alpha * boldIncreasePercentage);
          }
//          if (verbose) {
//            LOG.info("Iteration " + iteration + " | Alpha: " + alpha + "\n");
//          }
        }
        lastGradient = gradient;
      }
      // check annealing
      if (annealingIteration > 0) {
        // always pick the initial learning rate
        alpha = this.alpha / (1d + iteration / annealingIteration);
      }
      // save our last parameter
      lastTheta = theta;
      // basically subtract the gradient multiplied with the learning rate
      theta = theta.subtract(gradient.elementWise(alphaArray_));
      
//      theta = theta.subtract(gradient.multiply(alpha));
      if (lastTheta != null && momentum != 0d) {
        // we add momentum as the parameter "m" multiplied by the difference of
        // both theta vectorxs
        theta = theta.add((lastTheta.subtract(theta)).multiply(momentum));
      }
      onIterationFinished(iteration, evaluateCost.getCost(), theta);
    }
    bw.close();
    return theta;

  }

  /**
   * Minimize a given cost function f with the initial parameters pInput (also
   * called theta) with a learning rate alpha and a fixed number of iterations.
   * The loop can break earlier if costs converge below the limit. If the same
   * cost was archieved three times in a row, it will also break the iterations.
   * 
   * @param f the function to minimize.
   * @param pInput the starting parameters.
   * @param alpha the learning rate.
   * @param limit the cost to archieve to break the iterations.
   * @param length the number of iterations.
   * @param verbose if true prints progress to STDOUT.
   * @return the learned minimal parameters.
 * @throws IOException 
   */
  public static DoubleVector minimizeFunction(CostFunction f,
      DoubleVector pInput, double alpha, double limit, int length,
      final boolean verbose) throws IOException {
    return new GradientDescent(alpha, limit).minimize(f, pInput, length,
        verbose);
  }

  static void shiftLeft(double[] lastCosts) {
    final int lastIndex = lastCosts.length - 1;
    for (int i = 0; i < lastIndex; i++) {
      lastCosts[i] = lastCosts[i + 1];
    }
    // shift MAX_VALUE into the last position
    lastCosts[lastIndex] = Double.MAX_VALUE;
  }

  static boolean converged(double[] lastCosts, double limit) {
    return Math.abs(getCostDifference(lastCosts)) < limit;
  }

  static boolean ascending(double[] lastCosts) {
    double last = lastCosts[0];
    boolean ascending = false;
    for (int i = 1; i < lastCosts.length; i++) {
      ascending = last < lastCosts[i];
      last = lastCosts[i];
    }
    return ascending;
  }

  private static double getCostDifference(double[] lastCosts) {
    return lastCosts[lastCosts.length - 1] - lastCosts[lastCosts.length - 2];
  }

}
