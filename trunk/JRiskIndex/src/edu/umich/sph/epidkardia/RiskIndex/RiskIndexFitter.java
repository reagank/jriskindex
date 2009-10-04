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


import weka.classifiers.functions.*;

import java.io.*;

import weka.core.*;
import weka.filters.supervised.attribute.NominalToBinary;
import weka.filters.unsupervised.attribute.*;
import weka.filters.*;

import java.util.*;
import java.lang.Math;

import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.*;
import org.jppf.server.protocol.JPPFRunnable;

import edu.umich.sph.epidkardia.RiskIndex.RiskIndexData.VARTYPES;
import edu.umich.sph.epidkardia.Util.*;

@SuppressWarnings({ "unused", "serial" })
public class RiskIndexFitter extends JPPFTask{
  
 
  /**
   * 
   */

  private EnumMap<VARTYPES, ArrayList<String>> modelvariables = new EnumMap<VARTYPES, ArrayList<String>>(VARTYPES.class);
  private EnumMap<VARTYPES, Double> modelcutpoints = new EnumMap<VARTYPES, Double>(VARTYPES.class);
  private EnumMap<VARTYPES,  ArrayList<String>> untrimmedModel= new EnumMap<VARTYPES,  ArrayList<String>>(VARTYPES.class);
  private EnumMap<VARTYPES, double[]> brierScores= new EnumMap<VARTYPES, double[]>(VARTYPES.class);
  
  
  private ArrayList<ArrayList<HashMap<String, HashMap<String, Double>>>> coefficientMap = new ArrayList<ArrayList<HashMap<String, HashMap<String, Double>>>>();
  
  private ArrayList<double[]> modelscores = new ArrayList<double[]>(); 

  RiskIndexData ridata;
  
  Instances bootstrappedOptSet;
  Instances[][] optimizationtrainCV;
  Instances[][] optimizationtestCV;
  
  int [][] indTestPredictions;
  int iteration;
  
  
  /**
   * A function to divide the data into Crossvalidation sets
   */
  public void setCrossValidation(){
    if(ridata.getCrossValidationFolds() == -99){
      ridata.setCrossValidationFolds(new Integer(4).intValue());
    }

    if(ridata.getCrossValidationReps() == -99){
      ridata.setCrossValidationReps(new Integer(10).intValue());
    }

    optimizationtrainCV = new Instances[ridata.getCrossValidationReps()][ridata.getCrossValidationFolds()];
    optimizationtestCV = new Instances[ridata.getCrossValidationReps()][ridata.getCrossValidationFolds()];


    Instances tmpdata = bootstrappedOptSet;

    for(int i=0; i<ridata.getCrossValidationReps(); i++){
      tmpdata.randomize(new Random());
      for(int j=0; j<ridata.getCrossValidationFolds(); j++){
	optimizationtrainCV[i][j] = tmpdata.trainCV(ridata.getCrossValidationFolds(), j);
	optimizationtestCV[i][j] = tmpdata.testCV(ridata.getCrossValidationFolds(), j);


      }
    }

  }

