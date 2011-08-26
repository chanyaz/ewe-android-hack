package com.expedia.bookings.test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Locale;
import java.util.Map;
import java.util.UnknownFormatConversionException;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

public class LocalizationTests extends AndroidTestCase {

	private Locale[] TEST_LOCALES = new Locale[] {
			new Locale("en", "US"),
			new Locale("zh", "TW"),
			new Locale("da"),
			new Locale("de"),
			new Locale("fr"),
			new Locale("it"),
			new Locale("jp"),
			new Locale("ko"),
			new Locale("no"),
			new Locale("pt"),
			new Locale("sv"),
			new Locale("in"),
			new Locale("ms"),
			new Locale("tl"),
			new Locale("zh"),
			new Locale("en", "UK"),
			new Locale("zh", "HK"),
			new Locale("es"),
			new Locale("fr", "CA"),
	};

	private static final double PERCENT_LARGER_CUTOFF = 1.5;

	private Resources r;
	DisplayMetrics metrics;
	private boolean mFailed;

	public LocalizationTests() {
		// Default constructor
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Log.configureLogging("ExpediaBookings", true);

		r = getContext().getResources();
		metrics = r.getDisplayMetrics();
	}

	//////////////////////////////////////////////////////////////////////////
	// Tools

	private void setLocale(Locale locale) {
		Configuration config = r.getConfiguration();
		config.locale = locale;
		r.updateConfiguration(config, metrics);
	}

	//////////////////////////////////////////////////////////////////////////
	// Tests

	public void testStringLength() {
		// Load a table of the strings in English (for comparison)
		setLocale(new Locale("en", "US"));

		Map<String, String> base = new HashMap<String, String>();
		Class<?> res = R.string.class;
		try {
			for (Field f : res.getFields()) {
				String name = f.getName();
				String val = r.getString(f.getInt(null));
				base.put(name, val);
			}
		}
		catch (Exception e) {
			fail("Something wonky happened: " + e.getMessage());
		}

		Log.i("Number of strings: " + base.size());

		mFailed = false;
		for (Locale locale : TEST_LOCALES) {
			setLocale(locale);

			String format = "\"%1$s\" in %2$s is %3$.2fx base length.  (\"%4$s\" to \"%5$s\")";
			try {
				for (Field f : res.getFields()) {
					String name = f.getName();
					String val = r.getString(f.getInt(null));

					if (base.containsKey(name)) {
						String baseStr = base.get(name);
						double percent = val.length() / (double) baseStr.length();

						if (percent > 3) {
							Log.e("EXTREME: " + String.format(format, name, locale.toString(), percent, baseStr, val));
							mFailed = true;
						}
						else if (percent > 2) {
							Log.w("LONG: " + String.format(format, name, locale.toString(), percent, baseStr, val));
						}
						else if (percent > 1.5) {
							Log.i("MED: " + String.format(format, name, locale.toString(), percent, baseStr, val));
						}
					}
					else {
						Log.w("Locale " + locale.toString() + " contains key not in base set: " + name);
						mFailed = true;
					}
				}
			}
			catch (Exception e) {
				fail("Something wonky happened: " + e.getMessage());
			}
		}

		if (mFailed) {
			fail("Some strings failed the test.");
		}
	}

	public void testStringFormatting() {
		mFailed = false;
		for (Locale locale : TEST_LOCALES) {
			setLocale(locale);

			// ExpediaBookings
			testPlural("number_of_adults");
			testPlural("number_of_children");
			testPlural("length_of_stay");
			testString("booking_info_template", "Chicago, IL", "Aug", 24, "Aug", 25, 2011);
			testString("price_range_template", "$50", "$60");
			testString("from_template", "$150");
			testString("rooms_left_template", 4);
			testString("savings_template", 12.45);
			testString("room_rate_template", "10/24/2011");
			testString("charge_details_template", "$50.12");
			testString("invalid_currency_for_amex", "USD");
			testString("contact_phone_template", "12345680");
			testString("contact_phone_china_template", "12345680", "12345680");
			testString("contact_phone_default_template", "12345680", "12345680");
			testString("share_subject_template", "My Hotel", "3/21", "3/24");
			testString("preference_currency_summary_template", "US Dollars", "USD");
			testString("default_currency_template", "USD");

			// HotelLib
			testString("AdultsTemplate", 2);
			testString("ChildrenTemplate", 2);
			testString("NoGeocodingResults", "San Francisco");
			testString("LatLonSearchTemplate", 12.14242, -124.445);
			testString("map_snippet_price_template", "$3.50");
			testString("RatePerNightTemplate", "$179");
			testString("RateAveragePerNightTemplate", "$179");
			testString("or_template", "apple", "orange");
			testString("or_short_template", "apple", "orange");
			testString("BillingTitleTemplate", "HotelPal", "Contact Info", 1);
			testString("RequiredFieldTemplate", "Telephone");
			testString("InvalidFieldTemplate", "email");
			testString("distance_template", "5", "miles");
			testString("NextTemplate", "Address");

			// AndroidUtils
			testString("MailChimpFailureReasonTemplate", "Failure reason");
		}

		if (mFailed) {
			fail("Some strings failed the test.");
		}
	}

	private void testString(String stringName, Object... args) {
		try {
			Class<?> res = R.string.class;
			Field field = res.getField(stringName);
			int stringId = field.getInt(null);
			r.getString(stringId, args);
		}
		catch (UnknownFormatConversionException e) {
			Log.e("FAILURE: \"" + stringName + "\" in " + r.getConfiguration().locale);
			mFailed = true;
		}
		catch (IllegalFormatConversionException e) {
			Log.e("FAILURE: \"" + stringName + "\" in " + r.getConfiguration().locale);
			mFailed = true;
		}
		catch (Exception e) {
			fail("Something wonky happened: " + e.getMessage());
		}
	}

	private void testPlural(String pluralName) {
		int a = -1;
		try {
			Class<?> res = R.plurals.class;
			Field field = res.getField(pluralName);
			int stringId = field.getInt(null);
			for (; a < 10; a++) {
				r.getQuantityString(stringId, a, a);
			}
		}
		catch (UnknownFormatConversionException e) {
			Log.e("FAILURE: \"" + pluralName + "\" in " + r.getConfiguration().locale + ", at quantity " + a);
			mFailed = true;
		}
		catch (Exception e) {
			fail("Something wonky happened");
		}
	}
}
