package com.expedia.bookings.tracking;

import java.util.Date;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Item;
import com.google.analytics.tracking.android.Transaction;

public class AdTracker {

	public static void initialize(Context context) {
		// Google
		EasyTracker.getInstance().setContext(context);

		// AdX
		AdX.initialize(context, true);
	}

	public static void trackFirstLaunch() {
		// Google Analytics
		EasyTracker.getTracker().trackEvent("user_action", "first_launch", "launch", null);

		// Other
		AdX.trackFirstLaunch();
	}

	public static void trackLaunch(Context context) {
		// Google Analytics
		EasyTracker.getTracker().trackEvent("user_action", "launch", "launch", null);

		// Other
		AdX.trackLaunch();

		OmnitureTracking.trackAppLaunch(context);
	}

	public static void trackLogin() {
		// Google Analytics
		EasyTracker.getTracker().trackEvent("user_action", "login", "login", null);

		// Other
		AdX.trackLogin();
	}

	public static void trackViewItinList() {
		AdX.trackViewItinList();
	}

	public static void trackHotelBooked() {
		// Values
		final SearchParams searchParams = Db.getSearchParams().copy();
		final Property property = Db.getSelectedProperty();
		final Rate rate = Db.getSelectedRate();

		final String propertyId = property.getPropertyId();
		final String propertyName = property.getName();
		final String currency = rate.getDisplayRate().getCurrency();
		final Integer duration = searchParams.getStayDuration();
		final Double avgPrice = rate.getAverageRate().getAmount().doubleValue();
		final Double totalPrice = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		final Double totalTax = rate.getTaxesAndFeesPerRoom() != null ? rate.getTaxesAndFeesPerRoom().getAmount()
				.doubleValue() : 0;
		final Integer daysRemaining = (int) ((searchParams.getCheckInDate().getTime().getTime() - new Date().getTime()) / (24 * 60 * 60 * 1000));

		// Google Analytics
		Transaction transaction = new Transaction.Builder(currency, (long) (totalPrice * 1000000))
				.setAffiliation("Expedia").setTotalTaxInMicros((long) (totalTax * 1000000)).setShippingCostInMicros(0)
				.build();

		transaction.addItem(new Item.Builder(propertyId, propertyName, (long) (avgPrice * 1000000),
				(long) (duration * 1000000)).setProductCategory("Hotel").build());

		EasyTracker.getTracker().trackTransaction(transaction);

		// Other
		AdX.trackHotelBooked(currency, totalPrice);
	}

	public static void trackFlightBooked(String currency, double value, int days, String destAirport) {
		AdX.trackFlightBooked(currency, value);
	}

	public static void trackHotelCheckoutStarted() {
		final Rate rate = Db.getSelectedRate();
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
		SearchParams params = Db.getSearchParams();
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
