package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.SuggestionResponse;
import com.expedia.bookings.data.SuggestionSort;
import com.expedia.bookings.interfaces.IExpediaServicesListener;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class LocationSuggestionsDownloadFragment extends Fragment {

	private static final String STATE_LAT = "STATE_LAT";
	private static final String STATE_LON = "STATE_LON";
	private static final String DL_NEARBY_SUGGESTION_SEARCH = "DL_NEARBY_SUGGESTION_SEARCH";

	public static LocationSuggestionsDownloadFragment newInstance(double lat, double lon) {
		LocationSuggestionsDownloadFragment frag = new LocationSuggestionsDownloadFragment();
		frag.setLatLon(lat, lon);
		return frag;
	}

	private double mLat;
	private double mLon;
	private boolean mDoDlOnResume = false;
	private ExpediaServices mServices;
	private IExpediaServicesListener mListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mServices = new ExpediaServices(getActivity());

		if (savedInstanceState != null) {
			mLat = savedInstanceState.getDouble(STATE_LAT, mLat);
			mLon = savedInstanceState.getDouble(STATE_LON, mLon);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, IExpediaServicesListener.class);
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DL_NEARBY_SUGGESTION_SEARCH)) {
			bd.registerDownloadCallback(DL_NEARBY_SUGGESTION_SEARCH, mSearchCallback);
		}
		else {
			startOrRestart();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(DL_NEARBY_SUGGESTION_SEARCH);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(DL_NEARBY_SUGGESTION_SEARCH);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putDouble(STATE_LAT, mLat);
		outState.putDouble(STATE_LON, mLon);
	}

	public void startOrRestart() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_NEARBY_SUGGESTION_SEARCH);
		if (dl.isDownloading(DL_NEARBY_SUGGESTION_SEARCH)) {
			dl.cancelDownload(DL_NEARBY_SUGGESTION_SEARCH);
		}
		if (mServices != null && mListener != null) {
			dl.startDownload(DL_NEARBY_SUGGESTION_SEARCH, mSearchDownload, mSearchCallback);
		}
	}

	public boolean setLatLon(double lat, double lon) {
		boolean retVal = (lat != mLat || lon != mLon);
		mLat = lat;
		mLon = lon;
		return retVal;
	}

	private final BackgroundDownloader.Download<SuggestionResponse> mSearchDownload = new BackgroundDownloader.Download<SuggestionResponse>() {
		@Override
		public SuggestionResponse doDownload() {
			return mServices.suggestionsAirportsNearby(mLat, mLon, SuggestionSort.POPULARITY);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<SuggestionResponse> mSearchCallback = new BackgroundDownloader.OnDownloadComplete<SuggestionResponse>() {
		@Override
		public void onDownload(SuggestionResponse results) {
			if (results != null && !results.hasErrors()) {
				mListener.onExpediaServicesDownload(IExpediaServicesListener.ServiceType.SUGGEST_NEARBY, results);
			}
			else {
				Log.e("SuggestionResponse is null or contains errors.");
			}
		}
	};

}
