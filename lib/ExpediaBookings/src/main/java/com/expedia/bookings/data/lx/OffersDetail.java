package com.expedia.bookings.data.lx;

import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.ApiDateUtils;

public class OffersDetail {
	public List<Offer> offers;

	public boolean isAvailableOnDate(LocalDate dateSelected) {
		for (Offer offer : offers) {
			for (AvailabilityInfo activityAvailabilityInfo : offer.availabilityInfo) {
				LocalDate availabilityDate = ApiDateUtils
					.yyyyMMddHHmmssToLocalDate(activityAvailabilityInfo.availabilities.valueDate);
				if (availabilityDate.equals(dateSelected)) {
					return true;
				}
			}
		}

		return false;
	}
}
