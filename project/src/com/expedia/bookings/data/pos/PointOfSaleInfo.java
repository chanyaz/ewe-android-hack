package com.expedia.bookings.data.pos;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.style.URLSpan;

import com.expedia.bookings.R;
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
 * 
 * TO TEST:
 * - Upgrades from previous versions with POS already set
 * - Default POS being set correctly
 * 
 * TODO
 * - Replace SupportUtils.getAppSupportUrl()
 * - Look into all cases of getUrl() and correct where necessary what's happening
 * 
 */
public class PointOfSaleInfo {

	public static final String ACTION_POS_CHANGED = "com.expedia.bookings.action.pos_changed";

	// The identifier for this point of sale
	private PointOfSale mPointOfSale;

	// List of locales associated with this POS
	private List<PointOfSaleLocale> mLocales = new ArrayList<PointOfSaleLocale>();

	private Map<String, String[]> mReviewLocales = new HashMap<String, String[]>();

	// The base URL of the POS 
	private String mUrl;

	// The POS's TPID (Travel Product Identifier)
	private int mTPID;

	// The POS's site ID (which appears to be the TPID + EAPID, Expedia Affiliate Product Id, if it differs)
	private int mSiteId;

	// The POS's contact phone number
	private String mSupportPhoneNumber;

	// The POS's contact phone number for flights issues
	private String mFlightSupportPhoneNumber;

	// The two-letter country code associated with this locale (e.g. "US")
	private String mTwoLetterCountryCode; // LocaleUtils.sPOSCountryCodes

	// The three-letter country code associated with this locale (e.g. "USA")
	private String mThreeLetterCountryCode; // LocaleUtils.sPOSCountryCodes

	// The distance unit used by this POS.  Not always used.
	private DistanceUnit mDistanceUnit;

	// Whether or not to require a rules & restrictions checkbox for this POS
	private boolean mRequiresRulesRestrictionsCheckbox;

	// Whether or not to display total price + mandatory fees when displaying costs
	private boolean mDisplayMandatoryFees;

	// Whether or not we should show the best price guarantee
	private boolean mDisplayBestPriceGuarantee;

	// Whether or not to let users access flights on this POS
	private boolean mSupportsFlights;

	// Used to determine the default POS, based on the device's locale
	private String[] mDefaultLocales;

	/**
	 * There can be multiple different locales for a given POS.
	 * 
	 * I'm purposefully obscuring this from the POS, so you don't have to figure this stuff out
	 * (we select the locale automatically based on the current Locale of the system). 
	 */
	private static class PointOfSaleLocale {
		// The url leading to the support part of the website
		private String mSupportUrl;

		// The url for just the website
		private String mWebsiteUrl;

		// The url for the best price guarantee policy (if available in the POS)
		private String mBestPriceGuaranteePolicyUrl;

		// The rules & restrictions disclaimer for every booking
		private String mRulesRestrictionsConfirmation; // RulesRestrictionsUtils.getRulesRestrictionsConfirmation(), sort of

		// The text in mRulesRestrictionsConfirmation that should be linked for terms and conditions
		private String mTermsAndConditionsLinkText;

		// The url to link to in mTermsAndConditionsLinkText
		private String mTermsAndConditionsUrl; // RulesRestrictionsUtils.getTermsAndConditionsUrl()

		// The text in mRulesRestrictionsConfirmation that should be linked for the privacy policy
		private String mPrivacyPolicyLinkText;

		// The url to link to in mPrivacyPolicyLinkText
		private String mPrivacyPolicyUrl; // RulesRestrictionsUtils.getPrivacyPolicyUrl()

		// The language code that this locale associates with
		private String mLanguageCode; // Used in LocaleUtils.getDualLanguageId()

		// The language identifier linked to this locale (linked to language code)
		private int mLanguageId; // LocaleUtils.getDualLanguageId()
	}

	//////////////////////////////////////////////////////////////////////////
	// Info on each POS

	public PointOfSale getPointOfSale() {
		return mPointOfSale;
	}

	public String getUrl() {
		return mUrl;
	}

	public int getTpid() {
		return mTPID;
	}

	public int getSiteId() {
		return mSiteId;
	}

