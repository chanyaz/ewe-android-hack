package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.ErrorScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;

public class HotelUnknownCheckoutErrorTest extends HotelTestCase {

	@Test
	public void testUnknownCheckoutError() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("error_checkout_unknown");
		Common.delay(1);
		HotelInfoSiteScreen.bookFirstRoom();
		HotelScreen.checkout(true);

		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForErrorDisplayed();

		ErrorScreen.clickOnRetry();
		onView(withId(R.id.slide_to_purchase_widget)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.slide_to_purchase_widget);
	}
}
