package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.ViewUtils;
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
			flightSegmentSection.bind(flight, trip.getFlightSegmentAttributes(leg)[a], minTime, maxTime);
			MarginLayoutParams params = (MarginLayoutParams) flightSegmentSection.getLayoutParams();
			params.setMargins(0, cardMargins, 0, cardMargins);
			mInfoContainer.addView(flightSegmentSection);
		}

		// Arrive at row
		FlightInfoSection arriveAtSection = FlightInfoSection.inflate(inflater, container);
		arriveAtSection.bind(R.drawable.ic_return_arrow_small, getString(R.string.arrive_at_TEMPLATE,
				StrUtils.formatWaypoint(leg.getSegment(segmentCount - 1).mDestination)));
		mInfoContainer.addView(arriveAtSection);

		// Footer: https://mingle/projects/eb_ad_app/cards/660

		// Configure the first TextView, "Baggage Fee Information"
		int textViewResId;
		int drawableResId;
		if (leg.isSpirit()) {
			textViewResId = R.string.carry_on_baggage_fees_apply;
			drawableResId = R.drawable.ic_suitcase_baggage_fee;
		}
		else if (trip.hasBagFee()) {
			textViewResId = R.string.checked_baggage_not_included;
			drawableResId = R.drawable.ic_suitcase_baggage_fee;
		}
		else {
			textViewResId = R.string.baggage_fee_info;
			drawableResId = R.drawable.ic_suitcase_small;
		}

		ViewUtils.setAllCaps(mFeesTextView);
		mFeesTextView.setText(textViewResId);
		mFeesTextView.setCompoundDrawablesWithIntrinsicBounds(drawableResId, 0, 0, 0);

		// Configure the second TextView, "Payment Fees Apply"
		if (trip.getMayChargeObFees()) {
			mFeesSecondaryTextView.setVisibility(View.VISIBLE);
			mFeesSecondaryTextView.setText(getString(R.string.payment_and_baggage_fees_may_apply));
			ViewUtils.setAllCaps(mFeesSecondaryTextView);

			mFeesContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					AdditionalFeesDialogFragment dialogFragment = AdditionalFeesDialogFragment.newInstance(
							trip.getBaggageFeesUrl(), Db.getFlightSearch().getSearchResponse().getObFeesDetails());
					dialogFragment.show(((FragmentActivity) getActivity()).getSupportFragmentManager(),
							"additionalFeesDialog");
				}
			});
		}
		else {
			mFeesContainer.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int legPosition = getArguments().getInt(ARG_LEG_POSITION, 0);
					String trackingName = null;
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
					builder.setUrl(trip.getBaggageFeesUrl());
					builder.setTheme(R.style.FlightTheme);
					builder.setTitle(R.string.baggage_fees);
					builder.setTrackingName(trackingName);
					builder.setAllowMobileRedirects(false);
					getActivity().startActivity(builder.getIntent());
				}
			});
		}

		v.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
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
					v.getViewTreeObserver().removeGlobalOnLayoutListener(this);

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
		if (!mFeesContainerInScrollView) {
			if (enter) {
				values[0] = mFeesContainer.getHeight();
				values[1] = 0;
			}
			else {
				values[0] = 0;
				values[1] = mFeesContainer.getHeight();
			}
			set.add(ObjectAnimator.ofFloat(mFeesContainer, "translationY", values));
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
