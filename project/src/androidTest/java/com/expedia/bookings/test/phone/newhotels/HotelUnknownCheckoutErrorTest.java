package com.expedia.bookings.test.phone.newhotels;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.phone.newhotels.HotelScreen.doGenericSearch;

public class HotelUnknownCheckoutErrorTest extends HotelTestCase {
	public void testUnknownCheckoutError() throws Throwable {
		doGenericSearch();
		HotelScreen.selectHotel("error_checkout_unknown");
		Common.delay(1);
		HotelScreen.selectRoom();
		HotelScreen.checkout(true);

		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		HotelScreen.waitForErrorDisplayed();

		ErrorScreen.clickOnRetry();
		onView(withId(R.id.slide_to_purchase_widget)).perform(ViewActions.waitForViewToDisplay());
		assertViewIsDisplayed(R.id.slide_to_purchase_widget);
	}
}
