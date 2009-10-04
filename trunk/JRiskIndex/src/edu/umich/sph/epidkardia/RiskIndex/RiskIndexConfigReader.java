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
import java.util.ArrayList;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.*;

public class RiskIndexConfigReader extends DefaultHandler{
  
  private RiskIndexData ri = new RiskIndexData();
  private String currentTag = null;
  private ArrayList<String> vars;
  private String vartype;
  private String maxvars;
  private String beginning = null;  
  
  
  
  /**
   * A function that parses the XML risk index configuration file
   * 
   * @param XMLFile a String containing the full path to the risk index configuration file
   * 
   */
  public RiskIndexData parse(String XMLFile){
    SAXParserFactory factory = SAXParserFactory.newInstance();
    try {
      	SAXParser saxParser = factory.newSAXParser();
      	RiskIndexConfigReader riReader = new RiskIndexConfigReader();
        saxParser.parse(new File(XMLFile), riReader);
        
        System.out.println("Instances: " +riReader.ri.getFullDataset().numInstances());
        
        return(new RiskIndexData(riReader.ri));
    } catch (Throwable t) {
        t.printStackTrace ();
    }

    return(null);
  }
  

  /**
   * A function to identify the start of a new XML tag and use it's attributes to set variables
   */
  public void startElement(String uri, String localName, String qName, Attributes attributes)
  	throws SAXException{
    if(qName.equals("BasePath")){
      System.out.println("Base Path: " + attributes.getValue(uri, "path"));
      ri.setBaseName(attributes.getValue(uri, "path"));
    }
    
    else if(qName.equals("ResultsFolder")){
      System.out.println("Results Folder: " + attributes.getValue(uri, "folder"));
      ri.setResultsFolder(attributes.getValue(uri, "folder"));
    }
    
    else if(qName.equals("InputDataFile")){
      System.out.println("Input File: " + attributes.getValue(uri, "filename"));
      ri.loadData(attributes.getValue(uri, "filename"));
    }
    
    else if(qName.equals("CrossValidationFolds")){
      System.out.println("CV Folds: " + attributes.getValue(uri, "folds"));
      ri.setCrossValidationFolds(new Integer(attributes.getValue(uri, "folds")).intValue());
    }
    
    else if(qName.equals("CrossValidationReps")){
      System.out.println("CV Reps: " + attributes.getValue(uri, "reps"));
      ri.setCrossValidationReps(new Integer(attributes.getValue(uri, "reps")).intValue());
    }
    else if(qName.equals("Iterations")){
      System.out.println("Bootstrap Iterations: " + attributes.getValue(uri, "iterations"));
      ri.setIterations(new Integer(attributes.getValue(uri, "iterations")).intValue());
    }
    
    else if(qName.equals("ClassVariable")){
      System.out.println("Class Variable: " + attributes.getValue(uri, "name"));
      ri.setClass(attributes.getValue(uri, "name"));
    }
    
    else if(qName.equals("IDVariable")){
      System.out.println("ID Variable: " + attributes.getValue(uri, "name"));
      ri.setIDName(attributes.getValue(uri, "name"));
    }
    
    else if(qName.equals("VariableList")){
      System.out.println("Variable Type: " + attributes.getValue(uri, "vartype"));
      System.out.println("Max # of Variables: " + attributes.getValue(uri, "maxvars"));
      vartype = new String(attributes.getValue(uri, "vartype"));
      maxvars = new String(attributes.getValue(uri, "maxvars"));
      vars = new ArrayList<String>();
    }
    
    else if(qName.equals("Variable")){
      currentTag = "Variable";
      
    }
    
  }
  
  /**
   * A function to read character data enclosed by a pair of XML tags
   */
  
  public void characters(char ch[], int start, int length)
  	throws SAXException{
    if(currentTag != null && currentTag.equals("Variable")){
      if(beginning == null){
	beginning = new String(ch, start, length);
      }
      else{
	String tmp = new String(ch, start, length);
	beginning = new String(beginning.concat(tmp));
      }
    }
  }
  
  /**
   * A function to identify the end of an opened XML tag
   */
  public void endElement(String uri, String localName, String qName)
	throws SAXException{
    if(qName.equals("Variable")){
      System.out.println("Variable: " + beginning);
      vars.add(new String(beginning));
      beginning = null;
      currentTag = null;
    }
    if(qName.equals("VariableList")){
      System.out.println("Setting variables");
      ri.setVariables(vartype, vars);
      int max;
      if(maxvars.equals("all")){
	max = vars.size();
      }
      else{
	max = new Integer(maxvars).intValue();
      }
      ri.setMaxVars(vartype, max);
      currentTag = null;
    }
  }

  public void endDocument() throws SAXException{
    ri.setIndependentTestSet();
  }

}



