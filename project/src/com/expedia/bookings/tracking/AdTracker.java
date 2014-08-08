package com.expedia.bookings.tracking;

import org.joda.time.DateTime;

import android.content.Context;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.JodaUtils;
import com.mobiata.android.Log;

public class AdTracker {

	public static void initialize(Context context) {
		// AdX
		AdX.initialize(context, true);
	}

	public static void trackFirstLaunch() {
		// Other
		AdX.trackFirstLaunch();
	}

	public static void trackLaunch(Context context) {
		// Other
		AdX.trackLaunch();
	}

	public static void trackLogin() {
		// Other
		AdX.trackLogin();
	}

	public static void trackViewHomepage() {
		AdX.trackViewHomepage();
	}

	public static void trackViewItinList() {
		AdX.trackViewItinList();
	}

	public static void trackHotelBooked() {
		// Values
		final HotelSearchParams searchParams = Db.getHotelSearch().getSearchParams().copy();
		final Property property = Db.getHotelSearch().getSelectedProperty();
		final Rate rate = Db.getHotelSearch().getBookingRate();

		final String propertyId = property.getPropertyId();
		final String propertyName = property.getName();
		final String currency = rate.getDisplayPrice().getCurrency();
		final Integer duration = searchParams.getStayDuration();
		final Double avgPrice = rate.getAverageRate().getAmount().doubleValue();
		final Double totalPrice = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		final Double totalTax = rate.getTaxesAndFeesPerRoom() != null ? rate.getTaxesAndFeesPerRoom().getAmount()
				.doubleValue() : 0;

		// Other
		String orderNumber = Db.getHotelBookingResponse() != null ? Db.getHotelBookingResponse().getOrderNumber() : "";
		AdX.trackHotelBooked(Db.getHotelSearch(), orderNumber, currency, totalPrice, avgPrice);
	}

	public static void trackFlightBooked() {
		try {
			if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
				FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
				int days = 0;
				if (trip.getLegCount() > 0) {
					FlightLeg firstLeg = Db.getFlightSearch().getSelectedFlightTrip().getLeg(0);
					DateTime departureCal = new DateTime(firstLeg.getFirstWaypoint().getMostRelevantDateTime());
					DateTime now = DateTime.now();
					days = JodaUtils.daysBetween(departureCal, now);
					if (days < 0) {
						days = 0;
					}
				}
				Money money = Db.getFlightSearch().getSelectedFlightTrip().getTotalFare();
				String destAirportCode = Db.getFlightSearch().getSearchParams().getArrivalLocation()
					.getDestinationId();
				if (money != null) {
					String orderNumber = Db.getFlightCheckout() != null ? Db.getFlightCheckout().getOrderId() : "";
					AdX.trackFlightBooked(Db.getFlightSearch(), orderNumber, money.getCurrency(), money.getAmount().doubleValue());
				}
			}
		}
		catch (Exception ex) {
			Log.e("Exception tracking flight checkout", ex);
		}
	}

	public static void trackHotelCheckoutStarted() {
		final Rate rate = Db.getHotelSearch().getCheckoutRate();
		final Money totalPrice = rate.getTotalAmountAfterTax();
		AdX.trackHotelCheckoutStarted(Db.getHotelSearch(), totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
	}

	public static void trackFlightCheckoutStarted() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
			Money totalPrice = Db.getFlightSearch().getSelectedFlightTrip().getTotalFare();
			AdX.trackFlightCheckoutStarted(Db.getFlightSearch(), totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		}
	}

	public static void trackHotelSearch() {
		if (Db.getHotelSearch() != null) {
			AdX.trackHotelSearch(Db.getHotelSearch());
		}
	}

	public static void trackFlightSearch() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			AdX.trackFlightSearch(Db.getFlightSearch());
		}
	}
}
