package com.expedia.bookings.test.tests.flightsEspresso;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

/**
 * Created by dmadan on 4/8/14.
 */
public class HappyPathRunnerE extends ActivityInstrumentationTestCase2<SearchActivity> {
	public HappyPathRunnerE() {
		super(SearchActivity.class);
	}

	private HotelsUserData mUser;
	Context mContext;

	protected void setUp() throws Exception {
		super.setUp();
		mUser = new HotelsUserData(getInstrumentation());
		mContext = getInstrumentation().getTargetContext();
		SettingUtils.save(mContext, R.string.PointOfSaleKey, "28");
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Trunk (Stubbed)");
		getActivity();
	}

	// This test goes through a prototypical flight booking
	// UI flow, up to finally checking out.

	public void testMethod() throws Exception {
		mUser.setAirportsToRandomUSAirports();
		FlightsHappyPathE.execute(mUser);
	}
}
