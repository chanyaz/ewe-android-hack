package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightPaymentOptionsActivity;
import com.expedia.bookings.activity.FlightRulesActivity;
import com.expedia.bookings.activity.FlightTravelerInfoOptionsActivity;
import com.expedia.bookings.activity.LoginActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
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
import com.expedia.bookings.widget.UserToTripAssocLoginExtender;
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
	private ViewGroup mPaymentButton;

	private boolean mRefreshedUser;

	private CheckoutInformationListener mListener;

	public static FlightCheckoutFragment newInstance() {
		return new FlightCheckoutFragment();
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
		Db.loadBillingInfo(getActivity());
		mBillingInfo = Db.getBillingInfo();

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mPaymentButton = Ui.findView(v, R.id.payment_info_btn);
		mStoredCreditCard = Ui.findView(v, R.id.stored_creditcard_section_button);
		mCreditCardSectionButton = Ui.findView(v, R.id.creditcard_section_button);
		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mAccountLabel = Ui.findView(v, R.id.expedia_account_label);
		mTravelerContainer = Ui.findView(v, R.id.traveler_container);

		ViewUtils.setAllCaps(mAccountLabel);
		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.checkout_information_label));

		// Detect user state, update account button accordingly
		mAccountButton.setListener(this);

		// rules and restrictions link stuff
		TextView tv = Ui.findView(v, R.id.legal_blurb);
		tv.setText(PointOfSale.getPointOfSale().getStylizedFlightBookingStatement());

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

		buildTravelerBox();

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

		loadUser();

		//Set values
		populateTravelerData();
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		buildTravelerBox();

		bindAll();
		refreshAccountButtonState();
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

	private void refreshAccountButtonState() {
		if (User.isLoggedIn(getActivity())) {
			if (Db.getUser() == null) {
				Db.loadUser(getActivity());
			}

			if (Db.getUser() != null && Db.getUser().getPrimaryTraveler() != null
					&& !TextUtils.isEmpty(Db.getUser().getPrimaryTraveler().getEmail())) {
				//We have a user (either from memory, or loaded from disk)
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
				//We thought the user was logged in, but the user appears to not contain the data we need, get rid of the user
				User.signOut(getActivity());
				mAccountButton.bind(false, false, null, true);
			}
		}
		else {
			mAccountButton.bind(false, false, null, true);
		}
	}

	private void populateTravelerData() {
		List<Traveler> travelers = Db.getTravelers();
		if (travelers == null) {
			travelers = new ArrayList<Traveler>();
			Db.setTravelers(travelers);
		}

		buildTravelerBase();
	}

	private void buildTravelerBase() {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (Db.getTravelers() == null) {
			Db.setTravelers(new ArrayList<Traveler>());
		}

		// If there are more numAdults from SearchParams, add empty Travelers to the Db to anticipate the addition of
		// new Travelers in order for check out
		final int numTravelers = Db.getTravelers().size();
		final int numAdults = Db.getFlightSearch().getSearchParams().getNumAdults();
		if (numTravelers < numAdults) {
			for (int i = numTravelers; i < numAdults; i++) {
				Db.getTravelers().add(new Traveler());

				// Add the traveler sections
				SectionTravelerInfo travelerSection = (SectionTravelerInfo) inflater.inflate(
						R.layout.section_display_traveler_info_btn, null);
				//travelerSection.setOnClickListener(new OnTravelerClickListener(i, false));
				dressSectionTraveler(travelerSection, i);
				mTravelerSections.add(travelerSection);
			}
		}

		// If there are more Travelers than number of adults required by the SearchParams, remove the extra Travelers,
		// although, keep the first numAdults Travelers.
		else if (numTravelers > numAdults) {
			for (int i = numTravelers - 1; i >= numAdults; i--) {
				Db.getTravelers().remove(i);
			}
		}
	}

	private void buildTravelerBox() {
		mTravelerContainer.removeAllViews();
		mTravelerSections.clear();

		final int numAdults = Db.getFlightSearch().getSearchParams().getNumAdults();
		for (int i = 0; i < numAdults; i++) {
			constructIndividualTravelerBox(i, numAdults);
		}
	}

	private void constructIndividualTravelerBox(int index, int numAdults) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Traveler traveler = getTraveler(index);

		// The traveler has information, fill it in
		SectionTravelerInfo travelerSection = (SectionTravelerInfo) inflater.inflate(
				R.layout.section_display_traveler_info_btn, null);

		dressSectionTraveler(travelerSection, index);
		mTravelerSections.add(travelerSection);

		if (traveler != null && traveler.hasName()) {
			mTravelerContainer.addView(travelerSection);
		}

		// This traveler is likely blank, show the empty label, prompt user to fill in
		else {
			View v = inflater.inflate(R.layout.snippet_booking_overview_traveler, null);
			dressSectionTraveler(v, index);

			TextView tv = Ui.findView(v, R.id.traveler_empty_text_view);

			if (numAdults == 1) {
				tv.setText(mContext.getString(R.string.add_traveler));
			}
			else {
				tv.setText(mContext.getString(R.string.add_traveler_number_TEMPLATE, index + 1)); // no zero index for users
			}

			mTravelerContainer.addView(v);
		}

	}

	private void dressSectionTraveler(View travelerSection, int travelerIndex) {
		if (travelerIndex == 0) {
			travelerSection.setBackgroundResource(R.drawable.bg_checkout_information_top_tab);
		}
		else {
			travelerSection.setBackgroundResource(R.drawable.bg_checkout_information_middle_tab);
		}
		int padding = getResources().getDimensionPixelSize(R.dimen.traveler_button_padding);
		travelerSection.setPadding(padding, padding, padding, padding);
		travelerSection.setOnClickListener(new OnTravelerClickListener(travelerIndex, false));
	}

	private Traveler getTraveler(int index) {
		if (Db.getTravelers() == null) {
			return null;
		}

		if (Db.getTravelers().size() <= index) {
			return null;
		}

		return Db.getTravelers().get(index);
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

			// We tell the traveler edit activity which index we are editing.
			editTravelerIntent.putExtra(Codes.TRAVELER_INDEX, mTravelerIndex);

			// We setup the checkout manager to have the correct working traveler
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

	private boolean validateTravelers() {
		if (mTravelerSections == null || mTravelerSections.size() <= 0) {
			return false;
		}
		else {
			boolean allTravelersValid = true;
			if (Db.getTravelers() == null || Db.getTravelers().size() <= 0) {
				allTravelersValid = false;
			}
			else {
				TravelerFlowState state = TravelerFlowState.getInstance(getActivity());
				if (state == null) {
					return false;
				}
				List<Traveler> travelers = Db.getTravelers();

				for (int i = 0; i < travelers.size(); i++) {
					SectionTravelerInfo travSection = mTravelerSections.get(i);
					boolean currentTravelerValid = false;
					if (Db.getFlightSearch().getSelectedFlightTrip().isInternational()) {
						currentTravelerValid = (state.allTravelerInfoIsValidForInternationalFlight(travelers.get(i)));
					}
					else {
						currentTravelerValid = (state.allTravelerInfoIsValidForDomesticFlight(travelers.get(i)));
					}
					setValidationViewVisibility(travSection, R.id.validation_checkmark, currentTravelerValid);
					allTravelersValid &= currentTravelerValid;
				}
			}
			return allTravelersValid;
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

		if (mBillingInfo == null) {
			//We haven't been properly initialized yet...
			return;
		}

		boolean hasStoredCard = mBillingInfo.getStoredCard() != null;
		boolean paymentAddressValid = hasStoredCard ? hasStoredCard : state.hasValidBillingAddress(mBillingInfo);
		boolean paymentCCValid = hasStoredCard ? hasStoredCard : state.hasValidCardInfo(mBillingInfo);
		boolean travelerValid = validateTravelers();

		if (hasStoredCard) {
			mStoredCreditCard.setVisibility(View.VISIBLE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.GONE);
			setValidationViewVisibility(mStoredCreditCard, R.id.validation_checkmark, true);
		}
		else if (paymentAddressValid && paymentCCValid) {
			mStoredCreditCard.setVisibility(View.GONE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.VISIBLE);
			setValidationViewVisibility(mCreditCardSectionButton, R.id.validation_checkmark, true);
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

	private void setValidationViewVisibility(View view, int validationViewId, boolean valid) {
		View validationView = Ui.findView(view, validationViewId);
		if (validationView != null) {
			validationView.setVisibility(valid ? View.VISIBLE : View.GONE);
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

	private void loadUser() {
		if (Db.getUser() == null) {
			if (User.isLoggedIn(getActivity())) {
				Db.loadUser(getActivity());
			}
		}
	}

	private void populatePaymentDataFromUser() {
		if (User.isLoggedIn(getActivity())) {
			//Populate Credit Card only if the user doesn't have any manually entered (or selected) data
			if (Db.getUser().getStoredCreditCards() != null && Db.getUser().getStoredCreditCards().size() == 1
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
		String tripId = Db.getItinerary(Db.getFlightSearch().getSelectedFlightTrip().getItineraryNumber()).getTripId();
		Intent loginIntent = LoginActivity.createIntent(getActivity(), LineOfBusiness.FLIGHTS,
				new UserToTripAssocLoginExtender(tripId));
		startActivity(loginIntent);

		OmnitureTracking.trackPageLoadFlightLogin(mContext);
	}

	@Override
	public void accountLogoutClicked() {
		ConfirmLogoutDialogFragment df = new ConfirmLogoutDialogFragment();
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

		mTravelerSections.clear();

		// Remove all Expedia account Travelers and keep the manually entered, not saved Travelers
		Iterator<Traveler> i = Db.getTravelers().iterator();
		Traveler traveler;
		while (i.hasNext()) {
			traveler = i.next();
			if (traveler.hasTuid()) {
				i.remove();
			}
		}
		buildTravelerBase();

		// Update UI
		mAccountButton.bind(false, false, null, true);

		//After logout this will clear stored cards
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		buildTravelerBox();
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
		buildTravelerBox();
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
