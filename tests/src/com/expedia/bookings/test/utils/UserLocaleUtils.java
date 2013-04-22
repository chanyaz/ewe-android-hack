package com.expedia.bookings.test.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.test.ActivityInstrumentationTestCase2;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity;

public class UserLocaleUtils extends ActivityInstrumentationTestCase2<SearchActivity> {

	private Resources mRes;

	public UserLocaleUtils(Resources res) {
		super("com.expedia.bookings", SearchActivity.class);
		mRes = res;
	}

	////////////////////////////////////////////////////////
	//// Lists of Locales
	public static Locale[] AMERICAN_LOCALES = new Locale[] {
			new Locale("es", "AR"),
			new Locale("pt", "BR"),
			new Locale("en", "CA"),
			new Locale("fr", "CA"),
			new Locale("es", "MX"),
			new Locale("en", "US"),
	};

	public static Locale[] APAC_LOCALES = new Locale[] {
			new Locale("en", "HK"),
			new Locale("zh", "HK"),
			new Locale("id", "ID"),
			new Locale("en", "IN"),
			new Locale("ja", "JP"),
			new Locale("ko", "KR"),
			new Locale("en", "MY"),
			new Locale("ms", "MY"),
			new Locale("en", "PH"),
			new Locale("en", "SG"),
			new Locale("th", "TH"),
			new Locale("en", "TW"),
			new Locale("zh", "TW"),
			new Locale("vi", "VN"),
			new Locale("tl", "PH"),
			new Locale("zh", "CN"),
	};

	public static Locale[] WESTERN_LOCALES = new Locale[] {
			new Locale("de", "AT"),
			new Locale("en", "AU"),
			new Locale("fr", "BE"),
			new Locale("nl", "BE"),
			new Locale("de", "DE"),
			new Locale("da", "DK"),
			new Locale("es", "ES"),
			new Locale("fr", "FR"),
			new Locale("en", "IE"),
			new Locale("it", "IT"),
			new Locale("nl", "NL"),
			new Locale("nb", "NO"),
			new Locale("en", "NZ"),
			new Locale("sv", "SE"),
			new Locale("en", "UK"),
	};

	public static Locale[] FLIGHTS_LOCALES = new Locale[] {
			AMERICAN_LOCALES[2], //en_CA
			AMERICAN_LOCALES[3], //fr_CA
			AMERICAN_LOCALES[5], //en_US
			WESTERN_LOCALES[9],  //it_IT
			WESTERN_LOCALES[7],  //fr_FR
			WESTERN_LOCALES[4],  //de_DE
			WESTERN_LOCALES[14], //en_UK
			WESTERN_LOCALES[1],  //en_AU
			WESTERN_LOCALES[12], //en_NZ
			WESTERN_LOCALES[11], //nb_NO
			WESTERN_LOCALES[5],  //da_DK
			WESTERN_LOCALES[2],  //fr_BE
			WESTERN_LOCALES[3],  //nl_BE
			WESTERN_LOCALES[8],  //en_IE
			APAC_LOCALES[4],     //ja_JP
	};

	public static final Map<Locale, Integer> LOCALE_TO_COUNTRY = new HashMap<Locale, Integer>();
	static {
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[0], R.string.country_ar);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[1], R.string.country_br);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[2], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[3], R.string.country_ca);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[4], R.string.country_mx);
		LOCALE_TO_COUNTRY.put(AMERICAN_LOCALES[5], R.string.country_us);

		LOCALE_TO_COUNTRY.put(APAC_LOCALES[0], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[1], R.string.country_hk);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[2], R.string.country_id);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[3], R.string.country_in);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[4], R.string.country_jp);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[5], R.string.country_kr);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[6], R.string.country_my);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[7], R.string.country_my);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[8], R.string.country_ph);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[9], R.string.country_sg);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[10], R.string.country_th);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[11], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[12], R.string.country_tw);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[13], R.string.country_vn);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[14], R.string.country_ph);
		LOCALE_TO_COUNTRY.put(APAC_LOCALES[15], R.string.country_tw);

		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[0], R.string.country_at);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[1], R.string.country_au);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[2], R.string.country_be);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[3], R.string.country_be);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[4], R.string.country_de);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[5], R.string.country_dk);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[6], R.string.country_es);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[7], R.string.country_fr);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[8], R.string.country_ie);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[9], R.string.country_it);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[10], R.string.country_nl);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[11], R.string.country_no);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[12], R.string.country_nz);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[13], R.string.country_se);
		LOCALE_TO_COUNTRY.put(WESTERN_LOCALES[14], R.string.country_gb);
	}

	////////////////////////////////////////////////////////////////
	// Setting Locale
	
	// Select a random locale from the array of locales given
	public Locale setRandomLocale(Locale[] localeList) {
		Random rand = new Random();
		int localeIndex = rand.nextInt(localeList.length);
		Locale selectedLocale = localeList[localeIndex];
		setLocale(selectedLocale);
		
		return selectedLocale;
	}

	public void setLocale(Locale locale) {
		Configuration config = mRes.getConfiguration(); //get current configuration
		config.locale = locale; //set to locale specified
		Locale.setDefault(locale);
		mRes.updateConfiguration(config, mRes.getDisplayMetrics());
	}

}
