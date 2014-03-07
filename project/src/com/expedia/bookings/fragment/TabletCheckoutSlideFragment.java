package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletCheckoutActivity;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.enums.CheckoutSlideState;
import com.expedia.bookings.enums.CheckoutState;
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
import com.mobiata.android.util.Ui;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class TabletCheckoutSlideFragment extends LobableFragment implements IBackManageable,
	IStateProvider<CheckoutSlideState>, ICheckoutDataListener, CheckoutLoginButtonsFragment.ILoginStateChangedListener {

	private static final String STATE_CHECKOUTSLIDESTATE = "STATE_CHECKOUTSLIDESTATE";

	private ViewGroup mRootC;

	private StateManager<CheckoutSlideState> mStateManager = new StateManager<CheckoutSlideState>(
		CheckoutSlideState.TOS_ACCEPT, this);

	public static TabletCheckoutSlideFragment newInstance() {
		TabletCheckoutSlideFragment frag = new TabletCheckoutSlideFragment();
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_slide_to_purchase, container, false);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CHECKOUTSLIDESTATE)) {
			String stateName = savedInstanceState.getString(STATE_CHECKOUTSLIDESTATE);
			CheckoutSlideState state = CheckoutSlideState.valueOf(stateName);
			mStateManager.setDefaultState(state);
		}

		registerStateListener(mStateHelper, false);
		registerStateListener(new StateListenerLogger<CheckoutSlideState>(), false);

		View cvvBtn = Ui.findView(mRootC, R.id.goto_cvv_btn);
		cvvBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Activity activity = getActivity();
				if (activity instanceof TabletCheckoutActivity) {
					((TabletCheckoutActivity)activity).setCheckoutState(CheckoutState.CVV, true);
				}
			}
		});


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
		outState.putString(STATE_CHECKOUTSLIDESTATE, mStateManager.getState().name());
	}

	/*
	 * BINDING
	 */

	public void bindAll() {
	}

	/*
	 * ICheckoutDataListener
	 */

	@Override
	public void onCheckoutDataUpdated() {
		bindAll();
	}

	/*
	 * GETTERS / SETTERS
	 */

	@Override
	public void onLobSet(LineOfBusiness lob) {
		if (mRootC != null) {
			//TODO: set slide state depending if this POS needs "i accept"
		}
	}

	public void setState(CheckoutSlideState state, boolean animate) {
		mStateManager.setState(state, animate);
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
			if (mStateManager.getState() != CheckoutSlideState.TOS_ACCEPT) {
				setState(CheckoutSlideState.TOS_ACCEPT, true);
				return true;
			}
			return false;
		}

	};

	/*
	 * ISTATELISTENER
	 */

	private StateListenerHelper<CheckoutSlideState> mStateHelper = new StateListenerHelper<CheckoutSlideState>() {

		@Override
		public void onStateTransitionStart(CheckoutSlideState stateOne, CheckoutSlideState stateTwo) {
			if (stateTwo == CheckoutSlideState.TOS_ACCEPT) {
				//TODO: show "I Accept" part
			}
			else if (stateTwo == CheckoutSlideState.SLIDE) {
				//TODO: show slide part
			}
			else if (stateTwo == CheckoutSlideState.BOOK_NOW) {
				//TODO: show "Book now" button
			}
		}

		@Override
		public void onStateTransitionUpdate(CheckoutSlideState stateOne, CheckoutSlideState stateTwo, float percentage) {
			// TODO: show transitional part
		}

		@Override
		public void onStateTransitionEnd(CheckoutSlideState stateOne, CheckoutSlideState stateTwo) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStateFinalized(CheckoutSlideState state) {
			// TODO this part
		}

	};

	/*
	 * ISTATEPROVIDER
	 */
	private StateListenerCollection<CheckoutSlideState> mStateListeners = new StateListenerCollection<CheckoutSlideState>(
		mStateManager.getState());

	@Override
	public void startStateTransition(CheckoutSlideState stateOne, CheckoutSlideState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(CheckoutSlideState stateOne, CheckoutSlideState stateTwo, float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(CheckoutSlideState stateOne, CheckoutSlideState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(CheckoutSlideState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<CheckoutSlideState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<CheckoutSlideState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}

	@Override
	public void onLoginStateChanged() {
		bindAll();
	}
}
