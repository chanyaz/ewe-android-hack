package com.expedia.bookings.test.ui.tablet.tests.ui;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.LogIn;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.ui.tablet.pagemodels.Search;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.HotelsUserData;
import com.expedia.bookings.test.espresso.TabletTestCase;

public class CheckoutStoredCreditCardTest extends TabletTestCase {

	HotelsUserData mUser;

	private String creditCardName = "AmexTesting";

	private void addFlightHotelGoToCheckoutAndLogin() throws Throwable {
		// Test setup
		mUser = new HotelsUserData();
		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");
		Search.clickOriginButton();
		Search.typeInOriginEditText("San Francisco, CA");
		Search.clickSuggestion("San Francisco, CA");
		Search.clickSelectFlightDates();
		int randomOffset = 20 + (int) (Math.random() * 100);
		LocalDate startDate = LocalDate.now().plusDays(randomOffset);
		Search.clickDate(startDate, null);
		Search.clickSearchPopupDone();
		Results.swipeUpFlightList();
		Results.clickFlightAtIndex(1);
		Results.clickAddFlight();

		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		HotelDetails.clickAddHotel();
		screenshot("Both_Flight_Hotel_Bucket");

		Results.clickBookFlight();
		screenshot("Choose_Book_Flight_First");
		Results.clickBookHotel();
		screenshot("Choose_Book_Hotel_Next");

		Checkout.clickLoginButton();
		LogIn.enterUserName(mUser.email);
		LogIn.enterPassword(mUser.password);
		screenshot("Login_Info_Entered");
		LogIn.clickLoginExpediaButton();
		EspressoUtils.assertViewWithTextIsDisplayed(mUser.email);
		screenshot("Login_Successful");
	}

	public void selectStoredCCFromList() throws Throwable {
		Checkout.clickOnEmptyStoredCCSpinnerButton();
		Checkout.selectStoredCard(getInstrumentation(), creditCardName);
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.display_stored_card_desc, creditCardName);
		screenshot("CheckoutOverview_Stored_CC_Selected");
		Checkout.clickOnStoredCCEditButton();
		EspressoUtils.assertViewWithTextIsDisplayed(R.id.stored_card_name, creditCardName);
		screenshot("CCDetail_Stored_CC_Present");
		Common.pressBack();
	}

	public void removeSelectedStoredCC() throws Throwable {
		Checkout.clickOnStoredCCEditButton();
		Checkout.clickOnRemoveStoredCCButton();
		Common.pressBack();
		EspressoUtils.assertViewWithTextIsNotDisplayed(R.id.display_stored_card_desc, creditCardName);
		screenshot("CheckoutOverview_No_CC_Selected");
	}

	public void testLoginStoredCCOperations() throws Throwable {
		addFlightHotelGoToCheckoutAndLogin();
		selectStoredCCFromList();
		removeSelectedStoredCC();
	}

}
