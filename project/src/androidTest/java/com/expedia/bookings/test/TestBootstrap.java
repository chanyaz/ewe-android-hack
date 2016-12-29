package com.expedia.bookings.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.RouterActivity;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public class TestBootstrap extends ActivityInstrumentationTestCase2<RouterActivity> {

	private Activity mActivity;

	public TestBootstrap() {
		super(RouterActivity.class);
	}

	@Before
	public void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
		assertNotNull(mActivity);
	}

	@After
	public void tearDown() throws Exception {
		ActivityFinisher.finishOpenActivities();
		getActivity().finish();
		super.tearDown();
	}

}
