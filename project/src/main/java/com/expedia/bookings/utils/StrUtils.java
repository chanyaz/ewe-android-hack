package com.expedia.bookings.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.cars.Suggestion;
import com.expedia.bookings.data.pos.PointOfSale;
import com.mobiata.android.LocationServices;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Waypoint;

public class StrUtils {

	// e.g. San Francisco, CA, United States (SFO-San Francisco Int'l Airport) -> San Francisco, CA
	private static final Pattern CITY_STATE_PATTERN = Pattern.compile("^([^,]+,[^,]+)");
	// e.g. Kuantan, Malaysia (KUA-Sultan Haji Ahmad Shah) -> Kuantan, Malyasia
	private static final Pattern CITY_COUNTRY_PATTERN = Pattern.compile("^([^,]+,[^,]+(?= \\(.*\\)))");
	// e.g. Kuantan, Malaysia (KUA-Sultan Haji Ahmad Shah) -> KUA-Sultan Haji Ahmad Shah
	private static final Pattern AIRPORT_CODE_PATTERN = Pattern.compile("\\((.*?)\\)");
	// e.g. San Francisco, CA, United States (SFO-San Francisco Int'l Airport) -> San Francisco, CA, United States
	private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^((.+)(?= \\(.*\\)))");
	public static final String HTML_TAGS_REGEX = "<[^>]*>";
	/**
	 * Formats the display of how many adults and children are picked currently.
	 * This will display 0 adults or children.
	 */
	public static String formatGuests(Context context, int numAdults, int numChildren) {
		StringBuilder sb = new StringBuilder();
		Resources r = context.getResources();
		sb.append(r.getQuantityString(R.plurals.number_of_adults, numAdults, numAdults));

		if (numChildren > 0) {
			sb.append(", ");
			sb.append(r.getQuantityString(R.plurals.number_of_children, numChildren, numChildren));
		}

		return sb.toString();
	}

	public static String formatGuests(Context context, HotelSearchParams searchParams) {
		return formatGuests(context, searchParams.getNumAdults(), searchParams.getNumChildren());
	}

	public static String formatAddressStreet(Location location) {
		return formatAddress(location, F_STREET_ADDRESS);
	}

	public static String formatAddressCity(Location location) {
		return formatAddress(location, F_CITY + F_STATE_CODE + F_POSTAL_CODE);
	}

	public static String formatAddressCityState(Location location) {
		return formatAddress(location, F_CITY + F_STATE_CODE);
	}

	public static String formatAddress(Location location) {
		return formatAddress(location, F_STREET_ADDRESS + F_CITY + F_STATE_CODE + F_POSTAL_CODE);
	}

	public static String formatAddressShort(Location location) {
		return formatAddress(location, F_CITY + F_STATE_CODE + F_COUNTRY_CODE);
	}

	public static final int F_STREET_ADDRESS = 1;
	public static final int F_CITY = 2;
	public static final int F_STATE_CODE = 4;
	public static final int F_POSTAL_CODE = 8;
	public static final int F_COUNTRY_CODE = 16;

	private static final int SEPARATOR_SPACE = 1;
	private static final int SEPARATOR_COMMA = 2;
	private static final int SEPARATOR_NEWLINE = 3;

