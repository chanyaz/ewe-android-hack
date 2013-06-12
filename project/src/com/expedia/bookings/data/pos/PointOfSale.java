package com.expedia.bookings.data.pos;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.ResourceUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * All data related to a point of sale.
 *
 * You MUST call init() before using this (suggested usage: call in Application)
 */
public class PointOfSale {

	/**
	 * This enum defines the different types of fields required for hotels checkout.
	 */
	public enum RequiredPaymentFields {
		NONE,
		POSTAL_CODE,
		ALL,
	}

	public static final String ACTION_POS_CHANGED = "com.expedia.bookings.action.pos_changed";

	private static final int INVALID_SITE_ID = -1;

	// The identifier for this point of sale
	private PointOfSaleId mPointOfSale;

	// List of locales associated with this POS
	private List<PointOfSaleLocale> mLocales = new ArrayList<PointOfSaleLocale>();

	// Maps the current language --> list of languages for reviews
	private Map<String, String[]> mReviewLocales = new HashMap<String, String[]>();

	// The base URL of the POS
	private String mUrl;

	// The POS's TPID (Travel Product Identifier)
	private int mTPID;

	// The POS's site ID (which appears to be the TPID + EAPID, Expedia Affiliate Product Id, if it differs)
	private int mSiteId;

	// The POS's contact phone number
	private String mSupportPhoneNumber;

	// The POS's support email address
	private String mSupportEmail;

	// The two-letter country code associated with this locale (e.g. "US")
	private String mTwoLetterCountryCode;

	// The three-letter country code associated with this locale (e.g. "USA")
	private String mThreeLetterCountryCode;

	// The distance unit used by this POS.  Not always used.
	private DistanceUnit mDistanceUnit;

	// Whether or not to require a rules & restrictions checkbox for this POS
	private boolean mRequiresRulesRestrictionsCheckbox;

	// Whether or not to display total price + mandatory fees when displaying costs
	private boolean mDisplayMandatoryFees;

	// Whether or not we should show the best price guarantee
	private boolean mDisplayBestPriceGuarantee;

	//Whether we should show last name first
	private boolean mShowLastNameFirst;

	//Whether we should hide middle name
	private boolean mHideMiddleName;

	// Whether or not to let users access flights on this POS
	private boolean mSupportsFlights;

	// Whether or not to let users checkout using Google Wallet
	private boolean mSupportsGoogleWallet;

	// Used to determine the default POS, based on the device's locale
	private String[] mDefaultLocales;

	// Used to determine which fields are required for Flights checkout
	private RequiredPaymentFields mRequiredPaymentFieldsFlights;

	// Whether or not to show cross-sells
	private boolean mShowHotelCrossSell;

	// Whether or not to block domestic flight searches before flight/search is called
	private boolean mBlockDomesticFlightSearches;

	/**
	 * There can be multiple different locales for a given POS.
	 *
	 * I'm purposefully obscuring this from the POS, so you don't have to figure this stuff out
	 * (we select the locale automatically based on the current Locale of the system).
	 */
	private static class PointOfSaleLocale {
		// The locale identifier (e.g., "es_AR") for this locale
		private String mLocaleIdentifier;

		// The url leading to the support part of the website
		private String mSupportUrl;

		// The url for please to be downloading this app
		private String mAppInfoUrl;

		// The url for just the website
		private String mWebsiteUrl;

		// The url for travel insurance. Not present for all POS
		private String mInsuranceUrl;

		// The url for the best price guarantee policy (if available in the POS)
		private String mBestPriceGuaranteePolicyUrl;

		// The rules & restrictions disclaimer for every hotel booking
		private String mHotelBookingStatement;

		// The rules & restrictions disclaimer for every flight booking
		private String mFlightBookingStatement;

		// The URL for Terms and Conditions for this POS
		private String mTermsAndConditionsUrl;

		// The URL for Terms of Booking for this POS (see GB)
		private String mTermsOfBookingUrl;

		// The URL for Privacy Policy for this POS
		private String mPrivacyPolicyUrl;

		// The language code that this locale associates with
		private String mLanguageCode;

		// The language identifier linked to this locale (linked to language code)
		private int mLanguageId;
	}

	//////////////////////////////////////////////////////////////////////////
	// Info on each POS

	public PointOfSaleId getPointOfSaleId() {
		return mPointOfSale;
	}

	public String getUrl() {
		return mUrl;
	}

