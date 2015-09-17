package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.Map;

import com.expedia.bookings.R;
import com.expedia.bookings.data.lx.LXTicketType;

public class LXDataUtils {

	public static final Map<LXTicketType, Integer> LX_PER_TICKET_TYPE_MAP = new HashMap() {
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

	public static final Map<LXTicketType, Integer> LX_TICKET_TYPE_NAME_MAP = new HashMap() {
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
}
