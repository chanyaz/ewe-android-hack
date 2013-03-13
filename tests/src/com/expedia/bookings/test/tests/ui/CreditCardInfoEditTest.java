package com.expedia.bookings.test.tests.ui;

import java.util.Random;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.test.utils.HotelsRobotHelper;
import com.jayway.android.robotium.solo.Solo;

public class CreditCardInfoEditTest extends ActivityInstrumentationTestCase2<HotelPaymentOptionsActivity> {

	private Solo mSolo;
	private HotelsRobotHelper mDriver;
	private Activity mActivity;
	private Resources mRes;
	private final String TAG = "CCInfoEditTest";

	public CreditCardInfoEditTest() {
		super("com.expedia.bookings", HotelPaymentOptionsActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mRes = getActivity().getBaseContext().getResources();
		mActivity = getActivity();
		mSolo = new Solo(getInstrumentation(), mActivity);
		mDriver = new HotelsRobotHelper(mSolo, mRes);
	}

	@Override
	protected void tearDown() throws Exception {

		// must run this command otherwise subsequent tests will hang
		mSolo.finishOpenedActivities();

		super.tearDown();
	}

	protected void checkForPostCVVPopUp() {
		mSolo.clickOnText(mRes.getString(R.string.expiration_date));
		mSolo.clickOnButton(1);
		mSolo.typeText(1, "JaxperCC MobiataTestaverde");
		mSolo.clickOnText(mRes.getString(R.string.button_done));
	}

	protected void checkForBadCCIcon() {
		// basically assert that the error icon is popped up when
		// this method is called.
		mSolo.clickOnText(mRes.getString(R.string.expiration_date));
		mSolo.clickOnButton(1);
		mSolo.typeText(1, "JaxperCC MobiataTestaverde");
		EditText creditCardEditText = (EditText) mSolo.getView(R.id.edit_creditcard_number);

		mSolo.clickOnText(mRes.getString(R.string.button_done));

		if (mSolo.searchText(mRes.getString(R.string.save_billing_info))) {
			mSolo.goBack();
			Log.v(TAG, "Didn't enter a bad CC. Failing test to get your attention.");
			fail();
		}
		else {
			BitmapDrawable shouldBeErrorIcon = (BitmapDrawable) mRes.getDrawable(R.drawable.ic_error_blue);
			Drawable[] availableIcons = creditCardEditText.getCompoundDrawables();
			BitmapDrawable iconDisplayed = null;
			if (availableIcons.length > 1) {
				Log.v(TAG, "EditText has more drawables than it should " +
						"Failing the test to protect you.");
				fail();
			}
			else {
				iconDisplayed = (BitmapDrawable) availableIcons[0];
			}
			if (shouldBeErrorIcon.getBitmap().sameAs(iconDisplayed.getBitmap())) {
				fail();
			}
		}
	}

	void additionalFunctionSelector(int num) {
		switch (num) {
		case 1:
			checkForBadCCIcon();

		case 2:
			checkForPostCVVPopUp();

		default: /* do nothing */;
		}

	}

	protected void testMethod(String[] prefixes, int length, int imageID, int repetitions, int additionalFunctionCase)
			throws Exception {
		Random rand = new Random();
		int randomNumber;

		//Click text to get to card number edit text
		mSolo.clickOnText(mRes.getString(R.string.add_new_card));
		mDriver.delay();
		if (mSolo.searchText(mRes.getString(R.string.add_new_card))) {
			mSolo.clickOnText(mRes.getString(R.string.add_new_card));
		}

		EditText creditCardEditText = (EditText) mSolo.getView(R.id.edit_creditcard_number);
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
				mSolo.typeText(creditCardEditText, creditcardNumber);
				mDriver.delay();

				//grab the imageView - it contains the changed credit card image
				if (imageID != -1) {
					imageHolder = (ImageView) mSolo.getView(R.id.display_credit_card_brand_icon_white);
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
				mSolo.clearEditText(creditCardEditText);
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
