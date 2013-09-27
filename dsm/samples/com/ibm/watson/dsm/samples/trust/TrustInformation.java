/**
 * 
 */
package com.ibm.watson.dsm.samples.trust;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author dawood
 *
 */
public class TrustInformation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5485652632555039590L;
	/**
	 * 
	 */
	Map<String, Double> trustScores = new HashMap<String,Double>();
	Map<String, String> affiliationMap = new HashMap<String,String>();
	
	public void setTrust(String nodeName, double trustScore) {
		trustScores.put(nodeName, trustScore);
	}
	
	public void setAffiliation(String nodeName,  String affiliation) {
		affiliationMap.put(nodeName, affiliation);
	}
	
	/**
	 * Get the trust score for the given node.
	 * @param nodeName
	 * @return -1 if we don't have a trust score.
	 */
	public double getTrust(String nodeName) {
		Double v = trustScores.get(nodeName);
		return v == null ? -1 : v.doubleValue();
	}
	
	public Set<String> nodes() {
		return trustScores.keySet();
	}

	/**
	 * @param node
	 * @return
	 */
	public String getAffiliation(String nodeName) {
		String v = affiliationMap.get(nodeName);
		return v;
	}
}
