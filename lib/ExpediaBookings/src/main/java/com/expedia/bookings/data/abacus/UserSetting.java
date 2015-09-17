package com.expedia.bookings.data.abacus;

import com.google.gson.annotations.SerializedName;

public class UserSetting {
	@SerializedName("Value")
	public int value;

	public UserSetting copyForDebug() {
		UserSetting setting = new UserSetting();
		setting.value = AbacusUtils.ABTEST_IGNORE_DEBUG;
		return setting;
	}

}
