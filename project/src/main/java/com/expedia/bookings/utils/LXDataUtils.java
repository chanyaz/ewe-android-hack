package com.expedia.bookings.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.LocalDate;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXTicketType;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.deeplink.ActivityDeepLink;
import com.expedia.bookings.text.HtmlCompat;
import com.mobiata.android.text.StrikethroughTagHandler;
import com.mobiata.flightlib.data.Airport;
import com.squareup.phrase.Phrase;

public class LXDataUtils {
	private static final String RULES_RESTRICTIONS_URL_PATH = "Checkout/LXRulesAndRestrictions?tripid=";

	private static final Map<LXTicketType, Integer> LX_PER_TICKET_TYPE_MAP = new EnumMap<LXTicketType, Integer>(LXTicketType.class) {
		{
			put(LXTicketType.Traveler, R.string.per_ticket_type_traveler);
			put(LXTicketType.Adult, R.string.per_ticket_type_adult);
			put(LXTicketType.Child, R.string.per_ticket_type_child);
			put(LXTicketType.Infant, R.string.per_ticket_type_infant);
			put(LXTicketType.Youth, R.string.per_ticket_type_youth);
			put(LXTicketType.Senior, R.string.per_ticket_type_senior);
			put(LXTicketType.Group, R.string.per_ticket_type_group);
			put(LXTicketType.Couple, R.string.per_ticket_type_couple);
			put(LXTicketType.Two_Adults, R.string.per_ticket_type_two_adults);
			put(LXTicketType.Military, R.string.per_ticket_type_military);
			put(LXTicketType.Student, R.string.per_ticket_type_student);
			put(LXTicketType.Sedan, R.string.per_ticket_type_sedan);
			put(LXTicketType.Minivan, R.string.per_ticket_type_minivan);
			put(LXTicketType.Water_Taxi, R.string.per_ticket_type_water_taxi);
			put(LXTicketType.SUV, R.string.per_ticket_type_suv);
			put(LXTicketType.Executive_Car, R.string.per_ticket_type_executive_car);
			put(LXTicketType.Luxury_Car, R.string.per_ticket_type_luxury_car);
			put(LXTicketType.Limousine, R.string.per_ticket_type_limousine);
			put(LXTicketType.TownCar, R.string.per_ticket_type_towncar);
			put(LXTicketType.Vehicle_Parking_Spot, R.string.per_ticket_type_vehicle_parking_spot);
			put(LXTicketType.Book, R.string.per_ticket_type_book);
			put(LXTicketType.Guide, R.string.per_ticket_type_guide);
			put(LXTicketType.Travel_Card, R.string.per_ticket_type_travel_card);
			put(LXTicketType.Boat, R.string.per_ticket_type_boat);
			put(LXTicketType.Motorcycle, R.string.per_ticket_type_motorcycle);
			put(LXTicketType.Ceremony, R.string.per_ticket_type_ceremony);
			put(LXTicketType.Calling_Card, R.string.per_ticket_type_calling_card);
			put(LXTicketType.Pass, R.string.per_ticket_type_pass);
			put(LXTicketType.Minibus, R.string.per_ticket_type_minibus);
			put(LXTicketType.Helicopter, R.string.per_ticket_type_helicopter);
			put(LXTicketType.Device, R.string.per_ticket_type_device);
			put(LXTicketType.Room, R.string.per_ticket_type_room);
			put(LXTicketType.Carriage, R.string.per_ticket_type_carriage);
			put(LXTicketType.Buggy, R.string.per_ticket_type_buggy);
			put(LXTicketType.ATV, R.string.per_ticket_type_atv);
			put(LXTicketType.Jet_Ski, R.string.per_ticket_type_jet_ski);
			put(LXTicketType.Scooter, R.string.per_ticket_type_scooter);
			put(LXTicketType.Scooter_Car, R.string.per_ticket_type_scooter_car);
			put(LXTicketType.Snowmobile, R.string.per_ticket_type_snowmobile);
			put(LXTicketType.Day, R.string.per_ticket_type_day);
			put(LXTicketType.Bike, R.string.per_ticket_type_bike);
			put(LXTicketType.Week, R.string.per_ticket_type_week);
			put(LXTicketType.Subscription, R.string.per_ticket_type_subscription);
			put(LXTicketType.Electric_Bike, R.string.per_ticket_type_electric_bike);
			put(LXTicketType.Segway, R.string.per_ticket_type_segway);
			put(LXTicketType.Vehicle, R.string.per_ticket_type_vehicle);
		}
	};

