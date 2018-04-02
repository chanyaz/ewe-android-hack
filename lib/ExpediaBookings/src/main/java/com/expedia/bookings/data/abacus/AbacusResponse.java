package com.expedia.bookings.data.abacus;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.expedia.bookings.utils.Strings;

public class AbacusResponse {

	private Map<Integer, AbacusTest> abacusTestMap = new HashMap<>();
	private Map<Integer, AbacusTest> abacusTestDebugMap = new HashMap<>();

	public int variateForTest(ABTest abTest) {
		AbacusTest test = testForKey(abTest);
		if (test != null) {
			return test.getBucketVariate();
		}
		return AbacusVariant.CONTROL.getValue();
	}

	// To be removed up once we clean up the MID AB test
	public boolean isMIDUndetermined(ABTest abTest) {
		AbacusTest test = testForKey(abTest);
		if (test == null || test.getBucketVariate() == AbacusVariant.NO_BUCKET.getValue()) {
			return true;
		}
		return false;
	}

	public String getAnalyticsString(ABTest abTest) {
		AbacusTest test = testForKey(abTest);
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
	public void forceUpdateABTest(int key, int value) {
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

	public AbacusTest testForKey(ABTest abTest) {
		if (abacusTestDebugMap.get(abTest.getKey()) != null
			&& abacusTestDebugMap.get(abTest.getKey()).getBucketVariate() != AbacusVariant.DEBUG.getValue()) {
			return abacusTestDebugMap.get(abTest.getKey());
		}
		else {
			return abacusTestMap.get(abTest.getKey());
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

	public void setAbacusTestMap(Map<Integer, AbacusTest> map) {
		abacusTestMap = map;
		abacusTestDebugMap = new HashMap<>();
		for (Map.Entry<Integer, AbacusTest> entry : map.entrySet()) {
			abacusTestDebugMap.put(entry.getKey(), entry.getValue().copyForDebug());
		}
	}

	public Boolean isOverriddenForTest(@NotNull ABTest abTest) {
		AbacusTest abacusTest = abacusTestDebugMap.get(abTest.getKey());
		return abacusTest != null && abacusTest.getBucketVariate() != AbacusVariant.DEBUG.getValue();
	}
}
