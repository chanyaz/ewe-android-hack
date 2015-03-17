package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.expedia.bookings.utils.Strings;

public class CarSearchParamsBuilder {

	private String mOrigin;
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

	public CarSearchParamsBuilder origin(String origin) {
		mOrigin = origin;
		return this;
	}

	public CarSearchParamsBuilder originDescription(String originDescription) {
		mOriginDescription = originDescription;
		return this;
	}

	public CarSearchParams build() {
		CarSearchParams params = new CarSearchParams();
		params.origin = mOrigin;
		if (mDateTimeBuilder != null) {
			if (mDateTimeBuilder.mStartDate != null) {
				params.startDateTime = make(mDateTimeBuilder.mStartDate, mDateTimeBuilder.mStartMillis);
			}
			if (mDateTimeBuilder.mEndDate != null) {
				params.endDateTime = make(mDateTimeBuilder.mEndDate, mDateTimeBuilder.mEndMillis);
			}
			params.originDescription = mOriginDescription;
		}
		return params;
	}

	private DateTime make(LocalDate date, int millis) {
		DateTime convertedDateTime = new DateTime();
		return convertedDateTime.withYear(date.getYear())
			.withMonthOfYear(date.getMonthOfYear())
			.withDayOfMonth(date.getDayOfMonth())
			.withTimeAtStartOfDay()
			.plusMillis(millis);
	}

	public boolean areRequiredParamsFilled() {
		return hasOrigin() && hasStartAndEndDates();
	}

	public boolean hasStartAndEndDates() {
		return mDateTimeBuilder != null && mDateTimeBuilder.areRequiredParamsFilled();
	}

	public boolean hasOrigin() {
		return Strings.isNotEmpty(mOrigin);
	}
}
