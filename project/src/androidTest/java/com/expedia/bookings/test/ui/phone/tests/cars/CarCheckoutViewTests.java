package com.expedia.bookings.test.ui.phone.tests.cars;

import org.joda.time.DateTime;

import com.expedia.bookings.R;
import com.expedia.bookings.test.component.cars.CarViewModel;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.ui.utils.CarTestCase;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static com.expedia.bookings.test.ui.utils.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.ui.utils.EspressoUtils.assertViewIsNotDisplayed;
import static com.expedia.bookings.test.ui.utils.EspressoUtils.assertViewWithTextIsDisplayed;

public class CarCheckoutViewTests extends CarTestCase {

	private final static String CATEGORY = "Standard";
	private final static int CREDIT_CARD_NOT_REQUIRED = 0;

	public void testCheckoutView() throws Throwable {
		final DateTime startDateTime = DateTime.now().withTimeAtStartOfDay();
		final DateTime endDateTime = startDateTime.plusDays(3);
		CarViewModel.pickupLocation().perform(typeText("SFO"));
		CarViewModel.selectPickupLocation(getInstrumentation(), "San Francisco, CA");
		CarViewModel.selectDateButton().perform(click());
		CarViewModel.selectDates(startDateTime.toLocalDate(), endDateTime.toLocalDate());
		CarViewModel.searchButton().perform(click());
		CarViewModel.selectCarCategory(CATEGORY);
		CarViewModel.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);

		// test summary
		assertViewIsNotDisplayed(R.id.payment_info_card_view);
		assertViewWithTextIsDisplayed(R.id.car_vendor_text, "Fox");
		assertViewWithTextIsDisplayed(R.id.grand_total_text, "Total with Tax");
		assertViewWithTextIsDisplayed(R.id.price_text, "$108.47");
		assertViewWithTextIsDisplayed(R.id.pickup_text, "Due at pick-up");
		assertViewWithTextIsDisplayed(R.id.car_model_text, "Toyota Yaris or similar");
		assertViewWithTextIsDisplayed(R.id.location_description_text, "Shuttle to counter and car");
		assertViewWithTextIsDisplayed(R.id.airport_text, "San Francisco (SFO)");
		assertViewWithTextIsDisplayed(R.id.date_time_text, "Mar 10, 12:00 AM – Mar 12, 12:00 AM");

		CheckoutViewModel.enterTravelerInfo();
		assertViewWithTextIsDisplayed(R.id.purchase_total_text_view, "Amount due today: $0.00");
		assertViewIsDisplayed(R.id.slide_to_purchase_widget);
	}
}
