/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/**
 * @author Reagan Kelly
 * (c) 2009
 */

package edu.umich.sph.epidkardia.RiskIndex;

import java.io.File;
import java.io.IOException;
import java.util.*;

import weka.core.*;
import edu.umich.sph.epidkardia.RiskIndex.RiskIndexData.VARTYPES;
import edu.umich.sph.epidkardia.Util.DelimitedParserFormat;
import edu.umich.sph.epidkardia.Util.DelimitedWriter;


public class RiskIndexPerformanceEstimator {
  
  private enum PERFORMANCE {Sensitivity, Specificity, Misclassification, PPV};  
  private ArrayList<EnumMap<PERFORMANCE, Double>> indtestperformance = new ArrayList<EnumMap<PERFORMANCE, Double>>();
  
  private enum BOOTSTRAPPING {Bias, SE, LowerCI, UpperCI};
  private ArrayList<EnumMap<PERFORMANCE, EnumMap<BOOTSTRAPPING, Double>>> indBootstrapPerformance = new ArrayList<EnumMap<PERFORMANCE, EnumMap<BOOTSTRAPPING, Double>>>();

  private RiskIndexData riData;
  private RiskIndexModel [] riModel;
  private RiskIndexPredictions [] riPreds;
  
  public RiskIndexPerformanceEstimator(){
    
  }
  
  public RiskIndexPerformanceEstimator(RiskIndexData rd, RiskIndexModel [] rm){
    riData = new RiskIndexData(rd);
    
    riModel = new RiskIndexModel[rm.length];
    for(int i=0; i<rm.length; i++){
      System.out.println(i);
      riModel[i] = new RiskIndexModel();
      riModel[i].setCoefficients(rm[i].getCoefficients());
      riModel[i].setCutpoints(rm[i].getCutpoints());
      riModel[i].setModelVariables(rm[i].getModelVariables());
    }
    
    riPreds = new RiskIndexPredictions[riData.getIndependentTestSet().numInstances()];
  }
  
  /**
   * A method to set the data to be used to estimate performance
   * 
   * @param ri a RiskIndexData object
   */
  public void setData(RiskIndexData ri){
    riData = new RiskIndexData(ri);
    riPreds = new RiskIndexPredictions[riData.getIndependentTestSet().numInstances()];
  }
  
  /**
   * A method to set the model to be used to estimate performance
   * 
   * @param rm a RiskIndexModel[] object
   */
  public void setModel(RiskIndexModel[] rm){
    riModel = new RiskIndexModel[rm.length];
    for(int i=0; i<rm.length; i++){
      riModel[i].setCoefficients(rm[i].getCoefficients());
      riModel[i].setCutpoints(rm[i].getCutpoints());
      riModel[i].setModelVariables(rm[i].getModelVariables());
    }
  }
  
