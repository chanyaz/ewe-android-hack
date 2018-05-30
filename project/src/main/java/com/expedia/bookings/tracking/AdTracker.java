package com.expedia.bookings.tracking;


import org.joda.time.LocalDate;

import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.utils.TuneUtils;

public class AdTracker {
	public static void trackLogin() {
		// Other
		TuneUtils.trackLogin();
	}

	public static void trackLXSearchResults(LxSearchParams searchParams, LXSearchResponse searchResponse) {
		TuneUtils.trackLXSearch(searchParams, searchResponse);
		new FacebookEvents().trackLXSearch(searchParams, searchResponse);
	}

	public static void trackFilteredLXSearchResults(LxSearchParams searchParams, LXSearchResponse searchResponse) {
		new FacebookEvents().trackLXSearch(searchParams, searchResponse);
	}

	public static void trackLXDetails(String activityId, String destination, LocalDate startDate, String regionId,
		String currencyCode, String activityValue) {
		new FacebookEvents().trackLXDetail(activityId, destination, startDate, regionId, currencyCode, activityValue);
	}

	public static void trackLXCheckoutStarted(String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate,
		int selectedTicketCount, String lxActivityTitle, String regionId,
		String activityId, LocalDate startDate, int selectedChildTicketCount) {
		TuneUtils.trackLXDetails(lxActivityLocation, totalPrice, lxOfferSelectedDate,
			selectedTicketCount, lxActivityTitle, activityId);
		new FacebookEvents().trackLXCheckout(activityId, lxActivityLocation, startDate,
			regionId, totalPrice, selectedTicketCount, selectedChildTicketCount);
	}

	public static void updatePOS() {
		TuneUtils.updatePOS();
	}

	public static void trackLXBooked(String itinNumber, String lxActivityLocation, Money totalPrice, Money ticketPrice,
		String lxActivityStartDate, String lxActivityTitle, String activityId,
		LocalDate startDate, String regionId, int selectedTicketCount, int selectedChildTicketCount) {
		TuneUtils.trackLXConfirmation(itinNumber, activityId, lxActivityLocation, totalPrice, ticketPrice, lxActivityStartDate,
			lxActivityTitle, selectedTicketCount, selectedChildTicketCount);
		new FacebookEvents().trackLXConfirmation(activityId, lxActivityLocation, startDate, regionId, totalPrice,
			selectedTicketCount, selectedChildTicketCount);
	}
}
