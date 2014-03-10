package com.expedia.bookings.test.tests.ui;

import android.util.Pair;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;
import com.expedia.bookings.test.utils.CustomActivityInstrumentationTestCase;
import com.mobiata.android.util.SettingUtils;

/*
 * This test needs to be run against Production, so make sure
 * that your config.json has the server name variable as Production
 */

public class CreditCardErrorTest extends CustomActivityInstrumentationTestCase<SearchActivity> {

	private static final String TAG = CreditCardErrorTest.class.getSimpleName();

	public CreditCardErrorTest() {
		super(SearchActivity.class);
	}

	private static final Pair[] BAD_CREDIT_CARDS_PROD = {
		new Pair<String, Integer>("378734493671001", R.string.error_invalid_card_number), // AMEX
		new Pair<String, Integer>("94000000000001", R.string.error_invalid_card_number), // Carte Blanche
		new Pair<String, Integer>("30569309025905", R.string.error_invalid_card_number), // Diners Club
		new Pair<String, Integer>("6011000990139425", R.string.error_invalid_card_number), // Discover
		new Pair<String, Integer>("6867010054151583", R.string.error_invalid_card_number), // From test plan
		new Pair<String, Integer>("4321432143214321", R.string.error_invalid_card_number), // From test plan
	};

	private static final Pair[] BAD_CREDIT_CARDS_INTEGRATION = {
		new Pair<String, Integer>("4123456789012372", R.string.e3_error_checkout_payment_failed),
		new Pair<String, Integer>("5523456789012334", R.string.e3_error_checkout_payment_failed),
	};

	private void getToCheckout() throws Exception {
		mDriver.launchScreen().launchHotels();
		mUser.setHotelCityToRandomUSCity();
		mDriver.hotelsSearchScreen().clickSearchEditText();
		mDriver.hotelsSearchScreen().clickToClearSearchEditText();
		mDriver.enterLog(TAG, "Hotel search city is: " + mUser.getHotelSearchCity());
		mDriver.hotelsSearchScreen().enterSearchText(mUser.getHotelSearchCity());

		mDriver.hotelsSearchScreen().clickOnGuestsButton();
		mDriver.hotelsSearchScreen().guestPicker().clickOnSearchButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsSearchScreen().searchingForHotels());
		mDriver.hotelsSearchScreen().selectHotelFromList(1);
		mDriver.delay();
		mDriver.hotelsDetailsScreen().clickSelectButton();
		mDriver.waitForStringToBeGone(mDriver.hotelsRoomsRatesScreen().findingAvailableRooms());
		mDriver.hotelsRoomsRatesScreen().selectRoom(0);
		mDriver.waitForStringToBeGone(mDriver.hotelsCheckoutScreen().calculatingTaxesAndFees());
		mDriver.hotelsCheckoutScreen().clickCheckoutButton();
	}

	private void setUpTravelerInfo() {
		mDriver.hotelsCheckoutScreen().clickAddTravelerButton();
		mDriver.travelerInformationScreen().clickEnterANewTraveler();
		mDriver.travelerInformationScreen().enterFirstName(mUser.getFirstName());
		mDriver.travelerInformationScreen().enterLastName(mUser.getLastName());
		mDriver.travelerInformationScreen().enterPhoneNumber(mUser.getPhoneNumber());
		mDriver.travelerInformationScreen().enterEmailAddress(mUser.getLoginEmail());
		mDriver.travelerInformationScreen().clickDoneButton();
	}

	private void testCreditCardsAndTheirErrors(Pair[] cardsAndErrorID) throws Exception {
		mDriver.hotelsCheckoutScreen().clickSelectPaymentButton();
		mDriver.cardInfoScreen().clickOnExpirationDateButton();
		mDriver.cardInfoScreen().clickSetButton();
		mDriver.cardInfoScreen().typeTextPostalCode(mUser.getAddressPostalCode());
		mDriver.cardInfoScreen().typeTextNameOnCardEditText(mUser.getFirstName() + " " + mUser.getLastName());
		for (int i = 0; i < cardsAndErrorID.length; i++) {
			assertTrue(mDriver.searchText(mDriver.cardInfoScreen().cardInfo()));
			mDriver.cardInfoScreen().clearEditText(mDriver.cardInfoScreen().creditCardNumberEditText());
			mDriver.cardInfoScreen().typeTextCreditCardEditText((String) BAD_CREDIT_CARDS_PROD[i].first);
			mDriver.cardInfoScreen().clickOnDoneButton();
			mDriver.hotelsCheckoutScreen().slideToCheckout();
			mDriver.cvvEntryScreen().parseAndEnterCVV("1111");
			mDriver.cvvEntryScreen().clickBookButton();
			mDriver.waitForStringToBeGone(mDriver.cvvEntryScreen().booking());
			assertTrue(mDriver.searchText(getString((Integer) cardsAndErrorID[i].second)));
			mDriver.cvvEntryScreen().clickOkButton();
			mDriver.delay(1);
		}
	}

	public void testBadCreditCardsProduction() throws Exception {
		SettingUtils.save(this.getActivity().getApplicationContext(),
				mRes.getString(R.string.preference_which_api_to_use_key), "Production");
		getToCheckout();
		setUpTravelerInfo();
		testCreditCardsAndTheirErrors(BAD_CREDIT_CARDS_PROD);
	}

	public void testBadCreditCardsIntegration() throws Exception {
		SettingUtils.save(this.getActivity().getApplicationContext(),
				mRes.getString(R.string.preference_which_api_to_use_key), "Integration");
		getToCheckout();
		setUpTravelerInfo();
		testCreditCardsAndTheirErrors(BAD_CREDIT_CARDS_INTEGRATION);
	}
}
