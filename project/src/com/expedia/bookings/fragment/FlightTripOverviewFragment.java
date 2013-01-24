package com.expedia.bookings.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.InfoBarSection;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.util.AndroidUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
import com.nineoldandroids.view.animation.AnimatorProxy;

public class FlightTripOverviewFragment extends Fragment {

	private static final String ARG_TRIP_KEY = "ARG_TRIP_KEY";
	private static final String ARG_DISPLAY_MODE = "ARG_DISPLAY_MODE";
	private static final int ID_START_RANGE = Integer.MAX_VALUE - 100;
	private static final int ANIMATION_DURATION = 450;
	//The margin between cards when expanded
	private static final int FLIGHT_LEG_TOP_MARGIN = 20;

	private static final int TOP_CARD_STACKED_ALPHA = 255;
	private static final int TOP_CARD_UNSTACKED_ALPHA = 210;

	private FlightTrip mTrip;
	private RelativeLayout mFlightContainer;
	private InfoBarSection mFlightDateAndTravCount;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;

	private float mCurrentPercentage = 1f;

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

		mFlightDateAndTravCount.bindTripOverview(mTrip, Db.getFlightSearch().getSearchParams().getNumAdults());

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

		//Build the cards
		SectionFlightLeg tempFlight;
		for (int i = 0; i < mTrip.getLegCount(); i++) {
			tempFlight = (SectionFlightLeg) inflater.inflate(R.layout.section_display_flight_leg, null);
			tempFlight.setId(ID_START_RANGE + i);

			//Set our background to be a selector so we can set opacity quickly later during animations
			if (tempFlight.getId() == ID_START_RANGE) {
				View bgView = Ui.findView(tempFlight, R.id.flight_leg_summary_container);
				bgView.setBackgroundResource(R.drawable.bg_flight_card_search_results_opaque);
				bgView.getBackground().setAlpha(TOP_CARD_UNSTACKED_ALPHA);
			}

			tempFlight.bind(new FlightTripLeg(mTrip, mTrip.getLeg(i)));

			currentTop += FLIGHT_LEG_TOP_MARGIN;

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			params.topMargin = currentTop;
			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

			mFlightContainer.addView(tempFlight, params);

			measureCard(tempFlight);
			currentTop += Math.max(tempFlight.getMeasuredHeight(), tempFlight.getHeight());
		}

