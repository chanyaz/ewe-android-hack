package com.expedia.bookings.tracking;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;

import com.adobe.adms.measurement.ADMS_Measurement;
import com.adobe.adms.measurement.ADMS_ReferrerHandler;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Distance.DistanceUnit;
import com.expedia.bookings.data.Filter;
import com.expedia.bookings.data.Filter.PriceRange;
import com.expedia.bookings.data.Filter.SearchRadius;
import com.expedia.bookings.data.FlightFilter;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Itinerary;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SearchResponse;
import com.expedia.bookings.fragment.BookingInfoValidation;
import com.expedia.bookings.utils.CalendarUtils;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.SettingUtils;

/**
 * The spec behind this class can be found here: http://confluence/display/Omniture/Mobile+App+Flight+Tracking
 *
 * The basic premise behind this class is to encapsulate the tracking logic as much possible such that tracking events
 * can be inserted into the business logic as cleanly as possible. The events rely on Db.java to populate values when
 * needed, and exceptions are made to accommodate the events that require extra parameters to be sent. This is why there
 * exist so many methods, one for each event that is being tracked.
 *
 */

public class OmnitureTracking {
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// STANDARD PAGE NAME CONSTANTS

	private static final String TAG = "OmnitureTracking";

	private enum LOB {
		HOTELS,
		FLIGHTS
	}

	// Launcher
	private static final String LAUNCH_SCREEN = "App.LaunchScreen";

	// Flights
	private static final String FLIGHT_SEARCH = "App.Flight.Search";
	private static final String FLIGHT_SEARCH_INTERSTITIAL = "App.Flight.Search.Interstitial";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT = "App.Flight.Search.Roundtrip.Out";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS = "App.Flight.Search.Roundtrip.Out.Details";
	private static final String FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.Out.BaggageFee";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN = "App.Flight.Search.Roundtrip.In";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS = "App.Flight.Search.Roundtrip.In.Details";
	private static final String FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE = "App.Flight.Search.Roundtrip.In.BaggageFee";
	private static final String FLIGHT_RATE_DETAILS = "App.Flight.RateDetails";
	private static final String FLIGHT_CHECKOUT_INFO = "App.Flight.Checkout.Info";
	private static final String FLIGHT_CHECKOUT_LOGIN = "App.Flight.Checkout.Login";
	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT = "App.Flight.Checkout.Traveler.Select";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO = "App.Flight.Checkout.Traveler.Edit.Info";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS = "App.Flight.Checkout.Traveler.Edit.Details";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT = "App.Flight.Checkout.Traveler.Edit.Passport";
	private static final String FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE = "App.Flight.Checkout.Traveler.Edit.Save";
	private static final String FLIGHT_CHECKOUT_WARSAW = "App.Flight.Checkout.Warsaw";
	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT = "App.Flight.Checkout.Payment.Select";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS = "App.Flight.Checkout.Payment.Edit.Address";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD = "App.Flight.Checkout.Payment.Edit.Card";
	private static final String FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE = "App.Flight.Checkout.Payment.Edit.Save";
	private static final String FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE = "App.Flight.Checkout.SlideToPurchase";
	private static final String FLIGHT_CHECKOUT_PAYMENT_CID = "App.Flight.Checkout.Payment.CID";
	private static final String FLIGHT_CHECKOUT_CONFIRMATION = "App.Flight.Checkout.Confirmation";

	// Hotels
	private static final String HOTELS_ROOMS_RATES = "App.Hotels.RoomsRates";
	private static final String HOTELS_RATE_DETAILS = "App.Hotels.RateDetails";

	private static final String HOTELS_CHECKOUT_INFO = "App.Hotels.Checkout.Info";

	private static final String HOTELS_CHECKOUT_LOGIN = "App.Hotels.Checkout.Login";
	private static final String HOTELS_CHECKOUT_LOGIN_SUCCESS = "App.Hotels.Checkout.Login.Success";
	private static final String HOTELS_CHECKOUT_LOGIN_FORGOT = "App.Hotels.Checkout.Login.Forgot";

	private static final String HOTELS_CHECKOUT_TRAVELER_SELECT = "App.Hotels.Checkout.Traveler.Select";
	private static final String HOTELS_CHECKOUT_TRAVELER_EDIT_INFO = "App.Hotels.Checkout.Traveler.Edit.Info";
	private static final String HOTELS_CHECKOUT_TRAVELER_ENTER_MANUALLY = "App.Hotels.Checkout.Traveler.EnterManually";

	private static final String HOTELS_CHECKOUT_WARSAW = "App.Hotels.Checkout.Warsaw";

	private static final String HOTELS_CHECKOUT_PAYMENT_SELECT = "App.Hotels.Checkout.Payment.Select";
	private static final String HOTELS_CHECKOUT_PAYMENT_EDIT_CARD = "App.Hotels.Checkout.Payment.Edit.Card";
	private static final String HOTELS_CHECKOUT_PAYMENT_EDIT_SAVE = "App.Hotels.Checkout.Payment.Edit.Save";
	private static final String HOTELS_CHECKOUT_PAYMENT_SELECT_EXISTING = "App.Hotels.Checkout.Payment.Select.Existing";
	private static final String HOTELS_CHECKOUT_PAYMENT_ENTER_MANUALLY = "App.Hotels.Checkout.Payment.EnterManually";

