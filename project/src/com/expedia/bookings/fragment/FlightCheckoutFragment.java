package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
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
import com.expedia.bookings.data.CheckoutDataLoader;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.CreditCardType;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.SignInResponse;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.model.FlightPaymentFlowState;
import com.expedia.bookings.model.TravelerFlowState;
import com.expedia.bookings.section.SectionBillingInfo;
import com.expedia.bookings.section.SectionLocation;
import com.expedia.bookings.section.SectionStoredCreditCard;
import com.expedia.bookings.section.SectionTravelerInfo;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.utils.WalletUtils;
import com.expedia.bookings.widget.AccountButton;
import com.expedia.bookings.widget.AccountButton.AccountButtonClickListener;
import com.expedia.bookings.widget.UserToTripAssocLoginExtender;
import com.expedia.bookings.widget.WalletButton;
import com.google.android.gms.wallet.MaskedWallet;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.ViewUtils;

public class FlightCheckoutFragment extends LoadWalletFragment implements AccountButtonClickListener,
		ConfirmLogoutDialogFragment.DoLogoutListener {

	private static final String INSTANCE_REFRESHED_USER_TIME = "INSTANCE_REFRESHED_USER";
	private static final String KEY_REFRESH_USER = "KEY_REFRESH_USER";

	private BillingInfo mBillingInfo;

	private ArrayList<SectionTravelerInfo> mTravelerSections = new ArrayList<SectionTravelerInfo>();
	private List<View> mAddTravelerSections = new ArrayList<View>();

	private TextView mAccountLabel;
	private AccountButton mAccountButton;
	private WalletButton mWalletButton;
	private SectionBillingInfo mCreditCardSectionButton;
	private SectionLocation mSectionLocation;
	private SectionStoredCreditCard mStoredCreditCard;

	private ViewGroup mTravelerContainer;
	private ViewGroup mPaymentButton;
	private ViewGroup mPaymentOuterContainer;
	private TextView mCardFeeTextView;
	private View mLccTriangle;

	//When we last refreshed user data.
	private long mRefreshedUserTime = 0L;

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

		// #1363: Disable Google Wallet if not a valid payment type
		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();

		if (!trip.isCardTypeSupported(CreditCardType.GOOGLE_WALLET)) {
			disableGoogleWallet();
		}

		if (savedInstanceState != null) {
			mRefreshedUserTime = savedInstanceState.getLong(INSTANCE_REFRESHED_USER_TIME);
		}
		else {
			// Reset Google Wallet state each time we get here
			Db.clearGoogleWallet();
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
			mRefreshedUserTime = savedInstanceState.getLong(INSTANCE_REFRESHED_USER_TIME);
		}

		//The parent activity uses CheckoutDataLoader to load billingInfo, we wait for it to finish.
		if (CheckoutDataLoader.getInstance().isLoading()) {
			CheckoutDataLoader.getInstance().waitForCurrentThreadToFinish();
		}
		mBillingInfo = Db.getBillingInfo();

		if (mBillingInfo.getLocation() == null) {
			mBillingInfo.setLocation(new Location());
		}

		mPaymentButton = Ui.findView(v, R.id.payment_info_btn);
		mPaymentOuterContainer = Ui.findView(v, R.id.payment_outer_container);
		mStoredCreditCard = Ui.findView(v, R.id.stored_creditcard_section_button);
		mCreditCardSectionButton = Ui.findView(v, R.id.creditcard_section_button);
		mSectionLocation = Ui.findView(v, R.id.section_location_address);
		mAccountButton = Ui.findView(v, R.id.account_button_root);
		mAccountLabel = Ui.findView(v, R.id.expedia_account_label);
		mWalletButton = Ui.findView(v, R.id.wallet_button_layout);
		mTravelerContainer = Ui.findView(v, R.id.traveler_container);
		mCardFeeTextView = Ui.findView(v, R.id.lcc_card_fee_warning);
		mLccTriangle = Ui.findView(v, R.id.lcc_triangle);

		ViewUtils.setAllCaps(mAccountLabel);
		ViewUtils.setAllCaps((TextView) Ui.findView(v, R.id.checkout_information_label));

		if (!PointOfSale.getPointOfSale().requiresBillingAddressFlights()) {
			mSectionLocation.setVisibility(View.GONE);
		}

		// Detect user state, update account button accordingly
		mAccountButton.setListener(this);

		mWalletButton.setOnClickListener(mWalletButtonClickListener);
		mWalletButton.setPromoVisible(false);

		// rules and restrictions link stuff
		TextView tv = Ui.findView(v, R.id.legal_blurb);
		tv.setText(PointOfSale.getPointOfSale().getStylizedFlightBookingStatement());

		tv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), FlightRulesActivity.class);
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

		outState.putLong(INSTANCE_REFRESHED_USER_TIME, mRefreshedUserTime);
	}

	@Override
	public void onDetach() {
		super.onDetach();

		mListener = null; // Just in case Wallet is leaking
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

		// We can have more travelers than sections (if we have empty traveler sections)
		// Bind to sections as we have them available
		for (int a = 0; a < mTravelerSections.size(); a++) {
			mTravelerSections.get(a).bind(travelers.get(a));
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
				int userRefreshInterval = getResources().getInteger(R.integer.account_sync_interval);
				if (mRefreshedUserTime + userRefreshInterval < System.currentTimeMillis()) {
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

		// If there are more numAdults from HotelSearchParams, add empty Travelers to the Db to anticipate the addition of
		// new Travelers in order for check out
		final int numTravelers = travelers.size();
		final int numAdults = Db.getFlightSearch().getSearchParams().getNumAdults();
		if (numTravelers < numAdults) {
			for (int i = numTravelers; i < numAdults; i++) {
				travelers.add(new Traveler());
			}
		}

		// If there are more Travelers than number of adults required by the HotelSearchParams, remove the extra Travelers,
		// although, keep the first numAdults Travelers.
		else if (numTravelers > numAdults) {
			for (int i = numTravelers - 1; i >= numAdults; i--) {
				travelers.remove(i);
			}
		}
	}

	private void buildTravelerBox() {
		mTravelerContainer.removeAllViews();
		mTravelerSections.clear();
		mAddTravelerSections.clear();

		LayoutInflater inflater = LayoutInflater.from(getActivity());
		final int numAdults = Db.getFlightSearch().getSearchParams().getNumAdults();
		List<Traveler> travelers = Db.getTravelers();

		// Not sure if this state could happen, but there was a LOT of defensive code
		// I've removed, so I might as well keep everything from breaking.
		if (travelers == null || numAdults != travelers.size()) {
			populateTravelerData();
		}

		TravelerFlowState state = TravelerFlowState.getInstance(getActivity());
		boolean isInternational = Db.getFlightSearch().getSelectedFlightTrip().isInternational();
		for (int index = 0; index < numAdults; index++) {
			Traveler traveler = travelers.get(index);

			if (traveler != null && state.allTravelerInfoValid(traveler, isInternational)) {
				// The traveler has information, fill it in
				SectionTravelerInfo travelerSection = (SectionTravelerInfo) inflater.inflate(
						R.layout.section_display_traveler_info_btn, null);

				dressSectionTraveler(travelerSection, index);
				mTravelerSections.add(travelerSection);

				mTravelerContainer.addView(travelerSection);
			}

			// This traveler is likely blank, show the empty label, prompt user to fill in
			else {
				View v = inflater.inflate(R.layout.snippet_booking_overview_traveler, null);
				dressSectionTraveler(v, index);

				TextView tv = Ui.findView(v, R.id.traveler_empty_text_view);

				if (numAdults == 1) {
					tv.setText(R.string.add_traveler);
				}
				else {
					tv.setText(getString(R.string.add_traveler_number_TEMPLATE, index + 1)); // no zero index for users
				}

				mAddTravelerSections.add(v);
				mTravelerContainer.addView(v);
			}
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
		travelerSection.setOnClickListener(new OnTravelerClickListener(travelerIndex));
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

			// We tell the traveler edit activity which index we are editing.
			editTravelerIntent.putExtra(Codes.TRAVELER_INDEX, mTravelerIndex);

			// We setup the checkout manager to have the correct working traveler
			if (Db.getTravelers().size() > mTravelerIndex && Db.getTravelers().get(mTravelerIndex) != null) {
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(Db.getTravelers().get(mTravelerIndex));
			}
			else {
				Db.getWorkingTravelerManager().setWorkingTravelerAndBase(new Traveler());
			}

			Db.getWorkingTravelerManager().setAttemptToLoadFromDisk(false);

			startActivity(editTravelerIntent);
		}
	}

	private boolean validateTravelers() {
		if (mTravelerSections == null || mTravelerSections.size() == 0) {
			return false;
		}
		else {
			boolean allTravelersValid = true;
			if (Db.getTravelers() == null || Db.getTravelers().size() != mTravelerSections.size()) {
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
		FlightPaymentFlowState state = FlightPaymentFlowState.getInstance(getActivity());
		if (state == null) {
			//This is a rare case that happens when the fragment is attached and then detached quickly
			return;
		}

		if (mBillingInfo == null) {
			//We haven't been properly initialized yet...
			return;
		}

		boolean hasValidCard = state.hasAValidCardSelected(mBillingInfo);
		boolean travelerValid = validateTravelers();

		if (mBillingInfo.hasStoredCard()) {
			mStoredCreditCard.setVisibility(View.VISIBLE);
			mPaymentButton.setVisibility(View.GONE);
			mCreditCardSectionButton.setVisibility(View.GONE);
			setValidationViewVisibility(mStoredCreditCard, R.id.validation_checkmark, true);
		}
		else if (state.hasAValidCardSelected(mBillingInfo)) {
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

		if (hasValidCard && travelerValid) {
			mListener.checkoutInformationIsValid();
		}
		else {
			mListener.checkoutInformationIsNotValid();
		}

		Money cardFee = Db.getFlightSearch().getSelectedFlightTrip().getCardFee(mBillingInfo);
		if (hasValidCard && cardFee != null) {
			setPaymentContainerBg(R.drawable.bg_lcc_checkout_information_bottom_tab, false);

			mCardFeeTextView.setText(Html.fromHtml(getString(R.string.airline_card_fee_TEMPLATE,
					cardFee.getFormattedMoney())));
			mCardFeeTextView.setVisibility(View.VISIBLE);
			mLccTriangle.setVisibility(View.VISIBLE);
		}
		else {
			setPaymentContainerBg(R.drawable.bg_checkout_information_bottom_tab, true);
			mCardFeeTextView.setVisibility(View.GONE);
			mLccTriangle.setVisibility(View.GONE);
		}

		if (User.isLoggedIn(getActivity())) {
			mAccountLabel.setVisibility(View.VISIBLE);
		}
		else {
			mAccountLabel.setVisibility(View.GONE);
		}

		updateWalletViewVisibilities();
	}

	/**
	 * Sets the background resource and also makes sure to (re)-set the padding (otherwise it gets blown away)
	 * @param bgResId drawable resource id to set the background of the payment container
	 */
	private void setPaymentContainerBg(int bgResId, boolean padBottom) {
		mPaymentOuterContainer.setBackgroundResource(bgResId);
		int pad = (int) getResources().getDimension(R.dimen.flight_payment_container_padding);
		int bottomPad = padBottom ? pad : 0;
		mPaymentOuterContainer.setPadding(pad, pad, pad, bottomPad);
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
			BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), Db.getUser().getPrimaryTraveler(),
					LineOfBusiness.FLIGHTS);
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
			// Populate Credit Card only if the user doesn't have any manually entered (or selected) data
			if (Db.getUser().getStoredCreditCards() != null && Db.getUser().getStoredCreditCards().size() == 1
					&& !hasSomeManuallyEnteredData(mBillingInfo) && !mBillingInfo.hasStoredCard()) {
				StoredCreditCard scc = Db.getUser().getStoredCreditCards().get(0);
				// Make sure the card is supported by this flight trip before automatically selecting it
				if (Db.getFlightSearch().getSelectedFlightTrip().isCardTypeSupported(scc.getType())) {
					mBillingInfo.setStoredCard(scc);

					Db.getFlightSearch().getSelectedFlightTrip().setShowFareWithCardFee(true);
					mListener.onBillingInfoChange();
				}
			}
		}
		else if (Db.getMaskedWallet() == null) {
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
		Bundle args = LoginActivity.createArgumentsBundle(LineOfBusiness.FLIGHTS, new UserToTripAssocLoginExtender(
				tripId));
		User.signIn(getActivity(), args);

		OmnitureTracking.trackPageLoadFlightLogin(getActivity());
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
		mRefreshedUserTime = 0L;

		// Sign out user
		User.signOut(getActivity());

		// Remove all Expedia account Travelers and keep the manually entered, not saved Travelers
		Iterator<Traveler> i = Db.getTravelers().iterator();
		Traveler traveler;
		while (i.hasNext()) {
			traveler = i.next();
			if (traveler.hasTuid()) {
				i.remove();
			}
		}

		// Update UI
		mAccountButton.bind(false, false, null, true);

		//After logout this will clear stored cards
		populateTravelerData();
		populatePaymentDataFromUser();
		populateTravelerDataFromUser();
		buildTravelerBox();
		bindAll();
		updateViewVisibilities();

		// Update card fee logic
		if (Db.getBillingInfo().hasStoredCard()) {
			Db.getFlightSearch().getSelectedFlightTrip().setShowFareWithCardFee(false);
		}
		mListener.onBillingInfoChange();
	}

	public void onLoginCompleted() {
		mAccountButton.bind(false, true, Db.getUser(), true);
		mRefreshedUserTime = System.currentTimeMillis();

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

	//////////////////////////////////////////////////////////////////////////
	// LoadWalletFragment

	private OnClickListener mWalletButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			buyWithGoogleWallet();
		}
	};

	@Override
	protected Money getEstimatedTotal() {
		return Db.getFlightSearch().getSelectedFlightTrip().getTotalFare();
	}

	@Override
	protected int getMaskedWalletBuilderFlags() {
		return WalletUtils.F_PHONE_NUMBER_REQUIRED;
	}

	/**
	 * Binds the masked wallet to the billing info.  Warning: it WILL
	 * blow away whatever was here before - so only call this when
	 * we want to override the current data with Google Wallet!
	 */
	protected void onMaskedWalletFullyLoaded(boolean fromPreauth) {
		populateTravelerData();

		MaskedWallet maskedWallet = Db.getMaskedWallet();

		// If we don't currently have traveler data, and the wallet gives us sufficient data, use it
		// NOTE: At the moment we are *guaranteed* not to get sufficient data, but there's no reason
		// not to hope someday we will get it!
		Traveler traveler = WalletUtils.addWalletAsTraveler(getActivity(), maskedWallet);
		BookingInfoUtils.insertTravelerDataIfNotFilled(getActivity(), traveler, LineOfBusiness.FLIGHTS);

		// Bind credit card data, but only if they explicitly clicked "buy with wallet" or they have
		// no existing credit card info entered
		if (!fromPreauth || (TextUtils.isEmpty(mBillingInfo.getNumber()) && !mBillingInfo.hasStoredCard())) {
			WalletUtils.bindWalletToBillingInfo(maskedWallet, mBillingInfo);
		}

		bindAll();
		refreshAccountButtonState();
		updateViewVisibilities();
	}

	// We may want to update these more often than the rest of the Views
	protected void updateWalletViewVisibilities() {
		boolean showWalletButton = showWalletButton();
		boolean isWalletLoading = isWalletLoading();

		mWalletButton.setVisibility(showWalletButton ? View.VISIBLE : View.GONE);
		mWalletButton.setEnabled(!isWalletLoading);

		// Enable buttons if we're either not showing the wallet button or we're not loading a masked wallet
		boolean enableButtons = !showWalletButton || !isWalletLoading;
		mAccountButton.setEnabled(enableButtons);
		mPaymentButton.setEnabled(enableButtons);
		mStoredCreditCard.setEnabled(enableButtons);
		mCreditCardSectionButton.setEnabled(enableButtons);
		for (SectionTravelerInfo info : mTravelerSections) {
			info.setEnabled(enableButtons);
		}
		for (View v : mAddTravelerSections) {
			v.setEnabled(enableButtons);
		}
	}

	///////////////////////////////////
	// Interfaces
	//////////////////////////////////

	public interface CheckoutInformationListener {
		public void checkoutInformationIsValid();

		public void checkoutInformationIsNotValid();

		public void onBillingInfoChange();
	}
}
