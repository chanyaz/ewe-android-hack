package com.expedia.bookings.fragment;

import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightSearchResponse;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FlightUtils;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class FlightSearchDownloadFragment extends Fragment {

	private static final String STATE_PARAMS = "STATE_PARAMS";
	private static final String DL_SEARCH = "DL_FLIGHT_SEARCH";

	public static FlightSearchDownloadFragment newInstance(FlightSearchParams params) {
		FlightSearchDownloadFragment frag = new FlightSearchDownloadFragment();
		frag.setSearchParams(params);
		return frag;
	}

	private FlightSearchParams mSearchParams;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(DL_SEARCH);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DL_SEARCH);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_PARAMS, mSearchParams.toJson().toString());
	}

	public void startOrResumeForParams(FlightSearchParams params) {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_SEARCH);
		if (dl.isDownloading(DL_SEARCH)) {
			dl.cancelDownload(DL_SEARCH);
		}
		setSearchParams(params);
		dl.startDownload(DL_SEARCH, mSearchDownload, mSearchCallback);
	}

	private void setSearchParams(FlightSearchParams params) {
		mSearchParams = params;
	}

	private final Download<FlightSearchResponse> mSearchDownload = new Download<FlightSearchResponse>() {
		@Override
		public FlightSearchResponse doDownload() {
			if (getActivity() != null && FlightUtils.dateRangeSupportsFlightSearch(getActivity())) {
				ExpediaServices services = new ExpediaServices(getActivity());
				return services.flightSearch(mSearchParams, 0);
			}

			return null;
		}
	};

	private final OnDownloadComplete<FlightSearchResponse> mSearchCallback = new OnDownloadComplete<FlightSearchResponse>() {
		@Override
		public void onDownload(FlightSearchResponse results) {
			if (getActivity() != null) {
				Events.post(new Events.FlightSearchResponseAvailable(results));
			}
			else {
				Log.e("Our FlightSearch returned, but we cannot use it. getActivity() == null:" + (getActivity() == null));
			}
		}
	};
}
