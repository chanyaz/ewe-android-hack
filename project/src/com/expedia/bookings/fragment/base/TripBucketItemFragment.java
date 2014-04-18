package com.expedia.bookings.fragment.base;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.ColorAvgUtils;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.bitmaps.ColorScheme;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

/**
 * TripBucketItemFragment: Tablet 2014
 */
public abstract class TripBucketItemFragment extends Fragment implements IStateProvider<TripBucketItemState> {

	private static final String STATE_BUCKET_ITEM_STATE = "STATE_BUCKET_ITEM_STATE";
	private static final String STATE_OVERLAY_COLOR_FETCHED = "STATE_OVERLAY_COLOR_FETCHED";

	protected static final int[] DEFAULT_GRADIENT_COLORS = new int[] {
		0x00000000,
		0x40000000,
		0xa4000000,
	};
	protected static final float[] DEFAULT_GRADIENT_POSITIONS = null; // Distribute the gradient colors evenly

	//Views
	private ViewGroup mRootC;
	private ViewGroup mTopC;
	private ViewGroup mExpandedC;
	private ViewGroup mBookBtnContainer;
	private ImageView mTripBucketImageView;
	private TextView mBookBtnText;
	private TextView mTripPriceText;
	private TextView mNameText;
	private TextView mDurationText;
	private ImageView mBookingCompleteCheckImg;
	private HeaderBitmapColorAveragedDrawable mHeaderBitmapDrawable;

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

		if (savedInstanceState != null) {
			String stateName = savedInstanceState.getString(STATE_BUCKET_ITEM_STATE);
			TripBucketItemState state = TripBucketItemState.valueOf(stateName);
			mStateManager.setDefaultState(state);
		}

		addTopView(inflater, mTopC);

		registerStateListener(mStateHelper, false);

		return mRootC;
	}

	private void addTopView(LayoutInflater inflater, ViewGroup viewGroup) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.tablet_card_tripbucket, viewGroup);
		mTripBucketImageView = Ui.findView(root, R.id.tripbucket_card_background_view);
		mBookBtnContainer = Ui.findView(root, R.id.book_button_container);
		mBookBtnText = Ui.findView(root, R.id.book_button_text);
		mTripPriceText = Ui.findView(root, R.id.trip_bucket_price_text);
		mNameText = Ui.findView(root, R.id.name_text_view);
		mDurationText = Ui.findView(root, R.id.trip_duration_text_view);
		mBookingCompleteCheckImg = Ui.findView(root, R.id.booking_complete_check);

		mHeaderBitmapDrawable = new HeaderBitmapColorAveragedDrawable();
		mHeaderBitmapDrawable.setGradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_POSITIONS);
		mHeaderBitmapDrawable.setCornerMode(HeaderBitmapDrawable.CornerMode.ALL);
		mHeaderBitmapDrawable.setCornerRadius(getActivity().getResources().getDimensionPixelSize(R.dimen.tablet_result_corner_radius));
		mTripBucketImageView.setImageDrawable(mHeaderBitmapDrawable);
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

			mBookBtnText.setText(getBookButtonText());
			mBookBtnContainer.setOnClickListener(getOnBookClickListener());

			mTripPriceText.setText(getTripPrice());
			mNameText.setText(getNameText());
			mDurationText.setText(getDateRangeText());
			if (doTripBucketImageRefresh()) {
				mHeaderBitmapDrawable.enableOverlay();
				addTripBucketImage(mTripBucketImageView, mHeaderBitmapDrawable);
			}
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
			if (state == TripBucketItemState.EXPANDED || state == TripBucketItemState.CONFIRMATION) {
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
		switch (state) {
		case DEFAULT:
		case SHOWING_CHECKOUT_BUTTON:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.VISIBLE);
			mExpandedC.setVisibility(View.GONE);
			break;

		case DISABLED:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mExpandedC.setVisibility(View.GONE);
			break;

		case EXPANDED:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.VISIBLE);
			break;

		case PURCHASED:
			mBookingCompleteCheckImg.setVisibility(View.VISIBLE);
			mBookBtnContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.GONE);
			break;

		case CONFIRMATION:
			mBookingCompleteCheckImg.setVisibility(View.VISIBLE);
			mBookBtnContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.VISIBLE);
			break;
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

	public abstract CharSequence getBookButtonText();

	public abstract void addExpandedView(LayoutInflater inflater, ViewGroup viewGroup);

	public abstract void addTripBucketImage(ImageView imageView, HeaderBitmapColorAveragedDrawable headerBitmapDrawable);

	public abstract boolean doTripBucketImageRefresh();

	public abstract String getNameText();

	public abstract String getDateRangeText();

	public abstract String getTripPrice();

	public abstract OnClickListener getOnBookClickListener();

}
