package com.expedia.bookings.data.lx;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.Strings;

public class LXSearchParamsBuilder {
	public String location;
	public LocalDate startDate;
	public LocalDate endDate;

	public LXSearchParamsBuilder location(String location) {
		this.location = location;
		return this;
	}

	public LXSearchParamsBuilder startDate(LocalDate startDate) {
		this.startDate = startDate;
		return this;
	}

	public LXSearchParamsBuilder endDate(LocalDate endDate) {
		this.endDate = endDate;
		return this;
	}

	public LXSearchParams build() {
		LXSearchParams searchParams = new LXSearchParams();
		searchParams.location = location;
		if (startDate != null) {
			searchParams.startDate = startDate;
		}
		if (endDate != null) {
			searchParams.endDate = endDate;
		}
		return searchParams;
	}

	public boolean hasLocation() {
		return Strings.isNotEmpty(location);
	}

	public boolean hasStartDate() {
		return startDate != null;
	}
}