		//Z-index the card
		for (int i = mTrip.getLegCount() - 1; i >= 0; i--) {
			mFlightContainer.findViewById(ID_START_RANGE + i).bringToFront();
		}
	}

	private void measureCard(SectionFlightLeg card) {
		if (getActivity() != null) {
			//We are just guessing here, we know the cards don't take up the full screen...
			int lMargin = 10;
			int rMargin = 10;
			int w = getActivity().getResources().getDisplayMetrics().widthPixels - lMargin - rMargin;
			int h = getActivity().getResources().getDisplayMetrics().heightPixels;
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

	public int getCurrentHeight() {
		int lastInd = mFlightContainer.getChildCount() - 1;
		SectionFlightLeg lastFlight = Ui.findView(mFlightContainer, lastInd + ID_START_RANGE);
		if (lastFlight != null) {
			return lastFlight.getBottom();
		}
		else {
			return 0;
		}
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

		return retHeight;
	}

	public void stackCards(boolean animate) {
		mDisplayMode = DisplayMode.CHECKOUT;
		if (animate) {
			animateToPercentage(mCurrentPercentage, 0);
		}
		else {
			setExpandedPercentage(0);
		}
	}

	public void unStackCards(boolean animate) {
		//We default to overview mode, so there shouldn't be a time when we need to animate to overview if we are already in overview
		if (mDisplayMode.compareTo(DisplayMode.OVERVIEW) != 0) {
			mDisplayMode = DisplayMode.OVERVIEW;

			if (animate) {
				animateToPercentage(mCurrentPercentage, 1);
			}
			else {
				setExpandedPercentage(1);
			}
		}
	}

	public void setExpandedPercentage(float percentage) {
		mCurrentPercentage = percentage;
		if (this.isAdded()) {
			placeCardsFromPercentage(percentage);
			placeTopBarFromPercentage(percentage);
			setCardsAlpha(percentage);
			setTopCardBgAlpha(percentage);
		}
	}

	public float getExpandedPercentage() {
		return mCurrentPercentage;
	}

	protected void animateToPercentage(float startPercentage, float endPercentage) {
		if (!mCardsAnimating) {
			Animator animator = getAnimator(startPercentage, endPercentage);
			animator.setDuration(ANIMATION_DURATION);
			animator.addListener(mCardsAnimatingListener);
			animator.start();
		}
	}

	protected Animator getAnimator(float startPercentage, float endPercentage) {
		ValueAnimator animator = ValueAnimator.ofFloat(startPercentage, endPercentage);
		animator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator anim) {
				Float f = (Float) anim.getAnimatedValue();
				setExpandedPercentage(f.floatValue());

			}
		});
		return animator;
	}

	protected void placeCardsFromPercentage(float percentage) {

		if (percentage == 0) {
			placeCardsFromMargins(getStackedTopMargins());
		}
		else if (percentage == 1) {
			placeCardsFromMargins(getNormalTopMargins());
		}
		else {
			int[] stacked = getStackedTopMargins();
			int[] unstacked = getNormalTopMargins();
			int[] positions = new int[unstacked.length];
			for (int i = 0; i < positions.length; i++) {
				double dif = percentage * (unstacked[i] - stacked[i]);
				int val = (int) Math.round(stacked[i] + dif);

				positions[i] = val;
			}
			placeCardsFromMargins(positions);
		}
	}

	protected void placeTopBarFromPercentage(float percentage) {
		int height = Math.max(mFlightDateAndTravCount.getHeight(), mFlightDateAndTravCount.getMeasuredHeight());
		if (height <= 0) {
			this.measureDateAndTravelers();
			height = Math.max(mFlightDateAndTravCount.getHeight(), mFlightDateAndTravCount.getMeasuredHeight());
		}

		if (percentage == 0) {
			LayoutParams params = (LayoutParams) mFlightDateAndTravCount.getLayoutParams();
			params.topMargin = -height;
			mFlightDateAndTravCount.setLayoutParams(params);
		}
		else if (percentage == 1) {
			LayoutParams params = (LayoutParams) mFlightDateAndTravCount.getLayoutParams();
			params.topMargin = 0;
			mFlightDateAndTravCount.setLayoutParams(params);
		}
		else {
			int top = height - (int) Math.round(percentage * height);
			LayoutParams params = (LayoutParams) mFlightDateAndTravCount.getLayoutParams();
			params.topMargin = -top;
			mFlightDateAndTravCount.setLayoutParams(params);
		}
	}

	protected void setCardsAlpha(float percentage) {
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			View header = Ui.findView(tempFlight, R.id.info_text_view);
			View airline = Ui.findView(tempFlight, R.id.airline_text_view);

			setViewAlpha(percentage, header);
			setViewAlpha(percentage, airline);
		}
	}

	protected void setTopCardBgAlpha(float percentage) {
		SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE);
		if (tempFlight != null) {
			int minAlpha = TOP_CARD_UNSTACKED_ALPHA;
			int maxAlpha = TOP_CARD_STACKED_ALPHA;

			int useAlpha = 255 - Math.round((maxAlpha - minAlpha) * percentage);

			View bgView = Ui.findView(tempFlight, R.id.flight_leg_summary_container);
			bgView.getBackground().setAlpha(useAlpha);
		}
	}

	@SuppressLint("NewApi")
	protected void setViewAlpha(float percentage, View view) {
		if (AndroidUtils.getSdkVersion() >= 11) {
			view.setAlpha(percentage);
		}
		else {
			//Leverage nineolddroids...
			AnimatorProxy.wrap(view).setAlpha(percentage);
		}
	}

	private void placeCardsFromMargins(int[] margins) {
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			RelativeLayout.LayoutParams params = (LayoutParams) tempFlight.getLayoutParams();
			params.topMargin = margins[i];
			tempFlight.setLayoutParams(params);
		}
	}

	private void measureFlightContainer() {
		if (getActivity() != null && mFlightContainer != null) {
			int w = getActivity().getResources().getDisplayMetrics().widthPixels;
			int h = getActivity().getResources().getDisplayMetrics().heightPixels;
			mFlightContainer.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
					MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
		}
	}

	private void measureDateAndTravelers() {
		if (mFlightDateAndTravCount != null && mFlightDateAndTravCount.getMeasuredHeight() <= 0) {
			int w = getActivity().getResources().getDisplayMetrics().widthPixels;
			int h = getActivity().getResources().getDisplayMetrics().heightPixels;
			mFlightDateAndTravCount.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
					MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
		}
	}

	private int[] getNormalTopMargins() {
		measureFlightContainer();
		int[] retVal = new int[mFlightContainer.getChildCount()];
		int currentTop = 0;
		currentTop += Math.max(mFlightDateAndTravCount.getMeasuredHeight(), mFlightDateAndTravCount.getHeight());
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

			if (mDisplayMode.compareTo(DisplayMode.OVERVIEW) == 0) {
				Runnable layoutRunner = new Runnable() {
					@Override
					public void run() {
						updateCardInfoText();
					}
				};
				if (FlightTripOverviewFragment.this.getView() != null) {
					FlightTripOverviewFragment.this.getView().postDelayed(layoutRunner, 200);
				}
			}
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {

		}

		@Override
		public void onAnimationStart(Animator arg0) {
			mCardsAnimating = true;

		}

	};

	private void updateCardInfoText() {
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			//a small rebind
			if (mTrip != null && mTrip.getLegCount() > i) {
				tempFlight.setInfoText(mTrip.getLeg(i));
			}
		}
	}
}
