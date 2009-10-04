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


import java.util.*;

import org.jppf.client.*;
import org.jppf.server.protocol.JPPFTask;
import org.jppf.task.storage.*;


public class RiskIndexRunner {
  
  
  
  public static void main(String[] args){
    try{
      long s = System.currentTimeMillis();
      RiskIndexConfigReader configReader = new RiskIndexConfigReader();
      RiskIndexData ri = new RiskIndexData();
      
     
      
      ArrayList<RiskIndexModel> predictedModels = new ArrayList<RiskIndexModel>();
      
      if(args == null){
        System.err.println("Error! Must supply configuration filename & path");
        System.exit(-99);
      }
      else{
        ri = configReader.parse(args[0]);
        System.out.println(ri.getVariables().size());
      }
      
      
      	
      	
      
       JPPFClient client = new JPPFClient();
       JPPFJob job = new JPPFJob();
       DataProvider dataProvider = new MemoryMapDataProvider();
       dataProvider.setValue("ridata", ri);
       job.setDataProvider(dataProvider);
       
       for(int i=0; i<ri.getIterations(); i++){
	    job.addTask(new RiskIndexFitter());
       }
	
       List<JPPFTask> results = client.submit(job);
       
	for (int i=0; i<results.size(); i++){
	  if (results.get(i).getException() != null){
	    
	  }
	  else{
	    predictedModels.add((RiskIndexModel) results.get(i).getResult());
	  }
	}
	
      /* Run without the JPPF Framework
	for(int i=0; i<ri.getIterations(); i++){
	  predictedOutcomes.add(new RiskIndexFitter().run(i, ri));
	}
      */
	RiskIndexModel[] rm = new RiskIndexModel[predictedModels.size()];
	predictedModels.toArray(rm);
	
	RiskIndexModelAssessment ma = new RiskIndexModelAssessment(ri, rm);
	
	ma.assessModel();
	
	RiskIndexPerformanceEstimator performance = new RiskIndexPerformanceEstimator(ri, rm);
	performance.estimatePerformance();
	performance.bootstrapPerformanceEstimate(1000);
	
	performance.savePerformance();
	performance.savePredictions();
	performance.saveBootstrapPerformance();
	
	System.out.println(predictedModels.size());
	System.out.println(System.currentTimeMillis() - s);
	System.exit(0);

    }
    catch(Exception e){
	e.printStackTrace();
    }
  
    
  }
  
  
  
}
