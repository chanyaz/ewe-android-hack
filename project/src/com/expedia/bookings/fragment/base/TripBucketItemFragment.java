package com.expedia.bookings.fragment.base;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.expedia.bookings.R;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.mobiata.android.util.Ui;

/**
 * TripBucketItemFragment: Tablet 2014
 */
public abstract class TripBucketItemFragment extends Fragment implements IStateProvider<TripBucketItemState> {

	private static final String STATE_BUCKET_ITEM_STATE = "STATE_BUCKET_ITEM_STATE";

	//Views
	private ViewGroup mRootC;
	private ViewGroup mTopC;
	private ViewGroup mExpandedC;
	private ViewGroup mPurchasedC;
	private Button mBookBtn;

	//Colors
	private int mExpandedBgColor = Color.WHITE;
	private int mCollapsedBgColor = Color.TRANSPARENT;

	//Misc
	private StateManager<TripBucketItemState> mStateManager = new StateManager<TripBucketItemState>(
		TripBucketItemState.DEFAULT, this);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_item, null);
		mTopC = Ui.findView(mRootC, R.id.trip_bucket_item_top_container);
		mExpandedC = Ui.findView(mRootC, R.id.trip_bucket_item_expanded_container);
		mPurchasedC = Ui.findView(mRootC, R.id.trip_bucket_item_purchased_container);
		mBookBtn = Ui.findView(mRootC, R.id.checkout_button);
		FontCache.setTypeface(mBookBtn, Font.ROBOTO_MEDIUM);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_BUCKET_ITEM_STATE)) {
			String stateName = savedInstanceState.getString(STATE_BUCKET_ITEM_STATE);
			TripBucketItemState state = TripBucketItemState.valueOf(stateName);
			mStateManager.setDefaultState(state);
		}

		addTopView(inflater, mTopC);

		registerStateListener(mStateHelper, false);

		return mRootC;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_BUCKET_ITEM_STATE, mStateManager.getState().name());
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		bind();
	}

	public void setState(TripBucketItemState state) {
		mStateManager.setState(state, false);
	}

	public TripBucketItemState getState() {
		return mStateManager.getState();
	}

	public void bind() {
		if (mRootC != null) {
			//refresh the state...
			setState(mStateManager.getState());

			mBookBtn.setText(getBookButtonText());
			mBookBtn.setOnClickListener(getOnBookClickListener());

			doBind();
		}
	}

	/*
	ISTATELISTENER
	*/

	private StateListenerHelper<TripBucketItemState> mStateHelper = new StateListenerHelper<TripBucketItemState>() {

		@Override
		public void onStateTransitionStart(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
			//Currently we are never animating.
		}

		@Override
		public void onStateTransitionUpdate(TripBucketItemState stateOne, TripBucketItemState stateTwo, float percentage) {
			//Currently we are never animating.
		}

		@Override
		public void onStateTransitionEnd(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
			//Currently we are never animating.
		}

		@Override
		public void onStateFinalized(TripBucketItemState state) {
			if (state == TripBucketItemState.EXPANDED) {
				int padding = (int) getResources().getDimension(R.dimen.trip_bucket_expanded_card_padding);
				mRootC.setBackgroundColor(mExpandedBgColor);
				mRootC.setPadding(padding, padding, padding, padding);

				mExpandedC.removeAllViews();
				addExpandedView(getLayoutInflater(null), mExpandedC);
			}
			else {
				int padding = 0;
				mRootC.setBackgroundColor(mCollapsedBgColor);
				mRootC.setPadding(padding, padding, padding, padding);
				mExpandedC.removeAllViews();
			}

			setVisibilityState(state);
		}
	};

	protected void setVisibilityState(TripBucketItemState state) {
		if (state == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
			mBookBtn.setVisibility(View.VISIBLE);
		}
		else {
			mBookBtn.setVisibility(View.GONE);
		}

		if (state == TripBucketItemState.EXPANDED) {
			mExpandedC.setVisibility(View.VISIBLE);
		}
		else {
			mExpandedC.setVisibility(View.GONE);
		}

		if(state == TripBucketItemState.PURCHASED){
			mPurchasedC.setVisibility(View.VISIBLE);
		}else{
			mPurchasedC.setVisibility(View.GONE);
		}
	}



	/*
	ISTATEPROVIDER
	*/

	private StateListenerCollection<TripBucketItemState> mStateListeners = new StateListenerCollection<TripBucketItemState>(
		mStateManager.getState());

	@Override
	public void startStateTransition(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(TripBucketItemState stateOne, TripBucketItemState stateTwo, float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(TripBucketItemState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<TripBucketItemState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<TripBucketItemState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}

	/*
	ABSTRACT METHODS
 	*/

	protected abstract void doBind();

	public abstract CharSequence getBookButtonText();

	public abstract void addTopView(LayoutInflater inflater, ViewGroup viewGroup);

	public abstract void addExpandedView(LayoutInflater inflater, ViewGroup viewGroup);

	public abstract OnClickListener getOnBookClickListener();

}
