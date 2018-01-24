package com.expedia.bookings.utils;

import java.text.DecimalFormat;
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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import com.expedia.bookings.R;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SuggestionV4;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.text.HtmlCompat;
import com.mobiata.android.LocationServices;
import com.mobiata.flightlib.data.Airport;
import com.mobiata.flightlib.data.Waypoint;
import com.squareup.phrase.Phrase;

public class StrUtils {

	// e.g. San Francisco, CA, United States (SFO-San Francisco Int'l Airport) -> San Francisco, CA
	private static final Pattern CITY_STATE_PATTERN = Pattern.compile("^([^,]+,[^,]+)");
	// e.g. Kuantan, Malaysia (KUA-Sultan Haji Ahmad Shah) -> Kuantan, Malyasia
	private static final Pattern CITY_COUNTRY_PATTERN = Pattern.compile("^([^,]+,[^,]+(?= \\(.*\\)))");
	// e.g. San Francisco, CA, United States (SFO-San Francisco Int'l Airport) -> San Francisco
	private static final Pattern CITY_PATTERN = Pattern.compile("^([^,]+)");
	// e.g. Kuantan, Malaysia (KUA-Sultan Haji Ahmad Shah) -> KUA-Sultan Haji Ahmad Shah
	private static final Pattern AIRPORT_CODE_PATTERN = Pattern.compile("\\((.*?)\\)");
	// e.g. San Francisco, CA, United States (SFO-San Francisco Int'l Airport) -> San Francisco, CA, United States
	private static final Pattern DISPLAY_NAME_PATTERN = Pattern.compile("^((.+)(?= \\(.*\\)))");
	// e.g. Seattle, WA (SEA-Seattle - Tacoma Intl.) -> WA
	private static final Pattern STATE_CODE_PATTERN = Pattern.compile("\\, (.*?)\\ ");
	private static final String HTML_TAGS_REGEX = "<[^>]*>";
	private static final Pattern CITY_STATE_PATTERN_PACKAGE = Pattern.compile("^[^\\(]+");
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
		List<Object> tokens = new ArrayList<>();

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
				if (lastSeparator == 0) {
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
			return formatAirport(airport, waypoint.mCity);
		}

