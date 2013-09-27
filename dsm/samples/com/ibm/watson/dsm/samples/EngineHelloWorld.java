package com.ibm.watson.dsm.samples;

import java.io.IOException;
import java.io.StringReader;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.engine.IEvaluationListener;
import com.ibm.watson.dsm.engine.IRuleEngine;
import com.ibm.watson.dsm.engine.TupleState;
import com.ibm.watson.dsm.engine.app.DSMEngine;
import com.ibm.watson.dsm.engine.parser.dsm.DSMDefinition;
import com.ibm.watson.dsm.engine.parser.dsm.DSMParser;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.tuples.ITuple;
import com.ibm.watson.dsm.platform.tuples.ITupleSet;
import com.ibm.watson.dsm.platform.tuples.Tuple;
/**
 * This samples shows how to use a the rules engine to exchange data between one or more other 
 * engines and how to access the tables on which the engine and rules are operating.  
 * <p>
 * Start any number (greater than 1) instances of this application within 1 or 2 seconds and they
 * will all exchange 'Hello World!' messages with each other.  
 */
public class EngineHelloWorld  {

		
	final static String MSG_TABLE_NAME = "inmsg";
	final static String SAVED_MSG_TABLE_NAME = "savedmsg";
	
	public static class EngineListener implements IEvaluationListener {

		@Override
		public void endEvaluation(IRuleEngine e, TupleState beginState, TupleState endState, Object staticUserData) {
//			System.out.println("End evaluation. Tuple state: " + tupleState);
			System.out.println("End rule evaluation on engine: " + e.getApplicationDescriptor());
		}
		
	}
	final static String rules = 
			  "system peers(char[128] instanceID, char [64] topoRelationship);\n"
			// This table is how the local runtime sends data into the rules to trigger an evaluation
			+ "input inmsg(char[128] msg);\n"
			// This table is used to send data to neighboring peer engines
			+ "transport outmsg(char[128] msg);\n"
			// This tables stores the messages we receive.
			+ "persistent savedmsg(char[128] msg);\n"
			// This rule sends our input messages to all our neighbors
			+ "outmsg(msg)@dest if inmsg(msg), peers(dest,\"Neighbor\");\n"
			// This rule saves any messages we receive.
			+ "savedmsg(msg) if inmsg(msg);\n"
			;

	public static void main(String[] args) throws DSMException, IOException, InterruptedException {
		// Create a descriptor for the name space used by the engine we're creating.  
		IApplicationDescriptor appDesc = new ApplicationDescriptor("EngineHelloWorld");
		
		// Create the rule engine with the rules above.
		DSMDefinition def = DSMParser.parse(new StringReader(rules));
		DSMEngine dsmEngine = new DSMEngine(appDesc,  def);
		
		// Add a listener to get notified when a rule evaluation has completed.
		dsmEngine.addListener(new EngineListener(), null);
		
		// The engine must be started before anything can really be done with it.
		dsmEngine.start();
		
		// Give the underlying registry some time to share its information about all engines.
		System.out.print("Waiting for engines to register...");
		try {Thread.sleep(5000);} catch (InterruptedException e) {	}
		System.out.println("done.");
	
		// Add a hello message tuple to the input message table
		ITuple msg = new Tuple("Hello World! from app " + appDesc);
		dsmEngine.addTuples(MSG_TABLE_NAME, msg);
		
		// Wait for the other engine(s) to come up and send us a message
		Thread.sleep(5000);
		
		// Print out all the tuples we received.
		ITupleSet savedMsgs = dsmEngine.getTuples(SAVED_MSG_TABLE_NAME);
		for (ITuple t : savedMsgs) {
			String recvMsg = t.get(0).getValueAsString();
			System.out.println("Received msg: " + recvMsg);
		}
		
		// Shutdown the engine in an orderly manner.
		dsmEngine.stop();
	}
	

}
