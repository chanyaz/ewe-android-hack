package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.FlightRulesActivity;
import com.expedia.bookings.activity.HotelRulesActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.enums.CheckoutFormState;
import com.expedia.bookings.fragment.CheckoutLoginButtonsFragment.IWalletButtonStateChangedListener;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.fragment.base.TabletCheckoutDataFormFragment;
import com.expedia.bookings.interfaces.IAcceptingListenersListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.model.TravelerFlowStateTablet;
import com.expedia.bookings.utils.BookingInfoUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.utils.TravelerUtils;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.SizeCopyView;
import com.mobiata.android.util.Ui;

@SuppressWarnings("ResourceType")
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutFormsFragment extends LobableFragment implements IBackManageable,
	IStateProvider<CheckoutFormState>,
	ICheckoutDataListener,
	IFragmentAvailabilityProvider,
	CheckoutLoginButtonsFragment.ILoginStateChangedListener,
	IWalletButtonStateChangedListener,
	TabletCheckoutDataFormFragment.ICheckoutDataFormListener,
	TravelerButtonFragment.ITravelerIsValidProvider {

	public interface ISlideToPurchaseSizeProvider {
		public View getSlideToPurchaseContainer();
	}

	private static final String STATE_CHECKOUTFORMSTATE = "STATE_CHECKOUTFORMSTATE";

	private static final String FRAG_TAG_TRAVELER_FORM = "FRAG_TAG_TRAVELER_FORM";
	private static final String FRAG_TAG_PAYMENT_FORM = "FRAG_TAG_PAYMENT_FORM";
	private static final String FRAG_TAG_LOGIN_BUTTONS = "FRAG_TAG_LOGIN_BUTTONS";
	private static final String FRAG_TAG_PAYMENT_BUTTON = "FRAG_TAG_PAYMENT_BUTTON";
	private static final String FRAG_TAG_COUPON_CONTAINER = "FRAG_TAG_COUPON_CONTAINER";

	private static final String FRAG_TAG_TRAV_BTN_BASE = "FRAG_TAG_TRAV_BTN_BASE_";//We generate tags based on this

	//These act as dummy view ids (or the basis there of) that help us dynamically create veiws we can bind to
	private static final int TRAV_BTN_ID_START = 1000000;
	private static final int LOGIN_FRAG_CONTAINER_ID = 2000000;
	private static final int PAYMENT_FRAG_CONTAINER_ID = 2000001;
	private static final int COUPON_FRAG_CONTAINER_ID = 2000002;

	private ViewGroup mRootC;
	private LinearLayout mCheckoutRowsC;
	private ViewGroup mOverlayC;
	private ViewGroup mOverlayContentC;
	private ViewGroup mTravelerFormC;
	private ViewGroup mPaymentFormC;
	private FrameLayoutTouchController mOverlayShade;
	private View mPaymentView;

	private int mShowingViewIndex = -1;

	private CheckoutInformationListener mCheckoutInfoListener;

	private CheckoutLoginButtonsFragment mLoginButtons;
	private PaymentButtonFragment mPaymentButton;
	private TabletCheckoutTravelerFormFragment mTravelerForm;
	private TabletCheckoutPaymentFormFragment mPaymentForm;
	private CheckoutCouponFragment mCouponContainer;
	private SizeCopyView mSizeCopyView;
	private TravelerFlowStateTablet mTravelerFlowState;

	private ISlideToPurchaseSizeProvider mISlideToPurchaseSizeProvider;

	private ArrayList<View> mTravelerViews = new ArrayList<View>();
	private ArrayList<TravelerButtonFragment> mTravelerButtonFrags = new ArrayList<TravelerButtonFragment>();

	private StateManager<CheckoutFormState> mStateManager = new StateManager<CheckoutFormState>(
		CheckoutFormState.OVERVIEW, this);

	public static TabletCheckoutFormsFragment newInstance() {
		TabletCheckoutFormsFragment frag = new TabletCheckoutFormsFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCheckoutInfoListener = Ui.findFragmentListener(this, CheckoutInformationListener.class);
		mISlideToPurchaseSizeProvider = Ui.findFragmentListener(this, ISlideToPurchaseSizeProvider.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_checkout_forms, container, false);
		mCheckoutRowsC = Ui.findView(mRootC, R.id.checkout_forms_container);
		mOverlayC = Ui.findView(mRootC, R.id.overlay_container);
		mOverlayContentC = Ui.findView(mRootC, R.id.overlay_content_container);
		mOverlayShade = Ui.findView(mRootC, R.id.overlay_shade);
		mTravelerFormC = Ui.findView(mRootC, R.id.traveler_form_container);
		mPaymentFormC = Ui.findView(mRootC, R.id.payment_form_container);
		mSizeCopyView = Ui.findView(mRootC, R.id.slide_container_size_copy_view);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CHECKOUTFORMSTATE)) {
			String stateName = savedInstanceState.getString(STATE_CHECKOUTFORMSTATE);
			CheckoutFormState state = com.expedia.bookings.enums.CheckoutFormState.valueOf(stateName);
			mStateManager.setDefaultState(state);
		}

		if (getLob() != null) {
			buildCheckoutForm();
		}

		registerStateListener(mStateHelper, false);
		registerStateListener(new StateListenerLogger<CheckoutFormState>(), false);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();

		mBackManager.registerWithParent(this);

		onCheckoutDataUpdated();

		setState(mStateManager.getState(), false);

		mSizeCopyView.mimicViewSize(mISlideToPurchaseSizeProvider.getSlideToPurchaseContainer(), true, false, true);

		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, true);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		IAcceptingListenersListener readyForListeners = Ui
			.findFragmentListener(this, IAcceptingListenersListener.class, false);
		if (readyForListeners != null) {
			readyForListeners.acceptingListenersUpdated(this, false);
		}

		if (Db.getTravelersAreDirty()) {
			Db.kickOffBackgroundTravelerSave(getActivity());
		}

		if (Db.getBillingInfoIsDirty()) {
			Db.kickOffBackgroundBillingInfoSave(getActivity());
		}

		mBackManager.unregisterWithParent(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_CHECKOUTFORMSTATE, mStateManager.getState().name());
	}

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		if (tag == FRAG_TAG_TRAVELER_FORM) {
			return mTravelerForm;
		}
		else if (tag == FRAG_TAG_PAYMENT_FORM) {
			return mPaymentForm;
		}
		else if (tag == FRAG_TAG_LOGIN_BUTTONS) {
			return mLoginButtons;
		}
		else if (tag == FRAG_TAG_PAYMENT_BUTTON) {
			return mPaymentButton;
		}
		else if (tag == FRAG_TAG_COUPON_CONTAINER) {
			return mCouponContainer;
		}
		return null;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		if (tag == FRAG_TAG_TRAVELER_FORM) {
			return TabletCheckoutTravelerFormFragment.newInstance(getLob());
		}
		else if (tag == FRAG_TAG_PAYMENT_FORM) {
			return TabletCheckoutPaymentFormFragment.newInstance(getLob());
		}
		else if (tag == FRAG_TAG_LOGIN_BUTTONS) {
			return CheckoutLoginButtonsFragment.newInstance(getLob());
		}
		else if (tag == FRAG_TAG_PAYMENT_BUTTON) {
			return PaymentButtonFragment.newInstance(getLob());
		}
		else if (tag == FRAG_TAG_COUPON_CONTAINER) {
			return CheckoutCouponFragment.newInstance(getLob());
		}
		return null;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		//None of our frags require setup...
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
			buildCheckoutForm();
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
			if (Db.getFlightSearch().getSelectedFlightTrip() != null) {
				FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
				String cityName = StrUtils.getWaypointCityOrCode(trip.getLeg(0).getLastWaypoint());
				headingArg = getString(R.string.flights_to_TEMPLATE, cityName);
			}
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			if (Db.getHotelSearch() != null
				&& Db.getHotelSearch().getSelectedProperty() != null
				&& Db.getHotelSearch().getSelectedProperty().getName() != null) {
				headingArg = Db.getHotelSearch().getSelectedProperty().getName();
			}
		}
		addGroupHeading(Html.fromHtml(getString(R.string.now_booking_TEMPLATE, headingArg)));

		// LOGIN CONTAINER
		FrameLayout frame = new FrameLayout(getActivity());
		frame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		frame.setId(LOGIN_FRAG_CONTAINER_ID);
		add(frame);


		//TRAVELER CONTAINERS
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
				openPaymentForm();
			}
		});

		// COUPON CONTAINER
		if (getLob() == LineOfBusiness.HOTELS) {
			FrameLayout couponFrame = new FrameLayout(getActivity());
			couponFrame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			couponFrame.setId(COUPON_FRAG_CONTAINER_ID);
			add(couponFrame);
		}

		// LEGAL BLURB
		TextView legalBlurb = (TextView) addActionable(com.expedia.bookings.R.layout.include_tablet_legal_blurb_tv,
			new Runnable() {
				@Override
				public void run() {
					Intent intent = new Intent(getActivity(),
						getLob() == LineOfBusiness.FLIGHTS ? FlightRulesActivity.class : HotelRulesActivity.class);
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
		transaction.commit();

		bindAll();

	}

	protected View addGroupHeading(int resId) {
		CharSequence seq = getString(resId);
		return addGroupHeading(seq);
	}

	protected View addGroupHeading(CharSequence headingText) {
		TextView tv = Ui.inflate(R.layout.checkout_form_tablet_heading, mCheckoutRowsC, false);
		tv.setText(Html.fromHtml(headingText.toString()));
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
			List<Traveler> travelers = Db.getTravelers();
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
				openTravelerEntry(travelerNumber);
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
		btnFrag.enableShowValidMarker(true);
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

	protected void openTravelerEntry(int travelerNumber) {
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
			mShowingViewIndex = viewNumber;
			mTravelerForm.setHeaderText(getTravelerBoxLabelForIndex(travelerNumber));
			mTravelerForm.bindToDb(travelerNumber);
			setState(CheckoutFormState.EDIT_TRAVELER, true);
		}
	}

	private void setEntryFormShowingPercentage(float percentage, int viewIndex) {
		//TODO: Much of this stuff could be cached, which would speed up animation performance
		if (viewIndex < 0 || mCheckoutRowsC == null || mCheckoutRowsC.getChildCount() <= viewIndex) {
			return;
		}

		//Find bottom of the overlay
		int overlayBottom = mOverlayContentC.getBottom();
		for (int i = 0; i < mOverlayContentC.getChildCount(); i++) {
			View child = mOverlayContentC.getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				overlayBottom = child.getBottom();
				break;
			}
		}

		//Slide views (only if they are already measured etc.)
		View selectedView = mCheckoutRowsC.getChildAt(viewIndex);
		if (selectedView.getHeight() > 0) {
			float aboveViewsTransY = percentage * selectedView.getTop();
			float activeViewTransY = percentage
				* (selectedView.getTop() / 2f - selectedView.getHeight() / 2f);
			float belowViewsTransY = percentage * (overlayBottom - selectedView.getBottom());
			for (int i = 0; i < viewIndex; i++) {
				mCheckoutRowsC.getChildAt(i).setTranslationY(-aboveViewsTransY);
			}
			selectedView.setTranslationY(-activeViewTransY);
			selectedView.setAlpha(1f - percentage);
			selectedView.setPivotY(selectedView.getHeight());
			selectedView.setScaleY(1f + (percentage / 2f) * (mOverlayContentC.getHeight() / selectedView.getHeight()));
			for (int i = viewIndex + 1; i < mCheckoutRowsC.getChildCount(); i++) {
				mCheckoutRowsC.getChildAt(i).setTranslationY(belowViewsTransY);
			}
			//Form cross fade/scale
			mOverlayContentC.setAlpha(percentage);
			mOverlayShade.setAlpha(percentage);
			float minScaleY = selectedView.getHeight() / mOverlayContentC.getHeight();
			float scaleYPercentage = minScaleY + percentage * (1f - minScaleY);
			mOverlayContentC.setPivotY(selectedView.getBottom());
			mOverlayContentC.setScaleY(scaleYPercentage);
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

	private void setValidationViewVisibility(View view, int validationViewId, boolean valid) {
		View validationView = Ui.findView(view, validationViewId);
		if (validationView != null) {
			validationView.setVisibility(valid ? View.VISIBLE : View.GONE);
		}
	}

	/*
	 * PAYMENT FORM STUFF
	 */

	protected void openPaymentForm() {
		int viewNumber = getPaymentViewIndex();
		if (viewNumber >= 0) {
			mShowingViewIndex = viewNumber;
			mPaymentForm.bindToDb();
			setState(CheckoutFormState.EDIT_PAYMENT, true);
		}
	}

	protected boolean validatePaymentInfo() {
		return mPaymentButton != null && mPaymentButton.validate();
	}

	private int getPaymentViewIndex() {
		int viewNumber = -1;
		for (int i = 0; i < mCheckoutRowsC.getChildCount(); i++) {
			if (mCheckoutRowsC.getChildAt(i) == mPaymentView) {
				viewNumber = i;
				break;
			}
		}
		return viewNumber;
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

	private StateListenerHelper<CheckoutFormState> mStateHelper = new StateListenerHelper<CheckoutFormState>() {

		@Override
		public void onStateTransitionStart(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
			if (stateTwo == CheckoutFormState.OVERVIEW) {
				mOverlayC.setVisibility(View.VISIBLE);
			}
			else {
				mOverlayContentC.setAlpha(0f);
				mOverlayShade.setAlpha(0f);
				mOverlayC.setVisibility(View.VISIBLE);
				if (stateTwo == CheckoutFormState.EDIT_PAYMENT) {
					mPaymentFormC.setVisibility(View.VISIBLE);
				}
				else if (stateTwo == CheckoutFormState.EDIT_TRAVELER) {
					mTravelerFormC.setVisibility(View.VISIBLE);
				}
			}
		}

		@Override
		public void onStateTransitionUpdate(CheckoutFormState stateOne, CheckoutFormState stateTwo, float percentage) {
			if (stateTwo != CheckoutFormState.OVERVIEW) {
				setEntryFormShowingPercentage(percentage, mShowingViewIndex);
			}
			else {
				setEntryFormShowingPercentage(1f - percentage, mShowingViewIndex);
			}
		}

		@Override
		public void onStateTransitionEnd(CheckoutFormState stateOne, CheckoutFormState stateTwo) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStateFinalized(CheckoutFormState state) {
			if (state == CheckoutFormState.OVERVIEW) {
				mOverlayC.setVisibility(View.INVISIBLE);
				mPaymentFormC.setVisibility(View.INVISIBLE);
				mTravelerFormC.setVisibility(View.INVISIBLE);
				mOverlayShade.setBlockNewEventsEnabled(false);

				setEntryFormShowingPercentage(0f, mShowingViewIndex);
				mShowingViewIndex = -1;

				bindAll();
			}
			else {
				mOverlayC.setVisibility(View.VISIBLE);
				if (state == CheckoutFormState.EDIT_PAYMENT) {
					mPaymentFormC.setVisibility(View.VISIBLE);
					mTravelerFormC.setVisibility(View.INVISIBLE);
					int viewNumber = getPaymentViewIndex();
					if (viewNumber >= 0) {
						mShowingViewIndex = viewNumber;
					}
					mPaymentForm.onFormOpened();
				}
				else if (state == CheckoutFormState.EDIT_TRAVELER) {
					mTravelerFormC.setVisibility(View.VISIBLE);
					mPaymentFormC.setVisibility(View.INVISIBLE);
					mTravelerForm.onFormOpened();
				}
				mOverlayShade.setBlockNewEventsEnabled(true);
				setEntryFormShowingPercentage(1f, mShowingViewIndex);
			}
		}
	};

	/*
	 * ISTATEPROVIDER
	 */
	private StateListenerCollection<CheckoutFormState> mStateListeners = new StateListenerCollection<CheckoutFormState>(
		mStateManager.getState());

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

	@Override
	public void onLoginStateChanged() {
		mTravelerForm.onLoginStateChanged();
		mPaymentForm.onLoginStateChanged();

		// This calls bindAll() and changes the state if needed
		onCheckoutDataUpdated();
	}

	/*
	 * IWalletButtonStateChangedListener
	 */

	@Override
	public void onWalletButtonStateChanged(boolean enable) {
		Log.d("TabletCheckoutFormsFrag", "onWalletButtonStateChanged(" + enable + ")");
		onCheckoutDataUpdated();
		mPaymentButton.setEnabled(!enable);
		for (TravelerButtonFragment tbf : mTravelerButtonFrags) {
			tbf.setEnabled(enable);
		}
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
	 * ITravelerIsValidProvider
	 */

	@Override
	public boolean travelerIsValid(int travelerNumber) {
		if (travelerNumber >= 0 && Db.getTravelers() != null && travelerNumber < Db.getTravelers().size()
			&& mTravelerFlowState != null) {
			boolean needsEmail = TravelerUtils.travelerFormRequiresEmail(travelerNumber, getLob(), getActivity());
			boolean needsPassport = TravelerUtils.travelerFormRequiresPassport(getLob());
			return mTravelerFlowState.isValid(Db.getTravelers().get(travelerNumber), needsEmail, needsPassport);
		}
		return false;
	}
}
