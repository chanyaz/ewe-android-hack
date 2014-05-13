package com.expedia.bookings.fragment.base;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.dialog.BreakdownDialogFragment;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.graphics.HeaderBitmapDrawable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.ITripBucketBookClickListener;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

/**
 * TripBucketItemFragment: Tablet 2014
 */
public abstract class TripBucketItemFragment extends Fragment implements IStateProvider<TripBucketItemState> {

	private static final String STATE_BUCKET_ITEM_STATE = "STATE_BUCKET_ITEM_STATE";
	private static final String STATE_OVERLAY_COLOR_FETCHED = "STATE_OVERLAY_COLOR_FETCHED";
	private static final String STATE_PRICE_CHANGED_STRING = "STATE_PRICE_CHANGED_STRING";

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
	private ViewGroup mPriceChangedClipC;
	private ViewGroup mPriceChangedC;
	private ViewGroup mBookBtnContainer;
	private ImageView mTripBucketImageView;
	private TextView mBookBtnText;
	private TextView mTripPriceText;
	private ViewGroup mNameAndDurationContainer;
	private TextView mNameText;
	private TextView mDurationText;
	private android.widget.TextView mPriceChangedTv;
	private ImageView mBookingCompleteCheckImg;
	private HeaderBitmapColorAveragedDrawable mHeaderBitmapDrawable;

	private BreakdownDialogFragment mBreakdownFrag;

	//Colors
	private int mExpandedBgColor = Color.WHITE;
	private int mCollapsedBgColor = Color.TRANSPARENT;

	//Misc
	private StateManager<TripBucketItemState> mStateManager = new StateManager<TripBucketItemState>(
		TripBucketItemState.DEFAULT, this);

	private String mPriceChangeNotificationText;

