package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.InfoBarSection;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.Log;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlightTripOverviewFragment extends Fragment {

	private static final String ARG_TRIP_KEY = "ARG_TRIP_KEY";
	private static final String ARG_DISPLAY_MODE = "ARG_DISPLAY_MODE";
	private static final int ID_START_RANGE = Integer.MAX_VALUE - 100;
	private static final int ANIMATION_DURATION = 1000;
	//The margin between cards when expanded
	private static final int FLIGHT_LEG_TOP_MARGIN = 20;

	private static final int[] STATE_OPAQUE = { R.attr.state_opaque };
	private static final int[] STATE_TRANSPARENT = { R.attr.state_transparent };

	private FlightTrip mTrip;
	private RelativeLayout mFlightContainer;
	private InfoBarSection mFlightDateAndTravCount;

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

		mFlightDateAndTravCount.bindTripOverview(mTrip,
				(Db.getTravelers() != null && Db.getTravelers().size() != 0) ? Db.getTravelers()
						.size() : 1);

		buildCards(inflater);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
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

	//We build and add cards in the Overview configuration
	private void buildCards(LayoutInflater inflater) {
		//Inflate and store the sections
		mFlightContainer.removeAllViews();
		int currentTop = 0;

		measureDateAndTravelers();
		currentTop += Math.max(mFlightDateAndTravCount.getMeasuredHeight(), mFlightDateAndTravCount.getHeight());

		SectionFlightLeg tempFlight;
		for (int i = 0; i < mTrip.getLegCount(); i++) {
			tempFlight = (SectionFlightLeg) inflater.inflate(R.layout.section_display_flight_leg, null);
			tempFlight.setId(ID_START_RANGE + i);

			//Set our background to be a selector so we can set opacity quickly later during animations
			View bgView = Ui.findView(tempFlight, R.id.flight_leg_summary_container);
			bgView.setBackgroundResource(R.drawable.bg_flight_card_opacity_selector);

			tempFlight.bind(new FlightTripLeg(mTrip, mTrip.getLeg(i)));

			currentTop += FLIGHT_LEG_TOP_MARGIN;

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.topMargin = currentTop;
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

			mFlightContainer.addView(tempFlight, params);
			Log.d("Added card with topMargin:" + currentTop);

			measureCard(tempFlight);
			currentTop += Math.max(tempFlight.getMeasuredHeight(), tempFlight.getHeight());

		}

		//NOTE: The +50 is pretty unscientific, but we only need it to handle cases where text wraps, and causes things to be pushed down below the fold of the container
		// 50 seems to be sufficient
		mFlightContainer.setMinimumHeight(getUnstackedHeight() + 50);
		mFlightContainer.invalidate();
	}

	private void measureCard(SectionFlightLeg card) {
		if (getActivity() != null) {
			//We are just guessing here, we know the cards don't take up the full screen...
			int lMargin = 10;
			int rMargin = 10;
			int w = getActivity().getResources().getDisplayMetrics().widthPixels - lMargin - rMargin;
			int h = getActivity().getResources().getDisplayMetrics().heightPixels;
			Log.d("Measuring card with w:" + w + " h:" + h);
			card.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
					MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
		}
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
			animateCardsToStacked(ANIMATION_DURATION, 0);
		}
		else {
			//Adding a delay here prevents some display strangeness
			animateCardsToStacked(0, 250);
		}
		for (int i = mFlightContainer.getChildCount() - 1; i >= 0; i--) {
			Ui.findView(mFlightContainer, ID_START_RANGE + i).bringToFront();
		}
		mFlightContainer.invalidate();

	}

	public void unStackCards(boolean animate) {
		//We default to overview mode, so there shouldn't be a time when we need to animate to overview if we are already in overview
		if (mDisplayMode.compareTo(DisplayMode.OVERVIEW) != 0) {
			mDisplayMode = DisplayMode.OVERVIEW;
			if (animate) {
				animateCardsToUnStacked(ANIMATION_DURATION);
			}
			else {
				animateCardsToUnStacked(0);
			}
			getView().invalidate();
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
			View header = Ui.findView(tempFlight, R.id.info_text_view);
			View price = Ui.findView(tempFlight, R.id.price_text_view);
			View airline = Ui.findView(tempFlight, R.id.airline_text_view);
			View flightTripView = Ui.findView(tempFlight, R.id.flight_trip_view);

			int headerUnused = Math.max(header.getMeasuredHeight(), header.getHeight());
			int innerUnused = Math.max(Math.max(price.getMeasuredHeight(), price.getHeight()),
					Math.max(airline.getMeasuredHeight(), airline.getHeight()));

			//Dont forget margins...
			if (flightTripView != null && flightTripView.getLayoutParams() != null) {
				if (((RelativeLayout.LayoutParams) flightTripView.getLayoutParams()).topMargin > 0) {
					innerUnused += ((RelativeLayout.LayoutParams) flightTripView.getLayoutParams()).topMargin;
				}
			}

			int totalUnusedHeight = (int) (headerUnused + innerUnused);

			currentTop -= totalUnusedHeight;
			retVal[i] = currentTop;
			currentTop += Math.max(tempFlight.getMeasuredHeight(), tempFlight.getHeight());
		}
		return retVal;
	}

	private boolean mCardsAnimating = false;
	private AnimatorListener mCardsAnimatingListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator arg0) {
			mCardsAnimating = false;
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			mCardsAnimating = false;
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {

		}

		@Override
		public void onAnimationStart(Animator arg0) {
			mCardsAnimating = true;

		}

	};

	Runnable mCardBgOpacityRunner = new Runnable() {
		@Override
		public void run() {
			setTopCardBackgroundOpacity();
		}
	};

	////////////////////////////////
	//Animations

	private void animateCardsToStacked(int duration, int delay) {
		if (!mCardsAnimating) {
			ArrayList<Animator> animators = new ArrayList<Animator>();
			animators.addAll(getCardTextAlphaAnimators(1f, 0f));
			animators.addAll(getCardAnimators(getNormalTopMargins(), getStackedTopMargins()));
			animators.addAll(getDateTravelerBarAnimators(true));
			AnimatorSet animSet = new AnimatorSet();
			animSet.playTogether(animators);
			animSet.setDuration(duration);
			animSet.addListener(mCardsAnimatingListener);
			animSet.setStartDelay(delay);
			animSet.start();
			//We delay setting the bg until the thing is half done and thus avoid a jerky change
			this.mFlightContainer.postDelayed(mCardBgOpacityRunner, delay + (duration / 2));
		}
	}

	private void animateCardsToUnStacked(int duration) {
		if (!mCardsAnimating) {
			ArrayList<Animator> animators = new ArrayList<Animator>();
			animators.addAll(getCardTextAlphaAnimators(0f, 1f));
			animators.addAll(getCardAnimators(getStackedTopMargins(), getNormalTopMargins()));
			animators.addAll(getDateTravelerBarAnimators(false));
			AnimatorSet animSet = new AnimatorSet();
			animSet.playTogether(animators);
			animSet.setDuration(duration);
			animSet.addListener(mCardsAnimatingListener);
			animSet.start();
			//This looks best if we just change the bg right away before the animation
			setTopCardBackgroundOpacity();
		}
	}

	private void setTopCardBackgroundOpacity() {
		SectionFlightLeg firstCard = Ui.findView(mFlightContainer, ID_START_RANGE);
		if (firstCard != null) {
			View bgView = Ui.findView(firstCard, R.id.flight_leg_summary_container);
			if (bgView != null) {
				if (mDisplayMode.equals(DisplayMode.CHECKOUT)) {
					bgView.getBackground().setState(STATE_OPAQUE);
				}
				else {
					bgView.getBackground().setState(STATE_TRANSPARENT);
				}
				bgView.invalidate();
			}
		}
	}

	private ArrayList<Animator> getCardTextAlphaAnimators(float start, float end) {
		ArrayList<Animator> animators = new ArrayList<Animator>();
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			View header = Ui.findView(tempFlight, R.id.info_text_view);
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
