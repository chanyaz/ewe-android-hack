package com.expedia.bookings.test;

import java.util.Locale;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.activity.RouterActivity;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.tablet.pagemodels.Settings;
import com.expedia.bookings.utils.ExpediaNetUtils;

import cucumber.api.java.After;
import cucumber.api.java.Before;

public class TestBootstrap extends ActivityInstrumentationTestCase2<RouterActivity> {

	private Activity mActivity;

	public TestBootstrap() {
		super(RouterActivity.class);
	}

	//tagged hook to set test environment to Int before running matching scenarios
	@Before("@Int")
	public void setupForTestsOnIntegration() throws Exception {
		Settings.setServer("Integration");
	}

	@Before("@Prod")
	public void setupForTestsOnProduction() throws Exception {
		Settings.setServer("Production");
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
		ExpediaNetUtils.setFake(true, true);

		//clear private data
		Settings.clearPrivateData();

		Settings.setFakeCurrentLocation("0", "0");

		//set US locale and POS
		Common.setLocale(new Locale("en", "US"));
		Common.setPOS(PointOfSaleId.UNITED_STATES);

		Settings.setMockModeEndPoint();
		super.tearDown();
	}

}
