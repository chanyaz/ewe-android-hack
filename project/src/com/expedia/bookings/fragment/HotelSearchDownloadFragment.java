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
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DL_SEARCH)) {
			bd.registerDownloadCallback(DL_SEARCH, mSearchCallback);
		}
		else {
			bd.startDownload(DL_SEARCH, mSearchDownload, mSearchCallback);
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
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_PARAMS, mSearchParams.toJson().toString());
	}

	public void startOrRestart(){
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if(dl.isDownloading(DL_SEARCH)){
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
			return mServices.search(mSearchParams, ExpediaServices.F_HOTELS);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<HotelSearchResponse> mSearchCallback = new BackgroundDownloader.OnDownloadComplete<HotelSearchResponse>() {
		@Override
		public void onDownload(HotelSearchResponse results) {
			mListener.onExpediaServicesDownload(ExpediaServicesFragment.ServiceType.HOTEL_SEARCH, results);
		}
	};

}
