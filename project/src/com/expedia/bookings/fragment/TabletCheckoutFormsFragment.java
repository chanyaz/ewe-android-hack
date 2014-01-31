package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.Traveler;
import com.expedia.bookings.enums.CheckoutFormState;
import com.expedia.bookings.fragment.FlightCheckoutFragment.CheckoutInformationListener;
import com.expedia.bookings.fragment.base.LobableFragment;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.ICheckoutDataListener;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.widget.FrameLayoutTouchController;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutFormsFragment extends LobableFragment implements IBackManageable,
	IStateProvider<CheckoutFormState>,
	ICheckoutDataListener {

	private static final String STATE_CHECKOUTFORMSTATE = "STATE_CHECKOUTFORMSTATE";

	private static final String FRAG_TAG_TRAVELER_FORM = "FRAG_TAG_TRAVELER_FORM";
	private static final String FRAG_TAG_PAYMENT_FORM = "FRAG_TAG_PAYMENT_FORM";
	private static final String FRAG_TAG_LOGIN_BUTTONS = "FRAG_TAG_LOGIN_BUTTONS";
	private static final String FRAG_TAG_PAYMENT_BUTTON = "FRAG_TAG_PAYMENT_BUTTON";

	private static final String FRAG_TAG_TRAV_BTN_BASE = "FRAG_TAG_TRAV_BTN_BASE_";//We generate tags based on this

	//These act as dummy view ids (or the basis there of) that help us dynamically create veiws we can bind to
	private static final int TRAV_BTN_ID_START = 1000000;
	private static final int LOGIN_FRAG_CONTAINER_ID = 2000000;
	private static final int PAYMENT_FRAG_CONTAINER_ID = 2000001;

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

		bindAll();

		setState(mStateManager.getState(), false);
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

		mBackManager.unregisterWithParent(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_CHECKOUTFORMSTATE, mStateManager.getState().name());
	}

	public void attachTravelerForm() {
		FragmentManager manager = getChildFragmentManager();
		if (mTravelerForm == null) {
			mTravelerForm = (TabletCheckoutTravelerFormFragment) manager.findFragmentByTag(FRAG_TAG_TRAVELER_FORM);
		}
		if (mTravelerForm == null) {
			mTravelerForm = TabletCheckoutTravelerFormFragment.newInstance(getLob());
		}
		if (!mTravelerForm.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.traveler_form_container, mTravelerForm, FRAG_TAG_TRAVELER_FORM);
			transaction.commit();
		}
	}

	public void attachPaymentForm() {
		FragmentManager manager = getChildFragmentManager();
		if (mPaymentForm == null) {
			mPaymentForm = (TabletCheckoutPaymentFormFragment) manager.findFragmentByTag(FRAG_TAG_PAYMENT_FORM);
		}
		if (mPaymentForm == null) {
			mPaymentForm = TabletCheckoutPaymentFormFragment.newInstance(getLob());
		}
		if (!mPaymentForm.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(R.id.payment_form_container, mPaymentForm, FRAG_TAG_PAYMENT_FORM);
			transaction.commit();
		}
	}

	public void attachLoginButtons() {
		FragmentManager manager = getChildFragmentManager();
		if (mLoginButtons == null) {
			mLoginButtons = (CheckoutLoginButtonsFragment) manager.findFragmentByTag(FRAG_TAG_LOGIN_BUTTONS);
		}
		if (mLoginButtons == null) {
			mLoginButtons = CheckoutLoginButtonsFragment.newInstance(getLob());
		}
		if (!mLoginButtons.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(LOGIN_FRAG_CONTAINER_ID, mLoginButtons, FRAG_TAG_LOGIN_BUTTONS);
			transaction.commit();
		}
	}

	public void attachPaymentButton() {
		FragmentManager manager = getChildFragmentManager();
		if (mPaymentButton == null) {
			mPaymentButton = (PaymentButtonFragment) manager.findFragmentByTag(FRAG_TAG_PAYMENT_BUTTON);
		}
		if (mPaymentButton == null) {
			mPaymentButton = PaymentButtonFragment.newInstance(getLob());
		}
		if (!mPaymentButton.isAdded()) {
			FragmentTransaction transaction = manager.beginTransaction();
			transaction.add(PAYMENT_FRAG_CONTAINER_ID, mPaymentButton, FRAG_TAG_PAYMENT_BUTTON);
			transaction.commit();
		}
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

	protected void buildCheckoutForm() {

		//SET UP THE FORM FRAGMENTS
		attachTravelerForm();
		attachPaymentForm();

		//CLEAR THE CONTAINER
		mCheckoutRowsC.removeAllViews();

		//FIRST HEADING
		String headingArg = "";
		if (getLob() == LineOfBusiness.FLIGHTS) {
			headingArg = "FLIGHT";
		}
		else if (getLob() == LineOfBusiness.HOTELS) {
			headingArg = "HOTEL";
		}
		addGroupHeading(getString(R.string.now_booking_TEMPLATE, headingArg));

		//LOGIN STUFF
		FrameLayout frame = new FrameLayout(getActivity());
		frame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		frame.setId(LOGIN_FRAG_CONTAINER_ID);
		add(frame);
		attachLoginButtons();

		//TRAVELERS
		populateTravelerData();
		addGroupHeading(R.string.traveler_information);
		for (int i = 0; i < Db.getTravelers().size(); i++) {
			addTravelerView(i);
		}

		//PAYMENT
		addGroupHeading(R.string.payment_method);
		mPaymentView = new FrameLayout(getActivity());
		mPaymentView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		mPaymentView.setId(PAYMENT_FRAG_CONTAINER_ID);
		dressCheckoutView(mPaymentView, 0);
		addActionable(mPaymentView, new Runnable() {
			@Override
			public void run() {
				openPaymentForm();
			}
		});
		attachPaymentButton();

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
				public void onClick(View arg0) {
					arg0.post(action);
				}
			});
		}
		return add(view);
	}

	public View add(View view) {
		mCheckoutRowsC.addView(view);
		return view;
	}

	private void dressCheckoutView(View dressableView, int groupIndex) {
		if (groupIndex == 0) {
			dressableView.setBackgroundResource(R.drawable.bg_checkout_information_top_tab);
		}
		else {
			dressableView.setBackgroundResource(R.drawable.bg_checkout_information_middle_tab);
		}
		int padding = getResources().getDimensionPixelSize(R.dimen.traveler_button_padding);
		dressableView.setPadding(padding, padding, padding, padding);
	}

	/*
	 * TRAVELER FORM STUFF
	 */

	protected void addTravelerView(final int travelerNumber) {

		//Add the container to the layout (and make it actionable)
		FrameLayout frame = new FrameLayout(getActivity());
		frame.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		frame.setId(getTravelerButtonContainerId(travelerNumber));
		addActionable(frame, new Runnable() {
			@Override
			public void run() {
				openTravelerEntry(travelerNumber);
			}
		});
		dressCheckoutView(frame, travelerNumber);
		mTravelerViews.add(frame);

		//Add fragment to the new container
		FragmentManager manager = getChildFragmentManager();
		TravelerButtonFragment btnFrag = (TravelerButtonFragment) manager
			.findFragmentByTag(getTravelerButtonFragTag(travelerNumber));
		if (btnFrag == null) {
			btnFrag = TravelerButtonFragment.newInstance(getLob(), travelerNumber);
		}
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

		//Slide views
		View selectedView = mCheckoutRowsC.getChildAt(viewIndex);
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

	protected void bindTravelers() {
		for (TravelerButtonFragment btn : mTravelerButtonFrags) {
			btn.bindToDb();
		}
	}

	protected boolean validateTravelers() {
		boolean retVal = false;
		for (TravelerButtonFragment btn : mTravelerButtonFrags) {
			retVal = btn.isValid();
			if (!retVal) {
				break;
			}
		}
		return retVal;
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
		int numAdults = travelers.size();
		if (getLob() == LineOfBusiness.FLIGHTS) {
			numAdults = Db.getFlightSearch().getSearchParams().getNumAdults();
		}
		else {
			//Hotels currently always just has one traveler object
			numAdults = 1;
		}
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

	/*
	 * PAYMENT FORM STUFF
	 */

	protected void openPaymentForm() {
		//finding index
		int viewNumber = -1;
		for (int i = 0; i < mCheckoutRowsC.getChildCount(); i++) {
			if (mCheckoutRowsC.getChildAt(i) == mPaymentView) {
				viewNumber = i;
				break;
			}
		}
		if (viewNumber >= 0) {
			mShowingViewIndex = viewNumber;
			mPaymentForm.bindToDb();
			setState(CheckoutFormState.EDIT_PAYMENT, true);
		}
	}

	protected boolean validatePaymentInfo() {
		if (mPaymentButton != null) {
			return mPaymentButton.isValid();
		}
		return false;
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
				}
				else if (state == CheckoutFormState.EDIT_TRAVELER) {
					mTravelerFormC.setVisibility(View.VISIBLE);
					mPaymentFormC.setVisibility(View.INVISIBLE);
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
}