	/**
	 * This is an all-purpose address formatter.  Provide flags for what
	 * information you want to display (if it is available)
	 */
	public static String formatAddress(Location location, int flags) {
		List<Object> tokens = new ArrayList<Object>();

		if ((flags & F_STREET_ADDRESS) != 0) {
			List<String> streetAddress = location.getStreetAddress();
			if (streetAddress != null) {
				int len = streetAddress.size();
				for (int a = 0; a < len; a++) {
					if (a != 0) {
						tokens.add(SEPARATOR_COMMA);
					}
					tokens.add(streetAddress.get(a));
				}

				tokens.add(SEPARATOR_NEWLINE);
			}
		}

		if ((flags & F_CITY) != 0) {
			tokens.add(location.getCity());
		}

		String stateCode = location.getStateCode();
		if ((flags & F_STATE_CODE) != 0 && stateCode != null && stateCode.length() > 0) {
			tokens.add(SEPARATOR_COMMA);
			tokens.add(stateCode);
		}

		String postalCode = location.getPostalCode();
		if ((flags & F_POSTAL_CODE) != 0 && postalCode != null && postalCode.length() > 0) {
			tokens.add(SEPARATOR_SPACE);
			tokens.add(postalCode);
		}

		String countryCode = location.getCountryCode();
		if ((flags & F_COUNTRY_CODE) != 0 && countryCode != null && countryCode.length() > 0) {
			tokens.add(SEPARATOR_COMMA);
			tokens.add(countryCode);
		}

		// Parse through the tokens, de-duplicating separators and removing missing data
		StringBuilder sb = new StringBuilder();
		int lastSeparator = 0;
		for (Object token : tokens) {
			if (token instanceof Integer) {
				if (lastSeparator != 0) {
					continue;
				}
				else {
					lastSeparator = (Integer) token;
				}
			}
			else if (token != null) {
				if (lastSeparator != 0) {
					switch (lastSeparator) {
					case SEPARATOR_SPACE:
						sb.append(" ");
						break;
					case SEPARATOR_COMMA:
						sb.append(", ");
						break;
					case SEPARATOR_NEWLINE:
						sb.append("\n");
						break;
					}
				}

				sb.append((String) token);
				lastSeparator = 0;
			}
		}

		return sb.toString().trim();
	}

	public static CharSequence[] formatAddresses(List<Address> addresses) {
		final int size = addresses.size();
		final CharSequence[] freeformLocations = new CharSequence[addresses.size()];
		for (int i = 0; i < size; i++) {
			freeformLocations[i] = removeUSAFromAddress(addresses.get(i));
		}
		return freeformLocations;
	}

	public static String removeUSAFromAddress(Address address) {
		return removeUSAFromAddress(LocationServices.formatAddress(address));
	}

	public static String removeUSAFromAddress(String address) {
		address = address.replace(", USA", "");
		address = address.replace(", United States of America", "");
		return address;
	}

	public static String formatHotelPrice(Money money) {
		return money.getFormattedMoney();
	}

	public static String formatHotelPrice(Money money, String currencyCode) {
		return money.getFormattedMoney(0, currencyCode);
	}

	public static String formatTravelerName(Traveler traveler) {
		return traveler.getFirstName() + " " + traveler.getLastName();
	}

	private static final Map<String, Locale> sCCToLocales = new HashMap<String, Locale>();

	private static void initCCToLocales() {
		if (sCCToLocales.size() == 0) {
			for (Locale locale : Locale.getAvailableLocales()) {
				String cc = locale.getCountry();
				if (cc.length() != 0) {
					sCCToLocales.put(cc, locale);
				}
			}
		}
	}

	public static String formatWaypoint(Waypoint waypoint) {
		Airport airport = waypoint.getAirport();
		if (airport != null) {
			return formatAirport(airport);
		}

		return waypoint.mAirportCode;
	}

	public static String formatAirport(Airport airport) {
		StringBuilder sb = new StringBuilder();

		if (!TextUtils.isEmpty(airport.mCity)) {
			sb.append(airport.mCity);
		}
		else {
			sb.append(airport.mAirportCode);
		}

		if (!TextUtils.isEmpty(airport.mCountryCode)) {
			String countryCode = airport.mCountryCode;
			if (countryCode.equals("US") && !TextUtils.isEmpty(airport.mStateCode)) {
				sb.append(", " + airport.mStateCode);
			}
			else {
				initCCToLocales();
				Locale locale = sCCToLocales.get(countryCode);
				String displayCountry = null;
				if (locale != null) {
					displayCountry = locale.getDisplayCountry();
				}

				if (!TextUtils.isEmpty(displayCountry)) {
					sb.append(", " + displayCountry);
				}
				else {
					sb.append(", " + airport.mCountryCode);
				}
			}
		}

		return sb.toString();
	}

	public static String getWaypointCityOrCode(Waypoint waypoint) {
		Airport airport = waypoint.getAirport();
		if (airport != null && !TextUtils.isEmpty(airport.mCity)) {
			return airport.mCity;
		}
		return waypoint.mAirportCode;
	}

