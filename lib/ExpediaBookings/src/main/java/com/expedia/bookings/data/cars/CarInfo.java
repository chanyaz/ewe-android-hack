package com.expedia.bookings.data.cars;

import java.util.List;

public class CarInfo {

	public CarCategory category;
	public CarType type;
	public Fuel fuel;
	public Transmission transmission;
	public Drive drive;

	public boolean hasAirConditioning;
	public List<String> makes;

	public int minDoors;
	public int maxDoors;
	public int adultCapacity;
	public int childCapacity;
	public int largeLuggageCapacity;
	public int smallLuggageCapacity;
}