	private static final Map<LXTicketType, Integer> LX_TICKET_TYPE_NAME_MAP = new EnumMap<LXTicketType, Integer>(LXTicketType.class) {
		{
			put(LXTicketType.Traveler, R.string.ticket_type_traveler);
			put(LXTicketType.Adult, R.string.ticket_type_adult);
			put(LXTicketType.Child, R.string.ticket_type_child);
			put(LXTicketType.Infant, R.string.ticket_type_infant);
			put(LXTicketType.Youth, R.string.ticket_type_youth);
			put(LXTicketType.Senior, R.string.ticket_type_senior);
			put(LXTicketType.Group, R.string.ticket_type_group);
			put(LXTicketType.Couple, R.string.ticket_type_couple);
			put(LXTicketType.Two_Adults, R.string.ticket_type_two_adults);
			put(LXTicketType.Military, R.string.ticket_type_military);
			put(LXTicketType.Student, R.string.ticket_type_student);
			put(LXTicketType.Sedan, R.string.ticket_type_sedan);
			put(LXTicketType.Minivan, R.string.ticket_type_minivan);
			put(LXTicketType.Water_Taxi, R.string.ticket_type_water_taxi);
			put(LXTicketType.SUV, R.string.ticket_type_suv);
			put(LXTicketType.Executive_Car, R.string.ticket_type_executive_car);
			put(LXTicketType.Luxury_Car, R.string.ticket_type_luxury_car);
			put(LXTicketType.Limousine, R.string.ticket_type_limousine);
			put(LXTicketType.TownCar, R.string.ticket_type_towncar);
			put(LXTicketType.Vehicle_Parking_Spot, R.string.ticket_type_vehicle_parking_spot);
			put(LXTicketType.Book, R.string.ticket_type_book);
			put(LXTicketType.Guide, R.string.ticket_type_guide);
			put(LXTicketType.Travel_Card, R.string.ticket_type_travel_card);
			put(LXTicketType.Boat, R.string.ticket_type_boat);
			put(LXTicketType.Motorcycle, R.string.ticket_type_motorcycle);
			put(LXTicketType.Ceremony, R.string.ticket_type_ceremony);
			put(LXTicketType.Calling_Card, R.string.ticket_type_calling_card);
			put(LXTicketType.Pass, R.string.ticket_type_pass);
			put(LXTicketType.Minibus, R.string.ticket_type_minibus);
			put(LXTicketType.Helicopter, R.string.ticket_type_helicopter);
			put(LXTicketType.Device, R.string.ticket_type_device);
			put(LXTicketType.Room, R.string.ticket_type_room);
			put(LXTicketType.Carriage, R.string.ticket_type_carriage);
			put(LXTicketType.Buggy, R.string.ticket_type_buggy);
			put(LXTicketType.ATV, R.string.ticket_type_atv);
			put(LXTicketType.Jet_Ski, R.string.ticket_type_jet_ski);
			put(LXTicketType.Scooter, R.string.ticket_type_scooter);
			put(LXTicketType.Scooter_Car, R.string.ticket_type_scooter_car);
			put(LXTicketType.Snowmobile, R.string.ticket_type_snowmobile);
			put(LXTicketType.Day, R.string.ticket_type_day);
			put(LXTicketType.Bike, R.string.ticket_type_bike);
			put(LXTicketType.Week, R.string.ticket_type_week);
			put(LXTicketType.Subscription, R.string.ticket_type_subscription);
			put(LXTicketType.Electric_Bike, R.string.ticket_type_electric_bike);
			put(LXTicketType.Segway, R.string.ticket_type_segway);
			put(LXTicketType.Vehicle, R.string.ticket_type_vehicle);
		}
	};

