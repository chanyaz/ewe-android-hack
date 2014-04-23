package com.expedia.bookings.test.tests.hotelsEspresso;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.utils.ClearPrivateDataUtil;
import com.mobiata.android.util.SettingUtils;

/**
 * Created by dmadan on 4/11/14.
 */
public class HappyPathRunnerE extends ActivityInstrumentationTestCase2<SearchActivity> {

	public HappyPathRunnerE() {
		super(SearchActivity.class);
	}

	private HotelsUserData mUser;
	SearchActivity mActivity;
	Context mContext;

	protected void setUp() throws Exception {
		super.setUp();
		mUser = new HotelsUserData(getInstrumentation());
		mUser.setHotelCityToRandomUSCity();
		mContext = getInstrumentation().getTargetContext();
		SettingUtils.save(mContext, R.string.PointOfSaleKey, "28");
		// Disable v2 automatically.
		SettingUtils.save(mContext, "preference_disable_domain_v2_hotel_search", true);
		ClearPrivateDataUtil.clear(mContext);
		SettingUtils.save(mContext, R.string.preference_which_api_to_use_key, "Trunk (Stubbed)");
		mActivity = getActivity();
	}

	// This test goes through a prototypical hotel booking
	// UI flow, through check out.
	// It runs pulling from the Integration API

	public void testMethod() throws Exception {
		HotelsHappyPathE.execute(mUser, mActivity);
	}
}
