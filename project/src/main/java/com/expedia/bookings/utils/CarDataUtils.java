package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;
import android.net.Uri;

import com.expedia.bookings.data.cars.LatLong;
import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CarSearchParamsBuilder;
import com.expedia.bookings.data.cars.CarType;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.RateBreakdownItem;
import com.expedia.bookings.data.cars.RateTerm;
import com.expedia.bookings.data.cars.RentalFareBreakdownType;
import com.expedia.bookings.data.cars.Transmission;
import com.expedia.bookings.services.CarServices;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.mobiata.android.Log;

/*
 * Connecting lib-ExpediaBookings car data stuctures to Android-based
 * resources.
 */

public class CarDataUtils {

	@SuppressWarnings("serial")
	public static final Map<CarCategory, Integer> CAR_TYPE_DESCRIPTION_MAP = new HashMap<CarCategory, Integer>() {
		{
			put(CarCategory.MINI, R.string.car_category_mini);
			put(CarCategory.ECONOMY, R.string.car_category_economy);
			put(CarCategory.COMPACT, R.string.car_category_compact);
			put(CarCategory.MIDSIZE, R.string.car_category_midsize);
			put(CarCategory.STANDARD, R.string.car_category_standard);
			put(CarCategory.FULLSIZE, R.string.car_category_fullsize);
			put(CarCategory.PREMIUM, R.string.car_category_premium);
			put(CarCategory.LUXURY, R.string.car_category_luxury);
			put(CarCategory.MINI_ELITE, R.string.car_category_mini_elite);
			put(CarCategory.ECONOMY_ELITE, R.string.car_category_economy_elite);
			put(CarCategory.COMPACT_ELITE, R.string.car_category_compact_elite);
			put(CarCategory.SPECIAL, R.string.car_type_special);
			put(CarCategory.MIDSIZE_ELITE, R.string.car_category_midsize_elite);
			put(CarCategory.STANDARD_ELITE, R.string.car_category_standard_elite);
			put(CarCategory.FULLSIZE_ELITE, R.string.car_category_fullsize_elite);
			put(CarCategory.PREMIUM_ELITE, R.string.car_category_premium_elite);
			put(CarCategory.LUXURY_ELITE, R.string.car_category_luxury_elite);
			put(CarCategory.OVERSIZE, R.string.car_category_oversize);
		}
	};

	@SuppressWarnings("serial")
	public static final Map<CarCategory, Integer> CAR_TYPE_SHARE_MSG_MAP = new HashMap<CarCategory, Integer>() {
		{
			put(CarCategory.MINI, R.string.share_template_short_car_type_mini);
			put(CarCategory.ECONOMY, R.string.share_template_short_car_type_economy);
			put(CarCategory.COMPACT, R.string.share_template_short_car_type_compact);
			put(CarCategory.MIDSIZE, R.string.share_template_short_car_type_midsize);
			put(CarCategory.STANDARD, R.string.share_template_short_car_type_standard);
			put(CarCategory.FULLSIZE, R.string.share_template_short_car_type_fullsize);
			put(CarCategory.PREMIUM, R.string.share_template_short_car_type_premium);
			put(CarCategory.LUXURY, R.string.share_template_short_car_type_luxury);
			put(CarCategory.SPECIAL, R.string.share_template_short_car_type_special);
			put(CarCategory.MINI_ELITE, R.string.share_template_short_car_type_mini_elite);
			put(CarCategory.ECONOMY_ELITE, R.string.share_template_short_car_type_economy_elite);
			put(CarCategory.COMPACT_ELITE, R.string.share_template_short_car_type_compact_elite);
			put(CarCategory.MIDSIZE_ELITE, R.string.share_template_short_car_type_midsize_elite);
			put(CarCategory.STANDARD_ELITE, R.string.share_template_short_car_type_standard_elite);
			put(CarCategory.FULLSIZE_ELITE, R.string.share_template_short_car_type_fullsize_elite);
			put(CarCategory.PREMIUM_ELITE, R.string.share_template_short_car_type_premium_elite);
			put(CarCategory.LUXURY_ELITE, R.string.share_template_short_car_type_luxury_elite);
			put(CarCategory.OVERSIZE, R.string.share_template_short_car_type_oversize);
		}
	};

