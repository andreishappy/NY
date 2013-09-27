package com.ibm.watson.dsm.samples;

import java.util.List;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.DSMProperties;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.topology.TopologyRelationship;
import com.ibm.watson.dsm.platform.tuples.ISharedTuplePlatform;
import com.ibm.watson.dsm.platform.tuples.ITupleAction;
import com.ibm.watson.dsm.platform.tuples.ITupleQueryResult;
import com.ibm.watson.dsm.platform.tuples.ITupleSet;
import com.ibm.watson.dsm.platform.tuples.ITupleSetDescriptor;
import com.ibm.watson.dsm.platform.tuples.SharedTuplePlatform;
import com.ibm.watson.dsm.platform.tuples.Tuple;
import com.ibm.watson.dsm.platform.tuples.TupleSet;
import com.ibm.watson.dsm.platform.tuples.storage.JDBCTupleStorage;
/**
 * A simple and fictitious example of using two platforms to exchange messages.
 * This is somewhat fictitious since generally there will be a single shared platform per JVM and here
 * we create two.  We do this to simplify running the example, but this could have also been done 
 * with two instances of a single application (with difference application instance names).  Left
 * as an exercise for the reader :).
 */
public class TupleHelloWorld implements  ITupleAction {
	
	// One instance per application to provide the data sharing services.
	ISharedTuplePlatform sharedTuplePlatform;
	
	// IDataStorage implementations must be able to hold data from multiple distinct applications.
	// Therefore, we can use a single static instance of ITupleStorage for all shared platform instances.
	// Although the platform only needs an ITupleStorage instance, we use the JDBCTupleStorage instance
	// to create the needed table (which ITupleStorage can not do).
	static JDBCTupleStorage tupleStorage = new JDBCTupleStorage(DSMProperties.instance().getProperties());
		
	final static String MSG_TABLE_NAME = "msg";
	
	/**
	 * Create an instance containing a shared tuple platform that has the given instance name.  
	 * The platform is started and  a subscription to all data is created.
	 * @param instanceID
	 * @throws DSMException
	 */
	public TupleHelloWorld(String instanceID) throws DSMException {
		System.out.println("Creating instance " + instanceID);
		
		// Create the application descriptor for this instance of the platform.  Note that both arguments to 
		// this constructor are optional, although in general, you'll want to set the first to define the
		// application name that peers all share.  Sharing the same name puts them in the same application
		// for data sharing space.
		IApplicationDescriptor appDesc = new ApplicationDescriptor("TupleHelloWorld", instanceID);
		
		// Create the shared tuple platform.  All platform instances will use the same ITupleStorage instance.
		sharedTuplePlatform = new SharedTuplePlatform(appDesc, tupleStorage);
		
		// The platform must be started before anything can really be done with it.
		sharedTuplePlatform.start();
		
		// Create the table to store our messages 
		tupleStorage.createStorage(appDesc, MSG_TABLE_NAME, "CREATE TABLE " + MSG_TABLE_NAME + " (text varchar(128))");

		// Create a subscription to ALL data on reachable nodes in this application.  
		// The subscription is to ALL data because neither an ITupleCondition or ITupleQuery is specified 
		// in the subscription creation request.
		sharedTuplePlatform.addRemoteDataListener(TopologyRelationship.Reachable, this);
		
	}

	public static void main(String[] args) throws DSMException {
		// Create/initialize 3 platforms all with different instance ids.
		// Normally there is only one platform/JVM, but here we create them all in the
		// same JVM to simplify this sample code.
		TupleHelloWorld localPlatform = new TupleHelloWorld("local");
		TupleHelloWorld remotePlatform1 = new TupleHelloWorld("remote1");
		TupleHelloWorld remotePlatform2 = new TupleHelloWorld("remote2");
		
		// Give the registry some time to share its information between hw1 and hw2
		System.out.print("Waiting for platforms to register...");
		try {Thread.sleep(5000);} catch (InterruptedException e) {	}
		System.out.println("done.");
	
		// Store a message.
		ITupleSetDescriptor desc = tupleStorage.getDescriptor(localPlatform.sharedTuplePlatform.getApplicationDescriptor(), 
																		MSG_TABLE_NAME);
		ITupleSet tset = new TupleSet(desc);
		tset.add(new Tuple("Hello World!"));
		localPlatform.sharedTuplePlatform.shareData(tset);
		
		// Resaving it overwrites the previous value.
		tset.clear();
		tset.add(new Tuple("Goodbye World!"));
		localPlatform.sharedTuplePlatform.shareData(tset);	
		
		// Now append a value to the MSG table and get notified with both Goodbye messages.
		tset.clear();
		tset.add(new Tuple("Goodbye World! (again)"));
		localPlatform.sharedTuplePlatform.appendTuples(tset);
		
		// Shutdown the platforms in an orderly manner.
		remotePlatform2.sharedTuplePlatform.stop();
		remotePlatform1.sharedTuplePlatform.stop();
		localPlatform.sharedTuplePlatform.stop();
	}
	


	/**
	 * This is the method that is called when we receive subscribed data.
	 */
	public void action(List<ITupleSet> localData, List<ITupleQueryResult> remoteData, Object listenerKey) {
		String appInstance = sharedTuplePlatform.getApplicationDescriptor().getInstanceID();
		StringBuilder sb = new StringBuilder();
		sb.append("Application instance '" + appInstance + "' received action request with the following data...");
		sb.append("\nLocal data list of size " + localData.size());
		if (localData.size() > 0) {
			for (int i=0 ; i<localData.size() ; i++) {
				ITupleSet sharedTuple = localData.get(i);
				sb.append("\nlocalData[" + i + "]=" + sharedTuple);
			}
				
		}
		sb.append("\nReceived results from " + remoteData.size() + " peer(s)");
		if (remoteData.size() > 0) {
			for (int i=0 ; i<remoteData.size() ; i++)
				sb.append("\nPeer '" + remoteData.get(i).getSource() + 
							"' sent the following list of results  " + remoteData.get(i).getResults());
		}
		sb.append("\n");
		System.out.println(sb.toString());
	}





}
