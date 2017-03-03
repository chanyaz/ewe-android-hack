package com.expedia.bookings.test.phone.cars;

import com.expedia.bookings.R;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.AbacusTestUtils;
import com.expedia.bookings.test.espresso.CarTestCase;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.ViewActions;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.SearchScreen;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertTextWithChildrenIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewIsNotDisplayed;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertViewWithTextIsDisplayed;

public class CarCheckoutViewTest extends CarTestCase {

	private final static String CATEGORY = "Standard";
	private final static int CREDIT_CARD_NOT_REQUIRED = 0;
	private final static int INSURANCE_INCLUDED = 1;

	public void testCheckoutView() throws Throwable {
		gotoCheckout(CREDIT_CARD_NOT_REQUIRED);

		// test summary
		assertViewIsNotDisplayed(R.id.payment_info_card_view);
		assertViewWithTextIsDisplayed(R.id.car_vendor_text, "Fox");
		assertViewWithTextIsDisplayed(R.id.grand_total_text, "Total with Tax");
		assertViewWithTextIsDisplayed(R.id.price_text, "$108.47");
		assertViewWithTextIsDisplayed(R.id.due_at_text, "Due at pick-up");
		assertViewWithTextIsDisplayed(R.id.car_model_text, "Toyota Yaris or similar");
		assertViewWithTextIsDisplayed(R.id.location_description_text, "Shuttle to counter and car");
		assertViewWithTextIsDisplayed(R.id.airport_text, "San Francisco (SFO)");
		assertViewWithTextIsDisplayed(R.id.date_time_text, "Mar 10, 12:00 AM – Mar 12, 12:00 AM");

		CheckoutViewModel.enterTravelerInfo();
		assertViewWithTextIsDisplayed(R.id.purchase_total_text_view, "Amount due today: $0.00");
		assertViewIsDisplayed(R.id.slide_to_purchase_widget);
	}

	public void testInsuranceIncludedViewAbacusTest() throws Throwable {
		AbacusTestUtils.updateABTest(AbacusUtils.EBAndroidAppCarInsuranceIncludedCKO,
			AbacusUtils.DefaultVariant.BUCKETED.ordinal());
		gotoCheckout(INSURANCE_INCLUDED);
		assertTextWithChildrenIsDisplayed(R.id.ticked_info_container, "Insurance included");
	}

	private void gotoCheckout(int carOfferIndex) throws Throwable {
		SearchScreen.doGenericCarSearch();
		CarScreen.selectCarCategory(CATEGORY);
		CarScreen.selectCarOffer(carOfferIndex);
	}

	public void testAcceptTermsVisibilityOnBack() throws Throwable {
		Common.setPOS(PointOfSaleId.FRANCE);
		gotoCheckout(CREDIT_CARD_NOT_REQUIRED);
		screenshot("Checkout_Payment_Card");
		CheckoutViewModel.enterTravelerInfo();
		Common.pressBack();
		Common.delay(1);
		CarScreen.searchButtonOnDetails().perform(click());
		CarScreen.locationCardView().perform(click());
		CarScreen.pickupLocation().perform(ViewActions.waitForViewToDisplay(), typeText("SFO"));
		CarScreen.selectPickupLocation("San Francisco, CA");
		CarScreen.searchButton().perform(click());
		CarScreen.selectCarCategory(CATEGORY);
		CarScreen.selectCarOffer(CREDIT_CARD_NOT_REQUIRED);
		CarScreen.acceptTermsWidget().check(matches((isDisplayed())));
	}

}
