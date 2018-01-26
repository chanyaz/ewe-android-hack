package com.expedia.bookings.data.abacus;

public class AbacusTest {

	public int id;
	public int value;
	public int instanceId;

	public boolean isUserInBucket() {
		return value != 0 && value != AbacusUtils.ABTEST_UNBUCKETED_OR_DEBUG;
	}

	int getBucketVariate() {
		return value;
	}

	/**
	 * This method copies all AbacusTest properties but defaults the bucket to IGNORE
	 * Retains the analytics key to report to Omniture dev instance.
	 */
	AbacusTest copyForDebug() {
		AbacusTest test = new AbacusTest();
		test.id = id;
		test.value = AbacusUtils.ABTEST_UNBUCKETED_OR_DEBUG;
		test.instanceId = instanceId;
		return test;
	}

}
