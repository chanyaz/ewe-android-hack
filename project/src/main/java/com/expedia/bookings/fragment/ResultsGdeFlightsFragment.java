package com.expedia.bookings.fragment;

import org.joda.time.LocalDate;
import org.joda.time.YearMonth;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchHistogramResponse;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.ResultsSearchState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CenteredCaptionedIcon;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;
import com.squareup.otto.Subscribe;

/**
 * ResultsGdeFlightsFragment: The GDE calendar/list fragment designed for tablet results 2013
 */
public class ResultsGdeFlightsFragment extends Fragment implements
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private static final String STATE_ORIGIN = "STATE_ORIGIN";
	private static final String STATE_DESTINATION = "STATE_DESTINATION";

	private static final String FTAG_HISTOGRAM = "FTAG_HISTOGRAM";
	private static final String FTAG_GDE_DOWNLOADER = "FTAG_GDE_DOWNLOADER";

	private View mRootC;
	private TouchableFrameLayout mHistogramC;

	private CenteredCaptionedIcon mGdeErrorMessageView;
	private TextView mGdeHeaderTv;
	private ProgressBar mGdeProgressBar;
	private TextView mGdePriceRangeTv;
	private ViewGroup mToolTipInvokeButton;
	private LinearLayout mToolTipView;
	private TextView mToolTipDoneButton;

	private ResultsFlightHistogramFragment mHistogramFrag;
	private GdeDownloadFragment mGdeDownloadFrag;

	private Location mOrigin;
	private Location mDestination;
	private LocalDate mDepartureDate;
	private String mPriceString;
	private String mHeaderString;

	public static ResultsGdeFlightsFragment newInstance() {
		ResultsGdeFlightsFragment frag = new ResultsGdeFlightsFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
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
		mToolTipView = Ui.findView(mRootC, R.id.gde_tool_tip_view);
		mGdeHeaderTv = Ui.findView(mRootC, R.id.flight_histogram_header);
		mGdeProgressBar = Ui.findView(mRootC, R.id.flight_histogram_progress_bar);
		mGdePriceRangeTv = Ui.findView(mRootC, R.id.flight_histogram_price_range);
		mToolTipInvokeButton = Ui.findView(mRootC, R.id.flight_trends_tool_tip_button);
		mToolTipDoneButton = Ui.findView(mRootC, R.id.action_button);
		mToolTipInvokeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setToolTipView();
			}
		});

		mGdeErrorMessageView = Ui.findView(mRootC, R.id.gde_error_message_view);
		mGdeErrorMessageView.setVisibility(View.GONE);
		mGdeErrorMessageView.setActionButton(R.string.missing_flight_info_button_prompt, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.ShowSearchFragment(ResultsSearchState.FLIGHT_ORIGIN));
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
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Sp.getBus().unregister(this);
		Events.unregister(this);
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
	 * Search Param updates
	 */

	@Subscribe
	public void answerSearchParamUpdate(Sp.SpUpdateEvent event) {
		setGdeInfo(Sp.getParams().getOriginLocation(true),
			Sp.getParams().getDestinationLocation(true),
			Sp.getParams().getStartDate());
	}

	/*
	 * Calendar interaction
	 */

	public void scrollToMonth(YearMonth yearMonth) {
		if (mHistogramFrag != null) {
			mHistogramFrag.scrollToMonth(yearMonth);
		}
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
				mHeaderString = getString(R.string.when_to_return);
			}
			else {
				mHeaderString = getString(R.string.flight_trends);
			}
			mGdeHeaderTv.setText(mHeaderString);
		}
	}

	private void setErrorNoPos() {
		if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()) {
			String posURL = PointOfSale.getPointOfSale().getWebsiteUrl();
			mGdeErrorMessageView.setCaption(Html.fromHtml(getString(R.string.tablet_drop_down_flight_pos_unavailable_TEMPLATE, posURL)), posURL);
		}
		else {
			mGdeErrorMessageView.setCaption(getString(R.string.invalid_flights_pos));
		}
		mGdeErrorMessageView.clearActionButton();
		mGdeErrorMessageView.setVisibility(View.VISIBLE);
		mGdeHeaderTv.setVisibility(View.INVISIBLE);
		mGdeProgressBar.setVisibility(View.INVISIBLE);
		mGdePriceRangeTv.setText("");
	}

	private void setErrorNoResults() {
		String destination = StrUtils.formatCity(Sp.getParams().getDestination());
		mGdeErrorMessageView.setCaption(getString(R.string.missing_dates_flight_TEMPLATE, destination));
		mGdeErrorMessageView.setVisibility(View.VISIBLE);
		mGdeErrorMessageView.clearActionButton();
		mGdeErrorMessageView.setActionButton(R.string.select_dates, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.UserClickedSelectDatesButton());
			}
		});
		mGdeHeaderTv.setVisibility(View.INVISIBLE);
		mGdeProgressBar.setVisibility(View.INVISIBLE);
		mGdePriceRangeTv.setText("");
	}

	private void setErrorNoOrigin() {
		mGdeErrorMessageView.setCaption(getString(R.string.flight_trends_missing_origin));
		mGdeErrorMessageView.setVisibility(View.VISIBLE);
		mGdeErrorMessageView.setActionButton(R.string.missing_flight_info_button_prompt, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Events.post(new Events.ShowSearchFragment(ResultsSearchState.FLIGHT_ORIGIN));
			}
		});
		mGdeHeaderTv.setVisibility(View.INVISIBLE);
		mGdeProgressBar.setVisibility(View.INVISIBLE);
		mGdePriceRangeTv.setText("");
	}

	private void setToolTipView() {
		mGdeHeaderTv.setText(R.string.flight_trends);
		mGdeHeaderTv.setVisibility(View.VISIBLE);
		mHistogramC.setVisibility(View.GONE);
		mGdeErrorMessageView.setVisibility(View.GONE);
		mGdePriceRangeTv.setText("");
		mToolTipInvokeButton.setVisibility(View.INVISIBLE);
		mToolTipView.setVisibility(View.VISIBLE);

		mToolTipDoneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mGdeHeaderTv.setText(mHeaderString);
				mGdeHeaderTv.setVisibility(View.VISIBLE);
				mHistogramC.setVisibility(View.VISIBLE);
				mGdePriceRangeTv.setText(mPriceString);
				mToolTipInvokeButton.setVisibility(View.VISIBLE);
				mToolTipView.setVisibility(View.GONE);
			}
		});
	}

	private void setStateLoading() {
		mGdeErrorMessageView.setVisibility(View.GONE);
		mGdeErrorMessageView.clearActionButton();
		mGdeHeaderTv.setVisibility(View.INVISIBLE);
		mGdeProgressBar.setVisibility(View.VISIBLE);
		mGdePriceRangeTv.setText("");
	}

	protected void startOrResumeDownload(GdeDownloadFragment frag) {
		if (frag == null) {
			return;
		}

		frag.startOrResumeForRoute(mOrigin, mDestination, mDepartureDate);
		if (mOrigin == null) {
			setErrorNoOrigin();
		}
		else {
			setStateLoading();
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
	 * Otto
	 */

	@Subscribe
	public void onGdeDataAvailable(Events.GdeDataAvailable event) {
		if (mGdeProgressBar != null) {
			mGdeProgressBar.setVisibility(View.GONE);
		}
		mGdeErrorMessageView.setVisibility(View.GONE);
		mHistogramC.setVisibility(View.GONE);
		mToolTipView.setVisibility(View.GONE);
		mToolTipInvokeButton.setVisibility(View.INVISIBLE);
		mGdeHeaderTv.setVisibility(View.VISIBLE);

		FlightSearchHistogramResponse response = event.response;

		// Error/unexpected input
		if (response == null || response.hasErrors()) {
			if (mHistogramFrag != null) {
				mHistogramFrag.setHistogramData(null);
			}

			if (response == null) {
				Log.e("FLIGHT_GDE_SEARCH null response");
			}
			else if (response.hasErrors()) {
				Log.e("FLIGHT_GDE_SEARCH Errors:" + response.gatherErrorMessage(getActivity()));
			}

			if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()
				|| !PointOfSale.getPointOfSale().isFlightSearchEnabledTablet()) {
				setErrorNoPos();
			}
			else if (mOrigin == null) {
				setErrorNoOrigin();
			}
			else {
				setErrorNoResults();
			}
		}

		// Normal/expected GDE response
		else {
			Db.setFlightSearchHistogramResponse(response);

			int count = response.getCount();

			if (count == 0) {
				if (PointOfSale.getPointOfSale().displayFlightDropDownRoutes()
					|| !PointOfSale.getPointOfSale().isFlightSearchEnabledTablet()) {
					setErrorNoPos();
				}
				else {
					setErrorNoResults();
				}
			}
			else if (mHistogramFrag != null) {
				mHistogramC.setVisibility(View.VISIBLE);
				mToolTipInvokeButton.setVisibility(View.VISIBLE);
				mHistogramFrag.setHistogramData(response);
			}

			Money minMoney = response.getMinPrice();
			Money maxMoney = response.getMaxPrice();

			String priceAsString;
			if (count == 0) {
				priceAsString = "";
			}
			else if (count == 1 || minMoney.equals(maxMoney)) {
				priceAsString = minMoney.getFormattedMoney(Money.F_NO_DECIMAL);
			}
			else {
				priceAsString = minMoney.getFormattedMoney(Money.F_NO_DECIMAL)
					+ "-" + maxMoney.getFormattedMoney(Money.F_NO_DECIMAL);
			}
			mPriceString = priceAsString;
			mGdePriceRangeTv.setText(priceAsString);
		}
	}
}
