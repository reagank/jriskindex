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



import java.io.*;
import weka.core.*;
import java.util.*;


public class RiskIndexData {
  protected enum VARTYPES {Demographic, Socioeconomic, Behavioral, Clinical, Biochemical, SNPs};
  private EnumMap<VARTYPES, ArrayList<String>> allvariables = new EnumMap<VARTYPES, ArrayList<String>>(VARTYPES.class);
  private EnumMap<VARTYPES, Integer> maximumvariables = new EnumMap<VARTYPES, Integer>(VARTYPES.class);
  
  private Integer nfold;
  private Integer reps;
  private Integer iterations;
  
  private Instances fulldata;
  private Instances optimizationSet;
  private Instances independentTestSet;
  
  //Information about the outcome variable
  private int classidx;
  private String className;
  private String idName;
  private String baseName;
  private String resultsFolder;

  
  public RiskIndexData(){
    
  }
  
  public RiskIndexData(RiskIndexData data) {
    fulldata = new Instances(data.getFullDataset());
    optimizationSet = new Instances(data.getOptimizationSet());
    independentTestSet = new Instances(data.getIndependentTestSet());
    
    allvariables = new EnumMap<VARTYPES, ArrayList<String>>(data.getVariables());
    maximumvariables = new EnumMap<VARTYPES, Integer>(data.getMaxVariables());
    
    setCrossValidationFolds(new Integer(data.getCrossValidationFolds()).intValue());
    setCrossValidationReps(new Integer(data.getCrossValidationReps()).intValue());
    setClass(new String(data.getClassName()));
    setIterations(new Integer(data.getIterations()).intValue());
    setBaseName(data.getBaseName());
    setResultsFolder(data.getResultsFolder());
    setIDName(data.getIDName());
    
  }





  /**
   * Load the data from and ARFF file
   * 
   * @param filename a String containing the full path and name of 
   * the ARFF file from which the data is to be loaded
   */
  public void loadData(String filename){
    try{
      fulldata = new Instances(
	  new BufferedReader(
	      new FileReader(filename)));
    }
    catch(Exception e){
      e.printStackTrace();
    }  
  }

  
  


  /**
   * A method to add the variables of a specific type to use for the risk index
   * 
   * @param vartype the type of variables these are (Demographic, Clinical, etc.)
   * @param vars the name of the vairables to be used
   */
  public void setVariables(VARTYPES vartype, String[] vars){
    ArrayList<String> varlist = new ArrayList<String>(Arrays.asList(vars));
    allvariables.put(vartype, varlist);
  }


  /**
   * A method to add the variables of a specific type to use for the risk index
   * 
   * @param vartype String represeting the type of variables these are (Demographic, Clinical, etc.)
   * @param vars String array representing the name of the variables to be used
   */
  public void setVariables(String vartype, String[] vars){
    ArrayList<String> varlist = new ArrayList<String>(Arrays.asList(vars));
    allvariables.put(VARTYPES.valueOf(VARTYPES.class, vartype), varlist);
  }

  /**
   * A method to add the variables of a specific type to use for the risk index
   * 
   * @param vartype String represeting the type of variables these are (Demographic, Clinical, etc.)
   * @param vars ArrayList representing the name of the variables to be used
   */
  public void setVariables(String vartype, ArrayList<String> vars){

    allvariables.put(VARTYPES.valueOf(VARTYPES.class, vartype), vars);
  }

  /**
   * A method to set the maximum number of variables of a specific type to use for the risk index
   * 
   * @param vartype String represeting the type of variables these are (Demographic, Clinical, etc.)
   * @param max int representing the maximum number of variables to be used
   */
  public void setMaxVars(String vartype, int max){

    maximumvariables.put(VARTYPES.valueOf(VARTYPES.class, vartype), new Integer(max));
  }

  /**
   * A method to divide the data into Optimization Set and Independent Testing Set
   */
  public void setIndependentTestSet(){

    Instances tmpdata = fulldata;
    int indLength = (tmpdata.numInstances()/4)+1;
    int optLength = tmpdata.numInstances()-indLength;

    tmpdata.randomize(new Random());
    independentTestSet = new Instances(tmpdata, 0, indLength);
    optimizationSet = new Instances(tmpdata, indLength, optLength); 
  }
  
  /**
   * A method to return the Independent Testing Set
   * 
   * @return Instances containing the data for the Independent Testing Set
   */
  public Instances getIndependentTestSet(){

    return(new Instances(independentTestSet));
     
  }
  
  /**
   * A method to return the Optimization Set
   * 
   * @return Instances containing the data for the Optimization Set
   */
  public Instances getOptimizationSet(){

    return(new Instances(optimizationSet));
     
  }
  
  /**
   * A method to return the full dataset
   * 
   * @return Instances containing the data for the entire dataset
   */
  public Instances getFullDataset(){

    return(new Instances(fulldata));
     
  }

