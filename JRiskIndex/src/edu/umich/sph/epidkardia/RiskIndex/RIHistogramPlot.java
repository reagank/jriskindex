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


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart; 
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.encoders.*;

import java.awt.Color;
import java.awt.Font;
import java.io.*;


public class RIHistogramPlot { 

  /**
   * A function that creates a histogram from a set of values in a double[] on a fixed x-axis
   * 
   * @param x a double[] containg the values to be plotted into a histogram
   * 
   * @param titleName a String containing the title to print at the top of the graph
   * 
   * @param fileName a String containing the file name the graph will be saved to
   * 
   * @param min a double giving the minimum value for the x-axis
   * 
   * @param max a double giving the maximum value for the x-axis
   * 
   */
  public static void plotHistogramWithFixedRange(double[] x, String titleName, String fileName, double min, double max) { 
   try{
    
    
     HistogramDataset series = new HistogramDataset(); 
    
     series.addSeries("default", x, 15);
     
     
     NumberAxis yaxis = new NumberAxis("Frequency");
     
     NumberAxis xaxis = new NumberAxis("Risk Index Value");
     xaxis.setRange(min, max);
     xaxis.setLabelFont(new Font("Arial", Font.PLAIN, 24));
     xaxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 18));
     
     XYBarRenderer rend = new XYBarRenderer();
     rend.setSeriesPaint(0, Color.black);
     rend.setSeriesVisibleInLegend(0, false);
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
  
  
  /**
   * A function that creates a histogram from a set of values in a double[] on a dynamically sized x-axis
   * 
   * @param x a double[] containg the values to be plotted into a histogram
   * 
   * @param titleName a String containing the title to print at the top of the graph
   * 
   * @param fileName a String containing the file name the graph will be saved to
   * 
   */
  public static void plotHistogram(double[] x, String titleName, String fileName) { 
    try{
     
     
      HistogramDataset series = new HistogramDataset(); 
     
      series.addSeries("default", x, 15);

      JFreeChart chart = ChartFactory.createHistogram(titleName, "Risk Index Values", null, series, PlotOrientation.VERTICAL, false, false, false);
      
      
      FileOutputStream fo = new FileOutputStream(fileName);
      EncoderUtil.writeBufferedImage(chart.createBufferedImage(1280, 1024), ImageFormat.PNG, fo);
      fo.close();

     
     }
    catch(Exception e){
      e.printStackTrace();
    }
   }
  
  
  
} 