  /**
   * A method to estimate the performance of a particular RiskIndexModel object on a given 
   * RiskIndexData object
   */
  public void estimatePerformance(){
    
    ArrayList<VARTYPES> v = new ArrayList<VARTYPES>(riData.getVariables().keySet());

    VARTYPES[] vtypes = new VARTYPES[v.size()];
    v.toArray(vtypes);
    
    double[] sen = new double[riModel[0].getModelVariables().size()];
    double[] spe = new double[riModel[0].getModelVariables().size()];
    double[] mc = new double[riModel[0].getModelVariables().size()];
    double[] ppv = new double[riModel[0].getModelVariables().size()];
    
    int[][][] predictedOutcomes = new int[riModel[0].getModelVariables().size()][riModel.length][riData.getIndependentTestSet().numInstances()];
  
    for(int i=0; i<riModel.length; i++){
	for(int w=0; w<riModel[i].getModelVariables().size(); w++){
	  
	  double[] testrisks = new double[riData.getIndependentTestSet().numInstances()];
	  
	  
	  testrisks = getRisks(riData.getIndependentTestSet(), riModel[i].getModelVariables().get(vtypes[w]), null, riModel[i].getCoefficients());
		
	  double criticalValue = riModel[i].getCutpoints().get(vtypes[w]);
	  
	  for(int q=0; q<testrisks.length; q++){
	    if(testrisks[q]<criticalValue){
	      predictedOutcomes[w][i][q]=0;
	    }
	    else{
	      predictedOutcomes[w][i][q]=1;
	    }
	  }
	}
    }
    
    int[][] finalPredictions = new int[riModel[0].getModelVariables().size()][riData.getIndependentTestSet().numInstances()];
    int[] outcome = new int[riData.getIndependentTestSet().numInstances()];
    for(int z=0; z<riData.getIndependentTestSet().numInstances(); z++){
	outcome[z] = (int) riData.getIndependentTestSet().instance(z).value(riData.getIndependentTestSet().attribute(riData.getClassName()));
    }
    
    for(int w=0; w<riModel[0].getModelVariables().size(); w++){
	double tp = 0;
	double tn = 0;
	double fn = 0;
	double fp = 0;
	for(int i=0; i<riData.getIndependentTestSet().numInstances(); i++){
	  double predsum = 0;
	  
	  
	  for(int m=0; m<riModel.length; m++){
	    predsum += predictedOutcomes[w][m][i];  
	  }
	  if(predsum >= riModel.length/2){
	    finalPredictions[w][i] = 1;
	  }
	  else{
	    finalPredictions[w][i] = 0;
	  }
	    	
	  if(outcome[i]==0 && finalPredictions[w][i]==0){
	    tn++;
	  }
	  else if(outcome[i]==1 && finalPredictions[w][i]==1){
	    tp++;
	  }
	  else if(outcome[i]==1 && finalPredictions[w][i]==0){
	    fn++;
	  }
	  else if(outcome[i]==0 && finalPredictions[w][i]==1){
	    fp++;
	  }
	  if(w==0){
	    riPreds[i] = new RiskIndexPredictions();
	  }
	  if(riData.getIDName() != null){
	    Attribute a = riData.getIndependentTestSet().attribute(riData.getIDName());
	    
	    int ididx = (int) riData.getIndependentTestSet().instance(i).value(a);
	    
	    riPreds[i].setID(new String(a.value(ididx)));
	  }
	  
	  riPreds[i].setOutcome(outcome[i]);
	  riPreds[i].addPrediction(vtypes[w], finalPredictions[w][i]);
	  
	  if(finalPredictions[w][i] == 1){
	    riPreds[i].addConfidence(vtypes[w], predsum/riModel.length);
	  }
	  else{
	    riPreds[i].addConfidence(vtypes[w], 1-(predsum/riModel.length));
	  }
	    
	}
	
	sen[w] = tp/(tp+fn);
	spe[w] = tn/(tn+fp);
	mc[w] = (fn+fp)/(tn+tp+fn+fp);
	ppv[w] = tp/(tp+fp);
	
	EnumMap<PERFORMANCE, Double> p = new EnumMap<PERFORMANCE, Double>(PERFORMANCE.class);
	p.put(PERFORMANCE.Sensitivity, new Double(sen[w]));
	p.put(PERFORMANCE.Specificity, new Double(spe[w]));
	p.put(PERFORMANCE.Misclassification, new Double(mc[w]));
	p.put(PERFORMANCE.PPV, new Double(ppv[w]));
	indtestperformance.add(p);
    }

    
  }
  
