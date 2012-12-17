package com.expedia.bookings.utils;

import java.util.Locale;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleInfo;

public class LocaleUtils {

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

	public static String convertCountryCode(String ccode) {
		if (ccode == null || ccode.length() > 2) {
			return ccode;
		}
		else {
			Locale loc = new Locale("", ccode);
			return loc.getISO3Country();
		}
	}
}
