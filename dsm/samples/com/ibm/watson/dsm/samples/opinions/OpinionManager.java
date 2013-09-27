/**
 * 
 */
package com.ibm.watson.dsm.samples.opinions;

import java.io.IOException;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.DSMLogger;
import com.ibm.watson.dsm.engine.app.DSMEngine;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.topology.application.UnstructuredTopology;
import com.ibm.watson.dsm.platform.tuples.IReadOnlyTupleStorage;
import com.ibm.watson.dsm.platform.tuples.ITupleSet;
import com.ibm.watson.dsm.platform.tuples.Tuple;

/**
 * @author dawood
 *
 */
public class OpinionManager {
	
	final static int ENGINE_COUNT = 3;
	
	public static void main(String[] args)throws IOException, DSMException, InterruptedException {
		DSMEngine[] engines = makeEngines(ENGINE_COUNT, "samples/com/ibm/watson/dsm/samples/opinions/opinions.dsmr");
		IApplicationDescriptor[] engineDesc = new IApplicationDescriptor[ENGINE_COUNT];
		for (int i=0 ; i<ENGINE_COUNT ; i++)
			engineDesc[i] = engines[i].getApplicationDescriptor();
		
		IReadOnlyTupleStorage[] engineStorage = new IReadOnlyTupleStorage[ENGINE_COUNT];
		for (int i=0 ; i<ENGINE_COUNT ; i++)
			engineStorage[i] = engines[i].getReadOnlyTupleStorage();
		
		// Add some engine 0 observations.
		DSMLogger.logger.info("Adding observations");

		
		if (ENGINE_COUNT > 2) {
			engines[0].addTuples("my_observations",
					new Tuple(engines[1].getApplicationDescriptor().getInstanceID(), .8)
					,new Tuple(engines[2].getApplicationDescriptor().getInstanceID(), .2)
					);
			Thread.sleep(2000);
			engines[2].addTuples("my_observations",
					new Tuple(engines[1].getApplicationDescriptor().getInstanceID(), .6)
					);
		} else {
			engines[0].addTuples("my_observations",
					new Tuple(engines[1].getApplicationDescriptor().getInstanceID(), .8)
					);
			
		}
		
		Thread.sleep(2000);
		for (int i=0 ; i<ENGINE_COUNT ; i++) {
			ITupleSet tset = engineStorage[i].getByName(engineDesc[i], "my_opinions");
			DSMLogger.logger.info("Engine " + i + " opinions...\n" + tset);
		}
		
		DSMLogger.logger.info("Stopping engines");
		for (int i=0 ; i<ENGINE_COUNT ; i++)
			engines[i].stop();
	}

	private static DSMEngine[] makeEngines(int count, String rulesFile) throws IOException, DSMException, InterruptedException {
		DSMEngine[] engines = new DSMEngine[count];
		for (int i=0 ; i<count ; i++) {
			IApplicationDescriptor appDesc = new ApplicationDescriptor("opinions", "engine" + i);
			engines[i] = new DSMEngine(appDesc, new UnstructuredTopology(), rulesFile);
			engines[i].start();
		}
		Thread.sleep(2000);
		return engines;
	}

}
