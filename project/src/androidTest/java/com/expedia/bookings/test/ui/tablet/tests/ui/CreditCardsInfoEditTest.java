package com.expedia.bookings.test.ui.tablet.tests.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.expedia.bookings.R;
import com.expedia.bookings.test.ui.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.expedia.bookings.test.ui.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.ui.tablet.pagemodels.Launch;
import com.expedia.bookings.test.ui.tablet.pagemodels.Results;
import com.expedia.bookings.test.espresso.EspressoUtils;
import com.expedia.bookings.test.espresso.TabletTestCase;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 9/22/14.
 */

public class CreditCardsInfoEditTest extends TabletTestCase {

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

	protected void runTestCase(TestData testData) throws Exception {
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

			Checkout.enterCreditCardNumber(creditcardNumber);

			/*
			* Case 1: verify cards working, test credit card logo displayed
			*/
			try {
				EspressoUtils.assertContainsImageDrawable(R.id.display_credit_card_brand_icon_tablet, testData.getDrawableId(), R.id.new_card_container);
				Common.enterLog(TAG, "Credit card brand logo is correctly displayed for " + testData.getTestName());
			}
			catch (Exception e) {
				throw new Exception("Failure-" + testData.getTestName(), e);
			}
			Checkout.clickOnDone();
			Checkout.slideToPurchase();

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

			EspressoUtils.assertContains(onView(withId(R.id.signature_text_view)), "M. Auto");
			Common.enterLog(TAG, " Security Code correctly shows the cardholders name for " + testData.getTestName());

			//go back and clear credit card edit box for next test data
			Common.pressBack();
			Checkout.clickCreditCardSection();
			Checkout.creditCardNumber().perform(clearText());
		}
	}

	public void recordData(String testName, String[] prefixes, int length, int imageID) {
		mTestData.add(new TestData(testName, prefixes, length, imageID));
	}

	public void testCCBrandIcon() throws Exception {
		recordData("Visa16", new String[] {"4"}, 16, R.drawable.ic_tablet_checkout_visa);

		recordData("Visa13", new String[] {"4"}, 13, R.drawable.ic_tablet_checkout_visa);

		recordData("MasterCard", new String[] {
			"51", "52", "53", "54", "55"
		}, 16, R.drawable.ic_tablet_checkout_mastercard);

		recordData("Maestro16", new String[] {
			"50", "63", "67"
		}, 16, R.drawable.ic_tablet_checkout_maestro);

		recordData("Maestro18", new String[] {
			"50", "63", "67"
		}, 18, R.drawable.ic_tablet_checkout_maestro);

		recordData("Maestro19", new String[] {
			"50", "63", "67"
		}, 19, R.drawable.ic_tablet_checkout_maestro);

		recordData("Discover", new String[] {"60"}, 16, R.drawable.ic_tablet_checkout_discover);

		recordData("DinersClub", new String[] {
			"30", "36", "38", "60"
		}, 14, R.drawable.ic_tablet_checkout_diners_club);

		recordData("ChinaUnion17", new String[] {
			"62"
		}, 17, R.drawable.ic_tablet_checkout_union_pay);

		recordData("ChinaUnion18", new String[] {
			"62"
		}, 18, R.drawable.ic_tablet_checkout_union_pay);

		recordData("ChinaUnion19", new String[] {
			"62"
		}, 19, R.drawable.ic_tablet_checkout_union_pay);

		recordData("CarteBlanche", new String[] {
			"94", "95"
		}, 14, R.drawable.ic_tablet_checkout_carte_blanche);

		recordData("Amex", new String[] {"34", "37"}, 15, R.drawable.ic_tablet_checkout_amex);

		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		Checkout.clickOnEmptyTravelerDetails();
		Checkout.enterFirstName("Mobiata");
		Checkout.enterLastName("Auto");
		Checkout.enterPhoneNumber("1112223333");
		Checkout.enterEmailAddress("aaa@aaa.com");
		Common.closeSoftKeyboard(Checkout.firstName());
		Checkout.clickOnDone();

		Checkout.clickOnEnterPaymentInformation();
		Checkout.setExpirationDate(2020, 12);
		Checkout.enterNameOnCard("Mobiata Auto");
		Checkout.enterPostalCode("95104");
		Common.closeSoftKeyboard(Checkout.postalCode());

		//loop through the TestData
		for (int i = 0; i < mTestData.size(); i++) {
			runTestCase(mTestData.get(i));
		}
	}
}
