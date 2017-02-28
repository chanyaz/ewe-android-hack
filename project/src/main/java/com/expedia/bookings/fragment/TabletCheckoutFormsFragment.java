package com.expedia.bookings.fragment;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightAndPackagesRulesActivity;
import com.expedia.bookings.activity.HotelRulesActivity;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearchParams;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Location;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.StoredCreditCard;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.User;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.CheckoutFormState;
import com.expedia.bookings.enums.CheckoutState;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.CheckoutInformationListener;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.model.TravelerFlowStateTablet;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.FlightUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.HotelUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.TravelerUtils;
import com.expedia.bookings.widget.SizeCopyView;
import com.expedia.bookings.widget.TabletCheckoutScrollView;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.mobiata.android.util.Ui;
import com.squareup.otto.Subscribe;
import com.squareup.phrase.Phrase;

@SuppressWarnings("ResourceType")
public class TabletCheckoutFormsFragment extends LobableFragment implements IBackManageable,
	IStateProvider<CheckoutFormState>,
	ICheckoutDataListener,
	IFragmentAvailabilityProvider,
	TabletCheckoutDataFormFragment.ICheckoutDataFormListener,
	TravelerButtonFragment.ITravelerButtonListener,
	PaymentButtonFragment.IPaymentButtonListener {

	public interface ISlideToPurchaseSizeProvider {
		View getSlideToPurchaseContainer();
	}

	private static final String STATE_CHECKOUTFORMSTATE = "STATE_CHECKOUTFORMSTATE";

	private static final String FRAG_TAG_TRAVELER_FORM = "FRAG_TAG_TRAVELER_FORM";
	private static final String FRAG_TAG_PAYMENT_FORM = "FRAG_TAG_PAYMENT_FORM";
	private static final String FRAG_TAG_LOGIN_BUTTONS = "FRAG_TAG_LOGIN_BUTTONS";
	private static final String FRAG_TAG_PAYMENT_BUTTON = "FRAG_TAG_PAYMENT_BUTTON";
	private static final String FRAG_TAG_COUPON_CONTAINER = "FRAG_TAG_COUPON_CONTAINER";
	private static final String FRAG_TAG_HORIZONTAL_ITEM_HOTEL = "FRAG_TAG_HORIZONTAL_ITEM_HOTEL";
	private static final String FRAG_TAG_HORIZONTAL_ITEM_FLIGHT = "FRAG_TAG_HORIZONTAL_ITEM_FLIGHT";

	//We generate tags based on this
	private static final String FRAG_TAG_TRAV_BTN_BASE = "FRAG_TAG_TRAV_BTN_BASE_";

	//These act as dummy view ids (or the basis there of) that help us dynamically create veiws we can bind to
	private static final int TRAV_BTN_ID_START = 1000000;
	private static final int LOGIN_FRAG_CONTAINER_ID = 2000000;
	private static final int PAYMENT_FRAG_CONTAINER_ID = 2000001;
	private static final int COUPON_FRAG_CONTAINER_ID = 2000002;

	private ViewGroup mRootC;
	private FrameLayout mHorizontalTripItemContainer;
	private LinearLayout mCheckoutRowsC;
	private ViewGroup mTravelerFormC;
	private ViewGroup mPaymentFormC;
	private View mPaymentView;
	private TouchableFrameLayout mTouchBlocker;
	private TabletCheckoutScrollView mScrollC;

	private CheckoutInformationListener mCheckoutInfoListener;

	private CheckoutLoginButtonsFragment mLoginButtons;
	private PaymentButtonFragment mPaymentButton;
	private TabletCheckoutTravelerFormFragment mTravelerForm;
	private TabletCheckoutPaymentFormFragment mPaymentForm;
	private CheckoutCouponFragment mCouponContainer;
	private SizeCopyView mSizeCopyView;
	private TravelerFlowStateTablet mTravelerFlowState;
	private TextView mResortFeeText;
	private TextView mCardFeeLegalText;
	private TextView mDepositPolicyTxt;
	private View mSplitTicketRulesView;
	private TextView mSplitTicketFeeLinks;

	private TripBucketHorizontalHotelFragment mHorizontalHotelFrag;
	private TripBucketHorizontalFlightFragment mHorizontalFlightFrag;

	private ISlideToPurchaseSizeProvider mISlideToPurchaseSizeProvider;

	private ArrayList<View> mTravelerViews = new ArrayList<>();
	private ArrayList<TravelerButtonFragment> mTravelerButtonFrags = new ArrayList<>();
	private boolean mIsLandscape;

	private SingleStateListener<CheckoutFormState> mPaymentOpenCloseListener;
	private SingleStateListener<CheckoutFormState> mTravelerOpenCloseListener;

	private StateManager<CheckoutFormState> mStateManager = new StateManager<>(CheckoutFormState.OVERVIEW, this);

	public static TabletCheckoutFormsFragment newInstance() {
		return new TabletCheckoutFormsFragment();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		mCheckoutInfoListener = Ui.findFragmentListener(this, CheckoutInformationListener.class);
		mISlideToPurchaseSizeProvider = Ui.findFragmentListener(this, ISlideToPurchaseSizeProvider.class);

		mIsLandscape = context.getResources().getBoolean(R.bool.landscape);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_checkout_forms, container, false);
		mHorizontalTripItemContainer = Ui.findView(mRootC, R.id.horizontal_trip_bucket_item);
		mCheckoutRowsC = Ui.findView(mRootC, R.id.checkout_forms_container);
		mTravelerFormC = Ui.findView(mRootC, R.id.traveler_form_container);
		mPaymentFormC = Ui.findView(mRootC, R.id.payment_form_container);
		mSizeCopyView = Ui.findView(mRootC, R.id.slide_container_size_copy_view);
		mTouchBlocker = Ui.findView(mRootC, R.id.forms_touch_blocker);
		mScrollC = Ui.findView(mRootC, R.id.checkout_scroll);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CHECKOUTFORMSTATE)) {
			String stateName = savedInstanceState.getString(STATE_CHECKOUTFORMSTATE);
			CheckoutFormState state = com.expedia.bookings.enums.CheckoutFormState.valueOf(stateName);
			mStateManager.setDefaultState(state);
		}

		if (getLob() != null) {
			buildCheckoutForm();
			BookingInfoUtils.populateTravelerDataFromUser(getActivity(), getLob());
			BookingInfoUtils.populateTravelerDataFromUser(getActivity(), getLob());
		}

		registerStateListener(new StateListenerLogger<CheckoutFormState>(), false);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);

		mBackManager.registerWithParent(this);

		onCheckoutDataUpdated();

		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, true);
		}

		if (mPaymentOpenCloseListener == null) {
			mPaymentOpenCloseListener = new SingleStateListener(CheckoutFormState.OVERVIEW,
				CheckoutFormState.EDIT_PAYMENT, true, new OpenCloseListener(mPaymentFormC, mPaymentForm));
		}
		if (mTravelerOpenCloseListener == null) {
			mTravelerOpenCloseListener = new SingleStateListener(CheckoutFormState.OVERVIEW,
				CheckoutFormState.EDIT_TRAVELER, true, new OpenCloseListener(mTravelerFormC, mTravelerForm));
		}

		registerStateListener(mPaymentOpenCloseListener, false);
		registerStateListener(mTravelerOpenCloseListener, false);

		setState(mStateManager.getState(), false);

		mSizeCopyView.mimicViewSize(mISlideToPurchaseSizeProvider.getSlideToPurchaseContainer(), true, false, true);
	}

	@Override
	public void onPause() {
		Events.unregister(this);
		super.onPause();

		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, false);
		}

		unRegisterStateListener(mPaymentOpenCloseListener);
		unRegisterStateListener(mTravelerOpenCloseListener);
		mBackManager.unregisterWithParent(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_CHECKOUTFORMSTATE, mStateManager.getState().name());
	}

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		switch (tag) {

		case FRAG_TAG_TRAVELER_FORM:
			return mTravelerForm;

		case FRAG_TAG_PAYMENT_FORM:
			return mPaymentForm;

		case FRAG_TAG_LOGIN_BUTTONS:
			return mLoginButtons;

		case FRAG_TAG_PAYMENT_BUTTON:
			return mPaymentButton;

		case FRAG_TAG_COUPON_CONTAINER:
			return mCouponContainer;

		case FRAG_TAG_HORIZONTAL_ITEM_HOTEL:
			return mHorizontalHotelFrag;

		case FRAG_TAG_HORIZONTAL_ITEM_FLIGHT:
			return mHorizontalFlightFrag;

		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		switch (tag) {

		case FRAG_TAG_TRAVELER_FORM:
			return TabletCheckoutTravelerFormFragment.newInstance(getLob());

		case FRAG_TAG_PAYMENT_FORM:
			return TabletCheckoutPaymentFormFragment.newInstance(getLob());

		case FRAG_TAG_LOGIN_BUTTONS:
			return CheckoutLoginButtonsFragment.newInstance(getLob());

		case FRAG_TAG_PAYMENT_BUTTON:
			return PaymentButtonFragment.newInstance(getLob());

		case FRAG_TAG_COUPON_CONTAINER:
			return CheckoutCouponFragment.newInstance(getLob());

		case FRAG_TAG_HORIZONTAL_ITEM_HOTEL:
			return TripBucketHorizontalHotelFragment.newInstance();

		case FRAG_TAG_HORIZONTAL_ITEM_FLIGHT:
			return TripBucketHorizontalFlightFragment.newInstance();

		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (FRAG_TAG_HORIZONTAL_ITEM_HOTEL.equals(tag)) {
			TripBucketHorizontalHotelFragment f = (TripBucketHorizontalHotelFragment) frag;
			f.setState(TripBucketItemState.EXPANDED);
		}
		else if (FRAG_TAG_HORIZONTAL_ITEM_FLIGHT.equals(tag)) {
			TripBucketHorizontalFlightFragment f = (TripBucketHorizontalFlightFragment) frag;
			f.setState(TripBucketItemState.EXPANDED);
		}
		//None of the other frags require setup...
	}

	/*
	 * BINDING
	 */

	public void bindAll() {
		if (mLoginButtons != null) {
			mLoginButtons.bind();
		}
		if (mPaymentButton != null) {
			mPaymentButton.bindToDb();
		}
		bindTravelers();

		LineOfBusiness lob = getLob();
		if (lob == LineOfBusiness.HOTELS && mHorizontalHotelFrag != null) {
			if (Db.getTripBucket().getHotel().hasPriceChanged()) {
				mHorizontalHotelFrag.bind();
				mHorizontalHotelFrag.setState(TripBucketItemState.SHOWING_PRICE_CHANGE, false);
			}
			else {
				mHorizontalHotelFrag.setState(TripBucketItemState.EXPANDED, false);
			}
		}

		if (lob == LineOfBusiness.FLIGHTS && mHorizontalFlightFrag != null) {
			if (Db.getTripBucket().getFlight().hasPriceChanged()) {
				mHorizontalFlightFrag.bind();
				mHorizontalFlightFrag.setState(TripBucketItemState.SHOWING_PRICE_CHANGE, false);
			}
			else {
				mHorizontalFlightFrag.setState(TripBucketItemState.EXPANDED, false);
			}
		}
	}

	/*
	 * VALIDATION
	 */

	public boolean hasValidCheckoutInfo() {
		return validatePaymentInfo() && validateTravelers();
	}

	/*
	 * ICheckoutDataListener
	 */

	@Override
	public void onCheckoutDataUpdated() {
		bindAll();
		mCheckoutInfoListener.onBillingInfoChange();

		if (hasValidCheckoutInfo()) {
			mCheckoutInfoListener.checkoutInformationIsValid();
		}
		else {
			mCheckoutInfoListener.checkoutInformationIsNotValid();
		}
	}

	/*
	 * GETTERS / SETTERS
	 */

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mRootC != null) {
			// Let's not build the checkout form when trip is sold out.
			if (lob == LineOfBusiness.FLIGHTS && Db.getTripBucket().getFlight().canBePurchased()) {
				buildCheckoutForm();
			}
			else if (lob == LineOfBusiness.HOTELS && Db.getTripBucket().getHotel().canBePurchased()) {
				buildCheckoutForm();
			}
		}
	}

	public void setState(CheckoutFormState state, boolean animate) {
		mStateManager.setState(state, animate);
	}

	/*
	 * CHECKOUT FORM BUILDING METHODS
	 */

	protected void clearCheckoutForm() {
		//REMOVE ALL THE FRAGMENTS
		FragmentManager fragmentManager = getChildFragmentManager();
		FragmentTransaction removeFragsTransaction = fragmentManager.beginTransaction();
		mTravelerForm = FragmentAvailabilityUtils
			.setFragmentAvailability(false, FRAG_TAG_TRAVELER_FORM, fragmentManager, removeFragsTransaction, this,
				R.id.traveler_form_container, false);
		mPaymentForm = FragmentAvailabilityUtils
			.setFragmentAvailability(false, FRAG_TAG_PAYMENT_FORM, fragmentManager, removeFragsTransaction, this,
				R.id.payment_form_container, false);
		mLoginButtons = FragmentAvailabilityUtils
			.setFragmentAvailability(false, FRAG_TAG_LOGIN_BUTTONS, fragmentManager, removeFragsTransaction, this,
				LOGIN_FRAG_CONTAINER_ID, false);
		mPaymentButton = FragmentAvailabilityUtils
			.setFragmentAvailability(false, FRAG_TAG_PAYMENT_BUTTON, fragmentManager, removeFragsTransaction, this,
				PAYMENT_FRAG_CONTAINER_ID, false);
		mCouponContainer = FragmentAvailabilityUtils
			.setFragmentAvailability(false, FRAG_TAG_COUPON_CONTAINER, fragmentManager, removeFragsTransaction, this,
				COUPON_FRAG_CONTAINER_ID, false);
		mHorizontalHotelFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(false, FRAG_TAG_HORIZONTAL_ITEM_HOTEL, fragmentManager, removeFragsTransaction,
				this,
				R.id.horizontal_trip_bucket_item, false);
		mHorizontalFlightFrag = FragmentAvailabilityUtils
			.setFragmentAvailability(false, FRAG_TAG_HORIZONTAL_ITEM_FLIGHT, fragmentManager, removeFragsTransaction,
				this,
				R.id.horizontal_trip_bucket_item, false);

		for (TravelerButtonFragment btnFrag : mTravelerButtonFrags) {
			removeFragsTransaction.remove(btnFrag);
		}

		removeFragsTransaction.commit();
		fragmentManager.executePendingTransactions();

		//REMOVE OLD REFS
		mTravelerButtonFrags.clear();
		mTravelerViews.clear();
		mTravelerBoxLabels = null;

		//CLEAR ALL VIEWS
		mCheckoutRowsC.removeAllViews();
	}

	protected void buildCheckoutForm() {
		FragmentManager fragmentManager = getChildFragmentManager();

		// CLEAN UP
		clearCheckoutForm();


		//Traveler validation
		mTravelerFlowState = new TravelerFlowStateTablet(getActivity(), getLob());

		// HEADING
		String headingArg = "";
		if (getLob() == LineOfBusiness.FLIGHTS) {
			if (Db.getTripBucket().getFlight().getFlightTrip() != null) {
				FlightTrip trip = Db.getTripBucket().getFlight().getFlightTrip();
				String cityName = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());
				headingArg = getString(R.string.flights_to_TEMPLATE, cityName);
			}
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			if (Db.getTripBucket().getHotel() != null
				&& Db.getTripBucket().getHotel().getProperty() != null
				&& Db.getTripBucket().getHotel().getProperty().getName() != null) {
				headingArg = Db.getTripBucket().getHotel().getProperty().getName();
			}
		}
		if (getResources().getBoolean(R.bool.show_now_booking_heading)) {
			addGroupHeading(HtmlCompat.fromHtml(getString(R.string.now_booking_TEMPLATE, headingArg)));
		}

		// LOGIN CONTAINER
		FrameLayout frame = new FrameLayout(getActivity());
		frame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		frame.setId(LOGIN_FRAG_CONTAINER_ID);
		add(frame);


		// TRAVELER CONTAINERS
		BookingInfoUtils.populateTravelerData(getLob());
		addGroupHeading(R.string.travelers);
		for (int i = 0; i < Db.getTravelers().size(); i++) {
			addTravelerView(i);
		}

		// PAYMENT CONTAINER
		addGroupHeading(R.string.payment);
		mPaymentView = new FrameLayout(getActivity());
		mPaymentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mPaymentView.setId(PAYMENT_FRAG_CONTAINER_ID);
		// Let's set the padding to payment view here fragment_checkout_payment_button.xml So we can add the LCC message view inside the fragment.
		dressCheckoutView(mPaymentView, false);
		addActionable(mPaymentView, new Runnable() {
			@Override
			public void run() {
				onCreditCardEditButtonPressed();
			}
		});

		// COUPON CONTAINER
		if (getLob() == LineOfBusiness.HOTELS) {
			FrameLayout couponFrame = new FrameLayout(getActivity());
			couponFrame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			couponFrame.setId(COUPON_FRAG_CONTAINER_ID);
			add(couponFrame);
		}

		if (getLob() == LineOfBusiness.FLIGHTS && PointOfSale.getPointOfSale().shouldShowAirlinePaymentMethodFeeMessage()) {
			if (mCardFeeLegalText == null) {
				mCardFeeLegalText = Ui.inflate(R.layout.include_tablet_card_fee_tv, mCheckoutRowsC, false);
			}
			add(mCardFeeLegalText);
			setupCardFeeTextView();
		}

		if (getLob() == LineOfBusiness.FLIGHTS) {
			if (mSplitTicketRulesView == null) {
				mSplitTicketRulesView = Ui.inflate(R.layout.include_split_ticket_fee_rules_tv, mCheckoutRowsC, false);
				mSplitTicketFeeLinks = (TextView) mSplitTicketRulesView.findViewById(R.id.split_ticket_fee_rules_text);
			}
			add(mSplitTicketRulesView);
			updateSplitTicketRulesText();
		}

		// LEGAL BLURB
		TextView legalBlurb = (TextView) addActionable(com.expedia.bookings.R.layout.include_tablet_legal_blurb_tv,
			new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(getActivity(),
						getLob() == LineOfBusiness.FLIGHTS ? FlightAndPackagesRulesActivity.class : HotelRulesActivity.class);
					intent.putExtra("LOB", getLob());
					startActivity(intent);
				}
			}
		);
		if (getLob() == com.expedia.bookings.data.LineOfBusiness.FLIGHTS) {
			legalBlurb.setText(PointOfSale.getPointOfSale().getStylizedFlightBookingStatement(true));
		}
		else {
			legalBlurb.setText(PointOfSale.getPointOfSale().getStylizedHotelBookingStatement(true));
		}

		if (getLob() == LineOfBusiness.HOTELS) {
			if (mDepositPolicyTxt == null) {
				mDepositPolicyTxt = Ui.inflate(R.layout.include_tablet_resort_blurb_tv, mCheckoutRowsC, false);
			}
			add(mDepositPolicyTxt);
			updateDepositPolicyText();
		}

		if (getLob() == LineOfBusiness.HOTELS && PointOfSale.getPointOfSale().showFTCResortRegulations() &&
			Db.getTripBucket().getHotel().getRate().showResortFeesMessaging()) {
			if (mResortFeeText == null) {
				mResortFeeText = Ui.inflate(R.layout.include_tablet_resort_blurb_tv, mCheckoutRowsC, false);
			}
			add(mResortFeeText);
			updateResortFeeText();
		}

		//SET UP THE FORM FRAGMENTS
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		mTravelerForm = FragmentAvailabilityUtils
			.setFragmentAvailability(true, FRAG_TAG_TRAVELER_FORM, fragmentManager, transaction, this,
				R.id.traveler_form_container, true);
		mPaymentForm = FragmentAvailabilityUtils
			.setFragmentAvailability(true, FRAG_TAG_PAYMENT_FORM, fragmentManager, transaction, this,
				R.id.payment_form_container, true);
		mLoginButtons = FragmentAvailabilityUtils
			.setFragmentAvailability(true, FRAG_TAG_LOGIN_BUTTONS, fragmentManager, transaction, this,
				LOGIN_FRAG_CONTAINER_ID, true);
		mPaymentButton = FragmentAvailabilityUtils
			.setFragmentAvailability(true, FRAG_TAG_PAYMENT_BUTTON, fragmentManager, transaction, this,
				PAYMENT_FRAG_CONTAINER_ID, true);
		if (getLob() == LineOfBusiness.HOTELS) {
			mCouponContainer = FragmentAvailabilityUtils
				.setFragmentAvailability(true, FRAG_TAG_COUPON_CONTAINER, fragmentManager, transaction, this,
					COUPON_FRAG_CONTAINER_ID, true);
		}

		if (!mIsLandscape) {
			mHorizontalTripItemContainer.setVisibility(View.VISIBLE);
			final boolean lobIsHotels = getLob() == LineOfBusiness.HOTELS;
			final boolean lobIsFlights = getLob() == LineOfBusiness.FLIGHTS;

			if (lobIsHotels) {
				mHorizontalHotelFrag = FragmentAvailabilityUtils
					.setFragmentAvailability(true, FRAG_TAG_HORIZONTAL_ITEM_HOTEL, fragmentManager, transaction, this,
						R.id.horizontal_trip_bucket_item, true);
			}

			if (lobIsFlights) {
				mHorizontalFlightFrag = FragmentAvailabilityUtils
					.setFragmentAvailability(true, FRAG_TAG_HORIZONTAL_ITEM_FLIGHT, fragmentManager, transaction, this,
						R.id.horizontal_trip_bucket_item, true);
			}
		}
		transaction.commit();

		bindAll();
	}

	private void updateDepositPolicyText() {
		if (mDepositPolicyTxt != null) {
			Rate rate = Db.getTripBucket().getHotel().getRate();
			String[] depositPolicy = rate.getDepositPolicy();
			if (depositPolicy != null) {
				mDepositPolicyTxt.setText(HotelUtils.getDepositPolicyText(getActivity(), depositPolicy));
				mDepositPolicyTxt.setVisibility(View.VISIBLE);
			}
			else {
				mDepositPolicyTxt.setVisibility(View.GONE);
			}
		}
	}

	private void updateResortFeeText() {
		if (mResortFeeText != null) {
			Spanned resortBlurb = HotelUtils
				.getCheckoutResortFeesText(getActivity(), Db.getTripBucket().getHotel().getRate());
			mResortFeeText.setText(resortBlurb);
		}
	}

	private void setupCardFeeTextView() {
		if (mCardFeeLegalText != null) {
			Spanned cardFeeLegalText = FlightUtils.getCardFeeLegalText(getContext(), R.color.tablet_legal_blurb_text_color);
			mCardFeeLegalText.setText(cardFeeLegalText);
			mCardFeeLegalText.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(getActivity());
					builder.setUrl(
						PointOfSale.getPointOfSale().getAirlineFeeBasedOnPaymentMethodTermsAndConditionsURL());
					builder.setTheme(R.style.FlightTheme);
					builder.setTitle(R.string.Airline_fee);
					builder.setInjectExpediaCookies(true);
					startActivity(builder.getIntent());
				}
			});
		}
	}

	private void updateSplitTicketRulesText() {
		if (mSplitTicketRulesView != null) {
			if (Db.getTripBucket().getFlight().getFlightTrip().isSplitTicket()) {
				mSplitTicketRulesView.setVisibility(View.VISIBLE);
				FlightTrip flightTrip = Db.getTripBucket().getFlight().getFlightTrip();
				String baggageFeesUrlLegOne = flightTrip.getLeg(0).getBaggageFeesUrl();
				String baggageFeesUrlLegTwo = flightTrip.getLeg(1).getBaggageFeesUrl();

				String baggageFeesTextFormatted = Phrase.from(getContext(), R.string.split_ticket_baggage_fees_TEMPLATE)
					.put("departurelink", baggageFeesUrlLegOne)
					.put("returnlink", baggageFeesUrlLegTwo)
					.format().toString();
				SpannableStringBuilder spannableStringBuilder =
					StrUtils.getSpannableTextByColor(baggageFeesTextFormatted, ContextCompat.getColor(getContext(),R.color.tablet_legal_blurb_text_color), true);
				mSplitTicketFeeLinks.setText(spannableStringBuilder);
				mSplitTicketFeeLinks.setMovementMethod(LinkMovementMethod.getInstance());
			}
			else {
				mSplitTicketRulesView.setVisibility(View.GONE);
			}
		}
	}

	protected View addGroupHeading(int resId) {
		CharSequence seq = getString(resId);
		return addGroupHeading(seq);
	}

	protected View addGroupHeading(CharSequence headingText) {
		TextView tv = Ui.inflate(R.layout.checkout_form_tablet_heading, mCheckoutRowsC, false);
		tv.setText(HtmlCompat.fromHtml(headingText.toString()));
		return add(tv);
	}

	protected View addActionable(int resId, final Runnable action) {
		View view = Ui.inflate(resId, mCheckoutRowsC, false);
		return addActionable(view, action);
	}

	protected View addActionable(View view, final Runnable action) {
		if (action != null) {
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					view.post(action);
				}
			});
		}
		return add(view);
	}

	public View add(View view) {
		mCheckoutRowsC.addView(view);
		return view;
	}

	private void dressCheckoutView(View dressableView, boolean addPadding) {
		dressableView.setBackgroundResource(R.drawable.bg_checkout_information_single);
		if (addPadding) {
			int padding = getResources().getDimensionPixelSize(R.dimen.traveler_button_padding);
			dressableView.setPadding(padding, padding, padding, padding);
		}
	}

	/*
	 * TRAVELER FORM STUFF
	 */

	private ArrayList<String> mTravelerBoxLabels;

	private String getTravelerBoxLabelForIndex(int index) {
		if (mTravelerBoxLabels == null) {
			//Collections.sort(travelers);
			mTravelerBoxLabels = TravelerUtils.generateTravelerBoxLabels(getActivity(), Db.getTravelers());
		}
		return mTravelerBoxLabels.get(index);
	}

	protected void addTravelerView(final int travelerNumber) {

		// Add the container to the layout (and make it actionable)
		FrameLayout frame = new FrameLayout(getActivity());
		frame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		frame.setId(getTravelerButtonContainerId(travelerNumber));
		addActionable(frame, new Runnable() {
			@Override
			public void run() {
				onTravelerEditButtonPressed(travelerNumber);
			}
		});
		dressCheckoutView(frame, true);
		mTravelerViews.add(frame);

		//Add fragment to the new container
		FragmentManager manager = getChildFragmentManager();
		TravelerButtonFragment btnFrag = (TravelerButtonFragment) manager
			.findFragmentByTag(getTravelerButtonFragTag(travelerNumber));
		if (btnFrag == null) {
			btnFrag = TravelerButtonFragment.newInstance(getLob(), travelerNumber);
		}
		btnFrag.setEmptyViewLabel(getTravelerBoxLabelForIndex(travelerNumber));
		if (!btnFrag.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(getTravelerButtonContainerId(travelerNumber), btnFrag,
				getTravelerButtonFragTag(travelerNumber));
			transaction.commit();
		}
		if (!mTravelerButtonFrags.contains(btnFrag)) {
			mTravelerButtonFrags.add(btnFrag);
		}
	}

	private String getTravelerButtonFragTag(int travNumber) {
		return FRAG_TAG_TRAV_BTN_BASE + travNumber;
	}

	private int getTravelerButtonContainerId(int travNumber) {
		return TRAV_BTN_ID_START + travNumber;
	}

	public void openTravelerEntry(int travelerNumber) {
		//finding index
		View travSection = mTravelerViews.get(travelerNumber);
		int viewNumber = -1;
		for (int i = 0; i < mCheckoutRowsC.getChildCount(); i++) {
			if (mCheckoutRowsC.getChildAt(i) == travSection) {
				viewNumber = i;
				break;
			}
		}
		if (viewNumber >= 0) {
			mTravelerForm.setHeaderText(getTravelerBoxLabelForIndex(travelerNumber));
			mTravelerForm.bindToDb(travelerNumber);
			setState(CheckoutFormState.EDIT_TRAVELER, true);
		}
	}

	protected void bindTravelers() {
		for (TravelerButtonFragment btn : mTravelerButtonFrags) {
			if (btn.isAdded()) {
				btn.bindToDb();
			}
		}
	}

	protected boolean validateTravelers() {
		if (mTravelerButtonFrags.size() == 0) {
			return false;
		}

		for (TravelerButtonFragment btn : mTravelerButtonFrags) {
			if (!btn.isValid()) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (mLoginButtons != null) {
			mLoginButtons.onActivityResult(requestCode, resultCode, data);
		}
	}

	/*
	 * PAYMENT FORM STUFF
	 */

	protected void openPaymentForm() {
		for (int i = 0; i < mCheckoutRowsC.getChildCount(); i++) {
			if (mCheckoutRowsC.getChildAt(i) == mPaymentView) {
				mPaymentForm.bindToDb();
				setState(CheckoutFormState.EDIT_PAYMENT, true);
				return;
			}
		}
	}

	protected boolean validatePaymentInfo() {
		return mPaymentButton != null && mPaymentButton.validate();
	}

	/*
	 * BACKMANAGEABLE
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			if (mStateManager.getState() != CheckoutFormState.OVERVIEW) {
				if (mStateManager.getState() == CheckoutFormState.EDIT_TRAVELER) {
					mTravelerForm.onFormClosed();
				}
				else if (mStateManager.getState() == CheckoutFormState.EDIT_PAYMENT) {
					mPaymentForm.onFormClosed();
				}
				setState(CheckoutFormState.OVERVIEW, true);
				return true;
			}
			return false;
		}
	};

	/*
	 * ISTATELISTENER
	 */

	private class OpenCloseListener implements ISingleStateListener {
		private View mFormContainer;
		private TabletCheckoutDataFormFragment mFormFragment;
		private int mSlideHeight;

		public OpenCloseListener(View container, TabletCheckoutDataFormFragment fragment) {
			mFormContainer = container;
			mFormFragment = fragment;
		}

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mFormContainer.setVisibility(View.VISIBLE);
			mSlideHeight = getView().getHeight();
			setEntryFormShowingPercentage(isReversed ? 1f : 0f);
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			setEntryFormShowingPercentage(percentage);
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {
		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			mPaymentFormC.setVisibility(View.INVISIBLE);
			mTravelerFormC.setVisibility(View.INVISIBLE);
			setEntryFormShowingPercentage(isReversed ? 0f : 1f);

			// TouchableFrameLayout
			mTouchBlocker.setBlockNewEventsEnabled(!isReversed);

			if (isReversed) {
				mFormFragment.onFormClosed();
				bindAll();
			}
			else {
				mFormContainer.setVisibility(View.VISIBLE);
				mFormFragment.onFormOpened();
			}
		}

		private void setEntryFormShowingPercentage(float percentage) {
			mFormContainer.setTranslationY(mSlideHeight * (1f - percentage));
			mFormContainer.setAlpha(percentage);
			if (mHorizontalTripItemContainer != null) {
				mHorizontalTripItemContainer.setAlpha(1f - percentage);
			}
			mCheckoutRowsC.setAlpha(1f - percentage);
		}
	}

	// ScrollView helper

	public void setCheckoutStateForScrollView(CheckoutState state) {
		if (mScrollC != null) {
			mScrollC.setCheckoutState(state);
		}
	}

	/*
	 * ISTATEPROVIDER
	 */
	private StateListenerCollection<CheckoutFormState> mStateListeners = new StateListenerCollection<>();

	@Override
	public void startStateTransition(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(CheckoutFormState stateOne, CheckoutFormState stateTwo, float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(CheckoutFormState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<CheckoutFormState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<CheckoutFormState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}

	public void onLoginStateChanged() {
		mTravelerForm.onLoginStateChanged();
		mPaymentForm.onLoginStateChanged();

		if (!User.isLoggedIn(getActivity())) {
			mCheckoutInfoListener.onLogout();
		}

		// This calls bindAll() and changes the state if needed
		onCheckoutDataUpdated();
	}

	/*
	 * ICheckoutDataFormListener
	 */

	@Override
	public void onFormRequestingClosure(TabletCheckoutDataFormFragment caller, boolean animate) {
		if (caller == mPaymentForm && mStateManager.getState() == CheckoutFormState.EDIT_PAYMENT) {
			setState(CheckoutFormState.OVERVIEW, animate);
		}
		else if (caller == mTravelerForm && mStateManager.getState() == CheckoutFormState.EDIT_TRAVELER) {
			setState(CheckoutFormState.OVERVIEW, animate);
		}
	}

	/*
	 * ITravelerButtonListener
	 */

	@Override
	public void onTravelerEditButtonPressed(int travelerNumber) {
		OmnitureTracking.trackTabletEditTravelerPageLoad(getLob());
		openTravelerEntry(travelerNumber);
	}

	@Override
	public void onTravelerChosen() {
		onCheckoutDataUpdated();
	}

	@Override
	public void onAddNewTravelerSelected(int travelerNumber) {
		openTravelerEntry(travelerNumber);
		// Let's refresh the checkout status since we removed previously selected traveler info.
		onCheckoutDataUpdated();
	}

	@Override
	public boolean travelerIsValid(int travelerNumber) {
		if (travelerNumber >= 0 && Db.getTravelers() != null && travelerNumber < Db.getTravelers().size()
			&& mTravelerFlowState != null) {
			boolean needsEmail = TravelerUtils.travelerFormRequiresEmail(travelerNumber, getActivity());
			boolean needsPassport = TravelerUtils.travelerFormRequiresPassport(getLob());
			Traveler traveler = Db.getTravelers().get(travelerNumber);

			if (getLob() == LineOfBusiness.FLIGHTS) {
				FlightSearchParams params = Db.getTripBucket().getFlight().getFlightSearchParams();
				return mTravelerFlowState.isValid(traveler, params, needsEmail, needsPassport, travelerNumber);
			}
			else {
				return mTravelerFlowState.isValid(traveler, needsEmail, needsPassport, travelerNumber);
			}
		}
		return false;
	}

	/*
	 * IPaymentButtonListener
	 */

	@Override
	public void onCreditCardEditButtonPressed() {
		openPaymentFormWithTracking();
	}

	@Override
	public void onAddNewCreditCardSelected() {
		// Let's reset the selectable/clickable state (in the stored card picker, checkout overview screen) of the currentCC
		StoredCreditCard currentCC = Db.getBillingInfo().getStoredCard();
		if (currentCC != null) {
			BookingInfoUtils.resetPreviousCreditCardSelectState(getActivity(), currentCC);
		}
		Db.getWorkingBillingInfoManager().shiftWorkingBillingInfo(new BillingInfo());
		Db.getWorkingBillingInfoManager().getWorkingBillingInfo().setLocation(new Location());
		Db.getWorkingBillingInfoManager().commitWorkingBillingInfoToDB();
		// Let's refresh the checkout status since we are replacing existing card (if any).
		onCheckoutDataUpdated();
		openPaymentFormWithTracking();
	}

	private void openPaymentFormWithTracking() {
		OmnitureTracking.trackTabletEditPaymentPageLoad(getLob());
		openPaymentForm();
	}

	@Override
	public void onStoredCreditCardChosen() {
		onCheckoutDataUpdated();
	}

	/*
	 * Otto events
	 */

	@Subscribe
	public void onLCCPaymentFeesAdded(Events.LCCPaymentFeesAdded event) {
		if (mHorizontalFlightFrag != null) {
			mHorizontalFlightFrag.refreshExpandedTripPrice();
		}
	}

	@Subscribe
	public void onHotelProductRateUp(Events.HotelProductRateUp event) {
		if (mHorizontalHotelFrag != null) {
			mHorizontalHotelFrag.refreshRate();
		}
		updateResortFeeText();
	}

	@Subscribe
	public void onFlightPriceChange(Events.FlightPriceChange event) {
		if (mHorizontalFlightFrag != null) {
			mHorizontalFlightFrag.refreshTripOnPriceChanged();
		}
	}

	@Subscribe
	public void onCouponApplySuccess(Events.CouponApplyDownloadSuccess event) {
		if (mHorizontalHotelFrag != null) {
			mHorizontalHotelFrag.refreshRate();
		}
		updateResortFeeText();
	}

	@Subscribe
	public void onCouponRemoveSuccess(Events.CouponRemoveDownloadSuccess event) {
		if (mHorizontalHotelFrag != null) {
			mHorizontalHotelFrag.refreshRate();
		}
		updateResortFeeText();
	}

	@Subscribe
	public void onCreateTripSuccess(Events.CreateTripDownloadSuccess event) {
		// In the case of hotels with resort fees, the data received from /create
		// is different enough to warrant a complete re-bind. This has to do with
		// rate types, etc.
		if (mHorizontalHotelFrag != null && event.createTripResponse instanceof CreateTripResponse) {
			mHorizontalHotelFrag.refreshRate();
		}
		updateResortFeeText();
		updateDepositPolicyText();
	}
}