  /**
   * A method to estimate the performance of a particular RiskIndexModel object on a given 
   * RiskIndexData object. A supplied number of bootstrap samples of the RiskIndexData object 
   * for this class are generated, performance is estimated on each, and 95% confidence intervals are 
   * estimated
   * 
   * @param bootstrap an int giving the number of bootstrap samples of the RiskIndexData object to create
   */
  public void bootstrapPerformanceEstimate(int bootstrap){

    ArrayList<VARTYPES> v = new ArrayList<VARTYPES>(riData.getVariables().keySet());

    VARTYPES[] vtypes = new VARTYPES[v.size()];
    v.toArray(vtypes);
    
    double [][] sen = new double [riModel[0].getModelVariables().size()][bootstrap];
    double [][] spe = new double [riModel[0].getModelVariables().size()][bootstrap];
    double [][] mc = new double [riModel[0].getModelVariables().size()][bootstrap];
    double [][] ppv = new double [riModel[0].getModelVariables().size()][bootstrap];
    

    double [] senbias = new double[riModel[0].getModelVariables().size()];
    double [] spebias = new double[riModel[0].getModelVariables().size()];
    double [] mcbias = new double[riModel[0].getModelVariables().size()];
    double [] ppvbias = new double[riModel[0].getModelVariables().size()];

    double [] sensd = new double[riModel[0].getModelVariables().size()];
    double [] spesd = new double[riModel[0].getModelVariables().size()];
    double [] mcsd = new double[riModel[0].getModelVariables().size()];
    double [] ppvsd = new double[riModel[0].getModelVariables().size()];

    double [] senlci = new double[riModel[0].getModelVariables().size()];
    double [] spelci = new double[riModel[0].getModelVariables().size()];
    double [] mclci = new double[riModel[0].getModelVariables().size()];
    double [] ppvlci = new double[riModel[0].getModelVariables().size()];

    double [] senuci = new double[riModel[0].getModelVariables().size()];
    double [] speuci = new double[riModel[0].getModelVariables().size()];
    double [] mcuci = new double[riModel[0].getModelVariables().size()];
    double [] ppvuci = new double[riModel[0].getModelVariables().size()];
    
    
    for(int b=0; b<bootstrap; b++){

      Instances sample = new Instances(riData.getIndependentTestSet());
      Instances bootSample = sample.resample(new Random());
      int[][][] predictedOutcomes = new int[riModel[0].getModelVariables().size()][riModel.length][bootSample.numInstances()];
    
      for(int i=0; i<riModel.length; i++){
	for(int w=0; w<riModel[i].getModelVariables().size(); w++){
	  int[] outcome = new int[bootSample.numInstances()];
	  double[] testrisks = new double[bootSample.numInstances()];
	  
	  for(int z=0; z<bootSample.numInstances(); z++){
	    outcome[z] = (int) bootSample.instance(z).value(bootSample.attribute(riData.getClassName()));
	  }

	  testrisks = getRisks(bootSample, riModel[i].getModelVariables().get(vtypes[w]), null, riModel[i].getCoefficients());
		
	  double criticalValue = riModel[i].getCutpoints().get(vtypes[w]);
	  
	  for(int q=0; q<testrisks.length; q++){
	    if(testrisks[q]<criticalValue){
	      predictedOutcomes[w][i][q]=0;
	    }
	    else{
	      predictedOutcomes[w][i][q]=1;
	    }
	  }
	}
      }
      
      int[][] finalPredictions = new int[riModel[0].getModelVariables().size()][bootSample.numInstances()];
      int[] outcome = new int[bootSample.numInstances()];
      for(int z=0; z<bootSample.numInstances(); z++){
  	outcome[z] = (int) bootSample.instance(z).value(bootSample.attribute(riData.getClassName()));
      }
      for(int w=0; w<riModel[0].getModelVariables().size(); w++){
	double tp = 0;
	double tn = 0;
	double fn = 0;
	double fp = 0;
	for(int i=0; i<bootSample.numInstances(); i++){
	  double predsum = 0;
	  
	  
	  for(int m=0; m<riModel.length; m++){
	    predsum += predictedOutcomes[w][m][i];  
	  }
	  if(predsum >= riModel.length/2){
	    finalPredictions[w][i] = 1;
	  }
	  else{
	    finalPredictions[w][i] = 0;
	  }
	    	
	  if(outcome[i]==0 && finalPredictions[w][i]==0){
	    tn++;
	  }
	  else if(outcome[i]==1 && finalPredictions[w][i]==1){
	    tp++;
	  }
	  else if(outcome[i]==1 && finalPredictions[w][i]==0){
	    fn++;
	  }
	  else if(outcome[i]==0 && finalPredictions[w][i]==1){
	    fp++;
	  }
	    	
	}
	
	sen[w][b] = tp/(tp+fn);
	spe[w][b] = tn/(tn+fp);
	mc[w][b] = (fn+fp)/(tn+tp+fn+fp);
	ppv[w][b] = tp/(tp+fp);
      }

      
      
      
    }
    
    
    for(int w=0; w<riModel[0].getModelVariables().size(); w++){
      senbias[w] = MathUtils.bias(indtestperformance.get(w).get(PERFORMANCE.Sensitivity).doubleValue(), sen[w]);
      spebias[w] = MathUtils.bias(indtestperformance.get(w).get(PERFORMANCE.Specificity).doubleValue(), spe[w]);
      mcbias[w] = MathUtils.bias(indtestperformance.get(w).get(PERFORMANCE.Misclassification).doubleValue(), mc[w]);
      ppvbias[w] = MathUtils.bias(indtestperformance.get(w).get(PERFORMANCE.PPV).doubleValue(), ppv[w]);
      
      sensd[w] = MathUtils.sd(sen[w]);
      spesd[w] = MathUtils.sd(spe[w]);
      mcsd[w] = MathUtils.sd(mc[w]);
      ppvsd[w] = MathUtils.sd(ppv[w]);
      
      double lower = Math.floor(bootstrap*0.025);
      double upper = Math.ceil(bootstrap*0.975);

      ArrayList<double[]> s = new ArrayList<double[]>(Arrays.asList(sen[w]));
      double [] sort =  s.get(0);
      Arrays.sort(sort);
      senlci[w]=sort[(int) lower];
      senuci[w]=sort[(int) upper];

      s = new ArrayList<double[]>(Arrays.asList(spe[w]));
      sort =  s.get(0);
      Arrays.sort(sort);
      spelci[w]=sort[(int) lower];
      speuci[w]=sort[(int) upper];

      s = new ArrayList<double[]>(Arrays.asList(mc[w]));
      sort =  s.get(0);
      Arrays.sort(sort);
      mclci[w]=sort[(int) lower];
      mcuci[w]=sort[(int) upper];

      s = new ArrayList<double[]>(Arrays.asList(ppv[w]));
      sort =  s.get(0);
      Arrays.sort(sort);
      ppvlci[w]=sort[(int) lower];
      ppvuci[w]=sort[(int) upper];

      
      EnumMap<BOOTSTRAPPING, Double> bp = new EnumMap<BOOTSTRAPPING, Double>(BOOTSTRAPPING.class);

      EnumMap<PERFORMANCE, EnumMap<BOOTSTRAPPING, Double>> per = new EnumMap<PERFORMANCE, EnumMap<BOOTSTRAPPING, Double>>(PERFORMANCE.class);

      bp.put(BOOTSTRAPPING.Bias, new Double(senbias[w]));
      bp.put(BOOTSTRAPPING.SE, new Double(sensd[w]));
      bp.put(BOOTSTRAPPING.LowerCI, new Double(senlci[w]));
      bp.put(BOOTSTRAPPING.UpperCI, new Double(senuci[w]));
      per.put(PERFORMANCE.Sensitivity, new EnumMap<BOOTSTRAPPING, Double>(bp));

      bp.clear();
      bp.put(BOOTSTRAPPING.Bias, new Double(spebias[w]));
      bp.put(BOOTSTRAPPING.SE, new Double(spesd[w]));
      bp.put(BOOTSTRAPPING.LowerCI, new Double(spelci[w]));
      bp.put(BOOTSTRAPPING.UpperCI, new Double(speuci[w]));
      per.put(PERFORMANCE.Specificity, new EnumMap<BOOTSTRAPPING, Double>(bp));

      bp.clear();
      bp.put(BOOTSTRAPPING.Bias, new Double(mcbias[w]));
      bp.put(BOOTSTRAPPING.SE, new Double(mcsd[w]));
      bp.put(BOOTSTRAPPING.LowerCI, new Double(mclci[w]));
      bp.put(BOOTSTRAPPING.UpperCI, new Double(mcuci[w]));
      per.put(PERFORMANCE.Misclassification, new EnumMap<BOOTSTRAPPING, Double>(bp));

      bp.clear();
      bp.put(BOOTSTRAPPING.Bias, new Double(ppvbias[w]));
      bp.put(BOOTSTRAPPING.SE, new Double(ppvsd[w]));
      bp.put(BOOTSTRAPPING.LowerCI, new Double(ppvlci[w]));
      bp.put(BOOTSTRAPPING.UpperCI, new Double(ppvuci[w]));
      per.put(PERFORMANCE.PPV, new EnumMap<BOOTSTRAPPING, Double>(bp));

      
      indBootstrapPerformance.add(new EnumMap<PERFORMANCE, EnumMap<BOOTSTRAPPING, Double>>(per));
    }


  }
 
