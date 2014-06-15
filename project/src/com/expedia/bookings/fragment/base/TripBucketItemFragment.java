package com.expedia.bookings.fragment.base;

import android.app.Activity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightSearch;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.TripBucketItem;
import com.expedia.bookings.dialog.BreakdownDialogFragment;
import com.expedia.bookings.enums.TripBucketItemState;
import com.expedia.bookings.graphics.HeaderBitmapColorAveragedDrawable;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.ITripBucketBookClickListener;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerHelper;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.interfaces.helpers.StateManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.ColorBuilder;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.TextView;
import com.mobiata.android.util.Ui;

/**
 * TripBucketItemFragment: Tablet 2014
 * Extended by TripBucketFlightFragment and TripBucketHotelFragment
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
	protected ViewGroup mRootC;
	private ViewGroup mTopC;
	private ViewGroup mExpandedC;
	private View mCardCornersBottom;
	private ViewGroup mPriceChangedClipC;
	private ViewGroup mPriceChangedC;
	private ViewGroup mBookBtnContainer;
	private ViewGroup mSoldOutContainer;
	private ImageView mTripBucketImageView;
	private TextView mBookBtnText;
	private TextView mSoldOutText;
	private TextView mTripPriceText;
	private ViewGroup mNameAndDurationContainer;
	private TextView mNameText;
	private TextView mDurationText;
	private android.widget.TextView mPriceChangedTv;
	private ImageView mBookingCompleteCheckImg;
	private HeaderBitmapColorAveragedDrawable mHeaderBitmapDrawable;
	private ColorDrawable mSoldOutSelectedOverlay;
	private ColorDrawable mSoldOutUnSelectedOverlay;

	private BreakdownDialogFragment mBreakdownFrag;

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
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_tripbucket_item, null);
		mTopC = Ui.findView(mRootC, R.id.trip_bucket_item_top_container);
		mExpandedC = Ui.findView(mRootC, R.id.trip_bucket_item_expanded_container);
		mCardCornersBottom = Ui.findView(mRootC, R.id.card_corners_bottom);

		if (savedInstanceState != null) {
			String stateName = savedInstanceState.getString(STATE_BUCKET_ITEM_STATE);
			TripBucketItemState state = TripBucketItemState.valueOf(stateName);
			mStateManager.setDefaultState(state);
			mPriceChangeNotificationText = savedInstanceState.getString(STATE_PRICE_CHANGED_STRING);
		}

		// Top Part
		mTripBucketImageView = Ui.findView(mTopC, R.id.tripbucket_card_background_view);
		mBookBtnContainer = Ui.findView(mTopC, R.id.book_button_container);
		mSoldOutContainer = Ui.findView(mTopC, R.id.sold_out_container);
		mBookBtnText = Ui.findView(mTopC, R.id.book_button_text);
		mSoldOutText = Ui.findView(mTopC, R.id.sold_out_text);
		mTripPriceText = Ui.findView(mTopC, R.id.trip_bucket_price_text);
		mNameAndDurationContainer = Ui.findView(mTopC, R.id.name_and_trip_duration_container);
		mNameText = Ui.findView(mTopC, R.id.name_text_view);
		mDurationText = Ui.findView(mTopC, R.id.trip_duration_text_view);
		mBookingCompleteCheckImg = Ui.findView(mTopC, R.id.booking_complete_check);

		mHeaderBitmapDrawable = new HeaderBitmapColorAveragedDrawable();
		mHeaderBitmapDrawable.setGradient(DEFAULT_GRADIENT_COLORS, DEFAULT_GRADIENT_POSITIONS);
		mTripBucketImageView.setImageDrawable(mHeaderBitmapDrawable);

		// Expanded / Receipt Part
		addExpandedView(inflater, mExpandedC);


		// Price Change Part
		mPriceChangedClipC = Ui.findView(mRootC, R.id.trip_bucket_item_price_change_clip_container);
		mPriceChangedC = Ui.findView(mPriceChangedClipC, R.id.price_change_notification_container);
		mPriceChangedTv = Ui.findView(mPriceChangedC, R.id.price_change_notification_text);

		registerStateListener(new StateListenerLogger<TripBucketItemState>(), false);
		registerStateListener(mStateHelper, false);

		ColorBuilder builder = new ColorBuilder(getResources().getColor(R.color.trip_bucket_sold_out_selected));
		mSoldOutSelectedOverlay = new ColorDrawable(builder.build());

		builder = new ColorBuilder(getResources().getColor(R.color.trip_bucket_sold_out_unselected));
		mSoldOutUnSelectedOverlay = new ColorDrawable(builder.build());

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
		refreshTripPrice();
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
		if (getItem() != null) {
			bind();
		}
	}

	public ITripBucketBookClickListener getTripBucketBookClickedListener() {
		return mListener;
	}

	public int getTopHeight() {
		return mTopC.getHeight();
	}

	public Rect getTopRect() {
		if (mTopC != null) {
			return ScreenPositionUtils.getGlobalScreenPositionWithoutTranslations(mTopC);
		}
		return new Rect();
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
			mSoldOutContainer.setOnClickListener(getOnBookClickListener());

			refreshTripPrice();
			mNameText.setText(getNameText());
			mDurationText.setText(getDateRangeText());
			if (doTripBucketImageRefresh()) {
				mHeaderBitmapDrawable.enableOverlay();
				addTripBucketImage(mTripBucketImageView, mHeaderBitmapDrawable);
			}
			mPriceChangedTv.setText(mPriceChangeNotificationText);
		}
	}

	private void refreshTripPrice() {
		mTripPriceText.setText(getTripPrice());
	}

	public void setPriceChangeNotificationText(String priceChangeText) {
		if (priceChangeText != null) {
			this.mPriceChangeNotificationText = priceChangeText;
			mPriceChangedTv.setText(priceChangeText);
		}
	}

	protected void showBreakdownDialog(LineOfBusiness lob) {
		mBreakdownFrag = com.expedia.bookings.utils.Ui
			.findSupportFragment(TripBucketItemFragment.this, BreakdownDialogFragment.TAG);
		if (mBreakdownFrag == null) {
			if (lob == LineOfBusiness.FLIGHTS) {
				mBreakdownFrag = BreakdownDialogFragment
					.buildFlightBreakdownDialog(getActivity(), Db.getFlightSearch(), Db.getBillingInfo());
			}
			else if (lob == LineOfBusiness.HOTELS) {
				mBreakdownFrag = BreakdownDialogFragment
					.buildHotelRateBreakdownDialog(getActivity(), Db.getHotelSearch());
			}
			else {
				throw new UnsupportedOperationException(
					"Attempting to show a price breakdown dialog for a LOB not supported.");
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

			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.BOOKING_UNAVAILABLE) {
				mSoldOutContainer.setVisibility(View.VISIBLE);
				mSoldOutContainer.setAlpha(0.0f);
				setNameAndDurationSoldOutSlidePercentage(1.0f);
				setExpandedSlidePercentage(1.0f);
				// SelectedState for BOOKING_UNAVAILABLE. Let's add padding to the selected trip bucket item when sold out.
				setItemSoldOutSelected(true);
			}

			// Expanded, Price Change --> Collapsed
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setVisibility(View.VISIBLE);
				mBookBtnContainer.setAlpha(0.0f);
				setNameAndDurationSlidePercentage(1.0f);
				setExpandedSlidePercentage(1.0f);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE
				&& stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
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
				mBookingCompleteCheckImg.setAlpha(0.0f);
			}

			if (stateOne == TripBucketItemState.BOOKING_UNAVAILABLE
				&& stateTwo == TripBucketItemState.BOOKING_UNAVAILABLE) {
				setItemSoldOutSelected(isSelected());
			}
		}

		@Override
		public void onStateTransitionUpdate(TripBucketItemState stateOne, TripBucketItemState stateTwo,
			float percentage) {
			// Collapsed --> Expanded, Price Change
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.EXPANDED) {

				mBookBtnContainer.setAlpha(1.0f - percentage);
				setNameAndDurationSlidePercentage(percentage);
				setExpandedSlidePercentage(percentage);
				mHeaderBitmapDrawable.setOverlayAlpha(percentage);
			}
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {

				mBookBtnContainer.setAlpha(1.0f - percentage);
				setNameAndDurationSlidePercentage(percentage);
				setExpandedSlidePercentage(percentage);
				setPriceChangePercentage(percentage);
			}

			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.BOOKING_UNAVAILABLE) {
				mSoldOutContainer.setAlpha(percentage);
				setNameAndDurationSoldOutSlidePercentage(1.0f - percentage);
				setExpandedSlidePercentage(1.0f - percentage);
			}
			// Expanded, Price Change --> Collapsed
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setAlpha(percentage);
				setNameAndDurationSlidePercentage(1.0f - percentage);
				setExpandedSlidePercentage(1.0f - percentage);
				mHeaderBitmapDrawable.setOverlayAlpha(1f - percentage);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE
				&& stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
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
				mHeaderBitmapDrawable.setOverlayAlpha(1f - percentage);
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
				mHeaderBitmapDrawable.setOverlayAlpha(0f);
			}
			if ((stateOne == TripBucketItemState.SHOWING_CHECKOUT_BUTTON || stateOne == TripBucketItemState.DEFAULT)
				&& stateTwo == TripBucketItemState.SHOWING_PRICE_CHANGE) {

				mBookBtnContainer.setAlpha(0.0f);
				setNameAndDurationSlidePercentage(1.0f);
				setExpandedSlidePercentage(1.0f);
				setPriceChangePercentage(1.0f);
				mHeaderBitmapDrawable.setOverlayAlpha(0f);
			}

			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.BOOKING_UNAVAILABLE) {
				mSoldOutContainer.setAlpha(1.0f);
				setNameAndDurationSoldOutSlidePercentage(0.0f);
				setExpandedSlidePercentage(0.0f);
				mHeaderBitmapDrawable.setOverlayAlpha(1f);

			}
			// Expanded, Price Change --> Collapsed
			if (stateOne == TripBucketItemState.EXPANDED && stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setAlpha(1.0f);
				setNameAndDurationSlidePercentage(0.0f);
				setExpandedSlidePercentage(0.0f);
				mHeaderBitmapDrawable.setOverlayAlpha(1f);
			}
			if (stateOne == TripBucketItemState.SHOWING_PRICE_CHANGE
				&& stateTwo == TripBucketItemState.SHOWING_CHECKOUT_BUTTON) {
				mBookBtnContainer.setAlpha(1.0f);
				setNameAndDurationSlidePercentage(0.0f);
				setExpandedSlidePercentage(0.0f);
				setPriceChangePercentage(0.0f);
				mHeaderBitmapDrawable.setOverlayAlpha(1f);
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
				mHeaderBitmapDrawable.setOverlayAlpha(0f);
			}

		}

		@Override
		public void onStateFinalized(TripBucketItemState state) {
			setVisibilityState(state);
		}
	};

	/*
	* Adjust UI changes to show if the sold out trip bucket card is selected or not.
	*/
	private void setItemSoldOutSelected(boolean isSelected) {
		// Let's desaturate the destination/hotel image
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0.0f);
		mTripBucketImageView.setColorFilter(new ColorMatrixColorFilter(cm));
		if (isSelected) {
			int padding = getResources().getDimensionPixelSize(R.dimen.trip_bucket_sold_out_container_padding);
			int paddingBottom = getResources()
				.getDimensionPixelSize(R.dimen.trip_bucket_sold_out_container_padding_bottom);
			mTopC.setPadding(padding, padding, padding, paddingBottom);
			mHeaderBitmapDrawable.setOverlayDrawable(mSoldOutSelectedOverlay);
		}
		else {
			mHeaderBitmapDrawable.setOverlayDrawable(mSoldOutUnSelectedOverlay);
			mTopC.setPadding(0, 0, 0, 0);
		}

	}

	protected void setVisibilityState(TripBucketItemState state) {
		switch (state) {
		case DEFAULT:
		case SHOWING_CHECKOUT_BUTTON:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.VISIBLE);
			mSoldOutContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.GONE);
			mPriceChangedC.setVisibility(View.GONE);
			setNameAndDurationSlidePercentage(0f);
			mHeaderBitmapDrawable.setOverlayAlpha(0f);
			break;

		case BOOKING_UNAVAILABLE:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mSoldOutContainer.setVisibility(View.VISIBLE);
			mExpandedC.setVisibility(View.GONE);
			mPriceChangedC.setVisibility(View.GONE);
			setNameAndDurationSlidePercentage(0f);
			mHeaderBitmapDrawable.setOverlayAlpha(0f);
			setItemSoldOutSelected(isSelected());
			break;

		case SHOWING_PRICE_CHANGE:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mSoldOutContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.VISIBLE);
			mPriceChangedC.setVisibility(View.VISIBLE);
			setNameAndDurationSlidePercentage(0f);
			mHeaderBitmapDrawable.setOverlayAlpha(1f);
			break;

		case DISABLED:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mSoldOutContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.GONE);
			mPriceChangedC.setVisibility(View.GONE);
			setNameAndDurationSlidePercentage(0f);
			mHeaderBitmapDrawable.setOverlayAlpha(0f);
			break;

		case EXPANDED:
			mBookingCompleteCheckImg.setVisibility(View.GONE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mSoldOutContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.VISIBLE);
			mPriceChangedC.setVisibility(View.GONE);
			setNameAndDurationSlidePercentage(1f);
			mHeaderBitmapDrawable.setOverlayAlpha(1f);
			break;

		case PURCHASED:
			mBookingCompleteCheckImg.setVisibility(View.VISIBLE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mSoldOutContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.GONE);
			mPriceChangedC.setVisibility(View.GONE);
			setNameAndDurationSlidePercentage(1f);
			mHeaderBitmapDrawable.setOverlayAlpha(0f);
			break;

		case CONFIRMATION:
			mBookingCompleteCheckImg.setVisibility(View.VISIBLE);
			mBookBtnContainer.setVisibility(View.INVISIBLE);
			mSoldOutContainer.setVisibility(View.GONE);
			mExpandedC.setVisibility(View.VISIBLE);
			mPriceChangedC.setVisibility(View.GONE);
			setNameAndDurationSlidePercentage(1f);
			mHeaderBitmapDrawable.setOverlayAlpha(0f);
			break;
		}
		mCardCornersBottom.setTranslationY(0f);
	}

	public void setNameAndDurationSoldOutSlidePercentage(float percentage) {
		int translationy = mSoldOutContainer.getBottom() - mNameAndDurationContainer.getBottom()
			+ mNameText.getTop(); // Vertical padding of text inside mNameAndDurationContainer
		mNameAndDurationContainer.setTranslationY(translationy * percentage);
	}

	public void setNameAndDurationSlidePercentage(float percentage) {
		int translationy = mBookBtnContainer.getBottom() - mNameAndDurationContainer.getBottom()
			+ mNameText.getTop(); // Vertical padding of text inside mNameAndDurationContainer
		mNameAndDurationContainer.setTranslationY(translationy * percentage);
	}

	public void setExpandedSlidePercentage(float percentage) {
		float amount = -mExpandedC.getHeight() * (1f - percentage);
		mExpandedC.setTranslationY(amount);
		mCardCornersBottom.setTranslationY(amount);
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
	* Convenience method to trigger "Book" button for trip bucket items.
	*/
	public void triggerTripBucketBookAction(LineOfBusiness lob) {
		setSelected(true);
		if (getTripBucketBookClickedListener() != null) {
			getTripBucketBookClickedListener().onTripBucketBookClicked(lob);
		}
	}

	/*
	ABSTRACT METHODS
	*/

	public abstract TripBucketItem getItem();

	public abstract CharSequence getBookButtonText();

	public abstract void addExpandedView(LayoutInflater inflater, ViewGroup viewGroup);

	public abstract void bindExpandedView(TripBucketItem item);

	public abstract void addTripBucketImage(ImageView imageView,
		HeaderBitmapColorAveragedDrawable headerBitmapDrawable);

	public abstract boolean doTripBucketImageRefresh();

	public abstract String getNameText();

	public abstract String getDateRangeText();

	public abstract CharSequence getTripPrice();

	public abstract OnClickListener getOnBookClickListener();

	public abstract boolean isSelected();

	public abstract void setSelected(boolean isSelected);

}
