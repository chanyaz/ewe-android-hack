package com.expedia.bookings.test.ui.phone.tests.localization;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.IllegalFormatConversionException;
import java.util.Locale;
import java.util.Map;
import java.util.UnknownFormatConversionException;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.test.AndroidTestCase;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

public class LocalizationTests extends AndroidTestCase {

	private static final Locale[] TEST_LOCALES = new Locale[] {
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
			new Locale("th"),
			new Locale("nl"),
			new Locale("zh"),
			new Locale("en", "UK"),
			new Locale("zh", "HK"),
			new Locale("es"),
			new Locale("fr", "CA"),
	};

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

		String name = null;
		String val = null;
		for (Field f : res.getFields()) {
			try {
				name = f.getName();
				val = r.getString(f.getInt(null));
				base.put(name, val);
			}
			catch (NotFoundException e) {
				Log.w("Could not find id in English: " + name);
			}
			catch (Exception e) {
				Log.e("Something really bad happened in testStringLength()", e);
				fail("Sadness: " + e.getMessage());
			}
		}

		Log.i("Number of strings: " + base.size());

		mFailed = false;
		for (Locale locale : TEST_LOCALES) {
			setLocale(locale);

			String format = "\"%1$s\" in %2$s is %3$.2fx base length.  (\"%4$s\" to \"%5$s\")";
			for (Field f : res.getFields()) {
				name = f.getName();
				try {
					val = r.getString(f.getInt(null));
				}
				catch (Exception e) {
					Log.v("Could not get \"" + name + "\" in " + locale.toString());
					continue;
				}

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
					Log.v("Locale " + locale.toString() + " contains key not in base set: " + name);
					mFailed = true;
				}
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
			testString("prompt_select_child_age", 33, 65);
			testString("booking_info_template", "Chicago, IL", "Aug 24", "Aug 25, 2011");
			testString("NoGeocodingResults", "San Francisco");
			testString("sort_hotels_template", "Distance");
			testString("filter_distance_miles_template", 5);
			testString("filter_distance_kilometers_template", 5);
			testString("from_template", "$150");
			testString("percent_off_template", 15.24f);
			testString("map_snippet_price_template", "$3.50");
			testString("widget_savings_template", 30.24f);
			testString("min_room_price_template", "$125");
			testString("bed_type_start_value_template", "2 Kings Bed");
			testString("reviews_recommended_template", 10, 20);
			testString("user_review_recommendation_tag_text", 5, 10);
			testString("user_review_name_and_location_signature", "Daniel", "Minneapolis");
			testString("savings_template", 12.45);
			testString("value_add_template", "a new car!");
			testString("common_value_add_template", "Bees");
			testString("gallery_title_template", "Kwuality Inn");
			testString("room_rate_template", "10/24/2011");
			testString("charge_details_template", "$50.12");
			testString("invalid_currency_for_amex", "USD");
			testString("error_booking_succeeded_with_errors", "THE SERVER IS ON FIRE");
			testString("check_in_out_time_template", "3:00 PM", "Thursday");
			testString("name_template", "Dan", "Lew");
			testString("contact_phone_template", "12345680");
			testString("share_subject_template", "My Hotel", "3/21", "3/24");
			testString("default_point_of_sale_template", "CANADIA");
			testString("distance_template", "5", "miles");
			testString("distance_template_short", "2", "miles");

			testPlural("select_each_childs_age");
			testPlural("child_age");
			testPlural("number_of_matching_hotels");
			testPlural("number_of_results");
			testPlural("number_of_reviews");
			testPlural("staying_nights");
			testPlural("number_of_rooms_left");
			testPlural("length_of_stay");
			testPlural("number_of_adults");
			testPlural("number_of_children");
			testPlural("number_of_guests");
			testPlural("number_of_nights");

			// AndroidUtils
			testString("ts_minutes", 5);
			testString("ts_hours", 5);
			testString("ts_days", 5);
			testString("ts_months", 5);
			testString("ts_years", 5);
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
			fail("Something wonky happened in locale " + r.getConfiguration().locale + ": " + e.getMessage());
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
			fail("Something wonky happened in locale " + r.getConfiguration().locale);
		}
	}
}
