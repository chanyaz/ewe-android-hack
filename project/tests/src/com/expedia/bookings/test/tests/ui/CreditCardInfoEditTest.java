package com.expedia.bookings.test.tests.ui;

import java.util.Random;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.test.ActivityInstrumentationTestCase2;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.test.utils.HotelsTestDriver;
import com.expedia.bookings.test.utils.HotelsUserData;
import com.expedia.bookings.test.utils.TestPreferences;

@SuppressLint("NewApi")
public class CreditCardInfoEditTest extends ActivityInstrumentationTestCase2<HotelPaymentOptionsActivity> {

	public CreditCardInfoEditTest() {
		super(HotelPaymentOptionsActivity.class);
	}

	private static final String TAG = CreditCardInfoEditTest.class.getSimpleName();

	private Resources mRes;
	DisplayMetrics mMetric;
	private HotelsTestDriver mDriver;
	private HotelsUserData mUser;
	private TestPreferences mPreferences;

	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mPreferences = new TestPreferences();
		mPreferences.setRotationPermission(false);
		mPreferences.setScreenshotPermission(false);
		mDriver = new HotelsTestDriver(getInstrumentation(), getActivity(), mRes, mPreferences);
		mUser = new HotelsUserData();
		mUser.setHotelCityToRandomUSCity();
	}

	@Override
	protected void tearDown() throws Exception {
		mDriver.finishOpenedActivities();
		super.tearDown();
	}

	protected void checkForPostCVVPopUp() {
		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.cardInfoScreen().clickMonthUpButton();
		mDriver.cardInfoScreen().clickYearUpButton();
		mDriver.cardInfoScreen().clickSetButton();
		mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		mDriver.cardInfoScreen().clickOnDoneButton();
	}

	protected void checkForBadCCIcon() {
		// basically assert that the error icon is popped up when
		// this method is called.
		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.cardInfoScreen().clickSetButton();

		mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		mDriver.cardInfoScreen().typeTextPostalCode(mUser.getAddressPostalCode());
		mDriver.cardInfoScreen().clickOnDoneButton();

		//If a pop up appears asking to save billing info, then the
		//app didn't see the bad CC
		if (mDriver.searchText(mDriver.cardInfoScreen().saveButtonString())) {
			mDriver.goBack();
			mDriver.enterLog(TAG, "Didn't enter a bad CC. Failing test to get your attention.");
			fail();
		}
		else {
			//Otherwise, grab the drawable displayed for the error, and make
			//sure it's the correct drawable
			BitmapDrawable shouldBeErrorIcon = (BitmapDrawable) mRes.getDrawable(R.drawable.ic_error_blue);
			Drawable[] availableIcons = mDriver.cardInfoScreen().creditCardNumberEditText().getCompoundDrawables();

			BitmapDrawable iconDisplayed = (BitmapDrawable) availableIcons[2];

			//Test fails if the icon presented isn't the proper icon
			if (!shouldBeErrorIcon.getBitmap().sameAs(iconDisplayed.getBitmap())) {
				mDriver.enterLog(TAG, "Desired icon: " + iconDisplayed.toString());
				mDriver.enterLog(TAG, "Actual icon: " + shouldBeErrorIcon.toString());
				fail();
			}
		}
	}

	//Switch for picking the proper followup 
	//test method
	//The "testToRun" determined in testMethod
	// "additionalFunctionCase"
	void additionalFunctionSelector(int testToRun) {
		switch (testToRun) {
		case 1:
			checkForBadCCIcon();
			break;
		case 2:
			checkForPostCVVPopUp();
			break;
		default: /* do nothing */
			break;
		}

	}

	protected void testMethod(String[] prefixes, int length, int imageID, int repetitions, int additionalFunctionCase)
			throws Exception {
		Random rand = new Random();
		int randomNumber;

		String creditcardNumber;
		ImageView imageHolder;

		for (int i = 0; i < prefixes.length; i++) {
			//check (repetition) # pseudo-random credit card numbers per card type
			for (int j = 0; j < repetitions; j++) {
				creditcardNumber = prefixes[i];
				for (int k = creditcardNumber.length(); k < length; k++) {
					randomNumber = rand.nextInt(10);
					creditcardNumber += randomNumber;
				}
				try {
					mDriver.cardInfoScreen().typeTextCreditCardEditText(creditcardNumber);
				}
				catch (Error e) {
					// try to enter CC again in case of Robotium error 
					mDriver.delay();
					mDriver.cardInfoScreen().typeTextCreditCardEditText(creditcardNumber);
				}
				catch (Exception ex) {
					mDriver.delay();
					mDriver.cardInfoScreen().typeTextCreditCardEditText(creditcardNumber);
				}
				mDriver.delay();

				//grab the imageView - it contains the changed credit card image
				if (imageID != -1) {
					imageHolder = (ImageView) mDriver.getView(R.id.display_credit_card_brand_icon_white);
					BitmapDrawable currentImage = (BitmapDrawable) imageHolder.getDrawable();
					BitmapDrawable desiredImage = (BitmapDrawable) mRes.getDrawable(imageID);

					//compare image displayed and desired image pixel-by-pixel
					//fail if different
					if (!currentImage.getBitmap().sameAs(desiredImage.getBitmap())) {
						mDriver.enterLog("CreditCardTest", "Failed on: " + creditcardNumber);
						fail();
					}
				}
				additionalFunctionSelector(additionalFunctionCase);
				mDriver.clearEditText(mDriver.cardInfoScreen().creditCardNumberEditText());
			}
		}
	}

	public void testLongCCError() throws Exception {
		String[] prefixes = { "4" };
		testMethod(prefixes, 30, -1, 1, 1);
	}

	///////////// Credit Card Logo Tests /////////////
	public void testVisa16() throws Exception {
		String[] prefixes = { "4" };
		testMethod(prefixes, 16, R.drawable.ic_visa_white, 10, 0);
	}

	public void testVisa13() throws Exception {
		String[] prefixes = { "4" };
		testMethod(prefixes, 13, R.drawable.ic_visa_white, 10, 0);
	}

	public void testMasterCard() throws Exception {
		String[] prefixes = { "51", "52", "53", "54", "55" };
		testMethod(prefixes, 16, R.drawable.ic_master_card_white, 10, 0);
	}

	public void testMaestro16() throws Exception {
		String[] prefixes = { "50", "63", "67" };
		testMethod(prefixes, 16, R.drawable.ic_maestro_white, 10, 0);
	}

	public void testMaestro18() throws Exception {
		String[] prefixes = { "50", "63", "67" };
		testMethod(prefixes, 18, R.drawable.ic_maestro_white, 10, 0);
	}

	public void testMaestro19() throws Exception {
		String[] prefixes = { "50", "63", "67" };
		testMethod(prefixes, 19, R.drawable.ic_maestro_white, 10, 0);
	}

	public void testDiscover() throws Exception {
		String[] prefixes = { "60" };
		testMethod(prefixes, 16, R.drawable.ic_discover_white, 10, 0);
	}

	public void testDinersClub() throws Exception {
		String[] prefixes = { "30", "36", "38", "60" };
		testMethod(prefixes, 14, R.drawable.ic_diners_club_white, 10, 0);
	}

	public void testChinaUnion16() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 16, R.drawable.ic_union_pay_white, 10, 0);
	}

	public void testChinaUnion17() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 17, R.drawable.ic_union_pay_white, 10, 0);
	}

	public void testChinaUnion18() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 18, R.drawable.ic_union_pay_white, 10, 0);
	}

	public void testChinaUnion19() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 19, R.drawable.ic_union_pay_white, 10, 0);
	}

	public void testCarteBlanche() throws Exception {
		String[] prefixes = { "94", "95" };
		testMethod(prefixes, 14, R.drawable.ic_carte_blanche_white, 10, 0);
	}

	public void testAmex() throws Exception {
		String[] prefixes = { "34", "37" };
		testMethod(prefixes, 15, R.drawable.ic_amex_white, 10, 0);
	}
	///////////////////////////////////////

}
