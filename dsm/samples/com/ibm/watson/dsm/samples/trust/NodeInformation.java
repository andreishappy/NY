/**
 * 
 */
package com.ibm.watson.dsm.samples.trust;

import java.io.Serializable;

/**
 * @author dawood
 *
 */
public class NodeInformation implements Serializable {

	private String affiliation;

	NodeInformation(String affiliation) {
		this.affiliation = affiliation;
	}
	
	public String getAffiliation() {
		return affiliation;
	}
}
