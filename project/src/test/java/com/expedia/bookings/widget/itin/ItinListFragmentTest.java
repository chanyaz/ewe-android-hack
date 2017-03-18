package com.expedia.bookings.widget.itin;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import org.robolectric.shadows.support.v4.SupportFragmentTestUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ItineraryGuestAddActivity;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.fragment.ItinItemListFragment;
import com.expedia.bookings.itin.activity.NewAddGuestItinActivity;
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
		listFragment.showUserReview();

		AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
		assertEquals(true, alertDialog.isShowing());
	}

	@Test
	public void testReviewPromptText() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		listFragment.showUserReview();

		AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
		assertEquals(true, alertDialog.isShowing());
		assertEquals("How was your experience?", getDialogText(alertDialog, R.id.title_text));
		assertEquals("Review", getDialogText(alertDialog, R.id.review_btn));
		assertEquals("Send Feedback", getDialogText(alertDialog, R.id.feedback_btn));
		assertEquals("No, thanks", getDialogText(alertDialog, R.id.no_btn));
	}

	@Test
	public void testReviewPromptTextBucketed() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		listFragment.showUserReview();

		AlertDialog alertDialog = ShadowAlertDialog.getLatestAlertDialog();
		assertEquals(true, alertDialog.isShowing());
		assertEquals("Love Our App?", getDialogText(alertDialog, R.id.title_text));
		assertEquals("Rate App", getDialogText(alertDialog, R.id.review_btn));
		assertEquals("Email App Support", getDialogText(alertDialog, R.id.feedback_btn));
		assertEquals("No Thanks", getDialogText(alertDialog, R.id.no_btn));
	}

	@NonNull
	private String getDialogText(AlertDialog alertDialog, int id) {
		return ((TextView) alertDialog.findViewById(id)).getText().toString();
	}

	@Test
	public void testShowCorrectAddGuestItinActivity() {
		Activity activity = Robolectric.buildActivity(Activity.class).create().get();

		listFragment.showAddGuestItinScreen();
		Intent startedIntent = Shadows.shadowOf(activity).getNextStartedActivity();
		assertIntentForActivity(ItineraryGuestAddActivity.class, startedIntent);

		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsNewSignInPage);

		listFragment.showAddGuestItinScreen();
		startedIntent = Shadows.shadowOf(activity).getNextStartedActivity();
		assertIntentForActivity(NewAddGuestItinActivity.class, startedIntent);
	}

	private void assertIntentForActivity(Class expectedActivityClass, Intent startedIntent) {
		assertEquals(expectedActivityClass.getName(), startedIntent.getComponent().getClassName());
	}
}