	public String getSupportPhoneNumber() {
		return mSupportPhoneNumber;
	}

	public String getFlightSupportPhoneNumber() {
		return mFlightSupportPhoneNumber;
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

	public boolean supportsFlights() {
		return mSupportsFlights;
	}

	public String[] getReviewLanguages() {
		Locale locale = Locale.getDefault();
		String langCode = locale.getLanguage();
		if (mReviewLocales.containsKey(langCode)) {
			return mReviewLocales.get(langCode);
		}
		return mReviewLocales.get("*");
	}

	public String getSupportUrl() {
		return getPosLocale().mSupportUrl;
	}

	public String getWebsiteUrl() {
		return getPosLocale().mWebsiteUrl;
	}

	public String getBestPriceGuaranteeUrl() {
		return getPosLocale().mBestPriceGuaranteePolicyUrl;
	}

	public CharSequence getRulesRestrictionsConfirmation() {
		PointOfSaleLocale posLocale = getPosLocale();

		SpannableStringBuilder text = new SpannableStringBuilder(posLocale.mRulesRestrictionsConfirmation);
		linkifyText(text, posLocale.mRulesRestrictionsConfirmation, posLocale.mTermsAndConditionsLinkText,
				posLocale.mTermsAndConditionsUrl);
		linkifyText(text, posLocale.mRulesRestrictionsConfirmation, posLocale.mPrivacyPolicyLinkText,
				posLocale.mPrivacyPolicyUrl);

		return text;
	}

	private void linkifyText(SpannableStringBuilder text, String origText, String linkText, String url) {
		int linkStart = origText.indexOf(linkText);
		text.setSpan(new URLSpan(url), linkStart, linkStart + linkText.length(), 0);
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
	private static PointOfSale sCachedPOS;

	// All POSes (pre-loaded at the start of the app)
	private static final Map<PointOfSale, PointOfSaleInfo> sPointOfSaleInfo = new HashMap<PointOfSale, PointOfSaleInfo>();

	// This is a backwards-compatible map from the old setting (which was based on a string) to a POS
	private static final Map<String, PointOfSale> sBackCompatPosMap = new HashMap<String, PointOfSale>();

	/**
	 * MUST be called before using any other POS methods
	 */
	public static void init(Context context) {
		// Load all data; in the future we may want to load only the POS requested, to save startup time
		loadPointOfSaleInfo(context);

		// Init the cache
		getPointOfSaleInfo(context);
	}

	/**
	 * @return the current PointOfSaleInfo (or the default if none has been set yet)
	 */
	public static PointOfSaleInfo getPointOfSaleInfo(Context context) {
		sCachedPOS = null;

		boolean savePos = false;

		String posSetting = SettingUtils.get(context, context.getString(R.string.PointOfSaleKey), null);
		if (posSetting == null) {
			// Get the default POS.  This is rare, thus we can excuse this excessive code.
			Locale locale = Locale.getDefault();
			String country = locale.getCountry().toLowerCase();
			String language = locale.getLanguage().toLowerCase();

			for (PointOfSaleInfo posInfo : sPointOfSaleInfo.values()) {
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
				sCachedPOS = PointOfSale.UNITED_KINGDOM;
			}

			savePos = true;
		}
		else {
			try {
				int posId = Integer.parseInt(posSetting);
				sCachedPOS = PointOfSale.getPointOfSaleFromId(posId);
			}
			catch (NumberFormatException e) {
				// For backwards compatibility, we need to map from the old (which used the url) to the new
				// system (and save it so we don't have to do this again).
				sCachedPOS = sBackCompatPosMap.get(posSetting);

				savePos = true;

			}
		}

		if (savePos) {
			SettingUtils
					.save(context, context.getString(R.string.PointOfSaleKey), Integer.toString(sCachedPOS.getId()));
		}

		return sPointOfSaleInfo.get(sCachedPOS);
	}

	/**
	 * @return the cached PointOfSaleInfo.  Will crash the app if it hasn't been cached yet.
	 */
	public static PointOfSaleInfo getPointOfSaleInfo() {
		if (sCachedPOS == null) {
			throw new RuntimeException("getPointOfSale() called before POS filled in by system");
		}

		return sPointOfSaleInfo.get(sCachedPOS);
	}

	/**
	 * Call this whenever the POS is changed; this will notify interested
	 * parties of the change.
	 */
	public static void onPointOfSaleChanged(Context context) {
		Log.i("Point of sale changed!");

		// Update the cache
		getPointOfSaleInfo(context);

		// clear all data
		Db.clear();

		// Notify app of POS change
		Intent intent = new Intent(ACTION_POS_CHANGED);
		context.sendBroadcast(intent);
	}

	// Provide context for sorting purposes
	public static List<PointOfSaleInfo> getAllPointOfSaleInfo(final Context context) {
		List<PointOfSaleInfo> poses = new ArrayList<PointOfSaleInfo>(sPointOfSaleInfo.values());

		Comparator<PointOfSaleInfo> comparator = new Comparator<PointOfSaleInfo>() {
			@Override
			public int compare(PointOfSaleInfo lhs, PointOfSaleInfo rhs) {
				String lhsName = context.getString(lhs.getCountryNameResId());
				String rhsName = context.getString(rhs.getCountryNameResId());
				return lhsName.compareTo(rhsName);
			}
		};

		Collections.sort(poses, comparator);

		return poses;
	}

	//////////////////////////////////////////////////////////////////////////
	// Data loading

	private static void loadPointOfSaleInfo(Context context) {
		long start = System.nanoTime();

		sPointOfSaleInfo.clear();

		try {
			InputStream is = context.getAssets().open("ExpediaSharedData/ExpediaPointOfSaleConfig.json");
			String data = IoUtils.convertStreamToString(is);
			JSONObject posData = new JSONObject(data);
			Iterator<String> keys = posData.keys();
			while (keys.hasNext()) {
				String posName = keys.next();
				PointOfSaleInfo pos = parsePointOfSale(posData.optJSONObject(posName));
				pos.mTwoLetterCountryCode = posName.toLowerCase();
				sPointOfSaleInfo.put(pos.mPointOfSale, pos);

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

	private static PointOfSaleInfo parsePointOfSale(JSONObject data) {
		PointOfSaleInfo pos = new PointOfSaleInfo();

		pos.mPointOfSale = PointOfSale.getPointOfSaleFromId(data.optInt("pointOfSaleId"));

		// POS data
		pos.mThreeLetterCountryCode = data.optString("countryCode", null);

		// Server access
		pos.mUrl = data.optString("url", null);
		pos.mTPID = data.optInt("TPID");
		pos.mSiteId = data.optInt("siteId");

		// Support
		pos.mSupportPhoneNumber = data.optString("contactPhoneNumber", null);
		pos.mFlightSupportPhoneNumber = data.optString("flightsContactPhoneNumber", null);

		// POS config
		pos.mDistanceUnit = data.optString("distanceUnit", "").equals("miles") ? DistanceUnit.MILES
				: DistanceUnit.KILOMETERS;
		pos.mDisplayMandatoryFees = data.optBoolean("shouldDisplayTotalPriceWithMandatoryFees");
		pos.mRequiresRulesRestrictionsCheckbox = data.optBoolean("explicitConsentRequired");
		pos.mDisplayBestPriceGuarantee = data.optBoolean("shouldDisplayBestPriceGuarantee");
		pos.mSupportsFlights = data.optBoolean("flightsEnabled");

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

		return pos;
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

		// Various URLs
		locale.mSupportUrl = data.optString("supportURL", null);
		locale.mWebsiteUrl = data.optString("contactURL", null);
		locale.mBestPriceGuaranteePolicyUrl = data.optString("bestPriceGuaranteePolicyURL", null);

		// All fields for rules & restrictions disclaimer
		locale.mRulesRestrictionsConfirmation = data.optString("hotelBookingStatement", null);
		locale.mTermsAndConditionsLinkText = data.optString("termsAndConditionsURL", null);
		locale.mTermsAndConditionsUrl = data.optString("termsAndConditionsURL", null);
		locale.mPrivacyPolicyLinkText = data.optString("privacyPolicyLinkText", null);
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
}
