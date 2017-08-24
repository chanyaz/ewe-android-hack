package com.expedia.bookings.data.trips;

import org.joda.time.DateTime;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.CarVendor;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.cars.CarCategory;
import com.expedia.bookings.data.trips.ItinCardData.ConfirmationNumberable;
import com.expedia.bookings.utils.CarDataUtils;
import com.expedia.bookings.utils.JodaUtils;
import com.google.android.gms.maps.model.LatLng;

public class ItinCardDataCar extends ItinCardData implements ConfirmationNumberable {

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

	private final Car mCar;

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

	public Car getCar() {
		return mCar;
	}

	public String getCarCategoryDescription(Context context) {
		CarCategory category = mCar.getCategory();
		if (category != null) {
			return CarDataUtils.getCategoryStringFor(context, category);
		}

		return null;
	}

	public DateTime getPickUpDate() {
		if (mCar.getPickUpDateTime() == null) {
			return getStartDate();
		}
		return mCar.getPickUpDateTime();
	}

	public DateTime getDropOffDate() {
		if (mCar.getDropOffDateTime() == null) {
			return getEndDate();
		}
		return mCar.getDropOffDateTime();
	}

	public Location getPickUpLocation() {
		return mCar.getPickUpLocation();
	}

	public Location getDropOffLocation() {
		return mCar.getDropOffLocation();
	}

	public String getFormattedPickUpTime(Context context) {
		return JodaUtils.formatDateTime(context, getPickUpDate(), TIME_FLAGS) + " "
				+ JodaUtils.formatTimeZone(getPickUpDate());
	}

	public String getFormattedDropOffTime(Context context) {
		return JodaUtils.formatDateTime(context, getDropOffDate(), TIME_FLAGS) + " "
				+ JodaUtils.formatTimeZone(getDropOffDate());
	}

	public String getFormattedShortPickUpDate(Context context) {
		return JodaUtils.formatDateTime(context, getPickUpDate(), SHORT_DATE_FLAGS);
	}

	public String getFormattedShortDropOffDate(Context context) {
		return JodaUtils.formatDateTime(context, getDropOffDate(), SHORT_DATE_FLAGS);
	}

	public String getFormattedLongPickUpDate(Context context) {
		return JodaUtils.formatDateTime(context, getPickUpDate(), LONG_DATE_FLAGS);
	}

	public String getFormattedLongDropOffDate(Context context) {
		return JodaUtils.formatDateTime(context, getDropOffDate(), LONG_DATE_FLAGS);
	}

	public int getDays() {
		Trip trip = getTripComponent().getParentTrip();
		return JodaUtils.daysBetween(trip.getStartDate(), trip.getEndDate());
	}

	public int getInclusiveDays() {
		return getDays() + 1;
	}

	public String getFormattedDays() {
		return Integer.toString(getInclusiveDays());
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

	public Intent getPickupDirectionsIntent() {
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

		return intent;
	}

	@Override
	public boolean hasConfirmationNumber() {
		return getTripComponent() != null && ((TripCar) getTripComponent()).getCar() != null
			&& !TextUtils.isEmpty(((TripCar) getTripComponent()).getCar().getConfNumber());
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
		DateTime pickUpDate = getPickUpDate();
		DateTime dropOffDate = getDropOffDate();
		DateTime now = DateTime.now(pickUpDate.getZone());

		// This should work as long as they're not renting a car for a year (not possible I believe)
		int pickUpDayOfYear = pickUpDate.getDayOfYear();
		int dropOffDayOfyear = dropOffDate.getDayOfYear();
		int dayOfYear = now.getDayOfYear();
		boolean sameDayRental = pickUpDayOfYear == dropOffDayOfyear;
		boolean isFourHoursBeforeDropOff = now.getMillis()
				> dropOffDate.getMillis() - (4 * DateUtils.HOUR_IN_MILLIS);

		return now.isBefore(pickUpDate) || (!sameDayRental && dayOfYear == pickUpDayOfYear)
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
