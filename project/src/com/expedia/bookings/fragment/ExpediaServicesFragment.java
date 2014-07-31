package com.expedia.bookings.fragment;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionSort;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.util.Ui;

/**
 * This is a view-less Fragment which can be used to call methods
 * in ExpediaServices (to do downloads and whatnot).
 * <p/>
 * It handles state on its own, and has callbacks for when downloads
 * succeed/fail.
 * <p/>
 * To avoid confusion, only one of each download type may run at a time;
 * if you start a new one, it will
 * <p/>
 * It should not manipulate Db; let the caller do that.
 */
public class ExpediaServicesFragment extends Fragment {

	public enum ServiceType {
		SUGGEST_NEARBY,
		HOTEL_SEARCH,
		HOTEL_SEARCH_HOTEL,
		HOTEL_INFO,
		FLIGHT_SEARCH,
		HOTEL_AFFINITY_SEARCH,
	}

	private Map<ServiceType, ResponseDownload> mToRequest = new ConcurrentHashMap<ServiceType, ResponseDownload>();

	private Map<ServiceType, ExpediaServices> mRequesting = new ConcurrentHashMap<ServiceType, ExpediaServices>();

	private Map<ServiceType, Response> mResponses = new ConcurrentHashMap<ServiceType, Response>();

	private ExpediaServicesFragmentListener mListener;

	// Because we can't have null references in a ConcurrentHashMap, we track null responses this way
	private static final Response NULL_RESPONSE = new Response();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, ExpediaServicesFragmentListener.class);

		// In case someone queued up downloads before we were attached
		executeDownloads();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);
	}

	@Override
	public void onResume() {
		super.onResume();

		// In case we were detached before reporting success, report it now
		informTheMen();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Cancel all downloads if we're leaving for good
		if (getActivity().isFinishing()) {
			for (ExpediaServices services : mRequesting.values()) {
				services.onCancel();
			}
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null;
	}

	//////////////////////////////////////////////////////////////////////////
	// Services

	public void startSuggestionsNearby(final double latitude, final double longitude, boolean continueIfInProgress) {
		doDownload(ServiceType.SUGGEST_NEARBY, continueIfInProgress, new ResponseDownload() {
			public Response execute(ExpediaServices services) {
				return services.suggestionsNearby(latitude, longitude, SuggestionSort.POPULARITY, 0);
			}
		});
	}

	public void startHotelSearch(final SearchParams searchParams, boolean continueIfInProgress) {
		startHotelSearch(searchParams.toHotelSearchParams(), continueIfInProgress);
	}

	public void startHotelSearch(final HotelSearchParams searchParams, boolean continueIfInProgress) {
		doDownload(ServiceType.HOTEL_SEARCH, continueIfInProgress, new ResponseDownload() {
			public Response execute(ExpediaServices services) {
				return services.search(searchParams, 0);
			}
		});
	}

	public void startFlightSearch(final SearchParams searchParams, boolean continueIfInProgress) {
		startFlightSearch(searchParams.toFlightSearchParams(), continueIfInProgress);
	}

	public void startFlightSearch(final FlightSearchParams searchParams, boolean continueIfInProgress) {
		doDownload(ServiceType.FLIGHT_SEARCH, continueIfInProgress, new ResponseDownload() {
			public Response execute(ExpediaServices services) {
				return services.flightSearch(searchParams, 0);
			}
		});
	}


	public boolean isDownloading(ServiceType type) {
		return mRequesting.containsKey(type);
	}

	public void cancel(ServiceType type) {
		if (isDownloading(type)) {
			mRequesting.get(type).onCancel();
			mRequesting.remove(type);
		}

		if (mResponses.containsKey(type)) {
			mResponses.remove(type);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Nuts and bolts

	private interface ResponseDownload {
		public Response execute(ExpediaServices services);
	}

	private void doDownload(ServiceType type, boolean continueIfInProgress, ResponseDownload download) {
		if (shouldStartNewDownload(type, continueIfInProgress)) {
			mToRequest.put(type, download);
			executeDownloads();
		}
	}

	/**
	 * Determines if we should start a new download thread for a ServiceType.
	 *
	 * @param type                 The ServiceType of the download
	 * @param continueIfInProgress true if we should not interrupt an
	 *                             existing download, false if we should cancel the download if it's found
	 * @return true if you should start a new download, false if we're
	 * currently downloading and don't want to interrupt
	 */
	private boolean shouldStartNewDownload(ServiceType type, boolean continueIfInProgress) {
		if (isDownloading(type)) {
			if (continueIfInProgress) {
				return false;
			}
			else {
				cancel(type);
			}
		}

		return true;
	}

	private void executeDownloads() {
		if (getActivity() != null) {
			Iterator<Map.Entry<ServiceType, ResponseDownload>> iterator = mToRequest.entrySet().iterator();

			while (iterator.hasNext()) {
				Map.Entry<ServiceType, ResponseDownload> entry = iterator.next();
				final ServiceType type = entry.getKey();
				final ResponseDownload download = entry.getValue();

				(new Thread(new Runnable() {
					@Override
					public void run() {
						// In case someone started a download while unattached, spin until we get re-attached
						ExpediaServices services = new ExpediaServices(getActivity().getApplicationContext());
						mRequesting.put(type, services);
						Response response = download.execute(services);
						mRequesting.remove(type);

						if (response == null) {
							response = NULL_RESPONSE;
						}
						mResponses.put(type, response);
						informTheMen();
					}
				})).start();

				iterator.remove();
			}
		}
	}

	// Kif, I have made it with a woman
	private void informTheMen() {
		if (mListener != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					for (ServiceType type : mResponses.keySet()) {
						Response response = mResponses.remove(type);
						if (response == NULL_RESPONSE) {
							response = null;
						}
						mListener.onExpediaServicesDownload(type, response);
					}
				}
			});
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface ExpediaServicesFragmentListener {

		/**
		 * To avoid a proliferation of methods, we require casting for responses.
		 */
		public void onExpediaServicesDownload(ServiceType type, Response response);

	}
}
