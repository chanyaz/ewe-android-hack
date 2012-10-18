package com.expedia.bookings.fragment;

import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.BaggageFeeActivity;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.FlightLayoverSection;
import com.expedia.bookings.section.FlightPathSection;
import com.expedia.bookings.section.FlightSegmentSection;
import com.expedia.bookings.section.InfoBarSection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;

public class FlightDetailsFragment extends Fragment {

	public static final String TAG = FlightDetailsFragment.class.getName();

	private static final String ARG_TRIP_LEG = "ARG_TRIP_LEG";
	private static final String ARG_LEG_POSITION = "ARG_LEG_POSITION";

	// Cached views
	private ScrollView mScrollView;
	private ViewGroup mInfoContainer;
	private TextView mBaggageInfoTextView;

	// Cached copies, not to be stored
	private FlightTripLeg mFlightTripLeg;
	private FlightTrip mFlightTrip;
	private FlightLeg mFlightLeg;

	public static FlightDetailsFragment newInstance(FlightTrip trip, FlightLeg leg, int legPosition) {
		FlightDetailsFragment fragment = new FlightDetailsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_POSITION, legPosition);
		JSONUtils.putJSONable(args, ARG_TRIP_LEG, new FlightTripLeg(trip, leg));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_details, container, false);

		LayoutUtils.adjustPaddingForOverlayMode(getActivity(), v, false);

		FlightTrip trip = getFlightTrip();
		FlightLeg leg = getFlightLeg();

		// Format header
		InfoBarSection infoBar = Ui.findView(v, R.id.info_bar);
		infoBar.bindFlightDetails(trip, leg);

		// Format content
		mInfoContainer = Ui.findView(v, R.id.flight_info_container);

		// Depart from row
		FlightPathSection departFromSection = (FlightPathSection) inflater.inflate(R.layout.section_flight_path,
				mInfoContainer, false);
		departFromSection.bind(leg.getSegment(0), true);
		mInfoContainer.addView(departFromSection);

		// Add each card, with layovers in between
		final int cardMargins = (int) getResources().getDimension(R.dimen.flight_segment_margin);
		Calendar minTime = leg.getFirstWaypoint().getMostRelevantDateTime();
		Calendar maxTime = leg.getLastWaypoint().getMostRelevantDateTime();
		int segmentCount = leg.getSegmentCount();
		for (int a = 0; a < segmentCount; a++) {
			if (a != 0) {
				FlightLayoverSection flightLayoverSection = (FlightLayoverSection) inflater.inflate(
						R.layout.section_flight_layover, mInfoContainer, false);
				flightLayoverSection.bind(leg.getSegment(a - 1), leg.getSegment(a));
				mInfoContainer.addView(flightLayoverSection);
			}

			FlightSegmentSection flightSegmentSection = (FlightSegmentSection) inflater.inflate(
					R.layout.section_flight_segment, mInfoContainer, false);
			flightSegmentSection.bind(leg.getSegment(a), trip.getFlightSegmentAttributes(leg).get(a), minTime, maxTime);
			MarginLayoutParams params = (MarginLayoutParams) flightSegmentSection.getLayoutParams();
			params.setMargins(0, cardMargins, 0, cardMargins);
			mInfoContainer.addView(flightSegmentSection);
		}

		// Arrive at row
		FlightPathSection arriveAtSection = (FlightPathSection) inflater.inflate(R.layout.section_flight_path,
				mInfoContainer, false);
		arriveAtSection.bind(leg.getSegment(segmentCount - 1), false);
		mInfoContainer.addView(arriveAtSection);

		mBaggageInfoTextView = Ui.findView(v, R.id.baggage_fee_text_view);
		mBaggageInfoTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FlightLeg leg = getFlightLeg();
				Intent baggageIntent = new Intent(getActivity(), BaggageFeeActivity.class);
				baggageIntent.putExtra(BaggageFeeFragment.TAG_ORIGIN, leg.getFirstWaypoint().mAirportCode);
				baggageIntent.putExtra(BaggageFeeFragment.TAG_DESTINATION, leg.getLastWaypoint().mAirportCode);
				baggageIntent.putExtra(BaggageFeeFragment.ARG_LEG_POSITION, getArguments().getInt(ARG_LEG_POSITION, 0));
				getActivity().startActivity(baggageIntent);
			}
		});

		// This is for determining whether the baggage info text view should
		// appear on the bottom of the screen or scrolling with the content
		mScrollView = Ui.findView(v, R.id.flight_info_scroll_view);
		mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (mScrollView.getHeight() < mInfoContainer.getHeight()) {
					// Move baggage container to the info container 
					((ViewGroup) mBaggageInfoTextView.getParent()).removeView(mBaggageInfoTextView);
					mInfoContainer.addView(mBaggageInfoTextView);

					// Reset the layout margins/padding to account for move
					MarginLayoutParams lp = (MarginLayoutParams) mBaggageInfoTextView.getLayoutParams();
					mInfoContainer.setPadding(mInfoContainer.getPaddingLeft(), mInfoContainer.getPaddingTop(),
							mInfoContainer.getPaddingRight(), cardMargins);
					lp.topMargin = cardMargins;
					lp.bottomMargin = 0;
				}

				// Remove the listener so this only happens once
				mScrollView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightSearchResultsDetails(getActivity(),
				getArguments().getInt(ARG_LEG_POSITION, 0));
	}

	public FlightTripLeg getFlightTripLeg() {
		if (mFlightTripLeg == null) {
			mFlightTripLeg = JSONUtils.getJSONable(getArguments(), ARG_TRIP_LEG, FlightTripLeg.class);
		}
		return mFlightTripLeg;
	}

	public FlightTrip getFlightTrip() {
		if (mFlightTrip == null) {
			mFlightTrip = getFlightTripLeg().getFlightTrip();
		}
		return mFlightTrip;
	}

	public FlightLeg getFlightLeg() {
		if (mFlightLeg == null) {
			mFlightLeg = getFlightTripLeg().getFlightLeg();
		}
		return mFlightLeg;
	}
}
