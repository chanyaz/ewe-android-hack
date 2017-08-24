package com.expedia.bookings.data.cars;

import java.util.ArrayList;
import java.util.List;

public class CarInfo {

	public CarCategory category;
	public CarType type;
	public Fuel fuel;
	public final Transmission transmission;
	public Drive drive;

	public final boolean hasAirConditioning;
	public List<String> makes = new ArrayList<>();

	public int minDoors;
	public int maxDoors;
	public int adultCapacity;
	public int childCapacity;
	public int largeLuggageCapacity;
	public int smallLuggageCapacity;
	public String carCategoryDisplayLabel;

	/**
	 * makes usually contains an array of car make descriptions, although, is
	 * occasionally empty.
	 *
	 * @return - description of the make of the car
	 */
	public String getMakesDescription() {
		if (makes.isEmpty()) {
			return "";
		}
		else {
			return makes.get(0);
		}
	}

}
