package com.expedia.bookings.test.phone.profile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.AccountSettingsActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LoyaltyMembershipTier;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.espresso.Common;
import com.expedia.bookings.test.phone.pagemodels.common.ProfileScreen;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static com.expedia.bookings.test.espresso.CustomMatchers.hasPhoneNumber;
import static com.expedia.bookings.test.espresso.EspressoUtils.assertIntentFiredToViewUri;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class ProfileScreenSupportNumberLoyaltyTierTest {

	private static final String TAG = ProfileScreenSupportNumberLoyaltyTierTest.class.getName();
	private String supportNumber;

	/*
	 *  #615. eb_tp test for Info screen phone numbers by POS and Loyalty Membership Tier (phone).
	 */

	@Rule
	public IntentsTestRule<AccountSettingsActivity> mRule = new IntentsTestRule<>(AccountSettingsActivity.class);

	@Before
	public void stubAllExternalIntents() {
		// By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
		// every test run. In this case all external Intents will be blocked.
		intending(not(isInternal()))
			.respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
	}

	public void setPOSUserTier(PointOfSaleId pos, LoyaltyMembershipTier tier) {
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
		ProfileScreen.clickBookingSupport();
		ProfileScreen.clickContactPhone();
		assertIntentFiredToViewUri(hasPhoneNumber(supportNumber));
	}

	@Test
	public void testAustraliaGold() {
		setPOSUserTier(PointOfSaleId.AUSTRALIA, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "AUSTRALIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testAustraliaSilver() {
		setPOSUserTier(PointOfSaleId.AUSTRALIA, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "AUSTRALIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNetherlandsGold() {
		setPOSUserTier(PointOfSaleId.NETHERLANDS, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "NETHERLANDS POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNetherlandsSilver() {
		setPOSUserTier(PointOfSaleId.NETHERLANDS, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "NETHERLANDS POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNewZealandGold() {
		setPOSUserTier(PointOfSaleId.NEW_ZEALND, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "NEW_ZEALAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNewZealandSilver() {
		setPOSUserTier(PointOfSaleId.NEW_ZEALND, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "NEW_ZEALAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMexicoGold() {
		setPOSUserTier(PointOfSaleId.MEXICO, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "MEXICO POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMexicoSilver() {
		setPOSUserTier(PointOfSaleId.MEXICO, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "MEXICO POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testJapanGold() {
		setPOSUserTier(PointOfSaleId.JAPAN, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "Japan POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testJapanSilver() {
		setPOSUserTier(PointOfSaleId.JAPAN, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "Japan POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSingaporeGold() {
		setPOSUserTier(PointOfSaleId.SINGAPORE, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "Singapore POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSingaporeSilver() {
		setPOSUserTier(PointOfSaleId.SINGAPORE, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "Singapore POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSpainGold() {
		setPOSUserTier(PointOfSaleId.SPAIN, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "Spain POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSpainSilver() {
		setPOSUserTier(PointOfSaleId.SPAIN, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "Spain POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNorwayGold() {
		setPOSUserTier(PointOfSaleId.NORWAY, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "Norway POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testNorwaySilver() {
		setPOSUserTier(PointOfSaleId.NORWAY, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "Norway POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSwedenGold() {
		setPOSUserTier(PointOfSaleId.SWEDEN, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "Sweden POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testSwedenSilver() {
		setPOSUserTier(PointOfSaleId.SWEDEN, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "Sweden POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testHongKongGold() {
		setPOSUserTier(PointOfSaleId.HONG_KONG, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "Hong Kong POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testHongKongSilver() {
		setPOSUserTier(PointOfSaleId.HONG_KONG, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "Hong Kong POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBrazilGold() {
		setPOSUserTier(PointOfSaleId.BRAZIL, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "BRAZIL POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBrazilSilver() {
		setPOSUserTier(PointOfSaleId.BRAZIL, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "BRAZIL POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testThailandGold() {
		setPOSUserTier(PointOfSaleId.THAILAND, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "THAILAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testThailandSilver() {
		setPOSUserTier(PointOfSaleId.THAILAND, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "THAILAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testIrelandGold() {
		setPOSUserTier(PointOfSaleId.IRELAND, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "IRELAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testIrelandSilver() {
		setPOSUserTier(PointOfSaleId.IRELAND, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "IRELAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testDenmarkGold() {
		setPOSUserTier(PointOfSaleId.DENMARK, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "DENMARK POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testDenmarkSilver() {
		setPOSUserTier(PointOfSaleId.DENMARK, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "DENMARK POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBelgiumGold() {
		setPOSUserTier(PointOfSaleId.BELGIUM, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "BELGIUM POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testBelgiumSilver() {
		setPOSUserTier(PointOfSaleId.BELGIUM, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "BELGIUM POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMalaysiaGold() {
		setPOSUserTier(PointOfSaleId.MALAYSIA, LoyaltyMembershipTier.TOP);
		Log.v(TAG, "MALAYSIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testMalaysiaSilver() {
		setPOSUserTier(PointOfSaleId.MALAYSIA, LoyaltyMembershipTier.MIDDLE);
		Log.v(TAG, "MALAYSIA POS Set");
		clickAndVerifyNumber();
	}
}
