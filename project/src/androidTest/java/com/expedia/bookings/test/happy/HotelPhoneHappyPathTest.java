package com.expedia.bookings.test.happy;

import android.support.test.espresso.Espresso;

import com.expedia.bookings.R;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.phone.hotels.HotelScreen;
import com.expedia.bookings.test.phone.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.phone.pagemodels.common.PaymentOptionsScreen;
import com.expedia.bookings.utils.MockModeShim;
import com.mobiata.mocke3.ExpediaDispatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.expedia.bookings.test.phone.hotels.HotelScreen.pickRoom;

public class HotelPhoneHappyPathTest extends HotelTestCase {

	public void testHotelPhoneHappyPath() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel();
		reviews();
		launchFullMap();
		HotelScreen.selectRoom();

		CheckoutViewModel.clickDone();
		CheckoutViewModel.enterTravelerInfo();

		CheckoutViewModel.enterPaymentInfoHotels();
		CheckoutViewModel.performSlideToPurchase(false);
		HotelScreen.enterCVVAndBook();
		assertICanSeeItinNumber();
		verifyTravelAdTracking();
	}

	public void testNewHotelPhoneHappyPathLoggedInCustomer() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel();
		HotelScreen.selectRoom();
		CheckoutViewModel.clickDone();

		HotelScreen.doLogin();
		CheckoutViewModel.selectStoredTraveler();
		Common.delay(1);

		// checkout
		CheckoutViewModel.selectStoredCard(true);
		CheckoutViewModel.clickDone();
		CheckoutViewModel.performSlideToPurchase(true);

		assertICanSeeItinNumber();
	}

	private void assertICanSeeItinNumber() throws Throwable {
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.itin_text_view, "Itinerary #174113329733");
	}

	private void reviews() throws Throwable {
		HotelScreen.clickRatingContainer();
		Common.delay(1);
		onView(withText(R.string.user_review_sort_button_critical)).perform(click());
		onView(withText(R.string.user_review_sort_button_favorable)).perform(click());
		onView(withText(R.string.user_review_sort_button_recent)).perform(click());
		// TO-DO: Change to close button once the functionality is implemented.
		Espresso.pressBack();
	}

	private void launchFullMap() throws Throwable {
		Common.delay(1);
		HotelScreen.clickDetailsMiniMap();
		Common.delay(1);
		HotelScreen.clickSelectARoomInFullMap();
	}

	private void verifyTravelAdTracking() {
		ExpediaDispatcher dispatcher = MockModeShim.getDispatcher();
		assertEquals(1, dispatcher.numOfTravelAdRequests("/travel"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdImpression"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/TravelAdsService/v3/Hotels/TravelAdClick"));
		assertEquals(1, dispatcher.numOfTravelAdRequests("/ads/hooklogic"));
	}

	/**
	 * PwP(Pay with points) Happy path - This test case covers Checkout screen and Payment Method screen validation for points only
	 *
	 * @throws Throwable
	 * @author lsagar@
	 */
	public void testPwPHappyPathPayWithPointsOnly() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel("happypath_pwp");
		Common.delay(1);
		pickRoom("happypath_pwp_points_only");
		CheckoutViewModel.signInOnCheckout("singlecard@mobiata.com", "password");
		CheckoutViewModel.assertEarnPointsText("You are a valued member");
		CheckoutViewModel.assertPurchaseTotalText("You are using 2,395 ($2,394.88) Expedia+ points");
		CheckoutViewModel.assertCardInfoText("Paying with Points");
		CheckoutViewModel.assertSlideToPurchaseDisplayed();
		CheckoutViewModel.clickPaymentInfo();
		PaymentOptionsScreen.assertTextInEditAmountMatches("2394.88");
		PaymentOptionsScreen.assertTotalDueAmountMatches("2,394.88");
		PaymentOptionsScreen.assertMenuDoneClickable();
		PaymentOptionsScreen.assertRemainingDueMatches("0.00");
		PaymentOptionsScreen.assertTotalPointsAvailableMatches("30,000");
		PaymentOptionsScreen.assertTotalAmountAvailableMatches("30,000.00");
		PaymentOptionsScreen.assertPointsAppliedMatches("2,395");
		PaymentOptionsScreen.assertCardSectionDisabled();
		//Disabling PwP feature
		PaymentOptionsScreen.togglePWP();
		Common.delay(1);
		PaymentOptionsScreen.assertMenuDoneClickable();
		PaymentOptionsScreen.assertCardSectionEnabled();
		PaymentOptionsScreen.assertCardSelectionMatches("Saved Visa 1111", 0);
		PaymentOptionsScreen.assertRemainingDueMatches("2,394.88");
		//Enabling PwP feature back
		PaymentOptionsScreen.togglePWP();
		PaymentOptionsScreen.assertCardSectionDisabled();

	}

	/**
	 * PwP(Pay with points) Happy path - This test case covers Checkout screen and Payment Method screen validation for points and Card
	 *
	 * @throws Throwable
	 * @author lsagar@
	 */
	public void testPwPHappyPathPayWithPointsAndCard() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel("happypath_pwp");
		Common.delay(1);
		pickRoom("happypath_pwp_points_with_card");
		CheckoutViewModel.signInOnCheckout("singlecard@mobiata.com", "password");
		CheckoutViewModel.assertEarnPointsText("earn 795 points");
		CheckoutViewModel.assertPurchaseTotalText("You are using 3,600 ($3,600) Expedia+ points\nYour card will be charged $794.88");
		CheckoutViewModel.assertCardInfoText("Paying with Points & Visa 1111");
		CheckoutViewModel.assertSlideToPurchaseDisplayed();
		CheckoutViewModel.clickPaymentInfo();
		Common.delay(1);
		PaymentOptionsScreen.assertTextInEditAmountMatches("3600");
		PaymentOptionsScreen.assertTotalDueAmountMatches("4,394.88");
		PaymentOptionsScreen.assertMenuDoneClickable();
		PaymentOptionsScreen.assertRemainingDueMatches("$794.88");
		PaymentOptionsScreen.assertTotalPointsAvailableMatches("3,600");
		PaymentOptionsScreen.assertTotalAmountAvailableMatches("3,600.00");
		PaymentOptionsScreen.assertPointsAppliedMatches("3,600");
		PaymentOptionsScreen.assertCardSectionEnabled();
		PaymentOptionsScreen.assertCardSelectionMatches("Saved Visa 1111", 0);
	}

	/**
	 * PwP(Pay with points) - Editing amount for point calculation and validate checkout screen
	 *
	 * @throws Throwable
	 * @author lsagar@
	 */
	public void testPwPCalculatePoints() throws Throwable {
		HotelScreen.doGenericSearch();
		HotelScreen.selectHotel("happypath_pwp");
		Common.delay(1);
		pickRoom("happypath_pwp_points_with_card");
		CheckoutViewModel.signInOnCheckout("singlecard@mobiata.com", "password");
		CheckoutViewModel.clickPaymentInfo();
		PaymentOptionsScreen.clickAmountForPointsCalculation();
		PaymentOptionsScreen.enterAmountForPointsCalculation("100");
		PaymentOptionsScreen.tapPointsAppliedLabel();
		PaymentOptionsScreen.assertPointsAppliedMatches("100");
		PaymentOptionsScreen.assertRemainingDueMatches("$4,294.88");
		PaymentOptionsScreen.assertMenuDoneClickable();
		PaymentOptionsScreen.assertCardSectionEnabled();
		PaymentOptionsScreen.clickMenuDone();
		CheckoutViewModel.assertEarnPointsText("earn 4,295 points");
		CheckoutViewModel.assertCardInfoText("Paying with Points & Visa 1111");
		CheckoutViewModel.assertPurchaseTotalText("You are using 100 ($100.00) Expedia+ points\nYour card will be charged $4,294.88");
		CheckoutViewModel.assertSlideToPurchaseDisplayed();
	}
}
