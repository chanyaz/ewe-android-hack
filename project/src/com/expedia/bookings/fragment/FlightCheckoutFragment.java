package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightBookingActivity;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity;
import com.expedia.bookings.activity.FlightTravelerInfoOptionsActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.PaymentFlowState;
import com.expedia.bookings.model.TravelerFlowState;
import com.expedia.bookings.section.FlightLegSummarySection;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionFlightTrip;
import com.expedia.bookings.section.SectionGeneralFlightInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class FlightCheckoutFragment extends Fragment implements AccountButtonClickListener {

	private static final String INSTANCE_REFRESHED_USER = "INSTANCE_REFRESHED_USER";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";
	private static final String KEY_TRAVELER_DATA = "KEY_TRAVELER_DATA";

	//We only want to load from disk once: when the activity is first started (as it is the first time BillingInfo is seen)
	private static boolean mLoaded = false;

	private FlightTrip mTrip;
	private BillingInfo mBillingInfo;

	private ArrayList<SectionTravelerInfo> mTravelerSections = new ArrayList<SectionTravelerInfo>();
	private ArrayList<FlightLegSummarySection> mFlights = new ArrayList<FlightLegSummarySection>();

	private AccountButton mAccountButton;
	private SectionBillingInfo mCreditCardSectionButton;
	private SectionFlightTrip mFlightTripSectionPriceBar;
	private SectionGeneralFlightInfo mFlightDateAndTravCount;
	private SectionStoredCreditCard mStoredCreditCard;

	private Button mReviewBtn;
	private ViewGroup mTravelerContainer;
	private ViewGroup mTravelerButton;
	private ViewGroup mPaymentButton;
	private RelativeLayout mFlightContainer;
	private LinearLayout mPaymentContainer;

	private boolean mRefreshedUser;

	public static FlightCheckoutFragment newInstance() {
		FlightCheckoutFragment fragment = new FlightCheckoutFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRefreshedUser = savedInstanceState.getBoolean(INSTANCE_REFRESHED_USER);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_flight_checkout, container, false);

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(getActivity())) {
				NavUtils.onDataMissing(getActivity());
				//return;
			}
		}

		if (savedInstanceState != null) {
			mRefreshedUser = savedInstanceState.getBoolean(INSTANCE_REFRESHED_USER);
		}

		mBillingInfo = Db.getBillingInfo();
		//		if (!mLoaded) {
		//			mBillingInfo.load(this);
		//			mLoaded = true;
		//		}

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mTravelerButton = Ui.findView(v, R.id.traveler_info_btn);
		mPaymentButton = Ui.findView(v, R.id.payment_info_btn);
		mStoredCreditCard = Ui.findView(v, R.id.stored_creditcard_section_button);
		mCreditCardSectionButton = Ui.findView(v, R.id.creditcard_section_button);
		mTravelerContainer = Ui.findView(v, R.id.travelers_container);
		mFlightContainer = Ui.findView(v, R.id.flight_legs_container);
		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mReviewBtn = Ui.findView(v, R.id.review_btn);
		mPaymentContainer = Ui.findView(v, R.id.payment_container);
		mFlightTripSectionPriceBar = Ui.findView(v, R.id.price_bar);
		mFlightDateAndTravCount = Ui.findView(v, R.id.date_and_travlers);

		// Detect user state, update account button accordingly
		mAccountButton.setListener(this);
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (!mRefreshedUser) {
				Log.d("Refreshing user profile...");

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				if (!bd.isDownloading(KEY_REFRESH_USER)) {
					bd.startDownload(KEY_REFRESH_USER, mRefreshUserDownload, mRefreshUserCallback);
				}
			}
			mAccountButton.bind(false, true, Db.getUser());
		}
		else {
			mAccountButton.bind(false, false, null);
		}

		mCreditCardSectionButton.setOnClickListener(gotoBillingAddress);
		mStoredCreditCard.setOnClickListener(gotoBillingAddress);
		mPaymentButton.setOnClickListener(gotoBillingAddress);
		mTravelerButton.setOnClickListener(new OnTravelerClickListener(0));
		mReviewBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBillingInfo != null) {
					mBillingInfo.save(getActivity());
				}
				Intent intent = new Intent(getActivity(), FlightBookingActivity.class);
				startActivity(intent);
			}
		});

		mTrip = Db.getFlightSearch().getSelectedFlightTrip();
		mFlightDateAndTravCount.bind(mTrip,
				(Db.getTravelers() != null && Db.getTravelers().size() != 0) ? Db.getTravelers()
						.size() : 1);

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		mBillingInfo = Db.getBillingInfo();

		//Set values
		populateTravelerData();
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		buildTravelerSections();
		buildLegSections();

		bindAll();
		updateViewVisibilities();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_REFRESH_USER);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_REFRESHED_USER, mRefreshedUser);
	}

	public void bindAll() {
		mCreditCardSectionButton.bind(mBillingInfo);
		mStoredCreditCard.bind(mBillingInfo.getStoredCard());
		mFlightTripSectionPriceBar.bind(mTrip);

		ArrayList<Traveler> travelers = Db.getTravelers();
		if (travelers.size() != mTravelerSections.size()) {
			Ui.showToast(getActivity(), "Traveler info out of date...");
			Log.e("Traveler info fail... travelers size():" + travelers.size() + " sections:"
					+ mTravelerSections.size());
		}
		else {
			for (int i = 0; i < travelers.size(); i++) {
				mTravelerSections.get(i).bind(travelers.get(i));
			}
		}
	}

	private void buildLegSections() {
		//Inflate and store the sections
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		FlightLegSummarySection tempFlight = null;
		mFlightContainer.removeAllViews();
		mFlights.clear();
		int padding = 20;
		int alphaMin = 100;
		int alphaMax = 225;
		for (int i = mTrip.getLegCount() - 1; i >= 0; i--) {
			tempFlight = (FlightLegSummarySection) inflater.inflate(R.layout.section_flight_leg_summary_short, null);
			tempFlight.setPadding(tempFlight.getPaddingLeft(),
					tempFlight.getPaddingTop() > padding ? tempFlight.getPaddingTop() : padding,
					tempFlight.getPaddingRight(), tempFlight.getPaddingBottom());
			tempFlight.bind(mTrip, mTrip.getLeg(mTrip.getLegCount() - 1 - i));
			tempFlight.getBackground().setAlpha(
					Math.max(alphaMin, Math.min((mTrip.getLegCount() - 1 - i) * 150 + 75, alphaMax)));
			mFlights.add(0, tempFlight);
			mFlightContainer.addView(tempFlight, 0);

			int widthMeasureSpec = MeasureSpec.makeMeasureSpec(RelativeLayout.LayoutParams.MATCH_PARENT,
					MeasureSpec.EXACTLY);
			int heightMeasureSpec = MeasureSpec.makeMeasureSpec(RelativeLayout.LayoutParams.WRAP_CONTENT,
					MeasureSpec.EXACTLY);
			tempFlight.measure(widthMeasureSpec, heightMeasureSpec);

			padding += tempFlight.getMeasuredHeight();
		}
	}

	private void populateTravelerData() {
		ArrayList<Traveler> travelers = Db.getTravelers();
		if (travelers == null) {
			travelers = new ArrayList<Traveler>();
			Db.setTravelers(travelers);
		}

		if (travelers.size() == 0) {
			Traveler fp = new Traveler();
			travelers.add(fp);
		}
	}

	private void buildTravelerSections() {
		mTravelerContainer.removeAllViews();
		mTravelerSections.clear();

		ArrayList<Traveler> travelers = Db.getTravelers();
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < travelers.size(); i++) {
			final int travelerNum = i;
			SectionTravelerInfo traveler = (SectionTravelerInfo) inflater.inflate(
					R.layout.section_display_traveler_info_btn, null);
			traveler.setOnClickListener(new OnTravelerClickListener(travelerNum));
			mTravelerSections.add(traveler);
			mTravelerContainer.addView(traveler);
		}
	}

	private class OnTravelerClickListener implements OnClickListener {
		int mTravelerIndex = 0;

		public OnTravelerClickListener(int travelerIndex) {
			if (travelerIndex >= 0) {
				mTravelerIndex = travelerIndex;
			}
		}

		@Override
		public void onClick(View v) {
			Intent editTravelerIntent = new Intent(getActivity(),
					FlightTravelerInfoOptionsActivity.class);
			editTravelerIntent.putExtra(Codes.TRAVELER_INDEX, mTravelerIndex);
			startActivity(editTravelerIntent);
		}
	}

	private boolean hasValidTravlers() {
		if (mTravelerSections == null || mTravelerSections.size() <= 0) {
			return false;
		}
		else {
			boolean travelerValid = true;
			if (Db.getTravelers() == null || Db.getTravelers().size() <= 0) {
				travelerValid = false;
			}
			else {
				ArrayList<Traveler> travelers = Db.getTravelers();
				for (int i = 0; i < travelers.size(); i++) {
					travelerValid &= (TravelerFlowState.getInstance(getActivity()).allTravelerInfoIsValid(
							travelers.get(i)));
				}
			}
			return travelerValid;
		}
	}

	OnClickListener gotoBillingAddress = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent editAddress = new Intent(getActivity(), FlightPaymentOptionsActivity.class);
			startActivity(editAddress);
		}
	};

	private void updateViewVisibilities() {

		boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : PaymentFlowState.getInstance(getActivity())
				.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : PaymentFlowState.getInstance(getActivity())
				.hasValidCardInfo(
						mBillingInfo);
		boolean travelerValid = hasValidTravlers();

		if (travelerValid) {
			mTravelerButton.setVisibility(View.GONE);
			mTravelerContainer.setVisibility(View.VISIBLE);
		}
		else {
			mTravelerButton.setVisibility(View.VISIBLE);
			mTravelerContainer.setVisibility(View.GONE);
		}

		if (hasStoredCard) {
			mStoredCreditCard.setVisibility(View.VISIBLE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.GONE);
		}
		else if (paymentAddressValid && paymentCCValid) {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
		}
		else {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentButton.setVisibility(View.VISIBLE);
			mCreditCardSectionButton.setVisibility(View.GONE);
		}

		if (paymentAddressValid && paymentCCValid && travelerValid) {
			mReviewBtn.setEnabled(true);
		}
		else {
			mReviewBtn.setEnabled(false);
		}
	}

	private void populateTravelerDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate traveler data
			if (Db.getTravelers() != null && Db.getTravelers().size() >= 1) {
				//If the first traveler in the list isn't valid, then populate it with data from the User
				if (!TravelerFlowState.getInstance(getActivity())
						.allTravelerInfoIsValid(Db.getTravelers().get(0))) {
					Log.w("SET USER TRAVELER DATA!");
					//Db.getTravelers().set(0, UserDataTransfer.getBestGuessStoredTraveler(Db.getUser()));
				}

				//TODO:Uncomment this when the traveler api is finished. This may or may not be working correctly.
				//				mGetTravelerInfo.setTraveler(Db.getTravelers().get(0));
				//				BackgroundDownloader bd = BackgroundDownloader.getInstance();
				//				if (!bd.isDownloading(KEY_TRAVELER_DATA)) {
				//					bd.startDownload(KEY_TRAVELER_DATA, mGetTravelerInfo, mGetTravelerCallback);
				//				}
			}
		}
		else {
			//Travelers that have tuids are from the account and thus should be removed.
			for (int i = 0; i < Db.getTravelers().size(); i++) {
				if (Db.getTravelers().get(i).hasTuid()) {
					Db.getTravelers().set(i, new Traveler());
				}
			}
		}
	}

	private void populatePaymentDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate Credit Card
			PaymentFlowState paymentState = PaymentFlowState.getInstance(getActivity());
			boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
			boolean paymentAddressValid = hasStoredCard ? hasStoredCard : paymentState
					.hasValidBillingAddress(mBillingInfo);
			boolean paymentCCValid = hasStoredCard ? hasStoredCard : paymentState.hasValidCardInfo(mBillingInfo);
			if (Db.getUser().getStoredCreditCards() != null && Db.getUser().getStoredCreditCards().size() > 0
					&& !(paymentAddressValid && paymentCCValid)) {
				mBillingInfo.setStoredCard(Db.getUser().getStoredCreditCards().get(0));
			}
		}
		else {
			//Remove stored card(s)
			Db.getBillingInfo().setStoredCard(null);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// AccountButtonClickListener

	@Override
	public void accountLoginClicked() {

		SignInFragment.newInstance().show(getFragmentManager(), getString(R.string.tag_signin));

	}

	@Override
	public void accountLogoutClicked() {
		// Stop refreshing user (if we're currently doing so)
		BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		mRefreshedUser = false;

		// Sign out user
		User.signOut(getActivity());

		// Update UI
		mAccountButton.bind(false, false, null);

		//After logout this will clear stored cards
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		bindAll();
		updateViewVisibilities();
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser());
		mRefreshedUser = true;

		populateTravelerData();

		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		bindAll();
		updateViewVisibilities();
	}

	//////////////////////////////////////////////////////////////////////////
	// Refresh user

	private final Download<SignInResponse> mRefreshUserDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REFRESH_USER, services);
			return services.signIn(ExpediaServices.F_FLIGHTS);
		}
	};

	private final OnDownloadComplete<SignInResponse> mRefreshUserCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			if (results == null || results.hasErrors()) {
				// TODO: Figure out how to properly handle an error refresh
				//
				// Currently, the app just forces you to log in again.  But I'm not
				// convinced that's the best solution for now, especially since
				// you can get a lot of errors on integration while trying to
				// re-sign in.

				mAccountButton.error();
				//onLoginFailed();
			}
			else {
				// Update our existing saved data
				User user = results.getUser();
				user.save(getActivity());
				Db.setUser(user);

				// Act as if a login just occurred
				onLoginCompleted();
			}
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Update Traveler

	private class TravelerDownload implements Download<SignInResponse> {
		Traveler mTraveler;

		public void setTraveler(Traveler traveler) {
			mTraveler = traveler;
		}

		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(getActivity());
			BackgroundDownloader.getInstance().addDownloadListener(KEY_TRAVELER_DATA, services);
			return services.updateTraveler(mTraveler, 0);
		}
	}

	private final TravelerDownload mGetTravelerInfo = new TravelerDownload();
	private final OnDownloadComplete<SignInResponse> mGetTravelerCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			if (results == null || results.hasErrors()) {
				//TODO:If we don't have traveler info do something useful...
				Ui.showToast(getActivity(), "Fail to update traveler");
			}
			else {
				// Update our existing saved data
				Traveler traveler = results.getTraveler();
				for (int i = 0; i < Db.getTravelers().size(); i++) {
					if (traveler.getTuid() == (Db.getTravelers().get(i).hasTuid() ? Db.getTravelers()
							.get(i).getTuid() : 0)) {
						Db.getTravelers().set(i, traveler);
						break;
					}
				}
				bindAll();

			}
		}
	};
}
