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
import com.expedia.bookings.content.SuggestionProvider;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.server.EndPoint;
import com.mobiata.android.Log;
import com.mobiata.android.util.IoUtils;
import com.mobiata.android.util.ResourceUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * All data related to a point of sale.
 * <p/>
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

	public static final int INVALID_EAPID = -1;

	// The identifier for this point of sale
	private PointOfSaleId mPointOfSale;

	// List of locales associated with this POS
	private List<PointOfSaleLocale> mLocales = new ArrayList<PointOfSaleLocale>();

	// The base URL of the POS
	private String mUrl;

	// The POS's TPID (Travel Product Identifier)
	private int mTPID;

	// The POS's site ID (which appears to be the TPID + EAPID, Expedia Affiliate Product Id, if it differs)
	private int mSiteId;

	// The POS's contact phone number
	private String mSupportPhoneNumber;

	// The POS's silver rewards member contact phone number
	private String mSupportPhoneNumberSilver;

	// The POS's gold rewards member phone number
	private String mSupportPhoneNumberGold;

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

	// Whether or not we should show the best price guarantee
	private boolean mDisplayBestPriceGuarantee;

	//Whether we should show last name first
	private boolean mShowLastNameFirst;

	//Whether we should hide middle name
	private boolean mHideMiddleName;

	// Whether or not to let users access flights on this POS
	private boolean mSupportsFlights;

	// Flag for whether GDE is enabled
	private boolean mSupportsGDE;

	// Whether or not to use downloaded routes (for AirAsia) or not
	private boolean mDisplayFlightDropDownRoutes;

	// Whether or not to let users checkout using Google Wallet
	private boolean mSupportsGoogleWallet;

	// Used to determine the default POS, based on the device's locale
	private String[] mDefaultLocales;

	// Used to determine which fields are required for Flights checkout
	private RequiredPaymentFields mRequiredPaymentFieldsFlights;

	// Whether or not to show cross-sells
	private boolean mShowHotelCrossSell;

	// Does this pos not allow debit cards for flights?
	private boolean mDoesNotAcceptDebitCardsFlights;

	// Does this POS have the VIP Access program?
	private boolean mSupportsVipAccess;

	// Does this POS support AirAttach pricing?
	private boolean mShouldShowAirAttach;

	// Does this POS support loyalty rewards?
	private boolean mShouldShowRewards;

	// Does this POS require FTC warnings to be shown on checkout?
	private boolean mShouldShowFTCResortRegulations;

	// Used to determine if this POS is disabled in Production App
	private boolean mDisableForProduction;

	// EAPID value and is used
	private int mEAPID;

	/**
	 * There can be multiple different locales for a given POS.
	 * <p/>
	 * I'm purposefully obscuring this from the POS, so you don't have to figure this stuff out
	 * (we select the locale automatically based on the current Locale of the system).
	 */
	private static class PointOfSaleLocale {
		// The locale identifier (e.g., "es_AR") for this locale
		private String mLocaleIdentifier;

		// The url leading to the support part of the website
		private String mAppSupportUrl;

		// A locale specific phone number, takes precedence over the POS supportNumber
		private String mSupportNumber;

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

		// directly gives the forgot_password Url for the POS
		private String mForgotPasswordUrl;
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

	/**
	 * If there is a locale-specific support number, use that over the generic POS support number.
	 *
	 * @return
	 */
	private String getSupportPhoneNumber() {
		String number = getPosLocale().mSupportNumber;
		if (TextUtils.isEmpty(number)) {
			number = mSupportPhoneNumber;
		}
		return number;
	}

	public String getSupportPhoneNumberSilver() {
		return mSupportPhoneNumberSilver;
	}

	public String getSupportPhoneNumberGold() {
		return mSupportPhoneNumberGold;
	}

	/**
	 * If the user is a rewards member, we return the silver or gold rewards number (if available)
	 * otherwise if the user is null, or a normal user, return  the regular support number
	 *
	 * @param usr - The current logged in user, or null.
	 * @return
	 */
	public String getSupportPhoneNumberBestForUser(User usr) {
		String number = null;

		if (usr != null) {
			Traveler traveler = usr.getPrimaryTraveler();
			if (traveler.getIsLoyaltyMembershipActive()) {
				switch (traveler.getLoyaltyMembershipTier()) {
				case SILVER:
					number = getSupportPhoneNumberSilver();
					break;
				case GOLD:
					number = getSupportPhoneNumberGold();
					break;
				}
			}
		}

		return !TextUtils.isEmpty(number) ? number : getSupportPhoneNumber();
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
		return ResourceUtils.getIdentifier(R.string.class, "country_" + mTwoLetterCountryCode);
	}

	public DistanceUnit getDistanceUnit() {
		return mDistanceUnit;
	}

	public boolean requiresRulesRestrictionsCheckbox() {
		return mRequiresRulesRestrictionsCheckbox;
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

	public boolean supportsGDE() {
		return mSupportsGDE;
	}

	/**
	 * Helper method to determine if flights are enabled and if we need to even
	 * kick off a flight search - TABLETS ONLY.
	 */
	public boolean isFlightSearchEnabledTablet() {
		return mSupportsFlights && !displayFlightDropDownRoutes();
	}

	public boolean displayFlightDropDownRoutes() {
		return mDisplayFlightDropDownRoutes;
	}

	public boolean supportsGoogleWallet() {
		return mSupportsGoogleWallet;
	}

	public boolean showAtolInfo() {
		// Possible TODO: Transfer this data into ExpediaPointOfSaleConfig
		return mPointOfSale == PointOfSaleId.UNITED_KINGDOM;
	}

	public boolean showFTCResortRegulations() {
		return mShouldShowFTCResortRegulations;
	}

	// Special case breakdown dialog string for some points of sale
	public CharSequence getCostSummaryMandatoryFeeTitle(Context context) {
		if (mPointOfSale == PointOfSaleId.GERMANY || mPointOfSale == PointOfSaleId.NEW_ZEALND ||
			mPointOfSale == PointOfSaleId.AUSTRALIA || mPointOfSale == PointOfSaleId.NETHERLANDS ||
			mPointOfSale == PointOfSaleId.NORWAY || mPointOfSale == PointOfSaleId.SWEDEN) {
			return context.getString(R.string.fees_paid_at_hotel);
		}
		else {
			return context.getString(R.string.MandatoryFees);
		}
	}

	public String getLocaleIdentifier() {
		return getPosLocale().mLocaleIdentifier;
	}

	public String getAppSupportUrl() {
		return getPosLocale().mAppSupportUrl;
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

	public int getEAPID() {
		return mEAPID;
	}

	public String getForgotPasswordUrl() {
		return getPosLocale().mForgotPasswordUrl;
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

	public boolean doesNotAcceptDebitCardsForFlights() {
		return mDoesNotAcceptDebitCardsFlights;
	}

	public boolean supportsVipAccess() {
		return mSupportsVipAccess;
	}

	public boolean shouldShowAirAttach() {
		// Show Air Attach on all POS that allow hotel cross-sell
		return mShowHotelCrossSell;
	}

	public boolean shouldShowRewards() {
		return mShouldShowRewards;
	}

	public boolean isDisabledForProduction() {
		return mDisableForProduction;
	}

	/**
	 * This is equivalent to calling getStylizedHotelBookingStatement(false)
	 *
	 * @return Stylized CharSequence
	 */
	public CharSequence getStylizedHotelBookingStatement() {
		return getStylizedStatement(getPosLocale().mHotelBookingStatement, false);
	}

	/**
	 * Return the hotel booking statement with all hyperlinks underlined and bolded.
	 *
	 * @param keepHyperLinks - If this is false, the hyperlinks will no longer be URLSpan types
	 *
	 * @return Stylized CharSequence
	 **/
	public CharSequence getStylizedHotelBookingStatement(boolean keepHyperLinks) {
		return getStylizedStatement(getPosLocale().mHotelBookingStatement, keepHyperLinks);
	}

	/**
	 * This is equivalent to calling getStylizedFlightBookingState(false)
	 *
	 * @return Stylized CharSequence
	 */
	public CharSequence getStylizedFlightBookingStatement() {
		return getStylizedFlightBookingStatement(false);
	}

	/**
	 * Return the flight booking statement with all hyperlinks underlined and bolded.
	 *
	 * @param keepHyperLinks - If this is false, the hyperlinks will no longer be URLSpan types
	 *
	 * @return Stylized CharSequence
	 **/
	public CharSequence getStylizedFlightBookingStatement(boolean keepHyperLinks) {
		if (!TextUtils.isEmpty(getPosLocale().mFlightBookingStatement)) {
			return getStylizedStatement(getPosLocale().mFlightBookingStatement, keepHyperLinks);
		}
		return "FAIL FAIL FAIL LOC NEEDED: flightBookingStatement";
	}


	private CharSequence getStylizedStatement(String statement, boolean keepHyperLinks) {
		SpannableStringBuilder text = new SpannableStringBuilder(Html.fromHtml(statement));

		// Replace <a> spans with our own version: bold and underlined.
		// Partially stylistic, but also we don't want them to be clickable
		// since the whole View displaying this stylized string is clickable.
		URLSpan[] spans = text.getSpans(0, statement.length(), URLSpan.class);
		for (URLSpan o : spans) {
			int start = text.getSpanStart(o);
			int end = text.getSpanEnd(o);
			if (!keepHyperLinks) {
				text.removeSpan(o);
			}
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
		Locale locale = Locale.getDefault();
		String localeString = locale.toString();

		Log.d("PointOfSale: getPosLocale, device locale=" + localeString);

		if (mLocales.size() > 1) {
			// First look for an exact match on the Locale, languageCode and countryCode
			for (PointOfSaleLocale posLocale : mLocales) {
				if (posLocale.mLocaleIdentifier.equalsIgnoreCase(localeString)) {
					Log.d("PointOfSale: Selecting POSLocale by locale, locale=" + posLocale.mLocaleIdentifier);
					return posLocale;
				}
			}

			// If there is no exact match on Locale, attempt to match on languageCode only
			String langCode = locale.getLanguage();
			for (PointOfSaleLocale posLocale : mLocales) {
				if (posLocale.mLanguageCode.equalsIgnoreCase(langCode)) {
					Log.d("PointOfSale: Selecting POSLocale by langCode, locale=" + posLocale.mLocaleIdentifier);
					return posLocale;
				}
			}
		}

		// In the case that we can't find the right locale (or there
		// is only one locale),  default to the first locale.
		PointOfSaleLocale posLocale = mLocales.get(0);
		Log.d("PointOfSale: Selecting default POSLocale locale=" + posLocale.mLocaleIdentifier);
		return posLocale;
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

		// Load supported Expedia suggest locales
		loadExpediaSuggestSupportedLanguages(context);

		// Load Expedia countries for which Payment Postal code is optional
		loadExpediaPaymentPostalCodeOptionalCountries(context);

		// Init the cache
		getPointOfSale(context);
	}

	/**
	 * Gets the current POS, or fills it in if it not cached yet.  WARNING: do not use
	 * this unless you have reason to think sCachedPOS might be null!  This is not thread
	 * safe, if you call it from multiple threads you might mess up sCachedPOS for someone
	 * else!
	 *
	 * @return the current PointOfSale (or the default if none has been set yet)
	 */
	public static PointOfSale getPointOfSale(Context context) {
		sCachedPOS = null;

		boolean savePos = false;

		String posSetting = SettingUtils.get(context, context.getString(R.string.PointOfSaleKey), null);
		if (posSetting == null) {
			// Get the default POS.  This is rare, thus we can excuse this excessive code.
			Locale locale = Locale.getDefault();
			String country = locale.getCountry().toLowerCase(Locale.ENGLISH);
			String language = locale.getLanguage().toLowerCase(Locale.ENGLISH);

			EndPoint endPoint = EndPoint.getEndPoint(context);
			for (PointOfSale posInfo : sPointOfSale.values()) {
				//Skip Non-Prod POS, if we are in PROD Environment
				if(endPoint == EndPoint.PRODUCTION && posInfo.isDisabledForProduction()) {
					continue;
				}

				for (String defaultLocale : posInfo.mDefaultLocales) {
					defaultLocale = defaultLocale.toLowerCase(Locale.ENGLISH);
					if (defaultLocale.endsWith(country) || defaultLocale.equals(language)) {
						sCachedPOS = posInfo.mPointOfSale;
						break;
					}
				}

				if (sCachedPOS != null) {
					break;
				}
			}

			if(sCachedPOS == null) {
				sCachedPOS = ProductFlavorFeatureConfiguration.getInstance().getDefaultPOS();
			}

			savePos = true;

			Log.i("No POS set yet, chose " + sCachedPOS + " based on current locale: " + locale.toString());
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
		PointOfSale pos = getPointOfSale(context);

		// clear all data
		Db.clear();

		// Clear suggestions from tablet search
		SuggestionProvider.clearRecents(context);

		// Download new flight route data for new POS (if applicable)
		if (pos.displayFlightDropDownRoutes()) {
			CrossContextHelper.updateFlightRoutesData(context.getApplicationContext(), true);
		}
		else {
			Db.deleteCachedFlightRoutes(context);
		}

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
	// Expedia suggest supported locales

	private static Set<String> sExpediaSuggestSupportedLocales = new HashSet<String>();

	public static boolean localeSupportedByExpediaSuggest(String localeIdentifier) {
		return sExpediaSuggestSupportedLocales.contains(localeIdentifier);
	}

	public static String getSuggestLocaleIdentifier() {
		String localeIdentifier = Locale.getDefault().toString();

		if (!localeSupportedByExpediaSuggest(localeIdentifier)) {
			String posLocaleIdentifier = getPointOfSale().getLocaleIdentifier();
			localeIdentifier = localeSupportedByExpediaSuggest(posLocaleIdentifier) ? posLocaleIdentifier : "en_US";
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
			InputStream is = context.getAssets().open(ProductFlavorFeatureConfiguration.getInstance().getPOSConfigurationPath());
			String data = IoUtils.convertStreamToString(is);
			JSONObject posData = new JSONObject(data);
			Iterator<String> keys = posData.keys();
			while (keys.hasNext()) {
				String posName = keys.next();
				PointOfSale pos = parsePointOfSale(context, posData.optJSONObject(posName));
				if (pos != null) {
					pos.mTwoLetterCountryCode = posName.toLowerCase(Locale.ENGLISH);
					sPointOfSale.put(pos.mPointOfSale, pos);

					// For backwards compatibility
					sBackCompatPosMap.put(pos.mUrl, pos.mPointOfSale);
				}
			}
		}
		catch (Exception e) {
			Log.e("Failed to parse POS config", e);
			// If the POSes fail to load, then we should fail horribly
			throw new RuntimeException(e);
		}

		Log.i("Loaded POS data in " + (System.nanoTime() - start) / 1000000 + " ms");
	}

	private static PointOfSale parsePointOfSale(Context context, JSONObject data) throws JSONException {

		PointOfSaleId pointOfSaleFromId = PointOfSaleId.getPointOfSaleFromId(data.optInt("pointOfSaleId"));
		if (pointOfSaleFromId == null) {
			return null;
		}

		PointOfSale pos = new PointOfSale();
		pos.mPointOfSale = pointOfSaleFromId;
		// POS data
		pos.mThreeLetterCountryCode = data.optString("countryCode", null);

		// Server access
		pos.mUrl = data.optString("url", null);
		pos.mTPID = data.optInt("TPID");
		pos.mSiteId = data.optInt("siteId", INVALID_SITE_ID);
		pos.mEAPID = data.optInt("EAPID", INVALID_EAPID);

		// Support
		pos.mSupportPhoneNumber = parseDeviceSpecificPhoneNumber(context, data, "supportPhoneNumber");
		pos.mSupportPhoneNumberSilver = parseDeviceSpecificPhoneNumber(context, data, "supportPhoneNumberSilver");
		pos.mSupportPhoneNumberGold = parseDeviceSpecificPhoneNumber(context, data, "supportPhoneNumberGold");
		pos.mSupportEmail = data.optString("supportEmail");

		// POS config
		pos.mDistanceUnit = data.optString("distanceUnit", "").equals("miles") ? DistanceUnit.MILES
			: DistanceUnit.KILOMETERS;
		pos.mRequiresRulesRestrictionsCheckbox = data.optBoolean("explicitConsentRequired");
		pos.mDisplayBestPriceGuarantee = data.optBoolean("shouldDisplayBestPriceGuarantee");
		pos.mShowLastNameFirst = data.optBoolean("shouldShowLastNameFirst");
		pos.mHideMiddleName = data.optBoolean("shouldHideMiddleName");
		pos.mSupportsFlights = data.optBoolean("flightsEnabled");
		pos.mSupportsGDE = data.optBoolean("gdeFlightsEnabled");
		pos.mDisplayFlightDropDownRoutes = data.optBoolean("shouldDisplayFlightDropDownList");
		pos.mSupportsGoogleWallet = data.optBoolean("googleWalletEnabled");
		pos.mShowHotelCrossSell = !data.optBoolean("hideHotelCrossSell", false);
		pos.mDoesNotAcceptDebitCardsFlights = data.optBoolean("doesNotAcceptDebitCards:flights", false);
		pos.mSupportsVipAccess = data.optBoolean("supportsVipAccess", false);
		pos.mShouldShowAirAttach = data.optBoolean("shouldShowAirAttach", false);
		pos.mShouldShowRewards = data.optBoolean("shouldShowRewards", false);
		pos.mShouldShowFTCResortRegulations = data.optBoolean("shouldShowFTCResortRegulations", false);
		pos.mDisableForProduction = data.optBoolean("disableForProduction", false);

		// Parse POS locales
		JSONArray supportedLocales = data.optJSONArray("supportedLocales");
		for (int a = 0; a < supportedLocales.length(); a++) {
			pos.mLocales.add(parseLocale(supportedLocales.optJSONObject(a)));
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
		locale.mAppSupportUrl = data.optString("appSupportURL", null);
		locale.mSupportNumber = data.optString("localeSpecificSupportPhoneNumber", null);
		locale.mAppInfoUrl = data.optString("appInfoURL", null);
		locale.mWebsiteUrl = data.optString("websiteURL", null);
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
		locale.mForgotPasswordUrl = data.optString("forgotPasswordURL", null);

		// Fix one thing with the iOS-based data...
		if ("zh-Hant".equals(locale.mLanguageCode)) {
			locale.mLanguageCode = "zh";
		}

		return locale;
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
