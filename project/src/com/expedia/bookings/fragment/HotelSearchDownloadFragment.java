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
	private ExpediaServicesFragment.ExpediaServicesFragmentListener mListener;

	private boolean mStartOrResumeOnAttach = false;

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
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, ExpediaServicesFragment.ExpediaServicesFragmentListener.class);

		if (mStartOrResumeOnAttach && mSearchParams != null) {
			// Reset filter on param change
			Db.resetFilter();
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
		if (bd.isDownloading(DL_SEARCH_HOTEL)) {
			bd.registerDownloadCallback(DL_SEARCH_HOTEL, mSearchHotelCallback);
		}
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

	public void ignoreNextDownload() {
		unregisterAllCallbacks();
	}

	public void startOrResumeForParams(HotelSearchParams params) {
		if (mListener == null) {
			setSearchParams(params);
			mStartOrResumeOnAttach = true;
		}
		else {
			// Unregister all callbacks at first because we might not need them
			unregisterAllCallbacks();

			BackgroundDownloader dl = BackgroundDownloader.getInstance();
			boolean areNewParams = mSearchParams != null && !mSearchParams.equals(params);

			if (dl.isDownloading(DL_SEARCH)) {
				if (areNewParams) {
					//We're in the middle of a download and we just got new (and different) params.
					setSearchParams(params);
					startOrRestartSearch();
				}
				else {
					dl.registerDownloadCallback(DL_SEARCH, mSearchCallback);
				}
			}
			else if (dl.isDownloading(DL_SEARCH_HOTEL)) {
				if (areNewParams) {
					//We're in the middle of a download and we just got new (and different) params.
					setSearchParams(params);
					startOrRestartSearchByHotel();
				}
				else {
					dl.registerDownloadCallback(DL_SEARCH_HOTEL, mSearchHotelCallback);
				}
			}
			else {
				setSearchParams(params);

				if (mSearchParams.getSearchType() == HotelSearchParams.SearchType.HOTEL) {
					startOrRestartSearchByHotel();
				}
				else {
					startOrRestartSearch();
				}
			}

		}
	}

	private void startOrRestartSearch() {
		cancelAllDownloads();
		unregisterAllCallbacks();
		BackgroundDownloader.getInstance().startDownload(DL_SEARCH, mSearchDownload, mSearchCallback);
	}

	private void startOrRestartSearchByHotel() {
		cancelAllDownloads();
		unregisterAllCallbacks();
		BackgroundDownloader.getInstance().startDownload(DL_SEARCH_HOTEL, mSearchHotelDownload, mSearchHotelCallback);
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

	private void unregisterAllCallbacks() {
		BackgroundDownloader dl = BackgroundDownloader.getInstance();
		dl.unregisterDownloadCallback(DL_SEARCH);
		dl.unregisterDownloadCallback(DL_SEARCH_HOTEL);
	}

	public boolean isDownloadingSearch() {
		return BackgroundDownloader.getInstance().isDownloading(DL_SEARCH);
	}

	public boolean isDownloadingSearchByHotel() {
		return BackgroundDownloader.getInstance().isDownloading(DL_SEARCH_HOTEL);
	}

	protected void setSearchParams(HotelSearchParams params) {
		mSearchParams = params;
	}

	private final BackgroundDownloader.Download<HotelSearchResponse> mSearchDownload = new BackgroundDownloader.Download<HotelSearchResponse>() {
		@Override
		public HotelSearchResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			return services.search(mSearchParams, ExpediaServices.F_HOTELS);
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

	private final BackgroundDownloader.Download<HotelOffersResponse> mSearchHotelDownload = new BackgroundDownloader.Download<HotelOffersResponse>() {
		@Override
		public HotelOffersResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());

			Property selectedProperty = new Property();
			selectedProperty.setPropertyId(Db.getHotelSearch().getSearchParams().getRegionId());

			return services.availability(mSearchParams, selectedProperty);
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<HotelOffersResponse> mSearchHotelCallback = new BackgroundDownloader.OnDownloadComplete<HotelOffersResponse>() {
		@Override
		public void onDownload(HotelOffersResponse results) {
			if (mListener != null && getActivity() != null) {
				mListener.onExpediaServicesDownload(ExpediaServicesFragment.ServiceType.HOTEL_SEARCH_HOTEL, results);
			}
			else {
				Log.e("Search by hotel returned but we can't use it. mListener == null:" + (mListener == null)
					+ " getActivity() == null:" + (getActivity() == null));
			}
		}
	};

}
