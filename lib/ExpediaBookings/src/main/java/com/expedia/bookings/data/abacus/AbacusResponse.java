package com.expedia.bookings.data.abacus;

import java.util.HashMap;
import java.util.Map;

import com.expedia.bookings.utils.Strings;

public class AbacusResponse {

	private Map<Integer, AbacusTest> abacusTestMap = new HashMap<>();
	private Map<Integer, AbacusTest> abacusTestDebugMap = new HashMap<>();

	public void setAbacusTestMap(Map<Integer, AbacusTest> map) {
		abacusTestMap = map;
		abacusTestDebugMap = new HashMap<>();
		for (Map.Entry<Integer, AbacusTest> entry : map.entrySet()) {
			abacusTestDebugMap.put(entry.getKey(), entry.getValue().copyForDebug());
		}
	}

	public boolean isUserBucketedForTest(int key) {
		AbacusTest test = testForKey(key);
		if (test != null) {
			return test.isUserInBucket();
		}
		return false;
	}

	public int variateForTest(int key) {
		AbacusTest test = testForKey(key);
		if (test != null) {
			return test.getBucketVariate();
		}
		return AbacusUtils.DefaultVariate.CONTROL.ordinal();
	}

	public String getAnalyticsString(int key) {
		AbacusTest test = testForKey(key);
		return AbacusUtils.getAnalyticsString(test);
	}

	/**
	 * Utility method to update/construct {@link AbacusTest} object for when we are testing using debug settings.
	 * If {@link AbacusTest} is null, meaning Abacus Test isn't created on the intermediate server, create a new one.
	 */
	public void updateABTestForDebug(int key, int value) {
		AbacusTest test = abacusTestDebugMap.get(key);
		if (test == null) {
			test = new AbacusTest();
			test.id = key;
			test.value = 0;
			test.instanceId = 0;
			abacusTestDebugMap.put(key, test);
		}

		test.value = value;
	}

	/**
	 * Utility method to force update {@link AbacusTest} object through deeplinks
	 * when we test the release builds.
	 */
	public void updateABTest(int key, int value) {
		AbacusTest test = abacusTestMap.get(key);
		if (test == null) {
			test = new AbacusTest();
			test.id = key;
			test.value = 0;
			test.instanceId = 0;
			abacusTestMap.put(key, test);
		}

		test.value = value;
	}

	public AbacusTest testForKey(int key) {
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

	public int numberOfTestsDebugMap() {
		return abacusTestDebugMap.size();
	}

	public void updateFrom(AbacusResponse otherResponse) {
		abacusTestMap.clear();
		abacusTestDebugMap.clear();
		abacusTestMap.putAll(otherResponse.abacusTestMap);
		abacusTestDebugMap.putAll(otherResponse.abacusTestDebugMap);
	}

	@Override
	public String toString() {
		return Strings.toPrettyString(this);
	}
}
