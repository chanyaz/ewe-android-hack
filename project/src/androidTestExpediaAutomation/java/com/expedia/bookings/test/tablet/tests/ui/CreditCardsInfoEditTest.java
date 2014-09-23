package com.expedia.bookings.test.tablet.tests.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.expedia.bookings.R;
import com.expedia.bookings.test.tablet.pagemodels.Checkout;
import com.expedia.bookings.test.tablet.pagemodels.HotelDetails;
import com.expedia.bookings.test.tablet.pagemodels.Launch;
import com.expedia.bookings.test.tablet.pagemodels.Results;
import com.expedia.bookings.test.utils.EspressoUtils;
import com.expedia.bookings.test.utils.TabletTestCase;

/**
 * Created by dmadan on 9/22/14.
 */

public class CreditCardsInfoEditTest extends TabletTestCase {

	List<TestData> mTestData = new LinkedList<TestData>();

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
			try {
				//compare credit card image displayed and desired image
				EspressoUtils.assertContainsImageDrawable(R.id.display_credit_card_brand_icon_tablet, testData.getDrawableId());
			}
			catch (Exception e) {
				throw new Exception("Failure-" + testData.getTestName(), e);
			}
			EspressoUtils.clear(Checkout.creditCardNumber());
		}
	}

	public void recordData(String testName, String[] prefixes, int length, int imageID) {
		mTestData.add(new TestData(testName, prefixes, length, imageID));
	}

	public void testCCBrandIcon() throws Exception {
		recordData("Visa16", new String[] {"4"}, 16, R.drawable.ic_tablet_checkout_visa);

		recordData("Visa13", new String[] {"4"}, 13, R.drawable.ic_tablet_checkout_visa);

		recordData("MasterCard", new String[] {"51", "52", "53", "54", "55"}, 16, R.drawable.ic_tablet_checkout_mastercard);

		recordData("Maestro16", new String[] {"50", "63", "67"}, 16, R.drawable.ic_tablet_checkout_maestro);

		recordData("Maestro18", new String[] {"50", "63", "67"}, 18, R.drawable.ic_tablet_checkout_maestro);

		recordData("Maestro19", new String[] {"50", "63", "67"}, 19, R.drawable.ic_tablet_checkout_maestro);

		recordData("Discover", new String[] {"60"}, 16, R.drawable.ic_tablet_checkout_discover);

		recordData("DinersClub", new String[] {"30", "36", "38", "60"}, 14, R.drawable.ic_tablet_checkout_diners_club);

		recordData("ChinaUnion17", new String[] {"62"}, 17, R.drawable.ic_tablet_checkout_union_pay);

		recordData("ChinaUnion18", new String[] {"62"}, 18, R.drawable.ic_tablet_checkout_union_pay);

		recordData("ChinaUnion19", new String[] {"62"}, 19, R.drawable.ic_tablet_checkout_union_pay);

		recordData("CarteBlanche", new String[] {"94", "95"}, 14, R.drawable.ic_tablet_checkout_carte_blanche);

		recordData("Amex", new String[] {"34", "37"}, 15, R.drawable.ic_tablet_checkout_amex);

		Launch.clickSearchButton();
		Launch.clickDestinationEditText();
		Launch.typeInDestinationEditText("Detroit, MI");
		Launch.clickSuggestion("Detroit, MI");

		Results.swipeUpHotelList();
		Results.clickHotelAtIndex(1);
		HotelDetails.clickAddHotel();
		Results.clickBookHotel();

		Checkout.clickOnEnterPaymentInformation();

		//loop through the TestData
		for (int i = 0; i < mTestData.size(); i++) {
			runTestCase(mTestData.get(i));
		}
	}
}
