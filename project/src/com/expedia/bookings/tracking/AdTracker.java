package com.expedia.bookings.tracking;

import android.content.Context;
import android.text.TextUtils;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Item;
import com.google.analytics.tracking.android.Transaction;
import com.mobiata.android.util.SettingUtils;

public class AdTracker {

	public static void initialize(Context context) {
		boolean trackingOptout = SettingUtils.get(context, context.getString(R.string.preference_tracking_optout), false);

		// Google
		EasyTracker.getInstance().setContext(context);

		// AdX
		boolean adxEnabled = !trackingOptout;
		AdX.initialize(context, adxEnabled);
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

		// Google Analytics
		Transaction transaction = new Transaction.Builder(currency, (long) (totalPrice * 1000000))
				.setAffiliation("Expedia").setTotalTaxInMicros((long) (totalTax * 1000000)).setShippingCostInMicros(0)
				.build();

		transaction.addItem(new Item.Builder(propertyId, propertyName, (long) (avgPrice * 1000000),
				(long) (duration * 1000000)).setProductCategory("Hotel").build());

		EasyTracker.getTracker().trackTransaction(transaction);

		// Other
		AdX.trackHotelBooked(Db.getHotelSearch(), currency, totalPrice, avgPrice);
	}

	public static void trackFlightBooked(String currency, double value, int days, String destAirport) {
		AdX.trackFlightBooked(Db.getFlightSearch(), currency, value);
	}

	public static void trackHotelCheckoutStarted() {
		final Rate rate = Db.getHotelSearch().getSelectedRate();
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
