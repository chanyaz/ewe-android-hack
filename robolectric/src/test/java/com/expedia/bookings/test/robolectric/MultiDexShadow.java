package com.expedia.bookings.test.robolectric;

import android.content.Context;
import android.support.multidex.MultiDex;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(MultiDex.class)
public class MultiDexShadow {
	@Implementation
	public static void install(Context context) {
		// Do nothing since with Robolectric nothing is dexed
	}
}
