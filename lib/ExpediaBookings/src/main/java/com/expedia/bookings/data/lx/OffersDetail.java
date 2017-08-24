package com.expedia.bookings.data.lx;

import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.utils.DateUtils;

public class OffersDetail {
	public final List<Offer> offers;

	public boolean isAvailableOnDate(LocalDate dateSelected) {
		for (Offer offer : offers) {
			for (AvailabilityInfo activityAvailabilityInfo : offer.availabilityInfo) {
				LocalDate availabilityDate = DateUtils
					.yyyyMMddHHmmssToLocalDate(activityAvailabilityInfo.availabilities.valueDate);
				if (availabilityDate.equals(dateSelected)) {
					return true;
				}
			}
		}

		return false;
	}
}
