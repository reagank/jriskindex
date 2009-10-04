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

import edu.umich.sph.epidkardia.RiskIndex.RiskIndexData.VARTYPES;

import java.util.*;

public class RiskIndexPredictions {

 private EnumMap<VARTYPES, Integer> predictions = new EnumMap<VARTYPES, Integer>(VARTYPES.class);
 private EnumMap<VARTYPES, Double> confidence = new EnumMap<VARTYPES, Double>(VARTYPES.class);
 private String id = new String();
 private int outcome;
 
 /**
  * A method to set the outcome for an individual
  * 
  * @param o an int representing an individual's outcome (0, 1)
  */
 public void setOutcome(int o){
   outcome = new Integer(o).intValue();
 }
 
 /**
  * A method to get the outcome for an individual
  * 
  * @return an int representing an individual's outcome (0, 1)
  */
 public int getOutcome(){
   return(new Integer(outcome).intValue());
 }
 
 /**
  * A method to set the id variable for a given individual
  * 
  * @param i a String representing an indivudal's ID variable
  */
 public void setID(String i){
   id = new String(i);
 }
 
 /**
  * A method to get the ID for the individual
  * 
  * @return a String representing the individual's ID variable
  */
 public String getID(){
   return(new String(id));
 }
 
 /**
  * A method to return all predictions for the individual
  * 
  * @return an EnumMap<VARTYPES, Integer> giving the predition for all of the VARTYPES
  */
 public EnumMap<VARTYPES, Integer> getPredictions(){
   return(new EnumMap<VARTYPES, Integer>(predictions));
 }
 
 /**
  * A method to return all prediction confidences for the individual
  * 
  * @return an EnumMap<VARTYPES, Double> giving the confidences for all of the 
  * VARTYPES used in prediction
  */
 public EnumMap<VARTYPES, Double> getConfidences(){
   return(new EnumMap<VARTYPES, Double>(confidence));
 }
 
 /**
  * A method to add a prediction for a given VARTYPE
  * 
  * @param vtype a String giving the name of the VARTYPE the prediction is associated with
  * @param pred an int representing the prediction for the VARTYPE (0, 1)
  */
 public void addPrediction(String name, int pred){
   predictions.put(VARTYPES.valueOf(VARTYPES.class, name), new Integer(pred));
 }
 
 /**
  * A method to add a prediction for a given VARTYPE
  * 
  * @param vtype the VARTYPE the prediction is associated with
  * @param pred an int representing the prediction for the VARTYPE (0, 1)
  */
 public void addPrediction(VARTYPES vtype, int pred){
   predictions.put(vtype, new Integer(pred));
 }
 
 /**
  * A method to add a prediction confidence to an indivdual's risk prediction for a given VARTYPE
  * 
  * @param vtype a String giving the name of the VARTYPE of the prediction the confidence is associated with
  * @param conf a double representing the confidence in an individual's prediction for VARTYPE
  */
 public void addConfidence(String name, double conf){
   confidence.put(VARTYPES.valueOf(VARTYPES.class, name), new Double(conf));
 }
 
 /**
  * A method to add a prediction confidence to an indivdual's risk prediction for a given VARTYPE
  * 
  * @param vtype the VARTYPE of the prediction the confidence is associated with
  * @param conf a double representing the confidence in an individual's prediction for VARTYPE
  */
 public void addConfidence(VARTYPES vtype, double conf){
   confidence.put(vtype, new Double(conf));
 }
 
 /**
  * A method to return the prediction for an indivdual for a particular VARTYPE 
  * 
  * @param vtype the VARTYPE to return the prediction for
  * @return an int representing an indivdual's prediction (0 or 1) for the given VARTYPE
  */
 public int getPrediction(VARTYPES vtype){
   return(new Integer(predictions.get(vtype)).intValue());
 }
 
 /**
  * A method to return the prediction confidence for an indivdual's risk prediction
  * for a particular VARTYPE
  * 
  * @param vtype the VARTYPE to get the prediction confidence for
  * @return a double represnting the confidence in the prediction for the VARTYPE
  */
 public double getConfidence(VARTYPES vtype){
   return(new Double(confidence.get(vtype)).doubleValue());
 }
 
  
}