	private static final String HOTELS_CHECKOUT_SLIDE_TO_PURCHASE = "App.Hotels.Checkout.SlideToPurchase";
	private static final String HOTELS_CHECKOUT_PAYMENT_CID = "App.Hotels.Checkout.Payment.CID";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ONEWAY PAGE NAME AND TRACK LINK CONSTANTS

	private static final String FLIGHT_SEARCH_RESULTS_ONE_WAY = "App.Flight.Search.OneWay";
	private static final String PREFIX_FLIGHT_SEARCH_ONE_WAY_SELECT = "App.Flight.Search.OneWay.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ONE_WAY_SORT = "App.Flight.Search.OneWay.Sort";
	private static final String FLIGHT_SEARCH_ONE_WAY_REFINE = "App.Flight.Search.OneWay.RefineSearch";
	private static final String FLIGHT_SEARCH_ONE_WAY_DETAILS = "App.Flight.Search.OneWay.Details";
	private static final String FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE = "App.Flight.Search.OneWay.BaggageFee";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ERROR PAGE NAME CONSTANTS

	private static final String FLIGHT_ERROR_NOT_YET_AVAILABLE = "App.Flight.Error.NotYetAvailable";
	private static final String FLIGHT_ERROR_CHECKOUT = "App.Flight.Error.Checkout";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET = "App.Flight.Error.Checkout.Payment.PriceChange.Ticket";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED = "App.Flight.Error.Checkout.Payment.Failed";
	private static final String FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV = "App.Flight.Error.Checkout.Payment.CVV";
	private static final String FLIGHT_ERROR_SOLD_OUT = "App.Flight.Error.SoldOut";
	private static final String FLIGHT_ERROR_SEARCH_EXPIRED = "App.Flight.Error.Search.Expired";

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// TRACK LINK RFFR ACTION ID CONSTANTS

	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT = "App.Flight.Search.Roundtrip.Out.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT = "App.Flight.Search.Roundtrip.Out.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE = "App.Flight.Search.Roundtrip.Out.RefineSearch";

	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT = "App.Flight.Search.Roundtrip.In.Select";
	private static final String PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT = "App.Flight.Search.Roundtrip.In.Sort";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE = "App.Flight.Search.Roundtrip.In.RefineSearch";
	private static final String FLIGHT_SEARCH_ROUNDTRIP_IN_REMOVE_OUT = "App.Flight.Search.Roundtrip.In.RemoveOut";

	private static final String FLIGHT_CHECKOUT_LOGIN_SUCCESS = "App.Flight.Checkout.Login.Success";
	private static final String FLIGHT_CHECKOUT_LOGIN_FORGOT = "App.Flight.Checkout.Login.Forgot";

	private static final String FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING = "App.Flight.Checkout.Traveler.Select.Existing";
	private static final String FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY = "App.Flight.Checkout.Traveler.EnterManually";
	private static final String FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING = "App.Flight.Checkout.Payment.Select.Existing";
	private static final String FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY = "App.Flight.Checkout.Payment.EnterManually";

	private static final String FLIGHT_CONFIRMATION_HOTEL_X_SELL = "App.Flight.Checkout.Confirmation.HotelXSell";

	// Hotels

	private static final String HOTELS_SEARCH_REFINE = "App.Hotels.Search.Refine";
	private static final String HOTELS_SEARCH_REFINE_NAME = "App.Hotels.Search.Refine.Name";
	private static final String HOTELS_SEARCH_REFINE_PRICE_RANGE = "App.Hotels.Search.Refine.PriceRange";
	private static final String HOTELS_SEARCH_REFINE_SEARCH_RADIUS = "App.Hotels.Search.Refine.SearchRadius";

	private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	// The SettingUtils key for the last version tracked
	private static final String TRACK_VERSION = "tracking_version";

	public static void init(Context context) {
		ADMS_Measurement s = ADMS_Measurement.sharedInstance(context);
		s.configureMeasurement(TrackingUtils.getReportSuiteIds(context), TrackingUtils.getTrackingServer());
	}

	// Lifecycle tracking

	public static void onResume(Activity activity) {
		ADMS_Measurement measurement = ADMS_Measurement.sharedInstance(activity);
		measurement.startActivity(activity);

	}

