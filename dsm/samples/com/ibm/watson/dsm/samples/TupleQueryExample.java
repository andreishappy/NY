/**
 * 
 */
package com.ibm.watson.dsm.samples;

import java.util.List;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.topology.TopologyRelationship;
import com.ibm.watson.dsm.platform.tuples.ColumnDescriptor;
import com.ibm.watson.dsm.platform.tuples.ISharedTuplePlatform;
import com.ibm.watson.dsm.platform.tuples.ITuple;
import com.ibm.watson.dsm.platform.tuples.ITupleAction;
import com.ibm.watson.dsm.platform.tuples.ITupleEntry;
import com.ibm.watson.dsm.platform.tuples.ITupleQuery;
import com.ibm.watson.dsm.platform.tuples.ITupleQueryResult;
import com.ibm.watson.dsm.platform.tuples.ITupleSet;
import com.ibm.watson.dsm.platform.tuples.ITupleSetDescriptor;
import com.ibm.watson.dsm.platform.tuples.NamedTupleQuery;
import com.ibm.watson.dsm.platform.tuples.SharedTuplePlatform;
import com.ibm.watson.dsm.platform.tuples.Tuple;
import com.ibm.watson.dsm.platform.tuples.TupleEntryType;
import com.ibm.watson.dsm.platform.tuples.TupleSet;
import com.ibm.watson.dsm.platform.tuples.TupleSetDescriptor;

/**
 * Demonstrates how to define and use ITupleQuery objects to request data with specific attributes from
 * remote platforms.  To demonstrate the function, run at least 2 instances of this class's main()
 * in separate JVMs.  
 * 
 * @author dawood
 *
 */
public class TupleQueryExample implements ITupleAction {
	
	private static Object fourListener;
	private static Object oddListener;
	/**
	 * Each main() uses a single shared data platform to share its data with other peers in the same 
	 * application name space.
	 */
	ISharedTuplePlatform platform;
	
	/**
	 * Create the instance and assign the given application instance id.
	 * @param instanceid may be null in which case a UUID will be assigned.
	 */
	public TupleQueryExample() {
		// Create a descriptor defining a common name space, TupleHelloWorld2, and automatically set instance id.
		IApplicationDescriptor appDesc = new ApplicationDescriptor("TupleQueryExample");
		
		// Create the shared Tuple platform and assign it the application namespace and instance id.
		platform = new SharedTuplePlatform(appDesc);
	}
	
	
//	/**
//	 * Defines a simple java object we'll use to share into the sharing platform.
//	 * Note that it must be serializable.
//	 */
//	public static class MySharedTupleData  implements Serializable {
//
//		private static final long serialVersionUID = 8148856369608926635L;
//		int	myInt;
//		
//		/**
//		 * Define the data value within this instance.
//		 * @param theInt
//		 */
//		public MySharedTupleData(int theInt) {
//			myInt = theInt;
//		}
//		
//		public String toString() {
//			return this.getClass().getSimpleName() + " myInt=" + myInt;
//		}
//	}
	
	/**
	 * Extends the super class to override isDataMatch() to compare Tuple data
	 * values with this instance's comparison value.
	 */
	public static class MyTupleValueQuery extends NamedTupleQuery implements ITupleQuery {

		private static final long serialVersionUID = 6775138628762526548L;
		private int value;

		/**
		 * Define the data value we're looking for within instances of MySharedTupleData.
		 * @param i
		 */
		public MyTupleValueQuery(int i) {
			this.value = i;
		}
		
		/**
		 * Define this to compare the given Tuple's first column/entry vs. this instances value.
		 */
		@Override
		public boolean isDataMatch(ITupleSet datum) {
			if (!super.isDataMatch(datum))
				return false;
			if (datum.size() == 0)
				return false;
			ITuple t = datum.get(0);
			if (t.size() != 1)
				return false;
			ITupleEntry e = t.get(0);
			if (e.getType() != TupleEntryType.Integer)
				return false;
			int value = Integer.parseInt(e.getValueAsString());
			
			return value == this.value;
		}
	}
	