	public static final Map<CarCategory, Integer> CAR_CATEGORY_DESCRIPTION_MAP_FOR_RESULTS = new HashMap<CarCategory, Integer>() {
		{
			put(CarCategory.MINI, R.string.car_category_result_mini);
			put(CarCategory.ECONOMY, R.string.car_category_result_economy);
			put(CarCategory.COMPACT, R.string.car_category_result_compact);
			put(CarCategory.MIDSIZE, R.string.car_category_result_midsize);
			put(CarCategory.STANDARD, R.string.car_category_result_standard);
			put(CarCategory.FULLSIZE, R.string.car_category_result_fullsize);
			put(CarCategory.PREMIUM, R.string.car_category_result_premium);
			put(CarCategory.LUXURY, R.string.car_category_result_luxury);
			put(CarCategory.MINI_ELITE, R.string.car_category_result_mini_elite);
			put(CarCategory.ECONOMY_ELITE, R.string.car_category_result_economy_elite);
			put(CarCategory.COMPACT_ELITE, R.string.car_category_result_compact_elite);
			put(CarCategory.SPECIAL, R.string.car_type_special);
			put(CarCategory.MIDSIZE_ELITE, R.string.car_category_result_midsize_elite);
			put(CarCategory.STANDARD_ELITE, R.string.car_category_result_standard_elite);
			put(CarCategory.FULLSIZE_ELITE, R.string.car_category_result_fullsize_elite);
			put(CarCategory.PREMIUM_ELITE, R.string.car_category_result_premium_elite);
			put(CarCategory.LUXURY_ELITE, R.string.car_category_result_luxury_elite);
			put(CarCategory.OVERSIZE, R.string.car_category_result_oversize);
		}
	};

	public static final Map<CarType, Integer> CAR_TYPE_DESCRIPTION_MAP_FOR_RESULTS = new HashMap<CarType, Integer>() {
		{
			put(CarType.TWO_DOOR_CAR, R.string.car_type_two_door);
			put(CarType.THREE_DOOR_CAR, R.string.car_type_three_door);
			put(CarType.FOUR_DOOR_CAR, R.string.car_type_four_door);
			put(CarType.VAN, R.string.car_type_van);
			put(CarType.WAGON, R.string.car_type_wagon);
			put(CarType.LIMOUSINE, R.string.car_type_limousine);
			put(CarType.RECREATIONAL_VEHICLE, R.string.car_type_recreational_vehicle);
			put(CarType.CONVERTIBLE, R.string.car_type_convertible);
			put(CarType.SPORTS_CAR, R.string.car_type_sports_car);
			put(CarType.SUV, R.string.car_type_suv);
			put(CarType.PICKUP_REGULAR_CAB, R.string.car_type_pickup_regular_cab);
			put(CarType.OPEN_AIR_ALL_TERRAIN, R.string.car_type_open_air_all_terrain);
			put(CarType.SPECIAL, R.string.car_type_special);
			put(CarType.COMMERCIAL_VAN_TRUCK, R.string.car_type_commercial_van_truck);
			put(CarType.PICKUP_EXTENDED_CAB, R.string.car_type_pickup_extended_cab);
			put(CarType.SPECIAL_OFFER_CAR, R.string.car_type_special_offer_car);
			put(CarType.COUPE, R.string.car_type_coupe);
			put(CarType.MONOSPACE, R.string.car_type_monospace);
			put(CarType.MOTORHOME, R.string.car_type_motor_home);
			put(CarType.TWO_WHEEL_VEHICLE, R.string.car_type_two_wheel_vehicle);
			put(CarType.ROADSTER, R.string.car_type_roadster);
			put(CarType.CROSSOVER, R.string.car_type_crossover);
		}
	};