	private ITripBucketBookClickListener mListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, ITripBucketBookClickListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_tripbucket_item, null);
		mTopC = Ui.findView(mRootC, R.id.trip_bucket_item_top_container);
		mExpandedC = Ui.findView(mRootC, R.id.trip_bucket_item_expanded_container);

		if (savedInstanceState != null) {
			String stateName = savedInstanceState.getString(STATE_BUCKET_ITEM_STATE);
			TripBucketItemState state = TripBucketItemState.valueOf(stateName);
			mStateManager.setDefaultState(state);
			mPriceChangeNotificationText = savedInstanceState.getString(STATE_PRICE_CHANGED_STRING);
		}

		mPriceChangedClipC = Ui.findView(mRootC, R.id.trip_bucket_item_price_change_clip_container);
		mPriceChangedC = Ui.findView(mRootC, R.id.trip_bucket_item_price_change_container);

		addTopView(inflater, mTopC);
		addExpandedView(inflater, mExpandedC);
		addPriceChangeNotificationView(inflater, mPriceChangedC);

		registerStateListener(new StateListenerLogger<TripBucketItemState>(), false);
		registerStateListener(mStateHelper, false);

		return mRootC;
	}

	private void addTopView(LayoutInflater inflater, ViewGroup viewGroup) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.tablet_card_tripbucket, viewGroup);
		mTripBucketImageView = Ui.findView(root, R.id.tripbucket_card_background_view);
		mBookBtnContainer = Ui.findView(root, R.id.book_button_container);
		mBookBtnText = Ui.findView(root, R.id.book_button_text);
		mTripPriceText = Ui.findView(root, R.id.trip_bucket_price_text);
		mNameAndDurationContainer = Ui.findView(root, R.id.name_and_trip_duration_container);
		mNameText = Ui.findView(root, R.id.name_text_view);
		mDurationText = Ui.findView(root, R.id.trip_duration_text_view);
		mBookingCompleteCheckImg = Ui.findView(root, R.id.booking_complete_check);

		mHeaderBitmapDrawable = new HeaderBitmapColorAveragedDrawable();
		mHeaderBitmapDrawable.setGradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_POSITIONS);
		mHeaderBitmapDrawable.setCornerMode(HeaderBitmapDrawable.CornerMode.ALL);
		mHeaderBitmapDrawable.setCornerRadius(getActivity().getResources().getDimensionPixelSize(R.dimen.tablet_result_corner_radius));
		mTripBucketImageView.setImageDrawable(mHeaderBitmapDrawable);
	}

	private void addPriceChangeNotificationView(LayoutInflater inflater, ViewGroup viewGroup) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_price_change_notification, viewGroup);
		mPriceChangedTv = Ui.findView(root, R.id.price_change_notification_text);
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_BUCKET_ITEM_STATE, mStateManager.getState().name());
		outState.putString(STATE_PRICE_CHANGED_STRING, mPriceChangeNotificationText);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		bind();
	}

	public ITripBucketBookClickListener getTripBucketBookClickedListener() {
		return mListener;
	}

	public int getTopHeight() {
		return mTopC.getHeight();
	}

	public int getExpandedHeight() {
		return mExpandedC.getHeight();
	}

	public int getPriceChangeHeight() {
		return mPriceChangedC.getHeight();
	}

	public void setState(TripBucketItemState state) {
		setState(state, false);
	}

	public void setState(TripBucketItemState state, boolean animate) {
		mStateManager.setState(state, animate);
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
			mPriceChangedTv.setText(mPriceChangeNotificationText);
		}
	}

	public void setPriceChangeNotificationText(String priceChangeText) {
		if (priceChangeText != null) {
			this.mPriceChangeNotificationText = priceChangeText;
			mPriceChangedTv.setText(priceChangeText);
		}
	}

	protected void showBreakdownDialog(LineOfBusiness lob) {
		mBreakdownFrag = com.expedia.bookings.utils.Ui.findSupportFragment(TripBucketItemFragment.this, BreakdownDialogFragment.TAG);
		if (mBreakdownFrag == null) {
			if (lob == LineOfBusiness.FLIGHTS) {
				mBreakdownFrag = BreakdownDialogFragment.buildFlightBreakdownDialog(getActivity(), Db.getFlightSearch(), Db.getBillingInfo());
			}
			else if (lob == LineOfBusiness.HOTELS) {
				mBreakdownFrag = BreakdownDialogFragment.buildHotelRateBreakdownDialog(getActivity(), Db.getHotelSearch());
			}
			else {
				throw new UnsupportedOperationException("Attempting to show a price breakdown dialog for a LOB not supported.");
			}
		}
		if (!mBreakdownFrag.isAdded()) {
			mBreakdownFrag.show(getFragmentManager(), BreakdownDialogFragment.TAG);
		}
	}

	/*
	ISTATELISTENER
	*/

	private StateListenerHelper<TripBucketItemState> mStateHelper = new StateListenerHelper<TripBucketItemState>() {
		@Override
		public void onStateTransitionStart(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
			// Collapsed --> Expanded, Price Change
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.EXPANDED) {

				mExpandedC.setVisibility(View.VISIBLE);
				mBookBtnContainer.setVisibility(View.VISIBLE);
				mBookBtnContainer.setAlpha(1.0f);
				setNameAndDurationSlidePercentage(0.0f);
				setExpandedSlidePercentage(0.0f);
			}
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {

				mExpandedC.setVisibility(View.VISIBLE);
				mBookBtnContainer.setVisibility(View.VISIBLE);
				mBookBtnContainer.setAlpha(1.0f);
				setNameAndDurationSlidePercentage(0.0f);
				setExpandedSlidePercentage(0.0f);

				mPriceChangedC.setVisibility(View.VISIBLE);
				setPriceChangePercentage(0.0f);
			}

			// Expanded, Price Change --> Collapsed
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setVisibility(View.VISIBLE);
				mBookBtnContainer.setAlpha(0.0f);
				setNameAndDurationSlidePercentage(1.0f);
				setExpandedSlidePercentage(1.0f);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setVisibility(View.VISIBLE);
				mBookBtnContainer.setAlpha(0.0f);
				setNameAndDurationSlidePercentage(1.0f);
				setExpandedSlidePercentage(1.0f);

				// TODO animate price change
			}

			// Expanded <--> Price change
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {
				mPriceChangedC.setVisibility(View.VISIBLE);
				// TODO animate price change
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE && stateTwo == TripBucketItemState.EXPANDED) {
				mPriceChangedC.setVisibility(View.VISIBLE);
				// TODO animate price change
			}

			// Show confirmation checkmark
			if (stateTwo == TripBucketItemState.CONFIRMATION) {
				mBookingCompleteCheckImg.setVisibility(View.VISIBLE);
				mBookingCompleteCheckImg.setTranslationY(mBookBtnContainer.getBottom() - mNameAndDurationContainer.getBottom());
				mBookingCompleteCheckImg.setAlpha(0.0f);
			}
		}

		@Override
		public void onStateTransitionUpdate(TripBucketItemState stateOne, TripBucketItemState stateTwo, float percentage) {
			// Collapsed --> Expanded, Price Change
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.EXPANDED) {

				mBookBtnContainer.setAlpha(1.0f - percentage);
				setNameAndDurationSlidePercentage(percentage);
				setExpandedSlidePercentage(percentage);
			}
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {

				mBookBtnContainer.setAlpha(1.0f - percentage);
				setNameAndDurationSlidePercentage(percentage);
				setExpandedSlidePercentage(percentage);
				setPriceChangePercentage(percentage);
			}


			// Expanded, Price Change --> Collapsed
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setAlpha(percentage);
				setNameAndDurationSlidePercentage(1.0f - percentage);
				setExpandedSlidePercentage(1.0f - percentage);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setAlpha(percentage);
				setNameAndDurationSlidePercentage(1.0f - percentage);
				setExpandedSlidePercentage(1.0f - percentage);
				setPriceChangePercentage(1.0f - percentage);
			}


			// Expanded <--> Price change
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {
				setPriceChangePercentage(percentage);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE && stateTwo == TripBucketItemState.EXPANDED) {
				setPriceChangePercentage(1.0f - percentage);
			}

			// Show confirmation checkmark
			if (stateTwo == TripBucketItemState.CONFIRMATION) {
				mBookingCompleteCheckImg.setAlpha(percentage);
			}
		}

		@Override
		public void onStateTransitionEnd(TripBucketItemState stateOne, TripBucketItemState stateTwo) {
			// Collapsed --> Expanded, Price Change
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.EXPANDED) {

				mBookBtnContainer.setAlpha(0.0f);
				setNameAndDurationSlidePercentage(1.0f);
				setExpandedSlidePercentage(1.0f);
			}
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {

				mBookBtnContainer.setAlpha(0.0f);
				setNameAndDurationSlidePercentage(1.0f);
				setExpandedSlidePercentage(1.0f);
				setPriceChangePercentage(1.0f);
			}


			// Expanded, Price Change --> Collapsed
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setAlpha(1.0f);
				setNameAndDurationSlidePercentage(0.0f);
				setExpandedSlidePercentage(0.0f);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setAlpha(1.0f);
				setNameAndDurationSlidePercentage(0.0f);
				setExpandedSlidePercentage(0.0f);
				setPriceChangePercentage(0.0f);
			}


			// Expanded <--> Price change
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {
				setPriceChangePercentage(1.0f);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE && stateTwo == TripBucketItemState.EXPANDED) {
				setPriceChangePercentage(0.0f);
			}

			// Show confirmation checkmark
			if (stateTwo == TripBucketItemState.CONFIRMATION) {
				mBookingCompleteCheckImg.setAlpha(1.0f);
			}
		}

		@Override
		public void onStateFinalized(TripBucketItemState state) {
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
			mPriceChangedC.setVisibility(View.GONE);
			break;

		case SHOWING_PRICE_CHANGE:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mExpandedC.setVisibility(View.VISIBLE);
			mPriceChangedC.setVisibility(View.VISIBLE);
			break;

		case DISABLED:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mExpandedC.setVisibility(View.GONE);
			mPriceChangedC.setVisibility(View.GONE);
			break;

		case EXPANDED:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mExpandedC.setVisibility(View.VISIBLE);
			mPriceChangedC.setVisibility(View.GONE);
			break;

		case PURCHASED:
			mBookingCompleteCheckImg.setVisibility(View.VISIBLE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mExpandedC.setVisibility(View.GONE);
			mPriceChangedC.setVisibility(View.GONE);
			break;

		case CONFIRMATION:
			mBookingCompleteCheckImg.setVisibility(View.VISIBLE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mExpandedC.setVisibility(View.VISIBLE);
			mPriceChangedC.setVisibility(View.GONE);
			break;
		}
	}

	public void setNameAndDurationSlidePercentage(float percentage) {
		mNameAndDurationContainer.setTranslationY((mBookBtnContainer.getBottom() - mNameAndDurationContainer.getBottom()) * percentage);
	}

	public void setExpandedSlidePercentage(float percentage) {
		float amount = mExpandedC.getHeight() * -(1.0f - percentage);
		mExpandedC.setTranslationY(amount);
		mPriceChangedClipC.setTranslationY(amount);
	}

	public void setPriceChangePercentage(float percentage) {
		mPriceChangedC.setTranslationY(-mPriceChangedC.getHeight() * (1.0f - percentage));
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

	public abstract CharSequence getTripPrice();

	public abstract OnClickListener getOnBookClickListener();

}
