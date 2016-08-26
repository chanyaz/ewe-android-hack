package com.expedia.bookings.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarLocation;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.cars.CreateTripCarFare;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.flights.FlightCheckoutResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelSearchResponse;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.notification.PushNotificationUtils;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.leanplum.Leanplum;
import com.leanplum.LeanplumActivityHelper;
import com.leanplum.LeanplumPushNotificationCustomizer;
import com.leanplum.LeanplumPushService;
import com.leanplum.annotations.Parser;
import com.leanplum.callbacks.VariablesChangedCallback;
import com.mobiata.android.Log;

public class LeanPlumUtils {
	public static Map<String, Object> userAtrributes = new HashMap<String, Object>();
	public static final String CAMPAIGN_TEXT_KEY = "campaignText";
	public static final String DEFAULT_CAMPAIGN_TEXT = "leanplum.notification";
	public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZZ";
	private static Context context;
	private static boolean initialized = false;

	public static void init(ExpediaBookingApp app) {
		initialized = true;
		context = app.getApplicationContext();
		Leanplum.setIsTestModeEnabled(ExpediaBookingApp.isAutomation());
		if (BuildConfig.DEBUG) {
			String appId = context.getString(R.string.lean_plum_sdk_dev_appid);
			String key = context.getString(R.string.lean_plum_sdk_dev_key);
			Leanplum.setAppIdForDevelopmentMode(appId, key);
		}
		else {
			String appId = context.getString(R.string.lean_plum_sdk_prod_appid);
			String key = context.getString(R.string.lean_plum_sdk_prod_key);
			Leanplum.setAppIdForProductionMode(appId, key);
		}
		String localeIdentifier = PointOfSale.getPointOfSale().getLocaleIdentifier();
		userAtrributes.put("PosLocale", localeIdentifier);

		String deviceLocale = Locale.getDefault().toString();
		userAtrributes.put("DeviceLocale", deviceLocale);

		String countryCode = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
		userAtrributes.put("CountryCode", countryCode);

		String deviceType = ExpediaBookingApp.useTabletInterface(context) ? "Tablet" : "Phone";
		userAtrributes.put("DeviceType", deviceType);

		LeanplumPushService.setGcmSenderId(PushNotificationUtils.SENDER_ID);
		LeanplumPushService.setCustomizer(new LeanplumPushNotificationCustomizer() {
			@Override
			public void customize(NotificationCompat.Builder builder, Bundle bundle) {
				String campaignText = bundle.getString(CAMPAIGN_TEXT_KEY, DEFAULT_CAMPAIGN_TEXT);
				OmnitureTracking.trackLeanPlumNotification(campaignText);
				builder.setSmallIcon(ProductFlavorFeatureConfiguration.getInstance().getNotificationIconResourceId());
			}
		});

		Leanplum.setApplicationContext(context);
		LeanplumActivityHelper.enableLifecycleCallbacks(app);
		registerTemplates();
		Leanplum.start(context, userAtrributes);
		updateLoggedInStatus();
		Parser.parseVariablesForClasses(LeanPlumFlags.class);
		Leanplum.addVariablesChangedHandler(flightShareCallback);
	}

	public static void registerTemplates() {
		LeanPlumTemplate.register(context);
		GTLeanPlumTemplate.register(context);
	}

	public static void updatePOS() {
		if (initialized) {
			PointOfSale pos = PointOfSale.getPointOfSale();
			userAtrributes.put("PosLocale", pos.getLocaleIdentifier());
			userAtrributes.put("CountryCode", pos.getTwoLetterCountryCode());

			String deviceLocale = Locale.getDefault().toString();
			userAtrributes.put("DeviceLocale", deviceLocale);

			boolean isUserAirAttachQualified = Db.getTripBucket() != null &&
				Db.getTripBucket().isUserAirAttachQualified();
			updateAirAttachState(isUserAirAttachQualified);
		}
	}

