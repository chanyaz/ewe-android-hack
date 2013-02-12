package com.expedia.bookings.data.trips;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.Car.Type;

public class ItinCardDataCar extends ItinCardData {
	@SuppressWarnings("serial")
	private static final Map<Type, Integer> CAR_TYPE_DESCRIPTION_MAP = new HashMap<Type, Integer>() {
		{
			put(Type.TWO_DOOR_CAR, R.string.car_type_two_door);
			put(Type.THREE_DOOR_CAR, R.string.car_type_three_door);
			put(Type.FOUR_DOOR_CAR, R.string.car_type_four_door);
			put(Type.VAN, R.string.car_type_van);
			put(Type.WAGON, R.string.car_type_wagon);
			put(Type.LIMOUSINE, R.string.car_type_limousine);
			put(Type.RECREATIONAL_VEHICLE, R.string.car_type_recreational_vehicle);
			put(Type.CONVERTIBLE, R.string.car_type_convertible);
			put(Type.SPORTS_CAR, R.string.car_type_sports_car);
			put(Type.SUV, R.string.car_type_suv);
			put(Type.PICKUP_REGULAR_CAB, R.string.car_type_pickup_regular_cab);
			put(Type.OPEN_AIR_ALL_TERRAIN, R.string.car_type_open_air_all_terrain);
			put(Type.SPECIAL, R.string.car_type_special);
			put(Type.COMMERCIAL_VAN_TRUCK, R.string.car_type_commercial_van_truck);
			put(Type.PICKUP_EXTENDED_CAB, R.string.car_type_pickup_extended_cab);
			put(Type.SPECIAL_OFFER_CAR, R.string.car_type_special_offer_car);
			put(Type.COUPE, R.string.car_type_coupe);
			put(Type.MONOSPACE, R.string.car_type_monospace);
			put(Type.MOTORHOME, R.string.car_type_motor_home);
			put(Type.TWO_WHEEL_VEHICLE, R.string.car_type_two_wheel_vehicle);
			put(Type.ROADSTER, R.string.car_type_roadster);
			put(Type.CROSSOVER, R.string.car_type_crossover);
		}
	};

	private Car mCar;

	public ItinCardDataCar(TripComponent tripComponent) {
		super(tripComponent);
		mCar = ((TripCar) tripComponent).getCar();
	}

	public String getCarTypeDescription(Context context) {
		final int resId = CAR_TYPE_DESCRIPTION_MAP.get(mCar.getType());
		return context.getString(resId);
	}

	public String getVendorName() {
		return mCar.getVendor().getShortName();
	}

	public String getVendorPhone() {
		return "(888) 555-1212";
	}
}