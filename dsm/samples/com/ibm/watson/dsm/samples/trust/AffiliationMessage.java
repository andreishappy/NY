/**
 * 
 */
package com.ibm.watson.dsm.samples.trust;

import com.ibm.watson.dsm.platform.messaging.IMessage;

/**
 * @author dawood
 *
 */
public class AffiliationMessage implements IMessage {
	

	private static final long serialVersionUID = -7852287547990303242L;
	final String newAffiliation;
	
	AffiliationMessage(String affil) {
		newAffiliation = affil;
	}


}
