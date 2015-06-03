package com.expedia.bookings.tracking;

import android.content.Context;

import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.KahunaUtils;
import com.expedia.bookings.utils.LeanPlumUtils;
import com.mobiata.android.Log;

public class AdTracker {

	public static void initialize(Context context) {
		boolean adxEnabled = !ExpediaBookingApp.isAutomation() && ProductFlavorFeatureConfiguration.getInstance().isAdXEnabled();
		AdX.initialize(context, adxEnabled);
	}

	public static void trackFirstLaunch() {
		// Other
		AdX.trackFirstLaunch();
		LeanPlumUtils.tracking("FirstLaunch");
	}

	public static void trackLaunch(Context context) {
		// Other
		AdX.trackLaunch();
		LeanPlumUtils.tracking("Launch");
	}

	public static void trackLogin() {
		// Other
		AdX.trackLogin();
		LeanPlumUtils.tracking("Login");
		KahunaUtils.tracking("Login");
	}

	public static void trackLogout() {
		LeanPlumUtils.updateLoggedInStatus();
		KahunaUtils.tracking("Logout");
	}

	public static void trackViewHomepage() {
		AdX.trackViewHomepage();
	}

	public static void trackViewItinList() {
		AdX.trackViewItinList();
		LeanPlumUtils.tracking("Itinerary");
		KahunaUtils.tracking("view_itinerary");
	}

	public static void trackViewItinExpanded() {
		LeanPlumUtils.tracking("Expand Itinerary");
	}

	public static void trackHotelBooked() {
		// Values
		final Rate rate = Db.getTripBucket().getHotel().getRate();

		final String currency = rate.getDisplayPrice().getCurrency();
		final Double avgPrice = rate.getAverageRate().getAmount().doubleValue();
		final Double totalPrice = rate.getTotalAmountAfterTax().getAmount().doubleValue();

		// Other
		HotelBookingResponse response = Db.getTripBucket().getHotel().getBookingResponse();
		String orderNumber = response != null ? response.getOrderNumber() : "";
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getProperty();
		AdX.trackHotelBooked(params, property, orderNumber, currency, totalPrice, avgPrice);
		LeanPlumUtils.trackHotelBooked(params, property, orderNumber, currency, totalPrice, avgPrice);
		KahunaUtils.trackHotelBooked(params, property, orderNumber, currency, totalPrice, avgPrice);
	}

	public static void trackFlightBooked() {
		try {
			if (Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().getFlightTrip() != null) {
				FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
				Money money = trip.getTotalFare();
				if (money != null) {
					String orderNumber = Db.getTripBucket().getFlight().getCheckoutResponse() != null ?
						Db.getTripBucket().getFlight().getCheckoutResponse().getOrderId() : "";
					AdX.trackFlightBooked(Db.getTripBucket().getFlight().getFlightSearch(), orderNumber,
						money.getCurrency(), money.getAmount().doubleValue());
					LeanPlumUtils.trackFlightBooked(Db.getTripBucket().getFlight().getFlightSearch(), orderNumber,
						money.getCurrency(), money.getAmount().doubleValue());
					KahunaUtils.trackFlightBooked(Db.getTripBucket().getFlight().getFlightSearch(), orderNumber,
						money.getCurrency(), money.getAmount().doubleValue());
				}
			}
		}
		catch (Exception ex) {
			Log.e("Exception tracking flight checkout", ex);
		}
	}

	public static void trackCarBooked(CarCheckoutResponse carCheckoutResponse) {
		KahunaUtils.trackCarBooked(carCheckoutResponse);
	}

	public static void trackHotelCheckoutStarted() {
		final Rate rate = Db.getTripBucket().getHotel().getRate();
		final Money totalPrice = rate.getTotalAmountAfterTax();
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getProperty();
		AdX.trackHotelCheckoutStarted(params, property, totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		LeanPlumUtils.trackHotelCheckoutStarted(params, property, totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		KahunaUtils.trackHotelCheckoutStarted(params, property, totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
	}

	public static void trackFlightCheckoutStarted() {
		if (Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().getFlightTrip() != null) {
			Money totalPrice = Db.getTripBucket().getFlight().getFlightTrip().getTotalFare();
			AdX.trackFlightCheckoutStarted(Db.getTripBucket().getFlight().getFlightSearch(), totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
			LeanPlumUtils.trackFlightCheckoutStarted(Db.getTripBucket().getFlight().getFlightSearch(), totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
			KahunaUtils.trackFlightCheckoutStarted(Db.getTripBucket().getFlight().getFlightSearch(), totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		}
	}

	public static void trackCarCheckoutStarted(CreateTripCarOffer carOffer) {
		KahunaUtils.trackCarCheckoutStarted(carOffer);
	}

	public static void trackHotelSearch() {
		if (Db.getHotelSearch() != null) {
			AdX.trackHotelSearch(Db.getHotelSearch());
			LeanPlumUtils.trackHotelSearch();
			KahunaUtils.trackHotelSearch();
		}
	}

	public static void trackHotelInfoSite() {
		KahunaUtils.trackHotelInfoSite();
	}

	public static void trackFlightSearch() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			AdX.trackFlightSearch(Db.getFlightSearch());
			LeanPlumUtils.trackFlightSearch();
			KahunaUtils.trackFlightSearch();
		}
	}

	public static void updatePOS() {
		LeanPlumUtils.updatePOS();
		KahunaUtils.updatePOS();
	}

	public static void trackCarSearch(CarSearchParams params) {
		KahunaUtils.trackCarSearch(params);
	}

}
