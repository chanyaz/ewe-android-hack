package com.expedia.bookings.widget.itin;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.ItinCardDataAdapter;
import com.expedia.bookings.test.robolectric.RobolectricRunner;
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB;
import com.expedia.bookings.utils.AbacusTestUtils;
import com.mobiata.android.util.SettingUtils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricRunner.class)
@Config(sdk = 21, shadows = {ShadowAccountManagerEB.class})
public class ItinAdapterTest {

	private Context getContext() {
		return RuntimeEnvironment.application;
	}
	private ItinCardDataAdapter adapter;

	@Before
	public void before() {
		adapter = new ItinCardDataAdapter(getContext());
	}

	@Test
	public void testReviewPromptOnlyShowsOnce() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		assertTrue(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_user_has_seen_review_prompt, true);
		assertFalse(adapter.showUserReview());
	}

	@Test
	public void testReviewPromptOnlyShowsAgainAfterCleared() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		assertTrue(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_user_has_seen_review_prompt, true);
		assertFalse(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_user_has_seen_review_prompt, false);
		assertTrue(adapter.showUserReview());
	}

	@Test
	public void testReviewPromptDoesNotShowAbacus() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.unbucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		assertFalse(adapter.showUserReview());
	}

	@Test
	public void testReviewPromptShowsAfterThreeMonths() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		assertTrue(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_user_has_seen_review_prompt, true);
		SettingUtils.save(getContext(), R.string.preference_date_last_review_prompt_shown, DateTime.now().getMillis());
		assertFalse(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_date_last_review_prompt_shown, DateTime.now().minusMonths(3).minusDays(1).getMillis());
		assertTrue(adapter.showUserReview());
	}

	@Test
	public void testReviewPromptDoesNotShowBeforeThreeMonths() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		assertTrue(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_user_has_seen_review_prompt, true);
		SettingUtils.save(getContext(), R.string.preference_date_last_review_prompt_shown, DateTime.now().getMillis());
		assertFalse(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_date_last_review_prompt_shown, DateTime.now().minusMonths(2).getMillis());
		assertFalse(adapter.showUserReview());
	}

	@Test
	public void testReviewPromptDoesNotShowThreeMonthsInFuture() {
		SettingUtils.save(getContext(), R.string.preference_user_has_booked_hotel_or_flight, true);
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppTripsUserReviews);
		assertTrue(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_user_has_seen_review_prompt, true);
		SettingUtils.save(getContext(), R.string.preference_date_last_review_prompt_shown, DateTime.now().getMillis());
		assertFalse(adapter.showUserReview());
		SettingUtils.save(getContext(), R.string.preference_date_last_review_prompt_shown, DateTime.now().plusMonths(3).plusDays(1).getMillis());
		assertFalse(adapter.showUserReview());
	}
}
