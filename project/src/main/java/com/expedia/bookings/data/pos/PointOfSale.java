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
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
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
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.server.CrossContextHelper;
import com.expedia.bookings.server.EndPoint;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.IoUtils;
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

	private static final int INVALID_SITE_ID = -1;

	public static final int INVALID_EAPID = -1;

	// The identifier for this point of sale
	private PointOfSaleId mPointOfSale;

	// List of locales associated with this POS
	private List<PointOfSaleLocale> mLocales = new ArrayList<>();

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

	// The POS's silver rewards member contact email
	private String mSupportEmailSilver;

	// The POS's gold rewards member contact email
	private String mSupportEmailGold;

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

	//Whether we should provide example names
	private boolean mShowExampleNames;

	//Whether we should hide middle name
	private boolean mHideMiddleName;

	// Whether or not to let users access flights on this POS
	private boolean mSupportsFlights;

	// Whether to show cars on this POS
	private boolean mSupportsCars;

	// Whether to show activities on this POS
	private boolean mSupportsLx;

	// Whether or not to use downloaded routes (for AirAsia) or not
	private boolean mDisplayFlightDropDownRoutes;

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

	// Does this POS support loyalty rewards?
	private boolean mShouldShowRewards;

	// Does this POS require FTC warnings to be shown on checkout?
	private boolean mShouldShowFTCResortRegulations;

	// Used to determine if this POS is disabled in Production App
	private boolean mDisableForProduction;

	// EAPID value and is used
	private int mEAPID;

	// Should we show strikethrough prices on half-width launch tiles for this POS?
	private boolean mShowHalfTileStrikethroughPrice;

	// Should we show free cancellation of flights for this POS?
	private boolean mShowFlightsFreeCancellation;

	// Should we show the marketing opt in checkbox
	private MarketingOptIn mMarketingOptIn;

	// Should we show cirlces for rating
	private boolean shouldShowCircleForRatings;

	// 5810 - Do Airlines Charge Additional Fee Based On Payment Method?
	private boolean doAirlinesChargeAdditionalFeeBasedOnPaymentMethod;

	private static boolean mIsTablet;

	private boolean mRequiresHotelPostalCode;

	private boolean isPwPEnabledForHotels;

	private boolean isSWPEnabledForHotels;
	private boolean isEarnMessageEnabledForHotels;
	private boolean isEarnMessageEnabledForFlights;

	private static class CountryResources {
		@StringRes
		int countryNameResId;
		@DrawableRes
		int countryFlagResId;

		CountryResources(@StringRes int nameResId) {
			this(nameResId, 0);
		}

		CountryResources(@StringRes int nameResId, @DrawableRes int flagResId) {
			countryNameResId = nameResId;
			countryFlagResId = flagResId;
		}
	}
	private static Map<String, CountryResources> sCountryCodeMap;

	private static void setUpCountryCodeMap() {
		sCountryCodeMap = new HashMap<>();
		sCountryCodeMap.put("af", new CountryResources(R.string.country_af));
		sCountryCodeMap.put("al", new CountryResources(R.string.country_al));
		sCountryCodeMap.put("dz", new CountryResources(R.string.country_dz));
		sCountryCodeMap.put("as", new CountryResources(R.string.country_as));
		sCountryCodeMap.put("ad", new CountryResources(R.string.country_ad));
		sCountryCodeMap.put("ao", new CountryResources(R.string.country_ao));
		sCountryCodeMap.put("ai", new CountryResources(R.string.country_ai));
		sCountryCodeMap.put("aq", new CountryResources(R.string.country_aq));
		sCountryCodeMap.put("ag", new CountryResources(R.string.country_ag));
		sCountryCodeMap.put("ar", new CountryResources(R.string.country_ar, R.drawable.ic_flag_ar));
		sCountryCodeMap.put("am", new CountryResources(R.string.country_am));
		sCountryCodeMap.put("aw", new CountryResources(R.string.country_aw));
		sCountryCodeMap.put("au", new CountryResources(R.string.country_au, R.drawable.ic_flag_au));
		sCountryCodeMap.put("at", new CountryResources(R.string.country_at, R.drawable.ic_flag_at));
		sCountryCodeMap.put("az", new CountryResources(R.string.country_az));
		sCountryCodeMap.put("bs", new CountryResources(R.string.country_bs));
		sCountryCodeMap.put("bh", new CountryResources(R.string.country_bh));
		sCountryCodeMap.put("bd", new CountryResources(R.string.country_bd));
		sCountryCodeMap.put("bb", new CountryResources(R.string.country_bb));
		sCountryCodeMap.put("by", new CountryResources(R.string.country_by));
		sCountryCodeMap.put("be", new CountryResources(R.string.country_be, R.drawable.ic_flag_be));
		sCountryCodeMap.put("bz", new CountryResources(R.string.country_bz));
		sCountryCodeMap.put("bj", new CountryResources(R.string.country_bj));
		sCountryCodeMap.put("bm", new CountryResources(R.string.country_bm));
		sCountryCodeMap.put("bt", new CountryResources(R.string.country_bt));
		sCountryCodeMap.put("bo", new CountryResources(R.string.country_bo));
		sCountryCodeMap.put("ba", new CountryResources(R.string.country_ba));
		sCountryCodeMap.put("bw", new CountryResources(R.string.country_bw));
		sCountryCodeMap.put("br", new CountryResources(R.string.country_br, R.drawable.ic_flag_br));
		sCountryCodeMap.put("io", new CountryResources(R.string.country_io));
		sCountryCodeMap.put("bn", new CountryResources(R.string.country_bn));
		sCountryCodeMap.put("bg", new CountryResources(R.string.country_bg));
		sCountryCodeMap.put("bf", new CountryResources(R.string.country_bf));
		sCountryCodeMap.put("bi", new CountryResources(R.string.country_bi));
		sCountryCodeMap.put("kh", new CountryResources(R.string.country_kh));
		sCountryCodeMap.put("cm", new CountryResources(R.string.country_cm));
		sCountryCodeMap.put("ca", new CountryResources(R.string.country_ca, R.drawable.ic_flag_ca));
		sCountryCodeMap.put("cv", new CountryResources(R.string.country_cv));
		sCountryCodeMap.put("ky", new CountryResources(R.string.country_ky));
		sCountryCodeMap.put("cf", new CountryResources(R.string.country_cf));
		sCountryCodeMap.put("td", new CountryResources(R.string.country_td));
		sCountryCodeMap.put("cl", new CountryResources(R.string.country_cl));
		sCountryCodeMap.put("cn", new CountryResources(R.string.country_cn));
		sCountryCodeMap.put("cx", new CountryResources(R.string.country_cx));
		sCountryCodeMap.put("cc", new CountryResources(R.string.country_cc));
		sCountryCodeMap.put("co", new CountryResources(R.string.country_co));
		sCountryCodeMap.put("km", new CountryResources(R.string.country_km));
		sCountryCodeMap.put("cg", new CountryResources(R.string.country_cg));
		sCountryCodeMap.put("cd", new CountryResources(R.string.country_cd));
		sCountryCodeMap.put("ck", new CountryResources(R.string.country_ck));
		sCountryCodeMap.put("cr", new CountryResources(R.string.country_cr));
		sCountryCodeMap.put("hr", new CountryResources(R.string.country_hr));
		sCountryCodeMap.put("cy", new CountryResources(R.string.country_cy));
		sCountryCodeMap.put("cz", new CountryResources(R.string.country_cz));
		sCountryCodeMap.put("ci", new CountryResources(R.string.country_ci));
		sCountryCodeMap.put("dk", new CountryResources(R.string.country_dk, R.drawable.ic_flag_dk));
		sCountryCodeMap.put("dj", new CountryResources(R.string.country_dj));
		sCountryCodeMap.put("dm", new CountryResources(R.string.country_dm));
		sCountryCodeMap.put("do", new CountryResources(R.string.country_do));
		sCountryCodeMap.put("ec", new CountryResources(R.string.country_ec));
		sCountryCodeMap.put("eg", new CountryResources(R.string.country_eg));
		sCountryCodeMap.put("sv", new CountryResources(R.string.country_sv));
		sCountryCodeMap.put("gq", new CountryResources(R.string.country_gq));
		sCountryCodeMap.put("er", new CountryResources(R.string.country_er));
		sCountryCodeMap.put("ee", new CountryResources(R.string.country_ee));
		sCountryCodeMap.put("et", new CountryResources(R.string.country_et));
		sCountryCodeMap.put("fk", new CountryResources(R.string.country_fk));
		sCountryCodeMap.put("fo", new CountryResources(R.string.country_fo));
		sCountryCodeMap.put("fj", new CountryResources(R.string.country_fj));
		sCountryCodeMap.put("fi", new CountryResources(R.string.country_fi, R.drawable.ic_flag_fi));
		sCountryCodeMap.put("fr", new CountryResources(R.string.country_fr, R.drawable.ic_flag_fr));
		sCountryCodeMap.put("gf", new CountryResources(R.string.country_gf));
		sCountryCodeMap.put("pf", new CountryResources(R.string.country_pf));
		sCountryCodeMap.put("ga", new CountryResources(R.string.country_ga));
		sCountryCodeMap.put("gm", new CountryResources(R.string.country_gm));
		sCountryCodeMap.put("ge", new CountryResources(R.string.country_ge));
		sCountryCodeMap.put("de", new CountryResources(R.string.country_de, R.drawable.ic_flag_de));
		sCountryCodeMap.put("gh", new CountryResources(R.string.country_gh));
		sCountryCodeMap.put("gi", new CountryResources(R.string.country_gi));
		sCountryCodeMap.put("gr", new CountryResources(R.string.country_gr));
		sCountryCodeMap.put("gl", new CountryResources(R.string.country_gl));
		sCountryCodeMap.put("gd", new CountryResources(R.string.country_gd));
		sCountryCodeMap.put("gp", new CountryResources(R.string.country_gp));
		sCountryCodeMap.put("gu", new CountryResources(R.string.country_gu));
		sCountryCodeMap.put("gt", new CountryResources(R.string.country_gt));
		sCountryCodeMap.put("gg", new CountryResources(R.string.country_gg));
		sCountryCodeMap.put("gn", new CountryResources(R.string.country_gn));
		sCountryCodeMap.put("gw", new CountryResources(R.string.country_gw));
		sCountryCodeMap.put("gy", new CountryResources(R.string.country_gy));
		sCountryCodeMap.put("ht", new CountryResources(R.string.country_ht));
		sCountryCodeMap.put("va", new CountryResources(R.string.country_va));
		sCountryCodeMap.put("hn", new CountryResources(R.string.country_hn));
		sCountryCodeMap.put("hk", new CountryResources(R.string.country_hk, R.drawable.ic_flag_hk));
		sCountryCodeMap.put("hu", new CountryResources(R.string.country_hu));
		sCountryCodeMap.put("is", new CountryResources(R.string.country_is));
		sCountryCodeMap.put("in", new CountryResources(R.string.country_in, R.drawable.ic_flag_in));
		sCountryCodeMap.put("id", new CountryResources(R.string.country_id, R.drawable.ic_flag_id));
		sCountryCodeMap.put("ir", new CountryResources(R.string.country_ir));
		sCountryCodeMap.put("iq", new CountryResources(R.string.country_iq));
		sCountryCodeMap.put("ie", new CountryResources(R.string.country_ie, R.drawable.ic_flag_ie));
		sCountryCodeMap.put("im", new CountryResources(R.string.country_im));
		sCountryCodeMap.put("il", new CountryResources(R.string.country_il));
		sCountryCodeMap.put("it", new CountryResources(R.string.country_it, R.drawable.ic_flag_it));
		sCountryCodeMap.put("jm", new CountryResources(R.string.country_jm));
		sCountryCodeMap.put("jp", new CountryResources(R.string.country_jp, R.drawable.ic_flag_jp));
		sCountryCodeMap.put("je", new CountryResources(R.string.country_je));
		sCountryCodeMap.put("jo", new CountryResources(R.string.country_jo));
		sCountryCodeMap.put("kz", new CountryResources(R.string.country_kz));
		sCountryCodeMap.put("ke", new CountryResources(R.string.country_ke));
		sCountryCodeMap.put("ki", new CountryResources(R.string.country_ki));
		sCountryCodeMap.put("kp", new CountryResources(R.string.country_kp));
		sCountryCodeMap.put("kr", new CountryResources(R.string.country_kr, R.drawable.ic_flag_kr));
		sCountryCodeMap.put("kw", new CountryResources(R.string.country_kw));
		sCountryCodeMap.put("kg", new CountryResources(R.string.country_kg));
		sCountryCodeMap.put("la", new CountryResources(R.string.country_la));
		sCountryCodeMap.put("lv", new CountryResources(R.string.country_lv));
		sCountryCodeMap.put("lb", new CountryResources(R.string.country_lb));
		sCountryCodeMap.put("ls", new CountryResources(R.string.country_ls));
		sCountryCodeMap.put("lr", new CountryResources(R.string.country_lr));
		sCountryCodeMap.put("ly", new CountryResources(R.string.country_ly));
		sCountryCodeMap.put("li", new CountryResources(R.string.country_li));
		sCountryCodeMap.put("lt", new CountryResources(R.string.country_lt));
		sCountryCodeMap.put("lu", new CountryResources(R.string.country_lu));
		sCountryCodeMap.put("mo", new CountryResources(R.string.country_mo));
		sCountryCodeMap.put("mk", new CountryResources(R.string.country_mk));
		sCountryCodeMap.put("mg", new CountryResources(R.string.country_mg));
		sCountryCodeMap.put("mw", new CountryResources(R.string.country_mw));
		sCountryCodeMap.put("my", new CountryResources(R.string.country_my, R.drawable.ic_flag_my));
		sCountryCodeMap.put("mv", new CountryResources(R.string.country_mv));
		sCountryCodeMap.put("ml", new CountryResources(R.string.country_ml));
		sCountryCodeMap.put("mt", new CountryResources(R.string.country_mt));
		sCountryCodeMap.put("mh", new CountryResources(R.string.country_mh));
		sCountryCodeMap.put("mq", new CountryResources(R.string.country_mq));
		sCountryCodeMap.put("mr", new CountryResources(R.string.country_mr));
		sCountryCodeMap.put("mu", new CountryResources(R.string.country_mu));
		sCountryCodeMap.put("yt", new CountryResources(R.string.country_yt));
		sCountryCodeMap.put("mx", new CountryResources(R.string.country_mx, R.drawable.ic_flag_mx));
		sCountryCodeMap.put("fm", new CountryResources(R.string.country_fm));
		sCountryCodeMap.put("md", new CountryResources(R.string.country_md));
		sCountryCodeMap.put("mc", new CountryResources(R.string.country_mc));
		sCountryCodeMap.put("mn", new CountryResources(R.string.country_mn));
		sCountryCodeMap.put("me", new CountryResources(R.string.country_me));
		sCountryCodeMap.put("ms", new CountryResources(R.string.country_ms));
		sCountryCodeMap.put("ma", new CountryResources(R.string.country_ma));
		sCountryCodeMap.put("mz", new CountryResources(R.string.country_mz));
		sCountryCodeMap.put("mm", new CountryResources(R.string.country_mm));
		sCountryCodeMap.put("na", new CountryResources(R.string.country_na));
		sCountryCodeMap.put("nr", new CountryResources(R.string.country_nr));
		sCountryCodeMap.put("np", new CountryResources(R.string.country_np));
		sCountryCodeMap.put("nl", new CountryResources(R.string.country_nl, R.drawable.ic_flag_nl));
		sCountryCodeMap.put("an", new CountryResources(R.string.country_an));
		sCountryCodeMap.put("nc", new CountryResources(R.string.country_nc));
		sCountryCodeMap.put("nz", new CountryResources(R.string.country_nz, R.drawable.ic_flag_nz));
		sCountryCodeMap.put("ni", new CountryResources(R.string.country_ni));
		sCountryCodeMap.put("ne", new CountryResources(R.string.country_ne));
		sCountryCodeMap.put("ng", new CountryResources(R.string.country_ng));
		sCountryCodeMap.put("nu", new CountryResources(R.string.country_nu));
		sCountryCodeMap.put("nf", new CountryResources(R.string.country_nf));
		sCountryCodeMap.put("mp", new CountryResources(R.string.country_mp));
		sCountryCodeMap.put("no", new CountryResources(R.string.country_no, R.drawable.ic_flag_no));
		sCountryCodeMap.put("om", new CountryResources(R.string.country_om));
		sCountryCodeMap.put("pk", new CountryResources(R.string.country_pk));
		sCountryCodeMap.put("pw", new CountryResources(R.string.country_pw));
		sCountryCodeMap.put("ps", new CountryResources(R.string.country_ps));
		sCountryCodeMap.put("pa", new CountryResources(R.string.country_pa));
		sCountryCodeMap.put("pg", new CountryResources(R.string.country_pg));
		sCountryCodeMap.put("py", new CountryResources(R.string.country_py));
		sCountryCodeMap.put("pe", new CountryResources(R.string.country_pe));
		sCountryCodeMap.put("ph", new CountryResources(R.string.country_ph, R.drawable.ic_flag_ph));
		sCountryCodeMap.put("pn", new CountryResources(R.string.country_pn));
		sCountryCodeMap.put("pl", new CountryResources(R.string.country_pl));
		sCountryCodeMap.put("pt", new CountryResources(R.string.country_pt));
		sCountryCodeMap.put("pr", new CountryResources(R.string.country_pr));
		sCountryCodeMap.put("qa", new CountryResources(R.string.country_qa));
		sCountryCodeMap.put("ro", new CountryResources(R.string.country_ro));
		sCountryCodeMap.put("ru", new CountryResources(R.string.country_ru));
		sCountryCodeMap.put("rw", new CountryResources(R.string.country_rw));
		sCountryCodeMap.put("re", new CountryResources(R.string.country_re));
		sCountryCodeMap.put("bl", new CountryResources(R.string.country_bl));
		sCountryCodeMap.put("sh", new CountryResources(R.string.country_sh));
		sCountryCodeMap.put("kn", new CountryResources(R.string.country_kn));
		sCountryCodeMap.put("lc", new CountryResources(R.string.country_lc));
		sCountryCodeMap.put("mf", new CountryResources(R.string.country_mf));
		sCountryCodeMap.put("pm", new CountryResources(R.string.country_pm));
		sCountryCodeMap.put("vc", new CountryResources(R.string.country_vc));
		sCountryCodeMap.put("ws", new CountryResources(R.string.country_ws));
		sCountryCodeMap.put("sm", new CountryResources(R.string.country_sm));
		sCountryCodeMap.put("st", new CountryResources(R.string.country_st));
		sCountryCodeMap.put("sa", new CountryResources(R.string.country_sa));
		sCountryCodeMap.put("sn", new CountryResources(R.string.country_sn));
		sCountryCodeMap.put("rs", new CountryResources(R.string.country_rs));
		sCountryCodeMap.put("sc", new CountryResources(R.string.country_sc));
		sCountryCodeMap.put("sl", new CountryResources(R.string.country_sl));
		sCountryCodeMap.put("sg", new CountryResources(R.string.country_sg, R.drawable.ic_flag_sg));
		sCountryCodeMap.put("sk", new CountryResources(R.string.country_sk));
		sCountryCodeMap.put("si", new CountryResources(R.string.country_si));
		sCountryCodeMap.put("sb", new CountryResources(R.string.country_sb));
		sCountryCodeMap.put("so", new CountryResources(R.string.country_so));
		sCountryCodeMap.put("za", new CountryResources(R.string.country_za));
		sCountryCodeMap.put("gs", new CountryResources(R.string.country_gs));
		sCountryCodeMap.put("es", new CountryResources(R.string.country_es, R.drawable.ic_flag_es));
		sCountryCodeMap.put("lk", new CountryResources(R.string.country_lk));
		sCountryCodeMap.put("sd", new CountryResources(R.string.country_sd));
		sCountryCodeMap.put("sr", new CountryResources(R.string.country_sr));
		sCountryCodeMap.put("sj", new CountryResources(R.string.country_sj));
		sCountryCodeMap.put("sz", new CountryResources(R.string.country_sz));
		sCountryCodeMap.put("se", new CountryResources(R.string.country_se, R.drawable.ic_flag_se));
		sCountryCodeMap.put("ch", new CountryResources(R.string.country_ch, R.drawable.ic_flag_ch));
		sCountryCodeMap.put("sy", new CountryResources(R.string.country_sy));
		sCountryCodeMap.put("tw", new CountryResources(R.string.country_tw, R.drawable.ic_flag_tw));
		sCountryCodeMap.put("tj", new CountryResources(R.string.country_tj));
		sCountryCodeMap.put("tz", new CountryResources(R.string.country_tz));
		sCountryCodeMap.put("th", new CountryResources(R.string.country_th, R.drawable.ic_flag_th));
		sCountryCodeMap.put("tl", new CountryResources(R.string.country_tl));
		sCountryCodeMap.put("tg", new CountryResources(R.string.country_tg));
		sCountryCodeMap.put("tk", new CountryResources(R.string.country_tk));
		sCountryCodeMap.put("to", new CountryResources(R.string.country_to));
		sCountryCodeMap.put("tt", new CountryResources(R.string.country_tt));
		sCountryCodeMap.put("tn", new CountryResources(R.string.country_tn));
		sCountryCodeMap.put("tr", new CountryResources(R.string.country_tr));
		sCountryCodeMap.put("tm", new CountryResources(R.string.country_tm));
		sCountryCodeMap.put("tc", new CountryResources(R.string.country_tc));
		sCountryCodeMap.put("tv", new CountryResources(R.string.country_tv));
		sCountryCodeMap.put("ug", new CountryResources(R.string.country_ug));
		sCountryCodeMap.put("ua", new CountryResources(R.string.country_ua));
		sCountryCodeMap.put("ae", new CountryResources(R.string.country_ae));
		sCountryCodeMap.put("gb", new CountryResources(R.string.country_gb, R.drawable.ic_flag_gb));
		sCountryCodeMap.put("us", new CountryResources(R.string.country_us, R.drawable.ic_flag_us));
		sCountryCodeMap.put("um", new CountryResources(R.string.country_um));
		sCountryCodeMap.put("uy", new CountryResources(R.string.country_uy));
		sCountryCodeMap.put("uz", new CountryResources(R.string.country_uz));
		sCountryCodeMap.put("vu", new CountryResources(R.string.country_vu));
		sCountryCodeMap.put("ve", new CountryResources(R.string.country_ve));
		sCountryCodeMap.put("vn", new CountryResources(R.string.country_vn, R.drawable.ic_flag_vn));
		sCountryCodeMap.put("vg", new CountryResources(R.string.country_vg));
		sCountryCodeMap.put("vi", new CountryResources(R.string.country_vi));
		sCountryCodeMap.put("wf", new CountryResources(R.string.country_wf));
		sCountryCodeMap.put("eh", new CountryResources(R.string.country_eh));
		sCountryCodeMap.put("ye", new CountryResources(R.string.country_ye));
		sCountryCodeMap.put("zm", new CountryResources(R.string.country_zm));
		sCountryCodeMap.put("zw", new CountryResources(R.string.country_zw));
		sCountryCodeMap.put("ax", new CountryResources(R.string.country_ax));
	}

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

		// The URL for Loyalty Terms and Conditions for this POS
		private String mLoyaltyTermsAndConditionsUrl;

		// The URL for Help Url for Airlines Additional Fee Based On Payment Method for this POS
		private String airlineFeeBasedOnPaymentMethodTermsAndConditionsURL;

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

		// Account creation marketing text
		private String mMarketingText;
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

	public String getSupportEmailSilver() {
		return mSupportEmailSilver;
	}

	public String getSupportEmailGold() {
		return mSupportEmailGold;
	}

	/**
	 * If the user is a rewards member, we return the silver or gold rewards number (if available)
	 * otherwise if the user is null, or a normal user, return  the regular support number
	 *
	 * @param usr - The current logged in user, or null.
	 * @return the best support phone number for the current user
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

	public String getTwoLetterCountryCode() {
		return mTwoLetterCountryCode;
	}

	public String getThreeLetterCountryCode() {
		return mThreeLetterCountryCode;
	}

	public @StringRes int getCountryNameResId() {
		if (sCountryCodeMap == null) {
			setUpCountryCodeMap();
		}
		return sCountryCodeMap.get(mTwoLetterCountryCode).countryNameResId;
	}

	public @DrawableRes int getCountryFlagResId() {
		if (sCountryCodeMap == null) {
			setUpCountryCodeMap();
		}
		return sCountryCodeMap.get(mTwoLetterCountryCode).countryFlagResId;
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

	public boolean showExampleNames() {
		return mShowExampleNames;
	}

	public boolean hideMiddleName() {
		return mHideMiddleName;
	}

	public boolean requiresHotelPostalCode() {
		return mRequiresHotelPostalCode;
	}

	public boolean supports(LineOfBusiness lob) {
		switch (lob) {
		case CARS:
			return mSupportsCars && !mIsTablet;
		case LX:
			return mSupportsLx && !mIsTablet;
		case FLIGHTS:
			return mSupportsFlights;
		case HOTELS:
			return true;

		}

		return false;
	}

	public boolean supportsStrikethroughPrice() {
		return mShowHalfTileStrikethroughPrice;
	}

	public boolean supportsFlightsFreeCancellation() {
		return mShowFlightsFreeCancellation;
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

	public boolean showAtolInfo() {
		// Possible TODO: Transfer this data into ExpediaPointOfSaleConfig
		return mPointOfSale == PointOfSaleId.UNITED_KINGDOM;
	}

	public boolean showFTCResortRegulations() {
		return mShouldShowFTCResortRegulations;
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

	public String getLoyaltyTermsAndConditionsURL() {
		return getPosLocale().mLoyaltyTermsAndConditionsUrl;
	}

	public String getAirlineFeeBasedOnPaymentMethodTermsAndConditionsURL() {
		return getPosLocale().airlineFeeBasedOnPaymentMethodTermsAndConditionsURL;
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

	public String getHotelBookingStatement() {
		return getPosLocale().mHotelBookingStatement;
	}

	// TODO: As more complicated payment combinations arise, think about a refactor

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

	public boolean shouldShowRewards() {
		return mShouldShowRewards;
	}

	public boolean isDisabledForProduction() {
		return mDisableForProduction;
	}

	public boolean shouldShowMarketingOptIn() {
		return mMarketingOptIn != MarketingOptIn.DO_NOT_SHOW && mMarketingOptIn != MarketingOptIn.DO_NOT_SHOW_AUTO_ENROLL;
	}

	public boolean shouldEnableMarketingOptIn() {
		return mMarketingOptIn == MarketingOptIn.SHOW_CHECKED || mMarketingOptIn == MarketingOptIn.DO_NOT_SHOW_AUTO_ENROLL;
	}

	public String getMarketingText() {
		return getPosLocale().mMarketingText;
	}

	public boolean shouldShowCircleForRatings() {
		return shouldShowCircleForRatings;
	}

	public boolean doAirlinesChargeAdditionalFeeBasedOnPaymentMethod() {
		return doAirlinesChargeAdditionalFeeBasedOnPaymentMethod;
	}

	public boolean isPwPEnabledForHotels() {
		return isPwPEnabledForHotels;
	}

	public boolean isSWPEnabledForHotels() {
		return isSWPEnabledForHotels;
	}

	public boolean isEarnMessageEnabledForHotels() {
		return isEarnMessageEnabledForHotels;
	}

	public PointOfSale setEarnMessageEnabledForHotels(boolean earnMessageEnabledForHotels) {
		isEarnMessageEnabledForHotels = earnMessageEnabledForHotels;
		return this;
	}

	public boolean isEarnMessageEnabledForFlights() {
		return isEarnMessageEnabledForFlights;
	}

	public void setEarnMessageEnabledForFlights(boolean earnMessageEnabledForFlights) {
		isEarnMessageEnabledForFlights = earnMessageEnabledForFlights;
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
	 * @return Stylized CharSequence
	 */
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
	 * @return Stylized CharSequence
	 */
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
	private static final Map<PointOfSaleId, PointOfSale> sPointOfSale = new HashMap<>();

	// This is a backwards-compatible map from the old setting (which was based on a string) to a POS
	private static final Map<String, PointOfSaleId> sBackCompatPosMap = new HashMap<>();

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
		mIsTablet = AndroidUtils.isTablet(context);
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

			EndPoint endPoint = Ui.getApplication(context).appComponent().endpointProvider().getEndPoint();
			for (PointOfSale posInfo : sPointOfSale.values()) {
				//Skip Non-Prod POS, if we are in PROD Environment
				if (endPoint == EndPoint.PRODUCTION && posInfo.isDisabledForProduction()) {
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

			if (sCachedPOS == null) {
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

		Log.d("New POS id: " + sCachedPOS);
	}

	// Provide context for sorting purposes
	public static List<PointOfSale> getAllPointsOfSale(final Context context) {
		List<PointOfSale> poses = new ArrayList<>(sPointOfSale.values());

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

	private static Set<String> sExpediaSuggestSupportedLocales = new HashSet<>();

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
			InputStream is = context.getAssets()
				.open(ProductFlavorFeatureConfiguration.getInstance().getPOSConfigurationPath());
			String data = IoUtils.convertStreamToString(is);
			JSONObject posData = new JSONObject(data);
			Iterator<String> keys = posData.keys();
			while (keys.hasNext()) {
				String posName = keys.next();
				PointOfSale pos = parsePointOfSale(context, posName, posData.optJSONObject(posName));
				if (pos != null) {
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

	private static PointOfSale parsePointOfSale(Context context, String posName, JSONObject data) throws JSONException {

		PointOfSaleId pointOfSaleFromId = PointOfSaleId.getPointOfSaleFromId(data.optInt("pointOfSaleId"));
		if (pointOfSaleFromId == null) {
			return null;
		}

		PointOfSale pos = new PointOfSale();
		pos.mPointOfSale = pointOfSaleFromId;
		// POS data
		pos.mThreeLetterCountryCode = data.optString("countryCode", null);

		//By default the POS Key represents Two Letter Country Code
		//with provision of override via the "twoLetterCountryCode" element
		pos.mTwoLetterCountryCode = data.optString("twoLetterCountryCode", posName).toLowerCase(Locale.ENGLISH);
		// Server access
		pos.mUrl = data.optString("url", null);
		pos.mTPID = data.optInt("TPID");
		pos.mSiteId = data.optInt("siteId", INVALID_SITE_ID);
		pos.mEAPID = data.optInt("EAPID", INVALID_EAPID);

		// Support
		pos.mSupportPhoneNumber = parseDeviceSpecificPhoneNumber(context, data, "supportPhoneNumber");
		pos.mSupportPhoneNumberSilver = parseDeviceSpecificPhoneNumber(context, data, "supportPhoneNumberSilver");
		pos.mSupportPhoneNumberGold = parseDeviceSpecificPhoneNumber(context, data, "supportPhoneNumberGold");

		// Support email
		pos.mSupportEmailGold = data.optString("supportEmailGold", null);
		pos.mSupportEmailSilver = data.optString("supportEmailSilver", null);

		// POS config
		pos.mDistanceUnit = data.optString("distanceUnit", "").equals("miles") ? DistanceUnit.MILES
			: DistanceUnit.KILOMETERS;
		pos.mRequiresRulesRestrictionsCheckbox = data.optBoolean("explicitConsentRequired");
		pos.mDisplayBestPriceGuarantee = data.optBoolean("shouldDisplayBestPriceGuarantee");
		pos.mShowLastNameFirst = data.optBoolean("shouldShowLastNameFirst");
		pos.mShowExampleNames = data.optBoolean("shouldShowExampleNames");
		pos.mHideMiddleName = data.optBoolean("shouldHideMiddleName");
		pos.mSupportsFlights = data.optBoolean("flightsEnabled");
		pos.mSupportsCars = data.optBoolean("carsEnabled");
		pos.mSupportsLx = data.optBoolean("lxEnabled");
		pos.mDisplayFlightDropDownRoutes = data.optBoolean("shouldDisplayFlightDropDownList");
		pos.mShowHotelCrossSell = !data.optBoolean("hideHotelCrossSell", false);
		pos.mDoesNotAcceptDebitCardsFlights = data.optBoolean("doesNotAcceptDebitCards:flights", false);
		pos.mSupportsVipAccess = data.optBoolean("supportsVipAccess", false);
		pos.mShouldShowRewards = data.optBoolean("shouldShowRewards", false);
		pos.mShouldShowFTCResortRegulations = data.optBoolean("shouldShowFTCResortRegulations", false);
		pos.mDisableForProduction = data.optBoolean("disableForProduction", false);
		pos.mShowHalfTileStrikethroughPrice = data.optBoolean("launchScreenStrikethroughEnabled", false);
		pos.mShowFlightsFreeCancellation = data.optBoolean("shouldShowFlightsFreeCancellation", false);
		pos.mMarketingOptIn = MarketingOptIn
			.valueOf(data.optString("marketingOptIn", MarketingOptIn.DO_NOT_SHOW.name()));
		pos.shouldShowCircleForRatings = data.optBoolean("shouldDisplayCirclesForRatings", false);
		pos.doAirlinesChargeAdditionalFeeBasedOnPaymentMethod = data.optBoolean("doAirlinesChargeAdditionalFeeBasedOnPaymentMethod", false);
		pos.mRequiresHotelPostalCode = data.optString("requiredPaymentFields:hotels").equals("postalCode");

		pos.isPwPEnabledForHotels = data.optBoolean("pwpEnabled:hotels", false);
		pos.isSWPEnabledForHotels = data.optBoolean("swpEnabled:hotels", false);
		pos.isEarnMessageEnabledForHotels = data.optBoolean("earnMessageEnabled:hotels", false);
		pos.isEarnMessageEnabledForFlights = data.optBoolean("enableFlightSearchEarnMessaging", false);

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
		locale.mLoyaltyTermsAndConditionsUrl = data.optString("loyaltyTermsAndConditionsURL", null);
		locale.mTermsOfBookingUrl = data.optString("termsOfBookingURL", null);
		locale.mPrivacyPolicyUrl = data.optString("privacyPolicyURL", null);
		locale.airlineFeeBasedOnPaymentMethodTermsAndConditionsURL = data.optString("airlineFeeBasedOnPaymentMethodTermsAndConditionsURL", null);

		// Language identifier
		locale.mLanguageCode = data.optString("languageCode", null);
		locale.mLanguageId = data.optInt("languageIdentifier");
		locale.mForgotPasswordUrl = data.optString("forgotPasswordURL", null);
		locale.mMarketingText = data.optString("createAccountMarketingText");
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

	private static Set<String> sExpediaPaymentPostalCodeOptionalCountries = new HashSet<>();

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

	public enum MarketingOptIn {
		DO_NOT_SHOW_AUTO_ENROLL,
		SHOW_CHECKED,
		SHOW_UNCHECKED,
		DO_NOT_SHOW
	}
}
