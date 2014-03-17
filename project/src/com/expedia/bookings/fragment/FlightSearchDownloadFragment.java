package com.expedia.bookings.fragment;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class FlightSearchDownloadFragment extends Fragment {

	private static final String STATE_PARAMS = "STATE_PARAMS";
	private static final String DL_SEARCH = "DL_FLIGHT_SEARCH";
	private static final String DL_GDE_SEARCH = "DL_FLIGHT_GDE_SEARCH";

	public static FlightSearchDownloadFragment newInstance(FlightSearchParams params) {
		FlightSearchDownloadFragment frag = new FlightSearchDownloadFragment();
		frag.setSearchParams(params);
		return frag;
	}

	private FlightSearchParams mSearchParams;
	private ExpediaServices mServices;
	private ExpediaServicesFragment.ExpediaServicesFragmentListener mListener;

	private boolean mStartOrResumeOnAttach = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mServices = new ExpediaServices(getActivity());

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_PARAMS)) {
			try {
				String searchParamsStr = savedInstanceState.getString(STATE_PARAMS);
				JSONObject searchParamsJson = new JSONObject(searchParamsStr);
				FlightSearchParams params = new FlightSearchParams();
				params.fromJson(searchParamsJson);
				mSearchParams = params;
			}
			catch (Exception ex) {
				Log.w("Exception trying to parse saved search params", ex);
			}
		}
		if (mSearchParams == null) {
			throw new RuntimeException("SearchParams must be set.");
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, ExpediaServicesFragment.ExpediaServicesFragmentListener.class);

		if(mStartOrResumeOnAttach && mSearchParams != null){
			startOrResumeForParams(mSearchParams);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(DL_SEARCH);
			BackgroundDownloader.getInstance().cancelDownload(DL_GDE_SEARCH);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DL_SEARCH);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DL_GDE_SEARCH);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null;
		mStartOrResumeOnAttach = false;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_PARAMS, mSearchParams.toJson().toString());
	}

	public void startSearch() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DL_SEARCH)) {
			bd.registerDownloadCallback(DL_SEARCH, mSearchCallback);
		}
		else {
			bd.startDownload(DL_SEARCH, mSearchDownload, mSearchCallback);
		}
	}

	public void startOrResumeForParams(FlightSearchParams params) {
		if (mListener == null) {
			setSearchParams(params);
			mStartOrResumeOnAttach = true;
		}
		else {
			BackgroundDownloader dl = BackgroundDownloader.getInstance();
			dl.unregisterDownloadCallback(DL_SEARCH);
			dl.unregisterDownloadCallback(DL_GDE_SEARCH);
			if (dl.isDownloading(DL_SEARCH) || dl.isDownloading(DL_GDE_SEARCH)) {
				if (mSearchParams != null && !mSearchParams.equals(params)) {
					//We're in the middle of a download and we just got new (and different) params.
					dl.cancelDownload(DL_SEARCH);
					dl.cancelDownload(DL_GDE_SEARCH);

					setSearchParams(params);

					startOrRestartSearch();
					startOrRestartGdeSearch();
				}
				else {
					//Our params haven't changed so just listen for the existing download
					dl.registerDownloadCallback(DL_SEARCH, mSearchCallback);
					dl.registerDownloadCallback(DL_GDE_SEARCH, mGdeSearchCallback);
				}
			}
			else {
				//We weren't downloading, so we should start
				setSearchParams(params);
				startOrRestartSearch();
				startOrRestartGdeSearch();
			}
		}
	}

	public void startOrRestartSearch() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_SEARCH);
		if (dl.isDownloading(DL_SEARCH)) {
			dl.cancelDownload(DL_SEARCH);
		}
		dl.startDownload(DL_SEARCH, mSearchDownload, mSearchCallback);
	}

	public void startGdeSearch() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DL_GDE_SEARCH)) {
			bd.registerDownloadCallback(DL_GDE_SEARCH, mGdeSearchCallback);
		}
		else {
			bd.startDownload(DL_GDE_SEARCH, mGdeSearchDownload, mGdeSearchCallback);
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

	public boolean isDownloadingFlightSearch() {
		return BackgroundDownloader.getInstance().isDownloading(DL_SEARCH);
	}

	public boolean isDownloadingGdeSearch() {
		return BackgroundDownloader.getInstance().isDownloading(DL_GDE_SEARCH);
	}

	protected void setSearchParams(FlightSearchParams params) {
		mSearchParams = params;
	}

	private final Download<FlightSearchResponse> mSearchDownload = new Download<FlightSearchResponse>() {
		@Override
		public FlightSearchResponse doDownload() {
			//TODO: Remove try catch, write good search param validation so we don't kick off if we don't have data.
			try {
				return mServices.flightSearch(mSearchParams, 0);
			}
			catch (Exception ex) {
				Log.e("Flight search download exception", ex);
			}
			return null;
		}
	};

	private final OnDownloadComplete<FlightSearchResponse> mSearchCallback = new OnDownloadComplete<FlightSearchResponse>() {
		@Override
		public void onDownload(FlightSearchResponse results) {
			mListener.onExpediaServicesDownload(ExpediaServicesFragment.ServiceType.FLIGHT_SEARCH, results);
		}
	};

	// GDE Histogram Download

	private final Download<FlightSearchHistogramResponse> mGdeSearchDownload = new Download<FlightSearchHistogramResponse>() {
		@Override
		public FlightSearchHistogramResponse doDownload() {
			//TODO: Remove try catch, write good search param validation so we don't kick off if we don't have data.
			try {
				return mServices.flightSearchHistogram(mSearchParams);
			}
			catch (Exception ex) {
				Log.e("Flight GDE search download exception", ex);
			}
			return null;
		}
	};

	private final OnDownloadComplete<FlightSearchHistogramResponse> mGdeSearchCallback = new OnDownloadComplete<FlightSearchHistogramResponse>() {
		@Override
		public void onDownload(FlightSearchHistogramResponse results) {
			mListener.onExpediaServicesDownload(ExpediaServicesFragment.ServiceType.FLIGHT_GDE_SEARCH, results);
		}
	};


}
