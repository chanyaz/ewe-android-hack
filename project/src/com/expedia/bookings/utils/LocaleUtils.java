package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.pos.PointOfSaleInfo;
import com.mobiata.android.util.ResourceUtils;

@SuppressWarnings("serial")
public class LocaleUtils {

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

	public static DistanceUnit getPosDistanceUnit(Context context) {
		if (PointOfSaleInfo.getPointOfSaleInfo().getUrl().equals(context.getString(R.string.point_of_sale_us))) {
			return DistanceUnit.MILES;
		}
		else {
			return DistanceUnit.KILOMETERS;
		}
	}

	/**
	 * Returns the language id for the POS, if it is needed.  (Returns null for non-dual-language POSes). 
	 */
	public static String getDualLanguageId(Context context) {
		String pos = PointOfSaleInfo.getPointOfSaleInfo().getUrl();
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

		return sTPIDs.get(PointOfSaleInfo.getPointOfSaleInfo().getUrl());
	}

	private static Map<String, String> sSiteIds;

	public static String getSiteId(Context context) {
		if (sSiteIds == null) {
			sSiteIds = ResourceUtils.getStringMap(context, R.array.siteid_map);
		}

		return sSiteIds.get(PointOfSaleInfo.getPointOfSaleInfo().getUrl());
	}

	//////////////////////////////////////////////////////////////////////////////////////////////////////
	// BazaarVoice Reviews/LanguageCode stuff

	public static LinkedList<String> getLanguages(Context context) {
		ensurePOSCountryCodesCacheFilled(context);
		ensurePOSDefaultLocalesCacheFilled(context);

		// construct the device locale based on device language and device POS
		String locale = Locale.getDefault().getLanguage();
		locale += "_";
		locale += sPOSCountryCodes.get(PointOfSaleInfo.getPointOfSaleInfo().getUrl());

		// set locale to default if the constructed locale does not make sense
		if (!LOCALE_TO_EXPEDIA_PRIORITY_LIST.containsKey(locale)) {
			locale = sPOSDefaultLocales.get(PointOfSaleInfo.getPointOfSaleInfo().getUrl());
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

	public static void init(Context context) {
		initMandatoryFees(context);
	}

	private static String[] sMandatoryFeesPointOfSales;

	public static void initMandatoryFees(Context context) {
		sMandatoryFeesPointOfSales = context.getResources().getStringArray(R.array.pos_mandatory_fees);
		Arrays.sort(sMandatoryFeesPointOfSales);
	}

	public static boolean shouldDisplayMandatoryFees(Context context) {
		if (sMandatoryFeesPointOfSales == null) {
			throw new RuntimeException("Need to call LocaleUtils.init(context) on app start");
		}

		return Arrays.binarySearch(sMandatoryFeesPointOfSales, PointOfSaleInfo.getPointOfSaleInfo().getUrl()) >= 0;
	}

	public static boolean shouldDisplayMandatoryFees() {
		if (sMandatoryFeesPointOfSales == null) {
			throw new RuntimeException("Need to call LocaleUtils.init(context) on app start");
		}

		return Arrays.binarySearch(sMandatoryFeesPointOfSales, PointOfSaleInfo.getPointOfSaleInfo().getUrl()) >= 0;
	}
}
