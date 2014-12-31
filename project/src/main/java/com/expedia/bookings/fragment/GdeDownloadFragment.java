package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.squareup.otto.Produce;

public class GdeDownloadFragment extends Fragment {

	private static final String STATE_ORIGIN = "STATE_ORIGIN";
	private static final String STATE_DESTINATION = "STATE_DESTINATION";
	private static final String STATE_DEPARTURE_DATE = "STATE_DEPARTURE_DATE";
	private static final String DL_GDE_SEARCH = "DL_FLIGHT_GDE_SEARCH";

	public static GdeDownloadFragment newInstance() {
		GdeDownloadFragment frag = new GdeDownloadFragment();
		return frag;
	}

	private Location mOrigin;
	private Location mDestination;
	private LocalDate mDepartureDate;
	private FlightSearchHistogramResponse mCachedResults;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			try {
				if (savedInstanceState.containsKey(STATE_ORIGIN)) {
					String originStr = savedInstanceState.getString(STATE_ORIGIN);
					JSONObject originJson = new JSONObject(originStr);
					Location location = new Location();
					location.fromJson(originJson);
					mOrigin = location;
				}
				if (savedInstanceState.containsKey(STATE_DESTINATION)) {
					String destStr = savedInstanceState.getString(STATE_DESTINATION);
					JSONObject destJson = new JSONObject(destStr);
					Location location = new Location();
					location.fromJson(destJson);
					mDestination = location;
				}
				if (savedInstanceState.containsKey(STATE_DEPARTURE_DATE)) {
					mDepartureDate = ISODateTimeFormat.basicDate()
						.parseLocalDate(savedInstanceState.getString(STATE_DEPARTURE_DATE));
				}
			}
			catch (Exception ex) {
				Log.w("Exception trying to parse saved search params", ex);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);

		if (mOrigin != null && mDestination != null) {
			startOrResumeForRoute(mOrigin, mDestination, mDepartureDate);
		}
	}

	@Override
	public void onPause() {
		Events.unregister(this);
		super.onPause();

		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(DL_GDE_SEARCH);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DL_GDE_SEARCH);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mOrigin != null) {
			outState.putString(STATE_ORIGIN, mOrigin.toJson().toString());
		}
		if (mDestination != null) {
			outState.putString(STATE_DESTINATION, mDestination.toJson().toString());
		}
		if (mDepartureDate != null) {
			outState.putString(STATE_DEPARTURE_DATE, ISODateTimeFormat.basicDate().print(mDepartureDate));
		}
	}

	public void startOrResumeForRoute(Location origin, Location destination, LocalDate departureDate) {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_GDE_SEARCH);
		if (dl.isDownloading(DL_GDE_SEARCH)) {
			boolean originNew = mOrigin == null ? origin != null : !mOrigin.equals(origin);
			boolean destNew = mDestination == null ? destination != null : !mDestination.equals(destination);
			boolean dateNew = mDepartureDate == null ? departureDate != null : !mDepartureDate.equals(departureDate);

			if (origin == null || destination == null || originNew || destNew || dateNew) {
				//Something has changed, so we cancel the previous download
				dl.cancelDownload(DL_GDE_SEARCH);

				setGdeInfo(origin, destination, departureDate);

				if (origin != null && destination != null && (originNew || destNew || dateNew)) {
					//Something has changed and we have valid data, so we kick off the new search
					startOrRestartGdeSearch();
				}
			}
			else {
				//Our Locations haven't changed so we just resume the existing dl
				dl.registerDownloadCallback(DL_GDE_SEARCH, mGdeSearchCallback);
			}
		}
		else {
			//We weren't downloading, so we should start
			setGdeInfo(origin, destination, departureDate);
			startOrRestartGdeSearch();
		}
	}

	public void startOrRestartGdeSearch() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_GDE_SEARCH);
		if (dl.isDownloading(DL_GDE_SEARCH)) {
			dl.cancelDownload(DL_GDE_SEARCH);
		}
		dl.startDownload(DL_GDE_SEARCH, mGdeSearchDownload, mGdeSearchCallback);
	}

	public boolean isDownloadingGdeSearch() {
		return BackgroundDownloader.getInstance().isDownloading(DL_GDE_SEARCH);
	}

	private void setGdeInfo(Location origin, Location destination, LocalDate departureDate) {
		mOrigin = origin;
		mDestination = destination;
		mDepartureDate = departureDate;
	}

	// GDE Histogram Download
	private final Download<FlightSearchHistogramResponse> mGdeSearchDownload = new Download<FlightSearchHistogramResponse>() {
		@Override
		public FlightSearchHistogramResponse doDownload() {
			if (!PointOfSale.getPointOfSale().supportsGDE()) {
				return null;
			}

			if (mOrigin == null || mDestination == null) {
				return null;
			}

			if (getActivity() == null) {
				return null;
			}

			ExpediaServices services = new ExpediaServices(getActivity());
			return services.flightSearchHistogram(mOrigin, mDestination, mDepartureDate);
		}
	};

	private final OnDownloadComplete<FlightSearchHistogramResponse> mGdeSearchCallback
		= new OnDownloadComplete<FlightSearchHistogramResponse>() {
		@Override
		public void onDownload(FlightSearchHistogramResponse results) {
			mCachedResults = results;
			Events.post(produceGdeDataAvailable());
		}
	};

	@Produce
	public Events.GdeDataAvailable produceGdeDataAvailable() {
		return new Events.GdeDataAvailable(mCachedResults);
	}
}
