package com.expedia.bookings.utils;

import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.tracking.TrackingUtils;
import com.mobiata.android.Log;

public class BookingInfoUtils {

	public static final int DIALOG_BOOKING_PROGRESS = 1;
	public static final int DIALOG_BOOKING_NULL = 2;
	public static final int DIALOG_BOOKING_ERROR = 3;

	public static void focusAndOpenKeyboard(Context context, View view) {
		view.requestFocus();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, 0);
	}

	public static void onCompletedSection(Context context, String sectionName) {
		Log.d("Tracking \"" + sectionName + "\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", sectionName);
	}

	public static void onCountrySpinnerClick(Context context) {
		Log.d("Tracking \"country spinner\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.BD.ChangeCountry");
	}

	public static void onClickSubmit(Context context) {
		Log.d("Tracking \"submit\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.BD.Confirm");
	}

	public static void determineExpediaPointsDisclaimer(Context context, View view) {
		// #12652: Only display Expedia Points disclaimer if user is the in US POS.
		// (This may change in the future as more POSes support points.)
		int visibility = LocaleUtils.getPointOfSale().equals(context.getString(R.string.point_of_sale_us)) ? View.VISIBLE
				: View.GONE;
		TextView pointsDisclaimerView = (TextView) view.findViewById(R.id.expedia_points_disclaimer_text_view);
		pointsDisclaimerView.setVisibility(visibility);
	}

	//////////////////////////////////////////////////////////////////////////////////
	// More static data (that just takes up a lot of space, so at bottom)

	// Where to find security info on each card
	@SuppressWarnings("serial")
	public static final HashMap<CreditCardType, Integer> CREDIT_CARD_SECURITY_LOCATION = new HashMap<CreditCardType, Integer>() {
		{
			put(CreditCardType.AMERICAN_EXPRESS, R.string.security_code_tip_front);
			put(CreditCardType.CARTE_BLANCHE, R.string.security_code_tip_back);
			put(CreditCardType.CHINA_UNION_PAY, R.string.security_code_tip_back);
			put(CreditCardType.DINERS_CLUB, R.string.security_code_tip_back);
			put(CreditCardType.DISCOVER, R.string.security_code_tip_back);
			put(CreditCardType.JAPAN_CREDIT_BUREAU, R.string.security_code_tip_back);
			put(CreditCardType.MAESTRO, R.string.security_code_tip_back);
			put(CreditCardType.MASTERCARD, R.string.security_code_tip_back);
			put(CreditCardType.VISA, R.string.security_code_tip_back);
		}
	};

	// Which icon to use with which credit card
	@SuppressWarnings("serial")
	public static final HashMap<CreditCardType, Integer> CREDIT_CARD_ICONS = new HashMap<CreditCardType, Integer>() {
		{
			put(CreditCardType.AMERICAN_EXPRESS, R.drawable.ic_cc_amex);
			put(CreditCardType.CARTE_BLANCHE, R.drawable.ic_cc_carte_blanche);
			put(CreditCardType.CHINA_UNION_PAY, R.drawable.ic_cc_china_union_pay);
			put(CreditCardType.DINERS_CLUB, R.drawable.ic_cc_diners_club);
			put(CreditCardType.DISCOVER, R.drawable.ic_cc_discover);
			put(CreditCardType.JAPAN_CREDIT_BUREAU, R.drawable.ic_cc_jcb);
			put(CreditCardType.MAESTRO, R.drawable.ic_cc_maestro);
			put(CreditCardType.MASTERCARD, R.drawable.ic_cc_mastercard);
			put(CreditCardType.VISA, R.drawable.ic_cc_visa);
		}
	};

	// Which icon to use with which credit card
	@SuppressWarnings("serial")
	public static final HashMap<CreditCardType, Integer> CREDIT_CARD_GREY_ICONS = new HashMap<CreditCardType, Integer>() {
		{
			put(CreditCardType.AMERICAN_EXPRESS, R.drawable.ic_amex_grey);
			put(CreditCardType.CARTE_BLANCHE, R.drawable.ic_carte_blanche_grey);
			put(CreditCardType.CHINA_UNION_PAY, R.drawable.ic_union_pay_grey);
			put(CreditCardType.DINERS_CLUB, R.drawable.ic_diners_club_grey);
			put(CreditCardType.DISCOVER, R.drawable.ic_discover_grey);
			put(CreditCardType.JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_grey);
			put(CreditCardType.MAESTRO, R.drawable.ic_maestro_grey);
			put(CreditCardType.MASTERCARD, R.drawable.ic_master_card_grey);
			put(CreditCardType.VISA, R.drawable.ic_visa_grey);
		}
	};

	// Which icon to use with which credit card
	@SuppressWarnings("serial")
	public static final HashMap<CreditCardType, Integer> CREDIT_CARD_BLACK_ICONS = new HashMap<CreditCardType, Integer>() {
		{
			put(CreditCardType.AMERICAN_EXPRESS, R.drawable.ic_amex_black);
			put(CreditCardType.CARTE_BLANCHE, R.drawable.ic_carte_blanche_black);
			put(CreditCardType.CHINA_UNION_PAY, R.drawable.ic_union_pay_black);
			put(CreditCardType.DINERS_CLUB, R.drawable.ic_diners_club_black);
			put(CreditCardType.DISCOVER, R.drawable.ic_discover_black);
			put(CreditCardType.JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_black);
			put(CreditCardType.MAESTRO, R.drawable.ic_maestro_black);
			put(CreditCardType.MASTERCARD, R.drawable.ic_master_card_black);
			put(CreditCardType.VISA, R.drawable.ic_visa_black);
		}
	};
	
	// Which icon to use with which credit card
		@SuppressWarnings("serial")
		public static final HashMap<CreditCardType, Integer> CREDIT_CARD_WHITE_ICONS = new HashMap<CreditCardType, Integer>() {
			{
				put(CreditCardType.AMERICAN_EXPRESS, R.drawable.ic_amex_white);
				put(CreditCardType.CARTE_BLANCHE, R.drawable.ic_carte_blanche_white);
				put(CreditCardType.CHINA_UNION_PAY, R.drawable.ic_union_pay_white);
				put(CreditCardType.DINERS_CLUB, R.drawable.ic_diners_club_white);
				put(CreditCardType.DISCOVER, R.drawable.ic_discover_white);
				put(CreditCardType.JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_white);
				put(CreditCardType.MAESTRO, R.drawable.ic_maestro_white);
				put(CreditCardType.MASTERCARD, R.drawable.ic_master_card_white);
				put(CreditCardType.VISA, R.drawable.ic_visa_white);
			}
		};

	// Which icon to use with which credit card
	@SuppressWarnings("serial")
	public static final HashMap<CreditCardType, Integer> CREDIT_CARD_CVV_ICONS = new HashMap<CreditCardType, Integer>() {
		{
			put(CreditCardType.AMERICAN_EXPRESS, R.drawable.ic_amex_grey_cvv);
			put(CreditCardType.CARTE_BLANCHE, R.drawable.ic_carte_blanche_grey_cvv);
			put(CreditCardType.CHINA_UNION_PAY, R.drawable.ic_union_pay_grey_cvv);
			put(CreditCardType.DINERS_CLUB, R.drawable.ic_diners_club_grey_cvv);
			put(CreditCardType.DISCOVER, R.drawable.ic_discover_grey_cvv);
			put(CreditCardType.JAPAN_CREDIT_BUREAU, R.drawable.ic_jcb_grey_cvv);
			put(CreditCardType.MAESTRO, R.drawable.ic_maestro_grey_cvv);
			put(CreditCardType.MASTERCARD, R.drawable.ic_master_card_grey_cvv);
			put(CreditCardType.VISA, R.drawable.ic_visa_grey_cvv);
		}
	};

	// Static data that auto-fills states/countries
	@SuppressWarnings("serial")
	public static final HashMap<CharSequence, Integer> COMMON_US_CITIES = new HashMap<CharSequence, Integer>() {
		{
			put("new york", R.string.state_code_ny);
			put("los angeles", R.string.state_code_ca);
			put("chicago", R.string.state_code_il);
			put("houston", R.string.state_code_tx);
			put("philadelphia", R.string.state_code_pa);
			put("phoenix", R.string.state_code_az);
			put("san antonio", R.string.state_code_tx);
			put("san diego", R.string.state_code_ca);
			put("dallas", R.string.state_code_tx);
			put("san jose", R.string.state_code_ca);
			put("jacksonville", R.string.state_code_fl);
			put("indianapolis", R.string.state_code_in);
			put("san francisco", R.string.state_code_ca);
			put("austin", R.string.state_code_tx);
			put("columbus", R.string.state_code_oh);
			put("fort worth", R.string.state_code_tx);
			put("charlotte", R.string.state_code_nc);
			put("detroit", R.string.state_code_mi);
			put("el paso", R.string.state_code_tx);
			put("memphis", R.string.state_code_tn);
			put("baltimore", R.string.state_code_md);
			put("boston", R.string.state_code_ma);
			put("seattle", R.string.state_code_wa);
			put("washington", R.string.state_code_dc);
			put("nashville", R.string.state_code_tn);
			put("denver", R.string.state_code_co);
			put("louisville", R.string.state_code_ky);
			put("milwaukee", R.string.state_code_wi);
			put("portland", R.string.state_code_or);
			put("las vegas", R.string.state_code_nv);
			put("oklahoma city", R.string.state_code_ok);
			put("albuquerque", R.string.state_code_nm);
			put("tucson", R.string.state_code_az);
			put("fresno", R.string.state_code_ca);
			put("sacramento", R.string.state_code_ca);
			put("long beach", R.string.state_code_ca);
			put("kansas city", R.string.state_code_mo);
			put("mesa", R.string.state_code_az);
			put("virginia beach", R.string.state_code_va);
			put("atlanta", R.string.state_code_ga);
			put("colorado springs", R.string.state_code_co);
			put("omaha", R.string.state_code_ne);
			put("raleigh", R.string.state_code_nc);
			put("miami", R.string.state_code_fl);
			put("cleveland", R.string.state_code_oh);
			put("tulsa", R.string.state_code_ok);
			put("oakland", R.string.state_code_ca);
			put("minneapolis", R.string.state_code_mn);
			put("wichita", R.string.state_code_ks);
			put("arlington", R.string.state_code_tx);
			put("bakersfield", R.string.state_code_ca);
			put("new orleans", R.string.state_code_la);
			put("honolulu", R.string.state_code_hi);
			put("anaheim", R.string.state_code_ca);
			put("tampa", R.string.state_code_fl);
			put("aurora", R.string.state_code_co);
			put("santa ana", R.string.state_code_ca);
			put("st louis", R.string.state_code_mo);
			put("pittsburgh", R.string.state_code_pa);
			put("corpus christi", R.string.state_code_tx);
			put("riverside", R.string.state_code_ca);
			put("cincinnati", R.string.state_code_oh);
			put("lexington", R.string.state_code_ky);
			put("anchorage", R.string.state_code_ak);
			put("stockton", R.string.state_code_ca);
			put("toledo", R.string.state_code_oh);
			put("st paul", R.string.state_code_mn);
			put("newark", R.string.state_code_nj);
			put("greensboro", R.string.state_code_nc);
			put("buffalo", R.string.state_code_ny);
			put("plano", R.string.state_code_tx);
			put("lincoln", R.string.state_code_ne);
			put("henderson", R.string.state_code_nv);
			put("fort wayne", R.string.state_code_in);
			put("jersey city", R.string.state_code_nj);
			put("st petersburg", R.string.state_code_fl);
			put("chula vista", R.string.state_code_ca);
			put("norfolk", R.string.state_code_va);
			put("orlando", R.string.state_code_fl);
			put("chandler", R.string.state_code_az);
			put("laredo", R.string.state_code_tx);
			put("madison", R.string.state_code_wi);
			put("winston-salem", R.string.state_code_nc);
			put("lubbock", R.string.state_code_tx);
			put("baton rouge", R.string.state_code_la);
			put("durham", R.string.state_code_nc);
			put("garland", R.string.state_code_tx);
			put("glendale", R.string.state_code_az);
			put("reno", R.string.state_code_nv);
			put("hialeah", R.string.state_code_fl);
			put("paradise", R.string.state_code_nv);
			put("chesapeake", R.string.state_code_va);
			put("scottsdale", R.string.state_code_az);
			put("north las vegas", R.string.state_code_nv);
			put("irving", R.string.state_code_tx);
			put("fremont", R.string.state_code_ca);
			put("irvine", R.string.state_code_ca);
			put("birmingham", R.string.state_code_al);
			put("rochester", R.string.state_code_ny);
			put("san bernardino", R.string.state_code_ca);
			put("spokane", R.string.state_code_wa);
			put("gilbert", R.string.state_code_az);
			put("arlington", R.string.state_code_va);
			put("montgomery", R.string.state_code_al);
			put("boise", R.string.state_code_id);
			put("richmond", R.string.state_code_va);
			put("des moines", R.string.state_code_ia);
			put("modesto", R.string.state_code_ca);
			put("fayetteville", R.string.state_code_nc);
			put("shreveport", R.string.state_code_la);
			put("akron", R.string.state_code_oh);
			put("tacoma", R.string.state_code_wa);
			put("aurora", R.string.state_code_il);
			put("oxnard", R.string.state_code_ca);
			put("fontana", R.string.state_code_ca);
			put("yonkers", R.string.state_code_ny);
			put("augusta", R.string.state_code_ga);
			put("mobile", R.string.state_code_al);
			put("little rock", R.string.state_code_ar);
			put("moreno valley", R.string.state_code_ca);
			put("glendale", R.string.state_code_ca);
			put("amarillo", R.string.state_code_tx);
			put("huntington beach", R.string.state_code_ca);
			put("columbus", R.string.state_code_ga);
			put("grand rapids", R.string.state_code_mi);
			put("salt lake city", R.string.state_code_ut);
			put("tallahassee", R.string.state_code_fl);
			put("worcester", R.string.state_code_ma);
			put("newport news", R.string.state_code_va);
			put("huntsville", R.string.state_code_al);
			put("knoxville", R.string.state_code_tn);
			put("providence", R.string.state_code_ri);
			put("santa clarita", R.string.state_code_ca);
			put("grand prairie", R.string.state_code_tx);
			put("brownsville", R.string.state_code_tx);
			put("jackson", R.string.state_code_ms);
			put("overland park", R.string.state_code_ks);
			put("garden grove", R.string.state_code_ca);
			put("santa rosa", R.string.state_code_ca);
			put("chattanooga", R.string.state_code_tn);
			put("oceanside", R.string.state_code_ca);
			put("fort lauderdale", R.string.state_code_fl);
			put("rancho cucamonga", R.string.state_code_ca);
			put("port st. lucie", R.string.state_code_fl);
			put("ontario", R.string.state_code_ca);
			put("vancouver", R.string.state_code_wa);
			put("tempe", R.string.state_code_az);
			put("springfield", R.string.state_code_mo);
			put("lancaster", R.string.state_code_ca);
			put("eugene", R.string.state_code_or);
			put("pembroke pines", R.string.state_code_fl);
			put("salem", R.string.state_code_or);
			put("cape coral", R.string.state_code_fl);
			put("peoria", R.string.state_code_az);
			put("sioux falls", R.string.state_code_sd);
			put("springfield", R.string.state_code_ma);
			put("elk grove", R.string.state_code_ca);
			put("rockford", R.string.state_code_il);
			put("palmdale", R.string.state_code_ca);
			put("corona", R.string.state_code_ca);
			put("salinas", R.string.state_code_ca);
			put("pomona", R.string.state_code_ca);
			put("pasadena", R.string.state_code_tx);
			put("joliet", R.string.state_code_il);
			put("paterson", R.string.state_code_nj);
			put("kansas city", R.string.state_code_ks);
			put("torrance", R.string.state_code_ca);
			put("syracuse", R.string.state_code_ny);
			put("bridgeport", R.string.state_code_ct);
			put("hayward", R.string.state_code_ca);
			put("fort collins", R.string.state_code_co);
			put("escondido", R.string.state_code_ca);
			put("lakewood", R.string.state_code_co);
			put("naperville", R.string.state_code_il);
			put("dayton", R.string.state_code_oh);
			put("hollywood", R.string.state_code_fl);
			put("sunnyvale", R.string.state_code_ca);
			put("alexandria", R.string.state_code_va);
			put("mesquite", R.string.state_code_tx);
			put("hampton", R.string.state_code_va);
			put("pasadena", R.string.state_code_ca);
			put("orange", R.string.state_code_ca);
			put("savannah", R.string.state_code_ga);
			put("cary", R.string.state_code_nc);
			put("fullerton", R.string.state_code_ca);
			put("warren", R.string.state_code_mi);
			put("clarksville", R.string.state_code_tn);
			put("mckinney", R.string.state_code_tx);
			put("mcallen", R.string.state_code_tx);
			put("new haven", R.string.state_code_ct);
			put("sterling heights", R.string.state_code_mi);
			put("west valley city", R.string.state_code_ut);
			put("columbia", R.string.state_code_sc);
			put("killeen", R.string.state_code_tx);
			put("topeka", R.string.state_code_ks);
			put("thousand oaks", R.string.state_code_ca);
			put("cedar rapids", R.string.state_code_ia);
			put("olathe", R.string.state_code_ks);
			put("elizabeth", R.string.state_code_nj);
			put("waco", R.string.state_code_tx);
			put("hartford", R.string.state_code_ct);
			put("visalia", R.string.state_code_ca);
			put("gainesville", R.string.state_code_fl);
			put("simi valley", R.string.state_code_ca);
			put("stamford", R.string.state_code_ct);
			put("bellevue", R.string.state_code_wa);
			put("concord", R.string.state_code_ca);
			put("miramar", R.string.state_code_fl);
			put("coral springs", R.string.state_code_fl);
			put("lafayette", R.string.state_code_la);
			put("charleston", R.string.state_code_sc);
			put("carrollton", R.string.state_code_tx);
			put("roseville", R.string.state_code_ca);
			put("thornton", R.string.state_code_co);
			put("beaumont", R.string.state_code_tx);
			put("allentown", R.string.state_code_pa);
			put("surprise", R.string.state_code_az);
			put("evansville", R.string.state_code_in);
			put("abilene", R.string.state_code_tx);
			put("frisco", R.string.state_code_tx);
			put("independence", R.string.state_code_mo);
			put("santa clara", R.string.state_code_ca);
			put("springfield", R.string.state_code_il);
			put("vallejo", R.string.state_code_ca);
			put("victorville", R.string.state_code_ca);
			put("athens", R.string.state_code_ga);
			put("peoria", R.string.state_code_il);
			put("lansing", R.string.state_code_mi);
			put("ann arbor", R.string.state_code_mi);
			put("el monte", R.string.state_code_ca);
			put("denton", R.string.state_code_tx);
			put("berkeley", R.string.state_code_ca);
			put("provo", R.string.state_code_ut);
			put("downey", R.string.state_code_ca);
			put("midland", R.string.state_code_tx);
			put("norman", R.string.state_code_ok);
			put("waterbury", R.string.state_code_ct);
			put("costa mesa", R.string.state_code_ca);
			put("inglewood", R.string.state_code_ca);
			put("manchester", R.string.state_code_nh);
			put("murfreesboro", R.string.state_code_tn);
			put("columbia", R.string.state_code_mo);
			put("elgin", R.string.state_code_il);
			put("clearwater", R.string.state_code_fl);
			put("miami gardens", R.string.state_code_fl);
			put("rochester", R.string.state_code_mn);
			put("pueblo", R.string.state_code_co);
			put("lowell", R.string.state_code_ma);
			put("wilmington", R.string.state_code_nc);
			put("arvada", R.string.state_code_co);
			put("ventura", R.string.state_code_ca);
			put("westminster", R.string.state_code_co);
			put("west covina", R.string.state_code_ca);
			put("gresham", R.string.state_code_or);
			put("fargo", R.string.state_code_nd);
			put("norwalk", R.string.state_code_ca);
			put("carlsbad", R.string.state_code_ca);
			put("fairfield", R.string.state_code_ca);
			put("cambridge", R.string.state_code_ma);
			put("wichita falls", R.string.state_code_tx);
			put("high point", R.string.state_code_nc);
			put("billings", R.string.state_code_mt);
			put("green bay", R.string.state_code_wi);
			put("west jordan", R.string.state_code_ut);
			put("richmond", R.string.state_code_ca);
			put("murrieta", R.string.state_code_ca);
			put("burbank", R.string.state_code_ca);
			put("palm bay", R.string.state_code_fl);
			put("everett", R.string.state_code_wa);
			put("flint", R.string.state_code_mi);
			put("antioch", R.string.state_code_ca);
			put("erie", R.string.state_code_pa);
			put("south bend", R.string.state_code_in);
			put("daly city", R.string.state_code_ca);
			put("centennial", R.string.state_code_co);
			put("temecula", R.string.state_code_ca);
		}
	};
}
