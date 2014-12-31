package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.section.FlightInfoBarSection;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.utils.FragmentBailUtils;
import com.expedia.bookings.utils.Ui;

public class FlightTripOverviewFragment extends Fragment {

	private static final String ARG_DISPLAY_MODE = "ARG_DISPLAY_MODE";
	private static final int ID_START_RANGE = Integer.MAX_VALUE - 100;

	//The margin between cards when expanded
	private static final int FLIGHT_LEG_TOP_MARGIN = 20;
	private static final int TOP_CARD_STACKED_ALPHA = 255;
	private static final int TOP_CARD_UNSTACKED_ALPHA = 210;

	private FlightTrip mTrip;
	private FrameLayout mFlightContainer;
	private FlightInfoBarSection mFlightDateAndTravCount;
	private ViewGroup mRootView;

	private DisplayMode mDisplayMode = DisplayMode.OVERVIEW;

	private float mCurrentPercentage = 1f;

	public enum DisplayMode {
		CHECKOUT, OVERVIEW
	}

	public static FlightTripOverviewFragment newInstance(DisplayMode mode) {
		FlightTripOverviewFragment fragment = new FlightTripOverviewFragment();
		Bundle args = new Bundle();
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
		if (FragmentBailUtils.shouldBail(getActivity())) {
			return null;
		}

		mRootView = Ui.inflate(inflater, R.layout.fragment_flight_trip_overview, container, false);

		mFlightContainer = Ui.findView(mRootView, R.id.flight_legs_container);
		mFlightDateAndTravCount = Ui.findView(mRootView, R.id.date_and_travlers);

		mTrip = Db.getTripBucket().getFlight().getFlightTrip();

		mFlightDateAndTravCount.bindTripOverview(mTrip, Db.getTripBucket().getFlight().getFlightSearchParams().getNumTravelers());

		buildCards(inflater);

		return mRootView;
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

		measureDateAndTravelers();

		//Build the cards
		SectionFlightLeg tempFlight;
		for (int i = 0; i < mTrip.getLegCount(); i++) {
			tempFlight = Ui.inflate(inflater, R.layout.section_display_flight_leg, null);
			tempFlight.setId(ID_START_RANGE + i);

			//Set our background to be a selector so we can set opacity quickly later during animations
			if (tempFlight.getId() == ID_START_RANGE) {
				View bgView = Ui.findView(tempFlight, R.id.flight_leg_summary_container);
				bgView.setBackgroundResource(R.drawable.bg_flight_card_search_results_opaque);
				bgView.getBackground().setAlpha(TOP_CARD_UNSTACKED_ALPHA);
			}

			tempFlight.bind(new FlightTripLeg(mTrip, mTrip.getLeg(i)));
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);

			mFlightContainer.addView(tempFlight, params);

			measureCard(tempFlight);
		}

		//Z-index the card
		for (int i = mTrip.getLegCount() - 1; i >= 0; i--) {
			mFlightContainer.findViewById(ID_START_RANGE + i).bringToFront();
		}

		//Set the container to be tall enough
		LayoutParams containerParams = (LayoutParams) mFlightContainer.getLayoutParams();
		containerParams.height = getUnstackedHeight();
		mFlightContainer.setLayoutParams(containerParams);

		placeCardsFromPercentage(1);
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
		int[] margins = getStackedTopMargins();
		int retHeight = 0;
		SectionFlightLeg flight = Ui.findView(mFlightContainer, ID_START_RANGE);
		retHeight = getHeightFromMargins(margins, false) - margins[0] - getFlightLegUnusedHeight(flight);
		return retHeight;
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

		return retHeight;
	}

	public float getScrollOffsetForPercentage(float percentage) {
		return percentage * (getScrollOffsetStacked() - getScrollOffsetUnstacked());
	}

	public float getScrollOffsetUnstacked() {
		return 0f;
	}

	public float getScrollOffsetStacked() {
		return getUnstackedHeight() - getStackedHeight();
	}

	public void setExpandedPercentage(float percentage) {
		mCurrentPercentage = percentage;
		if (this.isAdded()) {
			placeCardsFromPercentage(percentage);
			setCardsAlpha(percentage);
			setTopCardBgAlpha(percentage);
		}
	}

	public float getExpandedPercentage() {
		return mCurrentPercentage;
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

	protected void setCardsAlpha(float percentage) {
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			View header = Ui.findView(tempFlight, R.id.info_text_view);
			View airline = Ui.findView(tempFlight, R.id.airline_text_view);

			header.setAlpha(percentage);
			airline.setAlpha(percentage);
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

	private void placeCardsFromMargins(int[] margins) {
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			tempFlight.setTranslationY(margins[i]);
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
		int flightContainerHeight = Math.max(mFlightContainer.getMeasuredHeight(), mFlightContainer.getHeight());
		int[] retVal = new int[mFlightContainer.getChildCount()];
		int currentTop = flightContainerHeight;
		for (int i = mFlightContainer.getChildCount() - 1; i >= 0; i--) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);

			int totalUnusedHeight = getFlightLegUnusedHeight(tempFlight);

			currentTop -= Math.max(tempFlight.getMeasuredHeight(), tempFlight.getHeight());
			retVal[i] = currentTop;
			currentTop += totalUnusedHeight;

		}
		return retVal;
	}

	private int getFlightLegUnusedHeight(SectionFlightLeg sectionFlightLeg) {
		View header = Ui.findView(sectionFlightLeg, R.id.info_text_view);
		View price = Ui.findView(sectionFlightLeg, R.id.price_text_view);
		View airline = Ui.findView(sectionFlightLeg, R.id.airline_text_view);
		View flightTripView = Ui.findView(sectionFlightLeg, R.id.flight_trip_view);

		int headerUnused = Math.max(header.getMeasuredHeight(), header.getHeight());
		int innerUnused = Math.max(Math.max(price.getMeasuredHeight(), price.getHeight()),
				Math.max(airline.getMeasuredHeight(), airline.getHeight()));

		//Dont forget margins...
		if (flightTripView != null && flightTripView.getLayoutParams() != null) {
			if (((RelativeLayout.LayoutParams) flightTripView.getLayoutParams()).topMargin > 0) {
				innerUnused += ((RelativeLayout.LayoutParams) flightTripView.getLayoutParams()).topMargin;
			}
		}
		return headerUnused + innerUnused;
	}

	public void updateCardInfoText() {
		for (int i = 0; i < mFlightContainer.getChildCount(); i++) {
			SectionFlightLeg tempFlight = Ui.findView(mFlightContainer, ID_START_RANGE + i);
			//a small rebind
			if (mTrip != null && mTrip.getLegCount() > i) {
				tempFlight.setInfoText(mTrip.getLeg(i));
			}
		}
	}
}
