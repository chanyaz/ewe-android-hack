package com.expedia.bookings.data.cars;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Strings;

public class CarSearchParamsBuilder {

	// Car Search requires:
	// 1. Origin
	//     OR
	// 2. pickupLocation
	// Both are mutually exclusive.
	// Origin is used for just Airport Code based searches. Pickup Location based searches allow for non-airport searches too.
	// If both are present, the consume should honor only one of them!
	private String mOrigin;
	private LatLong mPickupLocationLatLng;

	//A descriptive name of the Search Location, to be displayed in the Toolbar
	private String mOriginDescription;

	private DateTimeBuilder mDateTimeBuilder;

	public static class DateTimeBuilder {
		private LocalDate mStartDate;
		private LocalDate mEndDate;
		private int mStartMillis;
		private int mEndMillis;

		public DateTimeBuilder() {
		}

		public DateTimeBuilder startDate(LocalDate date) {
			mStartDate = date;
			return this;
		}

		public DateTimeBuilder endDate(LocalDate date) {
			mEndDate = date;
			return this;
		}

		public DateTimeBuilder startMillis(int millis) {
			mStartMillis = millis;
			return this;
		}

		public DateTimeBuilder endMillis(int millis) {
			mEndMillis = millis;
			return this;
		}

		public boolean areRequiredParamsFilled() {
			return mStartDate != null && mEndDate != null;
		}

		public boolean isStartDateEqualToToday() {
			if (mStartDate != null) {
				return mStartDate.isEqual(LocalDate.now());
			}
			return false;
		}

		public boolean isEndDateEqualToToday() {
			if (mEndDate != null) {
				return mEndDate.isEqual(LocalDate.now());
			}
			return false;
		}

		public boolean isStartEqualToEnd() {
			if (mStartDate != null && mEndDate != null) {
				return mStartDate.isEqual(mEndDate);
			}
			return false;
		}

		public long getStartMillis() {
			return mStartMillis;
		}

		public long getEndMillis() {
			return mEndMillis;
		}
	}

	public CarSearchParamsBuilder dateTimeBuilder(DateTimeBuilder dtb) {
		mDateTimeBuilder = dtb;
		return this;
	}

	public CarSearchParamsBuilder pickupLocationLatLng(LatLong pickupLocationLatLng) {
		if (Strings.isNotEmpty(mOrigin) && pickupLocationLatLng != null) {
			throw new IllegalStateException("CarSearchParamsBuilder in an invalid state. Attempt to set pickupLocationLatLng while pickupLocation too exists.");
		}
		mPickupLocationLatLng = pickupLocationLatLng;
		return this;
	}

	public CarSearchParamsBuilder origin(String origin) {
		if (mPickupLocationLatLng != null && Strings.isNotEmpty(origin)) {
			throw new IllegalStateException("CarSearchParamsBuilder in an invalid state. Attempt to set pickupLocation while pickupLocationLatLng too exists.");
		}
		mOrigin = origin;
		return this;
	}

	public CarSearchParamsBuilder originDescription(String originDescription) {
		mOriginDescription = originDescription;
		return this;
	}

	public CarSearchParams build() {
		if (Strings.isEmpty(mOrigin) && (mPickupLocationLatLng == null)) {
			throw new IllegalStateException("CarSearchParamsBuilder in an invalid state. Both pickupLocation and pickupLocationLatLng are null.");
		}
		else if (Strings.isNotEmpty(mOrigin) && (mPickupLocationLatLng != null)) {
			throw new IllegalStateException("CarSearchParamsBuilder in an invalid state. Both pickupLocation and pickupLocationLatLng are non-null.");
		}

		CarSearchParams params = new CarSearchParams();

		params.origin = mOrigin;
		if (mPickupLocationLatLng != null) {
			params.pickupLocationLatLng = new LatLong(mPickupLocationLatLng.lat, mPickupLocationLatLng.lng);
		}
		else {
			params.pickupLocationLatLng = null;
		}

		params.originDescription = mOriginDescription;

		if (mDateTimeBuilder != null) {
			if (mDateTimeBuilder.mStartDate != null) {
				params.startDateTime = DateUtils.localDateAndMillisToDateTime(mDateTimeBuilder.mStartDate,
					mDateTimeBuilder.mStartMillis);
			}
			if (mDateTimeBuilder.mEndDate != null) {
				params.endDateTime = DateUtils.localDateAndMillisToDateTime(mDateTimeBuilder.mEndDate, mDateTimeBuilder.mEndMillis);
			}
		}
		return params;
	}

	public boolean areRequiredParamsFilled() {
		return hasOrigin() && hasStartAndEndDates();
	}

	public boolean hasStartAndEndDates() {
		return mDateTimeBuilder != null && mDateTimeBuilder.areRequiredParamsFilled();
	}

	public boolean hasOrigin() {
		return Strings.isNotEmpty(mOrigin) || mPickupLocationLatLng != null;
	}
}
