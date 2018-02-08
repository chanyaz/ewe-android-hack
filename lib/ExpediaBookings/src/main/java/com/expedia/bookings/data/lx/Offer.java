package com.expedia.bookings.data.lx;

import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;

public class Offer {
	public String id;
	public String title;
	public List<AvailabilityInfo> availabilityInfo;
	public boolean freeCancellation;
	public String duration;
	public String description;
	public int freeCancellationMinHours;
	public int discountPercentage;

	// This is not coming from server, its for client side manipulation
	public boolean isToggled;
	public String bags;
	public String passengers;
	public boolean isGroundTransport;
	public LXRedemptionType redemptionType;

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
}
