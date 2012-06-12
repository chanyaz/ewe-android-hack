package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;
import android.content.Intent;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.mobiata.android.Log;
import com.mobiata.android.util.ResourceUtils;
import com.mobiata.android.util.SettingUtils;

@SuppressWarnings("serial")
public class LocaleUtils {

	public static final String ACTION_POS_CHANGED = "com.expedia.bookings.action.pos_changed";

	private static final Map<String, Integer> POINT_OF_SALE_RES_ID = new HashMap<String, Integer>() {
		{
			put("US", R.string.point_of_sale_us);
			put("GB", R.string.point_of_sale_uk);
			put("AU", R.string.point_of_sale_au);
			put("FR", R.string.point_of_sale_fr);
			put("DE", R.string.point_of_sale_de);
			put("IT", R.string.point_of_sale_it);
			put("NL", R.string.point_of_sale_nl);
			put("ES", R.string.point_of_sale_es);
			put("NO", R.string.point_of_sale_no);
			put("DK", R.string.point_of_sale_dk);
			put("SE", R.string.point_of_sale_se);
			put("IE", R.string.point_of_sale_ie);
			put("BE", R.string.point_of_sale_be);
			put("CA", R.string.point_of_sale_ca);
			put("NZ", R.string.point_of_sale_nz);
			put("JP", R.string.point_of_sale_jp);
			put("MX", R.string.point_of_sale_mx);
			put("SG", R.string.point_of_sale_sg);
			put("MY", R.string.point_of_sale_my);
			put("KR", R.string.point_of_sale_kr);
			put("TH", R.string.point_of_sale_th);
			put("PH", R.string.point_of_sale_ph);
			put("ID", R.string.point_of_sale_id);
			put("BR", R.string.point_of_sale_br);
			put("HK", R.string.point_of_sale_hk);
			put("TW", R.string.point_of_sale_tw);
			put("VN", R.string.point_of_sale_vn);
			put("AR", R.string.point_of_sale_ar);
			put("AT", R.string.point_of_sale_at);
			put("IN", R.string.point_of_sale_in);
		}
	};

	private static final Map<Integer, Integer> POINT_OF_SALE_COUNTRY = new HashMap<Integer, Integer>() {
		{
			put(R.string.point_of_sale_us, R.string.country_us);
			put(R.string.point_of_sale_uk, R.string.country_gb);
			put(R.string.point_of_sale_au, R.string.country_au);
			put(R.string.point_of_sale_fr, R.string.country_fr);
			put(R.string.point_of_sale_de, R.string.country_de);
			put(R.string.point_of_sale_it, R.string.country_it);
			put(R.string.point_of_sale_nl, R.string.country_nl);
			put(R.string.point_of_sale_es, R.string.country_es);
			put(R.string.point_of_sale_no, R.string.country_no);
			put(R.string.point_of_sale_dk, R.string.country_dk);
			put(R.string.point_of_sale_se, R.string.country_se);
			put(R.string.point_of_sale_ie, R.string.country_ie);
			put(R.string.point_of_sale_be, R.string.country_be);
			put(R.string.point_of_sale_ca, R.string.country_ca);
			put(R.string.point_of_sale_nz, R.string.country_nz);
			put(R.string.point_of_sale_jp, R.string.country_jp);
			put(R.string.point_of_sale_mx, R.string.country_mx);
			put(R.string.point_of_sale_sg, R.string.country_sg);
			put(R.string.point_of_sale_my, R.string.country_my);
			put(R.string.point_of_sale_kr, R.string.country_kr);
			put(R.string.point_of_sale_th, R.string.country_th);
			put(R.string.point_of_sale_ph, R.string.country_ph);
			put(R.string.point_of_sale_id, R.string.country_id);
			put(R.string.point_of_sale_br, R.string.country_br);
			put(R.string.point_of_sale_hk, R.string.country_hk);
			put(R.string.point_of_sale_tw, R.string.country_tw);
			put(R.string.point_of_sale_vn, R.string.country_vn);
			put(R.string.point_of_sale_ar, R.string.country_ar);
			put(R.string.point_of_sale_at, R.string.country_at);
			put(R.string.point_of_sale_in, R.string.country_in);
		}
	};

	public static final Map<String, String> LANGUAGE_CODE_TO_CONTENT_LOCALE = new HashMap<String, String>() {
		{
			put("da", "da,da_DK");
			put("de", "de_DE, de_AT");
			put("en", "en,en_US,en_AU,en_CA,en_GB,en_ID,en_IE,en_IN,en_MY,en_NZ,en_PH,en_SG");
			put("es", "es,es_ES,es_AR,es_MX");
			put("fr", "fr,fr_FR,fr_BE,fr_CA");
			put("id", "id,id_ID");
			put("it", "it,it_IT");
			put("ja", "ja,ja_JP");
			put("ko", "ko,ko_KR");
			put("ms", "ms,ms_MY");
			put("nl", "nl,nl_NL,nl_BE");
			put("no", "no,no_NO");
			put("pt", "pt,pt_BR");
			put("sv", "sv,sv_SE");
			put("th", "th,th_TH");
			put("zh", "zh,zh_HK,zh_TW");
		}
	};

