package com.expedia.bookings.test.phone.hotels;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.RecyclerViewAssertions;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

public class HotelResultsPresenterTest extends HotelTestCase {

	public void testSearchResults() throws Throwable {
		// make sure the user is not bucketed for the favorite ab test
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelFavoriteTest, 0);

		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		SearchScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		SearchScreen.searchButton().perform(click());

		// Happy Path : First Item.
		assertViewIsDisplayedAtPosition(2, R.id.hotel_name_text_view);
		assertViewIsDisplayedAtPosition(2, R.id.price_per_night);
		assertViewIsDisplayedAtPosition(2, R.id.background);
		assertViewIsDisplayedAtPosition(2, R.id.star_rating_bar);
		assertViewIsDisplayedAtPosition(2, R.id.guest_rating);
		assertViewNotDisplayedAtPosition(2, R.id.discount_percentage);
		assertViewNotDisplayedAtPosition(2, R.id.strike_through_price);

		//test Top amenities
		assertViewWithTextIsDisplayedAtPosition(2, R.id.top_amenity_title, "Sponsored");
		assertViewWithTextIsDisplayedAtPosition(3, R.id.top_amenity_title, "Free Cancellation");

		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(4));
		assertViewWithTextIsDisplayedAtPosition(4, R.id.top_amenity_title, "Book Now, Pay Later");

		//test VIP message
		assertViewWithTextIsNotDisplayedAtPosition(4, R.id.vip_message, "+VIP");

		//test urgency messages
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(2));
		assertViewWithTextIsDisplayedAtPosition(2, R.id.urgency_message, "4 Rooms Left");
		assertViewWithTextIsDisplayedAtPosition(3, R.id.urgency_message, "Tonight Only!");
		assertViewWithTextIsDisplayedAtPosition(4, R.id.urgency_message, "Mobile Exclusive");
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(5));
		assertViewNotDisplayedAtPosition(5, R.id.urgency_message_layout);

		//test air attach
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(9));
		assertViewIsDisplayedAtPosition(9, R.id.air_attach_layout);
		assertViewWithTextIsDisplayedAtPosition(9, R.id.air_attach_discount, "-12%");
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(10));
		assertViewWithTextIsDisplayedAtPosition(10, R.id.urgency_message, "Sold Out");
	}

	public void testSearchResultWithFavorite() throws Throwable {
		// bucket the user for the favorite ab test
		AbacusTestUtils.bucketTests(AbacusUtils.EBAndroidAppHotelFavoriteTest);
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.edit().putBoolean(getActivity().getResources().getString(R.string.preference_enable_hotel_favorite), true).apply();

		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);

		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		SearchScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		SearchScreen.searchButton().perform(click());

		HotelScreen.waitForResultsLoaded();

		// Happy Path : First Item.
		assertViewIsDisplayedAtPosition(2, R.id.hotel_name_text_view);
		assertViewIsDisplayedAtPosition(2, R.id.price_per_night);
		assertViewIsDisplayedAtPosition(2, R.id.background);
		assertViewIsDisplayedAtPosition(2, R.id.star_rating_bar);
		assertViewIsDisplayedAtPosition(2, R.id.guest_rating);
		assertViewIsDisplayedAtPosition(2, R.id.heart_image_view);
		assertViewNotDisplayedAtPosition(2, R.id.discount_percentage);
		assertViewNotDisplayedAtPosition(2, R.id.strike_through_price);

		//test Top amenities
		assertViewWithTextIsDisplayedAtPosition(2, R.id.top_amenity_title, "Sponsored");
		assertViewWithTextIsDisplayedAtPosition(3, R.id.top_amenity_title, "Free Cancellation");

		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(4));
		assertViewWithTextIsDisplayedAtPosition(4, R.id.top_amenity_title, "Book Now, Pay Later");

		//test VIP message
		assertViewWithTextIsNotDisplayedAtPosition(4, R.id.vip_message, "+VIP");

		//test urgency messages
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(2));
		assertViewWithTextIsDisplayedAtPosition(2, R.id.urgency_message, "4 Rooms Left");
		assertViewWithTextIsDisplayedAtPosition(3, R.id.urgency_message, "Tonight Only!");
		assertViewWithTextIsDisplayedAtPosition(4, R.id.urgency_message, "Mobile Exclusive");
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(5));
		assertViewNotDisplayedAtPosition(5, R.id.urgency_message_layout);

		//test air attach
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(9));
		assertViewIsDisplayedAtPosition(9, R.id.air_attach_layout);
		assertViewWithTextIsDisplayedAtPosition(9, R.id.air_attach_discount, "-12%");
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(10));
		assertViewWithTextIsDisplayedAtPosition(10, R.id.urgency_message, "Sold Out");

		//test fav button
		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().clear().apply();

		//first fav button triggers a dialog
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.scrollToPosition(12));
		HotelScreen.hotelResultsList().perform(RecyclerViewActions.actionOnItemAtPosition(12, clickChildViewWithId(R.id.heart_image_view)));
		HotelScreen.firstTimeFavoriteDialog().check(ViewAssertions.matches(isDisplayed()));
		HotelScreen.firstTimeFavoriteDialogOkButton().perform(click());

	}

	private void assertViewNotDisplayedAtPosition(int position, int id) {
		HotelScreen.hotelResultsList().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), not(isDisplayed())))));
	}

	private void assertViewIsDisplayedAtPosition(int position, int id) {
		HotelScreen.hotelResultsList().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed()))));
	}

	private void assertViewWithTextIsDisplayedAtPosition(int position, int id, String text) {
		HotelScreen.hotelResultsList().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), isDisplayed(), withText(text)))));
	}

	private void assertViewWithTextIsNotDisplayedAtPosition(int position, int id, String text) {
		HotelScreen.hotelResultsList().check(
			RecyclerViewAssertions.assertionOnItemAtPosition(position, hasDescendant(
				CoreMatchers.allOf(withId(id), not(isDisplayed()), withText(text)))));
	}

	private static ViewAction clickChildViewWithId (final int id) {
		return new ViewAction() {
			@Override
			public Matcher<View> getConstraints() {
				return null;
			}

			@Override
			public String getDescription() {
				return "Click on a child view with specified id.";
			}

			@Override
			public void perform(UiController uiController, View view) {
				View v = view.findViewById(id);
				if (v != null) {
					v.performClick();
				}
			}
		};
	}

}
