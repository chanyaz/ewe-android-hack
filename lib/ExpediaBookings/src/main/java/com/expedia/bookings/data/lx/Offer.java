package com.expedia.bookings.data.lx;

import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;

public class Offer {
	public String id;
	public String title;
	public List<AvailabilityInfo> availabilityInfo;

	public AvailabilityInfo getAvailabilityInfoOnDate(LocalDate dateSelected) {
		for (AvailabilityInfo activityAvailabilityInfo : availabilityInfo) {
			LocalDate availabilityDate = DateUtils
				.yyyyMMddHHmmssToLocalDate(activityAvailabilityInfo.availabilities.valueDate);
			if (availabilityDate.equals(dateSelected)) {
				return activityAvailabilityInfo;
			}
		}

		return null;
	}

	public boolean isAvailableOnDate(LocalDate dateSelected) {
		for (AvailabilityInfo activityAvailabilityInfo : availabilityInfo) {
			LocalDate availabilityDate = DateUtils
				.yyyyMMddHHmmssToLocalDate(activityAvailabilityInfo.availabilities.valueDate);
			if (availabilityDate.equals(dateSelected)) {
				return true;
			}
		}

		return false;
	}
}
