package com.expedia.bookings.data.lx;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;
import com.expedia.bookings.utils.Strings;

public class LXSearchParams {

	public String location;
	public LocalDate startDate;
	public LocalDate endDate;
	public SearchType searchType = SearchType.EXPLICIT_SEARCH;
	public String filters;
	public String activityId;
	public String imageCode;

	public LXSearchParams imageCode(String imageCode) {
		this.imageCode = imageCode;
		return this;
	}

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

	public LXSearchParams filters(String filters) {
		this.filters = filters;
		return this;
	}

	public LXSearchParams activityId(String activityId) {
		this.activityId = activityId;
		return this;
	}

	public boolean hasLocation() {
		return Strings.isNotEmpty(location);
	}

	public boolean hasStartDate() {
		return startDate != null;
	}

	public String toServerStartDate() {
		return DateUtils.convertToLXDate(startDate);
	}

	public String toServerEndDate() {
		return DateUtils.convertToLXDate(endDate);
	}
}
