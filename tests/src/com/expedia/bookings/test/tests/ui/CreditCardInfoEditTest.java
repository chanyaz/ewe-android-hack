package com.expedia.bookings.test.tests.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Pair;
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

	protected void testMethod(String[] prefixes, int length, int imageID) throws Exception {
		Random rand = new Random();
		int randomNumber;

		//Click text to get to card number edit text
		mSolo.clickOnText(mRes.getString(R.string.add_new_card));

		EditText creditCardEditText = (EditText) mSolo.getView(R.id.edit_creditcard_number);
		String creditcardNumber;
		ImageView imageHolder;

		for (int i = 0; i < prefixes.length; i++) {
			//check 5 pseudo-random credit card numbers per card type
			for (int repetitions = 0; repetitions < 5; repetitions++) {
				creditcardNumber = prefixes[i];
				for (int j = creditcardNumber.length(); j < length; j++) {
					randomNumber = rand.nextInt(10);
					creditcardNumber += randomNumber;
				}
				mSolo.typeText(creditCardEditText, creditcardNumber);
				mDriver.delay();
				
				//grab the imageView - it contains the changed credit card image
				imageHolder = (ImageView) mSolo.getView(R.id.display_credit_card_brand_icon_white);
				BitmapDrawable currentImage = (BitmapDrawable) imageHolder.getDrawable();
				BitmapDrawable desiredImage = (BitmapDrawable) mRes.getDrawable(imageID);
				
				//compare image displayed and desired image pixel-by-pixel
				//fail if different
				if (!currentImage.getBitmap().sameAs(desiredImage.getBitmap())) {
					mDriver.enterLog("CreditCardTest", "Failed on: " + creditcardNumber);
					fail();
				}
				mSolo.clearEditText(creditCardEditText);
			}
		}
	}

	public void testVisa16() throws Exception {
		String[] prefixes = { "4" };
		testMethod(prefixes, 16, R.drawable.ic_visa_white);
	}

	public void testVisa13() throws Exception {
		String[] prefixes = { "4" };
		testMethod(prefixes, 13, R.drawable.ic_visa_white);
	}

	public void testMasterCard() throws Exception {
		String[] prefixes = { "51", "52", "53", "54", "55" };
		testMethod(prefixes, 16, R.drawable.ic_master_card_white);
	}

	public void testMaestro16() throws Exception {
		String[] prefixes = { "50", "63", "67" };
		testMethod(prefixes, 16, R.drawable.ic_maestro_white);
	}

	public void testMaestro18() throws Exception {
		String[] prefixes = { "50", "63", "67" };
		testMethod(prefixes, 18, R.drawable.ic_maestro_white);
	}

	public void testMaestro19() throws Exception {
		String[] prefixes = { "50", "63", "67" };
		testMethod(prefixes, 19, R.drawable.ic_maestro_white);
	}

	public void testDiscover() throws Exception {
		String[] prefixes = { "60" };
		testMethod(prefixes, 16, R.drawable.ic_discover_white);
	}

	public void testDinersClub() throws Exception {
		String[] prefixes = { "30", "36", "38", "60" };
		testMethod(prefixes, 14, R.drawable.ic_diners_club_white);
	}

	public void testChinaUnion16() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 16, R.drawable.ic_union_pay_white);
	}

	public void testChinaUnion17() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 17, R.drawable.ic_union_pay_white);
	}

	public void testChinaUnion18() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 18, R.drawable.ic_union_pay_white);
	}

	public void testChinaUnion19() throws Exception {
		String[] prefixes = { "62" };
		testMethod(prefixes, 19, R.drawable.ic_union_pay_white);
	}

	public void testCarteBlanche() throws Exception {
		String[] prefixes = { "94", "95" };
		testMethod(prefixes, 14, R.drawable.ic_carte_blanche_white);
	}

	public void testAmex() throws Exception {
		String[] prefixes = { "34", "37" };
		testMethod(prefixes, 15, R.drawable.ic_amex_white);
	}
}