	private static final Map<String, LinkedList<String>> LOCALE_TO_EXPEDIA_PRIORITY_LIST = new HashMap<String, LinkedList<String>>() {
		{
			put("da_DK", new LinkedList<String>(Arrays.asList("da", "en")));
			put("de_AT", new LinkedList<String>(Arrays.asList("de", "en")));
			put("de_DE", new LinkedList<String>(Arrays.asList("de", "en")));
			put("en_CA", new LinkedList<String>(Arrays.asList("en", "fr")));
			put("en_ID", new LinkedList<String>(Arrays.asList("en", "id")));
			put("en_MY", new LinkedList<String>(Arrays.asList("en", "ms")));
			put("es_AR", new LinkedList<String>(Arrays.asList("es", "pt", "en")));
			put("es_ES", new LinkedList<String>(Arrays.asList("es", "en")));
			put("es_MX", new LinkedList<String>(Arrays.asList("es", "en")));
			put("fr_BE", new LinkedList<String>(Arrays.asList("fr", "en")));
			put("fr_CA", new LinkedList<String>(Arrays.asList("fr", "en")));
			put("fr_FR", new LinkedList<String>(Arrays.asList("fr", "en")));
			put("id_ID", new LinkedList<String>(Arrays.asList("id", "en")));
			put("it_IT", new LinkedList<String>(Arrays.asList("it", "en")));
			put("ja_JP", new LinkedList<String>(Arrays.asList("ja")));
			put("ko_KR", new LinkedList<String>(Arrays.asList("ko", "en")));
			put("ms_MY", new LinkedList<String>(Arrays.asList("ms", "en")));
			put("nl_BE", new LinkedList<String>(Arrays.asList("nl", "en")));
			put("nl_NL", new LinkedList<String>(Arrays.asList("nl", "en")));
			put("no_NO", new LinkedList<String>(Arrays.asList("no", "en")));
			put("pt_BR", new LinkedList<String>(Arrays.asList("pt", "es", "en")));
			put("sv_SE", new LinkedList<String>(Arrays.asList("sv", "en")));
			put("th_TH", new LinkedList<String>(Arrays.asList("th", "en")));
			put("zh_HK", new LinkedList<String>(Arrays.asList("zh", "en")));
			put("zh_TW", new LinkedList<String>(Arrays.asList("zh", "en")));

			// All other en displays: All English
			put("en_US", new LinkedList<String>(Arrays.asList("en")));
			put("en_GB", new LinkedList<String>(Arrays.asList("en")));
			put("en_PH", new LinkedList<String>(Arrays.asList("en")));
			put("en_SG", new LinkedList<String>(Arrays.asList("en")));
			put("en_NZ", new LinkedList<String>(Arrays.asList("en")));
			put("en_IN", new LinkedList<String>(Arrays.asList("en")));
			put("en_IE", new LinkedList<String>(Arrays.asList("en")));
			put("en_AU", new LinkedList<String>(Arrays.asList("en")));

		}
	};

	public static String getDefaultPointOfSale(Context context) {
		Locale locale = Locale.getDefault();
		String country = locale.getCountry();
		int resId = POINT_OF_SALE_RES_ID.containsKey(country) ? POINT_OF_SALE_RES_ID.get(country)
				: R.string.point_of_sale_uk;
		return context.getString(resId);
	}

	public static int getDefaultCountryResId(Context context) {
		Locale locale = Locale.getDefault();
		String country = locale.getCountry();
		int resId = POINT_OF_SALE_RES_ID.containsKey(country) ? POINT_OF_SALE_RES_ID.get(country)
				: R.string.point_of_sale_uk;

		return POINT_OF_SALE_COUNTRY.get(resId);
	}

	private static String sCachedPointOfSale;

	/**
	 * Gets the current POS.  By providing the context, you refresh the POS cache and guarantee that
	 * you have the correct POS returend.
	 */
	public static String getPointOfSale(Context context) {
		sCachedPointOfSale = SettingUtils.get(context, context.getString(R.string.PointOfSaleKey),
				getDefaultPointOfSale(context));
		return sCachedPointOfSale;
	}

