package com.expedia.bookings.data.lx;

import com.google.gson.annotations.SerializedName;

public enum LXTicketType {
	Traveler,
	Adult,
	Child,
	Infant,
	Youth,
	Senior,
	Group,
	Couple,
	@SerializedName("2 Adults")
	Two_Adults,
	Military,
	Student,
	Sedan,
	Minivan,
	@SerializedName("Water Taxi")
	Water_Taxi,
	SUV,
	@SerializedName("Executive Car")
	Executive_Car,
	@SerializedName("Luxury Car")
	Luxury_Car,
	Limousine,
	TownCar,
	@SerializedName("Vehicle Parking Spot")
	Vehicle_Parking_Spot,
	Book,
	Guide,
	@SerializedName("Travel Card")
	Travel_Card,
	Boat,
	Motorcycle,
	Ceremony,
	@SerializedName("Calling Card")
	Calling_Card,
	Pass,
	Minibus,
	Helicopter,
	Device,
	Room,
	Carriage,
	Buggy,
	ATV,
	@SerializedName("Jet Ski")
	Jet_Ski,
	Scooter,
	@SerializedName("Scooter Car")
	Scooter_Car,
	Snowmobile,
	Day,
	Bike,
	Week,
	Subscription,
	@SerializedName("Electric Bike")
	Electric_Bike,
	Segway,
	Vehicle
}
