package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightLeg;
import com.expedia.bookings.data.FlightPassenger;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.User;
import com.expedia.bookings.fragment.SignInFragment;
import com.expedia.bookings.fragment.SignInFragment.SignInFragmentListener;
import com.expedia.bookings.model.CheckoutFlowState;
import com.expedia.bookings.section.ISectionEditable.SectionChangeListener;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;
import com.mobiata.flightlib.data.Flight;

public class FlightCheckoutActivity extends SherlockFragmentActivity implements AccountButtonClickListener,
		SignInFragmentListener {

	private static final String INSTANCE_REFRESHED_USER = "INSTANCE_REFRESHED_USER";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	public static final String EXTRA_TRIP_KEY = "EXTRA_TRIP_KEY";

	String mTripKey;
	FlightTrip mTrip;
	BillingInfo mBillingInfo;

	ArrayList<SectionTravelerInfo> mTravelerSections = new ArrayList<SectionTravelerInfo>();

	private AccountButton mAccountButton;
	SectionBillingInfo mCreditCardSectionButton;
	SectionBillingInfo mCreditCardSecurityCodeSection;
	SectionLocation mAddressSection;

	Button mReviewBtn;
	ViewGroup mTravelerContainer;
	LinearLayout mPaymentContainer;

	View mPaymentDivOne;
	View mPaymentDivTwo;

	private boolean mRefreshedUser;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mRefreshedUser = savedInstanceState.getBoolean(INSTANCE_REFRESHED_USER);
		}

		setContentView(R.layout.activity_flight_checkout);

		mBillingInfo = Db.getBillingInfo();
		mBillingInfo.load(this);

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mAccountButton = Ui.findView(this, R.id.account_button_root);
		mCreditCardSectionButton = Ui.findView(this, R.id.creditcard_section_button);
		mCreditCardSecurityCodeSection = Ui.findView(this, R.id.creditcard_section_security_code);
		mTravelerContainer = Ui.findView(this, R.id.travelers_container);
		mAddressSection = Ui.findView(this, R.id.address_section);
		mReviewBtn = Ui.findView(this, R.id.review_btn);
		mPaymentContainer = Ui.findView(this, R.id.payment_container);
		mPaymentDivOne = Ui.findView(this, R.id.payment_div_one);
		mPaymentDivTwo = Ui.findView(this, R.id.payment_div_two);

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

		mCreditCardSecurityCodeSection.addChangeListener(new SectionChangeListener() {
			@Override
			public void onChange() {
				updatePaymentVisibilities();
			}
		});

		mTripKey = getIntent().getStringExtra(EXTRA_TRIP_KEY);
		if (mTripKey != null) {
			mTrip = Db.getFlightSearch().getFlightTrip(mTripKey);

			FlightLeg firstLeg = mTrip.getLeg(0);
			FlightLeg lastLeg = mTrip.getLeg(mTrip.getLegCount() - 1);

			String cityName = firstLeg.getSegment(firstLeg.getSegmentCount() - 1).mDestination.getAirport().mCity;

			Flight firstSeg = firstLeg.getSegment(0);
			Calendar startCal = firstSeg.mOrigin.getMostRelevantDateTime();
			String startMonthStr = DateUtils.getMonthString(startCal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
			int startDay = startCal.get(Calendar.DAY_OF_MONTH);
			String startDate = startMonthStr + " " + startDay;

			Flight lastSeg = lastLeg.getSegment(lastLeg.getSegmentCount() - 1);
			Calendar endCal = lastSeg.mDestination.getMostRelevantDateTime();
			String endMonthStr = DateUtils.getMonthString(endCal.get(Calendar.MONTH), DateUtils.LENGTH_SHORT);
			int endDay = endCal.get(Calendar.DAY_OF_MONTH);
			String endDate = "";

			if (startMonthStr.compareTo(endMonthStr) == 0) {
				//Same month
				endDate = "" + endDay;
			}
			else {
				//Dif months
				endDate = endMonthStr + " " + endDay;
			}

			Ui.setText(this, R.id.city_and_dates, String.format(
					getResources().getString(R.string.checkout_heading_city_and_dates_TEMPLATE), cityName, startDate,
					endDate));
		}

		ArrayList<FlightPassenger> passengers = Db.getFlightPassengers();
		if (passengers == null) {
			passengers = new ArrayList<FlightPassenger>();
			Db.setFlightPassengers(passengers);
		}

		if (passengers.size() == 0) {
			//TODO:Load from billing info, or from expedia account
			passengers.add(new FlightPassenger());
		}

		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < passengers.size(); i++) {
			final int travelerNum = i;
			SectionTravelerInfo traveler = (SectionTravelerInfo) inflater.inflate(
					R.layout.section_display_traveler_info_btn, null);
			traveler.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent editTravelerIntent = new Intent(FlightCheckoutActivity.this,
							FlightTravelerInfoOptionsActivity.class);
					editTravelerIntent.putExtra(Codes.PASSENGER_INDEX, travelerNum);
					startActivity(editTravelerIntent);
				}
			});
			mTravelerSections.add(traveler);
			mTravelerContainer.addView(traveler);
		}

		if (AndroidUtils.getSdkVersion() >= 11) {
			mPaymentContainer.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mBillingInfo = Db.getBillingInfo();
		bindAll();
		updatePaymentVisibilities();
		updateClickListeners();

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

		mAddressSection.bind(mBillingInfo.getLocation());
		mCreditCardSectionButton.bind(mBillingInfo);
		mCreditCardSecurityCodeSection.bind(mBillingInfo);

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

	private boolean hasValidTravlerSections() {
		if (mTravelerSections == null || mTravelerSections.size() <= 0) {
			return false;
		}
		else {
			//TODO:This isn't validating anything because these are display only sections
			for (int i = 0; i < mTravelerSections.size(); i++) {
				if (!mTravelerSections.get(i).hasValidInput()) {
					return false;
				}
			}
			return true;
		}
	}

	OnClickListener gotoBillingAddress = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent editAddress = new Intent(FlightCheckoutActivity.this, FlightPaymentAddressActivity.class);
			startActivity(editAddress);
		}
	};

	OnClickListener gotoCardInfo = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent editContact = new Intent(FlightCheckoutActivity.this, FlightPaymentCreditCardActivity.class);
			startActivity(editContact);
		}
	};

	private void updateClickListeners() {
		boolean paymentAddressValid = CheckoutFlowState.getInstance(this).hasValidBillingAddress(mBillingInfo);

		if (!paymentAddressValid) {
			mCreditCardSectionButton.setOnClickListener(gotoBillingAddress);
		}
		else {
			mCreditCardSectionButton.setOnClickListener(gotoCardInfo);
		}
		mAddressSection.setOnClickListener(gotoBillingAddress);
	}

	private void updatePaymentVisibilities() {
		boolean paymentAddressValid = CheckoutFlowState.getInstance(this).hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = CheckoutFlowState.getInstance(this).hasValidCardInfo(mBillingInfo);
		boolean paymentSecCodeValid = CheckoutFlowState.getInstance(this).hasValidSecurityCode(mBillingInfo);

		if (!paymentAddressValid && !paymentCCValid && !paymentSecCodeValid) {
			mAddressSection.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
			mCreditCardSecurityCodeSection.setVisibility(View.GONE);
			mReviewBtn.setEnabled(false);
			mPaymentDivOne.setVisibility(View.GONE);
			mPaymentDivTwo.setVisibility(View.GONE);
		}
		else if (paymentAddressValid && !paymentCCValid && !paymentSecCodeValid) {
			mAddressSection.setVisibility(View.VISIBLE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
			mCreditCardSecurityCodeSection.setVisibility(View.GONE);
			mReviewBtn.setEnabled(false);
			mPaymentDivOne.setVisibility(View.VISIBLE);
			mPaymentDivTwo.setVisibility(View.GONE);
		}
		else if (paymentAddressValid && paymentCCValid && !paymentSecCodeValid) {
			mAddressSection.setVisibility(View.VISIBLE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
			mCreditCardSecurityCodeSection.setVisibility(View.VISIBLE);
			mReviewBtn.setEnabled(false);
			mPaymentDivOne.setVisibility(View.VISIBLE);
			mPaymentDivTwo.setVisibility(View.VISIBLE);
		}
		else {
			mAddressSection.setVisibility(View.VISIBLE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
			mCreditCardSecurityCodeSection.setVisibility(View.VISIBLE);
			if (hasValidTravlerSections()) {
				mReviewBtn.setEnabled(true);
			}
			mPaymentDivOne.setVisibility(View.VISIBLE);
			mPaymentDivTwo.setVisibility(View.VISIBLE);
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