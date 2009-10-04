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
import weka.core.*;
import weka.core.converters.*;
import java.io.*;


public class CSVtoARFFConverter {
  
  private Instances data; 
  
  /**
   * Reads in a CSV file and creates Instances from it
   * 
   * @param csvFile should contain a string with the full 
   * path and name of the CSV file to be read
   * 
   */
  public void CSVReader(String csvFile){
    try{
      CSVLoader loader = new CSVLoader();
      loader.setSource(new File(csvFile));
      data = loader.getDataSet();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  /**
   * Write a set of Instances out to an ARFF file
   * 
   * @param arffFile should contain a string with the full 
   * path and name of the arff file to be written
   * 
   */
  public void ARFFWriter(String arffFile)
  	throws java.lang.Exception{
    if(data != null){
      ArffSaver saver = new ArffSaver();
      saver.setInstances(data);
      saver.setFile(new File(arffFile));
      saver.setDestination(new File(arffFile));
      saver.writeBatch();
    }
    else{
      throw new Exception("no file read in");
    }
  }

  
  /**
   * Reads in a CSV file and creates Instances from it
   * 
   * @param csvFile should be a File Object
   * from which the CSV file can be read
   * 
   */
  public void CSVReader(File csvFile){
    try{
      CSVLoader loader = new CSVLoader();
      loader.setSource(csvFile);
      data = loader.getDataSet();
    }
    catch(Exception e){
      e.printStackTrace();
    }
  }
  
  
  /**
   * Write a set of Instances out to an ARFF file
   * 
   * @param arffFile should be a File Object 
   * to which the ARFF File can be written
   * 
   */
  public void ARFFWriter(File arffFile)
  	throws java.lang.Exception{
    if(data != null){
      ArffSaver saver = new ArffSaver();
      saver.setInstances(data);
      saver.setFile(arffFile);
      saver.setDestination(arffFile);
      saver.writeBatch();
    }
    else{
      throw new Exception("no file write out");
    }
  }
  
  
  /**
   * Main method to convert a CSV file to an ARFF File
   * 
   * @param args should contain two strings: the full path and name
   * of the CSV file to be read and the full path and name of the 
   * ARFF file to be written
   * 
   */
  public static void main(String[] args){
    if (args.length != 2) {
      System.out.println("\nUsage: CSVtoARFFConverter <input.csv> <output.arff>\n");
      System.exit(1);
    }
    else{
      CSVtoARFFConverter conv = new CSVtoARFFConverter();
      conv.CSVReader(args[0]);
      try{
	conv.ARFFWriter(args[1]);
      }
      catch(Exception e){
	e.printStackTrace();
      }
    }
  }
 

}