  /**
   * A method to set the base path for the risk index configuration
   * 
   * @param bs a String representing the full path for the configuration
   */
  public void setBaseName(String bs){
    baseName = new String(bs);
  }

  /**
   * A method to return the base path for the risk index configuration
   * 
   * @return a String representing the base path for the configuration
   */
  public String getBaseName(){
    return(new String(baseName));
  }

  /**
   * A method to set the folder where results will be written
   * 
   * @param rf a String with the name of the folder where results files will be written
   */
  public void setResultsFolder(String rf){
    resultsFolder = new String(rf);
  }

  /**
   * A method to get the folder where results should be written
   * 
   * @return a String with the name of the folder where results should be written
   */
  public String getResultsFolder(){
    return(new String(resultsFolder));
  }
  
  
  /**
   * A method to set the number of cross-validation folds and repititions
   * 
   * @param numfolds the number of cross-validation folds to perform
   * @param repititions the number of times to repeat the CV procedure
   */
  public void setCrossValidationFoldsAndReps(int numfolds, int repititions){
    nfold = new Integer(numfolds);
    reps = new Integer(repititions);
  }

  /**
   * A method to set the number of cross-validation folds
   * 
   * @param numfolds the number of cross-validation folds to perform
   * 
   */
  public void setCrossValidationFolds(int numfolds){
    nfold = new Integer(numfolds);
  }
  
  
  /**
   * A method to get the number for cross-validation folds to be performed
   * 
   * @return an int containing the number of cross-validation folds to be performed
   */
  public int getCrossValidationFolds(){
    if(nfold != null){
      return((new Integer(nfold)).intValue());
    } 
    else
      return(-99);
  }

  /**
   * A method to set the number of cross-validation repititions
   * 
   * @param repititions the number of times to repeat the CV procedure
   */
  public void setCrossValidationReps(int repititions){
    reps = new Integer(repititions);
  }
  
  /**
   * A method to get the number of cross-validation repititions
   * 
   * @returns an int giving the number of times to repeat the CV procedure
   */
  public int getCrossValidationReps(){
    if(reps != null){
      return((new Integer(reps)).intValue());
    } 
    else
      return(-99);
  }
  
  /**
   * A method to set the outcome variable
   * 
   * @param classname String representing the Attribute in the dataset 
   * that will be used as the outcome 
   */
  public void setClass(String classname){
    classidx = fulldata.attribute(classname).index();
    className = classname;
  }
  
  /**
   * A method to get the index of the outcome variable
   * 
   * @return an int giving the index of the outcome variable 
   *  
   */
  public int getClassIndex(){
    return((new Integer(classidx)).intValue());
  }

  /**
   * A method to get the name of the outcome variable
   * 
   * @return a String giving the index of the outcome variable 
   *  
   */
  public String getClassName(){
    return(new String(className));
  }

  /**
   * A method to set the index of the outcome variable
   * 
   * @param cidx an integer giving the index of the outcome variable
   */
  public void setClassIndex(int cidx){
    classidx = new Integer(cidx).intValue();
  }

  /**
   * A method to set the name of the outcome variable
   * 
   * @param cName a String giving the name of the outcome variable
   */
  public void setClassName(String cName){
    className = new String(cName);
  }
  
  /**
   * A method to get the variables to be used to build the risk index model
   * 
   * @return an EnumMap<VARTYPES, ArrayList<String>> containing the variables to be 
   * used to build the risk index model for each of the VARTYPES 
   */
  public EnumMap<VARTYPES, ArrayList<String>> getVariables(){

    return new EnumMap<VARTYPES, ArrayList<String>>(allvariables);
  }

  /**
   * A method to get the maximum number variables to be added to the risk index model
   * 
   * @return an EnumMap<VARTYPES, Integer> containing the maximum bumber of variables to be 
   * added to build the risk index model for each of the VARTYPES 
   */
  public EnumMap<VARTYPES, Integer> getMaxVariables(){

    return new EnumMap<VARTYPES, Integer>(maximumvariables);
  }
  
  /**
   * A method to set the number of cross-validation iterations to be performed
   * 
   * @param index an int giving the number of cross-validation repitions to be performed
   */
  public void setIterations(int index){
    iterations = new Integer(index);
  }
  
  /**
   * A method to get the number of cross-validation iterations to be performed
   *  
   * @return an int giving the number of cross-validation iterations to be performed
   */
  public int getIterations(){
    return(new Integer(iterations).intValue());
  }
  
  /**
   * A method to set the ID variable used to identify individuals in the dataset
   * 
   * @param id a String containing the name of the ID variable
   */
  public void setIDName(String id){
    idName = new String(id);
  }
  
  /**
   * A method to get the name of the ID variable used to identify individuals in the dataset
   * 
   * @return a String containing the name of the ID variable
   */
  public String getIDName(){
    return(new String(idName));
  }
  
  
}
