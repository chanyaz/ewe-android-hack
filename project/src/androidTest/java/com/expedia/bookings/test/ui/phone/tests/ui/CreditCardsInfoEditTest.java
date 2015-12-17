package com.expedia.bookings.test.ui.phone.tests.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.joda.time.LocalDate;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CVVEntryScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CardInfoScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonPaymentMethodScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.CommonTravelerInformationScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.common.LaunchScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsCheckoutScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsDetailsScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsRoomsRatesScreen;
import com.expedia.bookings.test.ui.phone.pagemodels.hotels.HotelsSearchScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.PhoneTestCase;

public class CreditCardsInfoEditTest extends PhoneTestCase {

	List<TestData> mTestData = new LinkedList<TestData>();
	private static final String TAG = CreditCardsInfoEditTest.class.getSimpleName();

	private static class TestData {
		String mTestName;
		String[] mPrefixes;
		int mLength;
		int mDrawableId;

		TestData(String testName, String[] prefixes, int length, int imageID) {
			mTestName = testName;
			mPrefixes = prefixes;
			mLength = length;
			mDrawableId = imageID;
		}

		public String getTestName() {
			return mTestName;
		}

		public String[] getPrefixes() {
			return mPrefixes;
		}

		public int getLength() {
			return mLength;
		}

		public int getDrawableId() {
			return mDrawableId;
		}
	}

	private void runTestCase(TestData testData) throws Exception {
		Random rand = new Random();
		int randomNumber;

		String creditcardNumber;

		for (int i = 0; i < testData.getPrefixes().length; i++) {
			//random credit card numbers per card type
			creditcardNumber = testData.getPrefixes()[i];
			for (int k = creditcardNumber.length(); k < testData.getLength(); k++) {
				randomNumber = rand.nextInt(10);
				creditcardNumber += randomNumber;
			}

			CardInfoScreen.typeTextCreditCardEditText(creditcardNumber);
			Common.closeSoftKeyboard(CardInfoScreen.creditCardNumberEditText());
			CardInfoScreen.clickOnExpirationDateButton();
			CardInfoScreen.clickMonthUpButton();
			CardInfoScreen.clickYearUpButton();
			CardInfoScreen.clickSetButton();
			CardInfoScreen.typeTextPostalCode("94015");
			CardInfoScreen.typeTextNameOnCardEditText("Mobiata Auto");


			/*
			* Case 1: verify cards working, test credit card logo displayed
			*/
			try {
				EspressoUtils.assertContainsImageDrawable(R.id.display_credit_card_brand_icon_white, testData.getDrawableId());
				Common.enterLog(TAG, "Credit card brand logo is correctly displayed for " + testData.getTestName());

			}
			catch (Exception e) {
				throw new Exception("Failure-" + testData.getTestName(), e);
			}
			CardInfoScreen.clickOnDoneButton();
			HotelsCheckoutScreen.slideToCheckout();

			/*
			* Case 2: check cvv sub prompt text view
			* For Amex cards:"See front of the card" and for other cards: "See back of card"
			*/

			if (testData.getTestName().equals("Amex")) {
				EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.See_front_of_card));
				Common.enterLog(TAG, "CVV sub prompt text (See front of the card) is correctly displayed for " + testData.getTestName());
			}
			else {
				EspressoUtils.assertViewWithTextIsDisplayed(mRes.getString(R.string.See_back_of_card));
				Common.enterLog(TAG, "CVV sub prompt text (See back of the card) is correctly displayed for " + testData.getTestName());
			}

			/*
			* Case 3: Security Code will show the cardholders name Firstname Lastname as: F. Lastname
			*/

			EspressoUtils.assertContains(CVVEntryScreen.cvvSignatureText(), "M. Auto");
			Common.enterLog(TAG, " Security Code correctly shows the cardholders name for " + testData.getTestName());

			//go back for next test data
			Common.pressBack();
			Checkout.clickCreditCardSection();
			CommonPaymentMethodScreen.clickOnAddNewCardTextView();
		}
	}

	public void recordData(String testName, String[] prefixes, int length, int imageID) {
		mTestData.add(new TestData(testName, prefixes, length, imageID));
	}

	public void testCCBrandIcon() throws Throwable {
		recordData("Visa16", new String[] {"4"}, 16, R.drawable.ic_visa_white);

		recordData("Visa13", new String[] {"4"}, 13, R.drawable.ic_visa_white);

		recordData("MasterCard", new String[] {
			"51", "52", "53", "54", "55"
		}, 16, R.drawable.ic_master_card_white);

		recordData("Maestro16", new String[] {
			"50", "63", "67"
		}, 16, R.drawable.ic_maestro_white);

		recordData("Maestro18", new String[] {
			"50", "63", "67"
		}, 18, R.drawable.ic_maestro_white);

		recordData("Maestro19", new String[] {
			"50", "63", "67"
		}, 19, R.drawable.ic_maestro_white);

		recordData("Discover", new String[] {"60"}, 16, R.drawable.ic_discover_white);

		recordData("DinersClub", new String[] {
			"30", "36", "38", "60"
		}, 14, R.drawable.ic_diners_club_white);

		recordData("ChinaUnion17", new String[] {
			"62"
		}, 17, R.drawable.ic_union_pay_white);

		recordData("ChinaUnion18", new String[] {
			"62"
		}, 18, R.drawable.ic_union_pay_white);

		recordData("ChinaUnion19", new String[] {
			"62"
		}, 19, R.drawable.ic_union_pay_white);

		recordData("CarteBlanche", new String[] {
			"94", "95"
		}, 14, R.drawable.ic_carte_blanche_white);

		recordData("Amex", new String[] {"34", "37"}, 15, R.drawable.ic_amex_white);

		LaunchScreen.launchHotels();
		HotelsSearchScreen.clickSearchEditText();
		HotelsSearchScreen.clickToClearSearchEditText();
		HotelsSearchScreen.enterSearchText("New York, NY");
		HotelsSearchScreen.clickSuggestionWithName(getActivity(), "New York, NY");
		LocalDate startDate = LocalDate.now().plusDays(35);
		LocalDate endDate = LocalDate.now().plusDays(40);
		HotelsSearchScreen.clickOnCalendarButton();
		HotelsSearchScreen.clickDate(startDate, endDate);
		HotelsSearchScreen.clickOnGuestsButton();
		HotelsSearchScreen.guestPicker().clickOnSearchButton();
		HotelsSearchScreen.clickHotelWithName("happypath");
		HotelsDetailsScreen.clickSelectButton();
		HotelsRoomsRatesScreen.selectRoomItem(0);
		HotelsCheckoutScreen.clickCheckoutButton();

		HotelsCheckoutScreen.clickGuestDetails();
		CommonTravelerInformationScreen.enterFirstName("Mobiata");
		CommonTravelerInformationScreen.enterLastName("Auto");
		CommonTravelerInformationScreen.enterPhoneNumber("1112223333");
		CommonTravelerInformationScreen.enterEmailAddress("mobiataauto@gmail.com");
		CommonTravelerInformationScreen.clickDoneButton();

		HotelsCheckoutScreen.clickSelectPaymentButton();

		//loop through the TestData
		for (int i = 0; i < mTestData.size(); i++) {
			runTestCase(mTestData.get(i));
		}
	}

	// NOTE: subsequent tests after this test fail without backing out in this manner.
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (Common.isPhone(getInstrumentation())) {
			Common.pressBackOutOfApp(getInstrumentation());
		}
	}
}
