/**
 * 
 */
package com.ibm.watson.dsm.samples;

import java.io.Serializable;
import java.util.List;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.beans.IBeanAction;
import com.ibm.watson.dsm.platform.beans.IBeanQuery;
import com.ibm.watson.dsm.platform.beans.IBeanQueryResult;
import com.ibm.watson.dsm.platform.beans.ISharedBean;
import com.ibm.watson.dsm.platform.beans.ISharedBeanPlatform;
import com.ibm.watson.dsm.platform.beans.NamedBeanQuery;
import com.ibm.watson.dsm.platform.beans.SharedBeanPlatform;
import com.ibm.watson.dsm.platform.topology.TopologyRelationship;

/**
 * Demonstrates how to define and use IBeanQuery objects to request data with specific attributes from
 * remote platforms.  To demonstrate the function, run at least 2 instances of this class's main()
 * in separate JVMs.  
 * 
 * @author dawood
 *
 */
public class BeanQueryExample implements IBeanAction {
	
	private static Object fourListener;
	private static Object oddListener;
	/**
	 * Each main() uses a single shared data platform to share its data with other peers in the same 
	 * application name space.
	 */
	ISharedBeanPlatform platform;
	
	/**
	 * Create the instance and assign the given application instance id.
	 * @param instanceid may be null in which case a UUID will be assigned.
	 */
	public BeanQueryExample() {
		// Create a descriptor defining a common name space, BeanHelloWorld2, and automatically set instance id.
		IApplicationDescriptor appDesc = new ApplicationDescriptor("BeanQueryExample");
		
		// Create the shared bean platform and assign it the application namespace and instance id.
		platform = new SharedBeanPlatform(appDesc);
	}
	
	
	/**
	 * Defines a simple java object we'll use to share into the sharing platform.
	 * Note that it must be serializable.
	 */
	public static class MySharedBeanData  implements Serializable {

		private static final long serialVersionUID = 8148856369608926635L;
		int	myInt;
		
		/**
		 * Define the data value within this instance.
		 * @param theInt
		 */
		public MySharedBeanData(int theInt) {
			myInt = theInt;
		}
		
		public String toString() {
			return this.getClass().getSimpleName() + " myInt=" + myInt;
		}
	}
	
	/**
	 * Extends the super class to override isDataMatch() to compare bean data
	 * values with this instance's comparison value.
	 */
	public static class MyBeanValueQuery extends NamedBeanQuery implements IBeanQuery {

		private static final long serialVersionUID = 6775138628762526548L;
		private int value;

		/**
		 * Define the data value we're looking for within instances of MySharedBeanData.
		 * @param i
		 */
		public MyBeanValueQuery(int i) {
			this.value = i;
		}
		
		/**
		 * Define this to compare the given beans myInt value vs. this instances value.
		 * Note that we must check the type of java object stored as the bean inside
		 * ISharedBean since the platform can store any type of data as long as it
		 * implements Serializable.
		 */
		@Override
		public boolean isDataMatch(ISharedBean datum) {
			if (!super.isDataMatch(datum))
				return false;
			Object bean = datum.getBean();
			if (bean instanceof MySharedBeanData) {
				MySharedBeanData msbd = (MySharedBeanData)bean;
				if (msbd.myInt == this.value)
					return true;
			}
			return false;
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
		BeanQueryExample bqe = new BeanQueryExample();
		
		// Start the shared platform so that it is ready to send/receive messages and shared data.
		bqe.platform.start();
		
		// Set up a continuous query to listen for beans that are (re)shared with the name 'odd'.
		// Keep track of the key associated with this listener so we can use it in our action() method below.
		IBeanQuery query;
		query = new NamedBeanQuery("odd");
		oddListener = bqe.platform.addRemoteDataListener(TopologyRelationship.Neighbor, query, bqe);
		
		// Set up a continuous query to listen for beans whose value is changed/set to 4
		// Again, keep track of the key associated with this listener so we can use it in our action() method below.
		query = new MyBeanValueQuery(4);
		fourListener = bqe.platform.addRemoteDataListener(TopologyRelationship.Neighbor, query, bqe);
		
		// Now begin sharing MySharedBeanData instances into the platform so that other instances of this
		// main() can receive them.   We share 3 different beans, using 3 different names.  One bean
		// will track all values of our loop value, the other 2 will track the even and odd values.
		// If the other main() is up and running, it should receive calls to the action() method
		// as we go through this loop.
		for (int i=0 ; i< 8 ; i++) {
	
			MySharedBeanData bd = new MySharedBeanData(i);
			bqe.platform.shareBeanData("all-values", bd);
			System.out.println("Shared all-values instance with value " + i);
			
			bd = new MySharedBeanData(i);
			if ((i & 1) == 0) {
				bqe.platform.shareBeanData("even", bd);
				System.out.println("Shared even instance with value " + i);
			} else {
				bqe.platform.shareBeanData("odd", bd);
				System.out.println("Shared odd instance with value " + i);
			}
			
			try { Thread.sleep(2000); } catch (InterruptedException e) {}
			
		}
		
		// Finally, make a synchronous query for the 'even' bean.  The last main() to exit will receive null
		// results since the other platform(s) are not available.  
		List<? extends IBeanQueryResult> results = bqe.platform.queryRemoteData(TopologyRelationship.Neighbor, new NamedBeanQuery("even"));
		System.out.println("synchronous query results=" + results);
		
		// Be a good citizen and shut down the platform cleanly.
		bqe.platform.stop();

	}
	
	/**
	 * Because of our call to addRemoteDataListener() above, this will be called 
	 * when data matching the supplied query is found in other applications using the same name space. 
	 */
	public void action(List<ISharedBean> localData, List<IBeanQueryResult> remoteData, Object listenerKey) {
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
