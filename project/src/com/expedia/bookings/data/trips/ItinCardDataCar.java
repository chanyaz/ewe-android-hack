package com.expedia.bookings.data.trips;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.Car.Type;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.Location;

public class ItinCardDataCar extends ItinCardData implements ConfirmationNumberable {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("serial")
	public static final Map<Type, Integer> CAR_TYPE_DESCRIPTION_MAP = new HashMap<Type, Integer>() {
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

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final Format TIME_FORMAT = new SimpleDateFormat("h:mm a", Locale.getDefault());
	private static final Format SHORT_DATE_FORMAT = new SimpleDateFormat("MMM d", Locale.getDefault());
	private static final Format LONG_DATE_FORMAT = new SimpleDateFormat("EEEE MMMM d, yyyy", Locale.getDefault());

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private Car mCar;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTOR
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataCar(TripComponent tripComponent) {
		super(tripComponent);
		mCar = ((TripCar) tripComponent).getCar();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public String getCarTypeDescription(Context context) {
		final Integer resId = CAR_TYPE_DESCRIPTION_MAP.get(mCar.getType());
		if (resId != null) {
			return context.getString(resId);
		}

		return null;
	}

	public String getCarCategoryImageUrl() {
		return ((TripCar) getTripComponent()).getCarCategoryImageUrl();
	}

	public DateTime getPickUpDate() {
		return getTripComponent().getParentTrip().getStartDate();
	}

	public DateTime getDropOffDate() {
		return getTripComponent().getParentTrip().getEndDate();
	}

	public Location getPickUpLocation() {
		return mCar.getPickUpLocation();
	}

	public Location getDropOffLocation() {
		return mCar.getDropOffLocation();
	}

	public String getFormattedPickUpTime() {
		return TIME_FORMAT.format(getPickUpDate().getCalendar().getTime());
	}

	public String getFormattedDropOffTime() {
		return TIME_FORMAT.format(getDropOffDate().getCalendar().getTime());
	}

	public String getFormattedShortPickUpDate() {
		return SHORT_DATE_FORMAT.format(getPickUpDate().getCalendar().getTime());
	}

	public String getFormattedShortDropOffDate() {
		return SHORT_DATE_FORMAT.format(getDropOffDate().getCalendar().getTime());
	}

	public String getFormattedLongPickUpDate() {
		return LONG_DATE_FORMAT.format(getPickUpDate().getCalendar().getTime());
	}

	public String getFormattedLongDropOffDate() {
		return LONG_DATE_FORMAT.format(getDropOffDate().getCalendar().getTime());
	}

	public String getFormattedDays() {
		final DateTime start = getTripComponent().getParentTrip().getStartDate();
		final DateTime end = getTripComponent().getParentTrip().getEndDate();

		final long period = (end.getMillisFromEpoch() + end.getTzOffsetMillis())
				- (start.getMillisFromEpoch() + start.getTzOffsetMillis());

		return String.valueOf((int) (period / (1000 * 60 * 60 * 24)));
	}

	public String getRelevantVendorPhone() {
		if (!TextUtils.isEmpty(mCar.getVendor().getTollFreePhone())) {
			return mCar.getVendor().getTollFreePhone();
		}

		return mCar.getVendor().getLocalPhone();
	}

	public String getVendorName() {
		return mCar.getVendor().getShortName();
	}

	public Location getRelevantVendorLocation() {
		boolean pickup = System.currentTimeMillis() > mCar.getPickUpDateTime().getMillisFromEpoch();
		return pickup ? mCar.getPickUpLocation() : mCar.getDropOffLocation();
	}

	public String getConfirmationNumber() {
		return mCar.getConfNumber();
	}

	public Intent getPikcupDirectionsIntent() {
		return createDirectionsIntent(getPickUpLocation());
	}

	public Intent getDropOffDirectionsIntent() {
		return createDirectionsIntent(getDropOffLocation());
	}

	public Intent getRelevantDirectionsIntent() {
		return createDirectionsIntent(getRelevantVendorLocation());
	}

	private Intent createDirectionsIntent(Location location) {
		final Uri uri = Uri.parse("http://maps.google.com/maps?daddr=" + location.toLongFormattedString());
		final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setComponent(new ComponentName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"));

		return intent;
	}

	@Override
	public boolean hasConfirmationNumber() {
		if (getTripComponent() != null && ((TripCar) getTripComponent()).getCar() != null
				&& !TextUtils.isEmpty(((TripCar) getTripComponent()).getCar().getConfNumber())) {
			return true;
		}
		return false;
	}

	@Override
	public String getFormattedConfirmationNumbers() {
		if (hasConfirmationNumber()) {
			return ((TripCar) getTripComponent()).getCar().getConfNumber();
		}
		return null;
	}
}
