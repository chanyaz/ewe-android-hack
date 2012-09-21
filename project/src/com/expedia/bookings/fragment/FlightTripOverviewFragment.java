package com.expedia.bookings.fragment;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreateItineraryResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.FlightTripLeg;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.section.SectionFlightLeg;
import com.expedia.bookings.section.SectionGeneralFlightInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.Ui;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class FlightTripOverviewFragment extends Fragment {

	private static final String ARG_TRIP_KEY = "ARG_TRIP_KEY";
	private static final String ARG_DISPLAY_MODE = "ARG_DISPLAY_MODE";

	private static final String KEY_DETAILS = "KEY_DETAILS";

	private static final String INSTANCE_REQUESTED_DETAILS = "INSTANCE_REQUESTED_DETAILS";

	private static final int ID_START_RANGE = Integer.MAX_VALUE - 100;

	private static final int ANIMATION_DURATION = 1000;

	//The margin between cards when expanded
	private static final int FLIGHT_LEG_TOP_MARGIN = 20;

	private FlightTrip mTrip;
	private RelativeLayout mFlightContainer;
	private SectionGeneralFlightInfo mFlightDateAndTravCount;

	private boolean mRequestedDetails = false;

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

		if (savedInstanceState != null) {
			mRequestedDetails = savedInstanceState.getBoolean(INSTANCE_REQUESTED_DETAILS, false);
		}
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
		

		// See if we have flight details we can use, first.
		if (TextUtils.isEmpty(mTrip.getItineraryNumber())) {

			// Begin loading flight details in the background, if we haven't already
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			if (!bd.isDownloading(KEY_DETAILS) && !mRequestedDetails) {
				// Show a loading dialog
				LoadingDetailsDialogFragment df = new LoadingDetailsDialogFragment();
				df.show(getFragmentManager(), LoadingDetailsDialogFragment.TAG);

				bd.startDownload(KEY_DETAILS, mFlightDetailsDownload, mFlightDetailsCallback);
			}
		}

		mFlightDateAndTravCount.bind(mTrip,
				(Db.getTravelers() != null && Db.getTravelers().size() != 0) ? Db.getTravelers()
						.size() : 1);

		buildCards(inflater);
		return v;
	}
	
	@Override
	public void onResume(){
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_DETAILS)) {
			bd.registerDownloadCallback(KEY_DETAILS, mFlightDetailsCallback);
		}
		
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

		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_DETAILS);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_DETAILS);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(ARG_DISPLAY_MODE, this.mDisplayMode.name());
		outState.putBoolean(INSTANCE_REQUESTED_DETAILS, mRequestedDetails);
	}

	private void buildCards(LayoutInflater inflater) {
		//Inflate and store the sections
		mFlightContainer.removeAllViews();
		SectionFlightLeg tempFlight;
		for (int i = 0; i < mTrip.getLegCount(); i++) {
			tempFlight = (SectionFlightLeg) inflater.inflate(R.layout.section_display_flight_leg, null);
			tempFlight.setId(ID_START_RANGE + i);

			tempFlight.bind(new FlightTripLeg(mTrip, mTrip.getLeg(i)), false);

			mFlightContainer.addView(tempFlight);
		}
		mFlightContainer.setMinimumHeight(getUnstackedHeight());
		mFlightContainer.invalidate();
	}
	
	public int getStackedHeight(){
		return getHeightFromMargins(getStackedTopMargins(),false);
	}
	
	public int getUnstackedHeight(){
		return getHeightFromMargins(getNormalTopMargins(), true);
	}
	
	private int getHeightFromMargins(int[] margins, boolean includeTravBar){
		int retHeight = 0;
		
		int lastInd = margins.length - 1;
		int lastMargin = margins[lastInd];
		SectionFlightLeg lastFlight = Ui.findView(mFlightContainer, lastInd + ID_START_RANGE);

		if(includeTravBar){
			measureDateAndTravelers();
			retHeight += Math.max(mFlightDateAndTravCount.getMeasuredHeight(),mFlightDateAndTravCount.getHeight());
		}
		retHeight += lastMargin;
		retHeight += Math.max(lastFlight.getMeasuredHeight(),lastFlight.getHeight());
		
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
		if(mFlightContainer == null){
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
			Log.v("FlightContainer MeasuredHeight:" + mFlightContainer.getMeasuredHeight() + " Height:" + mFlightContainer.getHeight());
		}
	}
	
	private void measureDateAndTravelers(){
		if(mFlightDateAndTravCount != null && mFlightDateAndTravCount.getMeasuredHeight() <= 0){
			int w = getActivity().getResources().getDisplayMetrics().widthPixels;
			int h = getActivity().getResources().getDisplayMetrics().heightPixels;
			Log.i("measuring date and traveler bar... w:" + w + " h:" + h);
			mFlightDateAndTravCount.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.AT_MOST),
					MeasureSpec.makeMeasureSpec(h, MeasureSpec.AT_MOST));
			Log.i("mFlightDateAndTravCount MeasuredHeight:" + mFlightDateAndTravCount.getMeasuredHeight() + " Height:" + mFlightDateAndTravCount.getHeight());
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
			currentTop += Math.max(tempFlight.getMeasuredHeight(),tempFlight.getHeight());

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

			int headerUnused = Math.max(header.getMeasuredHeight(),header.getHeight());
			int innerUnused = Math.max(Math.max(price.getMeasuredHeight(),price.getHeight()),Math.max( airline.getMeasuredHeight(),airline.getHeight()));
			int totalUnusedHeight = headerUnused + innerUnused;

			currentTop -= totalUnusedHeight;
			retVal[i] = currentTop;
			currentTop += Math.max(tempFlight.getMeasuredHeight(),tempFlight.getHeight());
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
		animators.addAll(getCardAnimators(getNormalTopMargins(),getStackedTopMargins()));
		animators.addAll(getDateTravelerBarAnimators(true));
		AnimatorSet animSet = new AnimatorSet();
		animSet.playTogether(animators);
		animSet.setDuration(duration);
		animSet.start();
	}
	
	private void animateCardsToUnStacked(int duration) {
		ArrayList<Animator> animators = new ArrayList<Animator>();
		animators.addAll(getCardTextAlphaAnimators(0f, 1f));
		animators.addAll(getCardAnimators(getStackedTopMargins(),getNormalTopMargins()));
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
	
	private ArrayList<Animator> getDateTravelerBarAnimators(boolean hide){
		ArrayList<Animator> animators = new ArrayList<Animator>();
		
		int barHeight = Math.max(mFlightDateAndTravCount.getMeasuredHeight(),mFlightDateAndTravCount.getHeight());
		int start = 0;
		int end = -barHeight;
		float alphaStart = 1f;
		float alphaEnd = 0f;
		if(!hide){
			start = -barHeight;
			end = 0;
			alphaStart = 0f;
			alphaEnd = 1f;
		}
		
		//move and hide the traveler price bar
		ObjectAnimator mover = ObjectAnimator.ofFloat(mFlightDateAndTravCount, "y", start, end);
		animators.add(mover);
	
		ObjectAnimator dateTravBarAlpha = ObjectAnimator.ofFloat(mFlightDateAndTravCount, "alpha", alphaStart, alphaEnd);
		animators.add(dateTravBarAlpha);
		
		//move up the flights container
		ObjectAnimator flightsMover = ObjectAnimator.ofFloat(mFlightContainer, "y", start + barHeight, end + barHeight);
		animators.add(flightsMover);
		
		return animators;
	}


	//////////////////////////////////////////////////////////////////////////
	// Flight details download

	private Download<CreateItineraryResponse> mFlightDetailsDownload = new Download<CreateItineraryResponse>() {
		@Override
		public CreateItineraryResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_DETAILS, services);
			return services.createItinerary(mTrip.getProductKey(), 0);
		}
	};

	private OnDownloadComplete<CreateItineraryResponse> mFlightDetailsCallback = new OnDownloadComplete<CreateItineraryResponse>() {
		@Override
		public void onDownload(CreateItineraryResponse results) {
			LoadingDetailsDialogFragment df = Ui.findSupportFragment(getCompatibilityActivity(),
					LoadingDetailsDialogFragment.TAG);
			df.dismiss();

			if (results == null) {
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null,
						getString(R.string.error_server));
				dialogFragment.show(getFragmentManager(), "errorFragment");
			}
			else if (results.hasErrors()) {
				String error = results.getErrors().get(0).getPresentableMessage(getActivity());
				DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null, error);
				dialogFragment.show(getFragmentManager(), "errorFragment");
			}
			else {
				Db.addItinerary(results.getItinerary());
				mTrip.updateFrom(results.getOffer());
				mRequestedDetails = true;

				Db.kickOffBackgroundSave(getActivity());

				//TODO:Enable the menu checkout button (by default it should be disabled)...

				if (mTrip.notifyPriceChanged()) {
					String newFare = mTrip.getTotalFare().getFormattedMoney();
					Money oldAmount = new Money(mTrip.getTotalFare());
					oldAmount.subtract(mTrip.getPriceChangeAmount());
					String oldFare = oldAmount.getFormattedMoney();
					String msg = getString(R.string.price_change_alert_TEMPLATE, oldFare, newFare);

					DialogFragment dialogFragment = SimpleSupportDialogFragment.newInstance(null, msg);
					dialogFragment.show(getFragmentManager(), "noticeFragment");
				}
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Progress dialog

	public static class LoadingDetailsDialogFragment extends DialogFragment {

		public static final String TAG = LoadingDetailsDialogFragment.class.getName();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			setCancelable(true);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			ProgressDialog pd = new ProgressDialog(getActivity());
			pd.setMessage(getString(R.string.loading_flight_details));
			pd.setCanceledOnTouchOutside(false);
			return pd;
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);

			// If the dialog is canceled without finishing loading, don't show this page.
			getActivity().finish();
		}
	}
}
