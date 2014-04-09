package com.expedia.bookings.fragment;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class HotelSearchDownloadFragment extends Fragment {

	private static final String STATE_PARAMS = "STATE_PARAMS";
	private static final String DL_SEARCH = "DL_HOTEL_SEARCH";

	public static HotelSearchDownloadFragment newInstance(HotelSearchParams params) {
		HotelSearchDownloadFragment frag = new HotelSearchDownloadFragment();
		frag.setSearchParams(params);
		return frag;
	}

	private HotelSearchParams mSearchParams;
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
				HotelSearchParams params = new HotelSearchParams();
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

		if (mStartOrResumeOnAttach && mSearchParams != null) {
			startOrResumeForParams(mSearchParams);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DL_SEARCH)) {
			bd.registerDownloadCallback(DL_SEARCH, mSearchCallback);
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

	public void startOrResumeForParams(HotelSearchParams params) {
		if (mListener == null) {
			setSearchParams(params);
			mStartOrResumeOnAttach = true;
		}
		else {
			BackgroundDownloader dl = BackgroundDownloader.getInstance();
			dl.unregisterDownloadCallback(DL_SEARCH);
			if (dl.isDownloading(DL_SEARCH)) {
				if (mSearchParams != null && !mSearchParams.equals(params)) {
					//We're in the middle of a download and we just got new (and different) params.
					dl.cancelDownload(DL_SEARCH);
					setSearchParams(params);
					startOrRestart();
				}
				else {
					//Our params haven't changed so just listen for the existing download
					dl.registerDownloadCallback(DL_SEARCH, mSearchCallback);
				}
			}
			else {
				//We weren't downloading, so we should start
				setSearchParams(params);
				startOrRestart();
			}
		}
	}

	public void startOrRestart() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_SEARCH);
		if (dl.isDownloading(DL_SEARCH)) {
			dl.cancelDownload(DL_SEARCH);
		}
		dl.startDownload(DL_SEARCH, mSearchDownload, mSearchCallback);
	}

	protected void setSearchParams(HotelSearchParams params) {
		mSearchParams = params;
	}

	private final BackgroundDownloader.Download<HotelSearchResponse> mSearchDownload = new BackgroundDownloader.Download<HotelSearchResponse>() {
		@Override
		public HotelSearchResponse doDownload() {
			//TODO: Remove try catch, write good search param validation so we don't kick off if we don't have data.
			try {
				return mServices.search(mSearchParams, ExpediaServices.F_HOTELS);
			}
			catch (Exception ex) {
				Log.e("Hotel Search download exception.", ex);
			}
			return null;

		}
	};

	private final BackgroundDownloader.OnDownloadComplete<HotelSearchResponse> mSearchCallback = new BackgroundDownloader.OnDownloadComplete<HotelSearchResponse>() {
		@Override
		public void onDownload(HotelSearchResponse results) {
			if (mListener != null && getActivity() != null) {
				mListener.onExpediaServicesDownload(ExpediaServicesFragment.ServiceType.HOTEL_SEARCH, results);
			}
			else {
				Log.e("Our HotelSearch returned, but we cannot use it. mListener == null:" + (mListener == null)
					+ " getActivity() == null:" + (getActivity() == null));
			}
		}
	};

}
