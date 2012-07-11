package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightCheckoutActivity;
import com.expedia.bookings.activity.FlightDetailsActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightDetailsResponse;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

public class FlightTripOverviewFragment extends Fragment {

	private static final String ARG_TRIP_KEY = "ARG_TRIP_KEY";

	private static final String KEY_DETAILS = "KEY_DETAILS";

	private static final String INSTANCE_REQUESTED_DETAILS = "INSTANCE_REQUESTED_DETAILS";

	private static final int FLIGHT_LEG_BOTTOM_MARGIN = 20;

	private FlightTrip mTrip;
	private FlightTrip mOffer;

	private TextView mTripCostTextView;
	private ArrayList<SectionFlightLeg> mFlights;
	private ViewGroup mFlightContainer;
	private Button mCheckoutBtn;

	private boolean mRequestedDetails = false;

	public static FlightTripOverviewFragment newInstance(String tripKey) {
		FlightTripOverviewFragment fragment = new FlightTripOverviewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TRIP_KEY, tripKey);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRequestedDetails = savedInstanceState.getBoolean(INSTANCE_REQUESTED_DETAILS, false);
		}
		else {
			// If we're launching this for the first time, clear the current details response
			Db.setFlightDetails(null);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.activity_flight_trip_overview, container, false);

		mTripCostTextView = Ui.findView(v, R.id.trip_cost);
		mFlights = new ArrayList<SectionFlightLeg>();
		mFlightContainer = Ui.findView(v, R.id.flight_legs_container);
		mCheckoutBtn = Ui.findView(v, R.id.checkout_btn);

		mCheckoutBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FlightCheckoutActivity.class);
				intent.putExtra(FlightDetailsActivity.EXTRA_TRIP_KEY, mTrip.getProductKey());
				startActivity(intent);
			}
		});

		String tripKey = getArguments().getString(ARG_TRIP_KEY);
		mTrip = Db.getFlightSearch().getFlightTrip(tripKey);

		// See if we have flight details we can use, first.
		FlightDetailsResponse flightDetails = Db.getFlightDetails();
		if (flightDetails != null && !flightDetails.hasErrors()
				&& flightDetails.getOffer().getProductKey().equals(tripKey)) {
			mOffer = flightDetails.getOffer();
		}
		else {
			mCheckoutBtn.setEnabled(false);

			// Use the original search response as the offer for now
			mOffer = mTrip;

			// Begin loading flight details in the background, if we haven't already
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (!bd.isDownloading(KEY_DETAILS) && !mRequestedDetails) {
				bd.startDownload(KEY_DETAILS, mFlightDetailsDownload, mFlightDetailsCallback);
			}
		}

		//Set up the Activity's views.
		FlightLeg arrLeg = mTrip.getLeg(0);
		String cityName = arrLeg.getSegment(arrLeg.getSegmentCount() - 1).mDestination.getAirport().mCity;

		Flight firstSeg = arrLeg.getSegment(0);
		Calendar cal = firstSeg.mOrigin.getMostRelevantDateTime();
		String monthStr = DateUtils.getMonthString(cal.get(Calendar.MONTH), DateUtils.LENGTH_LONG);
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);

		String date = String
				.format(getResources().getString(R.string.long_form_date_TEMPLATE), monthStr, day, year);
		Ui.setText(v, R.id.departure_date_long_form, date);
		Ui.setText(v, R.id.your_trip_to,
				String.format(getResources().getString(R.string.your_trip_to_TEMPLATE), cityName));
		Ui.setText(v, R.id.traveler_count,
				String.format(getResources().getString(R.string.number_of_travelers_TEMPLATE), "1"));

		//Inflate and store the sections
		SectionFlightLeg tempFlight;
		float density = getActivity().getResources().getDisplayMetrics().density;
		for (int i = 0; i < mTrip.getLegCount(); i++) {
			tempFlight = (SectionFlightLeg) inflater.inflate(R.layout.section_display_flight_leg, null);
			if (i < mTrip.getLegCount() - 1) {
				tempFlight.setIsOutbound(true);
				LinearLayout.LayoutParams tempFlightLayoutParams = (LayoutParams) tempFlight.getLayoutParams();
				if (tempFlightLayoutParams == null) {
					tempFlightLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				}
				tempFlightLayoutParams.bottomMargin = (int) Math.floor(FLIGHT_LEG_BOTTOM_MARGIN * density);
				tempFlight.setLayoutParams(tempFlightLayoutParams);
			}
			else {
				tempFlight.setIsOutbound(false);
			}

			tempFlight.bind(mTrip.getLeg(i));

			mFlights.add(tempFlight);
			mFlightContainer.addView(tempFlight);
		}

		// Initial bind of data
		bindOffer();

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DETAILS)) {
			bd.registerDownloadCallback(KEY_DETAILS, mFlightDetailsCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_DETAILS);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_REQUESTED_DETAILS, mRequestedDetails);
	}

	public void bindOffer() {
		mTripCostTextView.setText(mOffer.getTotalFare().getFormattedMoney());
	}

	//////////////////////////////////////////////////////////////////////////
	// Flight details download

	private Download<FlightDetailsResponse> mFlightDetailsDownload = new Download<FlightDetailsResponse>() {
		@Override
		public FlightDetailsResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DETAILS, services);
			return services.flightDetails(mTrip.getProductKey(), 0);
		}
	};

	private OnDownloadComplete<FlightDetailsResponse> mFlightDetailsCallback = new OnDownloadComplete<FlightDetailsResponse>() {
		@Override
		public void onDownload(FlightDetailsResponse results) {
			Db.setFlightDetails(results);
			mRequestedDetails = true;

			if (results == null) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
						getString(R.string.error_server));
				dialogFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "errorFragment");
			}
			else if (results.hasErrors()) {
				String error = results.getErrors().get(0).getPresentableMessage(getActivity());
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null, error);
				dialogFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "errorFragment");
			}
			else {
				mCheckoutBtn.setEnabled(true);

				if (results.hasPriceChanged()) {
					String oldFare = results.getOldOffer().getTotalFare().getFormattedMoney();
					String newFare = results.getOffer().getTotalFare().getFormattedMoney();
					String msg = getString(R.string.price_change_alert_TEMPLATE, oldFare, newFare);

					DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null, msg);
					dialogFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(),
							"noticeFragment");
				}

				mOffer = results.getOffer();

				bindOffer();
			}
		}
	};
}
