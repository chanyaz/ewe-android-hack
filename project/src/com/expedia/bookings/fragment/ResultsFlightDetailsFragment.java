package com.expedia.bookings.fragment;

import java.text.DateFormat;
import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightStatsRating;
import com.expedia.bookings.data.FlightStatsRatingResponse;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.interfaces.IResultsFlightLegSelected;
import com.expedia.bookings.section.FlightLegSummarySectionTablet;
import com.expedia.bookings.section.FlightSegmentSection;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.RingedCountView;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.data.Layover;
import com.mobiata.flightlib.utils.DateTimeUtils;

/**
 * ResultsFlightDetailsFragment: The flight details fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsFlightDetailsFragment extends Fragment {

	private static final String ARG_LEG_NUMBER = "ARG_LEG_NUMBER";

	private static final String BGD_KEY_RATINGS = "BGD_KEY_RATINGS";

	public static ResultsFlightDetailsFragment newInstance(int legNumber) {
		ResultsFlightDetailsFragment frag = new ResultsFlightDetailsFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_LEG_NUMBER, legNumber);
		frag.setArguments(args);
		return frag;
	}

	private Flight mFlight;

	// Views

	private static final int NUM_SCROLLVIEW_HEADERS = 2; // Stats container + grey divider

	private ViewGroup mRootC;
	private ViewGroup mDetailsC;
	private FlightLegSummarySectionTablet mAnimationFlightRow;

	private TextView mTimeHeaderTv;
	private TextView mAddTripTv;

	private ViewGroup mOnTimeContainer;
	private RingedCountView mOnTimeRingView;
	private TextView mOnTimeTextView;
	private TextView mFlightDistanceTv;
	private TextView mFlightDurationTv;

	private ViewGroup mFlightLegsC;

	private FlightLegSummarySectionTablet mSelectedFlightLegAnimationRowSection;

	private IResultsFlightLegSelected mListener;

	// Position and size vars
	int mDetailsPositionLeft = -1;
	int mDetailsPositionTop = -1;
	int mDetailsWidth = -1;
	int mDetailsHeight = -1;
	int mRowPositionLeft = -1;
	int mRowPositionTop = -1;
	int mRowWidth = -1;
	int mRowHeight = -1;

	// Animation vars
	private Rect mAddToTripRect;

	// Misc
	private int mLegNumber = -1;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, IResultsFlightLegSelected.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_flight_details, null);
		mAnimationFlightRow = Ui.findView(mRootC, R.id.details_animation_row);
		mAnimationFlightRow.setPivotX(0);
		mAnimationFlightRow.setPivotY(0);
		mDetailsC = Ui.findView(mRootC, R.id.details_container);
		mDetailsC.setPivotX(0);
		mDetailsC.setPivotY(0);

		mTimeHeaderTv = Ui.findView(mRootC, R.id.details_time_header);
		mAddTripTv = Ui.findView(mRootC, R.id.details_add_trip_button);

		mOnTimeContainer = Ui.findView(mRootC, R.id.flight_punctuality_container);
		mOnTimeRingView = Ui.findView(mRootC, R.id.flight_on_time_ring_view);
		mOnTimeRingView.setCountText("");
		mOnTimeTextView = Ui.findView(mRootC, R.id.flight_punctuality_text_view);
		mFlightDistanceTv = Ui.findView(mRootC, R.id.flight_miles_text_view);
		mFlightDurationTv = Ui.findView(mRootC, R.id.flight_overall_duration_text_view);

		mFlightLegsC = Ui.findView(mRootC, R.id.flight_legs_container);

		mSelectedFlightLegAnimationRowSection = Ui.findView(mRootC, R.id.details_animation_row);

		setLegPosition(getArguments().getInt(ARG_LEG_NUMBER));

		applyDetailsDimensions();
		applyRowDimensions();

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (BackgroundDownloader.getInstance().isDownloading(BGD_KEY_RATINGS)) {
			BackgroundDownloader.getInstance().registerDownloadCallback(BGD_KEY_RATINGS, mRatingsCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(BGD_KEY_RATINGS);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(BGD_KEY_RATINGS);
		}
	}

	public void setLegPosition(int legNumber) {
		mLegNumber = legNumber;
	}

	public void bindWithDb() {
		FlightTripLeg flightTripLeg = Db.getFlightSearch().getSelectedLegs()[mLegNumber];
		FlightTrip trip = flightTripLeg.getFlightTrip();
		FlightLeg flightLeg = flightTripLeg.getFlightLeg();
		Flight flight;

		// Evaluate the flight, for the punctuality rating purposes
		Flight newFlight = Db.getFlightSearch().getSelectedLegs()[mLegNumber].getFlightLeg().getSegment(0);
		if (!newFlight.equals(mFlight)) {
			BackgroundDownloader bgd = BackgroundDownloader.getInstance();

			// We're downloading the onTimePercent for the old Flight, so cancel it
			if (bgd.isDownloading(BGD_KEY_RATINGS)) {
				bgd.cancelDownload(BGD_KEY_RATINGS);
			}

			mFlight = newFlight;

			FlightStatsRating rating = Db.getFlightSearch().getFlightStatsRating(mFlight);
			if (rating == null) {
				// Kick off a new download if necessary
				bgd.startDownload(BGD_KEY_RATINGS, mRatingsDownload, mRatingsCallback);
			}
			else {
				Log.v("FlightStats rating cache hit");
				bindFlightPunctuality(rating);
			}
		}

		final Context context = getActivity();
		final Resources res = getResources();

		// Grey header

		// 10:05 AM to 2:20 PM
		String departure = formatTime(flightLeg.getFirstWaypoint().getBestSearchDateTime());
		String arrival = formatTime(flightLeg.getLastWaypoint().getBestSearchDateTime());
		String time = res.getString(R.string.date_range_TEMPLATE, departure, arrival);
		mTimeHeaderTv.setText(time);

		// Add for $390
		String addTripStr = res.getString(R.string.add_for_TEMPLATE,
				trip.getTotalFare().getFormattedMoney(Money.F_NO_DECIMAL));
		mAddTripTv.setText(addTripStr);
		mAddTripTv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onTripAdded(mLegNumber);

				mSelectedFlightLegAnimationRowSection.bind(Db.getFlightSearch(), mLegNumber);
			}
		});

		// Statistics
		mFlightDistanceTv.setText(FlightUtils.formatDistance(context, flightLeg, true));
		mFlightDurationTv.setText(FlightUtils.formatDuration(context, flightLeg));

		// Flight Leg container

		//Arrival / Departure times
		Calendar depTime = flightLeg.getFirstWaypoint().getBestSearchDateTime();
		Calendar arrTime = flightLeg.getLastWaypoint().getBestSearchDateTime();

		FlightSegmentSection flightSegmentSection;

		LayoutInflater inflater = LayoutInflater.from(getActivity());
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
				String waypoint = StrUtils.formatWaypoint(flight.mOrigin);

				ViewGroup layoverC = Ui.inflate(inflater, R.layout.snippet_tablet_flight_layover, mFlightLegsC, false);
				TextView tv = Ui.findView(layoverC, R.id.flight_details_layover_text_view);
				String layoverStr = res.getString(R.string.layover_duration_location_TEMPLATE, duration, waypoint);
				tv.setText(Html.fromHtml(layoverStr).toString());
				mFlightLegsC.addView(layoverC);
			}

			// The FlightLeg with lines and circles
			flightSegmentSection = Ui.inflate(inflater, R.layout.section_flight_segment_tablet, mFlightLegsC, false);
			flightSegmentSection.bind(flight, trip.getFlightSegmentAttributes(mLegNumber)[i], depTime, arrTime);

			mFlightLegsC.addView(flightSegmentSection);
		}
	}

	private String formatTime(Calendar cal) {
		DateFormat df = android.text.format.DateFormat.getTimeFormat(getActivity());
		return df.format(DateTimeUtils.getTimeInLocalTimeZone(cal));
	}

	private void bindFlightPunctuality(FlightStatsRating rating) {
		mOnTimeContainer.setVisibility(View.VISIBLE);

		float percent = (float) rating.getOnTimePercent();
		String formatPercent = rating.getFormattedPercent();
		mOnTimeRingView.setPercent(percent);
		mOnTimeTextView.setText(getActivity().getString(R.string.flight_on_time_TEMPLATE, formatPercent));
	}

	private BackgroundDownloader.Download<FlightStatsRatingResponse> mRatingsDownload = new BackgroundDownloader.Download<FlightStatsRatingResponse>() {
		@Override
		public FlightStatsRatingResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			return services.getFlightStatsRating(mFlight);
		}
	};

	private BackgroundDownloader.OnDownloadComplete<FlightStatsRatingResponse> mRatingsCallback = new BackgroundDownloader.OnDownloadComplete<FlightStatsRatingResponse>() {
		@Override
		public void onDownload(FlightStatsRatingResponse results) {
			if (results == null || results.hasErrors() || !results.getFlightStatsRating().hasValidPercent()) {
				if (results != null && !results.hasErrors()) {
					Log.w("FlightStats onTimePercent=" + results.getFlightStatsRating().getOnTimePercent()
							+ " numObservations=" + results.getFlightStatsRating().getNumObservations());
				}
				Log.w("No valid flight onTimePercent forecast, removing from hierarchy");
				mOnTimeContainer.setVisibility(View.GONE);
			}
			else {
				Db.getFlightSearch().addFlightStatsRating(mFlight, results.getFlightStatsRating());
				bindFlightPunctuality(results.getFlightStatsRating());
			}
		}
	};

	/*
	 * SIZING AND POSITIONING.
	 * 
	 * We let the controller size and position things. This is less likely to bite us in the ass
	 * if/when we have a dedicated portrait mode.
	 */

	public void setDefaultDetailsPositionAndDimensions(Rect position, float additionalMarginPercentage) {
		int width = position.right - position.left;
		int height = position.bottom - position.top;

		mDetailsPositionLeft = (int) (position.left + (additionalMarginPercentage * width));
		mDetailsPositionTop = (int) (position.top + (additionalMarginPercentage * height));
		mDetailsWidth = (int) (width - (2 * width * additionalMarginPercentage));
		mDetailsHeight = (int) (height - (2 * height * additionalMarginPercentage));

		applyDetailsDimensions();
	}

	public void setDetaultRowDimensions(int width, int height) {
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
	}

	private void applyRowDimensions() {
		if (mAnimationFlightRow != null && mRowWidth >= 0) {
			Log.d("ResultsFlightDetails setting mAnimationFlightRowC dimensions - width:" + mRowWidth + " height:"
					+ mRowHeight);
			LayoutParams params = (LayoutParams) mAnimationFlightRow.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(mRowHeight, mRowWidth);
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
				params = new LayoutParams(mRowHeight, mRowWidth);
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

	public void prepareDepartureFlightSelectedAnimation(Rect globalDestSpot) {
		//We move the row into its destination position. The animation itself will
		//then start behind the Details and slide and scale its way back here.
		Rect local = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalDestSpot, mRootC);
		mRowWidth = local.right - local.left;
		mRowHeight = local.bottom - local.top;
		mRowPositionLeft = local.left;
		mRowPositionTop = local.top;
		applyRowDimensions();
		applyRowPosition();

		mAnimationFlightRow.setVisibility(View.VISIBLE);
		mDetailsC.setVisibility(View.VISIBLE);
	}

	public void setDepartureTripSelectedAnimationState(float percentage) {
		if (mAnimationFlightRow != null) {
			float rowScaleX = 1f + (((float) mDetailsWidth / mRowWidth) - 1f) * (1f - percentage);
			//float rowScaleY = 1f + (((float) mDetailsHeight / mRowHeight) - 1f) * (1f - percentage);

			float rowTranslationX = (1f - percentage) * (mDetailsPositionLeft - mRowPositionLeft);
			float rowTranslationY = (1f - percentage) * (mDetailsPositionTop - mRowPositionTop);

			mAnimationFlightRow.setScaleX(rowScaleX);
			//mAnimationFlightRowC.setScaleY(rowScaleY);
			mAnimationFlightRow.setTranslationX(rowTranslationX);
			mAnimationFlightRow.setTranslationY(rowTranslationY);
			mAnimationFlightRow.setAlpha(percentage);
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

	}

	/*
	 * ADD TO TRIP FROM DETAILS ANIMATION STUFF
	 */

	public void setAddToTripFromDetailsAnimationLayer(int layerType) {
		//These solve the same problem, so we can just re-use for now
		setDepartureTripSelectedAnimationLayer(layerType);
	}

	public void prepareAddToTripFromDetailsAnimation(Rect globalAddToTripPosition) {
		//These solve the same problem, so we can just re-use for now
		prepareDepartureFlightSelectedAnimation(globalAddToTripPosition);
	}

	public void setAddToTripFromDetailsAnimationState(float percentage) {
		//These solve the same problem, so we can just re-use for now
		setDepartureTripSelectedAnimationState(percentage);
	}

	public void finalizeAddToTripFromDetailsAnimation() {

	}

	/*
	 * ADD TO TRIP FROM DEPARTURE ANIMATION STUFF
	 */

	public void setAddToTripFromDepartureAnimationLayer(int layerType) {
		if (mAnimationFlightRow != null) {
			mAnimationFlightRow.setLayerType(layerType, null);
		}
	}

	public void prepareAddToTripFromDepartureAnimation(Rect globalDepartureRowPosition, Rect globalAddToTripPosition) {
		//This one is different, we set the row to be the dimensions of the start of the animation,
		//this way it should just sit on top of the actual row and not cause a jump as the scaling gets strange
		Rect local = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalDepartureRowPosition, mRootC);
		mRowWidth = local.right - local.left;
		mRowHeight = local.bottom - local.top;
		mRowPositionLeft = local.left;
		mRowPositionTop = local.top;

		applyRowDimensions();
		applyRowPosition();

		mAddToTripRect = ScreenPositionUtils.translateGlobalPositionToLocalPosition(globalAddToTripPosition, mRootC);
		mAnimationFlightRow.setVisibility(View.VISIBLE);
		mDetailsC.setVisibility(View.INVISIBLE);
	}

	public void setAddToTripFromDepartureAnimationState(float percentage) {
		if (mAnimationFlightRow != null && mAddToTripRect != null) {
			float rowScaleX = 1f + (((float) (mAddToTripRect.right - mAddToTripRect.left) / mRowWidth) - 1f)
					* percentage;
			float rowScaleY = 1f + (((float) (mAddToTripRect.bottom - mAddToTripRect.top) / mRowHeight) - 1f)
					* percentage;

			float rowTranslationX = percentage * (mAddToTripRect.left - mRowPositionLeft);
			float rowTranslationY = percentage * (mAddToTripRect.top - mRowPositionTop);

			mAnimationFlightRow.setScaleX(rowScaleX);
			mAnimationFlightRow.setScaleY(rowScaleY);
			mAnimationFlightRow.setTranslationX(rowTranslationX);
			mAnimationFlightRow.setTranslationY(rowTranslationY);
		}
	}

	public void finalizeAddToTripFromDepartureAnimation() {

	}

}
