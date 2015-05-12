package com.expedia.bookings.test.robolectric;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

@Implements(LeakCanary.class)
public class LeakCanaryShadow {

	@Implementation
	public static RefWatcher install(Application application) {
		return null;
	}

}