  /**
   * Uses Logistic Regression (from weka) to calculate the 
   * coefficients for the Risk Index
   * 
   * @param vars String[] representing the varaibles which will 
   * be used for Risk Index building
   */
  public HashMap<String, HashMap<String, Double>> getCoefficients(Instances data, EnumMap<VARTYPES, ArrayList<String>> varset){
    ArrayList<ArrayList<String>> varlist = new ArrayList<ArrayList<String>>(varset.values());
    
    
    int varlength = 0;
    for(int i =0; i<varlist.size(); i++){
      varlength += varlist.get(i).size();
    }
    
    String [] vars = new String[varlength];
    int count = 0;

    for(int i =0; i<varlist.size(); i++){
      for(int j=0; j<varlist.get(i).size(); j++){
	
	vars[count] = varlist.get(i).get(j);
	count++;
      }
    }



    double mapsize = varlength/0.75;
    double [][] betas;
    int intSize = (int)mapsize;

    HashMap<String, Double> coefficients;
    HashMap<String, HashMap<String, Double>> cMap = new HashMap<String, HashMap<String, Double>>(intSize);


    try{



      Remove dropAtts = new Remove();

      for(int i = 0; i<vars.length; i++){
	
	int [] keepidx = {ridata.getClassIndex(), data.attribute(vars[i]).index()};
	String varname = data.attribute(vars[i]).name(); 
	dropAtts.setAttributeIndicesArray(keepidx);
	dropAtts.setInvertSelection(true);
	dropAtts.setInputFormat(data);
	Instances shortdata = Filter.useFilter(data, dropAtts);
	shortdata.setClass(shortdata.attribute(ridata.getClassName()));
	if(shortdata.attribute(varname).isNominal()){
	  NominalToBinary nom = new NominalToBinary();

	  nom.setInputFormat(shortdata);

	  Instances nomdata = Filter.useFilter(shortdata, nom);
	  nomdata.setClass(nomdata.attribute(ridata.getClassName()));



	  Instances shortnomdata;

	  if(shortdata.attribute(varname).numValues() >2){
	    Remove trimValues = new Remove();
	    if(nomdata.classIndex() == 0){
	      int [] nomidx = {1};
	      trimValues.setAttributeIndicesArray(nomidx);
	      trimValues.setInputFormat(nomdata);
	      trimValues.setInvertSelection(false);
	      shortnomdata = Filter.useFilter(nomdata, trimValues);
	    }
	    else{
	      int [] nomidx = {0};
	      trimValues.setAttributeIndicesArray(nomidx);
	      trimValues.setInputFormat(nomdata);
	      trimValues.setInvertSelection(false);
	      shortnomdata = Filter.useFilter(nomdata, trimValues);
	    }

	    shortnomdata.setClass(shortnomdata.attribute(ridata.getClassName()));

	    Logistic logisticTest = new Logistic(); 
	    logisticTest.buildClassifier(shortnomdata);

	    betas = logisticTest.getParameters();

	    double coefflen = (betas.length-1)/0.75;
	    int intlen = (int)coefflen;

	    coefficients = new HashMap<String, Double>(intlen);
	  }

	  else{
	    Logistic logisticTest = new Logistic(); 

	    logisticTest.buildClassifier(nomdata);


	    betas = logisticTest.getParameters();

	    double coefflen = (betas.length-1)/0.75;
	    int intlen = (int)coefflen;

	    coefficients = new HashMap<String, Double>(intlen);
	  }
	  //System.out.println("Getcoeff betas: " + betas.toString());

	}


	else{
	  Logistic logisticTest = new Logistic(); 

	  logisticTest.buildClassifier(shortdata);


	  betas = logisticTest.getParameters();

	  double coefflen = (betas.length-1)/0.75;
	  int intlen = (int)coefflen;

	  coefficients = new HashMap<String, Double>(intlen);
	}

	if(shortdata.attribute(varname).isNominal()){
	  /*
          //System.out.println(shortdata.attribute(1));
          if(varname.equals("GENDER")){
    		System.out.println(shortdata.attribute(1));
    		System.out.println(shortdata.attribute(1).value(1)+" "+betas[1][0]);
          }
	   */
	  for (int j = 1; j < betas.length; j++) {
	    for (int k = 0; k < betas[j].length; k++){   

	      coefficients.put(shortdata.attribute(1).value(j),  new Double(betas[j][k]));
	    }
	  }
	  coefficients.put(shortdata.attribute(1).value(0),  new Double(0));
	}
	else{
	  for (int j = 1; j < betas.length; j++) {
	    for (int k = 0; k < betas[j].length; k++){   
	      coefficients.put("numeric",  new Double(-1*betas[j][k]));
	    }
	  }
	}
	
	cMap.put(vars[i], coefficients);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    return(cMap);
  }
 
  
  /**
   * A function to select the variables to use for the risk index
   * 
   * This function does the bulk of the work, choosing the best variables for each variable type 
   * and then choosing the best performing subset.
   */
  public void selectVars(){
    try{
      System.out.println(bootstrappedOptSet.numAttributes());
    //DelimitedWriter dwrite = new DelimitedWriter(new File(ridata.getBaseName() + ridata.getResultsFolder() + "/fulloptresults"+iteration+".csv"), DelimitedParserFormat.CSV);
    String[] line = new String[5];
    modelvariables.clear(); 
    coefficientMap.clear();

    long sc = System.currentTimeMillis();

    AttributeStats as = ridata.getFullDataset().attributeStats(ridata.getFullDataset().attribute(ridata.getClassName()).index());

    System.out.println(ridata.getFullDataset().attribute(ridata.getClassName()).index());
    
    double initialcutpoint = (double) as.nominalCounts[0] /  (double)as.totalCount;
    

    for(int i=0; i<ridata.getCrossValidationReps(); i++){
      ArrayList<HashMap<String, HashMap<String, Double>>>cmap = new ArrayList<HashMap<String, HashMap<String, Double>>>();
      for(int j=0; j<ridata.getCrossValidationFolds(); j++){

	cmap.add(getCoefficients(optimizationtrainCV[i][j], ridata.getVariables()));
      }
      coefficientMap.add(cmap);
    }
    long ec = System.currentTimeMillis();
    System.out.println("Estimating Coefficients:" + (ec-sc));
    
    ArrayList<VARTYPES> v = new ArrayList<VARTYPES>(ridata.getVariables().keySet());

    VARTYPES[] vtypes = new VARTYPES[v.size()];
    v.toArray(vtypes);
    ArrayList<String> fixedvars = new ArrayList<String>();
    ArrayList<String> currentvars = new ArrayList<String>();

    //long avglooptime=0;
    int totalcount=0;

    long sv = System.currentTimeMillis();
    int prevsize = 0;
    
    
    for(int w=0; w<ridata.getVariables().size(); w++){
      modelcutpoints.put(vtypes[w], new Double(initialcutpoint));
      
      currentvars = new ArrayList<String>(ridata.getVariables().get(vtypes[w]));
      double [] bestscore = new double[ridata.getMaxVariables().get(vtypes[w])];
      double [] bestcutpoint = new double[ridata.getMaxVariables().get(vtypes[w])];
      int size = currentvars.size();
      int l=0;
      
      for(l=0; l<ridata.getMaxVariables().get(vtypes[w]); l++){


	double [] avgperf = new double[size-l];
	double [] cutpoint = new double[size-l];
	String bestVar = null;
	double bestVarScore = -Double.MAX_VALUE;
	double bestCutpoint = 0.0;
	int bestidx = 0;
	for(int m=0; m<(currentvars.size()); m++){
	  
	  String currVar = currentvars.get(m);
	  

	  double [] cuts = new double[5];
	  cuts[0] = initialcutpoint - 0.1;
	  cuts[1] = initialcutpoint - 0.05;
	  cuts[2] = initialcutpoint;
	  cuts[3] = initialcutpoint + 0.05;
	  cuts[4] = initialcutpoint + 0.1;


	  double [] varscore = {0, 0, 0, 0, 0};
	  double [] varsen = {0, 0, 0, 0, 0};
	  double [] varspe = {0, 0, 0, 0, 0};

	  
	  
	  for(int y=0; y<cuts.length; y++){
	    for(int i=0; i<ridata.getCrossValidationReps(); i++){
	      double itscore = 0;
	      double sen = 0;
	      double spe = 0;
	      for(int j=0; j<ridata.getCrossValidationFolds(); j++){
	    	  double[] metrics;
	    	  AttributeStats outcome_stats = optimizationtestCV[i][j].attributeStats(optimizationtestCV[i][j].attribute(ridata.getClassName()).index());

	    	  double negs = (double) outcome_stats.nominalCounts[0] /  (double)outcome_stats.totalCount;
	    	  double pos = 1-negs;
	    	  
		int[] testoutcome = new int[optimizationtestCV[i][j].numInstances()];
		double[] testrisks = new double[optimizationtestCV[i][j].numInstances()];
		double[] trainrisks = new double[optimizationtrainCV[i][j].numInstances()];

		for(int z=0; z<optimizationtestCV[i][j].numInstances(); z++){
		  testoutcome[z] = (int) optimizationtestCV[i][j].instance(z).value(optimizationtestCV[i][j].attribute(ridata.getClassName()));
		}
		
		int[] trainoutcome = new int[optimizationtrainCV[i][j].numInstances()];
		for(int z=0; z<optimizationtrainCV[i][j].numInstances(); z++){
			  trainoutcome[z] = (int) optimizationtrainCV[i][j].instance(z).value(optimizationtrainCV[i][j].attribute(ridata.getClassName()));
		}
		testrisks = getRisks(optimizationtestCV[i][j], fixedvars, currVar, coefficientMap.get(i).get(j));
		trainrisks = getRisks(optimizationtrainCV[i][j], fixedvars, currVar, coefficientMap.get(i).get(j));
		
		metrics = makePredictions(trainrisks, testrisks, testoutcome, trainoutcome, cuts[y], negs, pos);
		sen += metrics[0]/4;
		spe += metrics[1]/4;
		itscore += metrics[2]/4;
		
	      }
	      varscore[y] += itscore/ridata.getCrossValidationReps();
	      varsen[y] += sen/ridata.getCrossValidationReps();
	      varspe[y] += spe/ridata.getCrossValidationReps();
	    }
	  }
	 

	  
	  double minvarscore = MathUtils.min(varscore);
	  for(int y=0; y<cuts.length; y++){
	    if(varscore[y] == minvarscore && varsen[y] >= 0.5 && varspe[y]>=0.5){
	      cutpoint[m] = cuts[y];
	      line[1] = (new Double(varsen[y])).toString();
	      line[2] = (new Double(varspe[y])).toString();
	      line[3] = (new Double(varscore[y])).toString();
	      line[4] = (new Double(cuts[y])).toString();
	      break;
	    }
	    else if(varscore[y] == minvarscore){
	      cutpoint[m] = cuts[y];
	      line[1] = (new Double(varsen[y])).toString();
	      line[2] = (new Double(varspe[y])).toString();
	      line[3] = (new Double(varscore[y])).toString();
	      line[4] = (new Double(cuts[y])).toString();
	      break;
	    }
	  }
	  
	  avgperf[m] = minvarscore;
	 
	  totalcount++;
	  
	  //System.out.println(m+" "+l+" "+bestVar+" "+bestidx);
	  if(bestVar == null || avgperf[m] < bestVarScore){
	    bestVar = currVar;
	    bestVarScore = avgperf[m];
	    bestCutpoint = cutpoint[m];
	    bestidx = m;
	  }

	  //System.out.println(currVar + " " + avgperf[m]);
	}
	
	bestscore[l] = bestVarScore;
	bestcutpoint[l]= bestCutpoint;
	fixedvars.add(bestVar);
	line[0] = fixedvars.toString();
	//System.out.println(MathUtils.display(line));
	currentvars.remove(bestidx);
	System.out.println("Iteration " + l+" Adding "+bestVar+" Brier score: "+bestVarScore);
	//dwrite.writeLine(line);
      }

      double[] bs = new double[bestscore.length];
      System.arraycopy(bestscore, 0, bs, 0, bestscore.length);
      brierScores.put(vtypes[w], bs);
      ArrayList<String> rawVars = new ArrayList<String>(fixedvars);
      while(rawVars.size() > bs.length){
	rawVars.remove(0);
      }
      untrimmedModel.put(vtypes[w], rawVars);
      modelscores.add(bestscore);

      //ModelScorePlot.plotModelScore(bestscore, vtypes[w].toString(), ridata.getBaseName()+ridata.getResultsFolder()+ "/"+vtypes[w].toString()+iteration+".png");
      double minscore = MathUtils.min(bestscore);
      for(int m=0; m<bestscore.length; m++){	 
	if(minscore == bestscore[m]){
	  modelcutpoints.put(vtypes[w], new Double(bestcutpoint[m]));
	  ArrayList<String> tmpvars = new ArrayList<String>();
	  for(int q=0; q<(m+prevsize+1); q++){
	  
	    tmpvars.add(new String(fixedvars.get(q)));
	  }
	  fixedvars.clear();
	  fixedvars.addAll(tmpvars);
	  fixedvars.trimToSize();
	  prevsize = fixedvars.size();
	  break;

	}
      }

      
      modelvariables.put(vtypes[w], new ArrayList<String>(fixedvars));
      
    }
    long ev = System.currentTimeMillis();
    System.out.println("Selecting Variables: " +(ev-sv));
    //dwrite.close();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }


  /**
   * Get risks based on coefficients and a variable set
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


  /**
   * Given a set of risk values and cutpoint this function makes a prediction about 
   * each person in the test set and quantifies its performance 
   * 
   * @param trainrisks the vector of risk values for the training set
   * @param testrisks the vector of risk values for the test set
   * @param testoutcome the true outcomes of the test set
   * @param cutpoint the percentile cutoff point to use to dichotomize risks
   * @return an EnumMap containing the performance characteristics of the model
   */
  public double[] makePredictions(double[] trainrisks, double[] testrisks, int[] testoutcome, int[] trainoutcome, double cutpoint, double negs, double pos){
    //System.out.println("About to calculate Brier score");
    double criticalValue;
    int [] testPredictions = new int[testrisks.length];
    int [] trainPredictions = new int[trainrisks.length];


    double tp = 0;
    double tn = 0;
    double fn = 0;
    double fp = 0;
    
    double traintp = 0;
    double traintn = 0;
    double trainfn = 0;
    double trainfp = 0;

    double [] s = trainrisks;
    Arrays.sort(s);
    double dposition = s.length*cutpoint;
    int position = (int) dposition;

    criticalValue = s[position];

    for(int i=0; i<testrisks.length; i++){
      if(testrisks[i]<criticalValue){
	testPredictions[i]=0;
      }

      else{
	testPredictions[i]=1;
      }


      if(testoutcome[i]==0 && testPredictions[i]==0){
	//System.out.println("True Negative");
	tn++;
      }
      else if(testoutcome[i]==1 && testPredictions[i]==1){
	//System.out.println("True Positive");
	tp++;
      }
      else if(testoutcome[i]==1 && testPredictions[i]==0){
	//System.out.println("False Negative");
	fn++;
      }
      else if(testoutcome[i]==0 && testPredictions[i]==1){
	//System.out.println("False Positive");
	fp++;
      }
    }

    for(int i=0; i<trainrisks.length; i++){
        if(trainrisks[i]<criticalValue){
        	trainPredictions[i]=0;
        }

        else{
        	trainPredictions[i]=1;
        }


        if(trainoutcome[i]==0 && trainPredictions[i]==0){
  	//System.out.println("True Negative");
        	traintn++;
        }
        else if(trainoutcome[i]==1 && trainPredictions[i]==1){
  	//System.out.println("True Positive");
        	traintp++;
        }
        else if(trainoutcome[i]==1 && trainPredictions[i]==0){
  	//System.out.println("False Negative");
        	trainfn++;
        }
        else if(trainoutcome[i]==0 && trainPredictions[i]==1){
  	//System.out.println("False Positive");
        	trainfp++;
        }
      }

    
    
    double sen = tp/(tp+fn);
    double spe = tn/(tn+fp);
    double trainppv; 
    if(trainfp==0 && traintp==0){
      trainppv = 0;
    }
    else{
      trainppv = traintp/(trainfp+traintp);
    }
    
    
    double trainnpv;
    if(trainfn==0 && traintn==0){
       trainnpv = 0;
    }
    else{
      trainnpv = traintn/(trainfn+traintn);
    }
    //System.out.println("FP:"+trainfp+" FN:"+trainfn+" TN:"+traintn+" TP:"+traintp);
    //System.out.println("PPV:"+trainppv+" NPV:"+trainnpv);
    int n = testrisks.length;
    double score = (n*pos*sen*Math.pow((1-trainppv), 2))+(n*pos*(1-sen)*Math.pow((1-(1-trainnpv)), 2))+(n*negs*spe*Math.pow((0-(1-trainnpv)), 2))+(n*negs*(1-spe)*Math.pow((0-trainppv), 2));
    //System.out.println(score);
    double [] metrics = new double[3];
    metrics[0] = sen;
    metrics[1] = spe;
    metrics[2] = score/n;
    return(metrics);
  }

  /**
   * Uses the model variables to estimate coefficients & cutpoint values in the 
   * bootstrapped optimazation set
   */
  public RiskIndexModel getModel(){



    ArrayList<VARTYPES> v = new ArrayList<VARTYPES>(ridata.getVariables().keySet());

    VARTYPES[] vtypes = new VARTYPES[v.size()];
    v.toArray(vtypes);
    HashMap<String, HashMap<String, Double>> optcoeff = getCoefficients(bootstrappedOptSet, modelvariables);

    EnumMap<VARTYPES, double[]> optSetRisks= new EnumMap<VARTYPES, double[]>(VARTYPES.class);
    
    EnumMap<VARTYPES, Double> cutpoints = new EnumMap<VARTYPES, Double>(VARTYPES.class);
    EnumMap<VARTYPES, Double> percentilecutpoints = new EnumMap<VARTYPES, Double>(VARTYPES.class);
    
    for(int w=0; w<modelvariables.size(); w++){
      double criticalValue;
      double cp = modelcutpoints.get(vtypes[w]).doubleValue();
      double[] trainrisks = new double[bootstrappedOptSet.numInstances()];   
      trainrisks = getRisks(bootstrappedOptSet, modelvariables.get(vtypes[w]), null, optcoeff);
      optSetRisks.put(vtypes[w], trainrisks);
      double [] s = trainrisks;
      Arrays.sort(s);
      double dposition = s.length*cp;
      int position = (int) dposition;
      criticalValue = s[position];
      percentilecutpoints.put(vtypes[w], new Double(cp));
      cutpoints.put(vtypes[w], new Double(criticalValue));
    }
    
 
    
    RiskIndexModel rm = new RiskIndexModel();
    
   
    System.out.println(optcoeff);
    rm.setCoefficients(optcoeff);
    rm.setModelVariables(modelvariables);
    rm.setUntrimmedModelVariables(untrimmedModel);
    rm.setCutpoints(cutpoints);
    rm.setOptSetRisks(optSetRisks);
    rm.setBrierScores(brierScores);
    rm.setPercentileCutpoints(percentilecutpoints);
    return(rm);

  }


  /**
   * The main method of the class - this method is called in parallel for each of the 
   * bootstrapped Optimization Sets by the class RiskIndexRunner
   */
  @JPPFRunnable
  public void run(){
     try{
        
        DataProvider d = getDataProvider();
        ridata = (RiskIndexData) d.getValue("ridata");
        indTestPredictions = new int[ridata.getVariables().size()][ridata.getIndependentTestSet().numInstances()];
        double [] finalScore = new double[ridata.getVariables().size()];
        
        int[][] predictedOutcomes;
        double[] scores = new double[ridata.getVariables().size()];
    
        Instances sample = new Instances(ridata.getOptimizationSet());
        bootstrappedOptSet = sample.resample(new Random());
          
        setCrossValidation();
          
        selectVars();

        System.out.println(ridata.getIndependentTestSet().numInstances());
        
        setResult(getModel());
     }
     catch(Exception e){
       e.printStackTrace();
     }
     
  }



 
  
    
}
