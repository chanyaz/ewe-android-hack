package com.expedia.bookings.fragment;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.interfaces.IResultsFlightLegSelected;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.section.FlightSegmentSection;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.RingedCountView;
import com.expedia.bookings.widget.TextView;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * ResultsFlightDetailsFragment: The flight details fragment designed for tablet results 2013
 */
public class ResultsFlightDetailsFragment extends Fragment implements FlightUtils.OnBaggageFeeViewClicked {

	private static final String ARG_LEG_NUMBER = "ARG_LEG_NUMBER";
	private static final int NUM_SCROLLVIEW_HEADERS = 2; // Stats container + grey divider

	public static ResultsFlightDetailsFragment newInstance(int legNumber) {
		ResultsFlightDetailsFragment frag = new ResultsFlightDetailsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_NUMBER, legNumber);
		frag.setArguments(args);
		return frag;
	}

	// Views
	private TouchableFrameLayout mRootC;
	private ViewGroup mDetailsC;
	private ScrollView mScrollC;
	private FlightLegSummarySectionTablet mAnimationFlightRow;

	private TextView mFromAndToHeaderTv;
	private TextView mTimeHeaderTv;
	private TextView mAddTripTv;
	private TextView mPriceTv;

	private ViewGroup mOnTimeContainer;
	private RingedCountView mOnTimeRingView;
	private TextView mOnTimeTextView;
	private TextView mFlightDistanceTv;
	private TextView mFlightDurationTv;
	private ViewGroup mFlightLegsC;
	private ViewGroup mBaggageFeesLinkC;
	private TextView mBaggageFeesLinkPrimaryTv;
	private TextView mBaggageFeesLinkSecondaryTv;
	private FlightLegSummarySectionTablet mSelectedFlightLegAnimationRowSection;

	//listeners
	private IResultsFlightLegSelected mFlightLegSelectedListener;

	// Position and size vars
	int mDetailsPositionLeft = -1;
	int mDetailsPositionTop = -1;
	int mDetailsWidth = -1;
	int mDetailsHeight = -1;
	int mDetailsVerticalMarginHeight = -1;
	int mRowPositionLeft = -1;
	int mRowPositionTop = -1;
	int mRowWidth = -1;
	int mRowHeight = -1;

	// Misc
	private int mLegNumber = -1;
	private boolean mBindInOnResume = false;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mFlightLegSelectedListener = Ui.findFragmentListener(this, IResultsFlightLegSelected.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_flight_details, null);
		mScrollC = Ui.findView(mRootC, R.id.flight_details_scroll_view);
		mAnimationFlightRow = Ui.findView(mRootC, R.id.details_animation_row);
		mAnimationFlightRow.setPivotX(0);
		mAnimationFlightRow.setPivotY(0);
		mDetailsC = Ui.findView(mRootC, R.id.details_container);
		mDetailsC.setPivotX(0);
		mDetailsC.setPivotY(0);

		mFromAndToHeaderTv = Ui.findView(mRootC, R.id.details_from_and_to_header);
		mTimeHeaderTv = Ui.findView(mRootC, R.id.details_time_header);
		mAddTripTv = Ui.findView(mRootC, R.id.details_add_trip_button);
		mPriceTv = Ui.findView(mRootC, R.id.details_price_text_view);

		mOnTimeContainer = Ui.findView(mRootC, R.id.flight_punctuality_container);
		mOnTimeRingView = Ui.findView(mRootC, R.id.flight_on_time_ring_view);
		mOnTimeRingView.setCountText("");
		mOnTimeTextView = Ui.findView(mRootC, R.id.flight_punctuality_text_view);
		mFlightDistanceTv = Ui.findView(mRootC, R.id.flight_miles_text_view);
		mFlightDurationTv = Ui.findView(mRootC, R.id.flight_overall_duration_text_view);
		mFlightLegsC = Ui.findView(mRootC, R.id.flight_legs_container);

		mBaggageFeesLinkC = Ui.findView(mRootC, R.id.fees_container);
		mBaggageFeesLinkPrimaryTv = Ui.findView(mRootC, R.id.fees_text_view);
		mBaggageFeesLinkSecondaryTv = Ui.findView(mRootC, R.id.fees_secondary_text_view);

		mSelectedFlightLegAnimationRowSection = Ui.findView(mRootC, R.id.details_animation_row);

		setLegPosition(getArguments().getInt(ARG_LEG_NUMBER));

		applyDetailsDimensions();
		applyRowDimensions();

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mBindInOnResume) {
			mBindInOnResume = false;
			bindWithDb();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	public void setLegPosition(int legNumber) {
		mLegNumber = legNumber;
	}

	public void bindWithDb() {
		if (!isAdded()) {
			mBindInOnResume = true;
			return;
		}

		FlightTripLeg flightTripLeg = Db.getFlightSearch().getSelectedLegs()[mLegNumber];
		FlightTrip trip = flightTripLeg.getFlightTrip();
		FlightLeg flightLeg = flightTripLeg.getFlightLeg();
		Flight flight;

		final Context context = getActivity();
		final Resources res = getResources();

		// Grey header

		// Boston to San Francisco
		// Waypoint
		String departurePlace = flightLeg.getAirport(true).mCity;
		String arrivalPlace = flightLeg.getAirport(false).mCity;
		String places = res.getString(R.string.date_range_TEMPLATE, departurePlace, arrivalPlace);
		mFromAndToHeaderTv.setText(places);

		// 10:05 AM to 2:20 PM
		String departure = formatTime(flightLeg.getFirstWaypoint().getBestSearchDateTime());
		String arrival = formatTime(flightLeg.getLastWaypoint().getBestSearchDateTime());
		String time = res.getString(R.string.date_range_TEMPLATE, departure, arrival);
		mTimeHeaderTv.setText(time);

		// Add for $390
		mPriceTv.setText(trip.getAverageTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		mAddTripTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSelectedFlightLegAnimationRowSection.bind(Db.getFlightSearch(), mLegNumber);
				mFlightLegSelectedListener.onTripAdded(mLegNumber);
			}
		});

		// Statistics
		mFlightDistanceTv.setText(FlightUtils.formatDistance(context, flightLeg, false));
		mFlightDurationTv.setText(FlightUtils.formatDuration(context, flightLeg));

		flight = Db.getFlightSearch().getSelectedLegs()[mLegNumber].getFlightLeg().getSegment(0);
		if (flight.mOnTimePercentage > 0.0f) {
			mOnTimeContainer.setVisibility(View.VISIBLE);
			mOnTimeRingView.setPercent(flight.mOnTimePercentage);
			String formatPercent = Long.toString(Math.round(flight.mOnTimePercentage * 100));
			mOnTimeTextView.setText(getActivity().getString(R.string.flight_on_time_TEMPLATE, formatPercent));
		}
		else {
			mOnTimeContainer.setVisibility(View.GONE);
		}

		// Flight Leg container

		//Arrival / Departure times
		DateTime depTime = flightLeg.getFirstWaypoint().getBestSearchDateTime();
		DateTime arrTime = flightLeg.getLastWaypoint().getBestSearchDateTime();

		FlightSegmentSection flightSegmentSection;

		int numFlights = flightLeg.getSegmentCount();

		// TODO recycle and rebind children views if we need it for performance
		// Remove all components below the statistsics header
		while (mFlightLegsC.getChildCount() > NUM_SCROLLVIEW_HEADERS) {
			mFlightLegsC.removeViewAt(NUM_SCROLLVIEW_HEADERS);
		}

		for (int i = 0; i < numFlights; i++) {
			flight = flightLeg.getSegments().get(i);
			boolean isFirstSegment = (i == 0);

			if (!isFirstSegment) {
				Flight prevFlight = flightLeg.getSegment(i - 1);
				Layover layover = new Layover(prevFlight, flight);
				String duration = DateTimeUtils.formatDuration(getResources(), layover.mDuration);
				String waypoint = StrUtils.formatWaypoint(flight.getOriginWaypoint());

				ViewGroup layoverC = Ui.inflate(R.layout.snippet_tablet_flight_layover, mFlightLegsC, false);
				TextView tv = Ui.findView(layoverC, R.id.flight_details_layover_text_view);
				String layoverStr = res.getString(R.string.layover_duration_location_TEMPLATE, duration, waypoint);
				tv.setText(HtmlCompat.stripHtml(layoverStr));
				mFlightLegsC.addView(layoverC);
			}

			// The FlightLeg with lines and circles
			flightSegmentSection = Ui.inflate(R.layout.section_flight_segment_tablet, mFlightLegsC, false);
			flightSegmentSection.bind(flight, trip.getFlightSegmentAttributes(mLegNumber)[i], depTime, arrTime);

			mFlightLegsC.addView(flightSegmentSection);
		}

		// Baggage fees
		FlightUtils.configureBaggageFeeViews(getContext(), trip, flightLeg, mBaggageFeesLinkPrimaryTv, mBaggageFeesLinkC,
			mBaggageFeesLinkSecondaryTv, false, getChildFragmentManager(), this);
	}

	public void scrollToTop() {
		if (mScrollC != null) {
			mScrollC.scrollTo(0, 0);
		}
	}

	@Override
	public void onBaggageFeeViewClicked(String title, String url) {
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
		Intent intent = builder.setUrl(url) //
			.setTitle(title) //
			.setAllowMobileRedirects(false) //
			.getIntent();
		startActivity(intent);
	}

	private String formatTime(DateTime cal) {
		DateTimeFormatter format = DateTimeFormat.forPattern("MMM dd, h:mm a");
		return format.print(cal);
	}

	/*
	 * SIZING AND POSITIONING.
	 * 
	 * We let the controller size and position things. This is less likely to bite us in the ass
	 * if/when we have a dedicated portrait mode.
	 */

	public void setDefaultDetailsPositionAndDimensions(Rect position, float additionalMarginPercentage) {
		int width = position.right - position.left;
		int height = position.bottom - position.top;

		mDetailsVerticalMarginHeight = (int) (height * additionalMarginPercentage);
		mDetailsPositionLeft = (int) (position.left + (additionalMarginPercentage * width));
		mDetailsPositionTop = (int) (position.top + (additionalMarginPercentage * height));
		mDetailsWidth = (int) (width - (2 * width * additionalMarginPercentage));
		mDetailsHeight = (int) (height - (1 * height * additionalMarginPercentage));

		applyDetailsDimensions();
	}

	public void setDefaultRowDimensions(int width, int height) {
		mRowWidth = width;
		mRowHeight = height;

		applyRowDimensions();
	}

	private void applyDetailsDimensions() {
		if (mDetailsC != null && mDetailsPositionLeft >= 0) {
			LayoutParams params = (LayoutParams) mDetailsC.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(mDetailsWidth, mDetailsHeight);
			}
			params.width = mDetailsWidth;
			params.height = mDetailsHeight;
			params.leftMargin = mDetailsPositionLeft;
			params.topMargin = mDetailsPositionTop;
			mDetailsC.setLayoutParams(params);
		}
		if (mBaggageFeesLinkC != null && mDetailsVerticalMarginHeight >= 0) {
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBaggageFeesLinkC.getLayoutParams();
			if (params == null) {
				params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					mDetailsVerticalMarginHeight);
			}
			params.height = mDetailsVerticalMarginHeight;
			mBaggageFeesLinkC.setLayoutParams(params);
		}
	}

	private void applyRowDimensions() {
		if (mAnimationFlightRow != null && mRowWidth >= 0) {
			Log.d("ResultsFlightDetails setting mAnimationFlightRowC dimensions - width:" + mRowWidth + " height:"
				+ mRowHeight);
			LayoutParams params = (LayoutParams) mAnimationFlightRow.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(mRowWidth, mRowHeight);
			}
			else {
				params.height = mRowHeight;
				params.width = mRowWidth;
			}
			mAnimationFlightRow.setLayoutParams(params);
		}
	}

	private void applyRowPosition() {
		if (mAnimationFlightRow != null && mRowPositionLeft >= 0) {
			Log.d("ResultsFlightDetails setting mAnimationFlightRowC position - left:" + mRowPositionLeft + " top:"
				+ mRowPositionTop);
			LayoutParams params = (LayoutParams) mAnimationFlightRow.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(mRowWidth, mRowHeight);
			}
			params.leftMargin = mRowPositionLeft;
			params.topMargin = mRowPositionTop;
			mAnimationFlightRow.setLayoutParams(params);
		}
	}

	/*
	 * SLIDE IN ANIMATION STUFF
	 */

	public void setSlideInAnimationLayer(int layerType) {
		mDetailsC.setLayerType(layerType, null);
	}

	public void prepareSlideInAnimation() {
		mAnimationFlightRow.setVisibility(View.INVISIBLE);
		mDetailsC.setVisibility(View.VISIBLE);
	}

	public void setDetailsSlideInAnimationState(float percentage, int totalSlideDistance, boolean fromLeft) {
		if (mDetailsC != null) {
			mDetailsC.setTranslationX((fromLeft ? -totalSlideDistance : totalSlideDistance) * (1f - percentage));
		}
	}

	public void finalizeSlideInPercentage() {
	}

	/*
	 * DEPARTURE FLIGHT SELECTED ANIMATION STUFF
	 */

	public void setDepartureTripSelectedAnimationLayer(int layerType) {
		if (mAnimationFlightRow != null) {
			mAnimationFlightRow.setLayerType(layerType, null);
		}
		if (mDetailsC != null) {
			mDetailsC.setLayerType(layerType, null);
		}
	}

	public void prepareDepartureFlightSelectedAnimation(Rect globalDestSpot, boolean addingToTrip) {
		//We move the row into its destination position. The animation itself will
		//then start behind the Details and slide and scale its way back here.
		Rect local = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalDestSpot, mRootC);
		mRowWidth = local.right - local.left;
		mRowHeight = local.bottom - local.top;
		mRowPositionLeft = local.left;
		mRowPositionTop = local.top;
		applyRowDimensions();
		applyRowPosition();

		if (addingToTrip && mLegNumber > 0) {
			mAnimationFlightRow.setTranslationX(-mAnimationFlightRow.getLeft());
			mAnimationFlightRow.setTranslationY(-mAnimationFlightRow.getTop());
		}

		mDetailsC.setVisibility(View.VISIBLE);
		if (!addingToTrip || (addingToTrip && mLegNumber > 0)) {
			mAnimationFlightRow.setVisibility(View.VISIBLE);
		}
		else {
			mAnimationFlightRow.setVisibility(View.INVISIBLE);
		}
	}

	public void setDepartureTripSelectedAnimationState(float percentage, boolean addingToTrip) {
		if (mAnimationFlightRow != null) {
			if (addingToTrip && mLegNumber > 0) {
				mAnimationFlightRow.setTranslationX(-mAnimationFlightRow.getLeft() * (1f - percentage));
				mAnimationFlightRow.setTranslationY(-mAnimationFlightRow.getTop() * (1f - percentage));
				mAnimationFlightRow.setAlpha(percentage);

			}
			else {
				float rowScaleX = 1f + (((float) mDetailsWidth / mRowWidth) - 1f) * (1f - percentage);
				float rowTranslationX = (1f - percentage) * (mDetailsPositionLeft - mRowPositionLeft);
				float rowTranslationY = (1f - percentage) * (mDetailsPositionTop - mRowPositionTop);

				mAnimationFlightRow.setScaleX(rowScaleX);
				mAnimationFlightRow.setTranslationX(rowTranslationX);
				mAnimationFlightRow.setTranslationY(rowTranslationY);
				mAnimationFlightRow.setAlpha(percentage);
			}
		}

		if (mDetailsC != null) {
			float detailsScaleX = 1f + (((float) mRowWidth / mDetailsWidth) - 1f) * percentage;
			float detailsScaleY = 1f + (((float) mRowHeight / mDetailsHeight) - 1f) * percentage;
			float detailsTranslationX = percentage * (mRowPositionLeft - mDetailsPositionLeft);
			float detailsTranslationY = percentage * (mRowPositionTop - mDetailsPositionTop);

			mDetailsC.setScaleX(detailsScaleX);
			mDetailsC.setScaleY(detailsScaleY);
			mDetailsC.setTranslationX(detailsTranslationX);
			mDetailsC.setTranslationY(detailsTranslationY);
			mDetailsC.setAlpha(1f - percentage);
		}
	}

	public void finalizeDepartureFlightSelectedAnimation() {
		resetDetailsC();
		resetAnimationRow();
	}

	/*
	 * ADD TO TRIP FROM DETAILS ANIMATION STUFF
	 */

	public void setAddToTripFromDetailsAnimationLayer(int layerType) {
		if (mAnimationFlightRow != null) {
			mAnimationFlightRow.setLayerType(layerType, null);
		}
	}

	public void prepareAddToTripFromDetailsAnimation(Rect globalAddToTripPosition) {
		//These solve the same problem, so we can just re-use for now
		prepareDepartureFlightSelectedAnimation(globalAddToTripPosition, true);
	}

	public void setAddToTripFromDetailsAnimationState(float percentage) {
		//These solve the same problem, so we can just re-use for now
		setDepartureTripSelectedAnimationState(percentage, true);
	}

	public void finalizeAddToTripFromDetailsAnimation() {
		finalizeDepartureFlightSelectedAnimation();
	}


	private void resetDetailsC() {
		if (mDetailsC != null) {
			mDetailsC.setScaleX(1f);
			mDetailsC.setScaleY(1f);
			mDetailsC.setTranslationX(0f);
			mDetailsC.setTranslationY(0f);
			mDetailsC.setAlpha(1f);
			applyDetailsDimensions();
		}
	}

	private void resetAnimationRow() {
		if (mAnimationFlightRow != null) {
			mAnimationFlightRow.setScaleX(1f);
			mAnimationFlightRow.setScaleY(1f);
			mAnimationFlightRow.setTranslationX(0);
			mAnimationFlightRow.setTranslationY(0);
		}
	}
}
