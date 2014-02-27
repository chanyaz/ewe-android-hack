package com.expedia.bookings.otto;

import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelProductResponse;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.fragment.HotelBookingFragment;
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
 * @author Supreeth
 *
 */
public class Events {

	private static final String TAG_EVENT = "OttoEventBus";

	private static Bus mBus = Db.getBus();

	private static final Events INSTANCE = new Events();

	private Events() {
		// Singleton - Cannot be instantiated
	}

	public static Events getInstance() {
		return INSTANCE;
	}

	public static void register(Object obj) {
		Log.d(TAG_EVENT, "Registering OttoEvents to : " + obj);
		mBus.register(obj);
	}

	public static void unregister(Object obj) {
		Log.d(TAG_EVENT, "UnRegistering OttoEvents for : " + obj);
		mBus.unregister(obj);
	}

	public static void post(Object obj) {
		Log.d(TAG_EVENT, "Posting OttoEvent : " + obj);
		mBus.post(obj);
	}

	/////////////////////////////////////////////////////////
	/// HotelBookingFragment related

	/**
	 * This event notifies HotelProductDownload call success by {@link HotelBookingFragment}
	 */
	public class HotelProductDownloadSuccess {
		private HotelProductResponse mHotelProductResponse;

		public HotelProductDownloadSuccess(HotelProductResponse hotelProductResponse) {
			this.mHotelProductResponse = hotelProductResponse;
		}

	}

	/**
	 * This event notifies CreateTripDownload call success by {@link HotelBookingFragment}
	 */
	public class CreateTripDownloadSuccess {
		private CreateTripResponse mCreateTripResponse;

		public CreateTripDownloadSuccess(CreateTripResponse createTripResponse) {
			this.mCreateTripResponse = createTripResponse;
		}

	}

	/**
	 * This event notifies CreateTripDownload call error by {@link HotelBookingFragment}
	 */
	public class CreateTripDownloadError {
		private ServerError mServerError;

		public CreateTripDownloadError(ServerError serverError) {
			this.mServerError = serverError;
		}

	}

	/**
	 * This event notifies that the CreateTripDownload retry dialog's "retry" operation has been initiated by {@link HotelBookingFragment}
	 */
	public class CreateTripDownloadRetry {
	}

	/**
	 * This event notifies that the CreateTripDownload retry dialog's "cancel" operation has been initiated by {@link HotelBookingFragment}
	 */
	public class CreateTripDownloadRetryCancel {
	}

}
