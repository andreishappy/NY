/*******************************************************************************
 * Copyright IBM Corporation 2013.
 *  
 * GOVERNMENT PURPOSE RIGHTS
 *  
 * Contract No. W911NF-06-3-0002
 * Contractor Name: IBM 
 * Contractor Address:  IBM T. J. Watson Research Center.
 *                      1101 Kitchawan Rd
 *                      Yorktown Heights, NY 10598 
 *  
 *  The Government's rights to use, modify, reproduce, release, perform, display or disclose this software are restricted 
 *  by Article 10 Intellectual Property Rights clause contained in the above identified contract. Any reproductions of the
 *  software or portions thereof marked with this legend must also reproduce the markings.
 *******************************************************************************/
package com.ibm.watson.dsm.samples;

import java.io.IOException;
import java.io.StringReader;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.engine.app.DSMEngine;
import com.ibm.watson.dsm.engine.parser.dsm.DSMDefinition;
import com.ibm.watson.dsm.engine.parser.dsm.DSMParser;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.tuples.ITuple;
import com.ibm.watson.dsm.platform.tuples.ITupleSet;
import com.ibm.watson.dsm.platform.tuples.Tuple;

/**
 * Demonstrates the use of the <i>topology_change</i> table.
 * The topology change table is used to track topology changes in the rule engine.  When a topology change
 * ocurrs (another egine in a distributed set of cooperating engines), the table is populated with this 
 * information and a rule evaluation is triggered.  It is like an <i>input</i> table that gets filled by
 * the rule run-time with the topology status updates.
 * <p>
 * Usage: java com.ibm.watson.dsm.samples.RulePeerChangeExample.class
 * <p>
 * Which should print out something like the following:
 * <pre>
 * Engine 1 peer changes...
 * TupleSet:TupleSetDescriptor: appDesc=RulePeerChangeExample/engine1, name=output1, [ColumnDescriptor: name=NODE, type=String, ColumnDescriptor: name=STATUS, type=String, ColumnDescriptor: name=TOC__INTERNAL, type=Timestamp][
 * Tuple[TupleEntry: value=engine2, TupleEntry: value=added, TupleEntry: value=1970-01-01 12:50:23.345]
 * ]
 * Engine 2 peer changes...
 * TupleSet:TupleSetDescriptor: appDesc=RulePeerChangeExample/engine2, name=output1, [ColumnDescriptor: name=NODE, type=String, ColumnDescriptor: name=STATUS, type=String, ColumnDescriptor: name=TOC__INTERNAL, type=Timestamp][
 * Tuple[TupleEntry: value=engine1, TupleEntry: value=removed, TupleEntry: value=1970-01-01 12:50:24.409]
 * ]
 * </pre>
 * @author dawood
 *
 */
public class RulePeerChangeExample {

	/**
	 * @param args
	 * @throws DSMException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws DSMException, IOException {

			String dsmRules = 
				  "system peer_changes(char[128] node, char[128] status);"
				+ "input clear(char[128] anything);\n"
				+ "input dummy(char[128] anything);\n"
				+ "persistent output1(char[128] node, char[128] status);\n"
				+ "output1(n,r) if peer_changes(n,r);\n"
				+ "output1(n,r) if prev output1(n,r), not clear(*);"
				;
			String AppName = "RulePeerChangeExample";


		
			DSMDefinition def = DSMParser.parse(new StringReader(dsmRules));
			DSMEngine engine1 = new DSMEngine(new ApplicationDescriptor(AppName, "engine1"),  def);
			def = DSMParser.parse(new StringReader(dsmRules));
			DSMEngine engine2 = new DSMEngine(new ApplicationDescriptor(AppName, "engine2"),  def);
			
			engine1.start();
			
			// Trigger an evaluation.  This should not have any peer change info either as the first evaluation, but after
			// this evaluation, any population changes should show up and trigger new evaluations.
			ITuple t1 = new Tuple("any thing");
			engine1.addTuples("dummy", t1);			
			
			// Now, add another engine and expect the peer change to show up for engine1
			engine2.start();
			try { Thread.sleep(1000); } catch (InterruptedException e) { }	
			ITupleSet tset = engine1.getTuples("output1");
			System.out.println("Engine 1 peer changes...\n" + tset);

			// Make sure the 2nd engine has evaluated once, so it can then begin catching peer changes.
			engine2.addTuples("dummy", t1);		
			
			// Now stop engine1 and expect engine 2 to see the delete.
			engine1.stop();
			try { Thread.sleep(1000); } catch (InterruptedException e) { }
			tset = engine2.getTuples("output1");
			System.out.println("Engine 2 peer changes...\n" + tset);
			
			engine2.stop();

		}

}