	private static final Map<LXTicketType, Integer> LX_TICKET_TYPE_COUNT_LABEL_MAP = new EnumMap<LXTicketType, Integer>(LXTicketType.class) {
		{
			put(LXTicketType.Traveler, R.plurals.ticket_type_traveler_TEMPLATE);
			put(LXTicketType.Adult, R.plurals.ticket_type_adult_TEMPLATE);
			put(LXTicketType.Child, R.plurals.ticket_type_child_TEMPLATE);
			put(LXTicketType.Infant, R.plurals.ticket_type_infant_TEMPLATE);
			put(LXTicketType.Youth, R.plurals.ticket_type_youth_TEMPLATE);
			put(LXTicketType.Senior, R.plurals.ticket_type_senior_TEMPLATE);
			put(LXTicketType.Group, R.plurals.ticket_type_group_TEMPLATE);
			put(LXTicketType.Couple, R.plurals.ticket_type_couple_TEMPLATE);
			put(LXTicketType.Two_Adults, R.plurals.ticket_type_two_adults_TEMPLATE);
			put(LXTicketType.Military, R.plurals.ticket_type_military_TEMPLATE);
			put(LXTicketType.Student, R.plurals.ticket_type_student_TEMPLATE);
			put(LXTicketType.Sedan, R.plurals.ticket_type_sedan_TEMPLATE);
			put(LXTicketType.Minivan, R.plurals.ticket_type_minivan_TEMPLATE);
			put(LXTicketType.Water_Taxi, R.plurals.ticket_type_water_taxi_TEMPLATE);
			put(LXTicketType.SUV, R.plurals.ticket_type_suv_TEMPLATE);
			put(LXTicketType.Executive_Car, R.plurals.ticket_type_executive_car_TEMPLATE);
			put(LXTicketType.Luxury_Car, R.plurals.ticket_type_luxury_car_TEMPLATE);
			put(LXTicketType.Limousine, R.plurals.ticket_type_limousine_TEMPLATE);
			put(LXTicketType.TownCar, R.plurals.ticket_type_towncar_TEMPLATE);
			put(LXTicketType.Vehicle_Parking_Spot, R.plurals.ticket_type_vehicle_parking_spot_TEMPLATE);
			put(LXTicketType.Book, R.plurals.ticket_type_book_TEMPLATE);
			put(LXTicketType.Guide, R.plurals.ticket_type_guide_TEMPLATE);
			put(LXTicketType.Travel_Card, R.plurals.ticket_type_travel_card_TEMPLATE);
			put(LXTicketType.Boat, R.plurals.ticket_type_boat_TEMPLATE);
			put(LXTicketType.Motorcycle, R.plurals.ticket_type_motorcycle_TEMPLATE);
			put(LXTicketType.Ceremony, R.plurals.ticket_type_ceremony_TEMPLATE);
			put(LXTicketType.Calling_Card, R.plurals.ticket_type_calling_card_TEMPLATE);
			put(LXTicketType.Pass, R.plurals.ticket_type_pass_TEMPLATE);
			put(LXTicketType.Minibus, R.plurals.ticket_type_minibus_TEMPLATE);
			put(LXTicketType.Helicopter, R.plurals.ticket_type_helicopter_TEMPLATE);
			put(LXTicketType.Device, R.plurals.ticket_type_device_TEMPLATE);
			put(LXTicketType.Room, R.plurals.ticket_type_room_TEMPLATE);
			put(LXTicketType.Carriage, R.plurals.ticket_type_carriage_TEMPLATE);
			put(LXTicketType.Buggy, R.plurals.ticket_type_buggy_TEMPLATE);
			put(LXTicketType.ATV, R.plurals.ticket_type_atv_TEMPLATE);
			put(LXTicketType.Jet_Ski, R.plurals.ticket_type_jet_ski_TEMPLATE);
			put(LXTicketType.Scooter, R.plurals.ticket_type_scooter_TEMPLATE);
			put(LXTicketType.Scooter_Car, R.plurals.ticket_type_scooter_car_TEMPLATE);
			put(LXTicketType.Snowmobile, R.plurals.ticket_type_snowmobile_TEMPLATE);
			put(LXTicketType.Day, R.plurals.ticket_type_day_TEMPLATE);
			put(LXTicketType.Bike, R.plurals.ticket_type_bike_TEMPLATE);
			put(LXTicketType.Week, R.plurals.ticket_type_week_TEMPLATE);
			put(LXTicketType.Subscription, R.plurals.ticket_type_subscription_TEMPLATE);
			put(LXTicketType.Electric_Bike, R.plurals.ticket_type_electric_bike_TEMPLATE);
			put(LXTicketType.Segway, R.plurals.ticket_type_segway_TEMPLATE);
			put(LXTicketType.Vehicle, R.plurals.ticket_type_vehicle_TEMPLATE);
		}
	};

	public static String perTicketTypeDisplayLabel(Context context, LXTicketType ticketType) {
		if (LX_PER_TICKET_TYPE_MAP.containsKey(ticketType)) {
			return context.getString(LXDataUtils.LX_PER_TICKET_TYPE_MAP.get(ticketType));
		}
		return "";
	}

	public static String ticketDisplayName(Context context, LXTicketType ticketType) {
		if (LX_TICKET_TYPE_NAME_MAP.containsKey(ticketType)) {
			return context.getString(LXDataUtils.LX_TICKET_TYPE_NAME_MAP.get(ticketType));
		}
		return "";
	}

