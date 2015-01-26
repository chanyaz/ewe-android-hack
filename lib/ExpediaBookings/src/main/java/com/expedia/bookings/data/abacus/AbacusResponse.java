package com.expedia.bookings.data.abacus;

import java.util.HashMap;
import java.util.Map;

public class AbacusResponse {

	/**
	 * ACTIVE KEYS
	 * <p/>
	 * When new tests need to be added just add a new key to this class
	 * Then call isUserBucketedForTest(String key) to check if the user is
	 * participating in the AB Test.
	 */
	public static final String EBAndroidAATest = "ExpediaAndroidAppAATest";
	//TODO: Update key
	public static final String EBAndroidETPTest = "EBAndroidETPTest";

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

	public AbacusTest testForKey(String key) {
		return abacusTestMap.get(key);
	}

	public int numberOfTests() {
		return abacusTestMap.size();
	}
}
