package com.expedia.bookings.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.expedia.bookings.utils.KahunaUtils;

/**
 * Created by mohsharma on 4/20/15.
 */
public class ExpediaActivityLifeCycleCallBack implements Application.ActivityLifecycleCallbacks {

	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

	}

	@Override
	public void onActivityStarted(Activity activity) {
		KahunaUtils.startKahunaTracking();
	}

	@Override
	public void onActivityResumed(Activity activity) {

	}

	@Override
	public void onActivityPaused(Activity activity) {

	}

	@Override
	public void onActivityStopped(Activity activity) {
		KahunaUtils.stopKahunaTracking();
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {

	}
}
