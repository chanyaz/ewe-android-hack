package com.expedia.bookings.data.lx;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;

public class LXSearchParams {

	public String location;
	public LocalDate startDate;
	public LocalDate endDate;
	public SearchType searchType;

	public LXSearchParams location(String location) {
		this.location = location;
		return this;
	}

	public LXSearchParams startDate(LocalDate startDate) {
		this.startDate = startDate;
		return this;
	}

	public LXSearchParams endDate(LocalDate endDate) {
		this.endDate = endDate;
		return this;
	}

	public LXSearchParams searchType(SearchType searchType) {
		this.searchType = searchType;
		return this;
	}

	public String toServerStartDate() {
		return DateUtils.convertToLXDate(startDate);
	}

	public String toServerEndDate() {
		return DateUtils.convertToLXDate(endDate);
	}
}
