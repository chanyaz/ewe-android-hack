package com.expedia.bookings.widget.itin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import android.app.AlertDialog;
import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB;
import com.expedia.bookings.utils.AbacusTestUtils;
import com.mobiata.android.util.SettingUtils;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricRunner.class)
@Config(sdk = 21, shadows = {ShadowAccountManagerEB.class})
public class ItinListFragmentTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}
	private ItinItemListFragment listFragment;

	@Before
	public void before() {
		listFragment = new ItinItemListFragment();
		SupportFragmentTestUtil.startFragment(listFragment);
	}

	@Test
	public void testReviewPromptOnlyShowsOnce() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		listFragment.showUserReview();

		AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
		assertEquals(true, alertDialog.isShowing());
	}
}
