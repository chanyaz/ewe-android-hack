package com.expedia.bookings.test.robolectric.shadows;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(UserManager.class)
public class ShadowUserManager extends org.robolectric.shadows.ShadowUserManager {
	@Implementation
	@TargetApi(18)
	public Bundle getUserRestrictions() {
		Bundle bundle = new Bundle();
		if (Build.VERSION.SDK_INT >= 18) {
			bundle.putBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false);
		}
		return bundle;
	}
}
