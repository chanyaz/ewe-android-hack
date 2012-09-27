package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.FlightLegSummarySection.FlightLegSummarySectionListener;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.section.SectionGeneralFlightInfo;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlightTripOverviewFragment extends Fragment {

	private static final String ARG_TRIP_KEY = "ARG_TRIP_KEY";
	private static final String ARG_DISPLAY_MODE = "ARG_DISPLAY_MODE";

	private static final int ID_START_RANGE = Integer.MAX_VALUE - 100;

	private static final int ANIMATION_DURATION = 1000;

	//The margin between cards when expanded
	private static final int FLIGHT_LEG_TOP_MARGIN = 20;

	private FlightTrip mTrip;
	private RelativeLayout mFlightContainer;
	private SectionGeneralFlightInfo mFlightDateAndTravCount;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;

	public enum DisplayMode {
		CHECKOUT, OVERVIEW
	}

	public static FlightTripOverviewFragment newInstance(String tripKey, DisplayMode mode) {
		FlightTripOverviewFragment fragment = new FlightTripOverviewFragment();
		Bundle args = new Bundle();
		args.putString(ARG_TRIP_KEY, tripKey);
		args.putString(ARG_DISPLAY_MODE, mode.name());
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null && savedInstanceState.containsKey(ARG_DISPLAY_MODE)) {
			mDisplayMode = DisplayMode.valueOf(savedInstanceState.getString(ARG_DISPLAY_MODE));
		}
		else {
			mDisplayMode = DisplayMode.valueOf(getArguments().getString(ARG_DISPLAY_MODE));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_trip_overview, container, false);

		mFlightContainer = Ui.findView(v, R.id.flight_legs_container);
		mFlightDateAndTravCount = Ui.findView(v, R.id.date_and_travlers);

		String tripKey = getArguments().getString(ARG_TRIP_KEY);
		mTrip = Db.getFlightSearch().getFlightTrip(tripKey);

		mFlightDateAndTravCount.bind(mTrip,
				(Db.getTravelers() != null && Db.getTravelers().size() != 0) ? Db.getTravelers()
						.size() : 1);

		buildCards(inflater);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mDisplayMode.compareTo(DisplayMode.OVERVIEW) == 0) {
			unStackCards(false);
		}
		else {
			stackCards(false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(ARG_DISPLAY_MODE, this.mDisplayMode.name());
	}

	private void buildCards(LayoutInflater inflater) {
		Activity activity = getActivity();
		if (!(activity instanceof FlightLegSummarySectionListener)) {
			throw new RuntimeException(
					"FlightTripOverviewFragment Activity must implement FlightLegSummarySectionListener!  " + activity);
		}
		FlightLegSummarySectionListener listener = (FlightLegSummarySectionListener) activity;

		//Inflate and store the sections
		mFlightContainer.removeAllViews();
		SectionFlightLeg tempFlight;
		for (int i = 0; i < mTrip.getLegCount(); i++) {
			tempFlight = (SectionFlightLeg) inflater.inflate(R.layout.section_display_flight_leg, null);
			tempFlight.setId(ID_START_RANGE + i);

			tempFlight.setListener(listener);
			tempFlight.bind(new FlightTripLeg(mTrip, mTrip.getLeg(i)));

			mFlightContainer.addView(tempFlight);
		}
		mFlightContainer.setMinimumHeight(getUnstackedHeight());
		mFlightContainer.invalidate();
	}

	public int getStackedHeight() {
		return getHeightFromMargins(getStackedTopMargins(), false);
	}

	public int getUnstackedHeight() {
		return getHeightFromMargins(getNormalTopMargins(), true);
	}

	private int getHeightFromMargins(int[] margins, boolean includeTravBar) {
		int retHeight = 0;

		int lastInd = margins.length - 1;
		int lastMargin = margins[lastInd];
		SectionFlightLeg lastFlight = Ui.findView(mFlightContainer, lastInd + ID_START_RANGE);

		if (includeTravBar) {
			measureDateAndTravelers();
			retHeight += Math.max(mFlightDateAndTravCount.getMeasuredHeight(), mFlightDateAndTravCount.getHeight());
		}
		retHeight += lastMargin;
		retHeight += Math.max(lastFlight.getMeasuredHeight(), lastFlight.getHeight());

		Log.i("getHeightFromMargins:" + retHeight);

		return retHeight;
	}

	public void stackCards(boolean animate) {
		mDisplayMode = DisplayMode.CHECKOUT;
		if (animate) {
			animateCardsToStacked(ANIMATION_DURATION);
		}
		else {
			animateCardsToStacked(1);
		}
		for (int i = mFlightContainer.getChildCount() - 1; i >= 0; i--) {
			Ui.findView(mFlightContainer, ID_START_RANGE + i).bringToFront();
		}
		mFlightContainer.invalidate();

	}

	public void unStackCards(boolean animate) {
		mDisplayMode = DisplayMode.OVERVIEW;
		if (animate) {
			animateCardsToUnStacked(ANIMATION_DURATION);
		}
		else {
			animateCardsToUnStacked(1);
		}
		getView().invalidate();
	}

	public void setCardOnClickListeners(OnClickListener listener) {
		if (mFlightContainer == null) {
			return;
		}

		SectionFlightLeg tempFlight;
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			tempFlight.setOnClickListener(listener);
		}
	}

	private void measureFlightContainer() {
		if (getActivity() != null && mFlightContainer != null) {
			int w = getActivity().getResources().getDisplayMetrics().widthPixels;
			int h = getActivity().getResources().getDisplayMetrics().heightPixels;
			Log.v("measuring flight container... w:" + w + " h:" + h);
			mFlightContainer.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
					MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
			Log.v("FlightContainer MeasuredHeight:" + mFlightContainer.getMeasuredHeight() + " Height:"
					+ mFlightContainer.getHeight());
		}
	}

	private void measureDateAndTravelers() {
		if (mFlightDateAndTravCount != null && mFlightDateAndTravCount.getMeasuredHeight() <= 0) {
			int w = getActivity().getResources().getDisplayMetrics().widthPixels;
			int h = getActivity().getResources().getDisplayMetrics().heightPixels;
			Log.i("measuring date and traveler bar... w:" + w + " h:" + h);
			mFlightDateAndTravCount.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
					MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
			Log.i("mFlightDateAndTravCount MeasuredHeight:" + mFlightDateAndTravCount.getMeasuredHeight() + " Height:"
					+ mFlightDateAndTravCount.getHeight());
		}
	}

	private int[] getNormalTopMargins() {
		measureFlightContainer();
		int[] retVal = new int[mFlightContainer.getChildCount()];
		int currentTop = 0;
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			currentTop += FLIGHT_LEG_TOP_MARGIN;
			retVal[i] = currentTop;
			currentTop += Math.max(tempFlight.getMeasuredHeight(), tempFlight.getHeight());

		}
		return retVal;
	}

	private int[] getStackedTopMargins() {
		measureFlightContainer();
		int[] retVal = new int[mFlightContainer.getChildCount()];
		int currentTop = 0;
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			View header = Ui.findView(tempFlight, R.id.display_flight_leg_header);
			View price = Ui.findView(tempFlight, R.id.price_text_view);
			View airline = Ui.findView(tempFlight, R.id.airline_text_view);

			int headerUnused = Math.max(header.getMeasuredHeight(), header.getHeight());
			int innerUnused = Math.max(Math.max(price.getMeasuredHeight(), price.getHeight()),
					Math.max(airline.getMeasuredHeight(), airline.getHeight()));
			int totalUnusedHeight = headerUnused + innerUnused;

			currentTop -= totalUnusedHeight;
			retVal[i] = currentTop;
			currentTop += Math.max(tempFlight.getMeasuredHeight(), tempFlight.getHeight());
		}
		return retVal;
	}

	////////////////////////////////
	//Placements without animations

	private void placeCardsUnStacked() {
		int[] tops = getNormalTopMargins();
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			RelativeLayout.LayoutParams tempFlightLayoutParams = (android.widget.RelativeLayout.LayoutParams) tempFlight
					.getLayoutParams();
			if (tempFlightLayoutParams == null) {
				tempFlightLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
			}

			Log.i("settingTopMargin:" + tops[i]);
			tempFlightLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			tempFlightLayoutParams.topMargin = tops[i];

			tempFlight.setLayoutParams(tempFlightLayoutParams);
		}
		mFlightContainer.invalidate();
	}

	private void placeCardsStacked() {
		int[] tops = getStackedTopMargins();
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			RelativeLayout.LayoutParams tempFlightLayoutParams = (android.widget.RelativeLayout.LayoutParams) tempFlight
					.getLayoutParams();
			if (tempFlightLayoutParams == null) {
				tempFlightLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
			}

			Log.i("settingTopMargin:" + tops[i]);
			tempFlightLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			tempFlightLayoutParams.topMargin = tops[i];

			tempFlight.setLayoutParams(tempFlightLayoutParams);
			tempFlight.requestLayout();
		}
		mFlightContainer.invalidate();
	}

	@SuppressLint("NewApi")
	private void setCardAlphas(float alpha) {
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			View header = Ui.findView(tempFlight, R.id.display_flight_leg_header);
			View price = Ui.findView(tempFlight, R.id.price_text_view);
			View airline = Ui.findView(tempFlight, R.id.airline_text_view);
			header.setAlpha(alpha);
			price.setAlpha(alpha);
			airline.setAlpha(alpha);
		}
	}

	////////////////////////////////
	//Animations

	private void animateCardsToStacked(int duration) {
		ArrayList<Animator> animators = new ArrayList<Animator>();
		animators.addAll(getCardTextAlphaAnimators(1f, 0f));
		animators.addAll(getCardAnimators(getNormalTopMargins(), getStackedTopMargins()));
		animators.addAll(getDateTravelerBarAnimators(true));
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(animators);
		animSet.setDuration(duration);
		animSet.start();
	}

	private void animateCardsToUnStacked(int duration) {
		ArrayList<Animator> animators = new ArrayList<Animator>();
		animators.addAll(getCardTextAlphaAnimators(0f, 1f));
		animators.addAll(getCardAnimators(getStackedTopMargins(), getNormalTopMargins()));
		animators.addAll(getDateTravelerBarAnimators(false));
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(animators);
		animSet.setDuration(duration);
		animSet.start();
	}

	private ArrayList<Animator> getCardTextAlphaAnimators(float start, float end) {
		ArrayList<Animator> animators = new ArrayList<Animator>();
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			View header = Ui.findView(tempFlight, R.id.display_flight_leg_header);
			View price = Ui.findView(tempFlight, R.id.price_text_view);
			View airline = Ui.findView(tempFlight, R.id.airline_text_view);

			ObjectAnimator headerOut = ObjectAnimator.ofFloat(header, "alpha", start, end);
			ObjectAnimator priceOut = ObjectAnimator.ofFloat(price, "alpha", start, end);
			ObjectAnimator airlineOut = ObjectAnimator.ofFloat(airline, "alpha", start, end);
			animators.add(headerOut);
			animators.add(priceOut);
			animators.add(airlineOut);
		}
		return animators;
	}

	private ArrayList<Animator> getCardAnimators(int[] startTops, int[] endTops) {
		ArrayList<Animator> animators = new ArrayList<Animator>();

		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			int id = ID_START_RANGE + i;
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, id);

			ObjectAnimator mover = ObjectAnimator.ofFloat(tempFlight, "y", startTops[i], endTops[i]);
			animators.add(mover);
		}
		return animators;
	}

	private ArrayList<Animator> getDateTravelerBarAnimators(boolean hide) {
		ArrayList<Animator> animators = new ArrayList<Animator>();

		int barHeight = Math.max(mFlightDateAndTravCount.getMeasuredHeight(), mFlightDateAndTravCount.getHeight());
		int start = 0;
		int end = -barHeight;
		float alphaStart = 1f;
		float alphaEnd = 0f;
		if (!hide) {
			start = -barHeight;
			end = 0;
			alphaStart = 0f;
			alphaEnd = 1f;
		}

		//move and hide the traveler price bar
		ObjectAnimator mover = ObjectAnimator.ofFloat(mFlightDateAndTravCount, "y", start, end);
		animators.add(mover);

		ObjectAnimator dateTravBarAlpha = ObjectAnimator
				.ofFloat(mFlightDateAndTravCount, "alpha", alphaStart, alphaEnd);
		animators.add(dateTravBarAlpha);

		//move up the flights container
		ObjectAnimator flightsMover = ObjectAnimator.ofFloat(mFlightContainer, "y", start + barHeight, end + barHeight);
		animators.add(flightsMover);

		return animators;
	}

}