	public static final Map<RentalFareBreakdownType, Integer> RENTAL_FARE_BREAKDOWN_TYPE_MAP = new HashMap<RentalFareBreakdownType, Integer>() {
		{
			put(RentalFareBreakdownType.BASE, R.string.car_rental_breakdown_base);
			put(RentalFareBreakdownType.TAXES_AND_FEES, R.string.taxes_and_fees);
			put(RentalFareBreakdownType.INSURANCE, R.string.car_rental_breakdown_insurance);
			put(RentalFareBreakdownType.DROP_OFF_CHARGE, R.string.car_rental_breakdown_drop_off_charges);

		}
	};

	public static String getShareMessageFor(Context c, CarCategory category) {
		return c.getResources().getString(CAR_TYPE_SHARE_MSG_MAP.get(category));
	}

	public static String getCategoryStringFor(Context c, CarCategory category) {
		return c.getResources().getString(CAR_TYPE_DESCRIPTION_MAP.get(category));
	}

	public static String getFareBreakdownType(Context ctx, RentalFareBreakdownType type) {
		return ctx.getResources().getString(RENTAL_FARE_BREAKDOWN_TYPE_MAP.get(type));
	}

	public static String getCategoryStringForResults(Context c, CarCategory category) {
		return c.getResources().getString(CAR_CATEGORY_DESCRIPTION_MAP_FOR_RESULTS.get(category));
	}

	public static String getTypeStringForResults(Context c, CarType type) {
		return c.getResources().getString(CAR_TYPE_DESCRIPTION_MAP_FOR_RESULTS.get(type));
	}

	public static String getStringForTransmission(Context c, Transmission transmission) {
		switch (transmission) {
		case AUTOMATIC_TRANSMISSION:
			return c.getString(R.string.car_auto_tran_text);
		case MANUAL_TRANSMISSION:
			return c.getString(R.string.car_man_tran_text);
		default:
			return c.getString(R.string.unknown);
		}
	}


	public static String getStringTemplateForRateTerm(Context c, RateTerm term) {
		switch (term) {
		case HOURLY:
			return c.getString(R.string.cars_hourly_text);
		case DAILY:
			return c.getString(R.string.cars_daily_text);
		case WEEKLY:
			return c.getString(R.string.cars_weekly_text);
		case MONTHLY:
			return c.getString(R.string.cars_monthly_text);
		default:
			return c.getString(R.string.cars_daily_text);
		}
	}

	public static String getMakeName(Context ctx, List<String> makes) {
		if (makes.isEmpty()) {
			return ctx.getString(R.string.car_model_unknown_template);
		}
		else {
			//TODO : This is a temporary ugly hack to fix the bad data coming from inventory #4226
			String makeName = makes.get(0);
			String makeNameLower = makeName.toLowerCase(Locale.US);
			if (makeNameLower.contains("or similar") ||
				makeNameLower.contains("we pick the car") ||
				makeNameLower.contains("exact model n/a")) {
				return makeName;
			}
			return ctx.getString(R.string.car_model_name_template, makeName);
		}
	}

	public static String getDoorCount(CategorizedCarOffers cco) {
		return getInfoCount(cco.doorSet);
	}

	public static String getPassengerCount(CategorizedCarOffers cco) {
		return getInfoCount(cco.passengerSet);
	}

	public static String getBagCount(CategorizedCarOffers cco) {
		return getInfoCount(cco.luggageSet);
	}

	public static String getInfoCount(Interval interval) {
		String s = null;
		int minVal = interval.getMin();
		int maxVal = interval.getMax();

		if (interval.different() && interval.bounded()) {
			s = minVal + "-" + maxVal;
		}
		else if (!interval.different()) {
			s = String.valueOf(maxVal);
		}
		return s;
	}

