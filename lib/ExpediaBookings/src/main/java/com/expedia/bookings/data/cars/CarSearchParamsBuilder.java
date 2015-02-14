package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.expedia.bookings.utils.Strings;

public class CarSearchParamsBuilder {

	private String mOrigin;
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
	}

	public CarSearchParamsBuilder dateTimeBuilder(DateTimeBuilder dtb) {
		mDateTimeBuilder = dtb;
		return this;
	}

	public CarSearchParamsBuilder origin(String origin) {
		mOrigin = origin;
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
		return Strings.isNotEmpty(mOrigin) && mDateTimeBuilder != null && mDateTimeBuilder.areRequiredParamsFilled();
	}
}
