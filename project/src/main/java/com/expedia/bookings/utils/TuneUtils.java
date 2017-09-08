package com.expedia.bookings.utils;

import com.expedia.bookings.ADMS_Measurement;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParam;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.flights.FlightCheckoutResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.flights.FlightTripDetails;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.data.trips.TripBucketItemFlight;
import com.expedia.bookings.data.trips.TripBucketItemHotel;
import com.expedia.bookings.data.user.UserStateManager;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.tracking.flight.FlightSearchTrackingData;
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData;
import com.mobiata.android.Log;
import com.mobiata.android.util.SettingUtils;
import com.tune.Tune;
import com.tune.TuneDeeplinkListener;
import com.tune.TuneEvent;
import com.tune.TuneEventItem;
import com.tune.ma.application.TuneActivityLifecycleCallbacks;


public class TuneUtils {

	private static Tune tune = null;
	private static boolean initialized = false;
	private static Context context;
	private static UserStateManager userStateManager;

	public static void init(Application app) {
		initialized = true;
		context = app.getApplicationContext();
		userStateManager = Ui.getApplication(context).appComponent().userStateManager();

		app.registerActivityLifecycleCallbacks(new TuneActivityLifecycleCallbacks());

		String advertiserID = app.getString(R.string.tune_sdk_app_advertiser_id);
		String conversionKey = app.getString(R.string.tune_sdk_app_conversion_key);

		tune = Tune.init(app, advertiserID, conversionKey);
		if (ProductFlavorFeatureConfiguration.getInstance().shouldSetExistingUserForTune()
			&& olderOrbitzVersionWasInstalled(context)) {
			tune.setExistingUser(true);
		}
		tune.setUserId(ADMS_Measurement.sharedInstance(app.getApplicationContext()).getVisitorID());
		tune.setGoogleUserId(getExpediaUserId());
		tune.setDebugMode(BuildConfig.DEBUG && SettingUtils
			.get(context, context.getString(R.string.preference_enable_tune), false));
		tune.registerDeeplinkListener(new TuneDeeplinkListener() {
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

		TuneEvent launchEvent = new TuneEvent("Custom_Open")
			.withAttribute1(getTuid())
			.withAttribute3(getMembershipTier())
			.withAttribute2(isUserLoggedIn());
		trackEvent(launchEvent);
	}

	// To check whether user is migrated form old app to new version of app
	// Old Orbitz app set `anonId` in `loginPreferences`
	private static boolean olderOrbitzVersionWasInstalled(Context context) {
		SharedPreferences olderOrbitzPreferences = context.getSharedPreferences("loginPreferences", Context.MODE_PRIVATE);
		return olderOrbitzPreferences.contains("anonId");
	}

	public static void updatePOS() {
		if (initialized) {
			String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
			String posEapid = Integer.toString(PointOfSale.getPointOfSale().getEAPID());
			String posData = posTpid;
			Boolean sendEapidToTuneTracking = ProductFlavorFeatureConfiguration.getInstance().sendEapidToTuneTracking();
			if (sendEapidToTuneTracking && Strings.isNotEmpty(posEapid) && !Strings.equals(posEapid, Integer.toString(PointOfSale.INVALID_EAPID))) {
				posData = posTpid + "-" + posEapid;
			}
			tune.setTwitterUserId(posData);
		}
	}

	public static void trackHomePageView() {
		if (initialized) {
			TuneEvent event = new TuneEvent("home_view");

			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn());
			trackEvent(event);
		}
	}

	public static void setFacebookReferralUrl(String facebookReferralUrl) {
		if (initialized) {
			tune.setReferralUrl(facebookReferralUrl);
		}
	}

	public static void trackHotelInfoSite(Property selectedProperty) {
		if (initialized) {
			TuneEvent event = new TuneEvent("hotel_infosite");
			TuneEventItem eventItem = new TuneEventItem("hotel_infosite_item");
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
			TuneEvent event = new TuneEvent("hotel_infosite");
			TuneEventItem eventItem = new TuneEventItem("hotel_infosite_item");
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
			TuneEvent event = new TuneEvent("hotel_rate_details");
			TuneEventItem eventItem = new TuneEventItem("hotel_rate_details_item");
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
			TuneEvent event = new TuneEvent("hotel_rate_details");
			TuneEventItem eventItem = new TuneEventItem("hotel_rate_details_item");

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
			TuneEvent event = new TuneEvent("hotel_search_results");
			TuneEventItem eventItem = new TuneEventItem("hotel_search_results_item");

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

	public static void trackHotelV2SearchResults(HotelSearchTrackingData trackingData) {
		if (initialized) {
			TuneEvent event = new TuneEvent("hotel_search_results");
			TuneEventItem eventItem = new TuneEventItem("hotel_search_results_item");

			Date checkInDate = trackingData.getCheckInDate().toDate();
			Date checkOutDate = trackingData.getCheckoutDate().toDate();

			StringBuilder topFiveHotelIdsBuilder = new StringBuilder();
			StringBuilder sb = new StringBuilder();

			List<Hotel> hotels = trackingData.getHotels();

			int hotelsCount = hotels.size();
			if (hotels != null && hotelsCount >= 0) {
				for (int i = 0; (i < 5 && i < hotelsCount); i++) {
					Hotel hotel = hotels.get(i);
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
				eventItem.withAttribute1(hotels.get(0).city);
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
			TuneEvent event = new TuneEvent("hotel_confirmation");
			TuneEventItem eventItem = new TuneEventItem("hotel_confirmation_item");

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
				.withAdvertiserRefId(getAdvertiserRefId(transactionId))
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
			TuneEvent event = new TuneEvent("hotel_confirmation");
			TuneEventItem eventItem = new TuneEventItem("hotel_confirmation_item");

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
				.withAdvertiserRefId(getAdvertiserRefId(hotelCheckoutResponse.checkoutResponse.bookingResponse.travelRecordLocator))
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

			TuneEvent event = new TuneEvent("flight_rate_details");
			TuneEventItem eventItem = new TuneEventItem("flight_rate_details_item");
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
				.withRevenue(trip.getTotalPrice().getAmount().doubleValue())
				.withCurrencyCode(trip.getTotalPrice().getCurrency())
				.withAttribute2(isUserLoggedIn())
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(departureDate);


			trackEvent(event);
		}
	}

	public static void trackFlightV2RateDetailOverview(
		com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		if (initialized) {
			FlightCreateTripResponse flightCreateTripResponse = Db.getTripBucket()
				.getFlightV2().flightCreateTripResponse;

			TuneEvent event = new TuneEvent("flight_rate_details");
			TuneEventItem eventItem = new TuneEventItem("flight_rate_details_item");
			eventItem.withQuantity(flightSearchParams.getGuests())
				.withAttribute2(flightSearchParams.getDepartureAirport().hierarchyInfo.airport.airportCode)
				.withAttribute3(flightSearchParams.getArrivalAirport().hierarchyInfo.airport.airportCode)
				.withAttribute4(flightCreateTripResponse.getDetails().legs.get(0).segments.get(0).airlineCode);

			Date departureDate = flightSearchParams.getDepartureDate().toDate();
			if (flightSearchParams.getReturnDate() != null) {
				Date returnDate = flightSearchParams.getReturnDate().toDate();
				event.withDate2(returnDate);
			}
			Money totalPrice = flightCreateTripResponse.getDetails().offer.totalPrice;
			withTuidAndMembership(event)
				.withRevenue(totalPrice.amount.doubleValue())
				.withCurrencyCode(totalPrice.currencyCode)
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
			TuneEvent event = new TuneEvent("flight_outbound_result");
			TuneEventItem eventItem = new TuneEventItem("flight_outbound_result_item");
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
						String currency = trip.getTotalPrice().getCurrency();
						String price = trip.getTotalPrice().amount.toString();
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

	public static void trackFlightV2OutBoundResults(FlightSearchTrackingData searchTrackingData) {
		if (initialized) {
			TuneEvent event = new TuneEvent("flight_outbound_result");
			TuneEventItem eventItem = new TuneEventItem("flight_outbound_result_item");
			eventItem.withAttribute2(searchTrackingData.getDepartureAirport().hierarchyInfo.airport.airportCode)
				.withAttribute3(searchTrackingData.getArrivalAirport().hierarchyInfo.airport.airportCode);

			if (searchTrackingData.getFlightLegList() != null && !searchTrackingData.getFlightLegList().isEmpty()) {
				int propertiesCount = searchTrackingData.getFlightLegList().size();
				StringBuilder sb = new StringBuilder();
				if (propertiesCount >= 0) {
					for (int i = 0; (i < 5 && i < propertiesCount); i++) {
						FlightLeg flightLeg = searchTrackingData.getFlightLegList().get(i);
						String carrier = flightLeg.segments.get(0).airlineCode;
						String currency = flightLeg.packageOfferModel.price.packageTotalPrice.currencyCode;
						String price = flightLeg.packageOfferModel.price.packageTotalPrice.amount.toString();
						String routeType = searchTrackingData.getReturnDate() != null ? "RT" : "OW";
						String route = String.format("%s-%s", searchTrackingData.getDepartureAirport().gaiaId,
							searchTrackingData.getArrivalAirport().gaiaId
						);

						sb.append(
							String.format("%s|%s|%s|%s|%s", carrier, currency, price, routeType, route));
						if (i != 4) {
							sb.append(":");
						}
					}
				}
				eventItem.withAttribute5(sb.toString());
			}
			Date departureDate = searchTrackingData.getDepartureDate().toDate();
			if (searchTrackingData.getReturnDate() != null) {
				Date returnDate = searchTrackingData.getReturnDate().toDate();
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
			TuneEvent event = new TuneEvent("flight_inbound_result");
			TuneEventItem eventItem = new TuneEventItem("flight_inbound_result_item");
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
						String currency = trip.getTotalPrice().getCurrency();
						String price = trip.getTotalPrice().amount.toString();
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

	public static void trackFlightV2InBoundResults(FlightSearchTrackingData trackingData) {
		if (initialized) {
			TuneEvent event = new TuneEvent("flight_inbound_result");
			TuneEventItem eventItem = new TuneEventItem("flight_inbound_result_item");
			eventItem.withAttribute2(trackingData.getArrivalAirport().hierarchyInfo.airport.airportCode)
				.withAttribute3(trackingData.getDepartureAirport().hierarchyInfo.airport.airportCode);
			List<FlightLeg> flightLegList = trackingData.getFlightLegList();
			if (flightLegList != null && !flightLegList.isEmpty()) {
				int propertiesCount = flightLegList.size();
				StringBuilder sb = new StringBuilder();
				if (propertiesCount >= 0) {
					for (int i = 0; (i < 5 && i < propertiesCount); i++) {
						String carrier = flightLegList.get(i).segments.get(0).airlineCode;
						String currency = flightLegList.get(i).packageOfferModel.price.packageTotalPrice.currencyCode;
						String price = flightLegList.get(i).packageOfferModel.price.packageTotalPrice.amount.toString();
						String routeType = trackingData.getReturnDate() != null ? "RT" : "OW";
						String route = String.format("%s-%s", trackingData.getArrivalAirport().gaiaId,
							trackingData.getDepartureAirport().gaiaId);

						sb.append(
							String.format("%s|%s|%s|%s|%s", carrier, currency, price, routeType, route));
						if (i != 4) {
							sb.append(":");
						}
					}
				}
				eventItem.withAttribute5(sb.toString());
			}
			Date departureDate = trackingData.getDepartureDate().toDate();
			if (trackingData.getReturnDate() != null) {
				Date returnDate = trackingData.getReturnDate().toDate();
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
			TuneEvent event = new TuneEvent("flight_confirmation");
			TuneEventItem eventItem = new TuneEventItem("flight_confirmation_item");
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
				.withAdvertiserRefId(getAdvertiserRefId(tripBucketItemFlight.getItinerary().getTravelRecordLocator()))
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(departureDate);

			trackEvent(event);
		}

	}

	public static void trackFlightV2Booked(FlightCheckoutResponse flightCheckoutResponse, com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		if (initialized) {
			TuneEvent event = new TuneEvent("flight_confirmation");
			TuneEventItem eventItem = new TuneEventItem("flight_confirmation_item");
			double totalPrice = flightCheckoutResponse.getTotalChargesPrice().amount.doubleValue();
			int totalGuests = flightSearchParams.getGuests();
			double averagePrice = totalPrice/totalGuests;
			FlightTripDetails firstFlightTripDetails = flightCheckoutResponse.getFirstFlightTripDetails();
			FlightLeg.FlightSegment firstFlightSegment = firstFlightTripDetails.getLegs().get(0).segments.get(0);
			eventItem.withQuantity(totalGuests)
				.withRevenue(totalPrice)
				.withUnitPrice(averagePrice)
				.withAttribute2(flightSearchParams.getDepartureAirport().gaiaId)
				.withAttribute3(flightSearchParams.getArrivalAirport().gaiaId)
				.withAttribute4(firstFlightSegment.airlineCode);


			Date departureDate = new DateTime(firstFlightSegment.departureTimeRaw).toDate();

			if (flightSearchParams.getReturnDate() != null) {
				FlightLeg.FlightSegment lastFlightSegment = flightCheckoutResponse.getLastFlightLastSegment();
				Date returnDate = new DateTime(lastFlightSegment.departureTimeRaw).toDate();
				event.withDate2(returnDate);
			}
			withTuidAndMembership(event)
				.withAttribute2(isUserLoggedIn())
				.withRevenue(totalPrice)
				.withCurrencyCode(flightCheckoutResponse.getTotalChargesPrice().currencyCode)
				.withQuantity(totalGuests)
				.withAdvertiserRefId(getAdvertiserRefId(flightCheckoutResponse.getNewTrip().getTravelRecordLocator()))
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(departureDate);

			trackEvent(event);
		}

	}

	public static void trackCarSearch(CarSearch search, CarSearchParam params) {
		if (initialized) {
			TuneEvent event = new TuneEvent("car_result");
			TuneEventItem eventItem = new TuneEventItem("car_result_item");
			eventItem.withAttribute2(params.getOriginLocation())
				.withAttribute3(params.getOriginLocation());

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
				.withDate2(params.getStartDateTime().toDate())
				.withSearchString("car")
				.withDate1(params.getEndDateTime().toDate());

			trackEvent(event);
		}
	}

	public static void trackCarRateDetails(CreateTripCarOffer carOffer) {
		if (initialized) {
			TuneEvent event = new TuneEvent("car_rate_details");
			TuneEventItem eventItem = new TuneEventItem("car_rate_details_item");
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
			TuneEvent event = new TuneEvent("car_confirmation");
			TuneEventItem eventItem = new TuneEventItem("car_confirmation_item");

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
				.withAdvertiserRefId(getAdvertiserRefId(carCheckoutResponse.newTrip.travelRecordLocator))
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(pickupTime)
				.withDate2(dropOffTime);

			trackEvent(event);
		}
	}

	public static void trackLXSearch(LxSearchParams searchParams, LXSearchResponse searchResponse) {
		if (initialized) {
			TuneEvent event = new TuneEvent("lx_search");
			TuneEventItem eventItem = new TuneEventItem("lx_search_item");

			eventItem.withAttribute2(searchParams.getLocation());
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
						sb.append(String.format(Locale.getDefault(), "%s|%s|%f", title, currency, price));
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
				.withDate1(searchParams.getActivityStartDate().toDate())
				.withSearchString("lx");

			trackEvent(event);
		}
	}

	public static void trackLXDetails(String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate,
		int selectedTicketCount, String lxActivityTitle) {
		if (initialized) {
			TuneEvent event = new TuneEvent("lx_details");
			TuneEventItem eventItem = new TuneEventItem("lx_details_item").withAttribute2(lxActivityLocation)
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
		LXCheckoutResponse checkoutResponse, String lxActivityTitle, int selectedTicketCount, int selectedChildTicketCount) {
		if (initialized) {
			TuneEvent event = new TuneEvent("lx_confirmation");
			TuneEventItem eventItem = new TuneEventItem("lx_confirmation_item");
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
				.withAdvertiserRefId(getAdvertiserRefId(checkoutResponse.newTrip.travelRecordLocator))
				.withEventItems(Arrays.asList(eventItem))
				.withDate1(DateUtils
					.yyyyMMddHHmmssToLocalDate(lxActivityStartDate)
					.toDate());

			trackEvent(event);
		}
	}

	private static void trackEvent(TuneEvent eventName) {
		if (initialized) {
			tune.measureEvent(eventName);
		}
	}

	public static void trackLogin() {
		if (initialized) {
			TuneEvent loginEvent = new TuneEvent("login");
			loginEvent.withAttribute1(getTuid());
			loginEvent.withAttribute2(getMembershipTier());
			trackEvent(loginEvent);
		}
	}

	//////////
	// Helpers

	private static String getMembershipTier() {
		if (userStateManager.isUserAuthenticated()) {
			lazyLoadUser();
			return userStateManager.getCurrentUserLoyaltyTier().toApiValue();
		}
		return "";
	}

	private static String getTuid() {
		if (userStateManager.isUserAuthenticated()) {
			lazyLoadUser();
			return Db.getUser().getTuidString();
		}
		return "";
	}

	private static String getExpediaUserId() {
		if (userStateManager.isUserAuthenticated()) {
			lazyLoadUser();
			return Db.getUser().getExpediaUserId();
		}
		return "";
	}

	private static void lazyLoadUser() {
		if (Db.getUser() == null && userStateManager.isUserAuthenticated()) {
			Db.loadUser(context);
		}
	}

	private static HotelSearchParams getHotelSearchParams() {
		return Db.getHotelSearch().getSearchParams();
	}

	private static TuneEvent withTuidAndMembership(TuneEvent event) {
		return event.withAttribute1(getTuid())
			.withAttribute3(getMembershipTier());
	}

	private static String isUserLoggedIn() {
		return userStateManager.isUserAuthenticated() ? "1" : "0";
	}

	private static String getAdvertiserRefId(String travelRecordLocator) {
		String tpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		return String.format("%s:%s", travelRecordLocator, tpid);
	}

}
