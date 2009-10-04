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


import org.jfree.chart.JFreeChart; 
import org.jfree.data.xy.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.encoders.*;
import java.awt.Color;
import java.awt.geom.*;
import java.io.*;
import java.awt.*;


public class ModelScorePlot { 

  /**
   * A function to plot the model score from a risk index model
   * 
   * @param y a double[][] containing the values to plot. The first dimension contains 1 array 
   * for each of the risk index models. The second dimension contains 1 value for each variable 
   * in the risk index model
   * 
   * @param titleName a String containing the title to print at the top of the graph
   * 
   * @param fileName a String containing the file name the graph will be saved to
   * 
   * 
   */
  public static void plotModelScore(double[][] y, String titleName, String fileName) { 
     
    try{
    
      double [] x = new double[y[0].length];
      for(int i=0; i<y[0].length; i++){
    	x[i]=i+1;
      }
    
    
    double max = Double.MIN_VALUE;
    
    DefaultXYDataset series = new DefaultXYDataset(); 
    for(int i=0; i<y.length; i++){
      double [][] ser = new double[2][x.length];
      
       Double tmpmax = MathUtils.max(y[i]);
       
       if(tmpmax > max){
	 max = (new Double(tmpmax)).doubleValue();
       }
       
       System.arraycopy(x, 0, ser[0], 0, x.length);
       System.arraycopy(y[i], 0, ser[1], 0, y[i].length);
       
       series.addSeries((new Integer(i)).toString(), ser);
    }
    
    NumberAxis yaxis = new NumberAxis("Model Score");
    yaxis.setTickUnit(new NumberTickUnit(0.02));
    yaxis.setRange(0.1, 0.4);
    
    yaxis.setLabelFont(new Font("Arial", Font.PLAIN, 24));
    yaxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 18));
    
    NumberAxis xaxis = new NumberAxis("Number of Variables");
    xaxis.setTickUnit(new NumberTickUnit(1));
    xaxis.setRange(0.5, x[x.length-1]+0.5);
    xaxis.setLabelFont(new Font("Arial", Font.PLAIN, 24));
    xaxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 18));
    
    XYLineAndShapeRenderer rend = new XYLineAndShapeRenderer();
    for(int i=0; i<y.length; i++){
      rend.setSeriesPaint(i, Color.black);
    }
    rend.setSeriesVisibleInLegend(0, false);
    rend.setSeriesShape(0, new Ellipse2D.Double(-3.0, -3.0, 5.0, 5.0));
    rend.setBaseItemLabelFont(new Font("Arial", Font.PLAIN, 40));
    
    XYPlot p = new XYPlot(series, xaxis, yaxis, rend);
    p.setOrientation(PlotOrientation.VERTICAL);
    p.setRangeGridlinesVisible(false);
    p.setDomainGridlinesVisible(false);
    
    
    JFreeChart chart = new JFreeChart(titleName, new Font("Arial", Font.PLAIN, 24), p, false);
    
    
    FileOutputStream fo = new FileOutputStream(fileName);
    EncoderUtil.writeBufferedImage(chart.createBufferedImage(1280, 1024), ImageFormat.PNG, fo);
    fo.close();
    
    
    
    }
   catch(Exception e){
     e.printStackTrace();
   }
  }
  
  
  
  
} 