	public static String getWaypointCodeOrCityStateString(Waypoint waypoint) {
		Airport airport = waypoint.getAirport();
		if (airport == null || Strings.isEmpty(airport.mCity)) {
			return waypoint.mAirportCode;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(airport.mCity);
		if (Strings.isNotEmpty(airport.mStateCode)) {
			builder.append(", ");
			builder.append(airport.mStateCode);
		}
		return builder.toString();
	}

	public static String getLocationCityOrCode(Location location) {
		String city = location.getCity();
		if (!TextUtils.isEmpty(location.getCity())) {
			return city;
		}
		return location.getDestinationId();
	}

	public static String printIntent(Intent intent) {
		if (intent == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			builder.append("Intent!\n");
			builder.append("component className=" + intent.getComponent().getClassName() + "\n");
			for (String key : extras.keySet()) {
				builder.append(String.format("key=%1s value=%2s\n", key, extras.get(key)));
			}
		}
		return builder.toString();
	}

	/**
	 * Note: Sniped from android.net.Uri source, only available on API 11+
	 *
	 * Returns a set of the unique names of all query parameters. Iterating
	 * over the set will return the names in order of their first occurrence.
	 *
	 * @throws UnsupportedOperationException if this isn't a hierarchical URI
	 *
	 * @return a set of decoded names
	 */
	public static Set<String> getQueryParameterNames(Uri uri) {
		if (uri.isOpaque()) {
			throw new UnsupportedOperationException("This isn't a hierarchical URI.");
		}

		String query = uri.getEncodedQuery();
		if (query == null) {
			return Collections.emptySet();
		}

		Set<String> names = new LinkedHashSet<String>();
		int start = 0;
		do {
			int next = query.indexOf('&', start);
			int end = (next == -1) ? query.length() : next;

			int separator = query.indexOf('=', start);
			if (separator > end || separator == -1) {
				separator = end;
			}

			String name = query.substring(start, separator);
			names.add(uri.decode(name));

			// Move start to end of name.
			start = end + 1;
		}
		while (start < query.length());

		return Collections.unmodifiableSet(names);
	}

	public static String formatCity(SuggestionV2 suggestion) {
		String city = null;
		if (suggestion.getLocation() != null) {
			city = suggestion.getLocation().getCity();
		}
		if (TextUtils.isEmpty(city)) {
			city = Html.fromHtml(suggestion.getDisplayName()).toString();
		}
		return formatCityName(city);
	}

	public static String formatCityName(String suggestion) {
		String city = suggestion;

		Matcher cityCountryMatcher = CITY_COUNTRY_PATTERN.matcher(city);
		if (cityCountryMatcher.find()) {
			city = cityCountryMatcher.group(1);
		}
		else {
			Matcher cityStateMatcher = CITY_STATE_PATTERN.matcher(city);
			if (cityStateMatcher.find()) {
				city = cityStateMatcher.group(1);
			}
		}
		return city;
	}

	public static String formatCityStateCountryName(String suggestion) {
		String displayName = suggestion;
		Matcher displayNameMatcher = DISPLAY_NAME_PATTERN.matcher(displayName);
		if (displayNameMatcher.find()) {
			displayName = displayNameMatcher.group(1);
		}
		return displayName;
	}

	public static String formatAirport(Suggestion suggestion) {
		String airportName = formatAirportName(suggestion.fullName);
		if (!airportName.equals(suggestion.airportCode)) {
			return airportName;
		}
		else {
			return formatCityName(suggestion.fullName);
		}
	}

	public static String formatAirportName(String suggestion) {
		String city = suggestion;
		Matcher cityCountryMatcher = AIRPORT_CODE_PATTERN.matcher(city);
		if (cityCountryMatcher.find()) {
			city = cityCountryMatcher.group(1);
		}
		return city;
	}

	public static String formatDisplayName(SuggestionResponse suggestionResponse) {
		String displayName = suggestionResponse.getSuggestions().get(0).getDisplayName();
		if (displayName.indexOf(",") != displayName.lastIndexOf(",")
			&& suggestionResponse.getSuggestions().size() > 1) {
			for (int i = 1; i < suggestionResponse.getSuggestions().size(); i++) {
				if (suggestionResponse.getSuggestions().get(i).getDisplayName().indexOf(",") == suggestionResponse
					.getSuggestions()
					.get(i).getDisplayName().lastIndexOf(",")) {
					displayName = suggestionResponse.getSuggestions().get(i).getDisplayName();
					break;
				}
			}
		}
		return displayName;
	}

	public static SpannableStringBuilder generateLegalClickableLink(Context context, String rulesAndRestrictionsURL) {
		SpannableStringBuilder legalTextSpan = new SpannableStringBuilder();

		String spannedRules = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			rulesAndRestrictionsURL, context.getResources().getString(R.string.rules_and_restrictions));
		String spannedTerms = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getTermsAndConditionsUrl(),
			context.getResources().getString(R.string.info_label_terms_conditions));
		String spannedPrivacy = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getPrivacyPolicyUrl(), context.getResources().getString(R.string.privacy_policy));
		String statement = context.getResources()
			.getString(R.string.legal_TEMPLATE, spannedRules, spannedTerms, spannedPrivacy);

		legalTextSpan.append(Html.fromHtml(statement));
		URLSpan[] spans = legalTextSpan.getSpans(0, statement.length(), URLSpan.class);

		for (final URLSpan span : spans) {
			int start = legalTextSpan.getSpanStart(span);
			int end = legalTextSpan.getSpanEnd(span);
			// Replace URL span with ClickableSpan to redirect to our own webview
			legalTextSpan.removeSpan(span);
			legalTextSpan.setSpan(
				new LegalClickableSpan(span.getURL(), legalTextSpan.subSequence(start, end).toString(), true), start,
				end, 0);
			legalTextSpan.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
			legalTextSpan.setSpan(new UnderlineSpan(), start, end, 0);
			legalTextSpan.setSpan(new ForegroundColorSpan(Ui.obtainThemeColor(context, R.attr.primary_color)), start,
				end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return legalTextSpan;
	}

	public static SpannableStringBuilder generateAccountCreationLegalLink(Context context) {
		SpannableStringBuilder legalTextSpan = new SpannableStringBuilder();

		String spannedTerms = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getTermsAndConditionsUrl(),
			context.getResources().getString(R.string.info_label_terms_conditions));
		String spannedPrivacy = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getPrivacyPolicyUrl(), context.getResources().getString(R.string.privacy_policy));
		String statement = context.getResources()
			.getString(R.string.account_creation_legal_TEMPLATE, spannedTerms, spannedPrivacy);

		legalTextSpan.append(Html.fromHtml(statement));
		URLSpan[] spans = legalTextSpan.getSpans(0, statement.length(), URLSpan.class);

		for (final URLSpan span : spans) {
			int start = legalTextSpan.getSpanStart(span);
			int end = legalTextSpan.getSpanEnd(span);
			// Replace URL span with ClickableSpan to redirect to our own webview
			legalTextSpan.removeSpan(span);
			legalTextSpan.setSpan(new LegalClickableSpan(span.getURL(), legalTextSpan.subSequence(start, end).toString(), false), start,
				end, 0);
			legalTextSpan.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
			legalTextSpan.setSpan(new ForegroundColorSpan(Color.WHITE), start,
				end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return legalTextSpan;
	}

	public static SpannableStringBuilder generateBulletedList(List<String> contentList) {
		SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
		for (int i = 0; i < contentList.size(); i++) {
			String content = stripHTMLTags(contentList.get(i));
			if (i < contentList.size() - 1) {
				content = content + "\n";
			}
			Spannable spannable = new SpannableString(content);
			spannable.setSpan(new BulletSpan(20), 0, spannable.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

			spannableStringBuilder.append(spannable);
		}
		return spannableStringBuilder;

	}

	public static String stripHTMLTags(String htmlContent) {
		return Html.fromHtml(htmlContent.replaceAll(HTML_TAGS_REGEX, "")).toString();
	}

	/**
	 * Fetch text of the child traveler in the spinner.
	 */
	public static String getChildTravelerAgeText(Resources res, int age) {
		age = age + GuestsPickerUtils.MIN_CHILD_AGE;
		String str = null;
		if (age == 0) {
			str = res.getString(R.string.child_age_less_than_one);
		}
		else {
			str = res.getQuantityString(R.plurals.child_age, age, age);
		}
		return Html.fromHtml(str).toString();
	}
}
