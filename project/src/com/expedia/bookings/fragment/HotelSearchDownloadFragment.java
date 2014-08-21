package com.expedia.bookings.fragment;

import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.expedia.bookings.data.HotelSearchParams;
import com.expedia.bookings.data.HotelSearchResponse;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class HotelSearchDownloadFragment extends Fragment {

	private static final String STATE_PARAMS = "STATE_PARAMS";
	private static final String DL_SEARCH = "DL_HOTEL_SEARCH";
	private static final String DL_SEARCH_HOTEL = "DL_HOTEL_SEARCH_HOTEL";

	public static HotelSearchDownloadFragment newInstance(HotelSearchParams params) {
		HotelSearchDownloadFragment frag = new HotelSearchDownloadFragment();
		frag.setSearchParams(params);
		return frag;
	}

	private HotelSearchParams mSearchParams;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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

		if (!isDownloading()) {
			startOrResumeForParams(mSearchParams);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		registerAllCallbacks();
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			cancelAllDownloads();
		}
		else {
			unregisterAllCallbacks();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_PARAMS, mSearchParams.toJson().toString());
	}

	public void startOrResumeForParams(HotelSearchParams params) {
		setSearchParams(params);
		unregisterAllCallbacks();
		cancelAllDownloads();

		if (mSearchParams.getSearchType() == HotelSearchParams.SearchType.HOTEL) {
			BackgroundDownloader.getInstance().startDownload(DL_SEARCH_HOTEL, mSearchHotelDownload, mSearchHotelCallback);
		}
		else {
			BackgroundDownloader.getInstance().startDownload(DL_SEARCH, mSearchDownload, mSearchCallback);
		}
	}

	private void cancelAllDownloads() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if (dl.isDownloading(DL_SEARCH)) {
			dl.cancelDownload(DL_SEARCH);
		}
		if (dl.isDownloading(DL_SEARCH_HOTEL)) {
			dl.cancelDownload(DL_SEARCH_HOTEL);
		}
	}

	private boolean isDownloading() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		if (dl.isDownloading(DL_SEARCH)) {
			return true;
		}
		if (dl.isDownloading(DL_SEARCH_HOTEL)) {
			return true;
		}

		return false;
	}

	private void registerAllCallbacks() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(DL_SEARCH)) {
			bd.registerDownloadCallback(DL_SEARCH, mSearchCallback);
		}
		if (bd.isDownloading(DL_SEARCH_HOTEL)) {
			bd.registerDownloadCallback(DL_SEARCH_HOTEL, mSearchHotelCallback);
		}
	}

	private void unregisterAllCallbacks() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_SEARCH);
		dl.unregisterDownloadCallback(DL_SEARCH_HOTEL);
	}

	public boolean isDownloadingSearch() {
		return BackgroundDownloader.getInstance().isDownloading(DL_SEARCH);
	}

	protected void setSearchParams(HotelSearchParams params) {
		mSearchParams = params;
	}

	private final BackgroundDownloader.Download<HotelSearchResponse> mSearchDownload = new BackgroundDownloader.Download<HotelSearchResponse>() {
		@Override
		public HotelSearchResponse doDownload() {
			if (getActivity() != null) {
				ExpediaServices services = new ExpediaServices(getActivity());
				return services.search(mSearchParams, ExpediaServices.F_HOTELS);
			}

			return null;
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<HotelSearchResponse> mSearchCallback = new BackgroundDownloader.OnDownloadComplete<HotelSearchResponse>() {
		@Override
		public void onDownload(HotelSearchResponse results) {
			if (getActivity() != null) {
				Events.post(new Events.HotelSearchResponseAvailable(results));
			}
		}
	};

	private final BackgroundDownloader.Download<HotelOffersResponse> mSearchHotelDownload = new BackgroundDownloader.Download<HotelOffersResponse>() {
		@Override
		public HotelOffersResponse doDownload() {
			if (getActivity() != null) {
				ExpediaServices services = new ExpediaServices(getActivity());

				Property selectedProperty = new Property();
				selectedProperty.setPropertyId(mSearchParams.getRegionId());

				HotelOffersResponse response = services.availability(mSearchParams, selectedProperty);

				if (response != null && response.isHotelUnavailable()) {
					response = services.hotelInformation(selectedProperty);
				}

				return response;
			}

			return null;
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<HotelOffersResponse> mSearchHotelCallback = new BackgroundDownloader.OnDownloadComplete<HotelOffersResponse>() {
		@Override
		public void onDownload(HotelOffersResponse results) {
			if (getActivity() != null) {
				Events.post(new Events.HotelOffersResponseAvailable(results));
			}
		}
	};
}
