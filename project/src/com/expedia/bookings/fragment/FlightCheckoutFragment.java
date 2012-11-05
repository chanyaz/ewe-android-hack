package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.TextView;
import com.expedia.bookings.R;
import com.expedia.bookings.activity.*;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.model.PaymentFlowState;
import com.expedia.bookings.model.TravelerFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class FlightCheckoutFragment extends Fragment implements AccountButtonClickListener,
		ConfirmLogoutDialogFragment.DoLogoutListener {

	private static final String INSTANCE_REFRESHED_USER = "INSTANCE_REFRESHED_USER";

	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	private Context mContext;

	private BillingInfo mBillingInfo;

	private ArrayList<SectionTravelerInfo> mTravelerSections = new ArrayList<SectionTravelerInfo>();

	private TextView mAccountLabel;
	private AccountButton mAccountButton;
	private SectionBillingInfo mCreditCardSectionButton;
	private SectionStoredCreditCard mStoredCreditCard;

	private ViewGroup mTravelerContainer;
	private ViewGroup mTravelerButton;
	private ViewGroup mPaymentButton;

	private boolean mRefreshedUser;

	private CheckoutInformationListener mListener;

	public static FlightCheckoutFragment newInstance() {
		FlightCheckoutFragment fragment = new FlightCheckoutFragment();
		Bundle args = new Bundle();
		//TODO:Set args here..
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof CheckoutInformationListener) {
			mListener = (CheckoutInformationListener) activity;
		}
		else {
			throw new RuntimeException(
					"FlightCheckoutFragment must bind to an activity that implements CheckoutInformationListener");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity();

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
			}
		}

		if (savedInstanceState != null) {
			mRefreshedUser = savedInstanceState.getBoolean(INSTANCE_REFRESHED_USER);
		}

		//If we had data on disk, it should already be loaded at this point
		mBillingInfo = Db.getBillingInfo();

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mTravelerButton = Ui.findView(v, R.id.traveler_info_btn);
		mPaymentButton = Ui.findView(v, R.id.payment_info_btn);
		mStoredCreditCard = Ui.findView(v, R.id.stored_creditcard_section_button);
		mCreditCardSectionButton = Ui.findView(v, R.id.creditcard_section_button);
		mTravelerContainer = Ui.findView(v, R.id.travelers_container);
		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mAccountLabel = Ui.findView(v, R.id.expedia_account_label);

		ViewUtils.setAllCaps(mAccountLabel);
		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.checkout_information_label));

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
			mAccountButton.bind(false, true, Db.getUser(), true);
		}
		else {
			mAccountButton.bind(false, false, null, true);
		}

		// rules and restrictions link stuff
		TextView tv = Ui.findView(v, R.id.legal_blurb);
		tv.setText(Html.fromHtml(mContext.getString(R.string.fare_rules_link)));

		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(mContext, FlightRulesActivity.class);
				startActivity(intent);
			}
		});

		mCreditCardSectionButton.setOnClickListener(gotoPaymentOptions);
		mStoredCreditCard.setOnClickListener(gotoPaymentOptions);
		mPaymentButton.setOnClickListener(gotoPaymentOptions);
		mTravelerButton.setOnClickListener(new OnTravelerClickListener(0, true));

		return v;
	}

	@Override
	public void onResume() {
		super.onResume();

		refreshData();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(KEY_REFRESH_USER)) {
			bd.registerDownloadCallback(KEY_REFRESH_USER, mRefreshUserCallback);
		}

		ConfirmLogoutDialogFragment confirmLogoutFrag = (ConfirmLogoutDialogFragment) getFragmentManager()
				.findFragmentByTag(ConfirmLogoutDialogFragment.TAG);
		if (confirmLogoutFrag != null) {
			confirmLogoutFrag.setDoLogoutListener(this);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (Db.getTravelersAreDirty()) {
			Db.kickOffBackgroundTravelerSave(getActivity());
		}

		if (Db.getBillingInfoIsDirty()) {
			Db.kickOffBackgroundBillingInfoSave(getActivity());
		}

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

	/**
	 * We refresh the billingInfo from Db and bind all the views.
	 * This is what happens to initally set the state on resume, so if data changes are made after resume is called
	 * we should call this method
	 */
	public void refreshData() {
		mBillingInfo = Db.getBillingInfo();

		//Set values
		populateTravelerData();
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		buildTravelerSections();

		bindAll();
		updateViewVisibilities();
	}

	private void bindAll() {
		mCreditCardSectionButton.bind(mBillingInfo);
		mStoredCreditCard.bind(mBillingInfo.getStoredCard());

		List<Traveler> travelers = Db.getTravelers();
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

	private void populateTravelerData() {
		List<Traveler> travelers = Db.getTravelers();
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

		List<Traveler> travelers = Db.getTravelers();
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		for (int i = 0; i < travelers.size(); i++) {
			final int travelerNum = i;
			SectionTravelerInfo traveler = (SectionTravelerInfo) inflater.inflate(
					R.layout.section_display_traveler_info_btn, null);
			traveler.setOnClickListener(new OnTravelerClickListener(travelerNum, false));
			mTravelerSections.add(traveler);
			mTravelerContainer.addView(traveler);
		}
	}

	private class OnTravelerClickListener implements OnClickListener {
		int mTravelerIndex = 0;
		boolean mAttemptDiskLoad = false;

		public OnTravelerClickListener(int travelerIndex, boolean attemptDiskLoad) {
			if (travelerIndex >= 0) {
				mTravelerIndex = travelerIndex;
			}
			mAttemptDiskLoad = attemptDiskLoad;
		}

		@Override
		public void onClick(View v) {
			Intent editTravelerIntent = new Intent(getActivity(),
					FlightTravelerInfoOptionsActivity.class);

			//We tell the traveler edit activity which index we are editing.
			editTravelerIntent.putExtra(Codes.TRAVELER_INDEX, mTravelerIndex);

			//We setup the checkout manager to have the correct working traveler
			if (Db.getTravelers().size() > mTravelerIndex && Db.getTravelers().get(mTravelerIndex) != null) {
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(mTravelerIndex));
			}
			else {
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(new Traveler());
			}
			if (mAttemptDiskLoad) {
				Db.getWorkingTravelerManager().setAttemptToLoadFromDisk(mAttemptDiskLoad);
			}
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
				TravelerFlowState state = TravelerFlowState.getInstance(getActivity());
				if (state == null) {
					return false;
				}
				List<Traveler> travelers = Db.getTravelers();
				for (int i = 0; i < travelers.size(); i++) {
					if (Db.getFlightSearch().getSelectedFlightTrip().isInternational()) {
						travelerValid &= (state.allTravelerInfoIsValidForInternationalFlight(travelers.get(i)));
					}
					else {
						travelerValid &= (state.allTravelerInfoIsValidForDomesticFlight(travelers.get(i)));
					}
				}
			}
			return travelerValid;
		}
	}

	OnClickListener gotoPaymentOptions = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Db.getWorkingBillingInfoManager().setWorkingBillingInfoAndBase(mBillingInfo);
			Intent editPaymentIntent = new Intent(getActivity(), FlightPaymentOptionsActivity.class);
			startActivity(editPaymentIntent);
		}
	};

	public void updateViewVisibilities() {

		PaymentFlowState state = PaymentFlowState.getInstance(getActivity());
		if (state == null) {
			//This is a rare case that happens when the fragment is attached and then detached quickly
			return;
		}

		boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : state.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : state.hasValidCardInfo(mBillingInfo);
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
			mListener.checkoutInformationIsValid();
		}
		else {
			mListener.checkoutInformationIsNotValid();
		}

		if (User.isLoggedIn(getActivity())) {
			mAccountLabel.setVisibility(View.VISIBLE);
		}
		else {
			mAccountLabel.setVisibility(View.GONE);
		}
	}

	private void populateTravelerDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate traveler data
			if (Db.getTravelers() != null && Db.getTravelers().size() >= 1) {
				//If the first traveler is not already all the way filled out, and the default profile for the expedia account has all required data, use the account profile
				boolean isInternational = Db.getFlightSearch().getSelectedFlightTrip().isInternational();
				TravelerFlowState state = TravelerFlowState.getInstance(getActivity());
				if (isInternational) {
					//International
					if (!state.allTravelerInfoIsValidForInternationalFlight(Db.getTravelers().get(0))) {
						if (state.allTravelerInfoIsValidForInternationalFlight(Db.getUser().getPrimaryTraveler())) {
							Db.getTravelers().set(0, Db.getUser().getPrimaryTraveler());
						}
					}
				}
				else {
					//Domestic
					if (!state.allTravelerInfoIsValidForDomesticFlight(Db.getTravelers().get(0))) {
						if (state.allTravelerInfoIsValidForDomesticFlight(Db.getUser().getPrimaryTraveler())) {
							Db.getTravelers().set(0, Db.getUser().getPrimaryTraveler());
						}
					}
				}
			}
		}
		else {
			for (int i = 0; i < Db.getTravelers().size(); i++) {
				//Travelers that have tuids are from the account and thus should be removed.
				if (Db.getTravelers().get(i).hasTuid()) {
					Db.getTravelers().set(i, new Traveler());
				}
				//We can't save travelers to an account if we aren't logged in, so we unset the flag
				Db.getTravelers().get(i).setSaveTravelerToExpediaAccount(false);
			}

		}
	}

	private boolean hasSomeManuallyEnteredData(BillingInfo info) {
		if (info == null) {
			return false;
		}

		if (info.getLocation() == null) {
			return false;
		}
		//Checkout the major fields, if any of them have data, then we know some data has been manually enetered
		if (!TextUtils.isEmpty(info.getLocation().getStreetAddressString())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getCity())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getPostalCode())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getLocation().getStateCode())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getNameOnCard())) {
			return true;
		}
		if (!TextUtils.isEmpty(info.getNumber())) {
			return true;
		}
		return false;
	}

	private void populatePaymentDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate Credit Card only if the user doesn't have any manually entered (or selected) data
			if (Db.getUser().getStoredCreditCards() != null && Db.getUser().getStoredCreditCards().size() > 0
					&& !hasSomeManuallyEnteredData(mBillingInfo) && mBillingInfo.getStoredCard() == null) {
				mBillingInfo.setStoredCard(Db.getUser().getStoredCreditCards().get(0));
			}
		}
		else {
			//Remove stored card(s)
			Db.getBillingInfo().setStoredCard(null);
			//Turn off the save to expedia account flag
			Db.getBillingInfo().setSaveCardToExpediaAccount(false);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// AccountButtonClickListener

	@Override
	public void accountLoginClicked() {
		SignInFragment.newInstance(true).show(getFragmentManager(), getString(R.string.tag_signin));

		OmnitureTracking.trackPageLoadFlightLogin(mContext);
	}

	@Override
	public void accountLogoutClicked() {
		ConfirmLogoutDialogFragment df = new ConfirmLogoutDialogFragment();
		df.setDoLogoutListener(this);
		df.show(this.getFragmentManager(), ConfirmLogoutDialogFragment.TAG);
	}

	@Override
	public void doLogout() {
		// Stop refreshing user (if we're currently doing so)
		BackgroundDownloader.getInstance().cancelDownload(KEY_REFRESH_USER);
		mRefreshedUser = false;

		if (!TextUtils.isEmpty(Db.getBillingInfo().getEmail())
				&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
			if (Db.getBillingInfo().getEmail().trim()
					.compareToIgnoreCase(Db.getUser().getPrimaryTraveler().getEmail().trim()) == 0) {
				//We were pulling email from the logged in user, so now we want to remove it.
				Db.getBillingInfo().setEmail("");
			}
		}

		// Sign out user
		User.signOut(getActivity());

		// Update UI
		mAccountButton.bind(false, false, null, true);

		//After logout this will clear stored cards
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		bindAll();
		updateViewVisibilities();
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), true);
		mRefreshedUser = true;

		Db.getBillingInfo().setEmail(Db.getUser().getPrimaryTraveler().getEmail());

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
			//Why flights AND hotels? Because the api will return blank for loyaltyMembershipNumber on flights
			return services.signIn(ExpediaServices.F_FLIGHTS | ExpediaServices.F_HOTELS);
		}
	};

	private final OnDownloadComplete<SignInResponse> mRefreshUserCallback = new OnDownloadComplete<SignInResponse>() {
		@Override
		public void onDownload(SignInResponse results) {
			if (results == null || results.hasErrors()) {
				//The refresh failed, so we just log them out. They can always try to login again.
				doLogout();
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

	///////////////////////////////////
	// Interfaces
	//////////////////////////////////

	public interface CheckoutInformationListener {
		public void checkoutInformationIsValid();

		public void checkoutInformationIsNotValid();
	}

}
