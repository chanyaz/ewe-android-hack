package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.FlightInfoSection;
import com.expedia.bookings.section.FlightSegmentSection;
import com.expedia.bookings.section.InfoBarSection;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.SupportUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlightDetailsFragment extends Fragment {

	public static final String TAG = FlightDetailsFragment.class.getName();

	private static final String ARG_TRIP_LEG = "ARG_TRIP_LEG";
	private static final String ARG_LEG_POSITION = "ARG_LEG_POSITION";

	private FlightDetailsFragmentListener mListener;

	// Cached views
	private ScrollView mScrollView;
	private ViewGroup mInfoContainer;
	private InfoBarSection mInfoBar;
	private TextView mBaggageInfoTextView;
	private TextView mBaggageFeesApplyTextView;
	private ViewGroup mBaggageContainer;

	// Cached copies, not to be stored
	private FlightTripLeg mFlightTripLeg;
	private FlightTrip mFlightTrip;
	private FlightLeg mFlightLeg;

	// Temporary data set for animation
	private boolean mBaggageInScrollView;

	public static FlightDetailsFragment newInstance(FlightTrip trip, FlightLeg leg, int legPosition) {
		FlightDetailsFragment fragment = new FlightDetailsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_POSITION, legPosition);
		JSONUtils.putJSONable(args, ARG_TRIP_LEG, new FlightTripLeg(trip, leg));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof FlightDetailsFragmentListener) {
			mListener = (FlightDetailsFragmentListener) activity;
		}
		else {
			throw new RuntimeException("FlightDetailsFragment activity must implement listener!");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_flight_details, container, false);

		LayoutUtils.adjustPaddingForOverlayMode(getActivity(), v, false);

		FlightTrip trip = getFlightTrip();
		FlightLeg leg = getFlightLeg();

		// Cache views
		mScrollView = Ui.findView(v, R.id.flight_info_scroll_view);
		mInfoContainer = Ui.findView(v, R.id.flight_info_container);
		mInfoBar = Ui.findView(v, R.id.info_bar);
		mBaggageInfoTextView = Ui.findView(v, R.id.baggage_fee_text_view);
		mBaggageFeesApplyTextView = Ui.findView(v, R.id.carry_on_baggage_fees_apply_textview);
		mBaggageContainer = Ui.findView(v, R.id.bagge_fee_container);

		//We show the baggage fees apply message for Spirit airlines only
		for (String airline : leg.getPrimaryAirlines()) {
			if (airline.equalsIgnoreCase("NK")) {
				mBaggageFeesApplyTextView.setVisibility(View.VISIBLE);
				break;
			}
		}

		// Format header
		mInfoBar.bindFlightDetails(trip, leg);

		// Format content
		// Depart from row
		FlightInfoSection departFromSection = FlightInfoSection.inflate(inflater, container);
		departFromSection.bind(R.drawable.ic_departure_arrow_small, getString(R.string.depart_from_TEMPLATE,
				StrUtils.formatWaypoint(leg.getSegment(0).mOrigin)));
		mInfoContainer.addView(departFromSection);

		// Add each card, with layovers in between
		final int cardMargins = (int) getResources().getDimension(R.dimen.flight_segment_margin);
		Calendar minTime = leg.getFirstWaypoint().getMostRelevantDateTime();
		Calendar maxTime = leg.getLastWaypoint().getMostRelevantDateTime();
		int segmentCount = leg.getSegmentCount();
		for (int a = 0; a < segmentCount; a++) {
			Flight flight = leg.getSegment(a);

			if (a != 0) {
				FlightInfoSection flightLayoverSection = FlightInfoSection.inflate(inflater, container);
				Flight prevFlight = leg.getSegment(a - 1);
				Layover layover = new Layover(prevFlight, flight);
				String duration = DateTimeUtils.formatDuration(getResources(), layover.mDuration);
				String waypoint = StrUtils.formatWaypoint(flight.mOrigin);
				flightLayoverSection.bind(R.drawable.ic_clock_small,
						Html.fromHtml(getString(R.string.layover_duration_location_TEMPLATE, duration, waypoint)));
				mInfoContainer.addView(flightLayoverSection);
			}

			FlightSegmentSection flightSegmentSection = (FlightSegmentSection) inflater.inflate(
					R.layout.section_flight_segment, mInfoContainer, false);
			flightSegmentSection.bind(flight, trip.getFlightSegmentAttributes(leg).get(a), minTime, maxTime);
			MarginLayoutParams params = (MarginLayoutParams) flightSegmentSection.getLayoutParams();
			params.setMargins(0, cardMargins, 0, cardMargins);
			mInfoContainer.addView(flightSegmentSection);
		}

		// Arrive at row
		FlightInfoSection arriveAtSection = FlightInfoSection.inflate(inflater, container);
		arriveAtSection.bind(R.drawable.ic_return_arrow_small, getString(R.string.arrive_at_TEMPLATE,
				StrUtils.formatWaypoint(leg.getSegment(segmentCount - 1).mDestination)));
		mInfoContainer.addView(arriveAtSection);

		mBaggageInfoTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FlightLeg leg = getFlightLeg();

				//TODO:We need to set the correct url based on Point of Sale
				String url = SupportUtils.getBaggageFeeUrl(leg.getFirstWaypoint().mAirportCode,
						leg.getLastWaypoint().mAirportCode);

				Intent baggageIntent = new Intent(getActivity(), WebViewActivity.class);
				baggageIntent.putExtra(WebViewActivity.ARG_URL, url);
				baggageIntent.putExtra(WebViewActivity.ARG_STYLE_RES_ID, R.style.FlightTheme);
				baggageIntent.putExtra(WebViewActivity.ARG_DISABLE_SIGN_IN, true);

				int legPosition = getArguments().getInt(ARG_LEG_POSITION, 0);
				if (legPosition == 0) {
					if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
						baggageIntent.putExtra(WebViewActivity.ARG_TRACKING_NAME,
								WebViewFragment.TrackingName.BaggageFeeOutbound.name());
					}
					else {
						baggageIntent.putExtra(WebViewActivity.ARG_TRACKING_NAME,
								WebViewFragment.TrackingName.BaggageFeeOneWay.name());
					}
				}
				else if (legPosition == 1) {
					baggageIntent.putExtra(WebViewActivity.ARG_TRACKING_NAME,
							WebViewFragment.TrackingName.BaggageFeeInbound.name());
				}

				getActivity().startActivity(baggageIntent);
			}
		});

		// We set the entire view invisible at first, so we can measure where we want everything to end up
		v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				// Remove the global layout listener, since we only want to do this once
				v.getViewTreeObserver().removeGlobalOnLayoutListener(this);

				// This is for determining whether the baggage info text view should
				// appear on the bottom of the screen or scrolling with the content
				mBaggageInScrollView = mScrollView.getHeight() < mInfoContainer.getHeight();
				if (mBaggageInScrollView) {
					((ViewGroup) mBaggageContainer.getParent()).removeView(mBaggageContainer);
					mInfoContainer.addView(mBaggageContainer);

					MarginLayoutParams lp = (MarginLayoutParams) mBaggageContainer.getLayoutParams();
					mInfoContainer.setPadding(mInfoContainer.getPaddingLeft(), mInfoContainer.getPaddingTop(),
							mInfoContainer.getPaddingRight(), cardMargins * 2);
					lp.topMargin = cardMargins * 2;
					lp.bottomMargin = 0;

				}

				mListener.onFlightDetailsLayout(FlightDetailsFragment.this);
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

	//////////////////////////////////////////////////////////////////////////
	// Animators

	public Animator createAnimator(int top, int bottom, boolean enter) {
		View v = getView();
		List<Animator> set = new ArrayList<Animator>();
		float[] values = new float[2];

		int center = (top + bottom) / 2;
		int height = bottom - top;

		// Animate the individual cards
		int childCount = mInfoContainer.getChildCount();
		for (int a = 0; a < childCount; a++) {
			View child = mInfoContainer.getChildAt(a);
			int translation = center - ((child.getTop() + child.getBottom()) / 2);
			if (enter) {
				values[0] = translation;
				values[1] = 0;
			}
			else {
				values[0] = 0;
				values[1] = translation;
			}
			set.add(ObjectAnimator.ofFloat(child, "translationY", values));

			// Scale the flight cards a bit, so they look like they are growing out of/into a row
			if (child instanceof FlightSegmentSection) {
				float change = (float) height / (float) child.getHeight();
				if (enter) {
					values[0] = change;
					values[1] = 1;
				}
				else {
					values[0] = 1;
					values[1] = change;
				}
				set.add(ObjectAnimator.ofFloat(child, "scaleY", values));
			}
		}

		// Animate the header
		if (enter) {
			values[0] = -mInfoBar.getHeight();
			values[1] = 0;
		}
		else {
			values[0] = 0;
			values[1] = -mInfoBar.getHeight();
		}
		set.add(ObjectAnimator.ofFloat(mInfoBar, "translationY", values));

		// Animate the baggage fee (if it's not in the scroll view)
		if (!mBaggageInScrollView) {
			if (enter) {
				values[0] = mBaggageContainer.getHeight();
				values[1] = 0;
			}
			else {
				values[0] = 0;
				values[1] = mBaggageContainer.getHeight();
			}
			set.add(ObjectAnimator.ofFloat(mBaggageContainer, "translationY", values));
		}

		// Make the entire screen fade out
		set.add(AnimUtils.createFadeAnimator(v, enter));

		return AnimUtils.playTogether(set);
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface FlightDetailsFragmentListener {
		public void onFlightDetailsLayout(FlightDetailsFragment fragment);
	}
}
