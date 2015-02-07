package com.expedia.bookings.data.abacus;

import java.util.HashMap;
import java.util.Map;

public class AbacusResponse {

	private Map<String, AbacusTest> abacusTestMap = new HashMap<>();
	private Map<String, AbacusTest> abacusTestDebugMap = new HashMap<>();

	public void setAbacusTestMap(Map<String, AbacusTest> map) {
		abacusTestMap = map;
		abacusTestDebugMap = new HashMap<>();
		for (Map.Entry<String, AbacusTest> entry : map.entrySet()) {
			abacusTestDebugMap.put(entry.getKey(), entry.getValue().copyForDebug());
		}
	}

	public boolean isUserBucketedForTest(String key) {
		AbacusTest test = testForKey(key);
		if (test != null) {
			return test.isUserInBucket() && test.isLive;
		}
		return false;
	}

	public int variateForTest(String key) {
		AbacusTest test = testForKey(key);
		if (test != null) {
			return test.getBucketVariate();
		}
		return AbacusUtils.DefaultVariate.CONTROL.ordinal();
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

	/**
	 * Utility method to update/construct {@link AbacusTest} object for when we are testing using debug settings.
	 * If {@link AbacusTest} is null, meaning Abacus Test isn't created on the intermediate server, create a new one.
	 */
	public void updateABTestForDebug(String key, int value) {
		AbacusTest test = abacusTestDebugMap.get(key);
		if (test == null) {
			test = new AbacusTest();
			test.name = key;
			test.experimentId = "0";
			test.instanceId = "0";
			test.treatmentId = "0";
			test.setting = new UserSetting().copyForDebug();
			abacusTestDebugMap.put(key, test);
		}

		test.setting.value = value;
		test.isLive = true;
	}

	public AbacusTest testForKey(String key) {
		if (abacusTestDebugMap.get(key) != null && abacusTestDebugMap.get(key).getBucketVariate() != AbacusUtils.ABTEST_IGNORE_DEBUG) {
			return abacusTestDebugMap.get(key);
		}
		else {
			return abacusTestMap.get(key);
		}
	}

	public int numberOfTests() {
		return abacusTestMap.size();
	}
}
