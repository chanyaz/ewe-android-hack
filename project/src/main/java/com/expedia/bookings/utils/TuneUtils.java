package com.expedia.bookings.utils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.TripDetails;
import com.expedia.bookings.data.flights.FlightCheckoutResponse;
import com.expedia.bookings.data.flights.FlightCreateTripResponse;
import com.expedia.bookings.data.flights.FlightLeg;
import com.expedia.bookings.data.flights.FlightTripDetails;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.hotels.HotelCreateTripResponse;
import com.expedia.bookings.data.hotels.HotelOffersResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.multiitem.BundleSearchResponse;
import com.expedia.bookings.data.packages.PackageCheckoutResponse;
import com.expedia.bookings.data.packages.PackageSearchParams;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.services.HotelCheckoutResponse;
import com.expedia.bookings.tracking.flight.FlightSearchTrackingData;
import com.expedia.bookings.tracking.hotel.HotelSearchTrackingData;
import com.tune.TuneEvent;
import com.tune.TuneEventItem;

public class TuneUtils {
	private static TuneTrackingProvider trackingProvider;

	public static void init(TuneTrackingProvider provider) {
		trackingProvider = provider;
		updatePOS();

		trackLaunchEvent();
	}

	public static void updatePOS() {
		if (trackingProvider != null) {
			String posTpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
			String posEapid = Integer.toString(PointOfSale.getPointOfSale().getEAPID());
			String posData = posTpid;
			Boolean sendEapidToTuneTracking = ProductFlavorFeatureConfiguration.getInstance().sendEapidToTuneTracking();
			if (sendEapidToTuneTracking && Strings.isNotEmpty(posEapid) && !Strings.equals(posEapid, Integer.toString(PointOfSale.INVALID_EAPID))) {
				posData = posTpid + "-" + posEapid;
			}
			trackingProvider.setPosData(posData);
		}
	}

	public static void setFacebookReferralUrl(String facebookReferralUrl) {
		if (trackingProvider != null) {
			trackingProvider.setFacebookReferralUrlString(facebookReferralUrl);
		}
	}

	private static void trackLaunchEvent() {
		if (trackingProvider != null) {
			TuneEvent launchEvent = new TuneEvent("Custom_Open")
					.withAttribute1(trackingProvider.getTuid())
					.withAttribute3(trackingProvider.getMembershipTier())
					.withAttribute2(trackingProvider.isUserLoggedInValue());

			trackingProvider.trackEvent(launchEvent);
		}
	}

