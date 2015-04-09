package com.expedia.bookings.otto;

import java.util.List;

import org.joda.time.LocalDate;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LaunchLocation;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.data.WeeklyFlightHistogram;
import com.expedia.bookings.data.cars.ApiError;
import com.expedia.bookings.data.cars.CarCheckoutParamsBuilder;
import com.expedia.bookings.data.cars.CarCheckoutResponse;
import com.expedia.bookings.data.cars.CarCreateTripResponse;
import com.expedia.bookings.data.cars.CarSearch;
import com.expedia.bookings.data.cars.CarSearchParams;
import com.expedia.bookings.data.cars.CategorizedCarOffers;
import com.expedia.bookings.data.cars.CreateTripCarOffer;
import com.expedia.bookings.data.cars.SearchCarOffer;
import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.data.lx.LXCheckoutParamsBuilder;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchParams;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.enums.ResultsSearchState;
import com.mobiata.android.Log;
import com.squareup.otto.Bus;

/**
 * This singleton class houses a list of all the events currently supported on the Otto Event Bus.
 * <p>
 * Otto is a Guava-based event bus system that uses annotations and code generation.
 * Basically, it saves us from needing to create a bunch of interfaces and register them between various parts of our code.
 * Instead, all we need to do is register with the singleton bus, and subscribe to events.
 * </p>
 * <p>
 * For more information on Otto https://github.com/square/otto
 * </p>
 * <p>
 * <pre>
 * <code>
 * //To publish a new event, call the post method
 * bus.post(new Event());
 *
 * //To listen for the event published
 * (annotate)Subscribe
 * public void listenToEvent(Event event) {
 * // Do something
 * }
 *
 * //In order to receive events, a class instance needs to register with the bus.
 * //Best practice to register in the onResume() lifecycle event.
 * bus.register(this);
 *
 * //In order to unregister from the event bus,
 * bus.unregister(this);
 *
 * <strong>Note:</strong>
 * Always unregister in the onPause() to ensure that events are not consumed when not needed.
 * </code>
 * </p>
 */
public class Events {

	private static final String TAG = "ExpediaOtto";
	private static final Bus sBus = new BetterBus();

	private Events() {
		// Singleton - Cannot be instantiated
	}

	public static void register(Object obj) {
		Log.v(TAG, "Registering: " + obj);
		sBus.register(obj);
	}

	public static void unregister(Object obj) {
		Log.v(TAG, "Unregistering: " + obj);
		sBus.unregister(obj);
	}

	public static void post(Object obj) {
		Log.v(TAG, "Posting event: " + obj);
		sBus.post(obj);
	}

	private static class BetterBus extends Bus {
		private static final Handler mHandler = new Handler(Looper.getMainLooper());