	/**
	 * This returns the last known point of sale.  It is available so you can get the POS without
	 * having to pass around a Context.  Only works if you call onPointOfSaleChanged() whenever
	 * the POS changes.
	 */
	public static String getPointOfSale() {
		if (sCachedPointOfSale == null) {
			throw new RuntimeException("getLastPointOfSale() called before POS filled in by system");
		}
		return sCachedPointOfSale;
	}

	/**
	 * Call this when the POS has changed.
	 */
	public static void onPointOfSaleChanged(Context context) {
		Log.i("onPointOfSaleChanged() called");

		// Update the cache
		getPointOfSale(context);

		// clear all data
		Db.clear();

		// Notify app of POS change
		Intent intent = new Intent(ACTION_POS_CHANGED);
		context.sendBroadcast(intent);
	}

	/**
	 * Returns the language id for the POS, if it is needed.  (Returns null for non-dual-language POSes). 
	 */
	public static String getDualLanguageId(Context context) {
		String pos = getPointOfSale(context);
		String langId = Locale.getDefault().getLanguage().toLowerCase();
		if (pos.equals(context.getString(R.string.point_of_sale_be))) {
			if (langId.equals("nl")) {
				return "1043";
			}
			else if (langId.equals("fr")) {
				return "1036";
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ca))) {
			if (langId.equals("en")) {
				return "4105";
			}
			else if (langId.equals("fr")) {
				return "3084";
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_hk))) {
			if (langId.equals("en")) {
				return "2057";
			}
			else if (langId.equals("zh")) {
				return "3076";
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_my))) {
			if (langId.equals("en")) {
				return "2057";
			}
			else if (langId.equals("ms")) {
				return "1086";
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ph))) {
			if (langId.equals("en")) {
				return "13321";
			}
			else if (langId.equals("tl")) {
				return "1124";
			}
		}

		// Not a dual-langauge POS or no valid language found, return null
		return null;
	}

	private static Map<String, String> sTPIDs;

	public static String getTPID(Context context) {
		if (sTPIDs == null) {
			sTPIDs = ResourceUtils.getStringMap(context, R.array.tpid_map);
		}

		return sTPIDs.get(getPointOfSale());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// BazaarVoice Reviews/LanguageCode stuff

	public static LinkedList<String> getLanguages(Context context) {
		ensurePOSCountryCodesCacheFilled(context);
		ensurePOSDefaultLocalesCacheFilled(context);

		// construct the device locale based on device language and device POS
		String locale = Locale.getDefault().getLanguage();
		locale += "_";
		locale += sPOSCountryCodes.get(sCachedPointOfSale);

		// set locale to default if the constructed locale does not make sense
		if (!LOCALE_TO_EXPEDIA_PRIORITY_LIST.containsKey(locale)) {
			locale = sPOSDefaultLocales.get(sCachedPointOfSale);
		}

		return LOCALE_TO_EXPEDIA_PRIORITY_LIST.get(locale);
	}

	/**
	 * Converts a list of languages codes into a BV/Expedia formatted list of language codes for use in pulling
	 * all reviews of a set of languages (one or more langauages, where each language is a set of locale codes)
	 * 
	 * @param a simple string list of language codes
	 * @return a formatted list of locale codes to be used as param by BV request
	 */
	public static String formatLanguageCodes(List<String> codes) {
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (String code : codes) {
			sb.append(prefix);
			prefix = ",";
			sb.append(LANGUAGE_CODE_TO_CONTENT_LOCALE.get(code));
		}

		return sb.toString();
	}

	private static Map<String, String> sPOSCountryCodes;
	private static Map<String, String> sPOSDefaultLocales;

	private static void ensurePOSCountryCodesCacheFilled(Context context) {
		if (sPOSCountryCodes == null) {
			Map<String, String> badCountryCodes = ResourceUtils.getStringMap(context, R.array.pos_country_code_map);
			Map<String, String> countryMaps = new HashMap<String, String>();
			for (Map.Entry<String, String> e : badCountryCodes.entrySet()) {
				countryMaps.put(e.getKey(), convertCountryCode(e.getValue()));
			}
			sPOSCountryCodes = countryMaps;
		}
	}

	private static void ensurePOSDefaultLocalesCacheFilled(Context context) {
		if (sPOSDefaultLocales == null) {
			sPOSDefaultLocales = ResourceUtils.getStringMap(context, R.array.pos_default_locale);
		}
	}

	public static String convertCountryCode(String ccode) {
		if (ccode == null || ccode.length() > 2) {
			return ccode;
		}
		else {
			Locale loc = new Locale("", ccode);
			return loc.getISO3Country();
		}
	}

	public static boolean shouldDisplayMandatoryFees(Context context) {
		String pos = getPointOfSale(context);
		if (pos == null) {
			return false;
		}
		return pos.equals(context.getString(R.string.point_of_sale_it))
				|| pos.equals(context.getString(R.string.point_of_sale_de));
	}
}
