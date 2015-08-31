package com.expedia.bookings.tracking;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.AdX.tag.AdXConnect;
import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearch;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.JodaUtils;
import com.expedia.bookings.utils.Strings;
import com.mobiata.android.Log;

public class AdX {
	private static Context context;
	private static boolean initialized = false;
	private static boolean connected = false;

	public static void initialize(Context context) {
		initialized = true;
		AdX.context = context.getApplicationContext();
		Log.i("AdX tracking initialized");
	}

	private static void connect(String pos, boolean launchedAgain) {
		if (initialized && !connected) {
			String adXKey = ProductFlavorFeatureConfiguration.getInstance().getAdXKey();
			if (Strings.isNotEmpty(adXKey)) {
				AdXConnect.setKey(adXKey);
			}

			int logLevel = BuildConfig.RELEASE ? 0 : 5;
			AdXConnect.getAdXConnectInstance(context, launchedAgain, logLevel);
			connected = true;
		}
	}

	public static void trackFirstLaunch() {
		if (initialized) {
			String pos = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
			connect(pos, false);
			AdXConnect.getAdXConnectEventInstance(context, "FirstLaunch", "", "");
			Log.i("AdX first launch event PointOfSale=" + pos);

			reportReferralToOmniture();

			// Retargeting
			AdXConnect.startNewExtendedEvent(context);
			AdXConnect.sendExtendedEventOfName("launch");
		}
	}

	public static void trackLaunch() {
		if (initialized) {
			String pos = PointOfSale.getPointOfSale().getTwoLetterCountryCode();
			connect(pos, true);
			AdXConnect.getAdXConnectEventInstance(context, "Launch", "", "");
			Log.i("AdX launch event PointOfSale=" + pos);

			// Retargeting
			AdXConnect.startNewExtendedEvent(context);
			AdXConnect.sendExtendedEventOfName("launch");
		}
	}

	public static void trackDeepLinkLaunch(Uri data) {
		if (initialized) {
			String adxid = data.getQueryParameter("ADXID");
			if (adxid != null && adxid.length() > 0) {
				AdXConnect.getAdXConnectEventInstance(context, "DeepLinkLaunch", adxid, "");
				Log.i("AdX deep link launch, Ad-X ID=" + adxid);
			}
		}
	}

	public static void trackLogin() {
		if (initialized) {
			AdXConnect.getAdXConnectEventInstance(context, "Login", "", "");
			Log.i("AdX login event");
		}
	}

	public static void trackViewHomepage() {
		if (initialized) {
			AdXConnect.startNewExtendedEvent(context);
			addCommonRetargeting();
			AdXConnect.sendExtendedEvent(AdXConnect.ADX_EVENT_HOMEPAGE);
		}
	}

	public static void trackViewItinList() {
		if (initialized) {
			AdXConnect.getAdXConnectEventInstance(context, "Itinerary", "", "");
			Log.i("AdX Itinerary event");
		}
	}

	public static void trackHotelBooked(HotelSearchParams params, Property property, String orderNumber, String currency, double totalPrice, double avgPrice) {
		if (initialized) {
			AdXConnect.getAdXConnectEventInstance(context, "Sale", String.valueOf(totalPrice), currency, "Hotel");
			Log.i("AdX hotel booking event currency=" + currency + " total=" + totalPrice);

			// Retargeting event
			DateTimeFormatter dtf = ISODateTimeFormat.date();

			AdXConnect.startNewExtendedEvent(context);
			addCommonRetargeting();

			Location location = property.getLocation();
			if (location != null) {
				addCommonProductRetargeting(location.getCity(), location.getStateCode(), location.getCountryCode());
				AdXConnect.setEventParameter(AdXConnect.ADX_DESTINATION_ID, location.getCity());
			}

			AdXConnect.setEventParameter(AdXConnect.ADX_START_DATE, dtf.print(params.getCheckInDate()));
			AdXConnect.setEventParameter(AdXConnect.ADX_END_DATE, dtf.print(params.getCheckOutDate()));
			AdXConnect.setEventParameterOfName("b_win", getBookingWindow(params.getCheckInDate()));
			AdXConnect.setEventParameterOfName("p_type", "HOTEL");

			AdXConnect.addProductToList(property.getPropertyId(), avgPrice, params.getStayDuration());
			AdXConnect.setEventParameterOfName("currency", currency);
			AdXConnect.setEventParameterOfName("id", orderNumber);

			AdXConnect.sendExtendedEvent(AdXConnect.ADX_EVENT_CONFIRMATION);
		}
	}

