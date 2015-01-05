package com.expedia.bookings.test.component.cars;

import android.app.Activity;
import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.PlaygroundActivity;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;

public class CarSearchParamsTests extends ActivityInstrumentationTestCase2 {

	public CarSearchParamsTests() {
		super(PlaygroundActivity.class);
	}

	private Activity mActivity;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Context context = getInstrumentation().getTargetContext();
		setActivityIntent(PlaygroundActivity.createIntent(context, R.layout.widget_car_search_params));
		mActivity = getActivity();
	}

	public void testFoo() throws Throwable {
		getInstrumentation().runOnMainSync(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(5000);
				}
				catch (InterruptedException e) {
					// ignore
				}
			}
		});
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		Common.pressBackOutOfApp();
	}
}
