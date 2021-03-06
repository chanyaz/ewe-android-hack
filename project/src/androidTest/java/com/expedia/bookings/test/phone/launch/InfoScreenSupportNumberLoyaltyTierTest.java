package com.expedia.bookings.test.phone.launch;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AboutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.phone.pagemodels.common.InfoScreen;
import com.expedia.bookings.test.espresso.Common;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class InfoScreenSupportNumberLoyaltyTierTest {

	private static final String TAG = InfoScreenSupportNumberLoyaltyTierTest.class.getName();
	private String supportNumber;

	/*
	 *  #615. eb_tp test for Info screen phone numbers by POS and Loyalty Membership Tier (phone).
	 */

	@Rule
	public IntentsTestRule<AboutActivity> mRule = new IntentsTestRule<>(AboutActivity.class);

	private org.hamcrest.Matcher<Uri> hasPhoneNumber(final String number) {
		return new BaseMatcher<Uri>() {

			@Override
			public boolean matches(Object uriObject) {
				Uri intentUri = (Uri) uriObject;
				String intentNumber = intentUri.toString().replaceAll("[^0-9]", "");
				Log.v(TAG, "Matching IntentURI (" + intentUri + ") with support number = " + number);
				return intentNumber.equals(number);
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Booking Support number should be = ").appendValue(number);
			}

		};
	}

	@Before
	public void stubAllExternalIntents() {
		// By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
		// every test run. In this case all external Intents will be blocked.
		intending(not(isInternal()))
			.respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
	}

	public void setPOSUserTier(PointOfSaleId pos, Traveler.LoyaltyMembershipTier tier) {
		SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.PointOfSaleKey, String.valueOf(pos.getId()));
		PointOfSale.onPointOfSaleChanged(InstrumentationRegistry.getTargetContext());

		User testUser = new User();
		Traveler primaryTraveler = new Traveler();
		primaryTraveler.setLoyaltyMembershipTier(tier);
		primaryTraveler.setLoyaltyMembershipActive(true);
		testUser.setPrimaryTraveler(primaryTraveler);
		supportNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(testUser);
		supportNumber = supportNumber.replaceAll("[^0-9]", "");
		Db.setUser(testUser);
		testUser.save(InstrumentationRegistry.getTargetContext());
		Log.v(TAG, "LoyaltyMembership Tier Set to " + tier);

		mRule.getActivity();
		Common.delay(2);
	}

	private void clickAndVerifyNumber() {
		InfoScreen.clickBookingSupport();
		InfoScreen.clickContactPhone();
		intended(allOf(
			hasAction(Intent.ACTION_VIEW),
			hasData(hasPhoneNumber(supportNumber))));
	}

	@Test
	public void testAustraliaGold() {
		setPOSUserTier(PointOfSaleId.AUSTRALIA, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "AUSTRALIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testAustraliaSilver() {
		setPOSUserTier(PointOfSaleId.AUSTRALIA, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "AUSTRALIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNetherlandsGold() {
		setPOSUserTier(PointOfSaleId.NETHERLANDS, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "NETHERLANDS POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNetherlandsSilver() {
		setPOSUserTier(PointOfSaleId.NETHERLANDS, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "NETHERLANDS POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNewZealandGold() {
		setPOSUserTier(PointOfSaleId.NEW_ZEALND, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "NEW_ZEALAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNewZealandSilver() {
		setPOSUserTier(PointOfSaleId.NEW_ZEALND, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "NEW_ZEALAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMexicoGold() {
		setPOSUserTier(PointOfSaleId.MEXICO, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "MEXICO POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMexicoSilver() {
		setPOSUserTier(PointOfSaleId.MEXICO, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "MEXICO POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testJapanGold() {
		setPOSUserTier(PointOfSaleId.JAPAN, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "Japan POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testJapanSilver() {
		setPOSUserTier(PointOfSaleId.JAPAN, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "Japan POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSingaporeGold() {
		setPOSUserTier(PointOfSaleId.SINGAPORE, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "Singapore POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSingaporeSilver() {
		setPOSUserTier(PointOfSaleId.SINGAPORE, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "Singapore POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSpainGold() {
		setPOSUserTier(PointOfSaleId.SPAIN, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "Spain POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSpainSilver() {
		setPOSUserTier(PointOfSaleId.SPAIN, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "Spain POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNorwayGold() {
		setPOSUserTier(PointOfSaleId.NORWAY, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "Norway POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNorwaySilver() {
		setPOSUserTier(PointOfSaleId.NORWAY, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "Norway POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSwedenGold() {
		setPOSUserTier(PointOfSaleId.SWEDEN, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "Sweden POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSwedenSilver() {
		setPOSUserTier(PointOfSaleId.SWEDEN, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "Sweden POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testHongKongGold() {
		setPOSUserTier(PointOfSaleId.HONG_KONG, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "Hong Kong POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testHongKongSilver() {
		setPOSUserTier(PointOfSaleId.HONG_KONG, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "Hong Kong POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBrazilGold() {
		setPOSUserTier(PointOfSaleId.BRAZIL, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "BRAZIL POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBrazilSilver() {
		setPOSUserTier(PointOfSaleId.BRAZIL, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "BRAZIL POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testThailandGold() {
		setPOSUserTier(PointOfSaleId.THAILAND, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "THAILAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testThailandSilver() {
		setPOSUserTier(PointOfSaleId.THAILAND, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "THAILAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testIrelandGold() {
		setPOSUserTier(PointOfSaleId.IRELAND, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "IRELAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testIrelandSilver() {
		setPOSUserTier(PointOfSaleId.IRELAND, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "IRELAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testDenmarkGold() {
		setPOSUserTier(PointOfSaleId.DENMARK, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "DENMARK POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testDenmarkSilver() {
		setPOSUserTier(PointOfSaleId.DENMARK, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "DENMARK POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBelgiumGold() {
		setPOSUserTier(PointOfSaleId.BELGIUM, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "BELGIUM POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBelgiumSilver() {
		setPOSUserTier(PointOfSaleId.BELGIUM, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "BELGIUM POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMalaysiaGold() {
		setPOSUserTier(PointOfSaleId.MALAYSIA, Traveler.LoyaltyMembershipTier.GOLD);
		Log.v(TAG, "MALAYSIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMalaysiaSilver() {
		setPOSUserTier(PointOfSaleId.MALAYSIA, Traveler.LoyaltyMembershipTier.SILVER);
		Log.v(TAG, "MALAYSIA POS Set");
		clickAndVerifyNumber();
	}
}
