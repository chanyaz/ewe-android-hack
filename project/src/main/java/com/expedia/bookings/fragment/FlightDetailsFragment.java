package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ScrollView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.FlightInfoBarSection;
import com.expedia.bookings.section.FlightInfoSection;
import com.expedia.bookings.section.FlightSegmentSection;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.AnimUtils;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightDetailsFragment extends Fragment implements FlightUtils.OnBaggageFeeViewClicked {

	public static final String TAG = FlightDetailsFragment.class.getName();

	private static final String ARG_TRIP_LEG = "ARG_TRIP_LEG";
	private static final String ARG_LEG_POSITION = "ARG_LEG_POSITION";

	private FlightDetailsFragmentListener mListener;

	// Cached views
	private ScrollView mScrollView;
	private ViewGroup mInfoContainer;
	private FlightInfoBarSection mInfoBar;
	private TextView mFeesTextView;
	private TextView mFeesSecondaryTextView;
	private ViewGroup mFeesContainer;

	// Cached copies, not to be stored
	private FlightTripLeg mFlightTripLeg;
	private FlightTrip mFlightTrip;
	private FlightLeg mFlightLeg;

	// Temporary data set for animation
	private boolean mFeesContainerInScrollView;

	// We need to do two layout passes to figure out where everything goes
	private boolean mFirstLayoutPass = true;

	public static FlightDetailsFragment newInstance(FlightTrip trip, FlightLeg leg, int legPosition) {
		FlightDetailsFragment fragment = new FlightDetailsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_POSITION, legPosition);
		JSONUtils.putJSONable(args, ARG_TRIP_LEG, new FlightTripLeg(trip, leg));
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, FlightDetailsFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_flight_details, container, false);

		if (FragmentBailUtils.shouldBail(getActivity())) {
			return v;
		}

		final FlightTrip trip = getFlightTrip();
		FlightLeg leg = getFlightLeg();

		// Cache views
		mScrollView = Ui.findView(v, R.id.flight_info_scroll_view);
		mInfoContainer = Ui.findView(v, R.id.flight_info_container);
		mInfoBar = Ui.findView(v, R.id.info_bar);
		mFeesContainer = Ui.findView(v, R.id.fees_container);
		mFeesTextView = Ui.findView(v, R.id.fees_text_view);
		mFeesSecondaryTextView = Ui.findView(v, R.id.fees_secondary_text_view);

		// Format header
		mInfoBar.bindFlightDetails(trip, leg);

		// Format content
		// Depart from row
		FlightInfoSection departFromSection = FlightInfoSection.inflate(inflater, container);
		departFromSection.bind(R.drawable.ic_departure_arrow_small, getString(R.string.depart_from_TEMPLATE,
				StrUtils.formatWaypoint(leg.getSegment(0).getOriginWaypoint())));
		mInfoContainer.addView(departFromSection);

		// Add each card, with layovers in between
		final int cardMargins = (int) getResources().getDimension(R.dimen.flight_segment_margin);
		DateTime minTime = leg.getFirstWaypoint().getMostRelevantDateTime();
		DateTime maxTime = leg.getLastWaypoint().getMostRelevantDateTime();
		int segmentCount = leg.getSegmentCount();
		for (int a = 0; a < segmentCount; a++) {
			Flight flight = leg.getSegment(a);

			if (a != 0) {
				FlightInfoSection flightLayoverSection = FlightInfoSection.inflate(inflater, container);
				Flight prevFlight = leg.getSegment(a - 1);
				Layover layover = new Layover(prevFlight, flight);
				String duration = DateTimeUtils.formatDuration(getResources(), layover.mDuration);
				String waypoint = StrUtils.formatWaypoint(flight.getOriginWaypoint());
				flightLayoverSection.bind(R.drawable.ic_clock_small,
						HtmlCompat.fromHtml(getString(R.string.layover_duration_location_TEMPLATE, duration, waypoint)));
				mInfoContainer.addView(flightLayoverSection);
			}

			FlightSegmentSection flightSegmentSection = Ui.inflate(inflater,
					R.layout.section_flight_segment, mInfoContainer, false);
			flightSegmentSection.bind(flight, trip.getFlightSegmentAttributes(leg)[a], minTime, maxTime);
			MarginLayoutParams params = (MarginLayoutParams) flightSegmentSection.getLayoutParams();
			params.setMargins(0, cardMargins, 0, cardMargins);
			mInfoContainer.addView(flightSegmentSection);
		}

		// Arrive at row
		FlightInfoSection arriveAtSection = FlightInfoSection.inflate(inflater, container);
		arriveAtSection.bind(R.drawable.ic_return_arrow_small, getString(R.string.arrive_at_TEMPLATE,
				StrUtils.formatWaypoint(leg.getSegment(segmentCount - 1).getDestinationWaypoint())));
		mInfoContainer.addView(arriveAtSection);

		// Footer: https://mingle/projects/eb_ad_app/cards/660
		FlightUtils.configureBaggageFeeViews(getContext(), trip, leg, mFeesTextView, mFeesContainer,
			mFeesSecondaryTextView, true, getChildFragmentManager(), this);

		mFirstLayoutPass = true;
		v.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (mFirstLayoutPass) {
					// In the first layout pass, we adapt the info container for however tall the info bar/fees
					// container will be.

					LayoutUtils.addPadding(mInfoContainer, 0, mInfoBar.getHeight(), 0, mFeesContainer.getHeight());
					mFirstLayoutPass = false;
				}
				else {
					// In the second layout pass, we determine if the info container will be scrolling within
					// its parent (and thus the baggage fees link would need to be moved)

					// Remove the global layout listener
					v.getViewTreeObserver().removeOnPreDrawListener(this);

					// This is for determining whether the baggage info text view should
					// appear on the bottom of the screen or scrolling with the content
					mFeesContainerInScrollView = mScrollView.getHeight() < mInfoContainer.getHeight();
					if (mFeesContainerInScrollView) {
						((ViewGroup) mFeesContainer.getParent()).removeView(mFeesContainer);
						mInfoContainer.addView(mFeesContainer);

						MarginLayoutParams lp = (MarginLayoutParams) mFeesContainer.getLayoutParams();
						mInfoContainer.setPadding(mInfoContainer.getPaddingLeft(), mInfoContainer.getPaddingTop(),
								mInfoContainer.getPaddingRight(), cardMargins * 2);
						lp.topMargin = cardMargins * 2;
						lp.bottomMargin = 0;
					}

					mListener.onFlightDetailsLayout(FlightDetailsFragment.this);
				}

				// We always return false so that we don't pre-draw before the animation starts
				return false;
			}
		});

		return v;
	}

	@Override
	public void onStart() {
		super.onStart();
		OmnitureTracking.trackPageLoadFlightSearchResultsDetails(
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
		List<Animator> set = new ArrayList<>();
		float[] values = new float[2];
		PropertyValuesHolder pvhAlpha = AnimUtils.createFadePropertyValuesHolder(enter);
		PropertyValuesHolder pvhTranslation = null;
		PropertyValuesHolder pvhScale = null;

		// A list of views to set in a HW layer.  We only do this for complex Views; for simple
		// Views it is somewhat a waste of time (as we're actually adding work, due to the slight
		// overhead of HW layers).  It is an experiment right now which are complex enough.
		final List<View> hwLayerViews = new ArrayList<>();

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

			pvhTranslation = PropertyValuesHolder.ofFloat("translationY", values);

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

				pvhScale = PropertyValuesHolder.ofFloat("scaleY", values);
				set.add(AnimUtils.ofPropertyValuesHolder(child, pvhAlpha, pvhTranslation, pvhScale));
			}
			else {
				set.add(AnimUtils.ofPropertyValuesHolder(child, pvhAlpha, pvhTranslation));
			}

			hwLayerViews.add(child);
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
		pvhTranslation = PropertyValuesHolder.ofFloat("translationY", values);
		set.add(AnimUtils.ofPropertyValuesHolder(mInfoBar, pvhAlpha, pvhTranslation));

		hwLayerViews.add(mInfoBar);

		// Animate the baggage fee (if it's not in the scroll view)
		if (!mFeesContainerInScrollView) {
			if (enter) {
				values[0] = mFeesContainer.getHeight();
				values[1] = 0;
			}
			else {
				values[0] = 0;
				values[1] = mFeesContainer.getHeight();
			}

			pvhTranslation = PropertyValuesHolder.ofFloat("translationY", values);
			set.add(AnimUtils.ofPropertyValuesHolder(mFeesContainer, pvhAlpha, pvhTranslation));

			hwLayerViews.add(mFeesContainer);
		}

		AnimatorSet animSet = AnimUtils.playTogether(set);

		animSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				for (View view : hwLayerViews) {
					view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
				}
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				for (View view : hwLayerViews) {
					view.setLayerType(View.LAYER_TYPE_NONE, null);
				}
			}
		});

		return animSet;
	}

	@Override
	public void onBaggageFeeViewClicked(String title, String url) {
		String trackingName = null;
		int legPosition = getArguments().getInt(ARG_LEG_POSITION, 0);
		if (legPosition == 0) {
			if (Db.getFlightSearch().getSearchParams().isRoundTrip()) {
				trackingName = WebViewFragment.TrackingName.BaggageFeeOutbound.name();
			}
			else {
				trackingName = WebViewFragment.TrackingName.BaggageFeeOneWay.name();
			}
		}
		else if (legPosition == 1) {
			trackingName = WebViewFragment.TrackingName.BaggageFeeInbound.name();
		}

		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
		builder.setUrl(url);
		builder.setTheme(R.style.FlightTheme);
		builder.setTitle(title);
		builder.setTrackingName(trackingName);
		builder.setAllowMobileRedirects(false);
		getActivity().startActivity(builder.getIntent());
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface FlightDetailsFragmentListener {
		void onFlightDetailsLayout(FlightDetailsFragment fragment);
	}
}
