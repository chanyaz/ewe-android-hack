package com.expedia.bookings.data.abacus;

import com.google.gson.annotations.SerializedName;

public class AbacusTest {
	public String name;

	public boolean isLive;
	public String locale;

	@SerializedName("View Settings")
	public UserSetting setting;

	public String experimentId;
	public String instanceId;
	public String treatmentId;

	public boolean isUserInBucket() {
		return getBucketVariate() != 0;
	}

	public int getBucketVariate() {
		if (setting != null) {
			return setting.value;
		}
		return AbacusUtils.DefaultVariate.CONTROL.ordinal();
	}

	/**
	 * This method copies all AbacusTest properties but defaults the bucket to IGNORE
	 * Retains the analytics key to report to Omniture dev instance.
	 */
	public AbacusTest copyForDebug() {
		AbacusTest test = new AbacusTest();
		test.name = name;
		test.isLive = isLive;
		test.locale = locale;
		test.setting = setting.copyForDebug();
		test.experimentId = experimentId;
		test.instanceId = instanceId;
		test.treatmentId = treatmentId;
		return test;
	}

}
