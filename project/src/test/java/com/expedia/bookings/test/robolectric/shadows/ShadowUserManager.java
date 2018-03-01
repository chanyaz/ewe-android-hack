package com.expedia.bookings.test.robolectric.shadows;

import android.os.Bundle;
import android.os.UserManager;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(UserManager.class)
public class ShadowUserManager extends org.robolectric.shadows.ShadowUserManager {
	@Implementation
	public Bundle getUserRestrictions() {
		Bundle bundle = new Bundle();
		bundle.putBoolean(UserManager.DISALLOW_MODIFY_ACCOUNTS, false);
		return bundle;
	}
}
