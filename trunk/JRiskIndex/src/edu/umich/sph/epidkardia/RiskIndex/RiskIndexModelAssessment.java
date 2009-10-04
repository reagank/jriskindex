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

import edu.umich.sph.epidkardia.RiskIndex.RiskIndexData.VARTYPES;
import edu.umich.sph.epidkardia.Util.DelimitedParserFormat;
import edu.umich.sph.epidkardia.Util.DelimitedWriter;


public class RiskIndexModelAssessment {

  private RiskIndexModel [] riModel;
  private RiskIndexData riData;
  
  private ArrayList<VARTYPES> v;
  private VARTYPES[] vtypes;
  
  EnumMap<VARTYPES, Integer> maxVars;
  
  public RiskIndexModelAssessment(RiskIndexData rd, RiskIndexModel [] rm){
    
    riData = new RiskIndexData(rd);
    
    riModel = new RiskIndexModel[rm.length];
    
    for(int i=0; i<rm.length; i++){
      riModel[i] = new RiskIndexModel();
      riModel[i].setCoefficients(rm[i].getCoefficients());
      riModel[i].setCutpoints(rm[i].getCutpoints());
      riModel[i].setModelVariables(rm[i].getModelVariables());
      riModel[i].setBrierScores(rm[i].getBrierScores());
      riModel[i].setPercentileCutpoints(rm[i].getPercentileCutpoints());
      riModel[i].setUntrimmedModelVariables(rm[i].getUntrimmedModelVariables());
      riModel[i].setOptSetRisks(rm[i].getOptSetRisks());
      
      v = new ArrayList<VARTYPES>(riData.getVariables().keySet());
      vtypes = new VARTYPES[v.size()];
      v.toArray(vtypes);
      maxVars = riData.getMaxVariables();
    }
  }
  
  /**
   * A method to create plots of the model score, histograms of risk index values, 
   * and files containing the untrimmed risk index models and model coefficients 
   */
  public void assessModel(){
    
    makeModelPlots();
    makeRIHistograms();
    
    writeUntrimmedModels();
    writeFinalModels();
    
  }

