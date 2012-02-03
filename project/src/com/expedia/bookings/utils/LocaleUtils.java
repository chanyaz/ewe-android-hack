package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
import com.mobiata.android.util.SettingUtils;

public class LocaleUtils {

	@SuppressWarnings("serial")
	private static final Map<String, Integer> POINT_OF_SALE_RES_ID = new HashMap<String, Integer>() {
		{
			put("US", R.string.point_of_sale_us);
			put("UK", R.string.point_of_sale_gb);
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

	public static String getDefaultPointOfSale(Context context) {
		Locale locale = Locale.getDefault();
		String country = locale.getCountry();
		int resId = POINT_OF_SALE_RES_ID.containsKey(country) ? POINT_OF_SALE_RES_ID.get(country)
				: R.string.point_of_sale_gb;
		return context.getString(resId);
	}

	public static String getPointOfSale(Context context) {
		return SettingUtils.get(context, context.getString(R.string.PointOfSaleKey), getDefaultPointOfSale(context));
	}
}
