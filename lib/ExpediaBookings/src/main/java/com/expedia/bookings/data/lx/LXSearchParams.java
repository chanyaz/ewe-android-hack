package com.expedia.bookings.data.lx;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;

public class LXSearchParams {

	public String location;
	public LocalDate startDate;
	public LocalDate endDate;

	public String toServerStartDate() {
		return DateUtils.convertToLXDate(startDate);
	}

	public String toServerEndDate() {
		return DateUtils.convertToLXDate(endDate);
	}
}
