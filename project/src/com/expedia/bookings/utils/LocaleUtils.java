package com.expedia.bookings.utils;

import java.util.Locale;

import android.content.Context;

import com.expedia.bookings.R;

public class LocaleUtils {

	public static String getDefaultPointOfSale(Context context) {
		Locale locale = Locale.getDefault();
		int resId = getPointOfSaleResId(locale);
		return context.getString(resId > 0 ? resId : R.string.point_of_sale_us);
	}

	private static int getPointOfSaleResId(Locale locale) {
		String localeString = locale.toString();
		if (localeString.equals("en_US")) {
			return R.string.point_of_sale_us;
		}
		else if (localeString.equals("en_GB")) {
			return R.string.point_of_sale_uk;
		}
		else if (localeString.equals("en_AU")) {
			return R.string.point_of_sale_au;
		}
		else if (localeString.equals("fr_FR")) {
			return R.string.point_of_sale_fr;
		}
		else if (localeString.equals("de_DE")) {
			return R.string.point_of_sale_de;
		}
		else if (localeString.equals("it_IT")) {
			return R.string.point_of_sale_it;
		}
		else if (localeString.equals("nl_NL")) {
			return R.string.point_of_sale_nl;
		}
		else if (localeString.equals("es_ES")) {
			return R.string.point_of_sale_es;
		}
		else if (localeString.equals("nb_NO")) {
			return R.string.point_of_sale_no;
		}
		else if (localeString.equals("da_DK")) {
			return R.string.point_of_sale_dk;
		}
		else if (localeString.equals("sv_SE")) {
			return R.string.point_of_sale_se;
		}
		else if (localeString.equals("en_IE")) {
			return R.string.point_of_sale_ie;
		}
		else if (localeString.equals("fr_BE")) {
			return R.string.point_of_sale_be;
		}
		else if (localeString.equals("nl_BE")) {
			return R.string.point_of_sale_be;
		}
		else if (localeString.equals("en_CA")) {
			return R.string.point_of_sale_ca;
		}
		else if (localeString.equals("fr_CA")) {
			return R.string.point_of_sale_ca;
		}
		else if (localeString.equals("en_NZ")) {
			return R.string.point_of_sale_nz;
		}
		else if (localeString.equals("ja_JP")) {
			return R.string.point_of_sale_jp;
		}
		else if (localeString.equals("es_MX")) {
			return R.string.point_of_sale_mx;
		}
		else if (localeString.equals("en_SG")) {
			return R.string.point_of_sale_sg;
		}
		else if (localeString.equals("ms_MY")) {
			return R.string.point_of_sale_my;
		}
		else if (localeString.equals("ko_KR")) {
			return R.string.point_of_sale_kr;
		}
		else if (localeString.equals("th_TH")) {
			return R.string.point_of_sale_th;
		}
		else if (localeString.equals("fil_PH")) {
			return R.string.point_of_sale_ph;
		}
		else if (localeString.equals("en_PH")) {
			return R.string.point_of_sale_ph;
		}
		else if (localeString.equals("id_ID")) {
			return R.string.point_of_sale_id;
		}
		else if (localeString.equals("pt_BR")) {
			return R.string.point_of_sale_br;
		}
		else if (localeString.equals("zh-HK")) {
			return R.string.point_of_sale_hk;
		}
		else if (localeString.equals("zh-TW")) {
			return R.string.point_of_sale_tw;
		}
		else if (localeString.equals("id")) {
			return R.string.point_of_sale_id;
		}

		return 0;
	}
}
