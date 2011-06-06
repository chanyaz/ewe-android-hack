package com.expedia.bookings.utils;

import java.util.Locale;

import android.content.Context;
import android.text.Html;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

public class RulesRestrictionsUtils {

	/**
	 * @return true if this locale requires us to get explicit user acceptance of the rules & restrictions
	 */
	public static boolean requiresRulesRestrictionsCheckbox() {
		String countryCode = Locale.getDefault().getCountry().toLowerCase();
		return countryCode.equals("fr") || countryCode.equals("it") || countryCode.equals("es")
						|| countryCode.equals("no");
	}

	/**
	 * @return the text to insert into a TextView at the bottom of the BookingInfoActivity
	 */
	public static CharSequence getRulesRestrictionsConfirmation(Context context) {
		Locale locale = Locale.getDefault();

		String countryCode = locale.getCountry().toLowerCase();
		String languageCode = locale.getLanguage().toLowerCase();

		// Use reflection to get both the disclaimer and its array of URLs to fill it with.
		int disclaimerId = -1;
		int urlArrayId = -1;

		try {
			disclaimerId = R.string.class.getField("rules_restrictions_disclaimer_" + countryCode + "_" + languageCode)
					.getInt(null);
		}
		catch (Exception ignore) {
			try {
				disclaimerId = R.string.class.getField("rules_restrictions_disclaimer_" + countryCode).getInt(null);
			}
			catch (Exception ignore2) {
				// Ignore
			}
		}

		try {
			urlArrayId = R.array.class
					.getField("rule_restrictions_disclaimer_urls_" + countryCode + "_" + languageCode).getInt(null);
		}
		catch (Exception ignore) {
			try {
				urlArrayId = R.array.class.getField("rule_restrictions_disclaimer_urls_" + countryCode).getInt(null);
			}
			catch (Exception ignore2) {
				// Ignore
			}
		}

		// If not found, use the default (UK)
		if (disclaimerId == -1 || urlArrayId == -1) {
			Log.d("Could not find rules & restrictions; countryCode=" + countryCode + " languageCode=" + languageCode
					+ " disclaimerId=" + disclaimerId + " urlArrayId=" + urlArrayId);
			disclaimerId = R.string.rules_restrictions_disclaimer_gb;
			urlArrayId = R.array.rule_restrictions_disclaimer_urls_gb;
		}

		String text = context.getString(disclaimerId, (Object[]) context.getResources().getStringArray(urlArrayId));

		return Html.fromHtml(text);
	}
}
