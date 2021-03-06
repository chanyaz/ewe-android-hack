package com.expedia.bookings.utils;

import java.util.Arrays;
import java.util.Date;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.TripBucketItemFlight;
import com.expedia.bookings.data.TripBucketItemHotel;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.hotels.HotelSearchResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.mobiata.android.Log;
import com.mobileapptracker.MATDeeplinkListener;
import com.mobileapptracker.MATEvent;
import com.mobileapptracker.MATEventItem;
import com.mobileapptracker.MobileAppTracker;

public class TuneUtils {

	public static MobileAppTracker mobileAppTracker = null;
	private static boolean initialized = false;
	public static Context context;

	public static void init(ExpediaBookingApp app) {
		initialized = true;
		context = app.getApplicationContext();

		String advertiserID = app.getString(R.string.tune_sdk_app_advertiser_id);
		String conversionKey = app.getString(R.string.tune_sdk_app_conversion_key);

		mobileAppTracker = MobileAppTracker.init(app, advertiserID, conversionKey);
		mobileAppTracker.setUserId(ADMS_Measurement.sharedInstance(app.getApplicationContext()).getVisitorID());
		mobileAppTracker.setDebugMode(false);
		mobileAppTracker.checkForDeferredDeeplink(new MATDeeplinkListener() {
			@Override
			public void didReceiveDeeplink(String deepLink) {
				Log.d("Deferred deeplink recieved: " + deepLink);
				if (Strings.isNotEmpty(deepLink)) {
					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent);
				}
			}

			@Override
			public void didFailDeeplink(String error) {
				Log.d("Deferred deeplink error: " + error);
			}
		});
		updatePOS();