	/**
	 * Runs the example to share data with, and query data from, other instances of this main().
	 * No arguments are required to run this main().
	 * @param args
	 * @throws DSMException 
	 */
	public static void main(String[] args) throws DSMException {
		
		// Create an instance of this application and its shared data platform.
		TupleQueryExample tqe = new TupleQueryExample();
		
		IApplicationDescriptor appDesc = tqe.platform.getApplicationDescriptor();
		
		// Start the shared platform so that it is ready to send/receive messages and shared data.
		tqe.platform.start();
		
		// Set up a continuous query to listen for Tuples that are (re)shared with the name 'odd'.
		// Keep track of the key associated with this listener so we can use it in our action() method below.
		ITupleQuery query;
		query = new NamedTupleQuery("odd");
		oddListener = tqe.platform.addRemoteDataListener(TopologyRelationship.Neighbor, query, tqe);
		
		// Set up a continuous query to listen for Tuples whose value is changed/set to 4
		// Again, keep track of the key associated with this listener so we can use it in our action() method below.
		query = new MyTupleValueQuery(4);
		fourListener = tqe.platform.addRemoteDataListener(TopologyRelationship.Neighbor, query, tqe);
		
		// Now begin sharing MySharedTupleData instances into the platform so that other instances of this
		// main() can receive them.   We share 3 different Tuples, using 3 different names.  One Tuple
		// will track all values of our loop value, the other 2 will track the even and odd values.
		// If the other main() is up and running, it should receive calls to the action() method
		// as we go through this loop.
		ITupleSetDescriptor allDesc = new TupleSetDescriptor(tqe.platform.getApplicationDescriptor(), 
											"allval", new ColumnDescriptor("value", TupleEntryType.Integer));
		ITupleSetDescriptor evenDesc = new TupleSetDescriptor(tqe.platform.getApplicationDescriptor(), 
											"even", new ColumnDescriptor("value", TupleEntryType.Integer));
		ITupleSetDescriptor oddDesc = new TupleSetDescriptor(tqe.platform.getApplicationDescriptor(), 
											"odd", new ColumnDescriptor("value", TupleEntryType.Integer));

		tqe.platform.getDataStorage().createStorage(appDesc, "ALLVAL", "CREATE TABLE ALLVAL (value int)");
		tqe.platform.getDataStorage().createStorage(appDesc, "EVEN", "CREATE TABLE EVEN (value int)");
		tqe.platform.getDataStorage().createStorage(appDesc, "ODD", "CREATE TABLE ODD (value int)");
		for (int i=0 ; i< 8 ; i++) {
			ITuple tuple = new Tuple(i);
			ITupleSet tset = new TupleSet(allDesc, tuple);
			tqe.platform.shareData(tset);
			System.out.println("Shared all-values tuple with value " + i);

			if ((i & 1) == 0) {
				tset = new TupleSet(evenDesc, tuple);
				tqe.platform.shareData(tset);
				System.out.println("Shared even tuple with value " + i);
			} else {
				tset = new TupleSet(oddDesc, tuple);
				tqe.platform.shareData(tset);
				System.out.println("Shared odd tuple with value " + i);
			}
			
			try { Thread.sleep(2000); } catch (InterruptedException e) {}
			
		}
		
		// Finally, make a synchronous query for the 'even' Tuple.  The last main() to exit will receive null
		// results since the other platform(s) are not available.  
		List<? extends ITupleQueryResult> results = tqe.platform.queryRemoteData(TopologyRelationship.Neighbor, new NamedTupleQuery("even"));
		System.out.println("synchronous query results=" + results);
		
		// Be a good citizen and shut down the platform cleanly.
		tqe.platform.stop();

	}
	
	/**
	 * Because of our call to addRemoteDataListener() above, this will be called 
	 * when data matching the supplied query is found in other applications using the same name space. 
	 */
	public void action(List<ITupleSet> localData, List<ITupleQueryResult> remoteData, Object listenerKey) {
		if (listenerKey == oddListener) 
			System.out.println("\nOdd listener");
		else if (listenerKey == fourListener) 
			System.out.println("\n4 listener");
		else
			throw new RuntimeException("Huh?");
		
		System.out.println("locaData=" + localData);
		System.out.println("remoteData" + remoteData);
		
	}
}
