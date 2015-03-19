package com.expedia.bookings.data.lx;

import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;

public class Offer {
	public String id;
	public String title;
	public List<AvailabilityInfo> availabilityInfo;

	// Utility for available info on selected date - not coming from the API
	public AvailabilityInfo availabilityInfoOfSelectedDate;


	public AvailabilityInfo updateAvailabilityInfoOfSelectedDate(LocalDate dateSelected) {
		for (AvailabilityInfo activityAvailabilityInfo : availabilityInfo) {
			LocalDate availabilityDate = DateUtils
				.yyyyMMddHHmmssToLocalDate(activityAvailabilityInfo.availabilities.valueDate);
			if (availabilityDate.equals(dateSelected)) {
				this.availabilityInfoOfSelectedDate = activityAvailabilityInfo;
				return availabilityInfoOfSelectedDate;
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
