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
		return AbacusUtils.Variate.CONTROL.ordinal();
	}

	/**
	 * This method copies all AbacusTest properties but defaults the bucket to IGNORE
	 * It also sets the analytics key to 0.0.0 for omniture purposes.
	 */
	public AbacusTest copyForDebug() {
		AbacusTest test = new AbacusTest();
		test.name = name;
		test.isLive = isLive;
		test.locale = locale;
		test.setting = setting.copyForDebug();
		test.experimentId = "0";
		test.instanceId = "0";
		test.treatmentId = "0";
		return test;
	}

}
