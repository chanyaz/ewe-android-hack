package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.server.ExpediaServices.ReviewSort;
import com.mobiata.android.Log;
import com.mobiata.android.util.ResourceUtils;
import com.mobiata.android.util.SettingUtils;

@SuppressWarnings("serial")
public class LocaleUtils {
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
		}
	};

	private static final Map<String, String> LANGUAGE_CODE_TO_CONTENT_LOCALE = new HashMap<String, String>() {
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
			put("es_AR", new LinkedList<String>(Arrays.asList("en", "pt", "es")));
			put("es_ES", new LinkedList<String>(Arrays.asList("es")));
			put("es_MX", new LinkedList<String>(Arrays.asList("es")));
			put("fr_BE", new LinkedList<String>(Arrays.asList("fr", "en")));
			put("fr_CA", new LinkedList<String>(Arrays.asList("fr")));
			put("fr_FR", new LinkedList<String>(Arrays.asList("fr", "en")));
			put("id_ID", new LinkedList<String>(Arrays.asList("id", "en")));
			put("it_IT", new LinkedList<String>(Arrays.asList("it", "en")));
			put("ja_JP", new LinkedList<String>(Arrays.asList("ja")));
			put("ko_KR", new LinkedList<String>(Arrays.asList("ko", "en")));
			put("ms_MY", new LinkedList<String>(Arrays.asList("ms", "en")));
			put("nl_BE", new LinkedList<String>(Arrays.asList("nl", "en")));
			put("nl_NL", new LinkedList<String>(Arrays.asList("nl", "en")));
			put("no_NO", new LinkedList<String>(Arrays.asList("no", "en")));
			put("pt_BR", new LinkedList<String>(Arrays.asList("pt", "es")));
			put("sv_SE", new LinkedList<String>(Arrays.asList("sv", "en")));
			put("th_TH", new LinkedList<String>(Arrays.asList("th", "en")));
			put("zh_HK", new LinkedList<String>(Arrays.asList("zh")));
			put("zh_TW", new LinkedList<String>(Arrays.asList("zh")));

			//			All other en displays: All English
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
	 * Call this liberally, whenever you think the POS may have changed.
	 */
	public static void onPointOfSaleChanged(Context context) {
		getPointOfSale(context);
	}

	public static void onPointOfSaleChanged(String newValue) {
		sCachedPointOfSale = newValue;
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

	private static String sCachedLanguageCode;

	public static void invalidateLanguageCodeCache() {
		sCachedLanguageCode = null;
	}

	public static String ensureCachedLanguageCodeFilled() {
		if (sCachedLanguageCode == null) {
			sCachedLanguageCode = Locale.getDefault().getLanguage();
		}
		return sCachedLanguageCode;
	}

	/**
	 * Determine the locales to request for reviews. BazaarVoice API works such that you can send a set of locale codes
	 * and it will retrieve all of the reviews that are in each locale bucket.
	 * 
	 * The inspiration for this method comes from https://team.mobiata.com/wiki/Expedia/Review_Behavior which in turn was
	 * inspired by the way in which Expedia desktop (www.expedia.com.*) handles the displaying of reviews.
	 * 
	 * @param context
	 * @return set of locales for BazaarVoice
	 */

	public static String getBazaarVoiceContentLocales(Context context, ReviewSort sort) {
		LinkedList<String> languageCodes = new LinkedList<String>();

		// ensure cache is filled
		if (null == sCachedPointOfSale) {
			sCachedPointOfSale = getDefaultPointOfSale(context);
		}

		if (null == sCachedLanguageCode) {
			sCachedLanguageCode = Locale.getDefault().getLanguage();
		}

		// grab the language codes
		if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_dk))) {
			// DANISH -> ENGLISH
			languageCodes.add("da");
			languageCodes.add("en");

		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_at))) {
			// GERMAN -> ENGLISH
			languageCodes.add("de");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_de))) {
			// GERMAN -> ENGLISH
			languageCodes.add("de");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_ca))) {
			if (sCachedLanguageCode.equals("fr")) {
				languageCodes.add("fr");
			}
			else {
				// FRENCH -> ENGLISH
				languageCodes.add("fr");
				languageCodes.add("en");
			}
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_ar))) {
			// LUMPED TOGETHER
			languageCodes.add("en");
			languageCodes.add("pt");
			languageCodes.add("es");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_es))) {
			languageCodes.add("es");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_mx))) {
			languageCodes.add("es");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_be))) {
			if (sCachedLanguageCode.equals("fr")) {
				// FRENCH -> ENGLISH
				languageCodes.add("fr");
				languageCodes.add("en");
			}
			else {
				// NL -> ENGLISH
				languageCodes.add("nl");
				languageCodes.add("en");
			}
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_id))) {
			// INDONESIAN -> ENGLISH
			languageCodes.add("id");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_fr))) {
			// FRENCH -> ENGLISH
			languageCodes.add("fr");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_it))) {
			// ITALIAN -> ENGLISH
			languageCodes.add("it");
			languageCodes.add("en");

		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_jp))) {
			languageCodes.add("ja");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_kr))) {
			// KOREAN -> ENGLISH
			languageCodes.add("ko");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_my))) {
			// MALAYSIAN -> ENGLISH
			languageCodes.add("ms");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_nl))) {
			// NEDERLANDS -> ENGLISH
			languageCodes.add("nl");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_no))) {
			// NORWEGIAN -> ENGLISH
			languageCodes.add("no");
			languageCodes.add("en");
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_br))) {
			// PORTUGEUESE -> SPANISH
			languageCodes.add("pt");
			languageCodes.add("es");

		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_se))) {
			// SWEDISH -> ENGLISH
			languageCodes.add("sv");
			languageCodes.add("en");

		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_th))) {
			if (sCachedLanguageCode.equals("th")) {
				// THAI -> ENGLISH
				languageCodes.add("th");
				languageCodes.add("en");
			}
			else {
				languageCodes.add("en");
			}
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_hk))) {
			if (sCachedLanguageCode.equals("zh")) {
				languageCodes.add("zh");
			}
			else {
				languageCodes.add("en");
			}
		}
		else if (sCachedPointOfSale.equals(context.getString(R.string.point_of_sale_tw))) {
			if (sCachedLanguageCode.equals("zh")) {
				languageCodes.add("zh");
			}
			else {
				// ENGLISH -> CHINESE
				languageCodes.add("en");
				languageCodes.add("zh");
			}
		}
		else {
			// FALLBACK TO ENGLISH DEFAULT?
			languageCodes.add("en");
		}

		String codes = formatLanguageCodes(languageCodes);

		// add flag to end of the string to denote that this list of language codes
		// has a priority, so request reviews by a certain language, then the next language
		// instead of grouping all of the languages at once
		if (sort == ReviewSort.NEWEST_REVIEW_FIRST) {

		}

		return codes;
	}

	/**
	 * The purpose of this class is to contain all of the bookkeeping related to the paging of reviews
	 * for a priority list of languages. For instance, using the language priority algorithm for the recent
	 * sort order, this instances of this class will store the pageNumber, the totalCount, localeCode
	 * 
	 * The UserReviewsListActivity will create a list of ReviewLanguageSet objects that are relevant to its POS
	 * and device language. Some POS will have only one object in its list, if there is no priority exhibited
	 * in the Expedia behavior. This makes the implementation extensible for all configurations that Expedia could
	 * possibly throw our way.
	 * 
	 * @author brad
	 *
	 */
	public static class ReviewLanguageSet {

		private String localesString;

		private int totalCount;

		private int pageNumber;

		private boolean attemptedDownload;

		public ReviewLanguageSet() {
			this.pageNumber = 0;
			this.attemptedDownload = false;
		}

		/**
		 * Function returns true if there are more reviews to be requested, i.e. another network call should be made
		 * @return
		 */
		public boolean hasMore() {
			// determine the number of reviews collected so far
			int numberCollected = pageNumber * ExpediaServices.REVIEWS_PER_PAGE;

			if (numberCollected >= totalCount) {
				return false;
			}

			return true;
		}

		public void setTotalCount(int count) {
			this.totalCount = count;
		}

		public void addLanguage(String languageCode) {
			if (localesString != null) {
				localesString += ",";
			}
			localesString += LANGUAGE_CODE_TO_CONTENT_LOCALE.get(languageCode);
		}

		public void setLocalesString(String localesString) {
			this.localesString = localesString;
		}

		public int getPageNumber() {
			return pageNumber;
		}

		public void incrementPageNumber() {
			pageNumber++;
		}

		public String getLocalesString() {
			return localesString;
		}

		public boolean getAttemptedDownload() {
			return attemptedDownload;
		}

		public void setAttemptedDownload(boolean attempted) {
			this.attemptedDownload = attempted;
		}
	}

	public static LinkedList<String> getLanguages(Context context) {
		ensurePOSCountryCodesCacheFilled(context);
		ensurePOSDefaultLocalesCacheFilled(context);

		// construct the device locale based on device language and device POS
		String locale = sCachedLanguageCode;
		locale += "_";
		locale += sPOSCountryCodes.get(sCachedPointOfSale);

		// set locale to default if the constructed locale is bunk
		if (!LOCALE_TO_EXPEDIA_PRIORITY_LIST.containsKey(locale)) {
			locale = sPOSDefaultLocales.get(sCachedPointOfSale);
		}

		// iterate through the list of language code, and create the proper ReviewLanguageSet meta object(s)
		// for each map to be store in list of ReviewLangaugeSet

		Log.d("attempting to retrive priority list from locale code: " + locale);
		return LOCALE_TO_EXPEDIA_PRIORITY_LIST.get(locale);
	}

	// perhaps come with a new format language code function that will attach a flag
	// at the end in order to maintain the correct state... whether or not to lump the
	// codes together, or page through them. this will upkeep the flag in the listview
	// hasMoreRecentReviews

	public static String formatLanguageCodes(LinkedList<String> codes) {
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
			sPOSCountryCodes = ResourceUtils.getStringMap(context, R.array.pos_country_code_map);
		}
	}

	private static void ensurePOSDefaultLocalesCacheFilled(Context context) {
		if (sPOSDefaultLocales == null) {
			sPOSDefaultLocales = ResourceUtils.getStringMap(context, R.array.pos_default_locale);
		}
	}

	private static Map<String, String> sTPIDs;

	public static String getTPID(Context context) {
		if (sTPIDs == null) {
			sTPIDs = ResourceUtils.getStringMap(context, R.array.tpid_map);
		}

		return sTPIDs.get(getPointOfSale());
	}
}