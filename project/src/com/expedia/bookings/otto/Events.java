package com.expedia.bookings.otto;

import java.util.List;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.ServerError;
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
 *
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

	public static class LaunchTileClicked {
		public LaunchTileClicked() {

		}
	}

	public static class LaunchMapPinClicked {
		public final Rect origin;

		public LaunchMapPinClicked(Rect origin) {
			this.origin = origin;
		}
	}

	/////////////////////////////////////////////////////////
	/// HotelBookingFragment related

	/**
	 * This event notifies HotelProductDownload call has succeeded
	 */
	public static class HotelProductDownloadSuccess {
		public final HotelProductResponse hotelProductResponse;

		public HotelProductDownloadSuccess(HotelProductResponse response) {
			this.hotelProductResponse = response;
		}
	}

	/**
	 * This event notifies HotelProductDownload call has succeeded and the rate of the selected room has gone up.
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
		private ServerError mServerError;

		public CreateTripDownloadError(ServerError serverError) {
			this.mServerError = serverError;
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
		public final String changeString;

		public FlightPriceChange(String changeText) {
			this.changeString = changeText;
		}
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

	// Tablet Launch Screen events
	public static class LaunchCollectionsAvailable {
		public List<LaunchCollection> collections;

		public LaunchCollectionsAvailable(List<LaunchCollection> collections) {
			this.collections = collections;
		}
	}
}
