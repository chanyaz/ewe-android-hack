package com.expedia.bookings.data.abacus;

import java.util.HashMap;
import java.util.Map;

public class AbacusResponse {

	private Map<String, AbacusTest> abacusTestMap = new HashMap<>();

	public void setAbacusTestMap(Map<String, AbacusTest> map) {
		abacusTestMap = map;
	}

	public boolean isUserBucketedForTest(String key) {
		AbacusTest test = testForKey(key);
		if (test != null) {
			return test.isUserInBucket() && test.isLive;
		}
		return false;
	}

	public int varianteForTest(String key) {
		AbacusTest test = testForKey(key);
		if (test != null) {
			return test.getBucketVariante();
		}
		return AbacusUtils.Variante.CONTROL.ordinal();
	}

	public boolean isTestLive(String key) {
		AbacusTest test = testForKey(key);
		if (test != null) {
			return test.isLive;
		}
		return false;
	}

	public String getAnalyticsString(String key) {
		String analyticsString = "";
		AbacusTest test = testForKey(key);
		if (test != null) {
			analyticsString = String.format("%s.%s.%s", test.experimentId, test.instanceId, test.treatmentId);
		}
		return analyticsString;
	}

	public static String appendString(String key) {
		if (key == null || key.length() == 0) {
			return "";
		}
		else {
			return String.format("%s|", key);
		}
	}

	public AbacusTest testForKey(String key) {
		return abacusTestMap.get(key);
	}

	public int numberOfTests() {
		return abacusTestMap.size();
	}
}