	public static void updateAirAttachState(boolean userIsAttachEligible) {
		if (initialized) {
			// Air attach state
			userAtrributes.put("airattach_eligible", userIsAttachEligible);
			Leanplum.setUserAttributes(userAtrributes);
		}
	}

	public static void updateLoggedInStatus() {
		if (initialized) {
			boolean isUserLoggedIn = User.isLoggedIn(context);
			userAtrributes.put("isUserLoggedIn", isUserLoggedIn);
			if (isUserLoggedIn) {
				if (Db.getUser() == null) {
					Db.loadUser(context);
				}
				if (Db.getUser().getPrimaryTraveler() != null) {
					userAtrributes.put("first_name",
						Db.getUser().getPrimaryTraveler().getFirstName());
				}
				userAtrributes.put("membershipTier",
					User.getLoggedInLoyaltyMembershipTier(context).toApiValue());
			}
			boolean isUserAirAttachQualified = Db.getTripBucket() != null &&
				Db.getTripBucket().isUserAirAttachQualified();
			updateAirAttachState(isUserAirAttachQualified);
		}
	}

	public static void tracking(String eventName) {
		if (initialized) {
			Leanplum.track(eventName);
			if (eventName.equalsIgnoreCase("Login")) {
				updateLoggedInStatus();
			}
		}
	}

	private static void tracking(String eventName, HashMap eventParams) {
		if (initialized) {
			Leanplum.track(eventName, eventParams);
		}
	}

