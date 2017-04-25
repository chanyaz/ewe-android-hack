package com.expedia.bookings.test.espresso;


import android.os.Build;

import com.mobiata.android.Log;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;

public class PermissionGranter {

	public static void allowPermission(String permissionNeeded) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			Log.d("Testinggggg grant permission test");
			getInstrumentation().getUiAutomation().executeShellCommand(
				"pm grant " + getTargetContext().getPackageName()
					+ " " + permissionNeeded);
		}
	}
}