	public int getTpid() {
		return mTPID;
	}

	/**
	 * In almost all cases TPID == SiteID. The one exception is for the AT POS. We only define this one exception in
	 * the shared JSON file, so we need to fallback on the TPID for all other POS to ensure siteId is returned.
	 */
	public int getSiteId() {
		if (mSiteId == INVALID_SITE_ID) {
			return mTPID;
		}
		else {
			return mSiteId;
		}
	}

	public String getSupportPhoneNumber() {
		return mSupportPhoneNumber;
	}

	public String getSupportEmail() {
		if (!TextUtils.isEmpty(mSupportEmail)) {
			return mSupportEmail;
		}
		else {
			return "support@expedia.com";
		}
	}

	public String getTwoLetterCountryCode() {
		return mTwoLetterCountryCode;
	}

	public String getThreeLetterCountryCode() {
		return mThreeLetterCountryCode;
	}

	public int getCountryNameResId() {
		return ResourceUtils.getIdentifier(R.string.class, "country_" + mTwoLetterCountryCode.toLowerCase());
	}

	public DistanceUnit getDistanceUnit() {
		return mDistanceUnit;
	}

	public boolean requiresRulesRestrictionsCheckbox() {
		return mRequiresRulesRestrictionsCheckbox;
	}

	public boolean displayMandatoryFees() {
		return mDisplayMandatoryFees;
	}

	public boolean displayBestPriceGuarantee() {
		return mDisplayBestPriceGuarantee;
	}

	public boolean showLastNameFirst() {
		return mShowLastNameFirst;
	}

	public boolean hideMiddleName() {
		return mHideMiddleName;
	}

	public boolean supportsFlights() {
		return mSupportsFlights;
	}

	public boolean supportsGoogleWallet() {
		return mSupportsGoogleWallet;
	}

	public boolean showAtolInfo() {
		// Possible TODO: Transfer this data into ExpediaPointOfSaleConfig
		return mPointOfSale == PointOfSaleId.UNITED_KINGDOM;
	}

	public String[] getReviewLanguages() {
		Locale locale = Locale.getDefault();
		String langCode = locale.getLanguage();
		if (mReviewLocales.containsKey(langCode)) {
			return mReviewLocales.get(langCode);
		}
		return mReviewLocales.get("*");
	}

	public String getLocaleIdentifier() {
		return getPosLocale().mLocaleIdentifier;
	}

	public String getSupportUrl() {
		return getPosLocale().mSupportUrl;
	}

	public String getAppInfoUrl() {
		return getPosLocale().mAppInfoUrl;
	}

	public String getWebsiteUrl() {
		return getPosLocale().mWebsiteUrl;
	}

	public String getInsuranceUrl() {
		return getPosLocale().mInsuranceUrl;
	}

	public String getBestPriceGuaranteeUrl() {
		return getPosLocale().mBestPriceGuaranteePolicyUrl;
	}

	public String getTermsAndConditionsUrl() {
		return getPosLocale().mTermsAndConditionsUrl;
	}

	public String getTermsOfBookingUrl() {
		return getPosLocale().mTermsOfBookingUrl;
	}

	public String getPrivacyPolicyUrl() {
		return getPosLocale().mPrivacyPolicyUrl;
	}

	// TODO: As more complicated payment combinations arise, think about a refactor

	public RequiredPaymentFields getRequiredPaymentFieldsFlights() {
		return mRequiredPaymentFieldsFlights;
	}

	public boolean requiresBillingAddressFlights() {
		return mRequiredPaymentFieldsFlights == RequiredPaymentFields.ALL;
	}

	public boolean showHotelCrossSell() {
		return mShowHotelCrossSell;
	}

	public boolean blockDomesticFlightSearch() {
		return mBlockDomesticFlightSearches;
	}

	/**
	 * On phone, we'll underline and bold the entire second half of the statement,
	 * which includes "Rules and Restrictions", "Terms and Conditions",
	 * "Privacy Policy", and "Terms of Booking"
	 * @return Stylized CharSequence
	 */
	public CharSequence getStylizedHotelBookingStatement() {
		return getStylizedStatement(getPosLocale().mHotelBookingStatement);
	}

