/**
 * 
 */
package com.ibm.watson.dsm.samples.monitor;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.AbstractSharedDataPlatform;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.monitor.AbstractMonitoringClient;
import com.ibm.watson.dsm.platform.monitor.IClientEventHandler;
import com.ibm.watson.dsm.platform.monitor.IMonitorEvent;
import com.ibm.watson.dsm.platform.monitor.debugger.DebugEventClient;

/**
 * The class demonstrates how to create a monitor for a name space of applications.
 * The primary effort is involved in implementing {@link IClientEventHandler#eventReceived(IApplicationDescriptor, IMonitorEvent)}
 * method.  The monitored application must have been started with the system property defined in
 * {@link AbstractSharedDataPlatform#ENABLE_PLATFORM_MONITORING_PROPERTY} set to true.
 * @author dawood
 *
 */
public class DistributedMonitor implements IClientEventHandler  {
	
	AbstractMonitoringClient clientMonitor;
	DistributedMonitor() {
		clientMonitor = new DebugEventClient(null, this);
	}

	/**
	 * @param args
	 * @throws DSMException 
	 */
	public static void main(String[] args) throws DSMException {
		DistributedMonitor monitor = new DistributedMonitor();
		
		monitor.clientMonitor.start();
		try {
			Thread.sleep(30000000);
		} catch (InterruptedException e) {
			;
		}
		monitor.clientMonitor.stop();
	}

	public void eventReceived(IApplicationDescriptor src, IMonitorEvent event) {
		System.out.println("Bean received from " + src + ", bean is " + event.toString());
		
	}
}
