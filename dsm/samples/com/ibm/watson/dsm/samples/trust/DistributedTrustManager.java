package com.ibm.watson.dsm.samples.trust;


import java.util.Iterator;
import java.util.List;

import com.ibm.watson.dsm.DSMException;
import com.ibm.watson.dsm.platform.ApplicationDescriptor;
import com.ibm.watson.dsm.platform.IApplicationDescriptor;
import com.ibm.watson.dsm.platform.beans.BeanStorage;
import com.ibm.watson.dsm.platform.beans.IBeanAction;
import com.ibm.watson.dsm.platform.beans.IBeanQuery;
import com.ibm.watson.dsm.platform.beans.IBeanQueryResult;
import com.ibm.watson.dsm.platform.beans.ISharedBean;
import com.ibm.watson.dsm.platform.beans.ISharedBeanPlatform;
import com.ibm.watson.dsm.platform.beans.NamedBeanQuery;
import com.ibm.watson.dsm.platform.beans.SharedBeanPlatform;
import com.ibm.watson.dsm.platform.messaging.IMessage;
import com.ibm.watson.dsm.platform.messaging.IMessageListener;
import com.ibm.watson.dsm.platform.topology.TopologyRelationship;
import com.ibm.watson.pml.util.CommandArgs;

public class DistributedTrustManager implements IBeanAction, IMessageListener  {
	final static String AppName = "Distributed Trust Manager";
	
	ISharedBeanPlatform sharedBeanPlatform;
	TrustInformation trustInfo = new TrustInformation();
	BeanStorage beanStorage = new BeanStorage();
	
	public DistributedTrustManager(String appName, String instance) {
		sharedBeanPlatform = new SharedBeanPlatform(new ApplicationDescriptor(appName, instance), beanStorage);
		sharedBeanPlatform.addMessageListener(this);
	}

	
	public void start() throws DSMException {
		sharedBeanPlatform.start();
		IBeanQuery query = new NamedBeanQuery(NODE_INFO_BEAN_NAME);
		sharedBeanPlatform.addRemoteDataListener(TopologyRelationship.Neighbor, query, this);
	}

	public void stop() throws DSMException {
		sharedBeanPlatform.stop();
	}

	final static String NODE_INFO_BEAN_NAME  = "NodeInfo";
	
	public static void main(String[] args) throws DSMException {
		CommandArgs cmdargs = new CommandArgs(args);
		String id = cmdargs.getOption("id", null);

		DistributedTrustManager mgr = new DistributedTrustManager(AppName, id);
		
		// Start the  underlying shared bean platform and its related services
		mgr.sharedBeanPlatform.start();

		// Create the data we will be sharing and store in the storage that is used by the platform
		String affil = cmdargs.getOption("affiliation", "US");
		NodeInformation info = new NodeInformation(affil);
		mgr.shareNodeInfo(info);

		// Subscribe to peers NodeInfo data that they are publishing (per the above line of code).
		mgr.sharedBeanPlatform.addRemoteDataListener(TopologyRelationship.Neighbor, mgr);

		//  Now just wait for changs in peers and respond to them via our subscriptions.
		while (true) {
			try { Thread.sleep(2000); } catch (InterruptedException e) { 	}
			mgr.showTrustInfo(affil);
		}
		
//		mgr.sharedBeanPlatform.stop();
	}


	private void shareNodeInfo(NodeInformation nodeInfo) throws DSMException {
		this.sharedBeanPlatform.shareBeanData(NODE_INFO_BEAN_NAME, nodeInfo);
	}

	private void showTrustInfo(String affil) {
		Iterator<String> nodeIter = trustInfo.nodes().iterator();
		System.out.println(affil + " Trust Information");
		while (nodeIter.hasNext()) {
			String node = nodeIter.next();
			double score = trustInfo.getTrust(node);
			System.out.println(node + ": " + score);
		}
	}

	public void action(List<ISharedBean> localData, List<IBeanQueryResult> remoteData, Object listenerKey) {
		if (remoteData.size() == 0)
			return;
		if (remoteData.size() != 1)
			throw new RuntimeException("Got more than one value");
		
		IBeanQueryResult result = remoteData.get(0);
		List<ISharedBean> beanList = result.getResults();
		if (beanList.size() != 1) 
			throw new RuntimeException("Did not get 1 remote bean during action processing");
		ISharedBean remoteBean = beanList.get(0);
		ISharedBean localBean = null;
		if (localData.size() == 1)
			localBean = localData.get(0);
		
		if (remoteBean.getName().equals(NODE_INFO_BEAN_NAME))
			nodeInfoAction(localBean, remoteBean);
		else
			throw new RuntimeException("unexpected bean name: " + remoteBean.getName());
		
	}

	private void nodeInfoAction(ISharedBean localBean, ISharedBean remoteBean) {
		NodeInformation localNodeInfo = (NodeInformation)localBean.getBean();
		NodeInformation remoteNodeInfo = (NodeInformation)remoteBean.getBean();
		String remoteNodeID = remoteBean.getApplication().getInstanceID();
		double newScore;
		String remoteAffiliation = remoteNodeInfo.getAffiliation();
		String localAffiliation = localNodeInfo.getAffiliation();
		
		double existingScore = trustInfo.getTrust(remoteNodeID);

		if (existingScore < 0)
			newScore = getTrustLevel(localAffiliation, remoteAffiliation);
		else
			newScore = existingScore;

		trustInfo.setTrust(remoteNodeID, newScore);

		
	}

	private double getTrustLevel(String localAffiliation, String remoteAffiliation) {
		if (remoteAffiliation.equals(localAffiliation))
			return 1.0;
		if (localAffiliation.equals("US")) {
			if (remoteAffiliation.equals("UK"))
				return .5;
			else if (remoteAffiliation.equals("UY"))
				return .3;
			else
				return .0;
		} else if (localAffiliation.equals("UK")) {
			if (remoteAffiliation.equals("US"))
				return .5;
			else if (remoteAffiliation.equals("UY"))
				return .2;
			else
				return .0;
		}if (localAffiliation.equals("UY")) {
			if (remoteAffiliation.equals("US"))
				return .3;
			else if (remoteAffiliation.equals("UK"))
				return .3;
			else
				return .0;
		}
		return 0;
	}


	public void newMessageArrived(IApplicationDescriptor src, IMessage msg) throws DSMException {
		if (msg instanceof AffiliationMessage) {
			AffiliationMessage amsg = (AffiliationMessage)msg;
			this.shareNodeInfo(new NodeInformation(amsg.newAffiliation));
		}
	}


}