	public static CarSearchParams fromFlightParams(FlightTrip trip) {
		FlightLeg firstLeg = trip.getLeg(0);
		FlightLeg secondLeg = trip.getLegCount() > 1 ? trip.getLeg(1) : null;

		LocalDate checkInDate = new LocalDate(firstLeg.getLastWaypoint().getBestSearchDateTime());

		LocalDate checkOutDate;
		if (secondLeg == null) {
			// 1-way flight
			checkOutDate = checkInDate.plusDays(3);
		}
		else {
			// Round-trip flight
			checkOutDate = new LocalDate(secondLeg.getFirstWaypoint()
				.getMostRelevantDateTime());
		}
		CarSearchParamsBuilder builder = new CarSearchParamsBuilder();
		CarSearchParamsBuilder.DateTimeBuilder dateTimeBuilder = new CarSearchParamsBuilder.DateTimeBuilder()
			.startDate(checkInDate)
			.endDate(checkOutDate);
		builder.origin(firstLeg.getAirport(false).mAirportCode);
		builder.originDescription(firstLeg.getAirport(false).mName);
		// Empty DateTimeBuilder
		builder.dateTimeBuilder(dateTimeBuilder);


		return builder.build();
	}

	public static CarSearchParams fromDeepLink(Uri data, Set<String> queryData) {
		String pickupLocation = getQueryParameterIfExists(data, queryData, "pickupLocation");
		String pickupLocationLatStr = getQueryParameterIfExists(data, queryData, "pickupLocationLat");
		String pickupLocationLngStr = getQueryParameterIfExists(data, queryData, "pickupLocationLng");
		LatLong pickupLocationLatLng = LatLong.fromLatLngStrings(pickupLocationLatStr, pickupLocationLngStr);
		String originDescription = getQueryParameterIfExists(data, queryData, "originDescription");

		//Input Validation
		//1. One of `origin` and `pickupLocationLatLng` should exist for Car Search Params to be valid
		//2. `originDescription` should be non-empty
		if ((Strings.isEmpty(pickupLocation) && pickupLocationLatLng == null) || Strings.isEmpty(originDescription)) {
			return null;
		}

		String pickupDateTimeStr = getQueryParameterIfExists(data, queryData, "pickupDateTime");
		String dropoffDateTimeStr = getQueryParameterIfExists(data, queryData, "dropoffDateTime");

		CarSearchParams carSearchParams = new CarSearchParams();
		// DateTime Sanity - In case the date time passed from the outside world is in a garbled format,
		// we fallback to proper defaults to have a graceful behavior and nothing undesirable.
		carSearchParams.startDateTime = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(pickupDateTimeStr, DateTime.now());
		carSearchParams.endDateTime = DateUtils.yyyyMMddTHHmmssToDateTimeSafe(dropoffDateTimeStr, carSearchParams.startDateTime.plusDays(3));
		carSearchParams.originDescription = originDescription;

		carSearchParams.origin = pickupLocation;
		carSearchParams.pickupLocationLatLng = pickupLocationLatLng;

		return carSearchParams;
	}

	public static CarSearchParams getCarSearchParamsFromJSON(String carSearchParamsJSON) {
		Gson gson = CarServices.generateGson();

		if (Strings.isNotEmpty(carSearchParamsJSON)) {
			try {
				return gson.fromJson(carSearchParamsJSON, CarSearchParams.class);
			}
			catch (JsonSyntaxException jse) {
				Log.e("Failed to fetch carSearchParams: " + carSearchParamsJSON, jse);
			}
		}

		return null;
	}

	private static String getQueryParameterIfExists(Uri uri, Set<String> queryData, String paramKey) {
		if (queryData.contains(paramKey)) {
			return uri.getQueryParameter(paramKey);
		}
		return null;
	}

	public static boolean areTaxesAndFeesIncluded(List<RateBreakdownItem> rateBreakdown) {
		if (rateBreakdown != null && rateBreakdown.size() > 0) {
			for (RateBreakdownItem item : rateBreakdown) {
				if (item.type == RentalFareBreakdownType.TAXES_AND_FEES) {
					return item.price == null;
				}
			}
		}

		return true;
	}
}