	/**
	 * On phone, we'll underline and bold the entire second half of the statement,
	 * which includes "Rules and Restrictions", "Terms and Conditions",
	 * "Privacy Policy", and "Terms of Booking"
	 * @return Stylized CharSequence
	 */
	public CharSequence getStylizedFlightBookingStatement() {
		if (!TextUtils.isEmpty(getPosLocale().mFlightBookingStatement)) {
			return getStylizedStatement(getPosLocale().mFlightBookingStatement);
		}
		return "FAIL FAIL FAIL LOC NEEDED: flightBookingStatement";
	}

	private CharSequence getStylizedStatement(String statement) {
		SpannableStringBuilder text = new SpannableStringBuilder(Html.fromHtml(statement));

		// Replace <a> spans with our own version: bold and underlined.
		// Partially stylistic, but also we don't want them to be clickable
		// since the whole View displaying this stylized string is clickable.
		URLSpan[] spans = text.getSpans(0, statement.length(), URLSpan.class);
		for (URLSpan o : spans) {
			int start = text.getSpanStart(o);
			int end = text.getSpanEnd(o);
			text.removeSpan(o);
			text.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
			text.setSpan(new UnderlineSpan(), start, end, 0);
		}

		return text;
	}

	public int getDualLanguageId() {
		return getPosLocale().mLanguageId;
	}

	// Returns the correct POSLocale based on the user's current locale
	private PointOfSaleLocale getPosLocale() {
		if (mLocales.size() > 1) {
			Locale locale = Locale.getDefault();
			String langCode = locale.getLanguage();
			for (PointOfSaleLocale posLocale : mLocales) {
				if (posLocale.mLanguageCode.equalsIgnoreCase(langCode)) {
					return posLocale;
				}
			}
		}

		// In the case that we can't find the right locale (or there
		// is only one locale),  default to the first locale.
		return mLocales.get(0);
	}

	//////////////////////////////////////////////////////////////////////////
	// POS Access

	// The last accessed POS (so that you can get the POS without a Context)
	private static PointOfSaleId sCachedPOS;

	// All POSes (pre-loaded at the start of the app)
	private static final Map<PointOfSaleId, PointOfSale> sPointOfSale = new HashMap<PointOfSaleId, PointOfSale>();

	// This is a backwards-compatible map from the old setting (which was based on a string) to a POS
	private static final Map<String, PointOfSaleId> sBackCompatPosMap = new HashMap<String, PointOfSaleId>();

	/**
	 * MUST be called before using any other POS methods
	 */
	public static void init(Context context) {
		// Load all data; in the future we may want to load only the POS requested, to save startup time
		loadPointOfSaleInfo(context);

		// Load review map
		loadReviewLanguageLocaleMap(context);

		// Load supported Expedia suggest locales
		loadExpediaSuggestSupportedLanguages(context);

		// Load Expedia countries for which Payment Postal code is optional
		loadExpediaPaymentPostalCodeOptionalCountries(context);

		// Init the cache
		getPointOfSale(context);
	}

	/**
	 * @return the current PointOfSale (or the default if none has been set yet)
	 */
	public static PointOfSale getPointOfSale(Context context) {
		sCachedPOS = null;

		boolean savePos = false;

		String posSetting = SettingUtils.get(context, context.getString(R.string.PointOfSaleKey), null);
		if (posSetting == null) {
			// VSC default
			if (ExpediaBookingApp.IS_VSC) {
				sCachedPOS = PointOfSaleId.VSC;
				savePos = true;
			}
			else {
				// Get the default POS.  This is rare, thus we can excuse this excessive code.
				Locale locale = Locale.getDefault();
				String country = locale.getCountry().toLowerCase();
				String language = locale.getLanguage().toLowerCase();

				for (PointOfSale posInfo : sPointOfSale.values()) {
					for (String defaultLocale : posInfo.mDefaultLocales) {
						defaultLocale = defaultLocale.toLowerCase();
						if (defaultLocale.endsWith(country) || defaultLocale.equals(language)) {
							sCachedPOS = posInfo.mPointOfSale;
							break;
						}
					}

					if (sCachedPOS != null) {
						break;
					}
				}

				// Default to UK POS if nothing matches the user's locale
				if (sCachedPOS == null) {
					sCachedPOS = PointOfSaleId.UNITED_KINGDOM;
				}

				savePos = true;

				Log.i("No POS set yet, chose " + sCachedPOS + " based on current locale: " + locale.toString());
			}
		}
		else {
			try {
				int posId = Integer.parseInt(posSetting);
				sCachedPOS = PointOfSaleId.getPointOfSaleFromId(posId);
				Log.v("Cached POS: " + sCachedPOS);
			}
			catch (NumberFormatException e) {
				// For backwards compatibility, we need to map from the old (which used the url) to the new
				// system (and save it so we don't have to do this again).
				sCachedPOS = sBackCompatPosMap.get(posSetting);

				savePos = true;

				Log.i("Upgrading from previous version of EB, from \"" + posSetting + "\" to " + sCachedPOS);
			}
		}

		if (savePos) {
			SettingUtils
					.save(context, context.getString(R.string.PointOfSaleKey), Integer.toString(sCachedPOS.getId()));
		}

		return sPointOfSale.get(sCachedPOS);
	}

