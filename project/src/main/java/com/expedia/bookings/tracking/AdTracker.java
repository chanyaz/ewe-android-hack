package com.expedia.bookings.tracking;

import java.util.List;

import org.joda.time.LocalDate;

import android.content.Context;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.LeanPlumUtils;
import com.expedia.bookings.utils.TuneUtils;
import com.facebook.AppEventsLogger;
import com.mobiata.android.Log;

public class AdTracker {
	public static void init(Context context) {

		if (ProductFlavorFeatureConfiguration.getInstance().isFacebookTrackingEnabled()) {
			TrackingPackage.setContext(context);
			TrackingPackage.setFacebookLogger(AppEventsLogger.newLogger(context));
		}
	}

	public static void trackFirstLaunch() {
		// Other
		AdX.trackFirstLaunch();
		LeanPlumUtils.tracking("FirstLaunch");
	}

	public static void trackLaunch() {
		// Other
		AdX.trackLaunch();
		LeanPlumUtils.tracking("Launch");
	}

	public static void trackLogin() {
		// Other
		AdX.trackLogin();
		LeanPlumUtils.tracking("Sign In Success");
		TuneUtils.trackLogin();
	}

	public static void trackLogout() {
		LeanPlumUtils.updateLoggedInStatus();
	}

	public static void trackSignInUpStarted() {
		LeanPlumUtils.tracking("Sign In Start");
	}

	public static void trackAccountCreated() {
		LeanPlumUtils.tracking("Account Creation Success");
	}

	public static void trackViewHomepage() {
		AdX.trackViewHomepage();
		TuneUtils.trackHomePageView();
	}

	public static void trackViewItinList() {
		AdX.trackViewItinList();
		LeanPlumUtils.tracking("Itinerary");
	}

	public static void trackViewItinExpanded() {
		LeanPlumUtils.tracking("Expand Itinerary");
	}

