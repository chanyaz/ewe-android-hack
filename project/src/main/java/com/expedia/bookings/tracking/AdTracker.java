package com.expedia.bookings.tracking;

import java.util.List;

import org.joda.time.LocalDate;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelBookingResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.utils.TuneUtils;
import com.mobiata.android.Log;

public class AdTracker {
	public static void trackLogin() {
		// Other
		TuneUtils.trackLogin();
	}

	public static void trackHotelBooked(String couponcode) {
		// Values
		final Rate rate = Db.getTripBucket().getHotel().getRate();

		final String currency = rate.getDisplayPrice().getCurrency();
		final Double displayPrice = rate.getDisplayPrice().getAmount().doubleValue();
		final Double totalPrice = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		final Double pricePerNight = rate.getNightlyRateTotal().getAmount().doubleValue();

		// Other
		HotelBookingResponse response = Db.getTripBucket().getHotel().getBookingResponse();
		String orderNumber = response != null ? response.getOrderNumber() : "";
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getProperty();
		TuneUtils.trackHotelConfirmation(totalPrice, pricePerNight, orderNumber, currency, Db.getTripBucket().getHotel());
		new FacebookEvents().trackHotelConfirmation(Db.getTripBucket().getHotel(), rate);
	}

	public static void trackFlightBooked() {
		try {
			if (Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().getFlightTrip() != null) {
				FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
				Money totalFare = trip.getTotalPrice();
				Money averageTotalFare = trip.getAverageTotalFare();
				if (totalFare != null) {
					String orderNumber = Db.getTripBucket().getFlight().getCheckoutResponse() != null ?
						Db.getTripBucket().getFlight().getCheckoutResponse().getOrderId() : "";
					TuneUtils.trackFlightBooked(Db.getTripBucket().getFlight(), orderNumber,
						totalFare.getCurrency(), totalFare.getAmount().doubleValue(), averageTotalFare.getAmount().doubleValue());
					new FacebookEvents().trackFlightConfirmation(Db.getTripBucket().getFlight());
				}
			}
		}
		catch (Exception ex) {
			Log.e("Exception tracking flight checkout", ex);
		}
	}

	public static void trackCarBooked(CarCheckoutResponse carCheckoutResponse) {
		TuneUtils.trackCarConfirmation(carCheckoutResponse);
		new FacebookEvents().trackCarConfirmation(carCheckoutResponse);
	}

	public static void trackHotelCheckoutStarted() {
		final Rate rate = Db.getTripBucket().getHotel().getRate();
		final Money totalPrice = rate.getTotalAmountAfterTax();
		HotelSearchParams params = Db.getTripBucket().getHotel().getHotelSearchParams();
		Property property = Db.getTripBucket().getHotel().getProperty();
		TuneUtils.trackHotelCheckoutStarted(property, totalPrice.getCurrency(), totalPrice.getAmount().doubleValue());
		new FacebookEvents().trackHotelCheckout(Db.getTripBucket().getHotel(), rate);
	}

	public static void trackFlightCheckoutStarted() {
		if (Db.getTripBucket().getFlight() != null && Db.getTripBucket().getFlight().getFlightTrip() != null) {
			new FacebookEvents().trackFlightCheckout(Db.getTripBucket().getFlight());
		}
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
		List<String> lxActivityCategories, int selectedTicketCount, String lxActivityTitle, String regionId,
		String activityId, LocalDate startDate, int selectedChildTicketCount) {
		TuneUtils
			.trackLXDetails(lxActivityLocation, totalPrice, lxOfferSelectedDate, selectedTicketCount, lxActivityTitle);
		new FacebookEvents()
			.trackLXCheckout(activityId, lxActivityLocation, startDate, regionId, totalPrice, selectedTicketCount,
				selectedChildTicketCount);
	}

	public static void trackCarCheckoutStarted(CreateTripCarOffer carOffer) {
		TuneUtils.trackCarRateDetails(carOffer);
		new FacebookEvents().trackCarCheckout(carOffer);
	}

	public static void trackHotelSearch() {
		if (Db.getHotelSearch() != null) {
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
		new FacebookEvents().trackHotelInfoSite(Db.getHotelSearch());
	}

	public static void trackFlightSearch() {
		if (Db.getFlightSearch() != null && Db.getFlightSearch().getSearchParams() != null) {
			new FacebookEvents().trackFlightSearch(Db.getFlightSearch());
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
		TuneUtils.updatePOS();
	}

	public static void trackCarResult(CarSearch search, CarSearchParam params) {
		TuneUtils.trackCarSearch(search, params);
		new FacebookEvents().trackCarSearch(params, search);
	}

	public static void trackFilteredCarResult(CarSearch search, CarSearchParam params) {
		new FacebookEvents().trackCarSearch(params, search);
	}

	public static void trackCarDetails(CarSearchParam searchParams, SearchCarOffer searchCarOffer) {
		new FacebookEvents().trackCarDetail(searchParams, searchCarOffer);
	}

	public static void trackLXBooked(String lxActivityLocation, Money totalPrice, Money ticketPrice,
		String lxActivityStartDate,
		List<String> lxActivityCategories, LXCheckoutResponse checkoutResponse, String lxActivityTitle, String activityId,
		LocalDate startDate, String regionId, int selectedTicketCount, int selectedChildTicketCount) {
		TuneUtils.trackLXConfirmation(lxActivityLocation, totalPrice, ticketPrice, lxActivityStartDate,
			checkoutResponse, lxActivityTitle, selectedTicketCount, selectedChildTicketCount);
		new FacebookEvents().trackLXConfirmation(activityId, lxActivityLocation, startDate, regionId, totalPrice,
			selectedTicketCount, selectedChildTicketCount);
	}
}
