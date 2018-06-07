package com.expedia.bookings.otto;

import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.expedia.bookings.data.ApiError;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.collections.Collection;
import com.expedia.bookings.data.collections.CollectionLocation;
import com.expedia.bookings.data.hotels.Hotel;
import com.expedia.bookings.data.lx.ActivityDetailsResponse;
import com.expedia.bookings.data.lx.LXActivity;
import com.expedia.bookings.data.lx.LXCategoryMetadata;
import com.expedia.bookings.data.lx.LXCheckoutParams;
import com.expedia.bookings.data.lx.LXCheckoutResponse;
import com.expedia.bookings.data.lx.LXCreateTripResponse;
import com.expedia.bookings.data.lx.LXSearchResponse;
import com.expedia.bookings.data.lx.LXSortFilterMetadata;
import com.expedia.bookings.data.lx.LxSearchParams;
import com.expedia.bookings.data.lx.Offer;
import com.expedia.bookings.data.lx.SearchType;
import com.expedia.bookings.data.lx.Ticket;
import com.expedia.bookings.widget.LXOfferDatesButton;
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

	// HotelSearch update events
	public static class HotelAvailabilityUpdated {
	}

	public static class ShowCVV {
		public BillingInfo billingInfo;

		public ShowCVV(BillingInfo info) {
			this.billingInfo = info;
		}
	}

	public static class FinishActivity {
		// ignore
	}

	public static class LXShowSearchWidget {
		// ignore
	}

	public static class LXNewSearch {
		public String locationName;
		public LocalDate startDate;
		public LocalDate endDate;

		public LXNewSearch(String locationName, LocalDate startDate, LocalDate endDate) {
			this.locationName = locationName;
			this.startDate = startDate;
			this.endDate = endDate;
		}
	}

	public static class LXNewSearchParamsAvailable {
		public LxSearchParams lxSearchParams;

		public LXNewSearchParamsAvailable(LxSearchParams params) {
			lxSearchParams = params;
		}

		public LXNewSearchParamsAvailable(String locationName, LocalDate startDate, LocalDate endDate, boolean modQualified) {
			lxSearchParams = (LxSearchParams) new LxSearchParams.Builder()
				.searchType(SearchType.DEFAULT_SEARCH).location(locationName).modQualified(modQualified).startDate(startDate).endDate(endDate)
				.build();
		}

		public LXNewSearchParamsAvailable(String locationName, LocalDate startDate, LocalDate endDate, String filters, boolean modQualified) {
			if (filters == null) {
				filters = "";
			}
			lxSearchParams = (LxSearchParams)new LxSearchParams.Builder()
				.searchType(SearchType.EXPLICIT_SEARCH).filters(
					filters).location(locationName).modQualified(modQualified).startDate(startDate).endDate(endDate).build();
		}

		public LXNewSearchParamsAvailable(String activityId, String locationName, LocalDate startDate,
			LocalDate endDate, boolean modQualified) {
			lxSearchParams = (LxSearchParams) new LxSearchParams.Builder()
				.searchType(SearchType.EXPLICIT_SEARCH).activityId(
					activityId).location(locationName).modQualified(modQualified).startDate(startDate).endDate(endDate).build();
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

	public static class LXFilterChanged {
		public LXSortFilterMetadata lxSortFilterMetadata;

		public LXFilterChanged(LXSortFilterMetadata lxSortFilterMetadata) {
			this.lxSortFilterMetadata = lxSortFilterMetadata;
		}
	}

	public static class LXSearchFilterResultsReady {
		public List<LXActivity> filteredActivities;
		public Map<String, LXCategoryMetadata> filterCategories;

		public LXSearchFilterResultsReady(List<LXActivity> filteredActivities,
			Map<String, LXCategoryMetadata> filterCategories) {
			this.filteredActivities = filteredActivities;
			this.filterCategories = filterCategories;
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

	public static class LXShowRulesOnCheckout {
		//ignore
	}

	public static class LXShowCheckoutAfterPriceChange {
		//ignore
	}

	public static class LXUpdateCheckoutSummaryAfterPriceChange {
		public LXCheckoutResponse lxCheckoutResponse;

		public LXUpdateCheckoutSummaryAfterPriceChange(LXCheckoutResponse lxCheckoutResponse) {
			this.lxCheckoutResponse = lxCheckoutResponse;
		}
	}

	public static class LXActivitySelectedRetry {
		//ignore
	}

	public static class LXShowDetails {
		public ActivityDetailsResponse activityDetails;

		public LXShowDetails(ActivityDetailsResponse activityDetails) {
			this.activityDetails = activityDetails;
		}
	}

	public static class LXCreateTripSucceeded {
		public LXCreateTripResponse createTripResponse;
		public LXActivity activity;

		public LXCreateTripSucceeded(LXCreateTripResponse createTripResponse, LXActivity activity) {
			this.createTripResponse = createTripResponse;
			this.activity = activity;
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
		public LXCheckoutParams checkoutParams;

		public LXKickOffCheckoutCall(LXCheckoutParams checkoutParams) {
			this.checkoutParams = checkoutParams;
		}
	}

	public static class LXCheckoutSucceeded {
		public LXCheckoutResponse checkoutResponse;

		public LXCheckoutSucceeded(LXCheckoutResponse checkoutResponse) {
			this.checkoutResponse = checkoutResponse;
		}
	}

	public static class LXError {
		public ApiError apiError;

		public LXError(ApiError apiError) {
			this.apiError = apiError;
		}
	}

	public static class LXDetailsDateChanged {
		public LocalDate dateSelected;
		public LXOfferDatesButton buttonSelected;

		public LXDetailsDateChanged(LocalDate dateSelected, LXOfferDatesButton buttonSelected) {
			this.dateSelected = dateSelected;
			this.buttonSelected = buttonSelected;
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

	public static class CollectionDownloadComplete {
		public Collection collection;

		public CollectionDownloadComplete(Collection collection) {
			this.collection = collection;
		}
	}

	public static class LaunchCollectionItemSelected {
		public String collectionUrl;
		public CollectionLocation collectionLocation;
		public Bundle animOptions;

		public LaunchCollectionItemSelected(CollectionLocation location, Bundle animOptions,
			String url) {
			this.collectionLocation = location;
			this.animOptions = animOptions;
			this.collectionUrl = url;
		}
	}

	// Launch screen -- air attach
	public static class PhoneLaunchOnResume {
	}

	public static class PhoneLaunchOnPOSChange {
	}

	public static class LoggedInSuccessful {

	}

	public static class AppBackgroundedOnResume {

	}
}
