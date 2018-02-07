package com.expedia.bookings.test.phone.hotels;

import org.joda.time.DateTime;
import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelResultsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class HotelDepositsV2Test extends HotelTestCase {

	@Test
	public void testInfoWithDepositRequired() throws Throwable {
		goToResults();
		HotelResultsScreen.selectHotel("hotel_etp_renovation_resort");
		HotelInfoSiteScreen.waitForDetailsLoaded();

		//assert book no pay later info screen
		onView(withId(R.id.etp_info_text)).perform(click());
		Common.delay(1);

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.deposit_terms_first_text,
			"You will be charged deposits by the property based on the following schedule. Any remaining amount will be due upon arrival:");
		onView(allOf(withText("Your deposits will never exceed the total cost of your booking."),
			isDescendantOfA(withId(R.id.pay_later_options))))
			.check(matches(isDisplayed()));
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.etp_pay_later_currency_text, "Pay the hotel directly, in the hotel's local currency (USD).");
		onView(allOf(withText(R.string.etp_pay_later_cancellation_text),
			isDescendantOfA(withId(R.id.pay_later_options))))
			.check(matches(isDisplayed()));
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.no_charges_text, "Expedia will not charge you.");
		Common.pressBack();

		//assert Deposit terms info screen
		HotelInfoSiteScreen.waitForDetailsLoaded();
		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.clickPayLater();
		Common.delay(1);

		onView(withId(R.id.deposit_terms_text_view)).perform(click());

		onView(allOf(withText("You will be charged deposits by the property based on the following schedule. Any remaining amount will be due upon arrival:"),
			isDescendantOfA(hasSibling(withText("Reserve with deposit")))))
			.check(matches(isDisplayed()));
		onView(allOf(withText("Your deposits will never exceed the total cost of your booking."),
			isDescendantOfA(hasSibling(withText("Reserve with deposit")))))
			.check(matches(isDisplayed()));
		onView(allOf(withText("Pay the hotel directly, in the hotel's local currency (USD)."),
			isDescendantOfA(hasSibling(withText("Reserve with deposit")))))
			.check(matches(isDisplayed()));
		onView(allOf(withText("Expedia will not charge you."),
			isDescendantOfA(hasSibling(withText("Reserve with deposit")))))
			.check(matches(isDisplayed()));
		onView(allOf(withText("Free cancellation"),
			isDescendantOfA(hasSibling(withText("Reserve with deposit")))))
			.check(matches(isDisplayed()));
		Common.pressBack();

		HotelInfoSiteScreen.waitForDetailsLoaded();
		HotelInfoSiteScreen.clickPayLater();
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();

		Common.delay(2);

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.amount_due_today_label, "Due to Expedia today");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.total_price_with_tax_and_fees, "$0");

		onView(withId(R.id.amount_due_today_label)).perform(click());

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.price_type_text_view, "Due to Expedia today");
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.price_text_view, "$0");
	}

	@Test
	public void testInfoWithoutDepositNotRequired() throws Throwable {
		goToResults();
		HotelResultsScreen.selectHotel("hotel_etp_renovation_resort_with_free_cancellation");
		HotelInfoSiteScreen.waitForDetailsLoaded();

		//assert book no pay later info screen
		onView(withId(R.id.etp_info_text_small)).perform(click());
		Common.delay(1);

		EspressoUtils.assertViewWithTextIsDisplayed(R.id.etp_pay_later_currency_text,
			"Pay the hotel directly, in the hotel's local currency (USD).");
		onView(allOf(withText(R.string.etp_pay_later_cancellation_text),
			isDescendantOfA(withId(R.id.pay_later_options))))
			.check(matches(isDisplayed()));
		onView(withText(R.string.etp_pay_later_payment_info_text)).check(matches(isDisplayed()));
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.no_charges_text, "Expedia will not charge you.");
		Common.pressBack();

		HotelInfoSiteScreen.waitForDetailsLoaded();
		HotelInfoSiteScreen.clickStickySelectRoom();
		Common.delay(2);
		HotelInfoSiteScreen.clickPayLater();

		//We don't show deposit terms link if there is no deposit required
		onView(withId(R.id.deposit_terms_text_view)).check(matches(not(isDisplayed())));

	}

	private void goToResults() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		SearchScreen.searchEditText().perform(typeText("SFO"));
		SearchScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		SearchScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		SearchScreen.searchButton().perform(click());
	}
}
