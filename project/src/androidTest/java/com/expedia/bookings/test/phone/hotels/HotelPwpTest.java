package com.expedia.bookings.test.phone.hotels;

import org.junit.Test;

import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.espresso.HotelTestCase;
import com.expedia.bookings.test.pagemodels.common.CheckoutViewModel;
import com.expedia.bookings.test.pagemodels.common.PaymentOptionsScreen;
import com.expedia.bookings.test.pagemodels.common.SearchScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelInfoSiteScreen;
import com.expedia.bookings.test.pagemodels.hotels.HotelScreen;

public class HotelPwpTest extends HotelTestCase {

	/**
	 * PwP(Pay with points) Happy path - This test case covers Checkout screen and Payment Method screen validation for points only
	 *
	 * @throws Throwable
	 * @author lsagar@
	 */
	@Test
	public void testPwPHappyPathPayWithPointsOnly() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("happypath_pwp");
		Common.delay(1);
		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("happypath_pwp_points_only");
		CheckoutViewModel.signInOnCheckout("singlecard@mobiata.com", "password");
		CheckoutViewModel.assertEarnPointsText("You are a valued member");
		CheckoutViewModel.assertPurchaseTotalText("You are using 2,395 ($2,394.88) Expedia+ points");
		CheckoutViewModel.assertCardInfoText("Paying with Points");
		CheckoutViewModel.assertSlideToPurchaseDisplayed();
		CheckoutViewModel.clickPaymentInfo();
		PaymentOptionsScreen.assertTextInEditAmountMatches("2394.88");
		PaymentOptionsScreen.assertTotalDueAmountMatches("2,394.88");
		PaymentOptionsScreen.assertMenuDoneClickable();

		PaymentOptionsScreen.clickPwpSwitch();
		PaymentOptionsScreen.assertMenuDoneClickable();
		PaymentOptionsScreen.clickPwpSwitch();

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
	@Test
	public void testPwPHappyPathPayWithPointsAndCard() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("happypath_pwp");
		Common.delay(1);
		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("happypath_pwp_points_with_card");
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
	@Test
	public void testPwPCalculatePoints() throws Throwable {
		SearchScreen.doGenericHotelSearch();
		HotelScreen.selectHotel("happypath_pwp");
		Common.delay(1);
		HotelInfoSiteScreen.clickStickySelectRoom();
		HotelInfoSiteScreen.bookRoomType("happypath_pwp_points_with_card");
		CheckoutViewModel.signInOnCheckout("singlecard@mobiata.com", "password");
		CheckoutViewModel.clickPaymentInfo();
		PaymentOptionsScreen.clickAmountForPointsCalculation();
		PaymentOptionsScreen.enterAmountForPointsCalculation("100");
		PaymentOptionsScreen.tapPointsAppliedLabel();
		Common.delay(1);
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