	/**
	 * @return the cached PointOfSale.  Will crash the app if it hasn't been cached yet.
	 */
	public static PointOfSale getPointOfSale() {
		if (sCachedPOS == null) {
			throw new RuntimeException("getPointOfSale() called before POS filled in by system");
		}

		return sPointOfSale.get(sCachedPOS);
	}

	/**
	 * Call this whenever the POS is changed; this will notify interested
	 * parties of the change.
	 */
	public static void onPointOfSaleChanged(Context context) {
		Log.i("Point of sale changed!");

		Log.d("Old POS id: " + sCachedPOS);

		// Update the cache
		getPointOfSale(context);

		// clear all data
		Db.clear();

		// Notify app of POS change
		Intent intent = new Intent(ACTION_POS_CHANGED);
		context.sendBroadcast(intent);

		Log.d("New POS id: " + sCachedPOS);
	}

	// Provide context for sorting purposes
	public static List<PointOfSale> getAllPointsOfSale(final Context context) {
		List<PointOfSale> poses = new ArrayList<PointOfSale>(sPointOfSale.values());

		Comparator<PointOfSale> comparator = new Comparator<PointOfSale>() {
			@Override
			public int compare(PointOfSale lhs, PointOfSale rhs) {
				String lhsName = context.getString(lhs.getCountryNameResId());
				String rhsName = context.getString(rhs.getCountryNameResId());
				return lhsName.compareTo(rhsName);
			}
		};

		Collections.sort(poses, comparator);

		return poses;
	}

	//////////////////////////////////////////////////////////////////////////
	// Review language locale mappings

	// This maps a language ("de") to its multiple review locales ("de,de_AT,de_DE").
	private static Map<String, String[]> mReviewLanguageMap = new HashMap<String, String[]>();