	public static String ticketCountSummary(Context context, LXTicketType ticketType, int ticketCount) {
		if (!LX_TICKET_TYPE_COUNT_LABEL_MAP.containsKey(ticketType) || ticketCount < 1) {
			return "";
		}

		return context.getResources()
			.getQuantityString(LXDataUtils.LX_TICKET_TYPE_COUNT_LABEL_MAP.get(ticketType), ticketCount, ticketCount);
	}

	/*
	 *  Prepare Tickets Count Summary like "1 Adult, 3 Children"
	 */
	public static String ticketsCountSummary(Context context, List<Ticket> tickets) {
		if (CollectionUtils.isEmpty(tickets)) {
			return "";
		}

		List<String> ticketSummaries = new ArrayList<>();

		Map<LXTicketType, Integer> ticketTypeToCountMap = new LinkedHashMap<>();
		for (Ticket ticket : tickets) {
			if (ticket.count <= 0) {
				continue;
			}

			if (!ticketTypeToCountMap.containsKey(ticket.code)) {
				ticketTypeToCountMap.put(ticket.code, ticket.count);
			}
			else {
				int existingCount = ticketTypeToCountMap.get(ticket.code);
				ticketTypeToCountMap.put(ticket.code, existingCount + ticket.count);
			}
		}

		for (Map.Entry<LXTicketType, Integer> entry : ticketTypeToCountMap.entrySet()) {
			ticketSummaries.add(LXDataUtils.ticketCountSummary(context, entry.getKey(), entry.getValue()));
		}

		return Strings.joinWithoutEmpties(", ", ticketSummaries);
	}

	public static LxSearchParams fromFlightParams(Context context, FlightTrip trip) {
		FlightLeg firstLeg = trip.getLeg(0);
		LocalDate checkInDate = new LocalDate(firstLeg.getLastWaypoint().getBestSearchDateTime());

		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder().searchType(SearchType.EXPLICIT_SEARCH)
			.location(formatAirport(context, firstLeg.getAirport(false)))
			.startDate(checkInDate)
			.endDate(checkInDate.plusDays(14)).build();
		return searchParams;
	}

	private static String formatAirport(Context c, Airport airport) {
		return c.getResources().getString(R.string.lx_destination_TEMPLATE, airport.mCity, Strings.isEmpty(airport.mStateCode) ? airport.mCountryCode : airport.mStateCode);
	}

	public static LxSearchParams fromHotelParams(Context context, LocalDate checkInDate, LocalDate checkOutDate, Location location) {
		LxSearchParams searchParams = (LxSearchParams) new LxSearchParams.Builder()
			.searchType(SearchType.EXPLICIT_SEARCH)
			.location(formatLocation(context, location))
			.startDate(checkInDate)
			.endDate(checkOutDate).build();

		return searchParams;
	}

	public static String getRulesRestrictionsUrl(String e3EndpointUrl, String tripId) {
		return e3EndpointUrl + RULES_RESTRICTIONS_URL_PATH + tripId;
	}

	public static String getCancelationPolicyDisplayText(Context context, int freeCancellationMinHours) {
		if (freeCancellationMinHours > 0) {
			return Phrase.from(context, R.string.lx_cancellation_policy_TEMPLATE)
				.put("hours", freeCancellationMinHours).format().toString();
		}
		else {
			return context.getString(R.string.lx_policy_non_cancellable);
		}
	}

	private static String formatLocation(Context c, Location location) {
		return c.getResources().getString(R.string.lx_destination_TEMPLATE, location.getCity(), Strings.isEmpty(location.getStateCode()) ? location.getCountryCode() : location.getStateCode());
	}

	public static LxSearchParams buildLXSearchParamsFromDeeplink(ActivityDeepLink activityDeepLink) {
		LocalDate startDate = LocalDate.now();
		if (activityDeepLink.getStartDate() != null) {
			startDate = activityDeepLink.getStartDate();
		}

		LocalDate endDate = startDate.plusDays(14);

		String location = "";
		String filters = "";
		String activityId = "";
		if (activityDeepLink.getLocation() != null) {
			location = activityDeepLink.getLocation();
		}
		if (activityDeepLink.getFilters() != null) {
			filters = activityDeepLink.getFilters();
		}
		if (activityDeepLink.getActivityID() != null) {
			activityId = activityDeepLink.getActivityID();
		}
		return new LxSearchParams(location, DateUtils.ensureDateIsTodayOrInFuture(startDate),
			DateUtils.ensureDateIsTodayOrInFuture(endDate), SearchType.EXPLICIT_SEARCH, filters, activityId, "");
	}

	public static boolean isActivityGT(List<String> activityCategories) {
		if (CollectionUtils.isNotEmpty(activityCategories)) {
			return activityCategories.contains("Private Transfers") || activityCategories.contains("Shared Transfers");
		}

		return false;
	}

