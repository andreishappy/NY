package com.ibm.watson.dsm.samples;

import java.util.List;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.beans.BeanStorage;
import com.ibm.watson.dsm.platform.beans.IBeanAction;
import com.ibm.watson.dsm.platform.beans.IBeanQueryResult;
import com.ibm.watson.dsm.platform.beans.IBeanStorage;
import com.ibm.watson.dsm.platform.beans.ISharedBean;
import com.ibm.watson.dsm.platform.beans.ISharedBeanPlatform;
import com.ibm.watson.dsm.platform.beans.SharedBeanPlatform;
import com.ibm.watson.dsm.platform.topology.TopologyRelationship;

/**
 * A simple and fictitious example of using two platforms to exchange messages.
 * This is somewhat fictitious since generally there will be a single shared platform per JVM and here
 * we create two.  We do this to simplify running the example, but this could have also been done 
 * with two instances of a single application (with difference application instance names).  Left
 * as an exercise for the reader :).
 * 
 * @author dawood
 *
 */
public class BeanHelloWorld implements  IBeanAction {
	
	// One instance per application to provide the data sharing services.
	ISharedBeanPlatform sharedBeanPlatform;
	
	// IDataStorage implementations must be able to hold data from multiple distinct applications.
	// Therefore, we can use a single static instance of BeanStorage for all shared platform instances.
	static IBeanStorage beanStorage = new BeanStorage();

	/**
	 * Create an instance containing a shared bean platform that has the given instance name.  
	 * The platform is started and  a subscription to all data is created.
	 * @param instanceID
	 * @throws DSMException
	 */
	public BeanHelloWorld(String instanceID) throws DSMException {
		System.out.println("Creating instance " + instanceID);
		
		// Create the application descriptor for this instance of the platform.  Note that both arguments to 
		// this constructor are optional, although in general, you'll want to set the first to define the
		// application name that peers all share.  Sharing the same name puts them in the same application
		// for data sharing space.
		IApplicationDescriptor appDesc = new ApplicationDescriptor("BeanHelloWorld", instanceID);
		
		// Create the shared bean platform.  All platform instances will use the same IBeanStorage instance.
		sharedBeanPlatform = new SharedBeanPlatform(appDesc, beanStorage);
		
		
		// The platform must be started before anything can really be done with it.
		sharedBeanPlatform.start();

		// Create a subscription to ALL data on reachable nodes in this application.  
		// The subscription is to ALL data because neither an IBeanCondition or IBeanQuery is specified 
		// in the subscription creation request.
		sharedBeanPlatform.addRemoteDataListener(TopologyRelationship.Reachable, this);
		
	}

	public static void main(String[] args) throws DSMException {
		// Create/initialize 3 platforms all with different instance ids.
		// Normally there is only one platform/JVM, but here we create them all in the
		// same JVM to simplify this sample code.
		BeanHelloWorld localPlatform = new BeanHelloWorld("local");
		BeanHelloWorld remotePlatform1 = new BeanHelloWorld("remote1");
		BeanHelloWorld remotePlatform2 = new BeanHelloWorld("remote2");
		
		// Give the registry some time to share its information platforms
		System.out.print("Waiting for platforms to register...");
		try {Thread.sleep(5000);} catch (InterruptedException e) {	}
		System.out.println("done.");
	
		// Have the 'local' platform store a message with the given name. 
		// It should be sent to the 'remote' platforms and they should receive it.
		localPlatform.sharedBeanPlatform.shareBeanData("msg", "Hello World!");	
		
		// Resaving it overwrites the previous value.
		// Again, 'remote' platforms should receive notification of the new value of the 'msg' bean.
		localPlatform.sharedBeanPlatform.shareBeanData("msg", "Goodbye World!");	
		
		// Shutdown the platforms in an orderly manner.
		remotePlatform2.sharedBeanPlatform.stop();
		remotePlatform1.sharedBeanPlatform.stop();
		localPlatform.sharedBeanPlatform.stop();
	}


	/**
	 * This is the method that is called when we receive subscribed data.
	 */
	public void action(List<ISharedBean> localData, List<IBeanQueryResult> remoteData, Object listenerKey) {
		String appInstance = sharedBeanPlatform.getApplicationDescriptor().getInstanceID();
		StringBuilder sb = new StringBuilder();
		sb.append("Application instance '" + appInstance + "' received action request with the following data...");
		sb.append("\nLocal data list of size " + localData.size());
		if (localData.size() > 0) {
			for (int i=0 ; i<localData.size() ; i++) {
				ISharedBean sharedBean = localData.get(i);
				sb.append("\nlocalData[" + i + "]=" + sharedBean.getBean());
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
