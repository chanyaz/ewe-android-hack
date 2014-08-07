package com.expedia.bookings.utils;

import android.app.Activity;

public class FragmentBailUtils {

	/**
	 * When an activity is finishing, we really wanna make sure our fragments exit
	 * themselves early otherwise a big 'ol mess could ensue.
	 * @param activity
	 * @return
	 */
	public static boolean shouldBail(Activity activity) {
		return activity == null || activity.isFinishing();
	}

}
