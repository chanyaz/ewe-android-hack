package com.expedia.bookings.utils;

import java.util.Locale;

import android.content.Context;
import android.text.Html;

import com.expedia.bookings.R;
import com.expedia.bookings.data.pos.PointOfSaleInfo;
import com.mobiata.android.util.ResourceUtils;

public class RulesRestrictionsUtils {

	public static String getTermsAndConditionsUrl(Context context) {
		String pos = PointOfSaleInfo.getPointOfSaleInfo().getUrl();

		// Handle special dual-language cases
		int resId = -1;
		String langId = Locale.getDefault().getLanguage().toLowerCase();
		if (pos.equals(context.getString(R.string.point_of_sale_be))) {
			if (langId.equals("nl")) {
				resId = R.string.terms_conditions_url_be_nl;
			}
			else if (langId.equals("fr")) {
				resId = R.string.terms_conditions_url_be_fr;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ca))) {
			if (langId.equals("en")) {
				resId = R.string.terms_conditions_url_ca_en;
			}
			else if (langId.equals("fr")) {
				resId = R.string.terms_conditions_url_ca_fr;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_hk))) {
			if (langId.equals("en")) {
				resId = R.string.terms_conditions_url_hk_en;
			}
			else if (langId.equals("zh")) {
				resId = R.string.terms_conditions_url_hk_zh;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_my))) {
			if (langId.equals("en")) {
				resId = R.string.terms_conditions_url_my_en;
			}
			else if (langId.equals("ms")) {
				resId = R.string.terms_conditions_url_my_ms;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ph))) {
			if (langId.equals("en")) {
				resId = R.string.terms_conditions_url_ph_en;
			}
			else if (langId.equals("tl")) {
				resId = R.string.terms_conditions_url_ph_tl;
			}
		}

		if (resId != -1) {
			return context.getString(resId);
		}
		else {
			return ResourceUtils.getStringMap(context, R.array.terms_conditions_map).get(pos);
		}
	}

	public static String getPrivacyPolicyUrl(Context context) {
		String pos = PointOfSaleInfo.getPointOfSaleInfo().getUrl();

		// Handle special dual-language cases
		int resId = -1;
		String langId = Locale.getDefault().getLanguage().toLowerCase();
		if (pos.equals(context.getString(R.string.point_of_sale_be))) {
			if (langId.equals("nl")) {
				resId = R.string.privacy_policy_url_be_nl;
			}
			else if (langId.equals("fr")) {
				resId = R.string.privacy_policy_url_be_fr;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ca))) {
			if (langId.equals("en")) {
				resId = R.string.privacy_policy_url_ca_en;
			}
			else if (langId.equals("fr")) {
				resId = R.string.privacy_policy_url_ca_fr;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_hk))) {
			if (langId.equals("en")) {
				resId = R.string.privacy_policy_url_hk_en;
			}
			else if (langId.equals("zh")) {
				resId = R.string.privacy_policy_url_hk_zh;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_my))) {
			if (langId.equals("en")) {
				resId = R.string.privacy_policy_url_my_en;
			}
			else if (langId.equals("ms")) {
				resId = R.string.privacy_policy_url_my_ms;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ph))) {
			if (langId.equals("en")) {
				resId = R.string.privacy_policy_url_ph_en;
			}
			else if (langId.equals("tl")) {
				resId = R.string.privacy_policy_url_ph_tl;
			}
		}

		if (resId != -1) {
			return context.getString(resId);
		}
		else {
			return ResourceUtils.getStringMap(context, R.array.privacy_policy_map).get(pos);
		}
	}

	public static CharSequence getRulesRestrictionsConfirmation(Context context) {
		String pos = PointOfSaleInfo.getPointOfSaleInfo().getUrl();

		// Handle special dual-language cases
		int resId = -1;
		String langId = Locale.getDefault().getLanguage().toLowerCase();
		if (pos.equals(context.getString(R.string.point_of_sale_be))) {
			if (langId.equals("nl")) {
				resId = R.string.rules_restrictions_disclaimer_be_nl;
			}
			else if (langId.equals("fr")) {
				resId = R.string.rules_restrictions_disclaimer_be_fr;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ca))) {
			if (langId.equals("en")) {
				resId = R.string.rules_restrictions_disclaimer_ca_en;
			}
			else if (langId.equals("fr")) {
				resId = R.string.rules_restrictions_disclaimer_ca_fr;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_hk))) {
			if (langId.equals("en")) {
				resId = R.string.rules_restrictions_disclaimer_hk_en;
			}
			else if (langId.equals("zh")) {
				resId = R.string.rules_restrictions_disclaimer_hk_zh;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_my))) {
			if (langId.equals("en")) {
				resId = R.string.rules_restrictions_disclaimer_my_en;
			}
			else if (langId.equals("ms")) {
				resId = R.string.rules_restrictions_disclaimer_my_ms;
			}
		}
		else if (pos.equals(context.getString(R.string.point_of_sale_ph))) {
			if (langId.equals("en")) {
				resId = R.string.rules_restrictions_disclaimer_ph_en;
			}
			else if (langId.equals("tl")) {
				resId = R.string.rules_restrictions_disclaimer_ph_tl;
			}
		}

		String template;
		if (resId != -1) {
			template = context.getString(resId);
		}
		else {
			template = ResourceUtils.getStringMap(context, R.array.rules_restrictions_disclaimer_map).get(pos);
		}

		if (template != null) {
			return Html.fromHtml(String.format(template, getTermsAndConditionsUrl(context),
					getPrivacyPolicyUrl(context)));
		}

		return null;
	}
}