		return waypoint.mAirportCode;
	}

	public static String formatAirport(Airport airport, String localizedAirportCity) {
		StringBuilder sb = new StringBuilder();

		if (!TextUtils.isEmpty(localizedAirportCity)) {
			sb.append(localizedAirportCity);
		}
		else if (!TextUtils.isEmpty(airport.mCity)) {
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

	public static String getWaypointLocalizedCityOrCode(Waypoint waypoint) {
		if (!TextUtils.isEmpty(waypoint.mCity)) {
			return waypoint.mCity;
		}
		else {
			return getWaypointCityOrCode(waypoint);
		}
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
	 * Returns a set of the unique names of all query parameters. Iterating
	 * over the set will return the names in order of their first occurrence.
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
			names.add(Uri.decode(name));

			// Move start to end of name.
			start = end + 1;
		}
		while (start < query.length());

		return Collections.unmodifiableSet(names);
	}

	public static String formatCity(SuggestionV4 suggestion) {
		return SuggestionStrUtils.formatCityName(HtmlCompat.stripHtml(suggestion.regionNames.displayName));
	}

	public static String formatCityStateCountryName(String suggestion) {
		String displayName = suggestion;
		Matcher displayNameMatcher = DISPLAY_NAME_PATTERN.matcher(displayName);
		if (displayNameMatcher.find()) {
			displayName = displayNameMatcher.group(1);
		}
		return displayName;
	}

	public static String formatCityStateName(String suggestion) {
		if (suggestion == null) {
			return "";
		}
		String displayName = suggestion;
		Matcher displayNameMatcher = CITY_STATE_PATTERN_PACKAGE.matcher(displayName);
		if (displayNameMatcher.find()) {
			displayName = displayNameMatcher.group(0);
		}
		return displayName;
	}

	public static String formatAirportName(String suggestion) {
		String city = suggestion;
		Matcher cityCountryMatcher = AIRPORT_CODE_PATTERN.matcher(city);
		if (cityCountryMatcher.find()) {
			city = cityCountryMatcher.group(1);
		}
		if (city != null && city.contains("-")) {
			city = city.replace("-", " - ");
		}
		return city;
	}

	public static String formatCityName(SuggestionV4 suggestion) {
		String city = HtmlCompat.stripHtml(suggestion.regionNames.displayName);
		Matcher cityMatcher = CITY_PATTERN.matcher(city);
		if (cityMatcher.find()) {
			city = cityMatcher.group(1);
		}
		return city;
	}

	public static String formatCityName(String suggestion) {
		String city = "";
		Matcher cityMatcher = CITY_PATTERN.matcher(suggestion);
		if (cityMatcher.find()) {
			city = cityMatcher.group(1);
		}
		return city;
	}

	@Nullable
	public static String formatStateName(String suggestion) {
		if (suggestion == null) {
			return null;
		}
		String state = null;
		Matcher stateMatcher = STATE_CODE_PATTERN.matcher(suggestion);
		if (stateMatcher.find()) {
			state = stateMatcher.group(1);
		}
		return state;
	}

	@Nullable
	public static String formatPackageCityName(String suggestion) {
		if (suggestion == null) {
			return null;
		}
		String city = "";
		Matcher cityMatcher = CITY_PATTERN.matcher(suggestion);
		if (cityMatcher.find()) {
			city = cityMatcher.group(1);
		}
		CharSequence[] cityValue = city.split(" \\(");
		city = cityValue[0].toString();
		return city;
	}

	public static String formatAirportCodeCityName(SuggestionV4 suggestion) {
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(suggestion.hierarchyInfo.airport.airportCode).append(") ");
		sb.append(formatCityName(suggestion));
		return sb.toString();
	}

	public static String formatAirportCodeCityName(FlightLeg flight) {
		String city = flight.destinationCity;
		String airportCode = flight.destinationAirportCode;
		StringBuilder sb = new StringBuilder();
		sb.append("(").append(airportCode).append(") ");
		sb.append(city);
		return sb.toString();
	}

	public static String formatCarOriginDescription(Context context, FlightLeg flightLeg) {
		return Phrase.from(context, R.string.cars_cross_sell_origin_TEMPLATE)
			.put("airport_code", flightLeg.destinationAirportCode)
			.put("airport_localname", flightLeg.destinationAirportLocalName)
			.format().toString();
	}

	public static SpannableStringBuilder generateBaggageFeesTextWithClickableLinks(Context context,
		String outboundBaggageFeeUrl, String inboundBaggageFeeUrl) {
		String baggageFeesTextFormatted = Phrase.from(context, R.string.split_ticket_baggage_fees_TEMPLATE)
			.put("departurelink", outboundBaggageFeeUrl)
			.put("returnlink", inboundBaggageFeeUrl)
			.format().toString();
		return getSpannableTextByColor(baggageFeesTextFormatted,
			ContextCompat.getColor(context, R.color.flight_primary_color),
			true);
	}

	public static SpannableStringBuilder generateStatementWithClickableLink(String statementWithAnchorLink) {
		return getSpannableTextByColor(statementWithAnchorLink, Color.WHITE, true);
	}

	public static SpannableStringBuilder generateHotelsBookingStatement(Context context, String hotelBookingStatement,
		boolean makeClickable) {
		return getSpannableTextByPrimaryColor(context, hotelBookingStatement, makeClickable);
	}

	public static SpannableStringBuilder generateLegalClickableLink(Context context, String rulesAndRestrictionsURL) {
		String spannedRules = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			rulesAndRestrictionsURL, context.getResources().getString(R.string.rules_and_restrictions));
		String spannedTerms = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getTermsAndConditionsUrl(),
			context.getResources().getString(R.string.info_label_terms_conditions));
		String spannedPrivacy = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getPrivacyPolicyUrl(), context.getResources().getString(
			R.string.privacy_policy));
		String statement = context.getResources()
			.getString(R.string.legal_TEMPLATE, spannedRules, spannedTerms, spannedPrivacy);
		return getSpannableTextByPrimaryColor(context, statement, true);
	}

	public static SpannableStringBuilder generateRailLegalClickableLink(Context context, String rulesAndRestrictionsURL) {
		PointOfSale pos = PointOfSale.getPointOfSale();
		String statement = Phrase.from(context, R.string.rails_legal_TEMPLATE)
			.put("rules_and_restrictions_url", rulesAndRestrictionsURL)
			.put("conditions_of_travel_url", pos.getRailsNationalRailConditionsOfTravelUrl())
			.put("supplier_terms_and_conditions_url", pos.getRailsSupplierTermsAndConditionsUrl())
			.put("terms_of_use_url", pos.getRailsTermOfUseUrl())
			.put("privacy_policy_url", pos.getRailsPrivacyPolicyUrl())
			.put("payment_and_ticket_delivery_fees_url", pos.getRailsPaymentAndTicketDeliveryFeesUrl())
			.format().toString();
		return getSpannableTextByPrimaryColor(context, statement, true);
	}

	public static SpannableStringBuilder getSpannableTextByPrimaryColor(Context context, String statement, boolean makeClickable) {
		return getSpannableTextByColor(statement, Ui.obtainThemeColor(context, R.attr.primary_color), makeClickable);
	}

	public static SpannableStringBuilder getSpannableTextByColor(String statement, int color, boolean makeClickable) {
		SpannableStringBuilder legalTextSpan = new SpannableStringBuilder();
		legalTextSpan.append(HtmlCompat.fromHtml(statement));

		URLSpan[] spans = legalTextSpan.getSpans(0, HtmlCompat.fromHtml(statement).length(), URLSpan.class);
		for (final URLSpan span : spans) {
			int start = legalTextSpan.getSpanStart(span);
			int end = legalTextSpan.getSpanEnd(span);
			// Replace URL span with ClickableSpan to redirect to our own webview
			legalTextSpan.removeSpan(span);

			if (makeClickable) {
				legalTextSpan.setSpan(new LegalClickableSpan(span.getURL(), legalTextSpan.subSequence(start, end).toString(), true), start,
					end, 0);
			}
			legalTextSpan.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
			legalTextSpan.setSpan(new UnderlineSpan(), start, end, 0);
			legalTextSpan.setSpan(new ForegroundColorSpan(color), start,
				end,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		return legalTextSpan;
	}

	public static SpannableStringBuilder generateAccountCreationLegalLink(Context context) {
		SpannableStringBuilder legalTextSpan = new SpannableStringBuilder();

		String spannedTerms = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getTermsAndConditionsUrl(),
			context.getResources().getString(R.string.info_label_terms_of_use));
		String spannedPrivacy = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getPrivacyPolicyUrl(),
			context.getResources().getString(R.string.privacy_policy));
		String spannedTermsAndConditions = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
			PointOfSale.getPointOfSale().getAccountCreationTermsAndConditionsURL(),
			context.getResources().getString(R.string.info_label_terms_conditions));

		int statementResId = PointOfSale.getPointOfSale().shouldShowRewards() ? R.string.account_creation_legal_excluding_rewards_TEMPLATE
			: R.string.account_creation_legal_excluding_rewards_TEMPLATE;

		legalTextSpan.append(HtmlCompat.fromHtml(Phrase.from(context.getResources(), statementResId)
			.put("privacy_policy", spannedPrivacy)
			.put("terms_of_use", spannedTerms)
			.putOptional("terms_and_conditions", spannedTermsAndConditions)
			.putOptional("brand_reward_name", context.getString(R.string.brand_reward_name))
			.format().toString()));
		URLSpan[] spans = legalTextSpan.getSpans(0, legalTextSpan.length(), URLSpan.class);

		return formatLegalTextSpan(context, legalTextSpan, spans);
	}

	private static SpannableStringBuilder formatLegalTextSpan(Context context, SpannableStringBuilder legalTextSpan, URLSpan[] spans) {
		for (final URLSpan span : spans) {
			int start = legalTextSpan.getSpanStart(span);
			int end = legalTextSpan.getSpanEnd(span);
			// Replace URL span with ClickableSpan to redirect to our own webview
			legalTextSpan.removeSpan(span);
			legalTextSpan.setSpan(new LegalClickableSpan(span.getURL(), legalTextSpan.subSequence(start, end).toString(), false), start,
					end, 0);
			legalTextSpan.setSpan(new StyleSpan(Typeface.BOLD), start, end, 0);
		}

		return legalTextSpan;
	}

	public static SpannableStringBuilder generateLoyaltyRewardsLegalLink(Context context) {
		SpannableStringBuilder legalTextSpan = new SpannableStringBuilder();

		String spannedTermsAndConditions = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
				PointOfSale.getPointOfSale().getLoyaltyTermsAndConditionsUrl(),
				context.getResources().getString(R.string.info_label_terms_conditions));
		String spannedBrandRewards = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
				PointOfSale.getPointOfSale().getRewardsInfoURL(),
				context.getResources().getString(R.string.brand_reward_name));
		String spannedBrandRewardsCurrency = context.getResources().getString(R.string.textview_spannable_hyperlink_TEMPLATE,
				PointOfSale.getPointOfSale().getRewardsInfoURL(),
				context.getResources().getString(R.string.brand_reward_currency));

		legalTextSpan.append(HtmlCompat.fromHtml(Phrase.from(context.getResources(), R.string.account_creation_legal_rewards_TEMPLATE)
				.putOptional("brand_reward_name_link", spannedBrandRewards)
				.putOptional("brand_reward_currency", spannedBrandRewardsCurrency)
				.putOptional("brand_reward_name", context.getString(R.string.brand_reward_name))
				.put("terms_and_conditions", spannedTermsAndConditions)
				.format().toString()));
		URLSpan[] spans = legalTextSpan.getSpans(0, legalTextSpan.length(), URLSpan.class);

		return formatLegalTextSpan(context, legalTextSpan, spans);
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
		return HtmlCompat.stripHtml(htmlContent.replaceAll(HTML_TAGS_REGEX, ""));
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
		return HtmlCompat.stripHtml(str);
	}

	public static String getYouthTravelerAgeText(Resources res, int age) {
		if (age != GuestsPickerUtils.MIN_RAIL_YOUTH_AGE) {
			age = age + GuestsPickerUtils.MIN_RAIL_YOUTH_AGE;
		}

		String str = res.getQuantityString(R.plurals.child_age, age, age);
		return HtmlCompat.stripHtml(str);
	}

	public static String getSeniorTravelerAgeText(Resources res, int age) {
		if (age != GuestsPickerUtils.MIN_RAIL_SENIORS_AGE) {
			age = age + GuestsPickerUtils.MIN_RAIL_SENIORS_AGE;
		}
		String str = null;
		if (age > 61) {
			str = res.getString(R.string.senior_age_greater_than_sixth_two);
		}
		else {
			str = res.getQuantityString(R.plurals.child_age, age, age);
		}
		return HtmlCompat.stripHtml(str);
	}

	public static String roundOff(float number, int decimalPlaces) {
		StringBuilder formatBuilder = new StringBuilder("#");
		if (decimalPlaces > 0) {
			formatBuilder.append(".");
			//Add 0s equal to the number of decimal places
			for (int i = 0; i < decimalPlaces; i++) {
				formatBuilder.append('0');
			}
		}
		return new DecimalFormat(formatBuilder.toString()).format(number);
	}

	public static String getFormattedContent(Context context, String content) {
		final String bullet = "<br/>" + context.getString(R.string.bullet_point) + " ";
		final String justBullet = context.getString(R.string.bullet_point) + " ";

		// We strive to reformat the bullet points
		StringBuilder str = new StringBuilder(2048);
		String tag;
		int length = content.length();
		int i = 0;
		int start = -1;
		int end = -1;

		while (i < length) {
			start = content.indexOf('<', i);
			if (start < 0) {
				if (end == -1) {
					// Special case - there are no tags - append *all* content
					str.append(content);
				}
				else if (end + 1 < length) {
					// Append the rest of the content after the last tag
					str.append(content.substring(end + 1));
				}
				break;
			}
			end = content.indexOf('>', start);
			if (start > i) {
				str.append(content.substring(i, start));
			}
			i = end + 1;
			tag = content.substring(start + 1, end);

			if (tag.length() == 0) {
				continue; // drop the tag, it is empty
			}

			switch (tag.charAt(0)) {
			case 'l': // li
				if ('i' == tag.charAt(1) && tag.length() == 2) {
					if (!content.substring(i).startsWith("<ul>")) {
						if (str.length() > 0) {
							str.append(bullet);
						}
						else {
							str.append(justBullet);
						}
					}
				}
				break;
			case 'u': // ul
				if ('l' == tag.charAt(1) && tag.length() == 2) {
					if (content.substring(i).startsWith("</ul>")) {
						// Skip this noise
						i += 5;
					}
					else if (str.length() > 0) {
						str.append("<br/>");
					}
				}
				break;
			case '/':
				if (tag.equals("/ul")) {
					if ('l' == tag.charAt(2) && tag.length() == 3) {
						str.append("<br/>");
					}
				}
				break;
			default:
				// drop the tag
				break;
			}
		}
		return str.toString().trim();
	}

	public static String formatGuestString(Context context, int guests) {
		return context.getResources().getQuantityString(R.plurals.number_of_guests, guests,
			guests);
	}

	public static String formatRailcardString(Context context, int cards) {
		return context.getResources().getQuantityString(R.plurals.number_of_railcards, cards,
			cards);
	}

	public static String formatTravelerString(Context context, int numOfTravelers) {
		return context.getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numOfTravelers,
			numOfTravelers);
	}

	public static String formatMultipleTravelerString(Context context, int numOfTravelers) {
		if (numOfTravelers > 1) {
			return context.getResources().getQuantityString(R.plurals.number_of_travelers_TEMPLATE, numOfTravelers,
				numOfTravelers);
		}
		else {
			return "";
		}
	}

	public static String formatRoomString(Context context, int roomsCount) {
		return Phrase.from(context.getResources().getQuantityString(R.plurals.number_of_room_TEMPLATE, roomsCount))
			.put("room", roomsCount)
			.format().toString();
	}

	public static String formatNightsString(Context context, int nightsCount) {
		return Phrase.from(context.getResources().getQuantityString(R.plurals.number_of_nights_TEMPLATE, nightsCount))
			.put("night", nightsCount)
			.format().toString();
	}

	public static String formatLowerCaseGuestString(Context context, int guestsCount) {
		return Phrase.from(context.getResources().getQuantityString(R.plurals.number_of_guest_TEMPLATE, guestsCount))
			.put("guest", guestsCount)
			.format().toString();
	}

	public static List<String> getRedemptionLocationList(List<ActivityDetailsResponse.LXLocation> redemptionLocations) {
		List<String> redemptionLocationList = new ArrayList<>();

		for (int i = 0; i < redemptionLocations.size(); i++) {
			StringBuilder sb = new StringBuilder();
			if (Strings.isNotEmpty(redemptionLocations.get(i).addressName)) {
				sb.append(redemptionLocations.get(i).addressName);
			}
			if (Strings.isNotEmpty(redemptionLocations.get(i).street)) {
				sb.append(", ");
				sb.append(redemptionLocations.get(i).street);
			}
			if (Strings.isNotEmpty(redemptionLocations.get(i).city)) {
				sb.append(", ");
				sb.append(redemptionLocations.get(i).city);
			}
			if (Strings.isNotEmpty(redemptionLocations.get(i).province)) {
				sb.append(", ");
				sb.append(redemptionLocations.get(i).province);
			}
			if (Strings.isNotEmpty(redemptionLocations.get(i).postalCode)) {
				sb.append(", ");
				sb.append(redemptionLocations.get(i).postalCode);
			}
			redemptionLocationList.add(sb.toString());
		}
		return redemptionLocationList;
	}

	public static String getDisplayNameForGaiaNearby(String friendlyName, String airportName) {
		if (Strings.isEmpty(airportName)) {
			return friendlyName;
		}
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(friendlyName).append(" (").append(airportName).append(")");
			return sb.toString();
		}

	}

	public static CharSequence bundleTotalWithTaxesString(Context context) {
		SpannableBuilder builder = new SpannableBuilder();
		builder.append(context.getString(R.string.packages_append_taxes_and_fees),
			FontCache.getSpan(FontCache.Font.ROBOTO_REGULAR), new RelativeSizeSpan(0.8f));
		CharSequence appendTaxesFeesMessage = builder.build();
		return Phrase.from(context, R.string.packages_trip_total_with_taxes_and_fees_TEMPLATE)
			.put("totalstring", context.getString(R.string.packages_trip_total))
			.put("taxesandfeesstring", appendTaxesFeesMessage)
			.format();
	}
}
