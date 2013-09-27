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
 * This sample is part of the tutorial on the DSMEngine and the rule language found in the documentation
 * directory.  It uses a simple rule set to save input messages in a saved message table.  A callback
 * is used to track evaluation and then we read the messages back from the saved message table. 
 */
public class EngineTutorial  {

		
	final static String MSG_TABLE_NAME = "inmsg";
	final static String SAVED_MSG_TABLE_NAME = "savedmsg";

	
	public static class EngineListener implements IEvaluationListener {
		public boolean evaluationComplete = false;
		
		@Override
		public void endEvaluation(IRuleEngine e, TupleState beginState, TupleState endState, Object staticUserData) {
//			System.out.println("End evaluation. Tuple state: " + endState);
			ITupleSet tset = endState.getTuples(SAVED_MSG_TABLE_NAME);
			System.out.println("\n\nEnd evaluation, saved messages...\n" + tset + "\n");
			evaluationComplete = true;
			
//			System.out.println("End rule evaluation on engine: " + e.getApplicationDescriptor());
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
			// This rule keeps messages we've already received.
			+ "savedmsg(msg) if prev savedmsg(msg);\n"
			;

	public static void main(String[] args) throws DSMException, IOException, InterruptedException {
		// Create a descriptor for the name space used by the engine we're creating.  
		IApplicationDescriptor appDesc = new ApplicationDescriptor("EngineTutorial", "tutorial" );
		
		// Create the rule engine with the rules above.
		DSMDefinition def = DSMParser.parse(new StringReader(rules));
		DSMEngine dsmEngine = new DSMEngine(appDesc,  def);
		
		// Add a listener to get notified when a rule evaluation has completed.
		EngineListener listener = new EngineListener();
		dsmEngine.addListener(listener, null);
		
		// The engine must be started before anything can really be done with it.
		dsmEngine.start();
		
		// Give the underlying registry some time to share its information with other engines.
		System.out.print("Waiting for engine to register...");
		Thread.sleep(5000);
		System.out.println("...done waiting.");
	
		// Wait for the other engine(s) to be started at the command line to come up and send us a message
		// Listener will be called during this sleep(), if message is received.
		System.out.print("Waiting for engine to receive tuples (please run 'tuple insert EngineTutorial tutorial inmsg msg=Hello')...");
		while (!listener.evaluationComplete)
			Thread.yield();
		System.out.println("...done waiting for tuples.\n");
		
		// Now we demonstrate sending input tuples from Java
		System.out.println("Now adding tuples programmatically");
		dsmEngine.addTuples(MSG_TABLE_NAME,new Tuple("World!"));
		
		// Print out all the tuples we received.
		ITupleSet savedMsgs = dsmEngine.getTuples(SAVED_MSG_TABLE_NAME);
		for (ITuple t : savedMsgs) {
			String recvMsg = t.get(0).getValueAsString();
			System.out.println("Received msg: " + recvMsg);
		}
		
		// Keep engine running and allow 'tuple read EngineTutorial tutorial read' command to be issued.
		System.out.println("Press enter to exit.");
		System.in.read();
		
		// Shutdown the engine in an orderly manner.
		dsmEngine.stop();
	}
	

}
