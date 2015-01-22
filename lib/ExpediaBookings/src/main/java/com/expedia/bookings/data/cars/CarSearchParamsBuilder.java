package com.expedia.bookings.data.cars;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.expedia.bookings.utils.Strings;

public class CarSearchParamsBuilder {

	private LocalDate mStartDate;
	private LocalDate mEndDate;
	private String mOrigin;

	private int mStartMillis;
	private int mEndMillis;

	public CarSearchParamsBuilder startDate(LocalDate date) {
		mStartDate = date;
		return this;
	}

	public CarSearchParamsBuilder endDate(LocalDate date) {
		mEndDate = date;
		return this;
	}

	public CarSearchParamsBuilder startMillis(int millis) {
		mStartMillis = millis;
		return this;
	}

	public CarSearchParamsBuilder endMillis(int millis) {
		mEndMillis = millis;
		return this;
	}

	public CarSearchParamsBuilder origin(String origin) {
		mOrigin = origin;
		return this;
	}

	public CarSearchParams build() {
		CarSearchParams params = new CarSearchParams();
		params.origin = mOrigin;
		if (mStartDate != null) {
			DateTime start = make(mStartDate, mStartMillis);
			params.startDateTime = start;
		}
		if (mEndDate != null) {
			DateTime end = make(mEndDate, mEndMillis);
			params.endDateTime = end;
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
		return !Strings.isEmpty(mOrigin) && mStartDate != null && mEndDate != null;
	}
}