	public static String getFormattedLanguageCodes(List<String> codes) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String code : codes) {
			for (String locale : mReviewLanguageMap.get(code)) {
				if (first) {
					first = false;
				}
				else {
					sb.append(",");
				}

				sb.append(locale);
			}
		}

		return sb.toString();
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia suggest supported locales

	private static Set<String> sExpediaSuggestSupportedLocales = new HashSet<String>();

	public static boolean localeSupportedByExpediaSuggest(String localeIdentifier) {
		return sExpediaSuggestSupportedLocales.contains(localeIdentifier);
	}

	public static String getSuggestLocaleIdentifier() {
		String localeIdentifier = Locale.getDefault().toString();

		if (!localeSupportedByExpediaSuggest(localeIdentifier)) {
			localeIdentifier = getPointOfSale().getLocaleIdentifier();
		}

		return localeIdentifier;
	}

	//////////////////////////////////////////////////////////////////////////
	// Data loading

	@SuppressWarnings("unchecked")
	private static void loadPointOfSaleInfo(Context context) {
		long start = System.nanoTime();

		sPointOfSale.clear();

		try {
			InputStream is;
			if (ExpediaBookingApp.IS_VSC) {
				is = context.getAssets().open("ExpediaSharedData/VSCPointOfSaleConfig.json");
			}
			else {
				is = context.getAssets().open("ExpediaSharedData/ExpediaPointOfSaleConfig.json");
			}
			String data = IoUtils.convertStreamToString(is);
			JSONObject posData = new JSONObject(data);
			Iterator<String> keys = posData.keys();
			while (keys.hasNext()) {
				String posName = keys.next();
				PointOfSale pos = parsePointOfSale(context, posData.optJSONObject(posName));
				pos.mTwoLetterCountryCode = posName.toLowerCase();
				sPointOfSale.put(pos.mPointOfSale, pos);

				// For backwards compatibility
				sBackCompatPosMap.put(pos.mUrl, pos.mPointOfSale);
			}
		}
		catch (Exception e) {
			// If the POSes fail to load, then we should fail horribly
			throw new RuntimeException(e);
		}

		Log.i("Loaded POS data in " + (System.nanoTime() - start) / 1000000 + " ms");
	}

	@SuppressWarnings("unchecked")
	private static PointOfSale parsePointOfSale(Context context, JSONObject data) throws JSONException {
		PointOfSale pos = new PointOfSale();

		pos.mPointOfSale = PointOfSaleId.getPointOfSaleFromId(data.optInt("pointOfSaleId"));

		// POS data
		pos.mThreeLetterCountryCode = data.optString("countryCode", null);

		// Server access
		pos.mUrl = data.optString("url", null);
		pos.mTPID = data.optInt("TPID");
		pos.mSiteId = data.optInt("siteId", INVALID_SITE_ID);

		// Support
		pos.mSupportPhoneNumber = parseDeviceSpecificPhoneNumber(context, data, "supportPhoneNumber");
		pos.mSupportEmail = data.optString("supportEmail");

		// POS config
		pos.mDistanceUnit = data.optString("distanceUnit", "").equals("miles") ? DistanceUnit.MILES
				: DistanceUnit.KILOMETERS;
		pos.mDisplayMandatoryFees = data.optBoolean("shouldDisplayTotalPriceWithMandatoryFees");
		pos.mRequiresRulesRestrictionsCheckbox = data.optBoolean("explicitConsentRequired");
		pos.mDisplayBestPriceGuarantee = data.optBoolean("shouldDisplayBestPriceGuarantee");
		pos.mShowLastNameFirst = data.optBoolean("shouldShowLastNameFirst");
		pos.mHideMiddleName = data.optBoolean("shouldHideMiddleName");
		pos.mSupportsFlights = data.optBoolean("flightsEnabled");
		pos.mSupportsGoogleWallet = data.optBoolean("googleWalletEnabled");
		pos.mShowHotelCrossSell = !data.optBoolean("hideHotelCrossSell", false);
		pos.mBlockDomesticFlightSearches = data.optBoolean("blockDomesticFlightSearches", false);

		// Parse POS locales
		JSONArray supportedLocales = data.optJSONArray("supportedLocales");
		for (int a = 0; a < supportedLocales.length(); a++) {
			pos.mLocales.add(parseLocale(supportedLocales.optJSONObject(a)));
		}

		// Parse review locales
		JSONObject reviewLocales = data.optJSONObject("reviewLocales");
		Iterator<String> reviewLanguages = reviewLocales.keys();
		while (reviewLanguages.hasNext()) {
			String reviewLanguage = reviewLanguages.next();
			JSONArray reviewArr = reviewLocales.optJSONArray(reviewLanguage);
			pos.mReviewLocales.put(reviewLanguage, stringJsonArrayToArray(reviewArr));
		}

		JSONArray mappedLocales = data.optJSONArray("automaticallyMappedLocales");
		pos.mDefaultLocales = stringJsonArrayToArray(mappedLocales);

		pos.mRequiredPaymentFieldsFlights = parseRequiredPaymentFieldsFlights(data);

		return pos;
	}

	// e.g. "supportPhoneNumber": {
	//  "*": "<Default #>",
	//  "iPhone": "<iPhone #>",
	//  "iPad": "<iPad #>",
	//  "Android": "<Android non-tablet #>",
	//  "AndroidTablet": "<Android tablet #>"
	// },
	private static String parseDeviceSpecificPhoneNumber(Context context, JSONObject data, String name)
			throws JSONException {
		if (!data.has(name)) {
			return null;
		}
		JSONObject numbers = data.getJSONObject(name);

		// Try to find a device specific number
		String deviceSpecificKey = ExpediaBookingApp.useTabletInterface(context) ? "AndroidTablet" : "Android";
		String result = numbers.optString(deviceSpecificKey, null);
		if (!TextUtils.isEmpty(result)) {
			return result;
		}

		// Just use the default number (or null if it doesn't exist)
		return numbers.optString("*", null);
	}

	private static RequiredPaymentFields parseRequiredPaymentFieldsFlights(JSONObject data) {
		String paymentFields = data.optString("requiredPaymentFields:flights");
		RequiredPaymentFields type;
		if ("postalCode".equals(paymentFields)) {
			type = RequiredPaymentFields.POSTAL_CODE;
		}
		else if ("none".equals(paymentFields)) {
			type = RequiredPaymentFields.NONE;
		}
		else {
			type = RequiredPaymentFields.ALL;
		}
		return type;

	}

	private static String[] stringJsonArrayToArray(JSONArray stringJsonArr) {
		String[] arr = new String[stringJsonArr.length()];
		for (int a = 0; a < arr.length; a++) {
			arr[a] = stringJsonArr.optString(a);
		}
		return arr;
	}

	private static PointOfSaleLocale parseLocale(JSONObject data) {
		PointOfSaleLocale locale = new PointOfSaleLocale();

		locale.mLocaleIdentifier = data.optString("localeIdentifier", null);

		// Various URLs
		locale.mSupportUrl = data.optString("supportURL", null);
		locale.mAppInfoUrl = data.optString("appInfoURL", null);
		locale.mWebsiteUrl = data.optString("contactURL", null);
		locale.mInsuranceUrl = data.optString("insuranceURL", null);
		locale.mBestPriceGuaranteePolicyUrl = data.optString("bestPriceGuaranteePolicyURL", null);

		// All fields for rules & restrictions disclaimer
		locale.mHotelBookingStatement = data.optString("hotelBookingStatement", null);
		locale.mFlightBookingStatement = data.optString("flightBookingStatement", null);
		locale.mTermsAndConditionsUrl = data.optString("termsAndConditionsURL", null);
		locale.mTermsOfBookingUrl = data.optString("termsOfBookingURL", null);
		locale.mPrivacyPolicyUrl = data.optString("privacyPolicyURL", null);

		// Language identifier
		locale.mLanguageCode = data.optString("languageCode", null);
		locale.mLanguageId = data.optInt("languageIdentifier");

		// Fix one thing with the iOS-based data...
		if ("zh-Hant".equals(locale.mLanguageCode)) {
			locale.mLanguageCode = "zh";
		}

		return locale;
	}

	@SuppressWarnings("unchecked")
	private static void loadReviewLanguageLocaleMap(Context context) {
		mReviewLanguageMap.clear();

		try {
			InputStream is = context.getAssets().open("ExpediaSharedData/ExpediaReviewLanguageLocaleMap.json");
			String data = IoUtils.convertStreamToString(is);
			JSONObject langData = new JSONObject(data);
			Iterator<String> keys = langData.keys();
			while (keys.hasNext()) {
				String language = keys.next();
				mReviewLanguageMap.put(language, stringJsonArrayToArray(langData.optJSONArray(language)));
			}
		}
		catch (Exception e) {
			// If this data fails to load, then we should fail horribly
			throw new RuntimeException(e);
		}
	}

	private static void loadExpediaSuggestSupportedLanguages(Context context) {
		sExpediaSuggestSupportedLocales.clear();

		try {
			InputStream is = context.getAssets().open("ExpediaSharedData/ExpediaSuggestSupportedLocales.json");
			String data = IoUtils.convertStreamToString(is);
			JSONArray localeArr = new JSONArray(data);
			int len = localeArr.length();
			for (int a = 0; a < len; a++) {
				sExpediaSuggestSupportedLocales.add(localeArr.optString(a));
			}
		}
		catch (Exception e) {
			// If this data fails to load, then we should fail horribly
			throw new RuntimeException(e);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Expedia flight payment postal code optional locales

	private static Set<String> sExpediaPaymentPostalCodeOptionalCountries = new HashSet<String>();

	private static void loadExpediaPaymentPostalCodeOptionalCountries(Context context) {
		sExpediaPaymentPostalCodeOptionalCountries.clear();

		try {
			InputStream is = context.getAssets().open(
					"ExpediaSharedData/ExpediaPaymentPostalCodeOptionalCountries.json");
			String data = IoUtils.convertStreamToString(is);
			JSONArray countryArr = new JSONArray(data);
			int len = countryArr.length();
			for (int a = 0; a < len; a++) {
				sExpediaPaymentPostalCodeOptionalCountries.add(countryArr.optString(a));
			}
		}
		catch (Exception e) {
			// If this data fails to load, then we should fail horribly
			throw new RuntimeException(e);
		}
	}

	public static boolean countryPaymentRequiresPostalCode(String localeIdentifier) {
		return !sExpediaPaymentPostalCodeOptionalCountries.contains(localeIdentifier);
	}
}
