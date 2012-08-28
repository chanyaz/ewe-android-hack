package com.expedia.bookings.activity;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.SignInFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.model.PaymentFlowState;
import com.expedia.bookings.model.TravelerFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionFlightTrip;
import com.expedia.bookings.section.SectionGeneralFlightInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

public class FlightCheckoutActivity extends SherlockFragmentActivity implements AccountButtonClickListener,
		SignInFragmentListener {

	private static final String INSTANCE_REFRESHED_USER = "INSTANCE_REFRESHED_USER";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	//We only want to load from disk once: when the activity is first started (as it is the first time BillingInfo is seen)
	private static boolean mLoaded = false;

	FlightTrip mTrip;
	BillingInfo mBillingInfo;

	ArrayList<SectionTravelerInfo> mTravelerSections = new ArrayList<SectionTravelerInfo>();

	private AccountButton mAccountButton;
	SectionBillingInfo mCreditCardSectionButton;
	SectionFlightTrip mFlightTripSectionPriceBar;
	SectionGeneralFlightInfo mFlightDateAndTravCount;
	SectionStoredCreditCard mStoredCreditCard;

	Button mReviewBtn;
	ViewGroup mTravelerContainer;
	ViewGroup mTravelerButton;
	ViewGroup mPaymentButton;
	LinearLayout mPaymentContainer;

	private boolean mRefreshedUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Recover data if it was flushed from memory
		if (Db.getFlightSearch().getSearchResponse() == null) {
			if (!Db.loadCachedFlightData(this)) {
				NavUtils.onDataMissing(this);
				return;
			}
		}

		if (savedInstanceState != null) {
			mRefreshedUser = savedInstanceState.getBoolean(INSTANCE_REFRESHED_USER);
		}

		setContentView(R.layout.activity_flight_checkout);

		mBillingInfo = Db.getBillingInfo();
//		if (!mLoaded) {
//			mBillingInfo.load(this);
//			mLoaded = true;
//		}

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mTravelerButton = Ui.findView(this, R.id.traveler_info_btn);
		mPaymentButton = Ui.findView(this, R.id.payment_info_btn);
		mStoredCreditCard = Ui.findView(this, R.id.stored_creditcard_section_button);
		mCreditCardSectionButton = Ui.findView(this, R.id.creditcard_section_button);
		mTravelerContainer = Ui.findView(this, R.id.travelers_container);
		mAccountButton = Ui.findView(this, R.id.account_button_root);
		mReviewBtn = Ui.findView(this, R.id.review_btn);
		mPaymentContainer = Ui.findView(this, R.id.payment_container);
		mFlightTripSectionPriceBar = Ui.findView(this, R.id.price_bar);
		mFlightDateAndTravCount = Ui.findView(this, R.id.date_and_travlers);

		// Detect user state, update account button accordingly
		mAccountButton.setListener(this);
		if (User.isLoggedIn(this)) {
			if (Db.getUser() == null) {
				Db.loadUser(this);
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
					mBillingInfo.save(FlightCheckoutActivity.this);
				}
				Intent intent = new Intent(FlightCheckoutActivity.this, FlightBookingActivity.class);
				startActivity(intent);
			}
		});

		mTrip = Db.getFlightSearch().getSelectedFlightTrip();
		mFlightDateAndTravCount.bind(mTrip,
				(Db.getFlightPassengers() != null && Db.getFlightPassengers().size() != 0) ? Db.getFlightPassengers()
						.size() : 1);
		String cityName = mTrip.getLeg(0).getLastWaypoint().getAirport().mCity;
		String yourTripToStr = String.format(getString(R.string.your_trip_to_TEMPLATE), cityName);

	
		//Actionbar
		ActionBar actionBar = this.getSupportActionBar();
		actionBar.setTitle(yourTripToStr);
		
		//Set values
		populatePassengerData();
		buildPassengerSections();
	}

	private void populatePassengerData() {
		ArrayList<FlightPassenger> passengers = Db.getFlightPassengers();
		if (passengers == null) {
			passengers = new ArrayList<FlightPassenger>();
			Db.setFlightPassengers(passengers);
		}

		if (passengers.size() == 0) {
			FlightPassenger fp = new FlightPassenger();
			passengers.add(fp);
		}
	}

	private void buildPassengerSections() {
		ArrayList<FlightPassenger> passengers = Db.getFlightPassengers();
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < passengers.size(); i++) {
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
			Intent editTravelerIntent = new Intent(FlightCheckoutActivity.this,
					FlightTravelerInfoOptionsActivity.class);
			editTravelerIntent.putExtra(Codes.PASSENGER_INDEX, mTravelerIndex);
			startActivity(editTravelerIntent);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mBillingInfo = Db.getBillingInfo();
		bindAll();
		updateViewVisibilities();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_REFRESH_USER);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putBoolean(INSTANCE_REFRESHED_USER, mRefreshedUser);
	}

	public void bindAll() {
		Log.i("bindAll");

		mCreditCardSectionButton.bind(mBillingInfo);
		mStoredCreditCard.bind(mBillingInfo.getStoredCard());
		mFlightTripSectionPriceBar.bind(mTrip);

		ArrayList<FlightPassenger> passengers = Db.getFlightPassengers();
		if (passengers.size() != mTravelerSections.size()) {
			Ui.showToast(this, "Traveler info out of date...");
			Log.e("Traveler info fail... passengers size():" + passengers.size() + " sections:"
					+ mTravelerSections.size());
		}
		else {
			for (int i = 0; i < passengers.size(); i++) {
				mTravelerSections.get(i).bind(passengers.get(i));
			}
		}

	}

	private boolean hasValidTravlers() {
		if (mTravelerSections == null || mTravelerSections.size() <= 0) {
			return false;
		}
		else {
			boolean travelerValid = true;
			if (Db.getFlightPassengers() == null || Db.getFlightPassengers().size() <= 0) {
				travelerValid = false;
			}
			else {
				for (int i = 0; i < Db.getFlightPassengers().size(); i++) {
					travelerValid &= (TravelerFlowState.getInstance(this).allTravelerInfoIsValid(Db
							.getFlightPassengers()
							.get(i)));
				}
			}
			return travelerValid;
		}
	}

	OnClickListener gotoBillingAddress = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent editAddress = new Intent(FlightCheckoutActivity.this, FlightPaymentOptionsActivity.class);
			startActivity(editAddress);
		}
	};

	private void updateViewVisibilities() {

		boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : PaymentFlowState.getInstance(this)
				.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : PaymentFlowState.getInstance(this).hasValidCardInfo(
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

	//////////////////////////////////////////////////////////////////////////
	// AccountButtonClickListener

	@Override
	public void accountLoginClicked() {
		SignInFragment.newInstance().show(getSupportFragmentManager(), getString(R.string.tag_signin));
	}

	@Override
	public void accountLogoutClicked() {
		// Stop refreshing user (if we're currently doing so)
		BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		mRefreshedUser = false;

		// Sign out user
		User.signOut(this);

		// Update UI
		mAccountButton.bind(false, false, null);
	}

	//////////////////////////////////////////////////////////////////////////
	// SignInFragmentListener

	@Override
	public void onLoginStarted() {
		// Do nothing?
	}

	@Override
	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser());
		mRefreshedUser = true;
		
		if(Db.getFlightPassengers() != null && Db.getFlightPassengers().size() > 0){
			Db.getFlightPassengers().clear();
		}
		populatePassengerData();
		
		// TODO: Update rest of UI based on new logged-in data.
	}

	@Override
	public void onLoginFailed() {
		// TODO: Update UI to show that we're no longer logged in
	}

	//////////////////////////////////////////////////////////////////////////
	// Refresh user

	private final Download<SignInResponse> mRefreshUserDownload = new Download<SignInResponse>() {
		@Override
		public SignInResponse doDownload() {
			ExpediaServices services = new ExpediaServices(FlightCheckoutActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_REFRESH_USER, services);
			return services.signIn();
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
				onLoginFailed();
			}
			else {
				// Update our existing saved data
				User user = results.getUser();
				user.save(FlightCheckoutActivity.this);
				Db.setUser(user);

				// Act as if a login just occurred
				onLoginCompleted();
			}
		}
	};
}