	public static void onPause() {
		ADMS_Measurement measurement = ADMS_Measurement.sharedInstance();
		measurement.stopActivity();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC TRACK LINK METHODS

	public static void trackLinkLaunchScreenToHotels(Context context) {
		String link = LAUNCH_SCREEN + "." + "Hotel";
		internalTrackLink(context, link);
	}

	public static void trackLinkLaunchScreenToFlights(Context context) {
		String link = LAUNCH_SCREEN + "." + "Flight";
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightSearchSelect(Context context, int selectPos, int legPos) {
		String prefix = "";

		if (legPos == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SELECT;
			}
			else {
				prefix = PREFIX_FLIGHT_SEARCH_ONE_WAY_SELECT;
			}
		}
		else if (legPos == 1) {
			prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SELECT;
		}

		FlightFilter filter = Db.getFlightSearch().getFilter(legPos);
		String link = prefix + "." + filter.getSort().name() + "." + Integer.toString(selectPos);

		internalTrackLink(context, link);
	}

	public static void trackLinkFlightRefine(Context context, int legPosition) {
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_REFINE);
			}
			else {
				internalTrackLink(context, FLIGHT_SEARCH_ONE_WAY_REFINE);
			}
		}
		else if (legPosition == 1) {
			internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_IN_REFINE);
		}
	}

	public static void trackLinkFlightSort(Context context, String sortType, int legPosition) {
		String prefix = "";

		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_OUT_SORT;
			}
			else {
				prefix = PREFIX_FLIGHT_SEARCH_ONE_WAY_SORT;
			}
		}
		else if (legPosition == 1) {
			prefix = PREFIX_FLIGHT_SEARCH_ROUNDTRIP_IN_SORT;
		}

		String link = prefix + "." + sortType;
		internalTrackLink(context, link);
	}

	public static void trackLinkFlightRemoveOutboundSelection(Context context) {
		internalTrackLink(context, FLIGHT_SEARCH_ROUNDTRIP_IN_REMOVE_OUT);
	}

	public static void trackLinkFlightCheckoutLoginSuccess(Context context) {
		Log.d(TAG, "Tracking \"" + FLIGHT_CHECKOUT_LOGIN_SUCCESS + "\" linkClick");

		ADMS_Measurement s = createTrackLinkEvent(context, FLIGHT_CHECKOUT_LOGIN_SUCCESS);

		s.setEvents("event26");

		s.trackLink(null, "o", s.getEvar(28), null, null);
	}

	public static void trackLinkFlightCheckoutLoginForgot(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_LOGIN_FORGOT);
	}

	public static void trackLinkFlightCheckoutTravelerSelectExisting(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_TRAVELER_SELECT_EXISTING);
	}

	public static void trackLinkFlightCheckoutTravelerEnterManually(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_TRAVELER_ENTER_MANUALLY);
	}

	public static void trackLinkFlightCheckoutPaymentSelectExisting(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_PAYMENT_SELECT_EXISTING);
	}

	public static void trackLinkFlightCheckoutPaymentEnterManually(Context context) {
		internalTrackLink(context, FLIGHT_CHECKOUT_PAYMENT_ENTER_MANUALLY);
	}

	public static void trackLinkFlightConfirmationHotelCrossSell(Context context) {
		internalTrackLink(context, FLIGHT_CONFIRMATION_HOTEL_X_SELL);
	}

	public static void trackErrorPageLoadFlightUnsupportedPOS(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_NOT_YET_AVAILABLE, LOB.FLIGHTS);
	}

	public static void trackErrorPageLoadFlightCheckout(Context context) {
		internalTrackPageLoadEventPriceChange(context, FLIGHT_ERROR_CHECKOUT);
	}

	public static void trackErrorPageLoadFlightPriceChangeTicket(Context context) {
		internalTrackPageLoadEventPriceChange(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_PRICE_CHANGE_TICKET);
	}

	public static void trackErrorPageLoadFlightPaymentFailed(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_FAILED, LOB.FLIGHTS);
	}

	public static void trackErrorPageLoadFlightIncorrectCVV(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_CHECKOUT_PAYMENT_CVV, LOB.FLIGHTS);
	}

	public static void trackErrorPageLoadFlightSoldOut(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_SOLD_OUT, LOB.FLIGHTS);
	}

	public static void trackErrorPageLoadFlightSearchExpired(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_ERROR_SEARCH_EXPIRED, LOB.FLIGHTS);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Flights tracking events

	public static void trackPageLoadFlightCheckoutConfirmation(Context context) {
		Log.d(TAG, "Tracking \"" + FLIGHT_CHECKOUT_CONFIRMATION + "\" pageLoad");
		ADMS_Measurement s = createTrackPageLoadEventBase(context, FLIGHT_CHECKOUT_CONFIRMATION);
		addVars25And26LobAsConfirmer(s);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// Flight: <departure Airport Code>-<Destination Airport Code>:<departure date YYYYMMDD>-<return date YYYYMMDD>:<promo code applied N/Y>
		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();
		String origin = searchParams.getDepartureLocation().getDestinationId();
		String dest = searchParams.getArrivalLocation().getDestinationId();

		String eVar30 = "Flight:";
		eVar30 += origin;
		eVar30 += "-";
		eVar30 += dest;
		eVar30 += ":";

		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		eVar30 += df.format(searchParams.getDepartureDate().getCalendar().getTime());
		if (searchParams.isRoundTrip()) {
			eVar30 += "-";
			eVar30 += df.format(searchParams.getReturnDate().getCalendar().getTime());
		}

		eVar30 += ":N";
		s.setEvar(30, eVar30);

		// products variable, described here: http://confluence/display/Omniture/Product+string+format
		String airlineCode = trip.getLeg(0).getPrimaryAirlines().iterator().next();
		String tripType = getOmnitureStringCodeRepresentingTripTypeByNumLegs(trip.getLegCount());
		String numTravelers = Integer.toString(Db.getFlightSearch().getSearchParams().getNumAdults());
		String price = trip.getTotalFare().getAmount().toString();

		s.setProducts("Flight;Agency Flight:" + airlineCode + ":" + tripType + ";" + numTravelers + ";" + price);

		s.setCurrencyCode(trip.getTotalFare().getCurrency());
		s.setEvents("purchase");

		// order number with an "onum" prefix, described here: http://confluence/pages/viewpage.action?pageId=419913476
		final String orderId = Db.getFlightCheckout().getOrderId();
		s.setPurchaseID("onum" + orderId);

		// TRL
		Itinerary itin = Db.getItinerary(trip.getItineraryNumber());
		s.setProp(71, itin.getItineraryNumber());

		// order #
		s.setProp(72, orderId);

		s.track();
	}

	public static void trackPageLoadFlightCheckoutPaymentCid(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_CID, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightCheckoutSlideToPurchase(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_SLIDE_TO_PURCHASE, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_SAVE, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditCard(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_CARD, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightCheckoutPaymentEditAddress(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_EDIT_ADDRESS, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightCheckoutPaymentSelect(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_PAYMENT_SELECT, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightCheckoutWarsaw(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_WARSAW, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightTravelerEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_SAVE, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightTravelerEditPassport(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_PASSPORT, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightTravelerEditDetails(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_DETAILS, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightTravelerEditInfo(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_EDIT_INFO, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightTravelerSelect(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_TRAVELER_SELECT, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightLogin(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_LOGIN, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightCheckoutInfo(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_CHECKOUT_INFO, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightRateDetailsOverview(Context context) {
		internalTrackPageLoadEventPriceChangeAsShopper(context, FLIGHT_RATE_DETAILS);
	}

	private static boolean mTrackNewSearchResultSet;

	/**
	 * This method is used in the client and internally as bookkeeping to ensure that the special events, outbound list
	 * and oneway list of search results get tracked once and only once for a given set of search parameters. Clients
	 * will use this method with 'true' when performing a new search, and internally this method is used with 'false' to
	 * disallow additional tracking events after the first event has been tracked (until explicitly set true by client).
	 *
	 * Most importantly, this keeps the silly Omniture tracking details (mostly) out of client code and in this class.
	 *
	 * @param markTrackNewSearchResultSet
	 */
	public static void markTrackNewSearchResultSet(boolean markTrackNewSearchResultSet) {
		mTrackNewSearchResultSet = markTrackNewSearchResultSet;
	}

	public static void trackPageLoadFlightSearchResults(Context context, int legPosition) {
		if (legPosition == 0) {
			// Note: according the spec we want only to track the FlightSearchResults if it represents a new set of data
			if (mTrackNewSearchResultSet) {
				if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
					OmnitureTracking.trackPageLoadFlightSearchResultsOutboundList(context);
				}
				else {
					OmnitureTracking.trackPageLoadFlightSearchResultsOneWay(context);
				}
			}
		}

		// According to spec, we want to track the inbound list as many times as the user rotates device, etc...
		else if (legPosition == 1) {
			OmnitureTracking.trackPageLoadFlightSearchResultsInboundList(context);
		}
	}

	private static void trackPageLoadFlightSearchResultsOutboundList(Context context) {
		markTrackNewSearchResultSet(false);

		Log.d(TAG, "Tracking \"" + FLIGHT_SEARCH_ROUNDTRIP_OUT + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventStandardAsShopper(context, FLIGHT_SEARCH_ROUNDTRIP_OUT,
				LOB.FLIGHTS);

		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

		// Search Type: value always 'Flight'
		s.setEvar(2, "Flight");
		s.setProp(2, "Flight");

		// Search Origin: 3 letter airport code of origin
		String origin = searchParams.getDepartureLocation().getDestinationId();
		s.setEvar(3, origin);
		s.setProp(3, origin);

		// Search Destination: 3 letter airport code of destination
		String dest = searchParams.getArrivalLocation().getDestinationId();
		s.setEvar(4, dest);
		s.setProp(4, dest);

		// day computation date, TODO test this stuff
		final Calendar departureDate = searchParams.getDepartureDate().getCalendar();
		final Calendar returnDate = searchParams.getReturnDate() == null ? null : searchParams.getReturnDate()
				.getCalendar();
		final Calendar now = Calendar.getInstance();

		// num days between current day (now) and flight departure date
		String numDaysOut = Long.toString(CalendarUtils.getDaysBetween(now, departureDate));
		s.setEvar(5, numDaysOut);
		s.setProp(5, numDaysOut);

		// num days between departure and return dates
		String numDays = Long.toString(CalendarUtils.getDaysBetween(departureDate, returnDate));
		s.setEvar(6, numDays);
		s.setProp(6, numDays);

		s.setEvar(47, getEvar47String(searchParams));

		// Success event for 'Search'
		s.setEvents("event30");

		s.track();
	}

	private static void trackPageLoadFlightSearchResultsInboundList(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN, LOB.FLIGHTS);
	}

	private static void trackPageLoadFlightSearchResultsOneWay(Context context) {
		markTrackNewSearchResultSet(false);

		Log.d(TAG, "Tracking \"" + FLIGHT_SEARCH_RESULTS_ONE_WAY + "\" pageLoad");

		ADMS_Measurement s = createTrackPageLoadEventStandardAsShopper(context, FLIGHT_SEARCH_RESULTS_ONE_WAY,
				LOB.FLIGHTS);

		FlightSearchParams searchParams = Db.getFlightSearch().getSearchParams();

		// Search Type: value always 'Flight'
		s.setEvar(2, "Flight");
		s.setProp(2, "Flight");

		// Search Origin: 3 letter airport code of origin
		String origin = searchParams.getDepartureLocation().getDestinationId();
		s.setEvar(3, origin);
		s.setProp(3, origin);

		// Search Destination: 3 letter airport code of destination
		String dest = searchParams.getArrivalLocation().getDestinationId();
		s.setEvar(4, dest);
		s.setProp(4, dest);

		// day computation date
		final Calendar departureDate = searchParams.getDepartureDate().getCalendar();
		final Calendar now = Calendar.getInstance();

		// num days between current day (now) and flight departure date
		String daysOut = Long.toString(CalendarUtils.getDaysBetween(now, departureDate));
		s.setEvar(5, daysOut);
		s.setProp(5, daysOut);

		s.setEvar(47, getEvar47String(searchParams));

		// Success event for 'Search'
		s.setEvents("event30");

		s.track();
	}

	public static void trackPageLoadFlightBaggageFeeOneWay(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ONE_WAY_BAGGAGE_FEE, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightBaggageFeeOutbound(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_OUTBOUND_BAGGAGE_FEE, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightBaggageFeeInbound(Context context) {
		internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_INBOUND_BAGGAGE_FEE, LOB.FLIGHTS);
	}

	public static void trackPageLoadFlightSearchResultsDetails(Context context, int legPosition) {
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_OUT_DETAILS, LOB.FLIGHTS);
			}
			else {
				internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ONE_WAY_DETAILS, LOB.FLIGHTS);
			}
		}
		else if (legPosition == 1) {
			internalTrackPageLoadEventStandard(context, FLIGHT_SEARCH_ROUNDTRIP_IN_DETAILS, LOB.FLIGHTS);

		}
	}

	public static void trackPageLoadFlightSearchResultsPlaneLoadingFragment(Context context) {
		internalTrackPageLoadEventStandardNoVars25And25LobShopper(context, FLIGHT_SEARCH_INTERSTITIAL);
	}

	public static void trackPageLoadFlightSearch(Context context) {
		internalTrackPageLoadEventStandardNoVars25And25LobShopper(context, FLIGHT_SEARCH);
	}

	public static void trackPageLoadLaunchScreen(Context context) {
		internalTrackPageLoadEventStandardNoVars25And25LobShopper(context, LAUNCH_SCREEN);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Hotels tracking events

	public static void trackPageLoadHotelsRoomsRates(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_ROOMS_RATES, LOB.HOTELS);
	}

	public static void trackPageLoadHotelsRateDetails(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_RATE_DETAILS, LOB.HOTELS);
	}

	public static void trackPageLoadHotelsCheckoutInfo(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_INFO, LOB.HOTELS);
	}

	public static void trackPageLoadHotelsLogin(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_LOGIN, LOB.HOTELS);
	}

	public static void trackLinkHotelRefineName(Context context, String refinement) {
		String link = HOTELS_SEARCH_REFINE_NAME + "." + refinement;
		internalTrackLink(context, link);
	}

	public static void trackLinkHotelRefinePriceRange(Context context, PriceRange priceRange) {
		String link = HOTELS_SEARCH_REFINE_PRICE_RANGE;

		switch (priceRange) {
		case CHEAP: {
			link += ".1$";
			break;
		}
		case MODERATE: {
			link += ".2$";
			break;
		}
		case EXPENSIVE: {
			link += ".3$";
			break;
		}
		case ALL:
		default: {
			link += ".All";
			break;
		}
		}

		internalTrackLink(context, link);
	}

	public static void trackLinkHotelRefineSearchRadius(Context context, SearchRadius searchRadius) {
		String link = HOTELS_SEARCH_REFINE_SEARCH_RADIUS;

		if (searchRadius != Filter.SearchRadius.ALL) {
			final DistanceUnit distanceUnit = DistanceUnit.getDefaultDistanceUnit();
			final String unitString = distanceUnit.equals(DistanceUnit.MILES) ? "mi" : "km";

			link += "." + new DecimalFormat("##.#").format(searchRadius.getRadius(distanceUnit)) + unitString;
		}
		else {
			link += ".All";
		}

		internalTrackLink(context, link);
	}

	public static void trackLinkHotelRefineRating(Context context, String rating) {
		String link = HOTELS_SEARCH_REFINE + "." + rating;
		internalTrackLink(context, link);
	}

	public static final String HOTELS_SEARCH_SORT_POPULAR = "App.Hotels.Search.Sort.Popular";
	public static final String HOTELS_SEARCH_SORT_PRICE = "App.Hotels.Search.Sort.Price";
	public static final String HOTELS_SEARCH_SORT_DISTANCE = "App.Hotels.Search.Sort.Distance";
	public static final String HOTELS_SEARCH_SORT_RATING = "App.Hotels.Search.Sort.Rating";
	public static final String HOTELS_SEARCH_SORT_DEALS = "App.Hotels.Search.Sort.Deals";

	public static void trackLinkHotelSort(Context context, String pageName) {
		internalTrackLink(context, pageName);
	}

	// Login

	public static void trackLinkHotelsCheckoutLoginSuccess(Context context) {
		Log.d(TAG, "Tracking \"" + HOTELS_CHECKOUT_LOGIN_SUCCESS + "\" linkClick");

		ADMS_Measurement s = createTrackLinkEvent(context, HOTELS_CHECKOUT_LOGIN_SUCCESS);

		s.setEvents("event26");

		s.trackLink(null, "o", s.getEvar(28), null, null);
	}

	public static void trackLinkHotelsCheckoutLoginForgot(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_LOGIN_FORGOT);
	}

	// Travelers

	public static void trackPageLoadHotelsTravelerEditInfo(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_TRAVELER_EDIT_INFO, LOB.HOTELS);
	}

	public static void trackPageLoadHotelsTravelerSelect(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_TRAVELER_SELECT, LOB.HOTELS);
	}

	public static void trackLinkHotelsCheckoutTravelerEnterManually(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_TRAVELER_ENTER_MANUALLY);
	}

	// Payment

	public static void trackPageLoadHotelsCheckoutPaymentSelect(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_SELECT, LOB.HOTELS);
	}

	public static void trackPageLoadHotelsCheckoutPaymentEditCard(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_EDIT_CARD, LOB.HOTELS);
	}

	public static void trackPageLoadHotelsCheckoutPaymentEditSave(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_EDIT_SAVE, LOB.HOTELS);
	}

	public static void trackLinkHotelsCheckoutPaymentSelectExisting(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_PAYMENT_SELECT_EXISTING);
	}

	public static void trackLinkHotelsCheckoutPaymentEnterManually(Context context) {
		internalTrackLink(context, HOTELS_CHECKOUT_PAYMENT_ENTER_MANUALLY);
	}

	// Overview

	public static void trackPageLoadHotelsCheckoutSlideToPurchase(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_SLIDE_TO_PURCHASE, LOB.HOTELS);
	}

	// Rules

	public static void trackPageLoadHotelsCheckoutWarsaw(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_WARSAW, LOB.HOTELS);
	}

	// CVV Checkout

	public static void trackPageLoadHotelsCheckoutPaymentCid(Context context) {
		internalTrackPageLoadEventStandard(context, HOTELS_CHECKOUT_PAYMENT_CID, LOB.HOTELS);
	}

	public static void trackCrash(Context context, Throwable ex) {
		// Log the crash
		Log.d(TAG, "Tracking \"crash\" onClick");
		ADMS_Measurement s = getFreshTrackingObject(context);
		TrackingUtils.addStandardFields(context, s);
		s.setEvents("event39");
		s.setEvar(28, "App.Crash");
		s.setProp(16, "App.Crash");

		final Writer writer = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		s.setProp(36, ex.getMessage() + "|" + writer.toString());

		Log.i("prop36: " + s.getProp(36));

		TrackingUtils.trackOnClick(s);
	}

	public static void trackPageLoadHotelsInfosite(Context context, int position) {
		Log.d(TAG, "Tracking \"App.Hotels.Infosite\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.Infosite");

		s.setEvents("event32");

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		// Rating or highly rated
		Property property = Db.getSelectedProperty();
		TrackingUtils.addHotelRating(s, property);

		// Products
		TrackingUtils.addProducts(s, property);

		// Position, if opened from list

		if (position != -1) {
			s.setEvar(39, position + "");
		}

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelsInfositeMap(Context context) {
		Log.d(TAG, "Tracking \"App.Hotels.Infosite.Map\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.Infosite.Map");

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		// Products
		TrackingUtils.addProducts(s, Db.getSelectedProperty());

		// Send the tracking data
		s.track();
	}

	public static void trackPageLoadHotelDetails(Context context, Property property) {
		// Track that the full details has a pageload
		Log.d(TAG, "Tracking \"App.Hotels.Details\" pageLoad");

		ADMS_Measurement s = TrackingUtils
				.createSimpleEvent(context, "App.Hotels.Details", "event32", "Shopper", null);

		TrackingUtils.addHotelRating(s, property);

		s.setEvar(8, property.getLowestRate().getPromoDescription());

		s.track();
	}

	public static void trackPageLoadHotelsSearchQuickView(Context context, Property property, String referrer) {
		// Track that the mini details has a pageload
		Log.d(TAG, "Tracking \"App.Hotels.Search.QuickView\" onClick");

		ADMS_Measurement s = TrackingUtils.createSimpleEvent(context, "App.Hotels.Search.QuickView", null, "Shopper",
				referrer);

		s.setEvar(8, property.getLowestRate().getPromoDescription());

		s.track();
	}

	public static void trackAppLaunch(Context context, String id, String date) {
		Log.d(TAG, "Tracking \"App Launch\" pageLoad");
		ADMS_Measurement s = getFreshTrackingObject(context);
		s.setVisitorID(id);
		s.setEvar(7, id);
		s.setEvar(10, date);
		s.setEvar(27, "App Launch");
		s.track();
	}

	public static void trackAppInstallCustom(Context context) {
		Log.d(TAG, "Tracking \"App Install\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		String marketingDate = FORMATTER.format(new Date());
		String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

		SettingUtils.save(context, context.getString(R.string.preference_amobee_marketing_date), marketingDate);

		s.setVisitorID(androidId);
		s.setEvar(7, androidId);
		s.setEvar(10, marketingDate);
		s.setEvar(28, "App Install");

		s.track();
	}

	public static void trackGooglePlayReferralLink(Context context, Intent intent) {
		ADMS_ReferrerHandler handler = new ADMS_ReferrerHandler();
		handler.processIntent(context, intent);
	}

	public static void trackAppHotelsSearch(Context context, SearchParams searchParams, SearchResponse searchResponse,
			String refinements) {
		// Start actually tracking the search result change
		Log.d(TAG, "Tracking \"App.Hotels.Search\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.Search");

		if (refinements != null) {
			// Whether this was the first search or a refined search
			s.setEvents("event31");

			// Refinement
			s.setEvar(28, refinements);
			s.setProp(16, refinements);
		}
		else {
			s.setEvents("event30");
		}

		// LOB Search
		s.setEvar(2, "hotels");
		s.setProp(2, "hotels");

		// Region
		DecimalFormat df = new DecimalFormat("#.######");
		String region = null;
		if (!TextUtils.isEmpty(searchParams.getQuery())) {
			region = searchParams.getQuery();
		}
		else {
			region = df.format(searchParams.getSearchLatitude()) + "|" + df.format(searchParams.getSearchLongitude());
		}
		s.setEvar(4, region);
		s.setProp(4, region);

		// Check in/check out date
		String days5 = CalendarUtils.getDaysBetween(Calendar.getInstance(), searchParams.getCheckInDate()) + "";
		s.setEvar(5, days5);
		s.setProp(5, days5);

		String days6 = CalendarUtils.getDaysBetween(searchParams.getCheckInDate(), searchParams.getCheckOutDate()) + "";
		s.setEvar(6, days6);
		s.setProp(6, days6);

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		// Number adults searched for
		s.setEvar(47, "A" + searchParams.getNumAdults() + "|C" + searchParams.getNumChildren());

		// Freeform location
		if (!TextUtils.isEmpty(searchParams.getUserQuery())) {
			s.setEvar(48, searchParams.getUserQuery());
		}

		// Number of search results
		if (searchResponse != null && searchResponse.getFilteredAndSortedProperties() != null) {
			s.setProp(1, searchResponse.getFilteredAndSortedProperties().length + "");
		}

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsRoomsRates(Context context, Property property, String referrer) {
		Log.d(TAG, "Tracking \"App.Hotels.RoomsRates\" event");

		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.RoomsRates");

		// Promo description
		s.setEvar(9, property.getLowestRate().getPromoDescription());

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		// Rating or highly rated
		TrackingUtils.addHotelRating(s, property);

		// Products
		TrackingUtils.addProducts(s, property);

		// Referrer (determines whether a specific rate was clicked into here or not - only applicable for
		// the tablet version of the UI).
		// TODO: referrer has been moved in version 3.x of the library, original line s.referrer = referrer;

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutPayment(Context context, Property property,
			BookingInfoValidation validation) {
		Log.d(TAG, "Tracking \"App.Hotels.Checkout.Payment\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.Checkout.Payment");

		s.setEvents("event34");

		// Shopper/Confirmer
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		// Products
		TrackingUtils.addProducts(s, property);

		// If any sections were already complete, fill them in here
		String referrerId = null;
		if (validation.isGuestsSectionCompleted() && validation.isBillingSectionCompleted()) {
			referrerId = "CKO.BD.CompletedGuestInfo|CKO.BD.CompletedBillingInfo";
		}
		else if (validation.isGuestsSectionCompleted()) {
			referrerId = "CKO.BD.CompletedGuestInfo";
		}
		else if (validation.isBillingSectionCompleted()) {
			referrerId = "CKO.BD.CompletedBillingInfo";
		}

		s.setEvar(28, referrerId);
		s.setProp(16, referrerId);

		// Send the tracking data
		s.track();
	}

	public static void trackAppHotelsCheckoutConfirmation(Context context, SearchParams searchParams,
			Property property, BillingInfo billingInfo, Rate rate, BookingResponse response) {
		Log.d(TAG, "Tracking \"App.Hotels.Checkout.Confirmation\" pageLoad");

		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Hotels.Checkout.Confirmation");

		s.setEvents("purchase");

		// Promo description
		if (rate != null) {
			s.setEvar(9, rate.getPromoDescription());
		}

		// Shopper/Confirmer
		s.setEvar(25, "Confirmer");
		s.setProp(25, "Confirmer");

		// Product details
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		String checkIn = df.format(searchParams.getCheckInDate().getTime());
		String checkOut = df.format(searchParams.getCheckOutDate().getTime());
		s.setEvar(30, "Hotel:" + checkIn + "-" + checkOut + ":N");

		// Unique confirmation id
		// 14103: Remove timestamp from the purchaseID variable
		s.setProp(15, response.getItineraryId());
		s.setPurchaseID(response.getItineraryId());

		if (billingInfo != null) {
			Location location = billingInfo.getLocation();
			// Not all POS need a location so it is null in some cases
			if (location != null) {
				s.setProp(46, location.getCountryCode());
				s.setGeoState(location.getCountryCode());

				s.setProp(49, location.getPostalCode());
				s.setGeoZip(location.getPostalCode());
			}
		}

		// Products
		int numDays = searchParams.getStayDuration();
		double totalCost = 0;
		if (rate != null && rate.getTotalAmountAfterTax() != null) {
			totalCost = rate.getTotalAmountAfterTax().getAmount().doubleValue();
		}
		TrackingUtils.addProducts(s, property, numDays, totalCost);

		// Currency code
		s.setCurrencyCode(rate.getTotalAmountAfterTax().getCurrency());

		// Send the tracking data
		s.track();
	}

	public static void trackViewOnMap(Context context) {
		Log.d(TAG, "Tracking \"CKO.CP.ViewInMaps\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.CP.ViewInMaps");
	}

	public static void trackNewSearch(Context context) {
		Log.d(TAG, "Tracking \"new search\" onClick");
		TrackingUtils.trackSimpleEvent(context, null, null, "Shopper", "CKO.CP.StartNewSearch");
	}

	public static void trackAppLoading(Context context) {
		Log.d(TAG, "Tracking \"App.Loading\" pageLoad...");

		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		s.setAppState("App.Loading");

		// Determine if this is a new install, an upgrade, or just a regular launch
		String trackVersion = SettingUtils.get(context, TRACK_VERSION, null);
		String currentVersion = AndroidUtils.getAppVersion(context);

		boolean save = false;
		if (trackVersion == null) {
			// New install
			s.setEvents("event28");
			save = true;
		}
		else if (!trackVersion.equals(currentVersion)) {
			// App was upgraded
			s.setEvents("event29");
			save = true;
		}
		else {
			// Regular launch
			s.setEvents("event27");
		}

		if (save) {
			// Save new data
			SettingUtils.save(context, TRACK_VERSION, currentVersion);
		}

		// Send the tracking data
		s.track();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Private helper methods

	/**
	 * Method that returns a tracking object without any parameters set to be manipulating for tracking. Because the 
	 * library uses a static object for tracking, clearVars() must be called in order to remove potentially set
	 * variables from being erroneously sent along with the tracking call.
	 */
	public static ADMS_Measurement getFreshTrackingObject(Context context) {
		ADMS_Measurement a = ADMS_Measurement.sharedInstance(context);
		a.clearVars();
		return a;
	}

	private static void internalTrackPageLoadEventStandard(Context context, String pageName, LOB lob) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventStandardAsShopper(context, pageName, lob).track();
	}

	private static void internalTrackPageLoadEventStandardNoVars25And25LobShopper(Context context, String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventBase(context, pageName).track();
	}

	private static void internalTrackPageLoadEventPriceChange(Context context, String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventPriceChange(context, pageName).track();
	}

	private static void internalTrackPageLoadEventPriceChangeAsShopper(Context context, String pageName) {
		Log.d(TAG, "Tracking \"" + pageName + "\" pageLoad");
		createTrackPageLoadEventPriceChangeAsShopper(context, pageName).track();
	}

	private static void internalTrackLink(Context context, String link) {
		Log.d(TAG, "Tracking \"" + link + "\" linkClick");
		ADMS_Measurement s = createTrackLinkEvent(context, link);
		s.trackLink(null, "o", s.getEvar(28), null, null);
	}

	private static ADMS_Measurement createTrackLinkEvent(Context context, String link) {
		ADMS_Measurement s = getFreshTrackingObject(context);

		TrackingUtils.addStandardFields(context, s);

		// link
		s.setEvar(28, link);
		s.setProp(16, link);

		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventBase(Context context, String pageName) {
		ADMS_Measurement s = getFreshTrackingObject(context);

		// set the pageName
		s.setAppState(pageName);
		s.setEvar(18, pageName);

		TrackingUtils.addStandardFields(context, s);

		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventStandardAsShopper(Context context, String pageName, LOB lob) {
		ADMS_Measurement s = createTrackPageLoadEventBase(context, pageName);
		addVars25And26LobAsShopper(s, lob);
		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventPriceChangeAsShopper(Context context, String pageName) {
		ADMS_Measurement s = createTrackPageLoadEventPriceChange(context, pageName);
		addVars25And26LobAsShopper(s, LOB.FLIGHTS);
		return s;
	}

	private static ADMS_Measurement createTrackPageLoadEventPriceChange(Context context, String pageName) {
		ADMS_Measurement s = createTrackPageLoadEventBase(context, pageName);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		// This is only to be included when there is a price change shown on the page. This should be the % increase or
		// decrease in price. Round to whole integers.
		String priceChange = trip.computePercentagePriceChangeForOmnitureTracking();
		if (priceChange != null) {
			s.setEvents("event62");
			s.setProp(9, priceChange);
		}

		return s;
	}

	// Note: The following addVars methods are intended to be used only from within the private, internal event create
	// methods found above, NOT to be used from within the public methods used in the business logic. If you find
	// yourself wanting to use these methods in a new public event method, think about creating an internal method that
	// uses these methods.

	private static ADMS_Measurement addVars25And26LobAsShopper(ADMS_Measurement s, LOB lob) {
		s.setEvar(25, "Shopper");
		s.setProp(25, "Shopper");

		switch (lob) {
		case FLIGHTS:
			s.setEvar(26, "FLT Shopper");
			s.setProp(26, "FLT Shopper");
			break;
		case HOTELS:
			s.setEvar(26, "HOT Shopper");
			s.setProp(26, "HOT Shopper");
			break;
		}
		return s;
	}

	private static ADMS_Measurement addVars25And26LobAsConfirmer(ADMS_Measurement s) {
		s.setEvar(25, "Confirmer");
		s.setProp(25, "Confirmer");
		s.setEvar(26, "CKO Shopper");
		s.setProp(26, "CKO Shopper");
		return s;
	}

	// This method will eventually become more useful when we support multi-destination flights
	private static String getOmnitureStringCodeRepresentingTripTypeByNumLegs(final int numLegs) {
		switch (numLegs) {
		case 1:
			return "OW";
		case 2:
			return "RT";
		default:
			return "MD";
		}
	}

	private static String getEvar47String(FlightSearchParams params) {
		// Pipe delimited list of LOB, flight search type (OW, RT, MD), # of Adults, and # of Children)
		// e.g. FLT|RT|A2|C1
		// TODO update for when we add children support
		String str = "FLT|";
		if (params.isRoundTrip()) {
			str += "RT|A";
		}
		else {
			str += "OW|A";
		}

		str += params.getNumAdults();
		str += "|C0";

		return str;
	}
}
