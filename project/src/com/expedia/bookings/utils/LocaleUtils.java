package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
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

	private static Map<String, String> sTPIDs;

	public static String getTPID(Context context) {
		if (sTPIDs == null) {
			sTPIDs = ResourceUtils.getStringMap(context, R.array.tpid_map);
		}

		return sTPIDs.get(getPointOfSale());
	}
}