		@Override
		public void register(final Object listener) {
			if (Looper.myLooper() == Looper.getMainLooper()) {
				BetterBus.super.register(listener);
			}
			else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						BetterBus.super.register(listener);
					}
				});
			}
		}

		@Override
		public void unregister(final Object listener) {
			if (Looper.myLooper() == Looper.getMainLooper()) {
				BetterBus.super.unregister(listener);
			}
			else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						BetterBus.super.unregister(listener);
					}
				});
			}
		}

		@Override
		public void post(final Object event) {
			if (Looper.myLooper() == Looper.getMainLooper()) {
				BetterBus.super.post(event);
			}
			else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						BetterBus.super.post(event);
					}
				});
			}
		}
	}

	/////////////////////////////////////////////////////////
	/// TabletLaunchActivity related

	public static class LaunchCollectionsAvailable {
		public List<LaunchCollection> collections;
		public LaunchCollection selectedCollection;
		public LaunchLocation selectedLocation;

		public LaunchCollectionsAvailable(List<LaunchCollection> collections, LaunchCollection selectedCollection,
			LaunchLocation selectedLocation) {
			this.collections = collections;
			this.selectedCollection = selectedCollection;
			this.selectedLocation = selectedLocation;
		}
	}

	public static class LaunchCollectionClicked {
		public final LaunchCollection launchCollection;

		public LaunchCollectionClicked(LaunchCollection launchCollection) {
			this.launchCollection = launchCollection;
		}
	}

	public static class LaunchMapPinClicked {
		public final LaunchLocation launchLocation;

		public LaunchMapPinClicked(LaunchLocation launchLocation) {
			this.launchLocation = launchLocation;
		}
	}

	/////////////////////////////////////////////////////////
	/// HotelBookingFragment related

	/**
	 * This event notifies that the rate of the selected room has gone up after CreateTripDownload call has succeeded.
	 */
	public static class HotelProductRateUp {
		public final Rate newRate;

		public HotelProductRateUp(Rate r) {
			this.newRate = r;
		}
	}

	/**
	 * This event notifies CreateTripDownload call has succeeded
	 */
	public static class CreateTripDownloadSuccess {
		public final Response createTripResponse;

		public CreateTripDownloadSuccess(Response response) {
			this.createTripResponse = response;
		}
	}

	/**
	 * This event notifies CreateTripDownload call completed with an error
	 */
	public static class CreateTripDownloadError {
		private LineOfBusiness mLineOfBusiness;
		private ServerError mServerError;

		public CreateTripDownloadError(ServerError serverError) {
			this.mServerError = serverError;
		}

		public CreateTripDownloadError(LineOfBusiness lob, ServerError error) {
			mLineOfBusiness = lob;
			mServerError = error;
		}

		public LineOfBusiness getLob() {
			return mLineOfBusiness;
		}

		public ServerError getServerError() {
			return mServerError;
		}
	}

	/**
	 * This event notifies that the user has requested we retry our CreateTripDownload
	 */
	public static class CreateTripDownloadRetry {
	}

	/**
	 * This event notifies that the user does not want to retry the CreateTripDownload
	 */
	public static class CreateTripDownloadRetryCancel {
	}

	/**
	 * This event notifies CouponApplyDownload call has succeeded
	 */
	public static class CouponApplyDownloadSuccess {
		public final Rate newRate;

		public CouponApplyDownloadSuccess(Rate r) {
			this.newRate = r;
		}
	}

	/**
	 * This event notifies CouponRemoveDownload call has succeeded
	 */
	public static class CouponRemoveDownloadSuccess {
		public final Rate rate;

		public CouponRemoveDownloadSuccess(Rate r) {
			this.rate = r;
		}
	}

	/**
	 * This event notifies that user has requested to cancel applying the coupon
	 */
	public static class CouponDownloadCancel {
	}

	/**
	 * This event notifies that the CouponDownload call completed with an error
	 */
	public static class CouponDownloadError {

	}

	/**
	 * This event notifies when Booking Download has started
	 */
	public static class BookingDownloadStarted {

	}

	/**
	 * This event notifies that BookingDownload call has succeeded
	 */
	public static class BookingDownloadResponse {
		public final Response response;

		public BookingDownloadResponse(Response r) {
			this.response = r;
		}
	}

	/**
	 * This event notifies that BookingDownload call finished with a CVV error
	 */
	public static class BookingResponseErrorCVV {
		public final boolean setCVVMode;

		public BookingResponseErrorCVV(boolean isSetCVVMode) {
			this.setCVVMode = isSetCVVMode;
		}
	}

	/**
	 * This event notifies that BookingDownload call finished with a Trip already booked error
	 */
	public static class BookingResponseErrorTripBooked {
	}

	/**
	 * This event notifies that user clicked the Ok button of {@link SimpleCallbackDialogFragment}.
	 * It is primarily used for showing booking errors.
	 */

	public static class SimpleCallBackDialogOnClick {
		public final int callBackId;

		public SimpleCallBackDialogOnClick(int id) {
			this.callBackId = id;
		}
	}

	/**
	 * This event notifies that {@link SimpleCallbackDialogFragment} has been cancelled by the user.
	 * It is primarily used for showing booking errors.
	 */
	public static class SimpleCallBackDialogOnCancel {
		public final int callBackId;

		public SimpleCallBackDialogOnCancel(int id) {
			this.callBackId = id;
		}
	}

	/**
	 * This event notifies that the user clicked the OK button for {@link BirthDateInvalidDialog }
	 */
	public static class BirthDateInvalidEditTraveler {
	}

	/**
	 * This event notifies that the user clicked the Edit Search button for {@link BirthDateInvalidDialog }
	 */
	public static class BirthDateInvalidEditSearch {
	}

	/**
	 * This event notifies that the "No internet connection" dialog should be shown.
	 */
	public static class ShowNoInternetDialog {
		public final int callBackId;

		public ShowNoInternetDialog(int callBackId) {
			this.callBackId = callBackId;
		}
	}

	/**
	 * This event notifies that the user clicked the Accept button for {@link PriceChangeDialogFragment}
	 */
	public static class PriceChangeDialogAccept {
	}

	/**
	 * This event notifies that the user clicked the Cancel button for {@link PriceChangeDialogFragment}
	 */
	public static class PriceChangeDialogCancel {
	}

	/**
	 * This event notifies that the user clicked the Retry button for {@link UnhandledErrorDialogFragment}
	 */
	public static class UnhandledErrorDialogRetry {
	}

	/**
	 * This event notifies that the user clicked the CallCustomerSupport button for {@link UnhandledErrorDialogFragment}
	 */
	public static class UnhandledErrorDialogCallCustomerSupport {
	}

	/**
	 * This event notifies that the user clicked the Cancel button for {@link UnhandledErrorDialogFragment}
	 */
	public static class UnhandledErrorDialogCancel {
	}

	/**
	 * This event notifies that the user clicked the Book Next Item button in the Confirmation Screen.
	 */
	public static class BookingConfirmationBookNext {
		public final LineOfBusiness nextItem;

		public BookingConfirmationBookNext(LineOfBusiness item) {
			this.nextItem = item;
		}
	}

	/**
	 * This event notifies that the Flight createTrip succeeded with a price change.
	 */
	public static class FlightPriceChange {
	}

	/**
	 * This event notifies that the trip selected in the bucket is unavailable to checkout.
	 */
	public static class BookingUnavailable {
		public final LineOfBusiness lineOfBusiness;

		public BookingUnavailable(LineOfBusiness lob) {
			this.lineOfBusiness = lob;
		}
	}

	/**
	 * This event notifies that the item in the trip bucket has expired.
	 */
	public static class TripItemExpired {

		public final LineOfBusiness lineOfBusiness;

		public TripItemExpired(LineOfBusiness lob) {
			this.lineOfBusiness = lob;
		}
	}

	public static class TripBucketHasRedeyeItems {
		// ignore
	}

	public static class TripBucketHasMismatchedItems {
		// ignore
	}

	/**
	 * This event notifies that the LCC payment fees was added to the flight trip.
	 */
	public static class LCCPaymentFeesAdded {
	}

	// Suggestion query events
	public static class SuggestionQueryStarted {
	}

	public static class SuggestionResultsDelivered {
	}

	// HotelSearch update events
	public static class HotelAvailabilityUpdated {
	}

	public static class HotelRateSelected {
	}

	public static class SearchSuggestionSelected {
		public final SuggestionV2 suggestion;
		public final boolean isFromSavedParamsAndBucket;

		public SearchSuggestionSelected(SuggestionV2 suggestion) {
			this.suggestion = suggestion;
			this.isFromSavedParamsAndBucket = false;
		}

		public SearchSuggestionSelected(SuggestionV2 suggestion, boolean fromSavedParams) {
			this.suggestion = suggestion;
			this.isFromSavedParamsAndBucket = fromSavedParams;
		}
	}

	public static class GdeItemSelected {
		public final WeeklyFlightHistogram week;

		public GdeItemSelected(WeeklyFlightHistogram week) {
			this.week = week;
		}
	}

	public static class GdeDataAvailable {
		public final FlightSearchHistogramResponse response;

		public GdeDataAvailable(FlightSearchHistogramResponse response) {
			this.response = response;
		}
	}

	/**
	 * This event is used to trigger the search fragment to show up.
	 */
	public static class ShowSearchFragment {
		public final ResultsSearchState searchState;

		public ShowSearchFragment(ResultsSearchState state) {
			searchState = state;
		}
	}

	public static class FlightSearchResponseAvailable {
		public final FlightSearchResponse response;

		public FlightSearchResponseAvailable(FlightSearchResponse response) {
			this.response = response;
		}
	}

	public static class HotelSearchResponseAvailable {
		public final HotelSearchResponse response;

		public HotelSearchResponseAvailable(HotelSearchResponse response) {
			this.response = response;
		}
	}

	public static class HotelOffersResponseAvailable {
		public final HotelOffersResponse response;

		public HotelOffersResponseAvailable(HotelOffersResponse response) {
			this.response = response;
		}
	}

	public static class UserClickedSelectDatesButton {
	}

	/**
	 * Cars cars cars
	 */

	public static class CarsPriceChange {
		// ignore
	}

	public static class CarsSessionTimeout {
		// ignore
	}

	public static class CarsPaymentFailed {
		// ignore
	}

	public static class CarsSearchFailed {
		// ignore
	}

	public static class CarsInvalidInput {
		public String field;

		public CarsInvalidInput(String field) {
			this.field = field;
		}
	}

	public static class CarsGoToSearch {
		// ignore
	}

	public static class CarsGoToOverlay {
		// ignore
	}

	public static class CarsNewSearchParams {
		public CarSearchParams carSearchParams;

		public CarsNewSearchParams(CarSearchParams params) {
			carSearchParams = params;
		}
	}

	public static class CarsShowLoadingAnimation {
	}

	public static class CarsShowSearchResults {
		public CarSearch results;

		public CarsShowSearchResults(CarSearch results) {
			this.results = results;
		}
	}

	public static class CarsShowSearchResultsError {
		public ApiError error;

		public CarsShowSearchResultsError(ApiError error) {
			this.error = error;
		}
	}

	public static class CarsShowDetails {
		public CategorizedCarOffers categorizedCarOffers;

		public CarsShowDetails(CategorizedCarOffers offers) {
			categorizedCarOffers = offers;
		}
	}

	public static class CarsShowCheckoutAfterPriceChange {
		public CreateTripCarOffer originalCreateTripOffer;
		public CreateTripCarOffer newCreateTripOffer;
		public String tripId;

		public CarsShowCheckoutAfterPriceChange(CreateTripCarOffer originalOffer, CreateTripCarOffer newOffer, String tripId) {
			this.originalCreateTripOffer = originalOffer;
			this.newCreateTripOffer = newOffer;
			this.tripId = tripId;
		}
	}

	public static class CarsShowCheckout {
		public CarCreateTripResponse createTripResponse;

		public CarsShowCheckout(CarCreateTripResponse createTripResponse) {
			this.createTripResponse = createTripResponse;
		}
	}

	public static class ShowCVV {
		public BillingInfo billingInfo;

		public ShowCVV(BillingInfo info) {
			this.billingInfo = info;
		}
	}

	public static class CarsKickOffSearchCall {
		public CarSearchParams carSearchParams;

		public CarsKickOffSearchCall(CarSearchParams params) {
			this.carSearchParams = params;
		}
	}

	public static class CarsKickOffCreateTrip {
		public SearchCarOffer offer;

		public CarsKickOffCreateTrip(SearchCarOffer offer) {
			this.offer = offer;
		}
	}

	public static class CarsKickOffCheckoutCall {
		public CarCheckoutParamsBuilder checkoutParamsBuilder;

		public CarsKickOffCheckoutCall(CarCheckoutParamsBuilder checkoutParamsBuilder) {
			this.checkoutParamsBuilder = checkoutParamsBuilder;
		}
	}

	public static class CarsShowConfirmation {
		public CarCheckoutResponse checkoutResponse;

		public CarsShowConfirmation(CarCheckoutResponse checkoutResponse) {
			this.checkoutResponse = checkoutResponse;
		}
	}

	public static class FinishActivity {

	}

	public static class LXShowSearchWidget {
		// ignore
	}

	public static class LXNewSearchParamsAvailable {
		public LXSearchParams lxSearchParams;

		public LXNewSearchParamsAvailable(LXSearchParams params) {
			lxSearchParams = params;
		}

		public LXNewSearchParamsAvailable(String locationName, LocalDate startDate, LocalDate endDate, SearchType searchType) {
			lxSearchParams = new LXSearchParams().location(locationName).startDate(startDate).endDate(endDate).searchType(searchType);
		}

		public LXNewSearchParamsAvailable(String locationName, LocalDate startDate, LocalDate endDate) {
			this(locationName, startDate, endDate, SearchType.EXPLICIT_SEARCH);
		}
	}

	public static class LXSearchResultsAvailable {
		public LXSearchResponse lxSearchResponse;

		public LXSearchResultsAvailable(LXSearchResponse lxSearchResponse) {
			this.lxSearchResponse = lxSearchResponse;
		}
	}

	public static class LXFilterCategoryCheckedChanged {
		public LXCategoryMetadata lxCategoryMetadata;
		public String categoryKey;

		public LXFilterCategoryCheckedChanged(LXCategoryMetadata lxCategoryMetadata, String categoryKey) {
			this.lxCategoryMetadata = lxCategoryMetadata;
			this.categoryKey = categoryKey;
		}
	}

	public static class LXShowSearchError {
		public ApiError error;
		public SearchType searchType;

		public LXShowSearchError(ApiError error, SearchType searchType) {
			this.error = error;
			this.searchType = searchType;
		}
	}

	public static class LXActivitySelected {
		public LXActivity lxActivity;

		public LXActivitySelected(LXActivity lxActivity) {
			this.lxActivity = lxActivity;
		}
	}

	public static class LXShowDetails {
		public ActivityDetailsResponse activityDetails;

		public LXShowDetails(ActivityDetailsResponse activityDetails) {
			this.activityDetails = activityDetails;
		}
	}

	public static class LXCreateTripSucceeded {
		public LXCreateTripResponse createTripResponse;

		public LXCreateTripSucceeded(LXCreateTripResponse createTripResponse) {
			this.createTripResponse = createTripResponse;
		}
	}

	public static class LXTicketCountChanged {
		public Ticket ticket;
		public String offerId;

		public LXTicketCountChanged(Ticket ticket, String offerId) {
			this.ticket = ticket;
			this.offerId = offerId;
		}
	}

	public static class LXOfferExpanded {
		public Offer offer;

		public LXOfferExpanded(Offer offer) {
			this.offer = offer;
		}
	}

	public static class LXOfferBooked {
		public Offer offer;
		public List<Ticket> selectedTickets;

		public LXOfferBooked(Offer offer, List<Ticket> selectedTickets) {
			this.offer = offer;
			this.selectedTickets = selectedTickets;
		}
	}

	public static class LXKickOffCheckoutCall {
		public LXCheckoutParamsBuilder checkoutParamsBuilder;

		public LXKickOffCheckoutCall(LXCheckoutParamsBuilder checkoutParamsBuilder) {
			this.checkoutParamsBuilder = checkoutParamsBuilder;
		}
	}

	public static class LXCheckoutSucceeded {
		public LXCheckoutResponse checkoutResponse;

		public LXCheckoutSucceeded(LXCheckoutResponse checkoutResponse) {
			this.checkoutResponse = checkoutResponse;
		}
	}

	public static class LXDetailsDateChanged {
		public LocalDate dateSelected;

		public LXDetailsDateChanged(LocalDate dateSelected) {
			this.dateSelected = dateSelected;
		}
	}

	public static class LXSearchParamsOverlay {
		// ignore
	}

	public static class LXShowLoadingAnimation {
		// ignore
	}

	public static class LXInvalidInput {
		public String field;

		public LXInvalidInput(String field) {
			this.field = field;
		}
	}

	public static class LXSessionTimeout {
		// ignore
	}

	public static class LXPaymentFailed {
		// ignore
	}
	// Launch screen

	public static class LaunchHotelSearchResponse {
		public List<Hotel> topHotels;

		public LaunchHotelSearchResponse(List<Hotel> topHotels) {
			this.topHotels = topHotels;
		}
	}

	public static class LaunchListItemSelected {
		public Hotel offer;

		public LaunchListItemSelected(Hotel offer) {
			this.offer = offer;
		}
	}

	public static class LaunchSeeAllButtonPressed {
		public Bundle animOptions;

		public LaunchSeeAllButtonPressed(Bundle animOptions) {
			this.animOptions = animOptions;
		}
	}

	public static class LaunchLocationFetchComplete {
		public final Location location;

		public LaunchLocationFetchComplete(Location location) {
			this.location = location;
		}
	}

	public static class LaunchLocationFetchError {
	}

	public static class CollectionDownloadComplete {
		public Collection collection;

		public CollectionDownloadComplete(Collection collection) {
			this.collection = collection;
		}
	}

	public static class LaunchCollectionItemSelected {
		public CollectionLocation collectionLocation;
		public Bundle animOptions;

		public LaunchCollectionItemSelected(CollectionLocation location, Bundle animOptions) {
			this.collectionLocation = location;
			this.animOptions = animOptions;
		}
	}

	public static class LaunchOnlineState {
	}

	public static class LaunchOfflineState {
	}

	// Launch screen -- air attach

	public static class LaunchAirAttachBannerHide {
	}

	public static class LaunchAirAttachBannerShow {
		public HotelSearchParams params;

		public LaunchAirAttachBannerShow(HotelSearchParams params) {
			this.params = params;
		}
	}

	public static class LaunchLobRefresh {
	}

	public static class LaunchShowLoadingAnimation {
	}
}