		MATEvent launchEvent = new MATEvent("Custom_Open")
			.withAttribute1(getTuid())
			.withAttribute3(getMembershipTier())
			.withAttribute2(isUserLoggedIn());
		trackEvent(launchEvent);
	}

	public static void updatePOS() {
		String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		mobileAppTracker.setTwitterUserId(posTpid);

	}

	public static void startTune(Activity activity) {
		if (initialized) {
			// Get source of open for app re-engagement
			mobileAppTracker.setReferralSources(activity);
			// MAT will not function unless the measureSession call is included
			mobileAppTracker.measureSession();

		}
	}

	public static void trackHomePageView() {
		if (initialized) {
			MATEvent event = new MATEvent("home_view");

			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn());
			trackEvent(event);
		}
	}

	public static void trackHotelInfoSite(Property selectedProperty) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_infosite");
			MATEventItem eventItem = new MATEventItem("hotel_infosite_item");
			HotelSearchParams hotelSearchParams = getHotelSearchParams();
			eventItem.withAttribute1(selectedProperty.getLocation().getCity())
					 .withQuantity(getHotelSearchParams().getStayDuration());

			String supplierType = selectedProperty.getSupplierType();

			if (Strings.isEmpty(supplierType)) {
				supplierType = "";
			}

			eventItem.withAttribute2(supplierType);

			withTuidAndMembership(event)
				.withDate1(hotelSearchParams.getCheckInDate().toDate())
				.withDate2(hotelSearchParams.getCheckOutDate().toDate())
				.withEventItems(Arrays.asList(eventItem))
				.withAttribute2(isUserLoggedIn())
				.withQuantity(getHotelSearchParams().getStayDuration())
				.withContentType(selectedProperty.getName())
				.withContentId(selectedProperty.getPropertyId());
			if (selectedProperty.getLowestRate() != null) {
				event.withRevenue(selectedProperty.getLowestRate().getDisplayPrice().getAmount().doubleValue())
					.withCurrencyCode(selectedProperty.getLowestRate().getDisplayPrice().getCurrency());
			}
			trackEvent(event);
		}
	}

	public static void trackHotelV2InfoSite(HotelOffersResponse hotelOffersResponse) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_infosite");
			MATEventItem eventItem = new MATEventItem("hotel_infosite_item");
			final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-mm-dd");
			LocalDate checkInDate = dtf.parseDateTime(hotelOffersResponse.checkInDate).toLocalDate();
			LocalDate checkOutDate = dtf.parseDateTime(hotelOffersResponse.checkOutDate).toLocalDate();
			int stayDuration = JodaUtils.daysBetween(checkInDate, checkOutDate);
			eventItem.withAttribute1(hotelOffersResponse.hotelCity)
				.withQuantity(stayDuration);

			String supplierType = "";
			float lowestPrice = 0.0f;
			String currencyCode = "";

			if (hotelOffersResponse.hotelRoomResponse != null) {
				supplierType = hotelOffersResponse.hotelRoomResponse.get(0).supplierType;
				lowestPrice = hotelOffersResponse.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo.averageRate;
				currencyCode = hotelOffersResponse.hotelRoomResponse.get(0).rateInfo.chargeableRateInfo.currencyCode;
			}
			if (Strings.isEmpty(supplierType)) {
				supplierType = "";
			}

			eventItem.withAttribute2(supplierType);

			withTuidAndMembership(event)
				.withDate1(checkInDate.toDate())
				.withDate2(checkOutDate.toDate())
				.withEventItems(Arrays.asList(eventItem))
				.withAttribute2(isUserLoggedIn())
				.withQuantity(stayDuration)
				.withContentType(hotelOffersResponse.hotelName)
				.withContentId(hotelOffersResponse.hotelId);
			event.withRevenue(lowestPrice)
				.withCurrencyCode(currencyCode);

			trackEvent(event);
		}
	}

	public static void trackHotelCheckoutStarted(Property selectedProperty, String currency, double totalPrice) {
		if (initialized) {
			Rate selectedRate = Db.getTripBucket().getHotel().getRate();
			MATEvent event = new MATEvent("hotel_rate_details");
			MATEventItem eventItem = new MATEventItem("hotel_rate_details_item");
			eventItem.withAttribute1(selectedProperty.getLocation().getCity());
			eventItem.withAttribute3(selectedRate.getRoomDescription());

			Date checkInDate = getHotelSearchParams().getCheckInDate().toDate();
			Date checkOutDate = getHotelSearchParams().getCheckOutDate().toDate();

			withTuidAndMembership(event)
				.withRevenue(totalPrice)
				.withCurrencyCode(currency)
				.withAttribute2(isUserLoggedIn())
				.withContentType(selectedProperty.getName())
				.withContentId(selectedProperty.getPropertyId())
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(checkInDate)
				.withDate2(checkOutDate)
				.withQuantity(getHotelSearchParams().getStayDuration());

			trackEvent(event);
		}
	}

	public static void trackHotelV2CheckoutStarted(HotelCreateTripResponse.HotelProductResponse hotelProductResponse) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_rate_details");
			MATEventItem eventItem = new MATEventItem("hotel_rate_details_item");

			eventItem.withAttribute1(hotelProductResponse.hotelCity);
			eventItem.withAttribute3(hotelProductResponse.hotelRoomResponse.roomTypeDescription);

			LocalDate checkInDate = new LocalDate(hotelProductResponse.checkInDate);
			LocalDate checkOutDate = new LocalDate(hotelProductResponse.checkOutDate);

			int stayDuration = JodaUtils.daysBetween(checkInDate,checkOutDate);
			float totalPrice = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.total;

			String currency = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode;

			withTuidAndMembership(event)
				.withRevenue(totalPrice)
				.withCurrencyCode(currency)
				.withAttribute2(isUserLoggedIn())
				.withContentType(hotelProductResponse.getHotelName())
				.withContentId(hotelProductResponse.hotelId)
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(checkInDate.toDate())
				.withDate2(checkOutDate.toDate())
				.withQuantity(stayDuration);

			trackEvent(event);
		}
	}

	public static void trackHotelSearchResults() {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_search_results");
			MATEventItem eventItem = new MATEventItem("hotel_search_results_item");

			Date checkInDate = getHotelSearchParams().getCheckInDate().toDate();
			Date checkOutDate = getHotelSearchParams().getCheckOutDate().toDate();

			StringBuilder topFiveHotelIdsBuilder = new StringBuilder();
			StringBuilder sb = new StringBuilder();
			int propertiesCount = Db.getHotelSearch().getSearchResponse().getPropertiesCount();
			if (Db.getHotelSearch().getSearchResponse() != null && propertiesCount >= 0) {
				for (int i = 0; (i < 5 && i < propertiesCount); i++) {
					Property property = Db.getHotelSearch().getSearchResponse().getProperties().get(i);
					topFiveHotelIdsBuilder.append(property.getPropertyId());
					String hotelId = property.getPropertyId();
					String hotelName = property.getName();
					String price = "";
					String currency = "";
					if (property.getLowestRate() != null) {
						price = property.getLowestRate().getDisplayPrice().getAmount().toString();
						currency = property.getLowestRate().getDisplayBasePrice().getCurrency();
					}

					String starRating = Double.toString(property.getHotelRating());
					String miles = property.getDistanceFromUser() != null ? Double
						.toString(property.getDistanceFromUser().getDistance()) : "0";
					sb.append(
						String.format("%s|%s|%s|%s|%s|%s", hotelId, hotelName, currency, price, starRating, miles));
					if (i != 4) {
						sb.append(":");
						topFiveHotelIdsBuilder.append(",");
					}
				}
			}
			if (propertiesCount > 0) {
				eventItem
					.withAttribute1(Db.getHotelSearch().getSearchResponse().getProperties().get(0).getLocation().getCity());
			}
			eventItem.withAttribute4(topFiveHotelIdsBuilder.toString());
			eventItem.withAttribute5(sb.toString());

			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withDate1(checkInDate)
				.withDate2(checkOutDate)
				.withEventItems(Arrays.asList(eventItem))
				.withSearchString("hotel")
				.withLevel(1);

			trackEvent(event);
		}
	}

	public static void trackHotelV2SearchResults(com.expedia.bookings.data.hotels.HotelSearchParams searchParams,
		HotelSearchResponse searchResponse) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_search_results");
			MATEventItem eventItem = new MATEventItem("hotel_search_results_item");

			Date checkInDate = searchParams.getCheckIn().toDate();
			Date checkOutDate = searchParams.getCheckOut().toDate();

			StringBuilder topFiveHotelIdsBuilder = new StringBuilder();
			StringBuilder sb = new StringBuilder();
			int hotelsCount = searchResponse.hotelList.size();
			if (searchResponse.hotelList != null && hotelsCount >= 0) {
				for (int i = 0; (i < 5 && i < hotelsCount); i++) {
					Hotel hotel = searchResponse.hotelList.get(i);
					topFiveHotelIdsBuilder.append(hotel.hotelId);
					String hotelId = hotel.hotelId;
					String hotelName = hotel.localizedName;
					String price = "";
					String currency = "";

					if (hotel.lowRateInfo != null) {
						price = hotel.lowRateInfo.total + "";
						currency = hotel.lowRateInfo.currencyCode;
					}

					String starRating = Double.toString(hotel.hotelStarRating);

					String miles = hotel.proximityDistanceInMiles != 0 ? Double
						.toString(hotel.proximityDistanceInMiles) : "0";
					sb.append(
						String.format("%s|%s|%s|%s|%s|%s", hotelId, hotelName, currency, price, starRating, miles));
					if (i != 4) {
						sb.append(":");
						topFiveHotelIdsBuilder.append(",");
					}
				}
			}
			if (hotelsCount > 0) {
				eventItem.withAttribute1(searchResponse.hotelList.get(0).city);
			}
			eventItem.withAttribute4(topFiveHotelIdsBuilder.toString());
			eventItem.withAttribute5(sb.toString());

			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withDate1(checkInDate)
				.withDate2(checkOutDate)
				.withEventItems(Arrays.asList(eventItem))
				.withSearchString("hotel")
				.withLevel(1);

			trackEvent(event);
		}
	}


	public static void trackHotelConfirmation(double revenue, double nightlyRate, String transactionId, String currency, TripBucketItemHotel hotel) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_confirmation");
			MATEventItem eventItem = new MATEventItem("hotel_confirmation_item");

			int stayDuration = hotel.getHotelSearchParams().getStayDuration();
			eventItem.withQuantity(stayDuration)
				.withAttribute1(hotel.getProperty().getLocation().getCity())
				.withUnitPrice(nightlyRate)
				.withRevenue(revenue);

			Date checkInDate = hotel.getHotelSearchParams().getCheckInDate().toDate();
			Date checkOutDate = hotel.getHotelSearchParams().getCheckOutDate().toDate();

			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(revenue)
				.withCurrencyCode(currency)
				.withAdvertiserRefId(transactionId)
				.withQuantity(stayDuration)
				.withContentType(hotel.getProperty().getName())
				.withContentId(hotel.getProperty().getPropertyId())
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(checkInDate)
				.withDate2(checkOutDate);

			trackEvent(event);
		}
	}

	public static void trackHotelV2Confirmation(HotelCheckoutResponse hotelCheckoutResponse) {
		if (initialized) {
			MATEvent event = new MATEvent("hotel_confirmation");
			MATEventItem eventItem = new MATEventItem("hotel_confirmation_item");

			LocalDate checkInDate = new LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkInDate);
			LocalDate checkOutDate = new LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkOutDate);
			int stayDuration = JodaUtils.daysBetween(checkInDate, checkOutDate);
			double revenue = Double.parseDouble(hotelCheckoutResponse.totalCharges);
			float nightlyRate = hotelCheckoutResponse.checkoutResponse.productResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.averageRate;

			eventItem.withQuantity(stayDuration)
				.withAttribute1(hotelCheckoutResponse.checkoutResponse.productResponse.hotelCity)
				.withUnitPrice(nightlyRate)
				.withRevenue(revenue);


			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(revenue)
				.withCurrencyCode(hotelCheckoutResponse.currencyCode)
				.withAdvertiserRefId(hotelCheckoutResponse.orderId)
				.withQuantity(stayDuration)
				.withContentType(hotelCheckoutResponse.checkoutResponse.productResponse.getHotelName())
				.withContentId(hotelCheckoutResponse.checkoutResponse.productResponse.hotelId)
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(checkInDate.toDate())
				.withDate2(checkOutDate.toDate());

			trackEvent(event);
		}
	}


	public static void trackFlightRateDetailOverview() {
		if (initialized) {
			FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
			FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

			MATEvent event = new MATEvent("flight_rate_details");
			MATEventItem eventItem = new MATEventItem("flight_rate_details_item");
			eventItem.withQuantity(trip.getPassengerCount())
				.withAttribute2(searchParams.getDepartureLocation().getDestinationId())
				.withAttribute3(searchParams.getArrivalLocation().getDestinationId())
				.withAttribute4(trip.getLegs().get(0).getFirstAirlineCode());

			Date departureDate = searchParams.getDepartureDate().toDate();
			if (searchParams.isRoundTrip()) {
				Date returnDate = searchParams.getReturnDate().toDate();
				event.withDate2(returnDate);
			}
			withTuidAndMembership(event)
				.withRevenue(trip.getTotalFare().getAmount().doubleValue())
				.withCurrencyCode(trip.getTotalFare().getCurrency())
				.withAttribute2(isUserLoggedIn())
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(departureDate);


			trackEvent(event);
		}
	}

	public static void trackPageLoadFlightSearchResults(int legPosition) {
		if (legPosition == 0) {
			trackFlightOutBoundResults();
		}
		else if (legPosition == 1) {
			trackFlightInBoundResults();
		}
	}

	public static void trackFlightOutBoundResults() {
		if (initialized) {
			FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();
			MATEvent event = new MATEvent("flight_outbound_result");
			MATEventItem eventItem = new MATEventItem("flight_outbound_result_item");
			eventItem.withAttribute2(searchParams.getDepartureLocation().getDestinationId())
				.withAttribute3(searchParams.getArrivalLocation().getDestinationId());

			FlightSearchResponse response = Db.getFlightSearch().getSearchResponse();
			if (response != null) {
				int propertiesCount = response.getTripCount();
				StringBuilder sb = new StringBuilder();
				if (propertiesCount >= 0) {
					for (int i = 0; (i < 5 && i < propertiesCount); i++) {
						FlightTrip trip = response.getTrips().get(i);
						String carrier = trip.getLegs().get(0).getFirstAirlineCode();
						String currency = trip.getTotalFare().getCurrency();
						String price = trip.getTotalFare().amount.toString();
						String routeType = searchParams.isRoundTrip() ? "RT" : "OW";
						String route = String.format("%s-%s", searchParams.getDepartureLocation().getDestinationId(),
							searchParams.getArrivalLocation().getDestinationId());

						sb.append(
							String.format("%s|%s|%s|%s|%s", carrier, currency, price, routeType, route));
						if (i != 4) {
							sb.append(":");
						}
					}
				}
				eventItem.withAttribute5(sb.toString());
			}
			Date departureDate = searchParams.getDepartureDate().toDate();
			if (searchParams.isRoundTrip()) {
				Date returnDate = searchParams.getReturnDate().toDate();
				event.withDate2(returnDate);
			}
			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withEventItems(Arrays.asList(eventItem))
				.withSearchString("flight")
				.withDate1(departureDate);

			trackEvent(event);
		}

	}

	public static void trackFlightInBoundResults() {
		if (initialized) {
			FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();
			MATEvent event = new MATEvent("flight_inbound_result");
			MATEventItem eventItem = new MATEventItem("flight_inbound_result_item");
			eventItem.withAttribute2(searchParams.getArrivalLocation().getDestinationId())
				.withAttribute3(searchParams.getDepartureLocation().getDestinationId());

			FlightSearchResponse response = Db.getFlightSearch().getSearchResponse();
			if (response != null) {
				int propertiesCount = response.getTripCount();
				StringBuilder sb = new StringBuilder();
				if (propertiesCount >= 0) {
					for (int i = 0; (i < 5 && i < propertiesCount); i++) {
						FlightTrip trip = response.getTrips().get(i);
						String carrier = trip.getLegs().get(1).getFirstAirlineCode();
						String currency = trip.getTotalFare().getCurrency();
						String price = trip.getTotalFare().amount.toString();
						String routeType = searchParams.isRoundTrip() ? "RT" : "OW";
						String route = String.format("%s-%s", searchParams.getArrivalLocation().getDestinationId(),
							searchParams.getDepartureLocation().getDestinationId());

						sb.append(
							String.format("%s|%s|%s|%s|%s", carrier, currency, price, routeType, route));
						if (i != 4) {
							sb.append(":");
						}
					}
				}
				eventItem.withAttribute5(sb.toString());
			}
			Date departureDate = searchParams.getDepartureDate().toDate();
			if (searchParams.isRoundTrip()) {
				Date returnDate = searchParams.getReturnDate().toDate();
				event.withDate2(returnDate);
			}
			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withEventItems(Arrays.asList(eventItem))
				.withSearchString("flight")
				.withDate1(departureDate);

			trackEvent(event);
		}
	}

	public static void trackFlightBooked(TripBucketItemFlight tripBucketItemFlight, String orderId, String currency,
		double totalPrice, double averagePrice) {
		if (initialized) {
			MATEvent event = new MATEvent("flight_confirmation");
			MATEventItem eventItem = new MATEventItem("flight_confirmation_item");
			eventItem.withQuantity(tripBucketItemFlight.getFlightSearchParams().getNumTravelers())
				.withRevenue(totalPrice)
				.withUnitPrice(averagePrice)
				.withAttribute2(tripBucketItemFlight.getFlightSearchParams().getDepartureLocation().getDestinationId())
				.withAttribute3(tripBucketItemFlight.getFlightSearchParams().getArrivalLocation().getDestinationId())
				.withAttribute4(tripBucketItemFlight.getFlightTrip().getLeg(0).getFirstAirlineCode());

			Date departureDate = tripBucketItemFlight.getFlightTrip().getLeg(0).getFirstWaypoint().getBestSearchDateTime().toDate();
			if (tripBucketItemFlight.getFlightSearchParams().isRoundTrip()) {
				Date returnDate = tripBucketItemFlight.getFlightTrip().getLeg(1).getLastWaypoint().getBestSearchDateTime().toDate();
				event.withDate2(returnDate);
			}
			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(totalPrice)
				.withCurrencyCode(currency)
				.withQuantity(tripBucketItemFlight.getFlightSearchParams().getNumTravelers())
				.withAdvertiserRefId(orderId)
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(departureDate);

			trackEvent(event);
		}

	}

	public static void trackCarSearch(CarSearch search, CarSearchParams params) {
		if (initialized) {
			MATEvent event = new MATEvent("car_result");
			MATEventItem eventItem = new MATEventItem("car_result_item");
			eventItem.withAttribute2(params.origin)
				.withAttribute3(params.origin);

			if (search != null) {
				int propertiesCount = search.categories.size();
				StringBuilder sb = new StringBuilder();
				StringBuilder carTopFiveClass = new StringBuilder();
				if (propertiesCount >= 0) {
					for (int i = 0; (i < 5 && i < propertiesCount); i++) {
						CategorizedCarOffers carOffer = search.categories.get(i);
						String carClass = carOffer.carCategoryDisplayLabel;
						String currency = carOffer.getLowestTotalPriceOffer().fare.total.getCurrency();
						String price = carOffer.getLowestTotalPriceOffer().fare.total.amount.toString();
						carTopFiveClass.append(carClass);
						sb.append(String.format("%s|%s|%s", carClass, currency, price));
						if (i != 4) {
							sb.append(":");
							carTopFiveClass.append(",");
						}
					}
				}
				eventItem.withAttribute5(sb.toString());
				eventItem.withAttribute4(carTopFiveClass.toString());
			}


			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withEventItems(Arrays.asList(eventItem))
				.withDate2(params.startDateTime.toDate())
				.withSearchString("car")
				.withDate1(params.endDateTime.toDate());

			trackEvent(event);
		}
	}

	public static void trackCarRateDetails(CreateTripCarOffer carOffer) {
		if (initialized) {
			MATEvent event = new MATEvent("car_rate_details");
			MATEventItem eventItem = new MATEventItem("car_rate_details_item");
			eventItem.withAttribute2(carOffer.pickUpLocation.locationCode)
				.withAttribute3(carOffer.dropOffLocation.locationCode)
				.withAttribute4(carOffer.vehicleInfo.carCategoryDisplayLabel)
				.withAttribute5(carOffer.vendor.name);


			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(carOffer.detailedFare.grandTotal.getAmount().doubleValue())
				.withCurrencyCode(carOffer.detailedFare.grandTotal.getCurrency())
				.withEventItems(Arrays.asList(eventItem))
				.withDate2(carOffer.getDropOffTime().toDate())
				.withDate1(carOffer.getPickupTime().toDate());

			trackEvent(event);
		}
	}

	public static void trackCarConfirmation(CarCheckoutResponse carCheckoutResponse) {
		if (initialized) {
			MATEvent event = new MATEvent("car_confirmation");
			MATEventItem eventItem = new MATEventItem("car_confirmation_item");

			CreateTripCarOffer carOffer = carCheckoutResponse.newCarProduct;
			eventItem.withQuantity(1)
				.withRevenue(carCheckoutResponse.totalChargesPrice.getAmount().doubleValue())
				.withAttribute2(carOffer.pickUpLocation.locationCode)
				.withAttribute3(carOffer.dropOffLocation.locationCode)
				.withAttribute4(carOffer.vehicleInfo.carCategoryDisplayLabel)
				.withAttribute5(carOffer.vendor.name);


			Date pickupTime = carOffer.getPickupTime().toDate();
			Date dropOffTime = carOffer.getDropOffTime().toDate();
			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(carOffer.detailedFare.grandTotal.getAmount().doubleValue())
				.withCurrencyCode(carOffer.detailedFare.grandTotal.getCurrency())
				.withAdvertiserRefId(carCheckoutResponse.orderId)
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(pickupTime)
				.withDate2(dropOffTime);

			trackEvent(event);
		}
	}

	public static void trackLXSearch(LXSearchParams searchParams, LXSearchResponse searchResponse) {
		if (initialized) {
			MATEvent event = new MATEvent("lx_search");
			MATEventItem eventItem = new MATEventItem("lx_search_item");

			eventItem.withAttribute2(searchParams.location);
			if (searchResponse != null) {
				int activitiesCount = searchResponse.activities.size();
				StringBuilder sb = new StringBuilder();
				StringBuilder topFiveActivities = new StringBuilder();
				if (activitiesCount >= 0) {
					for (int i = 0; (i < 5 && i < activitiesCount); i++) {
						LXActivity activity = searchResponse.activities.get(i);
						String title = activity.title;
						String currency = activity.price.currencyCode;
						double price = activity.price.amount.doubleValue();
						topFiveActivities.append(title);
						sb.append(String.format("%s|%s|%f", title, currency, price));
						if (i != 4) {
							sb.append(":");
							topFiveActivities.append(",");
						}
					}
				}
				eventItem.withAttribute5(sb.toString());
				eventItem.withAttribute4(topFiveActivities.toString());
			}

			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(searchParams.startDate.toDate())
				.withSearchString("lx");

			trackEvent(event);
		}
	}

	public static void trackLXDetails(String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate,
		int selectedTicketCount, String lxActivityTitle) {
		if (initialized) {
			MATEvent event = new MATEvent("lx_details");
			MATEventItem eventItem = new MATEventItem("lx_details_item").withAttribute2(lxActivityLocation)
				.withAttribute3(lxActivityTitle);

			withTuidAndMembership(event)
				.withQuantity(selectedTicketCount)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(totalPrice.getAmount().doubleValue())
				.withCurrencyCode(totalPrice.getCurrency())
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(DateUtils
					.yyyyMMddHHmmssToLocalDate(lxOfferSelectedDate)
					.toDate());

			trackEvent(event);
		}
	}

	public static void trackLXConfirmation(String lxActivityLocation, Money totalPrice, Money ticketPrice,
		String lxActivityStartDate,
		String orderId, String lxActivityTitle, int selectedTicketCount, int selectedChildTicketCount) {
		if (initialized) {
			MATEvent event = new MATEvent("lx_confirmation");
			MATEventItem eventItem = new MATEventItem("lx_confirmation_item");
			double revenue = totalPrice.getAmount().doubleValue();
			double ticketPriceAmt = ticketPrice.getAmount().doubleValue();

			eventItem.withQuantity(selectedTicketCount + selectedChildTicketCount)
				.withRevenue(revenue)
				.withUnitPrice(ticketPriceAmt)
				.withAttribute2(lxActivityLocation)
				.withAttribute3(lxActivityTitle);

			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(revenue)
				.withQuantity(1)
				.withCurrencyCode(totalPrice.getCurrency())
				.withAdvertiserRefId(orderId)
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(DateUtils
					.yyyyMMddHHmmssToLocalDate(lxActivityStartDate)
					.toDate());

			trackEvent(event);
		}
	}

	private static void trackEvent(MATEvent eventName) {
		if (initialized) {
			mobileAppTracker.measureEvent(eventName);
		}
	}

	public static void trackLogin() {
		if (initialized) {
			MATEvent loginEvent = new MATEvent("login");
			loginEvent.withAttribute1(getTuid());
			loginEvent.withAttribute2(getMembershipTier());
			trackEvent(loginEvent);
		}
	}

	//////////
	// Helpers

	private static String getMembershipTier() {
		if (User.isLoggedIn(context)) {
			lazyLoadUser();
			return User.getLoggedInLoyaltyMembershipTier(context).toString();
		}
		return "";
	}

	private static String getTuid() {
		if (User.isLoggedIn(context)) {
			lazyLoadUser();
			return Db.getUser().getTuidString();
		}
		return "";
	}

	private static void lazyLoadUser() {
		if (Db.getUser() == null && User.isLoggedIn(context)) {
			Db.loadUser(context);
		}
	}

	private static HotelSearchParams getHotelSearchParams() {
		return Db.getHotelSearch().getSearchParams();
	}

	private static MATEvent withTuidAndMembership(MATEvent event) {
		return event.withAttribute1(getTuid())
			.withAttribute3(getMembershipTier());
	}

	private static String isUserLoggedIn() {
		return User.isLoggedIn(context) ? "1" : "0";
	}
	
}
