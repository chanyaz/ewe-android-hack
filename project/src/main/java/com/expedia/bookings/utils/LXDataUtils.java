package com.expedia.bookings.utils;

import java.util.EnumMap;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXTicketType;

public class LXDataUtils {

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
}