	public static void trackHotelBooked() {
		// Values
		final Rate rate = Db.getTripBucket().getHotel().getRate();

		final String currency = rate.getDisplayPrice().getCurrency();
		final Double displayPrice = rate.getDisplayPrice().getAmount().doubleValue();
		final Double totalPrice = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		final Double pricePerNight = Db.getTripBucket().getHotel().getRate().getNightlyRateTotal().getAmount().doubleValue();

		// Other
		HotelBookingResponse response = Db.getTripBucket().getHotel().getBookingResponse();
		String orderNumber = response != null ? response.getOrderNumber() : "";
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getProperty();
		AdX.trackHotelBooked(params, property, orderNumber, currency, totalPrice, displayPrice);
		LeanPlumUtils.trackHotelBooked(params, property, orderNumber, currency, totalPrice, displayPrice);
		TuneUtils.trackHotelConfirmation(totalPrice, pricePerNight, orderNumber, currency, Db.getTripBucket().getHotel());
		new FacebookEvents().trackHotelConfirmation(Db.getTripBucket().getHotel(), rate);
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
					LeanPlumUtils.trackFlightBooked(Db.getTripBucket().getFlight(), orderNumber,
						money.getCurrency(), money.getAmount().doubleValue());
					TuneUtils.trackFlightBooked(Db.getTripBucket().getFlight(), orderNumber,
						money.getCurrency(), money.getAmount().doubleValue());
					new FacebookEvents().trackFlightConfirmation(Db.getTripBucket().getFlight());
				}
			}
		}
		catch (Exception ex) {
			Log.e("Exception tracking flight checkout", ex);
		}
	}

	public static void trackCarBooked(CarCheckoutResponse carCheckoutResponse) {
		LeanPlumUtils.trackCarBooked(carCheckoutResponse);
		TuneUtils.trackCarConfirmation(carCheckoutResponse);
		new FacebookEvents().trackCarConfirmation(carCheckoutResponse);
	}

	public static void trackHotelCheckoutStarted() {
		final Rate rate = Db.getTripBucket().getHotel().getRate();
		final Money totalPrice = rate.getTotalAmountAfterTax();
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getProperty();
		AdX.trackHotelCheckoutStarted(params, property, totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		LeanPlumUtils.trackHotelCheckoutStarted(params, property, totalPrice.getCurrency(),
			totalPrice.getAmount().doubleValue());
		TuneUtils.trackHotelCheckoutStarted(property, totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		new FacebookEvents().trackHotelCheckout(Db.getTripBucket().getHotel(), rate);
	}

	public static void trackFlightCheckoutStarted() {
		if (Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().getFlightTrip() != null) {
			Money totalPrice = Db.getTripBucket().getFlight().getFlightTrip().getTotalFare();
			AdX.trackFlightCheckoutStarted(Db.getTripBucket().getFlight().getFlightSearch(), totalPrice.getCurrency(),
				totalPrice.getAmount().doubleValue());
			LeanPlumUtils
				.trackFlightCheckoutStarted(Db.getTripBucket().getFlight().getFlightSearch(), totalPrice.getCurrency(),
					totalPrice.getAmount().doubleValue());
			new FacebookEvents().trackFlightCheckout(Db.getTripBucket().getFlight());
		}
	}

	public static void trackLXSearch(LXSearchParams lxSearchParams) {
		LeanPlumUtils.trackLxSearch(lxSearchParams);
	}

	public static void trackLXSearchResults(LXSearchParams searchParams, LXSearchResponse searchResponse) {
		TuneUtils.trackLXSearch(searchParams, searchResponse);
		new FacebookEvents().trackLXSearch(searchParams, searchResponse);
	}

	public static void trackFilteredLXSearchResults(LXSearchParams searchParams, LXSearchResponse searchResponse) {
		new FacebookEvents().trackLXSearch(searchParams, searchResponse);
	}

	public static void trackLXDetails(String activityId, String destination, LocalDate startDate, String regionId,
		String currencyCode, String activityValue) {
		new FacebookEvents().trackLXDetail(activityId, destination, startDate, regionId, currencyCode, activityValue);
	}

	public static void trackLXCheckoutStarted(String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate,
		List<String> lxActivityCategories, int selectedTicketCount, String lxActivityTitle, String regionId,
		String activityId, LocalDate startDate, int selectedChildTicketCount) {
		LeanPlumUtils.trackLXCheckoutStarted(lxActivityLocation, totalPrice, lxOfferSelectedDate, lxActivityCategories);
		TuneUtils
			.trackLXDetails(lxActivityLocation, totalPrice, lxOfferSelectedDate, selectedTicketCount, lxActivityTitle);
		new FacebookEvents()
			.trackLXCheckout(activityId, lxActivityLocation, startDate, regionId, totalPrice, selectedTicketCount,
				selectedChildTicketCount);
	}

	public static void trackCarCheckoutStarted(CreateTripCarOffer carOffer) {
		LeanPlumUtils.trackCarCheckoutStarted(carOffer);
		TuneUtils.trackCarRateDetails(carOffer);
		new FacebookEvents().trackCarCheckout(carOffer);
	}

	public static void trackHotelSearch() {
		if (Db.getHotelSearch() != null) {
			AdX.trackHotelSearch(Db.getHotelSearch());
			LeanPlumUtils.trackHotelSearch();
			TuneUtils.trackHotelSearchResults();
			new FacebookEvents().trackHotelSearch(Db.getHotelSearch());
		}
	}

	public static void trackFilteredHotelSearch() {
		if (Db.getHotelSearch() != null) {
			new FacebookEvents().trackHotelSearch(Db.getHotelSearch());
		}
	}

	public static void trackHotelInfoSite() {
		TuneUtils.trackHotelInfoSite(Db.getHotelSearch().getSelectedProperty());
		new FacebookEvents().trackHotelInfosite(Db.getHotelSearch());
	}

	public static void trackFlightSearch() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			AdX.trackFlightSearch(Db.getFlightSearch());
			new FacebookEvents().trackFlightSearch(Db.getFlightSearch());
			LeanPlumUtils.trackFlightSearch();
		}
	}

	public static void trackFilteredFlightSearch(int legNumber) {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			new FacebookEvents().trackFilteredFlightSearch(Db.getFlightSearch(), legNumber);
		}
	}

	public static void trackPageLoadFlightSearchResults(int legPosition) {
		TuneUtils.trackPageLoadFlightSearchResults(legPosition);
	}

	public static void trackFlightRateDetailOverview() {
		TuneUtils.trackFlightRateDetailOverview();
		new FacebookEvents().trackFlightDetail(Db.getFlightSearch());
	}

	public static void trackTabletFlightViewContent() {
		new FacebookEvents().trackFlightDetail(Db.getFlightSearch());
	}

	public static void updatePOS() {
		LeanPlumUtils.updatePOS();
	}

	public static void trackCarSearch(CarSearchParams params) {
		LeanPlumUtils.trackCarSearch(params);
	}

	public static void trackCarResult(CarSearch search, CarSearchParams params) {
		TuneUtils.trackCarSearch(search, params);
		new FacebookEvents().trackCarSearch(params, search);
	}

	public static void trackFilteredCarResult(CarSearch search, CarSearchParams params) {
		new FacebookEvents().trackCarSearch(params, search);
	}

	public static void trackCarDetails(CarSearchParams searchParams, SearchCarOffer searchCarOffer) {
		new FacebookEvents().trackCarDetail(searchParams, searchCarOffer);
	}

	public static void trackLXBooked(String lxActivityLocation, Money totalPrice, String lxActivityStartDate,
		List<String> lxActivityCategories, String orderId, String lxActivityTitle, String activityId,
		LocalDate startDate, String regionId, int selectedTicketCount, int selectedChildTicketCount) {
		LeanPlumUtils.trackLXBooked(lxActivityLocation, totalPrice, lxActivityStartDate, lxActivityCategories);
		TuneUtils.trackLXConfirmation(lxActivityLocation, totalPrice, lxActivityStartDate,
			orderId, lxActivityTitle);
		new FacebookEvents().trackLXConfirmation(activityId, lxActivityLocation, startDate, regionId, totalPrice,
			selectedTicketCount, selectedChildTicketCount);
	}
}