	public static void trackHotelBooked(HotelSearchParams params, Property property, String orderNumber,
		String currency, double totalPrice, double avgPrice, String couponCode) {
		if (initialized) {
			String eventName = "Sale Hotel";
			Log.i("LeanPlum hotel booking event currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			Location location = property.getLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
				eventParams.put("Destination", location.getCity());
			}
			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(params.getCheckInDate()));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(params.getCheckOutDate()));
			eventParams.put("b_win", "" + getBookingWindow(params.getCheckInDate()));
			eventParams.put("p_type", "HOTEL");
			eventParams.put("hotel_friendly_name", property.getName());
			eventParams.put("PropertyId", property.getPropertyId());
			eventParams.put("AveragePrice", "" + avgPrice);
			eventParams.put("StayDuration", "" + params.getStayDuration());
			eventParams.put("currency", currency);
			eventParams.put("OrderNumber", orderNumber);
			eventParams.put("TotalPrice", String.valueOf(totalPrice));
			eventParams.put("guest_count",params.getNumTravelers());
			if (couponCode != null) {
				eventParams.put("coupon_code", couponCode);
			}
			tracking(eventName, eventParams);
		}
	}

	public static void trackHotelV2Booked(HotelCheckoutResponse hotelCheckoutResponse, int guestCount, String couponCode) {
		if (initialized) {
			String eventName = "Sale Hotel";
			Log.i("LeanPlum hotel booking event currency=" + hotelCheckoutResponse.currencyCode + " total=" + hotelCheckoutResponse.totalCharges);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			addCommonProductRetargeting(eventParams, hotelCheckoutResponse.checkoutResponse.productResponse.hotelCity,
				hotelCheckoutResponse.checkoutResponse.productResponse.hotelStateProvince,
				hotelCheckoutResponse.checkoutResponse.productResponse.hotelCountry);
			eventParams.put("Destination", hotelCheckoutResponse.checkoutResponse.productResponse.hotelCity);

			LocalDate checkInDate = new LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkInDate);
			LocalDate checkOutDate = new LocalDate(hotelCheckoutResponse.checkoutResponse.productResponse.checkOutDate);
			int numNights = JodaUtils.daysBetween(checkInDate, checkOutDate);
			String avgPrice =
				hotelCheckoutResponse.checkoutResponse.productResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.averageRate
					+ "";

			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(checkInDate));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(checkOutDate));
			eventParams.put("b_win", "" + getBookingWindow(checkInDate));
			eventParams.put("p_type", "HOTEL");
			eventParams.put("hotel_friendly_name", hotelCheckoutResponse.checkoutResponse.productResponse.getHotelName());
			eventParams.put("PropertyId", hotelCheckoutResponse.checkoutResponse.productResponse.hotelId);
			eventParams.put("AveragePrice", "" + avgPrice);
			eventParams.put("StayDuration", "" + numNights);
			eventParams.put("currency", hotelCheckoutResponse.currencyCode);
			eventParams.put("OrderNumber", hotelCheckoutResponse.orderId);
			eventParams.put("TotalPrice", hotelCheckoutResponse.totalCharges);
			eventParams.put("guest_count",guestCount);
			if (Strings.isNotEmpty(couponCode)) {
				eventParams.put("coupon_code", couponCode);
			}
			tracking(eventName, eventParams);
		}
	}


	public static void trackFlightBooked(TripBucketItemFlight tripBucketItemFlight, String orderId, String currency,
		double totalPrice) {
		if (initialized) {
			FlightSearchParams params = tripBucketItemFlight.getFlightSearch().getSearchParams();
			List<FlightLeg> flightLegs = tripBucketItemFlight.getFlightTrip().getLegs();
			String eventName = "Sale Flight";
			Log.i("LeanPlum flight booking event currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
			}
			eventParams.put("DepartureId", params.getDepartureLocation().getDestinationId());
			eventParams.put("ArrivalId", params.getArrivalLocation().getDestinationId());
			eventParams.put("DepartureDate", DateUtils.convertDatetoInt(params.getDepartureDate()));

			eventParams.put("DepartureTakeoffDatetime",
				flightLegs.get(0).getFirstWaypoint().getBestSearchDateTime().toString(DATE_PATTERN));
			eventParams.put("DepartureLandingDatetime",
				flightLegs.get(0).getLastWaypoint().getBestSearchDateTime().toString(DATE_PATTERN));
			if (params.isRoundTrip()) {
				eventParams.put("ReturnDate", DateUtils.convertDatetoInt(params.getReturnDate()));
				eventParams.put("ReturnTakeoffDatetime",
					flightLegs.get(1).getFirstWaypoint().getBestSearchDateTime().toString(DATE_PATTERN));
				eventParams.put("ReturnLandingDatetime",
					flightLegs.get(1).getLastWaypoint().getBestSearchDateTime().toString(DATE_PATTERN));
			}
			eventParams.put("b_win", "" + getBookingWindow(params.getDepartureDate()));
			eventParams.put("p_type", "FLIGHT");
			int numberOfTravelers = params.getNumAdults();
			String productId =
				params.getDepartureLocation().getDestinationId() + "/" + params.getArrivalLocation()
					.getDestinationId();
			eventParams.put("PropertyId", productId);
			eventParams.put("AveragePrice", "" + totalPrice / numberOfTravelers);
			eventParams.put("currency", currency);
			eventParams.put("OrderNumber", orderId);
			eventParams.put("TotalPrice", String.valueOf(totalPrice));
			tracking(eventName, eventParams);

		}
	}

	public static void trackFlightV2Booked(FlightCheckoutResponse flightCheckoutResponse, com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		if (initialized) {
			List<com.expedia.bookings.data.flights.FlightLeg> flightLegs = flightCheckoutResponse.getDetails().legs;
			String eventName = "Sale Flight";
			String currencyCode = flightCheckoutResponse.getCurrencyCode();
			String totalPrice = flightCheckoutResponse.getTotalChargesPrice().amount.toString();
			Log.i("LeanPlum flight booking event currency=" + currencyCode + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			int lastSegment = flightCheckoutResponse.getDetails().legs.get(0).segments.size() - 1;
			com.expedia.bookings.data.flights.FlightLeg.FlightSegment.AirportAddress airportAddress = flightCheckoutResponse.getDetails().legs.get(0).segments.get(lastSegment).arrivalAirportAddress;

			addFlightV2SearchInfo(flightSearchParams, airportAddress, eventParams);

			eventParams.put("DepartureTakeoffDatetime",
				new DateTime(flightLegs.get(0).segments.get(0).departureTimeRaw).toString(DATE_PATTERN));
			int lastFlightSegment = flightLegs.get(0).segments.size() - 1;
			eventParams.put("DepartureLandingDatetime",
				new DateTime(flightLegs.get(0).segments.get(lastFlightSegment).arrivalTimeRaw)
					.toString(DATE_PATTERN));

			if (flightSearchParams.getReturnDate() != null) {
				eventParams.put("ReturnTakeoffDatetime",
					new DateTime(flightLegs.get(1).segments.get(0).departureTimeRaw).toString(DATE_PATTERN));
				int lastReturnFlightSegment = flightLegs.get(1).segments.size() - 1;
				eventParams.put("ReturnLandingDatetime",
					new DateTime(flightLegs.get(1).segments.get(lastReturnFlightSegment).arrivalTimeRaw).toString(DATE_PATTERN));
			}
			int numberOfTravelers = flightSearchParams.getAdults();
			String productId =
				flightSearchParams.getArrivalAirport().gaiaId + "/" + flightSearchParams.getDepartureAirport().gaiaId;
			eventParams.put("PropertyId", productId);
			eventParams.put("AveragePrice", "" + flightCheckoutResponse.getTotalChargesPrice().amount.doubleValue() / numberOfTravelers);
			eventParams.put("currency", currencyCode);
			eventParams.put("OrderNumber", flightCheckoutResponse.getOrderId());
			eventParams.put("TotalPrice", totalPrice);
			tracking(eventName, eventParams);
		}
	}

	public static void trackHotelCheckoutStarted(HotelSearchParams params, Property property, String currency,
		double totalPrice) {
		if (initialized) {
			String eventName = "Checkout Hotel Started";
			Log.i("LeanPlum hotel checkout started currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			Location location = property.getLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
				eventParams.put("Destination", location.getCity());
			}
			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(params.getCheckInDate()));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(params.getCheckOutDate()));
			eventParams.put("b_win", "" + getBookingWindow(params.getCheckInDate()));
			eventParams.put("p_type", "HOTEL");
			eventParams.put("PropertyId", property.getPropertyId());
			eventParams.put("currency", currency);
			eventParams.put("TotalPrice", totalPrice);
			eventParams.put("hotel_friendly_name", property.getName());
			eventParams.put("guest_count", params.getNumTravelers());
			tracking(eventName, eventParams);
		}
	}

	public static void trackHotelV2CheckoutStarted(HotelCreateTripResponse.HotelProductResponse hotelProductResponse, int guestCount) {
		if (initialized) {
			String eventName = "Checkout Hotel Started";
			String price = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.getDisplayTotalPrice()
				.getFormattedMoney();
			String currency = hotelProductResponse.hotelRoomResponse.rateInfo.chargeableRateInfo.currencyCode;

			Log.i("LeanPlum hotel checkout started currency=" + currency + " total=" + price);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			addCommonProductRetargeting(eventParams, hotelProductResponse.hotelCity,
				hotelProductResponse.hotelStateProvince,
				hotelProductResponse.hotelCountry);
			eventParams.put("Destination", hotelProductResponse.hotelCity);

			LocalDate checkInDate = new LocalDate(hotelProductResponse.checkInDate);
			LocalDate checkOutDate = new LocalDate(hotelProductResponse.checkOutDate);

			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(checkInDate));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(checkOutDate));
			eventParams.put("b_win", "" + getBookingWindow(checkInDate));
			eventParams.put("p_type", "HOTEL");
			eventParams.put("PropertyId", hotelProductResponse.hotelId);
			eventParams.put("currency", currency);
			eventParams.put("TotalPrice", price);
			eventParams.put("hotel_friendly_name", hotelProductResponse.getHotelName());
			eventParams.put("guest_count", guestCount);
			tracking(eventName, eventParams);
		}
	}

	public static void trackFlightCheckoutStarted(FlightSearch search, String currency, double totalPrice) {
		if (initialized) {

			String eventName = "Checkout Flight Started";
			Log.i("LeanPlum flight checkout started currency=" + currency + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			FlightSearchParams params = search.getSearchParams();

			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
			}
			eventParams.put("DepartureId", params.getDepartureLocation().getDestinationId());
			eventParams.put("ArrivalId", params.getArrivalLocation().getDestinationId());

			eventParams.put("DepartureDate", DateUtils.convertDatetoInt(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				eventParams.put("ReturnDate", DateUtils.convertDatetoInt(params.getReturnDate()));
			}
			eventParams.put("b_win", "" + getBookingWindow(params.getDepartureDate()));
			eventParams.put("p_type", "FLIGHT");
			String productId =
				params.getDepartureLocation().getDestinationId() + "/" + params.getArrivalLocation()
					.getDestinationId();
			eventParams.put("PropertyId", productId);
			eventParams.put("currency", currency);
			eventParams.put("TotalPrice", totalPrice);
			tracking(eventName, eventParams);

		}
	}

	public static void trackFlightV2CheckoutStarted(FlightCreateTripResponse tripResponse,
		com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		if (initialized) {
			String eventName = "Checkout Flight Started";
			String currencyCode = tripResponse.tripTotalPayableIncludingFeeIfZeroPayableByPoints().currencyCode;
			String totalPrice = tripResponse.tripTotalPayableIncludingFeeIfZeroPayableByPoints().amount.toString();
			Log.i("LeanPlum flight checkout started currency=" + currencyCode + " total=" + totalPrice);
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			int lastSegment = tripResponse.getDetails().legs.get(0).segments.size() - 1;
			com.expedia.bookings.data.flights.FlightLeg.FlightSegment.AirportAddress airportAddress = tripResponse.getDetails().legs.get(0).segments.get(lastSegment).arrivalAirportAddress;

			addFlightV2SearchInfo(flightSearchParams, airportAddress, eventParams);

			String productId =
				flightSearchParams.getArrivalAirport().gaiaId + "/" + flightSearchParams.getDepartureAirport().gaiaId;
			eventParams.put("PropertyId", productId);
			eventParams.put("currency", currencyCode);
			eventParams.put("TotalPrice", totalPrice);
			tracking(eventName, eventParams);
		}
	}

	public static void trackHotelSearch() {
		if (initialized) {
			HotelSearchParams params = Db.getHotelSearch().getSearchParams();
			String eventName = "Search Hotel";
			Log.i("LeanPlum hotel search");
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			if (Db.getHotelSearch().getSearchResponse() != null
				&& Db.getHotelSearch().getSearchResponse().getPropertiesCount() > 0) {
				Location location = Db.getHotelSearch().getSearchResponse().getProperties().get(0).getLocation();
				if (location != null) {
					addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
						location.getCountryCode());
					eventParams.put("Destination", location.getCity());
				}
			}
			if (!TextUtils.isEmpty(params.getRegionId())) {
				eventParams.put("RegionId", params.getRegionId());
			}
			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(params.getCheckInDate()));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(params.getCheckOutDate()));
			eventParams.put("b_win", "" + getBookingWindow(params.getCheckInDate()));
			eventParams.put("p_type", "HOTEL");
			tracking(eventName, eventParams);
		}

	}

	public static void trackHotelV2Search(com.expedia.bookings.data.hotels.HotelSearchParams searchParams,
		HotelSearchResponse searchResponse) {
		if (initialized) {
			String eventName = "Search Hotel";
			Log.i("LeanPlum hotel search");
			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			if (searchResponse != null && !searchResponse.hotelList.isEmpty()) {
				addCommonProductRetargeting(eventParams, searchResponse.hotelList.get(0).city,
					searchResponse.hotelList.get(0).stateProvinceCode,
					searchResponse.hotelList.get(0).countryCode);
				eventParams.put("Destination", searchResponse.hotelList.get(0).city);
			}
			if (!Strings.isEmpty(searchParams.getSuggestion().gaiaId)) {
				eventParams.put("RegionId", searchParams.getSuggestion().gaiaId);
			}
			eventParams.put("CheckInDate", DateUtils.convertDatetoInt(searchParams.getCheckIn()));
			eventParams.put("CheckOutDate", DateUtils.convertDatetoInt(searchParams.getCheckOut()));
			eventParams.put("b_win", "" + getBookingWindow(searchParams.getCheckIn()));
			eventParams.put("p_type", "HOTEL");
			tracking(eventName, eventParams);
		}

	}


	public static void trackFlightSearch() {
		if (initialized) {
			FlightSearchParams params = Db.getFlightSearch().getSearchParams();
			String destinationAirport = params.getArrivalLocation().getDestinationId();
			String eventName = "Search Flight";
			Log.i("LeanPlum flight search destination=" + destinationAirport);

			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(eventParams, location.getCity(), location.getStateCode(),
					location.getCountryCode());
			}
			eventParams.put("Destination", destinationAirport);
			eventParams.put("DepartureId", params.getDepartureLocation().getDestinationId());
			eventParams.put("ArrivalId", params.getArrivalLocation().getDestinationId());

			eventParams.put("DepartureDate", DateUtils.convertDatetoInt(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				eventParams.put("ReturnDate", DateUtils.convertDatetoInt(params.getReturnDate()));
			}
			eventParams.put("b_win", "" + getBookingWindow(params.getDepartureDate()));
			eventParams.put("p_type", "FLIGHT");

			tracking(eventName, eventParams);

		}
	}

	public static void trackFlightV2Search(com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams,
		List<com.expedia.bookings.data.flights.FlightLeg> flightLegList) {
		if (initialized) {
			String eventName = "Search Flight";
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			int lastSegment = flightLegList.get(0).segments.size() - 1;
			com.expedia.bookings.data.flights.FlightLeg.FlightSegment.AirportAddress airportAddress = flightLegList.get(0).segments.get(lastSegment).arrivalAirportAddress;
			addFlightV2SearchInfo(flightSearchParams, airportAddress, eventParams);
			tracking(eventName, eventParams);
		}
	}

	public static void addFlightV2SearchInfo(com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams,
		com.expedia.bookings.data.flights.FlightLeg.FlightSegment.AirportAddress airportAddress,
		HashMap<String, Object> eventParams) {
		String destinationAirport = flightSearchParams.getArrivalAirport().hierarchyInfo.airport.airportCode;
		if (airportAddress != null) {
			addCommonProductRetargeting(eventParams, airportAddress.city, airportAddress.state,
				airportAddress.country);
		}
		eventParams.put("Destination", destinationAirport);
		eventParams.put("DepartureId", flightSearchParams.getDepartureAirport().gaiaId);
		eventParams.put("ArrivalId", flightSearchParams.getArrivalAirport().gaiaId);
		eventParams.put("DepartureDate", DateUtils.convertDatetoInt(flightSearchParams.getDepartureDate()));
		if (flightSearchParams.getReturnDate() != null) {
			eventParams.put("ReturnDate", DateUtils.convertDatetoInt(flightSearchParams.getReturnDate()));
		}
		eventParams.put("b_win", "" + getBookingWindow(flightSearchParams.getDepartureDate()));
		eventParams.put("p_type", "FLIGHT");
	}

	private static int getBookingWindow(LocalDate time) {
		return JodaUtils.daysBetween(LocalDate.now(), time);
	}

	private static HashMap addCommonProductRetargeting(HashMap eventParams, String city,
		String state, String country) {
		// common except home page view/ itin view
		if (!TextUtils.isEmpty(city)) {
			eventParams.put("fb_city", city);
		}
		if (!TextUtils.isEmpty(state)) {
			eventParams.put("fb_state", state);
		}
		if (!TextUtils.isEmpty(country)) {
			eventParams.put("fb_country", country);
		}
		return eventParams;
	}

	public static VariablesChangedCallback flightShareCallback = new VariablesChangedCallback() {
		@Override
		public void variablesChanged() {
			Log.i("Show Share flight Notification " + LeanPlumFlags.mShowShareFlightNotification);
		}
	};

	public static void trackCarSearch(CarSearchParam carSearchParams) {
		if (initialized) {
			String eventName = "Search Car";
			Log.i("LeanPlum car search destination=" + carSearchParams.getOriginLocation());

			/**
			 * Common retargeting params i.e. city, state and country are not available for airport searches.
			 * Add them once available.
			 */
			HashMap<String, Object> eventParams = new HashMap<String, Object>();
			eventParams.put("Destination", carSearchParams.getOriginLocation());
			eventParams.put("PickupDate", DateUtils.convertDatetoInt(carSearchParams.getStartDateTime().toLocalDate()));
			eventParams.put("PickupDatetime", carSearchParams.getStartDateTime().toString(DATE_PATTERN));
			eventParams.put("DropoffDate", DateUtils.convertDatetoInt(carSearchParams.getEndDateTime().toLocalDate()));
			eventParams.put("DropoffDatetime", carSearchParams.getEndDateTime().toString(DATE_PATTERN));
			eventParams.put("b_win", "" + getBookingWindow(carSearchParams.getStartDateTime().toLocalDate()));
			eventParams.put("p_type", "CAR");

			tracking(eventName, eventParams);
		}
	}

	public static void trackCarCheckoutStarted(CreateTripCarOffer carOffer) {
		if (initialized) {
			String eventName = "Checkout Car Started";
			Money total = carOffer.detailedFare.grandTotal;
			Log.i("LeanPlum car checkout started currency=" + total.getCurrency() + " total=" + total.getAmount()
				.doubleValue());

			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			CarLocation pickUpLocation = carOffer.pickUpLocation;
			addCommonProductRetargeting(eventParams, pickUpLocation.cityName, pickUpLocation.provinceStateName,
				pickUpLocation.countryCode);

			DateTime pickUpTime = carOffer.getPickupTime();
			DateTime dropOfTime = carOffer.getDropOffTime();

			eventParams.put("Destination", pickUpLocation.cityName);
			eventParams.put("PickupDate", DateUtils.convertDatetoInt(pickUpTime.toLocalDate()));
			eventParams.put("PickupDatetime", pickUpTime.toString(DATE_PATTERN));
			eventParams.put("DropoffDate", DateUtils.convertDatetoInt(dropOfTime.toLocalDate()));
			eventParams.put("DropoffDatetime", dropOfTime.toString(DATE_PATTERN));

			CreateTripCarFare carFare = carOffer.detailedFare;
			eventParams.put("TotalPrice", String.valueOf(carFare.grandTotal.getAmount().doubleValue()));
			eventParams.put("currency", carFare.grandTotal.getCurrency());

			eventParams.put("b_win", "" + getBookingWindow(pickUpTime.toLocalDate()));
			eventParams.put("p_type", "CAR");

			tracking(eventName, eventParams);
		}
	}

	public static void trackCarBooked(CarCheckoutResponse response) {
		if (initialized) {
			String eventName = "Sale Car";
			CarLocation pickUplocation = response.newCarProduct.pickUpLocation;
			Log.i("LeanPlum car booking event origin = " + pickUplocation.cityName);

			HashMap<String, Object> eventParams = new HashMap<String, Object>();

			addCommonProductRetargeting(eventParams, pickUplocation.cityName, pickUplocation.provinceStateName,
				pickUplocation.countryCode);

			DateTime pickUpTime = response.newCarProduct.getPickupTime();
			DateTime dropOfTime = response.newCarProduct.getDropOffTime();

			eventParams.put("Destination", pickUplocation.cityName);
			eventParams.put("PickupDate", DateUtils.convertDatetoInt(pickUpTime.toLocalDate()));
			eventParams.put("PickupDatetime", pickUpTime.toString(DATE_PATTERN));
			eventParams.put("DropoffDate", DateUtils.convertDatetoInt(dropOfTime.toLocalDate()));
			eventParams.put("DropoffDatetime", dropOfTime.toString(DATE_PATTERN));
			eventParams.put("RentalDuration", JodaUtils.hoursBetween(pickUpTime, dropOfTime));

			CreateTripCarFare carFare = response.newCarProduct.detailedFare;
			eventParams.put("TotalPrice", String.valueOf(carFare.grandTotal.getAmount().doubleValue()));
			eventParams.put("currency", carFare.grandTotal.getCurrency());

			eventParams.put("b_win", "" + getBookingWindow(pickUpTime.toLocalDate()));
			eventParams.put("p_type", "CAR");

			tracking(eventName, eventParams);
		}
	}

	public static void trackLxSearch(LxSearchParams lxSearchParams) {
		if (initialized) {
			String eventName = "Search LX";
			Log.i("LeanPlum LX search ActivityDatetime=" + lxSearchParams.getActivityStartDate().toDateTimeAtStartOfDay().toString(DATE_PATTERN));

			HashMap<String, Object> eventParams = new HashMap<>();

			/**
			 * Common retargeting params i.e. city, state and country are not available for LX.
			 * Add them once available.
			 */
			eventParams.put("p_type", "LX");
			eventParams.put("b_win", "" + getBookingWindow(lxSearchParams.getActivityStartDate()));

			eventParams.put("ActivityDate", "" + DateUtils.convertDatetoInt(lxSearchParams.getActivityStartDate()));
			eventParams.put("ActivityDatetime", "" + lxSearchParams.getActivityStartDate().toDateTimeAtStartOfDay().toString(DATE_PATTERN));

			tracking(eventName, eventParams);
		}
	}

	public static void trackLXCheckoutStarted(String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate , List<String> lxActivityCategories) {
		if (initialized) {
			String eventName = "Checkout LX Started";
			Log.i("LeanPlum LX checkout started event origin = " + lxActivityLocation);

			trackLXCheckoutInformation(eventName, lxActivityLocation, totalPrice, lxOfferSelectedDate, lxActivityCategories);
		}
	}

	public static void trackLXBooked(String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate , List<String> lxActivityCategories) {
		if (initialized) {
			String eventName = "Sale LX";
			Log.i("LeanPlum LX booking event origin = " + lxActivityLocation);

			trackLXCheckoutInformation(eventName, lxActivityLocation, totalPrice, lxOfferSelectedDate, lxActivityCategories);
		}
	}

	private static void trackLXCheckoutInformation(String eventName, String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate , List<String> lxActivityCategories) {
		HashMap<String, Object> eventParams = new HashMap<>();

		/**
		 * Common retargeting params i.e. city, state and country are not available for LX.
		 * Add them once available.
		 */
		eventParams.put("destination", lxActivityLocation);

		eventParams.put("TotalPrice", String.valueOf(totalPrice.getAmount().doubleValue()));
		eventParams.put("currency", totalPrice.getCurrency());

		eventParams.put("ActivityDate", "" + DateUtils.convertDatetoInt(DateUtils.yyyyMMddHHmmssToLocalDate(lxOfferSelectedDate)));
		eventParams.put("ActivityDatetime", "" + DateUtils.yyyyMMddHHmmssToDateTime(lxOfferSelectedDate).toString(DATE_PATTERN));
		eventParams.put("isGT", LXDataUtils.isActivityGT(lxActivityCategories));
		eventParams.put("b_win", "" + getBookingWindow(DateUtils.yyyyMMddHHmmssToLocalDate(lxOfferSelectedDate)));
		eventParams.put("p_type", "LX");

		tracking(eventName, eventParams);
	}
}
