package com.ibm.watson.dsm.samples;

import java.util.List;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.beans.IBeanAction;
import com.ibm.watson.dsm.platform.beans.IBeanQueryResult;
import com.ibm.watson.dsm.platform.beans.ISharedBean;
import com.ibm.watson.dsm.platform.beans.ISharedBeanPlatform;
import com.ibm.watson.dsm.platform.beans.SharedBeanPlatform;
import com.ibm.watson.dsm.platform.topology.TopologyRelationship;
import com.ibm.watson.pml.util.CommandArgs;

/**
 * Uses a single shared bean platform to send and receive messages from other similar applications in 
 * the same application name space.
 * 
 * @author dawood
 *
 */
public class BeanHelloWorld2 implements IBeanAction {

	/** Provides the data sharing and messaging services */
	ISharedBeanPlatform platform = null;
	
	/**
	 * Create the instance and assign the given application instance id.
	 * @param instanceid may be null in which case a UUID will be assigned.
	 */
	public BeanHelloWorld2(String instanceid) {
		// Create a descriptor defining a common name space, BeanHelloWorld2, and the optional id for this instance.
		IApplicationDescriptor appDesc = new ApplicationDescriptor("BeanHelloWorld2", instanceid);
		
		// Create the shared bean platform and assign it the given application namespace and id.
		platform = new SharedBeanPlatform(appDesc);
	}
	
	/**
	 * Runs an instance of the application.  
	 * Usage: java com.ibm.watson.dsm.samples.BeanHelloWorld2 [-instance id]
	 * @param args
	 * @throws DSMException 
	 */
	public static void main(String[] args) throws DSMException {
		// Parse the optional -instance argument
		CommandArgs cmdargs = new CommandArgs(args);
		String instanceid = cmdargs.getOption("instance");
		
		// Create an instance of this application and its shared data platform using the give optional instance id.
		BeanHelloWorld2 helloWorld = new BeanHelloWorld2(instanceid);
		
		// Start the shared platform so that it is ready to send/receive messages and shared data.
		helloWorld.platform.start();
		
		// Listen for any shared data from immediate neighbors in the same application name space.
		// Our action() method will be called when new data arrives.
		helloWorld.platform.addRemoteDataListener(TopologyRelationship.Neighbor, helloWorld);
		
		// Periodically send updates to the message. 
		for (int i=0 ; i<10 ; i++) {
			// Create the message we want to send.
			String msg = "hello world #" + i + " from " + helloWorld.platform.getApplicationDescriptor().getInstanceID();
			
			// Share the msg into the shared bean platform, which will cause it to be sent to those listening for it.
			helloWorld.platform.shareBeanData("hello", msg);
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				;
			}
		}
		
		// The shared data platform should be cleanly stopped.
		helloWorld.platform.stop();

	}
	
	/**
	 * Because of our call to addRemoteDataListener() above, this will be called 
	 * when data is received from other applications in this name space. 
	 */
	public void action(List<ISharedBean> localData, List<IBeanQueryResult> remoteData, Object listenerKey) {
		
		System.out.println("\nlocaData=" + localData);
		System.out.println("remoteData" + remoteData);
		
	}

}
