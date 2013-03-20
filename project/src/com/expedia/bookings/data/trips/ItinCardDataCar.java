package com.expedia.bookings.data.trips;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.Car.Category;
import com.expedia.bookings.data.Car.Type;
import com.expedia.bookings.data.CarVendor;
import com.expedia.bookings.data.DateTime;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.utils.CalendarUtils;
import com.google.android.gms.maps.model.LatLng;
import com.mobiata.flightlib.utils.DateTimeUtils;

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

	private static final int TIME_FLAGS = DateUtils.FORMAT_SHOW_TIME;
	private static final int SHORT_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR
			| DateUtils.FORMAT_ABBREV_MONTH;
	private static final int LONG_DATE_FLAGS = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
			| DateUtils.FORMAT_SHOW_WEEKDAY;

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

	public String getCarCategoryDescription(Context context) {
		Category category = mCar.getCategory();
		if (category != null) {
			return context.getString(category.getCategoryResId());
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

	public String getFormattedPickUpTime(Context context) {
		return getPickUpDate().formatTime(context, TIME_FLAGS) + " " + getPickUpDate().formatTimeZone();
	}

	public String getFormattedDropOffTime(Context context) {
		return getDropOffDate().formatTime(context, TIME_FLAGS) + " " + getDropOffDate().formatTimeZone();
	}

	public String getFormattedShortPickUpDate(Context context) {
		return getPickUpDate().formatTime(context, SHORT_DATE_FLAGS);
	}

	public String getFormattedShortDropOffDate(Context context) {
		return getDropOffDate().formatTime(context, SHORT_DATE_FLAGS);
	}

	public String getFormattedLongPickUpDate(Context context) {
		return getPickUpDate().formatTime(context, LONG_DATE_FLAGS);
	}

	public String getFormattedLongDropOffDate(Context context) {
		return getDropOffDate().formatTime(context, LONG_DATE_FLAGS);
	}

	public String getFormattedDays() {
		Trip trip = getTripComponent().getParentTrip();
		return Integer.toString((int) CalendarUtils.getDaysBetween(trip.getStartDate().getCalendar(), trip.getEndDate()
				.getCalendar()));
	}

	public String getRelevantVendorPhone() {
		CarVendor vendor = mCar.getVendor();
		String phone = vendor.getLocalPhone();
		if (TextUtils.isEmpty(phone)) {
			phone = vendor.getTollFreePhone();
		}
		return phone;
	}

	public String getLocalPhoneNumber() {
		return mCar.getVendor().getLocalPhone();
	}

	public String getTollFreePhoneNumber() {
		return mCar.getVendor().getTollFreePhone();
	}

	public String getVendorName() {
		return mCar.getVendor().getShortName();
	}

	public Location getRelevantVendorLocation() {
		return showPickUp() ? mCar.getPickUpLocation() : mCar.getDropOffLocation();
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

	/**
	 * @return true if we want to focus on the pickup time, false for drop off
	 */
	public boolean showPickUp() {
		Calendar pickUpCal = getPickUpDate().getCalendar();
		Calendar dropOffCal = getDropOffDate().getCalendar();
		Calendar now = Calendar.getInstance(pickUpCal.getTimeZone());

		// This should work as long as they're not renting a car for a year (not possible I believe)
		int pickUpDayOfYear = pickUpCal.get(Calendar.DAY_OF_YEAR);
		int dropOffDayOfyear = dropOffCal.get(Calendar.DAY_OF_YEAR);
		int dayOfYear = now.get(Calendar.DAY_OF_YEAR);
		boolean sameDayRental = pickUpDayOfYear == dropOffDayOfyear;
		boolean isFourHoursBeforeDropOff = now.getTimeInMillis() > dropOffCal.getTimeInMillis() - (1000 * 60 * 60 * 4);

		return now.before(pickUpCal) || (!sameDayRental && dayOfYear == pickUpDayOfYear)
				|| (sameDayRental && !isFourHoursBeforeDropOff);
	}

	@Override
	public int getConfirmationNumberLabelResId() {
		return R.string.car_rental_confirmation_code_label;
	}

	@Override
	public LatLng getLocation() {
		Location loc = showPickUp() ? getPickUpLocation() : getDropOffLocation();

		if (loc != null) {
			return new LatLng(loc.getLatitude(), loc.getLongitude());
		}

		return super.getLocation();
	}
}
