package com.expedia.bookings.tracking;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;

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

		OmnitureTracking.trackAppLaunch(context);
	}

	public static void trackLogin() {
		// Other
		AdX.trackLogin();
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
		AdX.trackHotelBooked(currency, totalPrice);
	}

	public static void trackFlightBooked(String currency, double value, int days, String destAirport) {
		AdX.trackFlightBooked(currency, value);
	}

	public static void trackHotelCheckoutStarted() {
		final Rate rate = Db.getHotelSearch().getSelectedRate();
		final Money totalPrice = rate.getTotalAmountAfterTax();
		AdX.trackHotelCheckoutStarted(totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
	}

	public static void trackFlightCheckoutStarted() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSelectedFlightTrip() != null) {
			Money totalPrice = Db.getFlightSearch().getSelectedFlightTrip().getTotalFare();
			AdX.trackFlightCheckoutStarted(totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		}
	}

	public static void trackHotelSearch() {
		HotelSearchParams params = Db.getHotelSearch().getSearchParams();
		if (params != null && !TextUtils.isEmpty(params.getRegionId())) {
			AdX.trackHotelSearch(params.getRegionId());
		}
	}

	public static void trackFlightSearch() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();
			String dest = searchParams.getArrivalLocation().getDestinationId();
			AdX.trackFlightSearch(dest);
		}
	}
}
