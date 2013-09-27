/**
 * 
 */
package com.ibm.watson.dsm.samples;

import java.io.Serializable;
import java.util.List;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.beans.BeanExpressionCondition;
import com.ibm.watson.dsm.platform.beans.IBeanAction;
import com.ibm.watson.dsm.platform.beans.IBeanCondition;
import com.ibm.watson.dsm.platform.beans.IBeanQuery;
import com.ibm.watson.dsm.platform.beans.IBeanQueryResult;
import com.ibm.watson.dsm.platform.beans.ISharedBean;
import com.ibm.watson.dsm.platform.beans.ISharedBeanPlatform;
import com.ibm.watson.dsm.platform.beans.NamedBeanQuery;
import com.ibm.watson.dsm.platform.beans.SharedBeanPlatform;
import com.ibm.watson.dsm.platform.topology.TopologyRelationship;

/**
 * Demonstrates how to define and use IBeanCondition objects to request notification when data 
 * with specific attributes is encountered on remote platforms.  This sample is very similar to
 * the BeanQueryExample class, which you should understand before learning this one.  This example
 * differs in the following ways:
 * <ol>
 * <li> MySharedBeanData now includes a getMyInt() method to allow the expression language to get the value of its myInt field.  See MOD1
 * <li> An implementation of IBeanCondition is provided instead of IBeanQuery (although the implementation uses a NamedBeanQuery). See MOD2 below.
 * <li> Our listeners are installed using our IBeanConditions, instead of IBeanQuerys. See MOD3 below.
 * <li> Only a single bean is stored in the loop and we apply our conditions to this bean. See MOD4 below.
 * <li> Remove the synchronous query after the loop is complete. See MOD5 below.
 * </ol>
 * <p>
 * To demonstrate the function, run at least 2 instances of this class's main()
 * in separate JVMs.  
 * 
 * @author dawood
 *
 */
public class BeanConditionExample implements IBeanAction {
	
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
	public BeanConditionExample() {
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
		
		/**
		 * Allows the bean expression, below, to have access to the myInt value.
		 * @return
		 * MOD1: added to enable the BeanExpressionCondition with an expression that gets the value of myInt.
		 */
		public int getMyInt() {
			return myInt;
		}
	}
	
	/**
	 * Implements a condition that operates on a named bean and tests for its value to 
	 * be odd.
	 * MOD2: Implementation to capture odd values of the named bean.
	 */
	public static class OddBeanCondition  implements IBeanCondition {

		IBeanQuery query ;
		
		public OddBeanCondition(String beanName) {
			query = new NamedBeanQuery(beanName);
		}

		/**
		 * This returns the query that will be evaluated on the remote system to get the data needed by the {@link #isSatisfied(List, List)}
		 * below.  This is a simple implementation that gets the beans by name.  Note that, since this condition does not involve comparisons
		 * with other bean data, the query returned here could have included the test for oddness.  
		 */
		public IBeanQuery getQuery() {
			return query;
		}

		/**
		 * This is a pair-wise condition, meaning it does not need values from multiple peers to evaluate the condition 
		 * in {@link #isSatisfied(List, List)}
		 */
		public boolean isAggregate() {
			return false;
		}

		/**
		 * This implements the test on oddness. Because it is not an aggregate condition the remoteData list will only contain
		 * results from a single peer.  Further, because our query names a single bean, the list of results from the peer
		 * will only contain a single ISharedBean.  Note that we test
		 * the type of data stored in the ISharedBean, however, this is not strictly necessary since we never share any other types
		 * of data in the platform. Its just good form :).
		 */
		public boolean isSatisfied(List<ISharedBean> localData, List<IBeanQueryResult> remoteData) {
			IBeanQueryResult r = remoteData.get(0);
			List<ISharedBean> sbl = r.getResults();
			ISharedBean sb = sbl.get(0);
			Serializable o = sb.getBean();
			if (o instanceof MySharedBeanData) {
				MySharedBeanData data = (MySharedBeanData)o;
				return (data.myInt & 1) == 1;
			}
			return false;
		}
		
	}
	
	final public static String BEAN_NAME = "all-values";
	/**
	 * Runs the example to share data with, and query data from, other instances of this main().
	 * No arguments are required to run this main().
	 * @param args
	 * @throws DSMException 
	 */
	public static void main(String[] args) throws DSMException {
		
		// Create an instance of this application and its shared data platform.
		BeanConditionExample bqe = new BeanConditionExample();
		
		// Start the shared platform so that it is ready to send/receive messages and shared data.
		bqe.platform.start();
		
		// MOD3: Set up a continuous query to listen for when the named bean gets an odd value.
		// Keep track of the key associated with this listener so we can use it in our action() method below.
		IBeanCondition condition;
		condition = new OddBeanCondition(BEAN_NAME);
		oddListener = bqe.platform.addRemoteDataListener(TopologyRelationship.Neighbor, condition, bqe);
		
		// MOD3: Set up a continuous query to listen for beans whose value is changed/set to 4.
		// Again, keep track of the key associated with this listener so we can use it in our action() method below.
		condition  =  new BeanExpressionCondition(" Peer.get(\"" + BEAN_NAME + "\").getMyInt() == 4");
		fourListener = bqe.platform.addRemoteDataListener(TopologyRelationship.Neighbor, condition, bqe);
		
		// Now begin sharing MySharedBeanData instances into the platform so that other instances of this
		// main() can receive them.   We share 3 different beans, using 3 different names.  One bean
		// will track all values of our loop value, the other 2 will track the even and odd values.
		// If the other main() is up and running, it should receive calls to the action() method
		// as we go through this loop.
		for (int i=0 ; i< 8 ; i++) {

			// MOD4: Only store a single bean into the platform (we don't shared "odd" or "even" beans as in the BeanQueryExample).
			MySharedBeanData bd = new MySharedBeanData(i);
			bqe.platform.shareBeanData(BEAN_NAME, bd);
			System.out.println("Shared all-values instance with value " + i);
			
			try { Thread.sleep(2000); } catch (InterruptedException e) {}
			
		}
		
		// MOD5: We don't bother doing a final synchronous query as in the BeanQueryExample.
		
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
