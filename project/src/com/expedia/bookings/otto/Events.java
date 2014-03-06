package com.expedia.bookings.otto;

import android.os.Handler;
import android.os.Looper;

import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.HotelProductResponse;
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
	 * This event notifies CreateTripDownload call has succeeded
	 */
	public static class CreateTripDownloadSuccess {
		public final CreateTripResponse createTripResponse;

		public CreateTripDownloadSuccess(CreateTripResponse response) {
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

	// Suggestion query events
	public static class SuggestionQueryStarted {
	}

	public static class SuggestionResultsDelivered {
	}
}