	public static void trackHotelV2InfoSite(HotelOffersResponse hotelOffersResponse) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("hotel_infosite");
			TuneEventItem eventItem = new TuneEventItem("hotel_infosite_item");
			LocalDate checkInDate = new LocalDate(hotelOffersResponse.checkInDate);
			LocalDate checkOutDate = new LocalDate(hotelOffersResponse.checkOutDate);

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
				.withEventItems(Collections.singletonList(eventItem))
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withQuantity(stayDuration)
				.withContentType(hotelOffersResponse.hotelName)
				.withContentId(hotelOffersResponse.hotelId);
			event.withRevenue(lowestPrice)
				.withCurrencyCode(currencyCode);

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackHotelV2CheckoutStarted(HotelCreateTripResponse.HotelProductResponse hotelProductResponse) {
		if (trackingProvider != null) {
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
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withContentType(hotelProductResponse.getHotelName())
				.withContentId(hotelProductResponse.hotelId)
				.withEventItems(Collections.singletonList(eventItem))
				.withDate1(checkInDate.toDate())
				.withDate2(checkOutDate.toDate())
				.withQuantity(stayDuration);

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackHotelV2SearchResults(HotelSearchTrackingData trackingData) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("hotel_search_results");
			TuneEventItem eventItem = new TuneEventItem("hotel_search_results_item");

			Date checkInDate = trackingData.getCheckInDate().toDate();
			Date checkOutDate = trackingData.getCheckoutDate().toDate();

			StringBuilder topHotelIdsBuilder = new StringBuilder();
			StringBuilder sb = new StringBuilder();

			List<Hotel> hotels = trackingData.getHotels();

			int hotelsCount = hotels.size();
			int lastIndex = getLastIndex(hotelsCount);
			if (hotelsCount >= 0) {
				for (int i = 0; i <= lastIndex; i++) {
					Hotel hotel = hotels.get(i);
					topHotelIdsBuilder.append(hotel.hotelId);
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
					if (i != lastIndex) {
						sb.append(":");
						topHotelIdsBuilder.append(",");
					}
				}
			}
			if (hotelsCount > 0) {
				eventItem.withAttribute1(hotels.get(0).city);
			}
			eventItem.withAttribute4(topHotelIdsBuilder.toString());
			eventItem.withAttribute5(sb.toString());

			withTuidAndMembership(event)
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withDate1(checkInDate)
				.withDate2(checkOutDate)
				.withEventItems(Collections.singletonList(eventItem))
				.withSearchString("hotel")
				.withLevel(1);

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackHotelV2Confirmation(HotelCheckoutResponse hotelCheckoutResponse) {
		if (trackingProvider != null) {
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
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withRevenue(revenue)
				.withCurrencyCode(hotelCheckoutResponse.currencyCode)
				.withAdvertiserRefId(getAdvertiserRefId(hotelCheckoutResponse.checkoutResponse.bookingResponse.travelRecordLocator))
				.withQuantity(stayDuration)
				.withContentType(hotelCheckoutResponse.checkoutResponse.productResponse.getHotelName())
				.withContentId(hotelCheckoutResponse.checkoutResponse.productResponse.hotelId)
				.withEventItems(Collections.singletonList(eventItem))
				.withDate1(checkInDate.toDate())
				.withDate2(checkOutDate.toDate());

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackFlightV2RateDetailOverview(
		com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		if (trackingProvider != null) {
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
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withEventItems(Collections.singletonList(eventItem))
				.withDate1(departureDate);

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackFlightV2OutBoundResults(FlightSearchTrackingData searchTrackingData) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("flight_outbound_result");
			TuneEventItem eventItem = new TuneEventItem("flight_outbound_result_item");
			eventItem.withAttribute2(searchTrackingData.getDepartureAirport().hierarchyInfo.airport.airportCode)
				.withAttribute3(searchTrackingData.getArrivalAirport().hierarchyInfo.airport.airportCode);

			if (searchTrackingData.getFlightLegList() != null && !searchTrackingData.getFlightLegList().isEmpty()) {
				int propertiesCount = searchTrackingData.getFlightLegList().size();
				StringBuilder sb = new StringBuilder();
				int lastIndex = getLastIndex(propertiesCount);
				if (propertiesCount >= 0) {
					for (int i = 0; i <= lastIndex; i++) {
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
						if (i != lastIndex) {
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
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withEventItems(Collections.singletonList(eventItem))
				.withSearchString("flight")
				.withDate1(departureDate)
				.withCurrencyCode(getCurrencyCode(searchTrackingData.getFlightLegList()));

			trackingProvider.trackEvent(event);
		}

	}

	public static void trackFlightV2InBoundResults(FlightSearchTrackingData trackingData) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("flight_inbound_result");
			TuneEventItem eventItem = new TuneEventItem("flight_inbound_result_item");
			eventItem.withAttribute2(trackingData.getDepartureAirport().hierarchyInfo.airport.airportCode)
				.withAttribute3(trackingData.getArrivalAirport().hierarchyInfo.airport.airportCode);
			List<FlightLeg> flightLegList = trackingData.getFlightLegList();
			if (!flightLegList.isEmpty()) {
				int propertiesCount = flightLegList.size();
				StringBuilder sb = new StringBuilder();
				int lastIndex = getLastIndex(propertiesCount);
				if (propertiesCount >= 0) {
					for (int i = 0; i <= lastIndex; i++) {
						String carrier = flightLegList.get(i).segments.get(0).airlineCode;
						String currency = flightLegList.get(i).packageOfferModel.price.packageTotalPrice.currencyCode;
						String price = flightLegList.get(i).packageOfferModel.price.packageTotalPrice.amount.toString();
						String routeType = trackingData.getReturnDate() != null ? "RT" : "OW";
						String route = String.format("%s-%s", trackingData.getDepartureAirport().gaiaId,
							trackingData.getArrivalAirport().gaiaId);

						sb.append(
							String.format("%s|%s|%s|%s|%s", carrier, currency, price, routeType, route));
						if (i != lastIndex) {
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
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withEventItems(Collections.singletonList(eventItem))
				.withSearchString("flight")
				.withDate1(departureDate)
				.withCurrencyCode(getCurrencyCode(trackingData.getFlightLegList()));

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackFlightV2Booked(FlightCheckoutResponse flightCheckoutResponse, com.expedia.bookings.data.flights.FlightSearchParams flightSearchParams) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("flight_confirmation");
			TuneEventItem eventItem = new TuneEventItem("flight_confirmation_item");
			double totalPrice = flightCheckoutResponse.getTotalChargesPrice().amount.doubleValue();
			int totalGuests = flightSearchParams.getGuests();
			double averagePrice = totalPrice/totalGuests;
			TripDetails trip = flightCheckoutResponse.getNewTrip() == null ? new TripDetails() : flightCheckoutResponse.getNewTrip();
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
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withRevenue(totalPrice)
				.withCurrencyCode(flightCheckoutResponse.getTotalChargesPrice().currencyCode)
				.withQuantity(totalGuests)
				.withAdvertiserRefId(getAdvertiserRefId(trip.getTravelRecordLocator()))
				.withEventItems(Collections.singletonList(eventItem))
				.withDate1(departureDate);

			trackingProvider.trackEvent(event);
		}

	}

	public static void trackPackageHotelSearchResults(BundleSearchResponse packageTrackingData) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("package_search_results");
			TuneEventItem eventItem = new TuneEventItem("package_search_result_item");

			Date checkInDate = LocalDate.parse(packageTrackingData.getHotelCheckInDate()).toDate();
			Date checkOutDate = LocalDate.parse(packageTrackingData.getHotelCheckOutDate()).toDate();

			StringBuilder topHotelIdsBuilder = new StringBuilder();
			StringBuilder sb = new StringBuilder();

			List<Hotel> hotels = packageTrackingData.getHotels();
			int hotelsCount = hotels.size();
			int lastIndex = getLastIndex(hotelsCount);

			for (int i = 0; i <= lastIndex; i++) {
				Hotel hotel = hotels.get(i);
				topHotelIdsBuilder.append(hotel.hotelId);
				String hotelId = hotel.hotelId;
				String hotelName = hotel.localizedName;
				String price = "";
				String currency = "";

				if (hotel.lowRateInfo != null) {
					price = hotel.lowRateInfo.total + "";
					currency = hotel.lowRateInfo.currencyCode;
				}

				String starRating = Double.toString(hotel.hotelStarRating);

				String miles = hotel.proximityDistanceInMiles != 0 ? Double.toString(hotel.proximityDistanceInMiles) : "0";
				sb.append(String.format("%s|%s|%s|%s|%s|%s", hotelId, hotelName, currency, price, starRating, miles));
				if (i != lastIndex) {
					sb.append(":");
					topHotelIdsBuilder.append(",");
				}
			}

			if (hotelsCount > 0) {
				eventItem.withAttribute1(hotels.get(0).city);
			}
			eventItem.withAttribute4(topHotelIdsBuilder.toString());
			eventItem.withAttribute5(sb.toString());

			withTuidAndMembership(event)
					.withAttribute2(trackingProvider.isUserLoggedInValue())
					.withDate1(checkInDate)
					.withDate2(checkOutDate)
					.withEventItems(Collections.singletonList(eventItem))
					.withSearchString("hotel")
					.withCurrencyCode(packageTrackingData.getCurrencyCode())
					.withLevel(1);

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackPackageOutBoundResults(PackageSearchParams searchTrackingData) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("package_outbound_search_results");
			TuneEventItem eventItem = new TuneEventItem("package_outbound_search_item");
			eventItem.withAttribute2(searchTrackingData.getOrigin().hierarchyInfo.airport.airportCode)
					.withAttribute3(searchTrackingData.getDestination().hierarchyInfo.airport.airportCode);

			if (searchTrackingData.getFlightLegList() != null && !searchTrackingData.getFlightLegList().isEmpty()) {
				int propertiesCount = searchTrackingData.getFlightLegList().size();
				StringBuilder sb = new StringBuilder();
				int lastIndex = getLastIndex(propertiesCount);

				for (int i = 0; i <= lastIndex; i++) {
					FlightLeg flightLeg = searchTrackingData.getFlightLegList().get(i);
					String carrier = flightLeg.carrierCode;
					String currency = flightLeg.packageOfferModel.price.packageTotalPrice.currencyCode;
					String price = flightLeg.packageOfferModel.price.packageTotalPrice.amount.toString();
					String routeType = "RT";
					String route = String.format("%s-%s", searchTrackingData.getOrigin().gaiaId, searchTrackingData.getDestination().gaiaId);
					sb.append(String.format("%s|%s|%s|%s|%s", carrier, currency, price, routeType, route));
					if (i != lastIndex) {
						sb.append(":");
					}
				}
				eventItem.withAttribute5(sb.toString());
			}
			Date departureDate = searchTrackingData.getStartDate().toDate();
			if (searchTrackingData.getEndDate() != null) {
				Date returnDate = searchTrackingData.getEndDate().toDate();
				event.withDate2(returnDate);
			}
			withTuidAndMembership(event)
					.withAttribute2(trackingProvider.isUserLoggedInValue())
					.withEventItems(Collections.singletonList(eventItem))
					.withSearchString("flight")
					.withCurrencyCode(getCurrencyCode(searchTrackingData.getFlightLegList()))
					.withDate1(departureDate);

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackPackageInBoundResults(PackageSearchParams searchTrackingData) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("package_inbound_search_results");
			TuneEventItem eventItem = new TuneEventItem("package_inbound_search_item");
			eventItem.withAttribute2(searchTrackingData.getOrigin().hierarchyInfo.airport.airportCode)
					.withAttribute3(searchTrackingData.getDestination().hierarchyInfo.airport.airportCode);

			if (searchTrackingData.getFlightLegList() != null && !searchTrackingData.getFlightLegList().isEmpty()) {
				int propertiesCount = searchTrackingData.getFlightLegList().size();
				StringBuilder sb = new StringBuilder();
				int lastIndex = getLastIndex(propertiesCount);

				for (int i = 0; i <= lastIndex; i++) {
					FlightLeg flightLeg = searchTrackingData.getFlightLegList().get(i);
					String carrier = flightLeg.carrierCode;
					String currency = flightLeg.packageOfferModel.price.packageTotalPrice.currencyCode;
					String price = flightLeg.packageOfferModel.price.packageTotalPrice.amount.toString();
					String routeType = "RT";
					String route = String.format("%s-%s", searchTrackingData.getOrigin().gaiaId, searchTrackingData.getDestination().gaiaId);
					sb.append(String.format("%s|%s|%s|%s|%s", carrier, currency, price, routeType, route));
					if (i != lastIndex) {
						sb.append(":");
					}
				}
				eventItem.withAttribute5(sb.toString());
			}
			Date departureDate = searchTrackingData.getStartDate().toDate();
			if (searchTrackingData.getEndDate() != null) {
				Date returnDate = searchTrackingData.getEndDate().toDate();
				event.withDate2(returnDate);
			}
			withTuidAndMembership(event)
					.withAttribute2(trackingProvider.isUserLoggedInValue())
					.withEventItems(Collections.singletonList(eventItem))
					.withSearchString("flight")
					.withCurrencyCode(getCurrencyCode(searchTrackingData.getFlightLegList()))
					.withDate1(departureDate);

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackPackageConfirmation(PackageCheckoutResponse packageCheckoutResponse, PackageSearchParams packageSearchParams) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("package_confirmation");
			TuneEventItem eventItem = new TuneEventItem("package_confirmation_item");

			LocalDate checkInDate = new LocalDate(packageCheckoutResponse.getPackageDetails().getHotel().checkInDate);
			LocalDate checkOutDate = new LocalDate(packageCheckoutResponse.getPackageDetails().getHotel().checkOutDate);
			int stayDuration = JodaUtils.daysBetween(checkInDate, checkOutDate);
			double revenue = packageCheckoutResponse.getPackageDetails().getPricing().getPackageTotal().amount.doubleValue();
			float nightlyRate = packageCheckoutResponse.getPackageDetails().getHotel().hotelRoomResponse.rateInfo.chargeableRateInfo.averageRate;
			TripDetails trip = packageCheckoutResponse.getNewTrip() == null ? new TripDetails() : packageCheckoutResponse.getNewTrip();
			String flightNumber = "";

			if (packageSearchParams.getFlightLegList() != null) {
				flightNumber = packageSearchParams.getFlightLegList().get(0).flightSegments.get(0).flightNumber;
			}

			eventItem.withQuantity(stayDuration)
					.withAttribute1(packageCheckoutResponse.getPackageDetails().getHotel().hotelCity)
					.withAttribute2(flightNumber)
					.withUnitPrice(nightlyRate)
					.withRevenue(revenue);

			withTuidAndMembership(event)
					.withAttribute2(trackingProvider.isUserLoggedInValue())
					.withRevenue(revenue)
					.withCurrencyCode(packageCheckoutResponse.getPackageDetails().getPricing().getPackageTotal().currencyCode)
					.withAdvertiserRefId(getAdvertiserRefId(trip.getTravelRecordLocator()))
					.withQuantity(stayDuration)
					.withContentType(packageCheckoutResponse.getPackageDetails().getHotel().getHotelName())
					.withContentId(packageCheckoutResponse.getPackageDetails().getHotel().hotelId)
					.withEventItems(Collections.singletonList(eventItem))
					.withDate1(checkInDate.toDate())
					.withDate2(checkOutDate.toDate());

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackLXSearch(LxSearchParams searchParams, LXSearchResponse searchResponse) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("lx_search");
			TuneEventItem eventItem = new TuneEventItem("lx_search_item");

			eventItem.withAttribute2(searchParams.getLocation());
			if (searchResponse != null) {
				int activitiesCount = searchResponse.activities.size();
				StringBuilder sb = new StringBuilder();
				StringBuilder topActivitiesBuilder = new StringBuilder();
				int lastIndex = getLastIndex(activitiesCount);
				if (activitiesCount >= 0) {
					for (int i = 0; i <= lastIndex; i++) {
						LXActivity activity = searchResponse.activities.get(i);
						String title = activity.title;
						String currency = activity.price.currencyCode;
						double price = activity.price.amount.doubleValue();
						topActivitiesBuilder.append(title);
						sb.append(String.format("%s|%s|%s", title, currency, price));
						if (i != lastIndex) {
							sb.append(":");
							topActivitiesBuilder.append(",");
						}
					}
				}
				eventItem.withAttribute5(sb.toString());
				eventItem.withAttribute4(topActivitiesBuilder.toString());
			}

			withTuidAndMembership(event)
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withCurrencyCode(searchResponse.currencyCode)
				.withEventItems(Collections.singletonList(eventItem))
				.withDate1(searchParams.getActivityStartDate().toDate())
				.withSearchString("lx");

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackLXDetails(String lxActivityLocation, Money totalPrice, String lxOfferSelectedDate,
		int selectedTicketCount, String lxActivityTitle, String activityId) {
		if (trackingProvider != null) {
			TuneEvent event = new TuneEvent("lx_details");
			TuneEventItem eventItem = new TuneEventItem("lx_details_item").withAttribute2(lxActivityLocation)
				.withAttribute3(lxActivityTitle);

			withTuidAndMembership(event)
				.withQuantity(selectedTicketCount)
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withRevenue(totalPrice.getAmount().doubleValue())
				.withCurrencyCode(totalPrice.getCurrency())
				.withContentId(activityId)
				.withEventItems(Collections.singletonList(eventItem))
				.withDate1(ApiDateUtils.yyyyMMddHHmmssToDateTime(lxOfferSelectedDate).toDate());
			trackingProvider.trackEvent(event);
		}
	}

	public static void trackLXConfirmation(String itinNumber, String activityId, String lxActivityLocation,
		Money totalPrice, Money ticketPrice, String lxActivityStartDate, String lxActivityTitle,
		int selectedTicketCount, int selectedChildTicketCount) {
		if (trackingProvider != null) {
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
				.withAttribute2(trackingProvider.isUserLoggedInValue())
				.withRevenue(revenue)
				.withQuantity(1)
				.withContentId(activityId)
				.withCurrencyCode(totalPrice.getCurrency())
				.withAdvertiserRefId(getAdvertiserRefId(itinNumber))
				.withEventItems(Collections.singletonList(eventItem))
				.withDate1(ApiDateUtils.yyyyMMddHHmmssToDateTime(lxActivityStartDate).toDate());

			trackingProvider.trackEvent(event);
		}
	}

	public static void trackLogin() {
		if (trackingProvider != null) {
			TuneEvent loginEvent = new TuneEvent("login");
			loginEvent.withAttribute1(trackingProvider.getTuid());
			loginEvent.withAttribute2(trackingProvider.getMembershipTier());
			trackingProvider.trackEvent(loginEvent);
		}
	}

	//////////
	// Helpers

	private static TuneEvent withTuidAndMembership(TuneEvent event) {
		return event.withAttribute1(trackingProvider.getTuid())
			.withAttribute3(trackingProvider.getMembershipTier());
	}

	private static String getAdvertiserRefId(String itinNumber) {
		String tpid = Integer.toString(PointOfSale.getPointOfSale().getTpid());
		return String.format("%s:%s", itinNumber, tpid);
	}

	private static int getLastIndex(int propertiesCount) {
		int top5 = 5;

		if (propertiesCount < top5) {
			return propertiesCount - 1;
		}
		else {
			return top5 - 1;
		}
	}

	private static String getCurrencyCode(List<FlightLeg> flightLegs) {
		String currencyCode = "";
		if (flightLegs != null && !flightLegs.isEmpty()) {
			currencyCode = flightLegs.get(0).packageOfferModel.price.packageTotalPrice.currencyCode;
		}
		return currencyCode;
	}

}
