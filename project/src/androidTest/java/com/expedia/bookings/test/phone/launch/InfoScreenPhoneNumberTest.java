package com.expedia.bookings.test.phone.launch;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

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
import com.expedia.bookings.test.phone.pagemodels.common.InfoScreen;
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
public class InfoScreenPhoneNumberTest {

	private static final String TAG = InfoScreenPhoneNumberTest.class.getName();

	/*
	 *  #264 eb_tp test for Info screen phone numbers by POS and device (phone).
	 */

	//Launch directly into the About/Info Screen
	@Rule
	public IntentsTestRule<AboutActivity> intentRule = new IntentsTestRule<>(AboutActivity.class);

	@Rule
	public TestRule posRule = new TestRule() {
		@Override
		public Statement apply(final Statement base, org.junit.runner.Description description) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					PointOfSaleId[] pointsOfSale = new PointOfSaleId[] {
						PointOfSaleId.ARGENTINA,
						PointOfSaleId.AUSTRALIA,
						PointOfSaleId.AUSTRIA,
						PointOfSaleId.BELGIUM,
						PointOfSaleId.BRAZIL,
						PointOfSaleId.CANADA,
						PointOfSaleId.DENMARK,
						PointOfSaleId.FRANCE,
						PointOfSaleId.GERMANY,
						PointOfSaleId.HONG_KONG,
						PointOfSaleId.INDIA,
						PointOfSaleId.INDONESIA,
						PointOfSaleId.IRELAND,
						PointOfSaleId.ITALY,
						PointOfSaleId.JAPAN,
						PointOfSaleId.SOUTH_KOREA,
						PointOfSaleId.MALAYSIA,
						PointOfSaleId.MEXICO,
						PointOfSaleId.NETHERLANDS,
						PointOfSaleId.NEW_ZEALND,
						PointOfSaleId.NORWAY,
						PointOfSaleId.PHILIPPINES,
						PointOfSaleId.SINGAPORE,
						PointOfSaleId.SPAIN,
						PointOfSaleId.SWEDEN,
						PointOfSaleId.TAIWAN,
						PointOfSaleId.THAILAND,
						PointOfSaleId.UNITED_KINGDOM,
						PointOfSaleId.UNITED_STATES,
						PointOfSaleId.VIETNAM,
					};

					for (PointOfSaleId pos : pointsOfSale) {
						setPOS(pos);
						base.evaluate();
					}
				}
			};
		}

		private void setPOS(PointOfSaleId pos) {
			Log.v(TAG, "POS Set:" + pos.toString());
			SettingUtils.save(InstrumentationRegistry.getTargetContext(), R.string.PointOfSaleKey,
				String.valueOf(pos.getId()));
			PointOfSale.onPointOfSaleChanged(InstrumentationRegistry.getTargetContext());
		}
	};


	private org.hamcrest.Matcher<Uri> hasPhoneNumber(final String expected) {
		return new BaseMatcher<Uri>() {
			private String remembered;

			@Override
			public boolean matches(Object uriObject) {
				Uri intentUri = (Uri) uriObject;
				remembered = intentUri.toString().replaceAll("[^0-9]", "");
				return remembered.equals(expected);
			}

			@Override
			public void describeTo(Description description) {
				description
					.appendText("Expected phone number: ")
					.appendValue(expected)
					.appendText(", but got: ")
					.appendValue(remembered);
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

	@Test
	public void phoneNumbers() {
		InfoScreen.clickBookingSupport();
		InfoScreen.clickContactPhone();
		String supportNumber = PointOfSale.getPointOfSale().getSupportPhoneNumberBestForUser(Db.getUser());
		supportNumber = supportNumber.replaceAll("[^0-9]", "");
		intended(allOf(
			hasAction(Intent.ACTION_VIEW),
			hasData(hasPhoneNumber(supportNumber))));
	}
}
