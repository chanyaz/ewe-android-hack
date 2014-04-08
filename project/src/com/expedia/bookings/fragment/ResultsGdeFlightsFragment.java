package com.expedia.bookings.fragment;

import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.mobiata.android.Log;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
public class ResultsGdeFlightsFragment extends Fragment implements
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider, ExpediaServicesFragment.ExpediaServicesFragmentListener {

	private static final String STATE_ORIGIN = "STATE_ORIGIN";
	private static final String STATE_DESTINATION = "STATE_DESTINATION";

	private static final String FTAG_HISTOGRAM = "FTAG_HISTOGRAM";
	private static final String FTAG_GDE_DOWNLOADER = "FTAG_GDE_DOWNLOADER";

	private View mRootC;
	private FrameLayoutTouchController mHistogramC;

	private ResultsFlightHistogramFragment mHistogramFrag;
	private GdeDownloadFragment mGdeDownloadFrag;

	private Location mOrigin;
	private Location mDestination;

	public static ResultsGdeFlightsFragment newInstance() {
		ResultsGdeFlightsFragment frag = new ResultsGdeFlightsFragment();
		return frag;
	}

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
			}
			catch (Exception ex) {
				Log.w("Exception trying to parse saved search params", ex);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_results_gde_flights, container, false);
		mHistogramC = Ui.findView(view, R.id.histogram_container);

		//Add default fragments
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mHistogramFrag = FragmentAvailabilityUtils.setFragmentAvailability(true,
			FTAG_HISTOGRAM,
			manager, transaction, this, R.id.histogram_container, false);
		mGdeDownloadFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			true,
			FTAG_GDE_DOWNLOADER, manager, transaction, this,
			0, false);


		transaction.commit();

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		mGdeDownloadFrag.startOrResumeForRoute(mOrigin, mDestination);
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
	}

	/*
	 * Local methods
	 */

	protected void setRoute(Location origin, Location destination) {
		mOrigin = origin;
		mDestination = destination;
		if (mGdeDownloadFrag != null) {
			mGdeDownloadFrag.startOrResumeForRoute(mOrigin, mDestination);
		}
	}

	/**
	 * FRAGMENT AVAILABILITY PROVIDER
	 */

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		if (tag == FTAG_HISTOGRAM) {
			return mHistogramFrag;
		}
		else if (tag == FTAG_GDE_DOWNLOADER) {
			return mGdeDownloadFrag;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FTAG_HISTOGRAM) {
			return new ResultsFlightHistogramFragment();
		}
		else if (tag == FTAG_GDE_DOWNLOADER) {
			return GdeDownloadFragment.newInstance();
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_GDE_DOWNLOADER) {
			((GdeDownloadFragment) frag).startOrResumeForRoute(mOrigin, mDestination);
		}
	}

	/*
	 * ExpediaServicesFragmentListener
	 */

	@Override
	public void onExpediaServicesDownload(ExpediaServicesFragment.ServiceType type, Response response) {
		if (type == ExpediaServicesFragment.ServiceType.FLIGHT_GDE_SEARCH) {
			if (response != null && !response.hasErrors()) {
				Db.setFlightSearchHistogramResponse((FlightSearchHistogramResponse) response);
				if (mHistogramFrag != null) {
					mHistogramFrag.setHistogramData((FlightSearchHistogramResponse) response);
				}
			}
			else if (response != null) {
				Log.e("FLIGHT_GDE_SEARCH Errors:" + response.gatherErrorMessage(getActivity()));
			}
			else {
				Log.e("FLIGHT_GDE_SEARCH null response");
			}
		}
	}
}
