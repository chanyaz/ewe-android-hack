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
		if (setting != null) {
			return setting.value != 0;
		}
		return false;
	}

}
