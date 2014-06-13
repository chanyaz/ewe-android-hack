package com.expedia.bookings.test.tests.ui;

import java.util.Random;

import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.HotelPaymentOptionsActivity;
import com.expedia.bookings.test.tests.pageModelsEspresso.common.CardInfoScreen;

import static com.expedia.bookings.test.utilsEspresso.CustomMatchers.withImageDrawable;
import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.action.ViewActions.clearText;
import static com.google.android.apps.common.testing.ui.espresso.assertion.ViewAssertions.matches;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;

/**
 * Created by dmadan on 6/12/14.
 */
public class CreditCardsInfoEditTest extends ActivityInstrumentationTestCase2<HotelPaymentOptionsActivity> {
	public CreditCardsInfoEditTest() {
		super(HotelPaymentOptionsActivity.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
		getActivity();
	}

	protected void testMethod(String[] prefixes, int length, int imageID, int repetitions)
		throws Exception {
		Random rand = new Random();
		int randomNumber;

		String creditcardNumber;

		for (int i = 0; i < prefixes.length; i++) {
			//check (repetition) # pseudo-random credit card numbers per card type
			for (int j = 0; j < repetitions; j++) {
				creditcardNumber = prefixes[i];
				for (int k = creditcardNumber.length(); k < length; k++) {
					randomNumber = rand.nextInt(10);
					creditcardNumber += randomNumber;
				}

				CardInfoScreen.typeTextCreditCardEditText(creditcardNumber);

				//compare credit card image displayed and desired image
				if (imageID != -1) {
					onView(withId(R.id.display_credit_card_brand_icon_white)).check(matches(withImageDrawable(imageID)));
				}
				CardInfoScreen.creditCardNumberEditText().perform(clearText());
			}
		}
	}

	///////////// Credit Card Logo Tests /////////////
	public void testVisa16() throws Exception {
		String[] prefixes = {"4"};
		testMethod(prefixes, 16, R.drawable.ic_visa_white, 5);
	}

	public void testVisa13() throws Exception {
		String[] prefixes = {"4"};
		testMethod(prefixes, 13, R.drawable.ic_visa_white, 5);
	}

	public void testMasterCard() throws Exception {
		String[] prefixes = {"51", "52", "53", "54", "55"};
		testMethod(prefixes, 16, R.drawable.ic_master_card_white, 5);
	}

	public void testMaestro16() throws Exception {
		String[] prefixes = {"50", "63", "67"};
		testMethod(prefixes, 16, R.drawable.ic_maestro_white, 5);
	}

	public void testMaestro18() throws Exception {
		String[] prefixes = {"50", "63", "67"};
		testMethod(prefixes, 18, R.drawable.ic_maestro_white, 5);
	}

	public void testMaestro19() throws Exception {
		String[] prefixes = {"50", "63", "67"};
		testMethod(prefixes, 19, R.drawable.ic_maestro_white, 5);
	}

	public void testDiscover() throws Exception {
		String[] prefixes = {"60"};
		testMethod(prefixes, 16, R.drawable.ic_discover_white, 5);
	}

	public void testDinersClub() throws Exception {
		String[] prefixes = {"30", "36", "38", "60"};
		testMethod(prefixes, 14, R.drawable.ic_diners_club_white, 5);
	}

	public void testChinaUnion16() throws Exception {
		String[] prefixes = {"62"};
		testMethod(prefixes, 16, R.drawable.ic_union_pay_white, 5);
	}

	public void testChinaUnion17() throws Exception {
		String[] prefixes = {"62"};
		testMethod(prefixes, 17, R.drawable.ic_union_pay_white, 5);
	}

	public void testChinaUnion18() throws Exception {
		String[] prefixes = {"62"};
		testMethod(prefixes, 18, R.drawable.ic_union_pay_white, 5);
	}

	public void testChinaUnion19() throws Exception {
		String[] prefixes = {"62"};
		testMethod(prefixes, 19, R.drawable.ic_union_pay_white, 5);
	}

	public void testCarteBlanche() throws Exception {
		String[] prefixes = {"94", "95"};
		testMethod(prefixes, 14, R.drawable.ic_carte_blanche_white, 5);
	}

	public void testAmex() throws Exception {
		String[] prefixes = {"34", "37"};
		testMethod(prefixes, 15, R.drawable.ic_amex_white, 5);
	}
}