	public static void trackFlightBooked(FlightSearch search, String orderId, String currency, double totalPrice) {
		if (initialized) {
			AdXConnect.getAdXConnectEventInstance(context, "Sale", String.valueOf(totalPrice), currency, "Flight");
			Log.i("AdX flight booking event currency=" + currency + " total=" + totalPrice);

			// Retargeting event
			FlightSearchParams params = search.getSearchParams();
			DateTimeFormatter dtf = ISODateTimeFormat.date();

			AdXConnect.startNewExtendedEvent(context);
			addCommonRetargeting();
			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(location.getCity(), location.getStateCode(), location.getCountryCode());
			}

			AdXConnect.setEventParameter(AdXConnect.ADX_SOURCE_ID, params.getDepartureLocation().getDestinationId());
			AdXConnect.setEventParameter(AdXConnect.ADX_DESTINATION_ID, params.getArrivalLocation().getDestinationId());
			AdXConnect.setEventParameter(AdXConnect.ADX_START_DATE, dtf.print(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				AdXConnect.setEventParameter(AdXConnect.ADX_END_DATE, dtf.print(params.getReturnDate()));
			}
			AdXConnect.setEventParameterOfName("b_win", getBookingWindow(params.getDepartureDate()));
			AdXConnect.setEventParameterOfName("p_type", "FLIGHT");

			int numberOfTravelers = params.getNumAdults();
			String productId = params.getDepartureLocation().getDestinationId() + "/" + params.getArrivalLocation().getDestinationId();
			AdXConnect.addProductToList(productId, totalPrice / numberOfTravelers, numberOfTravelers);
			AdXConnect.setEventParameterOfName("currency", currency);
			AdXConnect.setEventParameterOfName("id", orderId);

			AdXConnect.sendExtendedEvent(AdXConnect.ADX_EVENT_CONFIRMATION);
		}
	}

	public static void trackHotelCheckoutStarted(HotelSearchParams params, Property property, String currency, double totalPrice) {
		if (initialized) {
			AdXConnect.getAdXConnectEventInstance(context, "Checkout", String.valueOf(totalPrice), currency, "Hotel");
			Log.i("AdX hotel checkout started currency=" + currency + " total=" + totalPrice);

			// Retargeting event
			DateTimeFormatter dtf = ISODateTimeFormat.date();

			AdXConnect.startNewExtendedEvent(context);
			addCommonRetargeting();

			Location location = property.getLocation();
			if (location != null) {
				addCommonProductRetargeting(location.getCity(), location.getStateCode(), location.getCountryCode());
				AdXConnect.setEventParameter(AdXConnect.ADX_DESTINATION_ID, location.getCity());
			}

			AdXConnect.setEventParameter(AdXConnect.ADX_START_DATE, dtf.print(params.getCheckInDate()));
			AdXConnect.setEventParameter(AdXConnect.ADX_END_DATE, dtf.print(params.getCheckOutDate()));
			AdXConnect.setEventParameterOfName("b_win", getBookingWindow(params.getCheckInDate()));
			AdXConnect.setEventParameterOfName("p_type", "HOTEL");

			AdXConnect.setEventParameter(AdXConnect.ADX_PRODUCT, property.getPropertyId());
			AdXConnect.setEventParameterOfName("pr", String.valueOf(totalPrice));
			AdXConnect.setEventParameterOfName("currency", currency);

			AdXConnect.sendExtendedEvent(AdXConnect.ADX_EVENT_PRODUCTVIEW);
		}
	}

	public static void trackFlightCheckoutStarted(FlightSearch search, String currency, double totalPrice) {
		if (initialized) {
			AdXConnect.getAdXConnectEventInstance(context, "Checkout", String.valueOf(totalPrice), currency, "Flight");
			Log.i("AdX flight checkout started currency=" + currency + " total=" + totalPrice);

			// Retargeting event
			FlightSearchParams params = search.getSearchParams();
			DateTimeFormatter dtf = ISODateTimeFormat.date();

			AdXConnect.startNewExtendedEvent(context);
			addCommonRetargeting();
			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(location.getCity(), location.getStateCode(), location.getCountryCode());
			}

			AdXConnect.setEventParameter(AdXConnect.ADX_SOURCE_ID, params.getDepartureLocation().getDestinationId());
			AdXConnect.setEventParameter(AdXConnect.ADX_DESTINATION_ID, params.getArrivalLocation().getDestinationId());
			AdXConnect.setEventParameter(AdXConnect.ADX_START_DATE, dtf.print(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				AdXConnect.setEventParameter(AdXConnect.ADX_END_DATE, dtf.print(params.getReturnDate()));
			}
			AdXConnect.setEventParameterOfName("b_win", getBookingWindow(params.getDepartureDate()));
			AdXConnect.setEventParameterOfName("p_type", "FLIGHT");

			String productId = params.getDepartureLocation().getDestinationId() + "/" + params.getArrivalLocation().getDestinationId();
			AdXConnect.setEventParameter(AdXConnect.ADX_PRODUCT, productId);
			AdXConnect.setEventParameterOfName("pr", String.valueOf(totalPrice));
			AdXConnect.setEventParameterOfName("currency", currency);

			AdXConnect.sendExtendedEvent(AdXConnect.ADX_EVENT_PRODUCTVIEW);
		}
	}

