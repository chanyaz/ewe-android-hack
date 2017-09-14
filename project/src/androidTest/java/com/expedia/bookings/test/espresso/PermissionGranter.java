package com.expedia.bookings.test.espresso;


import android.os.Build;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;

public class PermissionGranter {

	public static void allowPermission(String permissionNeeded) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			getInstrumentation().getUiAutomation().executeShellCommand(
				"pm grant " + getTargetContext().getPackageName()
					+ " " + permissionNeeded);
		}
	}
}
