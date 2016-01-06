package com.expedia.bookings.test.phone.newhotels;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

public class HotelShowExampleNamesTest extends HotelTestCase {

	@Override
	public void runTest() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelShowExampleNamesTest,
			AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		super.runTest();
	}

	public void testExampleNamesHK() throws Throwable {
		Common.setPOS(PointOfSaleId.HONG_KONG);
		goToCheckout();
		onView(allOf(withId(R.id.edit_first_name), withHint(getHint(mRes.getString(R.string.first_name_hk))))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.edit_last_name), withHint(getHint(mRes.getString(R.string.last_name_hk))))).check(matches(isDisplayed()));
	}

	public void testExampleNamesKR() throws Throwable {
		Common.setPOS(PointOfSaleId.SOUTH_KOREA);
		goToCheckout();
		onView(allOf(withId(R.id.edit_first_name), withHint(getHint(mRes.getString(R.string.first_name_kr))))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.edit_last_name), withHint(getHint(mRes.getString(R.string.last_name_kr))))).check(matches(isDisplayed()));
	}

	public void testExampleNamesJP() throws Throwable {
		Common.setPOS(PointOfSaleId.JAPAN);
		goToCheckout();
		onView(allOf(withId(R.id.edit_first_name), withHint(getHint(mRes.getString(R.string.first_name_jp))))).check(matches(isDisplayed()));
		onView(allOf(withId(R.id.edit_last_name), withHint(getHint(mRes.getString(R.string.last_name_jp))))).check(matches(isDisplayed()));
	}

	private void goToCheckout() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		HotelScreen.location().perform(typeText("SFO"));
		HotelScreen.selectLocation("San Francisco, CA (SFO-San Francisco Intl.)");
		HotelScreen.selectDateButton().perform(click());
		HotelScreen.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());

		HotelScreen.searchButton().perform(click());
		Common.delay(1);
		HotelScreen.selectHotel("happypath");
		HotelScreen.waitForDetailsLoaded();
		HotelScreen.clickAddRoom();
		CheckoutViewModel.clickDone();
		CheckoutViewModel.clickDriverInfo();
	}

	private String getHint(String name) {
		return mRes.getString(R.string.hint_example_name_TEMPLATE, name);
	}
}
