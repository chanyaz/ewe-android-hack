package com.expedia.bookings.test.ui.phone.tests.ui;

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
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.pos.PointOfSaleId;
import com.expedia.bookings.test.ui.phone.pagemodels.common.InfoScreen;
import com.expedia.bookings.test.ui.tablet.pagemodels.Common;
import com.mobiata.android.util.SettingUtils;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class InfoScreenPhoneNumberTest {

	private static final String TAG = InfoScreenPhoneNumberTest.class.getName();

	/*
	 *  #264 eb_tp test for Info screen phone numbers by POS and device (phone).
	 */

	//Launch directly into the About/Info Screen
	@Rule
	public IntentsTestRule<AboutActivity> mRule = new IntentsTestRule<>(AboutActivity.class);

	private org.hamcrest.Matcher<Uri> hasPhoneNumber(final String number) {
		return new BaseMatcher<Uri>() {

			@Override
			public boolean matches(Object uriObject) {
				Uri intentUri = (Uri) uriObject;
				String intentNumber = intentUri.toString().replaceAll("[^0-9]", "");
				Common.enterLog(TAG, "Matching IntentURI (" + intentUri + ") with support number = " + number);
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

	private void clickBookingSupport() {
		InfoScreen.clickBookingSupport();
		InfoScreen.clickContactPhone();
	}

	private void clickAndVerifyNumber() {
		clickBookingSupport();
		String supportNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser());
		supportNumber = supportNumber.replaceAll("[^0-9]", "");
		intended(allOf(
			hasAction(Intent.ACTION_VIEW),
			hasData(hasPhoneNumber(supportNumber))));
	}

	@Test
	public void testPhoneArgentina() {
		setPOS(PointOfSaleId.ARGENTINA);
		Common.enterLog(TAG, "ARGENTINA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneAustralia() {
		setPOS(PointOfSaleId.AUSTRALIA);
		Common.enterLog(TAG, "AUSTRALIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneAustria() {
		setPOS(PointOfSaleId.AUSTRIA);
		Common.enterLog(TAG, "AUSTRIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneBelgium() {
		setPOS(PointOfSaleId.BELGIUM);
		Common.enterLog(TAG, "BELGIUM POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneBrazil() {
		setPOS(PointOfSaleId.BRAZIL);
		Common.enterLog(TAG, "BRAZIL POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneCanada() {
		setPOS(PointOfSaleId.CANADA);
		Common.enterLog(TAG, "CANADA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneDenmark() {
		setPOS(PointOfSaleId.DENMARK);
		Common.enterLog(TAG, "DENMARK POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneFrance() {
		setPOS(PointOfSaleId.FRANCE);
		Common.enterLog(TAG, "France POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneGermany() {
		setPOS(PointOfSaleId.GERMANY);
		Common.enterLog(TAG, "Germany POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneHongKong() {
		setPOS(PointOfSaleId.HONG_KONG);
		Common.enterLog(TAG, "HONG_KONG POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneIndia() {
		setPOS(PointOfSaleId.INDIA);
		Common.enterLog(TAG, "INDIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneIndonesia() {
		setPOS(PointOfSaleId.INDONESIA);
		Common.enterLog(TAG, "INDONESIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneIreland() {
		setPOS(PointOfSaleId.IRELAND);
		Common.enterLog(TAG, "IRELAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneItaly() {
		setPOS(PointOfSaleId.ITALY);
		Common.enterLog(TAG, "ITALY POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneJapan() {
		setPOS(PointOfSaleId.JAPAN);
		Common.enterLog(TAG, "Japan POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneKorea() {
		setPOS(PointOfSaleId.SOUTH_KOREA);
		Common.enterLog(TAG, "SOUTH_KOREA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneMalaysia() {
		setPOS(PointOfSaleId.MALAYSIA);
		Common.enterLog(TAG, "MALAYSIA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneMexico() {
		setPOS(PointOfSaleId.MEXICO);
		Common.enterLog(TAG, "MEXICO POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneNetherlands() {
		setPOS(PointOfSaleId.NETHERLANDS);
		Common.enterLog(TAG, "NETHERLANDS POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneNewZealand() {
		setPOS(PointOfSaleId.NEW_ZEALND);
		Common.enterLog(TAG, "NEW_ZEALND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneNorway() {
		setPOS(PointOfSaleId.NORWAY);
		Common.enterLog(TAG, "NORWAY POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhonePhilippines() {
		setPOS(PointOfSaleId.PHILIPPINES);
		Common.enterLog(TAG, "PHILIPPINES POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneSingapore() {
		setPOS(PointOfSaleId.SINGAPORE);
		Common.enterLog(TAG, "SINGAPORE POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneSpain() {
		setPOS(PointOfSaleId.SPAIN);
		Common.enterLog(TAG, "SPAIN POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneSweden() {
		setPOS(PointOfSaleId.SWEDEN);
		Common.enterLog(TAG, "SWEDEN POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneTaiwan() {
		setPOS(PointOfSaleId.TAIWAN);
		Common.enterLog(TAG, "TAIWAN POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneThailand() {
		setPOS(PointOfSaleId.THAILAND);
		Common.enterLog(TAG, "THAILAND POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneUnitedKingdom() {
		setPOS(PointOfSaleId.UNITED_KINGDOM);
		Common.enterLog(TAG, "UnitedKingdom POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneUSA() {
		setPOS(PointOfSaleId.UNITED_STATES);
		Common.enterLog(TAG, "USA POS Set");
		clickAndVerifyNumber();
	}

	@Test
	public void testPhoneVietnam() {
		setPOS(PointOfSaleId.VIETNAM);
		Common.enterLog(TAG, "VIETNAM POS Set");
		clickAndVerifyNumber();
	}

	public void setPOS(PointOfSaleId pos) {
		SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.PointOfSaleKey, String.valueOf(pos.getId()));
		PointOfSale.onPointOfSaleChanged(InstrumentationRegistry.getTargetContext());
	}
}
