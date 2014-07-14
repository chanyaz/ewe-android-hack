package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Response;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CenteredCaptionedIcon;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.TextView;
import com.larvalabs.svgandroid.widget.SVGView;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

/**
 * ResultsGdeFlightsFragment: The GDE calendar/list fragment designed for tablet results 2013
 */
public class ResultsGdeFlightsFragment extends Fragment implements
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider, ExpediaServicesFragment.ExpediaServicesFragmentListener,
	ResultsFlightHistogramFragment.IFlightHistogramListener {

	public interface IGdeFlightsListener {
		public void onGdeFirstDateSelected(LocalDate date);

		public void onGdeOneWayTrip(LocalDate date);

		public void onGdeTwoWayTrip(LocalDate depDate, LocalDate retDate);
	}

	private static final String STATE_ORIGIN = "STATE_ORIGIN";
	private static final String STATE_DESTINATION = "STATE_DESTINATION";

	private static final String FTAG_HISTOGRAM = "FTAG_HISTOGRAM";
	private static final String FTAG_GDE_DOWNLOADER = "FTAG_GDE_DOWNLOADER";

	private View mRootC;
	private FrameLayoutTouchController mHistogramC;

	private CenteredCaptionedIcon mMissingFlightInfo;
	private TextView mGdeHeaderTv;
	private ImageView mGdeBack;
	private ProgressBar mGdeProgressBar;
	private TextView mGdeMaxPriceTv;

	private ResultsFlightHistogramFragment mHistogramFrag;
	private GdeDownloadFragment mGdeDownloadFrag;

	private Location mOrigin;
	private Location mDestination;
	private LocalDate mDepartureDate;

	private IGdeFlightsListener mListener;

	public static ResultsGdeFlightsFragment newInstance() {
		ResultsGdeFlightsFragment frag = new ResultsGdeFlightsFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, IGdeFlightsListener.class);
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
		mRootC = inflater.inflate(R.layout.fragment_results_gde_flights, container, false);
		mHistogramC = Ui.findView(mRootC, R.id.histogram_container);
		mGdeHeaderTv = Ui.findView(mRootC, R.id.flight_histogram_header);
		mGdeBack = Ui.findView(mRootC, R.id.flight_histogram_back);
		mGdeProgressBar = Ui.findView(mRootC, R.id.flight_histogram_progress_bar);
		mGdeMaxPriceTv = Ui.findView(mRootC, R.id.flight_histogram_max_price);

		mMissingFlightInfo = Ui.findView(mRootC, R.id.missing_flight_info_view);
		mMissingFlightInfo.setCaption(getString(R.string.missing_flight_info_message_TEMPLATE, Html.fromHtml(Sp.getParams().getDestination().getDisplayName()).toString()));
		mMissingFlightInfo.setVisibility(View.GONE);

		mGdeBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setGdeInfo(mOrigin, mDestination, null);
			}
		});

		return mRootC;
	}

	@Override
	public void onStart() {
		super.onStart();

		//Add default fragments
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();
		mHistogramFrag = FragmentAvailabilityUtils.setFragmentAvailability(true,
			FTAG_HISTOGRAM, manager, transaction, this, R.id.histogram_container, false);
		mGdeDownloadFrag = FragmentAvailabilityUtils.setFragmentAvailability(
			true, FTAG_GDE_DOWNLOADER, manager, transaction, this, 0, false);
		transaction.commit();
	}

	@Override
	public void onResume() {
		super.onResume();
		startOrResumeDownload(mGdeDownloadFrag);
		Sp.getBus().register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Sp.getBus().unregister(this);
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

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/*
	 * Search Param updates
	 */
	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		setGdeInfo(Sp.getParams().getOriginLocation(true), Sp.getParams().getDestinationLocation(true),
			Sp.getParams().getStartDate());
	}

	/*
	 * Local methods
	 */

	protected void setGdeInfo(Location origin, Location destination, LocalDate departureDate) {
		mOrigin = origin;
		mDestination = destination;
		mDepartureDate = departureDate;

		startOrResumeDownload(mGdeDownloadFrag);

		if (mHistogramFrag != null) {
			mHistogramFrag.setSelectedDepartureDate(mDepartureDate);
		}

		if (mRootC != null) {
			if (departureDate != null) {
				mGdeBack.setVisibility(View.VISIBLE);
				mGdeHeaderTv.setText(R.string.when_to_return);
			}
			else {
				mGdeBack.setVisibility(View.GONE);
				mGdeHeaderTv.setText(R.string.when_to_fly);
			}
		}

	}

	protected void startOrResumeDownload(GdeDownloadFragment frag) {
		if (frag != null) {
			//We always pass null for the date here, because the one way search has all the information we need
			frag.startOrResumeForRoute(mOrigin, mDestination, null);
			mGdeProgressBar.setVisibility(View.VISIBLE);
			mGdeMaxPriceTv.setVisibility(View.GONE);
			mMissingFlightInfo.setVisibility(View.GONE);
		}
	}

	/**
	 * FRAGMENT AVAILABILITY PROVIDER
	 */

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
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
			startOrResumeDownload((GdeDownloadFragment) frag);
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
				if (mGdeMaxPriceTv != null) {
					//TODO: more appropriate currency conversion
					String priceAsString = "$" + (int)((FlightSearchHistogramResponse) response).getMaxPrice();
					mGdeMaxPriceTv.setVisibility(View.VISIBLE);
					mGdeMaxPriceTv.setText(priceAsString);
				}
			}
			else {
				if (mHistogramFrag != null) {
					mHistogramFrag.setHistogramData(null);
				}

				if (response != null) {
					Log.e("FLIGHT_GDE_SEARCH Errors:" + response.gatherErrorMessage(getActivity()));
				}
				else {
					Log.e("FLIGHT_GDE_SEARCH null response");
				}
			}
			if (mGdeProgressBar != null) {
				mGdeProgressBar.setVisibility(View.GONE);
			}
			// Let's show the missing Flight search info if origin is not set.
			if (mMissingFlightInfo != null && Sp.getParams().getOriginLocation(true) == null) {
				mMissingFlightInfo.setVisibility(View.VISIBLE);
			}
		}
	}

	/*
	IFlightHistogramListener
	 */

	@Override
	public void onGdeDateSelected(LocalDate date) {
		if (mDepartureDate == null) {
			//Selecting the first gde date
			setGdeInfo(mOrigin, mDestination, date);
			if (mListener != null) {
				mListener.onGdeFirstDateSelected(date);
			}
		}
		else {
			//Selecting a second gde date
			if (mListener != null) {
				mListener.onGdeTwoWayTrip(mDepartureDate, date);
			}
		}

	}
}