	public static boolean isCategoryAllThingsToDoAndItsCategoryImageAvailable(Context context, String categoryKey,
		String imageCode) {
		return isCategoryAllThingsToDo(context, categoryKey) && Strings.isNotEmpty(imageCode) && !imageCode
			.equals("null");
	}

	public static boolean isCategoryAllThingsToDo(Context context, String categoryKey) {
		return categoryKey.equals(context.getResources().getString(R.string.lx_category_key_all_things_to_do));
	}

	public static int getErrorDrawableForCategory(Context context, String categoryKey) {
		return isCategoryAllThingsToDo(context, categoryKey) ? R.drawable.itin_header_placeholder_activities
			: R.drawable.lx_category_all_things_to_do;
	}

	public static void bindDuration(Context context, String activityDuration, boolean isMultiDuration,
		TextView duration) {
		if (Strings.isNotEmpty(activityDuration)) {
			int contDescResId;
			if (isMultiDuration) {
				contDescResId = R.string.search_result_multiple_duration_cont_desc_TEMPLATE;
				duration.setText(Phrase.from(context,R.string.search_result_multiple_duration_TEMPLATE)
					.put("duration", activityDuration)
					.format());
			}
			else {
				contDescResId = R.string.search_result_duration_cont_desc_TEMPLATE;
				duration.setText(activityDuration);
			}
			duration.setContentDescription(
				Phrase.from(context, contDescResId).put("duration", activityDuration.toUpperCase(Locale.getDefault()))
					.format().toString());
			duration.setVisibility(View.VISIBLE);
		}
		else {
			duration.setText("");
			duration.setVisibility(View.GONE);
		}
	}

	public static void bindOriginalPrice(Context context, Money originalPrice, TextView activityOriginalPrice) {
		if (originalPrice.getAmount().equals(BigDecimal.ZERO)) {
			activityOriginalPrice.setVisibility(View.GONE);
		}
		else {
			activityOriginalPrice.setVisibility(View.VISIBLE);
			String formattedOriginalPrice = originalPrice.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP);
			activityOriginalPrice.setText(HtmlCompat.fromHtml(
				context.getString(R.string.strike_template, formattedOriginalPrice),
				null,
				new StrikethroughTagHandler()));
		}
	}

	public static void bindPriceAndTicketType(Context context, LXTicketType fromPriceTicketCode, Money price,
		Money originalPrice, TextView activityPrice, TextView fromPriceTicketType) {
		if (fromPriceTicketCode != null) {
			fromPriceTicketType.setText(
				LXDataUtils.perTicketTypeDisplayLabel(context, fromPriceTicketCode));
			fromPriceTicketType.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
			activityPrice.setText(price.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP));
			final CharSequence activityPriceContDesc;
			if (originalPrice.getAmount().equals(BigDecimal.ZERO)) {
				activityPriceContDesc = Phrase
					.from(context, R.string.activity_price_per_travelertype_without_discount_cont_desc_TEMPLATE)
					.put("activity_price", price.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP))
					.put("ticket_type", LXDataUtils.perTicketTypeDisplayLabel(context, fromPriceTicketCode))
					.format();
			}
			else {
				activityPriceContDesc = Phrase.from(context, R.string.activity_price_per_travelertype_with_discount_cont_desc_TEMPLATE)
					.put("activity_price", price.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP))
					.put("activity_original_price", originalPrice.getFormattedMoney(Money.F_NO_DECIMAL | Money.F_ROUND_HALF_UP))
					.put("ticket_type", LXDataUtils.perTicketTypeDisplayLabel(context, fromPriceTicketCode))
					.format();
			}
			activityPrice.setContentDescription(activityPriceContDesc);
		}
		else {
			fromPriceTicketType.setText("");
			activityPrice.setText("");
			activityPrice.setContentDescription(null);
		}
	}

	public static String getUserRecommendPercentString(Context context, int recommendationScore) {
		return Phrase.from(context.getResources(), R.string.lx_recommend_percent_Template)
			.put("recommend", recommendationScore).format().toString();
	}

	public static String getToolbarSearchDateText(Context context, LxSearchParams searchParams, boolean isContDesc) {
		return Phrase.from(context,
			isContDesc ? R.string.lx_toolbar_date_range_cont_desc_TEMPLATE : R.string.lx_toolbar_date_range_TEMPLATE)
			.put("from_date", LocaleBasedDateFormatUtils.localDateToMMMd(searchParams.getActivityStartDate()))
			.put("to_date", LocaleBasedDateFormatUtils.localDateToMMMd(searchParams.getActivityEndDate()))
			.format().toString();
	}


}
