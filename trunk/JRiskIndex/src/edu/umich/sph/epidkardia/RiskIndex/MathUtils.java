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

package edu.umich.sph.epidkardia.RiskIndex;
/**
 * 
 * @author Reagan Kelly
 * 
 * A class that contains static utility methods,  
 * such as mean, std. dev., and rounding.
 *
 */

import java.util.*;
import java.lang.Math;


public class MathUtils {
  
  
  /**
   * A utility function to display an array of String as a single string
   * 
   * @param arr a String[] to display
   * 
   * @return A string showing a space-delimited representation of the String[]
   */
  public static String display(String[] arr){
    String display="";
    for(int ctr=0; ctr<arr.length; ctr++){
      display+=arr[ctr]+" ";
    }
    return(display);
  }
  
  
  /**
   * A utility function that calculates the mean of an ArrayList of Double
   * 
   * @param arr an ArrayList<Double> to take the mean of
   * 
   * @return a double containing the mean of the ArrayList<Double>
   */
  public static double mean(ArrayList<Double> arr){
    double total = 0;
    double size = (double) arr.size();
    double mean;
    
    for(int i=0; i<arr.size(); i++){
      total += arr.get(i);
    }
    
    mean = total/size;
    return(mean);
  }
  
  /**
   * A utility function that calculates the mean of an double[]
   * 
   * @param arr double[] to take the mean of
   * 
   * @return a double containing the mean of the ArrayList<Double>
   */
  public static double mean(double[] arr){
    double total = 0;
    double size = arr.length;
    double mean;

    
    for(int i=0; i<size; i++){
      total += arr[i];
    }
    mean = total/size;
    
    return(mean);
    
  }
  
  
  /**
   * A utility function that calculates the standard deviation of an ArrayList of Double
   * 
   * @param arr an ArrayList<Double> to take the standard deviation of
   * 
   * @return a double containing the standard deviation of the ArrayList<Double>
   */
  public static double sd(ArrayList<Double> arr){
    double total = 0;
    double size = (double) arr.size();
    double mean;
    double diffsum=0;
    double var;
    double sd;
    
    for(int i=0; i<arr.size(); i++){
      total += arr.get(i);
    }
    mean = total/size;
    
    for(int i=0; i<arr.size(); i++){
      diffsum += Math.pow((mean-arr.get(i)), 2);
      
    }
    var = diffsum/(size-1);
    sd = Math.sqrt(var);
    
    return(sd);
    
  }
  
  
  /**
   * A utility function that calculates the standard deviation of a double[]
   * 
   * @param arr a double[] to take the standard deviation of
   * 
   * @return a double containing the standard deviation of the double[]
   */
  public static double sd(double[] arr){
    double total = 0;
    double size = arr.length;
    double mean;
    double diffsum=0;
    double var;
    double sd;
    
    for(int i=0; i<size; i++){
      total += arr[i];
    }
    mean = total/size;
    
    
    for(int i=0; i<size; i++){
      diffsum += Math.pow((mean-arr[i]), 2);
      
    }
    var = diffsum/(size-1);
    sd = Math.sqrt(var);
    
    return(sd);
    
  }
  
  
  /**
   * A utility function that calculates the bias of a double[] from a given value
   * 
   * @param arr a double[] to calculate the bias
   * 
   * @param val a double from which the bias is calculated 
   * 
   * @return a double containing bias of arr from val
   */
  public static double bias(double val, double[] arr){
    double total = 0;
    double bias;
    
    for(int i=0; i<arr.length; i++){
      total += (arr[i]-val);
    }
    
    bias = total/arr.length;
    return(bias);
  }
  
  
  /**
   * A utility function that finds the maximum value of a double[]
   * 
   * @param arr a double[] to find the maximum in
   * 
   * @return a double containing the maximum value in arr
   */
  public static double max(double[] arr){
    double max = Double.NEGATIVE_INFINITY;
    for(int i=0; i<arr.length; i++){
      max = Math.max(arr[i], max);
    }
      return(max);
  
  }
  
  /**
   * A utility function that finds the minimum value of a double[]
   * 
   * @param arr a double[] to find the minimum in
   * 
   * @return a double containing the minimum value in arr
   */
  public static double min(double[] arr){
	    double min = Double.POSITIVE_INFINITY;
	    for(int i=0; i<arr.length; i++){
	      min = Math.min(arr[i], min);
	    }
	      return(min);
	  
	  }
  
  /**
   * A utility function that finds the rounds the number of digits in a double
   * 
   * @param num a double to round
   * 
   * @param numDigits an int giving the number of digits to round num to
   * 
   * @return a double containing the rounded value of num
   */
  public static double roundDigits(double num, int numDigits){
    return Math.round(num * Math.pow(10, numDigits)) / Math.pow(10, numDigits);
  }
  
  
  /**
   * A utility function that finds the rounds the number of digits in a Double
   * 
   * @param num a Double to round
   * 
   * @param numDigits an int giving the number of digits to round num to
   * 
   * @return a Double containing the rounded value of num
   */
  public static Double roundDigits(Double num, int numDigits){
    double dnum = num.doubleValue();
    return new Double(Math.round(dnum * Math.pow(10, numDigits)) / Math.pow(10, numDigits));
  }
  

}