  /**
   * A method to save performance metrics out to a file fter the performance of the independent test set is assessed  
   * using a bootstrap sampling scheme, 
   */
  public void saveBootstrapPerformance(){


    try{
      ArrayList<VARTYPES> v = new ArrayList<VARTYPES>(riData.getVariables().keySet());

      VARTYPES[] vtypes = new VARTYPES[v.size()];
      v.toArray(vtypes);

      ArrayList<PERFORMANCE> p = new ArrayList<PERFORMANCE>(indtestperformance.get(0).keySet());
      PERFORMANCE[] ptypes = new PERFORMANCE[p.size()];
      p.toArray(ptypes);

      ArrayList<BOOTSTRAPPING> b = new ArrayList<BOOTSTRAPPING>(indBootstrapPerformance.get(0).get(ptypes[0]).keySet());
      BOOTSTRAPPING[] btypes = new BOOTSTRAPPING[b.size()];
      b.toArray(btypes);

      





      String[] indheaderRow = new String[(ptypes.length*(btypes.length+1))+1];
      indheaderRow[0] = "Model";
      int count=1;
      for(int i=0; i<ptypes.length; i++){
	indheaderRow[count] = ptypes[i] + " Est.";
	count++;
	indheaderRow[count] = ptypes[i] + " Lower CI";
	count++;
	indheaderRow[count] = ptypes[i] + " Upper CI";
	count++;
	indheaderRow[count] = ptypes[i] + " Bias";
	count++;
	indheaderRow[count] = ptypes[i] + " SE";
	count++;
      }

      DelimitedWriter dwrite = new DelimitedWriter(new File(riData.getBaseName() + riData.getResultsFolder()+ "BootstrappingIndTestSetResults.csv"), DelimitedParserFormat.CSV);
      String[] line = new String[(ptypes.length*(btypes.length+1))+1];
      dwrite.writeLine(indheaderRow);
      for(int i=0; i<vtypes.length;i++){
	line[0]=vtypes[i].toString();
	int pcount = 1;
	for(int j=0; j<ptypes.length; j++){
	  line[pcount]=MathUtils.roundDigits(indtestperformance.get(i).get(ptypes[j]), 3).toString();
	  pcount++;
	  line[pcount] = MathUtils.roundDigits(indBootstrapPerformance.get(i).get(ptypes[j]).get(BOOTSTRAPPING.LowerCI), 3).toString();
	  pcount++;
	  line[pcount] = MathUtils.roundDigits(indBootstrapPerformance.get(i).get(ptypes[j]).get(BOOTSTRAPPING.UpperCI), 3).toString();
	  pcount++;
	  line[pcount] = MathUtils.roundDigits(indBootstrapPerformance.get(i).get(ptypes[j]).get(BOOTSTRAPPING.Bias), 3).toString();
	  pcount++;
	  line[pcount] = MathUtils.roundDigits(indBootstrapPerformance.get(i).get(ptypes[j]).get(BOOTSTRAPPING.SE), 3).toString();
	  pcount++;

	}

	dwrite.writeLine(line);
      }
      
      dwrite.close();



    }
    catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * A method to save performance metrics out to a file after the performance of the independent test set is assessed 
   */
  public void savePerformance(){
    ArrayList<VARTYPES> v = new ArrayList<VARTYPES>(riData.getVariables().keySet());

    VARTYPES[] vtypes = new VARTYPES[v.size()];
    v.toArray(vtypes);
    
    String[] indheaderRow = new String[5];
    indheaderRow[0] = "Model";
    indheaderRow[1] = "Sensitivity";
    indheaderRow[2] = "Specificity";
    indheaderRow[3] = "Misclassification";
    indheaderRow[4] = "PPV";
   

    DelimitedWriter dwrite;
    try {
      dwrite = new DelimitedWriter(new File(riData.getBaseName() + riData.getResultsFolder()+ "IndependentTestSetResults.csv"), DelimitedParserFormat.CSV);
    
      String[] line = new String[5];
      dwrite.writeLine(indheaderRow);
      for(int w=0; w<indtestperformance.size(); w++){
	EnumMap<PERFORMANCE, Double> p = indtestperformance.get(w);
	line[0] = vtypes[w].toString();
	line[1] = new Double(MathUtils.roundDigits(p.get(PERFORMANCE.Sensitivity), 3)).toString();
	line[2] = new Double(MathUtils.roundDigits(p.get(PERFORMANCE.Specificity), 3)).toString();
	line[3] = new Double(MathUtils.roundDigits(p.get(PERFORMANCE.Misclassification), 3)).toString();
	line[4] = new Double(MathUtils.roundDigits(p.get(PERFORMANCE.PPV), 3)).toString();
	
	dwrite.writeLine(line);
      }

    
    dwrite.close();
      
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * A method to save the predictions made about the RiskIndexData object to a file
   */
  public void savePredictions(){
    ArrayList<VARTYPES> v = new ArrayList<VARTYPES>(riData.getVariables().keySet());

    VARTYPES[] vtypes = new VARTYPES[v.size()];
    v.toArray(vtypes);
    
    String[] indheaderRow = new String[2 + vtypes.length*2];
    indheaderRow[0] = "ID";
    indheaderRow[1] = "Outcome";
    for(int i=0; i<vtypes.length; i++){
      indheaderRow[2+(i*2)] = vtypes[i].toString() + " Prediction";
      indheaderRow[3+(i*2)] = vtypes[i].toString() + " Confidence";
    }
   

    DelimitedWriter dwrite;
    try {
      dwrite = new DelimitedWriter(new File(riData.getBaseName() + riData.getResultsFolder()+ "IndependentTestSetPredictions.csv"), DelimitedParserFormat.CSV);
    
      String[] line = new String[2 + vtypes.length*2];
      dwrite.writeLine(indheaderRow);
      for(int i=0; i<riPreds.length; i++){
 	line[0] = riPreds[i].getID();
	line[1] = Integer.toString(riPreds[i].getOutcome());
	for(int w=0; w<vtypes.length; w++){

	  
	  line[2+(w*2)] = Integer.toString(riPreds[i].getPrediction(vtypes[w]));
	  line[3+(w*2)] = Double.toString(MathUtils.roundDigits(riPreds[i].getConfidence(vtypes[w]), 3));
	}
 	dwrite.writeLine(line);
      }

    
    dwrite.close();
      
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  /**
   * A methof to get risks based on coefficients and a variable set
   * 
   * @param data the set of Instances to calculate the risks for
   * @param vars the variables to use to calculate the risk
   * @param coeffs the hashmap storing the logistic regression coefficients
   * @return an array of doubles representing the risk for each person in data
   */
  public double[] getRisks(Instances data, ArrayList<String> vars, String currVar, HashMap<String, HashMap<String, Double>> coeffs){
    
    int numVariables = vars.size();
    if(currVar != null){
      numVariables++;
    }
    double[] risks = new double[data.numInstances()];
    int[] nullCount = new int[data.numInstances()];
    for(int j=0; j<(numVariables); j++){
     
      //Plus one for the current variable being examined
      String loopvar = null;
	if(j==vars.size()){
	  loopvar = currVar;
	}
	else{
	  loopvar = vars.get(j);
	}
	Attribute attr = data.attribute(loopvar);
      for(int i=0; i<data.numInstances(); i++){
	
	if(attr.isNumeric()){
	  
	  risks[i] += (data.instance(i).value(attr))*coeffs.get(loopvar).get("numeric");
	  
	}
	else{

	  HashMap<String, Double> cm = coeffs.get(loopvar);

	  double v = data.instance(i).value(attr);
	  String a = attr.value((int) v);
	  
	  Double val = cm.get(a);
	  if(val == null){
	    val = new Double(0);
	    nullCount[i]++;
	  }
	  risks[i] += val;
	}
      }
      

    }
    
    for(int i=0; i<data.numInstances(); i++){
      risks[i] = risks[i]/(numVariables-nullCount[i]);
    }
    
    return(risks);
  }


  
}