	public static void trackHotelSearch(HotelSearch search) {
		if (initialized) {
			HotelSearchParams params = search.getSearchParams();
			if (!TextUtils.isEmpty(params.getRegionId())) {
				AdXConnect.getAdXConnectEventInstance(context, "Search", "", params.getRegionId(), "Hotel");
				Log.i("AdX hotel search regionId=" + params.getRegionId());
			}

			DateTimeFormatter dtf = ISODateTimeFormat.date();

			// Retargeting event
			AdXConnect.startNewExtendedEvent(context);
			addCommonRetargeting();
			if (search.getSearchResponse() != null && search.getSearchResponse().getPropertiesCount() > 0) {
				Location location = search.getSearchResponse().getProperty(0).getLocation();
				if (location != null) {
					addCommonProductRetargeting(location.getCity(), location.getStateCode(), location.getCountryCode());
					AdXConnect.setEventParameter(AdXConnect.ADX_DESTINATION_ID, location.getCity());
				}
			}
			AdXConnect.setEventParameter(AdXConnect.ADX_START_DATE, dtf.print(params.getCheckInDate()));
			AdXConnect.setEventParameter(AdXConnect.ADX_END_DATE, dtf.print(params.getCheckOutDate()));
			AdXConnect.setEventParameterOfName("b_win", getBookingWindow(params.getCheckInDate()));
			AdXConnect.setEventParameterOfName("p_type", "HOTEL");

			AdXConnect.sendExtendedEvent(AdXConnect.ADX_EVENT_SEARCH);
		}
	}

	public static void trackFlightSearch(FlightSearch flightSearch) {
		if (initialized) {
			FlightSearchParams params = flightSearch.getSearchParams();
			String destinationAirport = params.getArrivalLocation().getDestinationId();
			AdXConnect.getAdXConnectEventInstance(context, "Search", "", destinationAirport, "Flight");
			Log.i("AdX flight search destination=" + destinationAirport);

			DateTimeFormatter dtf = ISODateTimeFormat.date();

			// Retargeting event
			AdXConnect.startNewExtendedEvent(context);
			addCommonRetargeting();
			Location location = params.getArrivalLocation();
			if (location != null) {
				addCommonProductRetargeting(location.getCity(), location.getStateCode(), location.getCountryCode());
			}

			AdXConnect.setEventParameter(AdXConnect.ADX_SOURCE_ID, params.getDepartureLocation().getDestinationId());
			AdXConnect.setEventParameter(AdXConnect.ADX_DESTINATION_ID, params.getArrivalLocation().getDestinationId());
			AdXConnect.setEventParameter(AdXConnect.ADX_START_DATE, dtf.print(params.getDepartureDate()));
			if (params.isRoundTrip()) {
				AdXConnect.setEventParameter(AdXConnect.ADX_END_DATE, dtf.print(params.getReturnDate()));
			}
			AdXConnect.setEventParameterOfName("b_win", getBookingWindow(params.getDepartureDate()));
			AdXConnect.setEventParameterOfName("p_type", "FLIGHT");

			AdXConnect.sendExtendedEvent(AdXConnect.ADX_EVENT_SEARCH);
		}
	}

	private static void reportReferralToOmniture() {
		// getAdXReferral is blocking, run off the UI thread
		new Thread(new Runnable() {
			@Override
			public void run() {
				// Was told by the AdX guys to just hold off for a bit before
				// calling getAdXReferral()
				try {
					Thread.sleep(15 * DateUtils.SECOND_IN_MILLIS);
				}
				catch (Exception e) {
					// Should not ever happen
				}

				String referral = AdXConnect.getAdXReferral(context, 15);
				if (TextUtils.isEmpty(referral)) {
					Log.w("Unable to retrieve AdX referral string");
				}
				else {
					Log.d("Got AdX referral string: " + referral);
					OmnitureTracking.trackAdXReferralLink(referral);
				}
			}
		}).start();
	}

	private static void addCommonRetargeting() {
		int adXPosIdentifier = ProductFlavorFeatureConfiguration.getInstance().getAdXPosIdentifier();

		AdXConnect.setEventParameterOfName("a", adXPosIdentifier);

		if (Db.getUser() != null) {
			final String customerId = Db.getUser().getTuidString();
			AdXConnect.setEventParameter(AdXConnect.ADX_CUSTOMERID, customerId);
		}
		AdXConnect.setEventParameterOfName("pos", PointOfSale.getPointOfSale().getTwoLetterCountryCode());
		final String loggedIn = User.isLoggedIn(context) ? "loggedin | hard" : "unknown user";
		AdXConnect.setEventParameterOfName("fb_logged_in", loggedIn);
		final String rewardStatus = Db.getUser() != null && Db.getUser().isRewardsUser() ? "rewardsMember" : "notRewardsMember";
		AdXConnect.setEventParameterOfName("fb_reward_status", rewardStatus);
	}

	private static void addCommonProductRetargeting(String city, String state, String country) {
			// common except home page view/ itin view
			if (!TextUtils.isEmpty(city)) {
				AdXConnect.setEventParameterOfName("fb_city", city);
			}
			if (!TextUtils.isEmpty(state)) {
				AdXConnect.setEventParameterOfName("fb_state", state);
			}
			if (!TextUtils.isEmpty(country)) {
				AdXConnect.setEventParameterOfName("fb_country", country);
			}
	}

	private static int getBookingWindow(LocalDate time) {
		return JodaUtils.daysBetween(LocalDate.now(), time);
	}

}
