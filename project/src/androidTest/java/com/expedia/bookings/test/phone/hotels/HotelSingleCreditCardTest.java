package com.expedia.bookings.test.phone.hotels;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;
import com.expedia.bookings.test.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.phone.pagemodels.common.TripsScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

public class HotelSingleCreditCardTest extends PhoneTestCase {

	@Override
	public void runTest() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppHotelsABTest,
			AbacusUtils.DefaultVariate.BUCKETED.ordinal());
		super.runTest();
	}

	public void testSingleCardSelected() throws Throwable {
		LaunchScreen.tripsButton().perform(click());
		TripsScreen.clickOnLogInButton();
		HotelScreen.signIn("singlecard@mobiata.com");
		LaunchScreen.shopButton().perform(click());
		LaunchScreen.launchHotels();
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel("happypath");
		HotelScreen.selectRoom();
		onView(withId(R.id.card_info_name)).perform(scrollTo());
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.card_info_name, "Saved Visa 1111");
	}

}