  private void makeRIHistograms(){
    System.out.println("Making histograms");
    for(int w=0; w<vtypes.length; w++){
      double min = new Double(Double.MAX_VALUE).doubleValue();
      double max = new Double(Double.MIN_VALUE).doubleValue();
      for(int b=9; b<riModel.length; b+=10){
	double tmpmax = MathUtils.max(riModel[b].getOptSetRisks().get(vtypes[w]));
	double tmpmin = MathUtils.min(riModel[b].getOptSetRisks().get(vtypes[w]));
	if(tmpmax > max){
	  max = new Double(tmpmax).doubleValue();
	}
	if(tmpmin < min){
	  min = new Double(tmpmin).doubleValue();
	}
      }
      for(int b=9; b<riModel.length; b+=10){
	
	RIHistogramPlot.plotHistogramWithFixedRange(riModel[b].getOptSetRisks().get(vtypes[w]), vtypes[w].toString() + " Risk Index Values Bootstrap "+ new Double(b).toString(), riData.getBaseName() + riData.getResultsFolder()+ vtypes[w].toString()+ "Bootstrap"+new Double(b).toString()+"RiskIndexHistogramFixedRange.png", min, max);
	RIHistogramPlot.plotHistogram(riModel[b].getOptSetRisks().get(vtypes[w]), vtypes[w].toString() + " Risk Index Values Bootstrap "+ new Double(b).toString(), riData.getBaseName() + riData.getResultsFolder()+ vtypes[w].toString()+ "Bootstrap"+new Double(b).toString()+"RiskIndexHistogram.png");
      }
    }
    
  }
  
  
  private void writeFinalModels() {
    for(int w=0; w<vtypes.length; w++){
      String[] indheaderRow = new String[3 + riData.getVariables().get(vtypes[w]).size()];
      indheaderRow[0] = "Bootstrap Sample";
      for(int i=0; i<riData.getVariables().get(vtypes[w]).size(); i++){
	System.out.println(riData.getVariables().get(vtypes[w]).get(i));
	indheaderRow[1+(i)] = riData.getVariables().get(vtypes[w]).get(i);
      }
      
      indheaderRow[riData.getVariables().get(vtypes[w]).size() + 1] = "Percentile Cutoff";
      
      indheaderRow[riData.getVariables().get(vtypes[w]).size() + 2] = "Risk Index Value Cutoff";
      
      
      DelimitedWriter dwrite;
      try {
	
	dwrite = new DelimitedWriter(new File(riData.getBaseName() + riData.getResultsFolder()+ vtypes[w].toString()+ "ModelCoefficients.csv"), DelimitedParserFormat.CSV);
	System.out.println(indheaderRow.length);
        dwrite.writeLine(indheaderRow);
        
        
        
	for(int b=0; b<riModel.length; b++){
	  String[] line = new String[indheaderRow.length];
	  line[0] = new Integer(b+1).toString();
	  for(int m=0; m<riModel[b].getModelVariables().get(vtypes[w]).size(); m++){
	    for(int k=0; k<riData.getVariables().get(vtypes[w]).size(); k++){
	      if(riModel[b].getModelVariables().get(vtypes[w]).get(m).equals(riData.getVariables().get(vtypes[w]).get(k))){
		String var = new String(riModel[b].getModelVariables().get(vtypes[w]).get(m));
		if(riData.getOptimizationSet().attribute(var).isNumeric()){
		  Double co = new Double(riModel[b].getCoefficients().get(var).get("numeric"));
		  line[k+1] = MathUtils.roundDigits(co, 4).toString();
		}
		if(riData.getOptimizationSet().attribute(var).isNominal()){
		  String tmp = new String("");
		  for(int j=1; j<riData.getOptimizationSet().attribute(var).numValues(); j++){
		  	try{
		  	  Double co = new Double(riModel[b].getCoefficients().get(var).get(riData.getOptimizationSet().attribute(var).value(j)));
		  	  tmp = new String(tmp + " "+ riData.getOptimizationSet().attribute(var).value(j) + "=" + MathUtils.roundDigits(co, 4).toString());
		  	}
		  	catch(Exception e){
		  	  e.printStackTrace();
		  	}
		  }
		  line[k+1] = tmp;
		}
		
	      }
	    }
	  }
	  for(int k=0; k<=riData.getVariables().get(vtypes[w]).size(); k++){
	    System.out.println(line[k]);
	    if(line[k]==null){
	      line[k] = new String("");
	      System.out.println("k("+ k + "):" + line[k]);
	    }
	  }
	  line[line.length-1] = new Double(riModel[b].getCutpoints().get(vtypes[w])).toString();
	  line[line.length-2] = new Double(riModel[b].getPercentileCutpoints().get(vtypes[w])).toString();
	  
	  
	  dwrite.writeLine(line);
	}
	
	dwrite.close();
	
      }
      
      catch (IOException e) {
	e.printStackTrace();
      }
      
      
    }
    
  }

  private void writeUntrimmedModels() {
    
    for(int w=0; w<vtypes.length; w++){
        String[] indheaderRow = new String[1 + maxVars.get(vtypes[w])];
        indheaderRow[0] = "Bootstrap Sample";
        for(int i=0; i<maxVars.get(vtypes[w]); i++){
          indheaderRow[1+(i)] = "Variable" + new Integer(i+1).toString();
        }
       
    
        DelimitedWriter dwrite;
        try {
          dwrite = new DelimitedWriter(new File(riData.getBaseName() + riData.getResultsFolder()+ vtypes[w].toString()+ "UntrimmedModels.csv"), DelimitedParserFormat.CSV);
        
          dwrite.writeLine(indheaderRow);
          
          String[] line = new String [maxVars.get(vtypes[w]) + 1];
          
          for(int i=0; i<riModel.length; i++){
            line[0] = new Integer(i+1).toString();
            for(int m=0; m< riModel[i].getUntrimmedModelVariables().get(vtypes[w]).size(); m++){
              line[m+1] = riModel[i].getUntrimmedModelVariables().get(vtypes[w]).get(m);
            }
            
            dwrite.writeLine(line);
            
          }
          

    
          dwrite.close();
        }
        catch (IOException e) {
          e.printStackTrace();
	}
    }
    
    
    
    
  }
  
  private void makeModelPlots(){
    
    System.out.println("Model score plots?");
    for(int w=0; w < vtypes.length; w++){
      double [][] brierScores = new double[riModel.length][maxVars.get(vtypes[w])];
      System.out.println(vtypes[w].toString());
      for(int i=0; i< riModel.length; i++){
	brierScores[i] = riModel[i].getBrierScores().get(vtypes[w]);
      }
      System.out.println("Do I even make it here?");
      ModelScorePlot.plotModelScore(brierScores, vtypes[w].toString(), riData.getBaseName() + riData.getResultsFolder()+ vtypes[w].toString()+ "ModelScores.png");
    }
  }
  
}
