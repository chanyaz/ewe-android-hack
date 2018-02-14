package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.animation.ResizeAnimator;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataAdapter;
import com.expedia.bookings.data.trips.ItinCardDataRails;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity;
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity;
import com.expedia.bookings.itin.activity.LegacyItinCardDataActivity;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.FrameLayout;
import com.expedia.bookings.widget.itin.ItinCard.OnItinCardClickListener;
import com.mobiata.android.Log;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Semaphore;

@SuppressWarnings("rawtypes")
public class ItinListView extends ListView implements OnItemClickListener, OnScrollListener, OnItinCardClickListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	public interface OnListModeChangedListener {
		void onListModeChanged(boolean isInDetailMode, boolean animated);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final String STATE_DO_AUTOSCROLL = "STATE_DO_AUTOSCROLL";
	private static final String STATE_DEFAULT_SAVESTATE = "STATE_DEFAULT_SAVESTATE";
	private static final String STATE_LAST_ITEM_COUNT = "STATE_LAST_ITEM_COUNT";
	private static final String STATE_SELECTED_CARD_ID = "STATE_SELECTED_CARD_ID";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinCardDataAdapter mAdapter;

	private String mSelectedCardId;

	private OnItemClickListener mOnItemClickListener;
	private OnScrollListener mOnScrollListener;

	private View mLastChild = null;
	private boolean mWasChildConsumedTouch = false;

	private int mScrollState = SCROLL_STATE_IDLE;
	private boolean mScrollToReleventOnDataSetChange;

	private int mLastItemCount = 0;

	private Semaphore mModeSwitchSemaphore = new Semaphore(1);
	Queue<Runnable> mUiQueue = new LinkedList<Runnable>();

	private FooterView mFooterView;

	private int mExpandedHeight = 0;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinListView(Context context) {
		this(context, null);
	}

	public ItinListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ItinListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mAdapter = new ItinCardDataAdapter(context);
		mAdapter.setOnItinCardClickListener(this);
		mAdapter.syncWithManager();

		// We have a footer view taking up blank space presumably so that the last card
		// in the list has room to expand smoothly. We'll increase its height upon showDetails()
		// and decrease its height back to 0 upon hideDetails().
		mFooterView = new FooterView(context);
		addFooterView(mFooterView);

		setAdapter(mAdapter);
		setOnItemClickListener(null);
		setOnScrollListener(null);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		int oldExpandedHeight = mExpandedHeight;
		mExpandedHeight = Math.max(h, oldh);
		Log.d("ItinListView.onSizeChanged mExpandedHeight - oldValue:" + oldExpandedHeight + " newValue:" + mExpandedHeight);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinCardDataAdapter getItinCardDataAdapter() {
		return mAdapter;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable(STATE_DEFAULT_SAVESTATE, super.onSaveInstanceState());
		bundle.putBoolean(STATE_DO_AUTOSCROLL, mScrollToReleventOnDataSetChange);
		bundle.putInt(STATE_LAST_ITEM_COUNT, mLastItemCount);
		bundle.putString(STATE_SELECTED_CARD_ID, mSelectedCardId);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle && ((Bundle) state).containsKey(STATE_DEFAULT_SAVESTATE)) {
			Bundle bundle = (Bundle) state;
			super.onRestoreInstanceState(bundle.getParcelable(STATE_DEFAULT_SAVESTATE));
			mScrollToReleventOnDataSetChange = bundle.getBoolean(STATE_DO_AUTOSCROLL, true);
			mLastItemCount = bundle.getInt(STATE_LAST_ITEM_COUNT, 0);
			setSelectedCardId(bundle.getString(STATE_SELECTED_CARD_ID));
		}
		else {
			super.onRestoreInstanceState(state);
		}
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		unregisterDataSetObserver();
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		registerDataSetObserver();
		mAdapter.syncWithManager();
	}

	@Override
	public void setOnItemClickListener(OnItemClickListener listener) {
		mOnItemClickListener = listener;
		super.setOnItemClickListener(this);
	}

	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		mOnScrollListener = listener;
		super.setOnScrollListener(this);
	}

	// Touch overrides
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		//If we are in list mode, some shit goes down.
		//We want the appropriate action to reach the appropriate target.
		//Thus when we touch a card and drag so our finger is no longer on the card
		//we send ACTION_DOWN to the card, all of the ACTION_MOVES that occur on the card
		//also get sent there, however, when we are no longer over the card
		//we send ACTION_CANCEL to the card, and ACTION_DOWN to the list (along with the rest of the events).
		//We try to handle all events like this, sending ACTION_CANCEL when we aren't above old views anymore,
		//and ACTION_DOWNS to the new touch targets.

		boolean isTouchDown = event.getAction() == MotionEvent.ACTION_DOWN;
		boolean isTouchUp = event.getAction() == MotionEvent.ACTION_UP;
		boolean isChildConsumedTouch = false;

		View child = findMotionView((int) event.getY());

		MotionEvent childEvent = MotionEvent.obtain(event);
		if (child != null) {
			childEvent.offsetLocation(0, -child.getTop());
			if (child.findViewById(R.id.add_guest_itin_text_view) != null) {
				isChildConsumedTouch = true;
			}
			else if (child instanceof ItinButtonCard
				|| (child instanceof ItinAirAttachCard
				&& (((ItinAirAttachCard) child).isTouchOnAirAttachButton(childEvent)
				|| ((ItinAirAttachCard) child).isTouchOnDismissButton(childEvent)))
				|| (child instanceof ItinCard && (((ItinCard) child).isTouchOnSummaryButtons(childEvent)
				|| ((ItinCard) child).isTouchOnCheckInButton(childEvent)))) {

				isChildConsumedTouch = true;
			}
		}

		MotionEvent downChildEvent = MotionEvent.obtain(event);
		if (mLastChild != null) {
			downChildEvent.offsetLocation(0, -mLastChild.getTop());
		}

		if (isTouchDown) {
			if (isChildConsumedTouch) {
				sendEventToView(childEvent, child);
				mLastChild = child;
			}
			else {
				onTouchEventSafe(event);
			}
		}
		else if (isTouchUp) {
			if (isChildConsumedTouch && mWasChildConsumedTouch) {
				if (child == mLastChild) {
					alterEventActionAndSendToView(childEvent, MotionEvent.ACTION_UP, child);
				}
				else {
					alterEventActionAndSendToView(downChildEvent, MotionEvent.ACTION_CANCEL, mLastChild);
					alterEventActionAndSendToView(childEvent, MotionEvent.ACTION_UP, child);
				}
			}
			else if (isChildConsumedTouch) {
				alterEventActionAndSendToView(childEvent, MotionEvent.ACTION_UP, child);
				alterEventActionAndFireTouchEvent(event, MotionEvent.ACTION_CANCEL);
			}
			else if (mWasChildConsumedTouch) {
				if (mLastChild != null) {
					alterEventActionAndSendToView(downChildEvent, MotionEvent.ACTION_CANCEL, mLastChild);
				}
				alterEventActionAndFireTouchEvent(event, MotionEvent.ACTION_UP);
			}
			else {
				onTouchEventSafe(event);
			}
			mLastChild = null;
		}
		else {
			if (isChildConsumedTouch && mWasChildConsumedTouch) {
				if (child == mLastChild) {
					sendEventToView(childEvent, child);
				}
				else {
					alterEventActionAndSendToView(downChildEvent, MotionEvent.ACTION_CANCEL, mLastChild);
					alterEventActionAndSendToView(childEvent, MotionEvent.ACTION_DOWN, child);
					mLastChild = child;
				}
			}
			else if (!isChildConsumedTouch && !mWasChildConsumedTouch) {
				onTouchEventSafe(event);
			}
			else if (isChildConsumedTouch) {
				alterEventActionAndFireTouchEvent(event, MotionEvent.ACTION_CANCEL);
				alterEventActionAndSendToView(childEvent, MotionEvent.ACTION_DOWN, child);
				mLastChild = child;
			}
			else if (mWasChildConsumedTouch) {
				if (mLastChild != null) {
					alterEventActionAndSendToView(downChildEvent, MotionEvent.ACTION_CANCEL, mLastChild);
				}
				alterEventActionAndFireTouchEvent(event, MotionEvent.ACTION_DOWN);
				mLastChild = null;
			}
			else {
				alterEventActionAndSendToView(childEvent, MotionEvent.ACTION_CANCEL, child);
				alterEventActionAndFireTouchEvent(event, MotionEvent.ACTION_DOWN);
				mLastChild = null;
			}
		}

		mWasChildConsumedTouch = !isTouchUp && isChildConsumedTouch;

		downChildEvent.recycle();
		childEvent.recycle();

		return true;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (mScrollState == SCROLL_STATE_IDLE) {
			return onTouchEvent(ev);
		}

		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public void onDraw(Canvas canvas) {
		//Draw the views
		super.onDraw(canvas);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void syncWithManager() {
		if (mAdapter == null) {
			return;
		}

		// This will trigger our DataSetObserver
		mAdapter.syncWithManager();
	}

	/**
	 * Calling this function will cause the list to be scrolled to the most relevant position the next time the data set changes
	 * if the previous data set contained 0 items. So when we first load up itins, scroll to our good position, otherwise dont
	 */
	public void enableScrollToRevelentWhenDataSetChanged() {
		mScrollToReleventOnDataSetChange = true;
	}

	public ItinCardData getItinCardData(int position) {
		return mAdapter.getItem(position);
	}

	public ItinCardData getSelectedItinCard() {
		int pos = mAdapter.getPosition(mSelectedCardId);
		if (pos != -1) {
			return mAdapter.getItem(pos);
		}
		return null;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	//Touch Helpers

	private boolean sendEventToView(MotionEvent event, View view) {
		return view.dispatchTouchEvent(event);
	}

	private boolean alterEventActionAndSendToView(MotionEvent event, int action, View view) {
		event.setAction(action);
		return sendEventToView(event, view);
	}

	private boolean onTouchEventSafe(MotionEvent event) {
		try {
			//This sometimes throws ArrayIndexOutOfBounds on 2.x. when we are fast scrolling. Cause unclear.
			return super.onTouchEvent(event);
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			Log.w("ArrayIndexOutOfBoundsException in ItinListView.onTouchEvent()", ex);
		}
		return false;
	}

	private boolean alterEventActionAndFireTouchEvent(MotionEvent event, int action) {
		event.setAction(action);
		return onTouchEventSafe(event);
	}

	private View findMotionView(int y) {
		int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			if (y <= v.getBottom()) {
				return v;
			}
		}
		return null;
	}

	private void setSelectedCardId(String cardId) {
		mSelectedCardId = cardId;
		mAdapter.setSelectedCardId(cardId);
	}

	public void hideDetails() {
	}

	public void showDetails(String id) {
		showDetails(mAdapter.getPosition(id));
	}

	@SuppressWarnings("unchecked")
	private void showDetails(final int position) {
		// Invalid index
		if (position < 0 || position >= mAdapter.getCount()) {
			return;
		}

		ItinCardData data = mAdapter.getItem(position);
		getContext().startActivity(LegacyItinCardDataActivity.createIntent(getContext(), data.getId()),
				ActivityOptionsCompat
						.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete)
						.toBundle());
	}

	private void registerDataSetObserver() {
		mAdapter.registerDataSetObserver(mDataSetObserver);
	}

	private void unregisterDataSetObserver() {
		mAdapter.unregisterDataSetObserver(mDataSetObserver);
	}

	/**
	 * Asks the adapter for the most relevent card and scrolls to it.
	 * @return the position scrolled to ( < 0 if invalid )
	 */
	private int scrollToMostRelevantCard() {
		if (mAdapter == null) {
			return -1;
		}

		final int pos = mAdapter.getMostRelevantCardPosition();
		if (pos >= 0) {
			post(new Runnable() {
				@Override
				public void run() {
					smoothScrollToPositionFromTop(pos, 0);
				}
			});
		}
		return pos;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ItinCardData data = mAdapter.getItem(position);

		Boolean isFlightItinCardDetailBucketed = AbacusFeatureConfigManager.isBucketedForTest(getContext(), AbacusUtils.TripsFlightsNewDesign);

		if (data != null) {
			if (view instanceof ItinButtonCard) {
				// Do nothing
			}
			else if (view instanceof ItinAirAttachCard) {
				return;
			}
			else if (data instanceof ItinCardDataRails) {
				openItinInWebView(data.getDetailsUrl());
			}
			else if (data.hasDetailData() && data.getTripComponentType() == TripComponent.Type.HOTEL) {
				getContext().startActivity(HotelItinDetailsActivity.createIntent(getContext(), data.getId()),
					ActivityOptionsCompat
						.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete)
						.toBundle());
			}
			else if (data.hasDetailData() && data.getTripComponentType() == TripComponent.Type.FLIGHT && isFlightItinCardDetailBucketed) {
				getContext().startActivity(FlightItinDetailsActivity.createIntent(getContext(), data.getId()),
						ActivityOptionsCompat
							.makeCustomAnimation(getContext(), R.anim.slide_in_right, R.anim.slide_out_left_complete)
							.toBundle());
			}
			else if (data.hasDetailData()) {
				showDetails(position);
			}
			else  {
				Log.w("ItinCard fallback clicked");
				if (!TextUtils.isEmpty(data.getDetailsUrl())) {
					openItinInWebView(data.getDetailsUrl());
				}
			}

			if (mOnItemClickListener != null) {
				mOnItemClickListener.onItemClick(parent, view, position, id);
			}
		}
	}

	private void openItinInWebView(String webDetailsURL) {
		Context context = getContext();
		WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
		builder.setUrl(webDetailsURL);
		builder.setTitle(R.string.itinerary);
		builder.setInjectExpediaCookies(true);
		builder.setAllowMobileRedirects(false);
		context.startActivity(builder.getIntent());
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		for (int i = 0; i < visibleItemCount; i++) {
			getChildAt(i).invalidate();
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollState = scrollState;

		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
	}

	@Override
	public void onCloseButtonClicked() {
		hideDetails();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASS INSTANCES
	//////////////////////////////////////////////////////////////////////////////////////

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		public void onChanged() {
			onDataSetChanged();
		}
	};

	private void onDataSetChanged() {

		if (!mModeSwitchSemaphore.tryAcquire()) {
			mUiQueue.add(new Runnable() {
				public void run() {
					onDataSetChanged();
				}
			});
			return;
		}

		synchronizedOnDataSetChanged();
	}

	private void synchronizedOnDataSetChanged() {
		if (mScrollToReleventOnDataSetChange || mLastItemCount <= 0) {
			if (scrollToMostRelevantCard() >= 0) {
				mScrollToReleventOnDataSetChange = false;
			}
		}

		mLastItemCount = mAdapter.getCount();

		// Draw the background line
		// We want our background line to be refreshed, but not until after
		// the list draws, 250 will usually be the right amount of time to delay.
		// This isn't a great solution, but the line is totally non-critical.
		postDelayed(new Runnable() {
			public void run() {
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}
		}, 250);

		releaseSemaphore();
	}

	private void releaseSemaphore() {
		mModeSwitchSemaphore.release();
		Ui.runOnNextLayout(this, new Runnable() {
			public void run() {
				if (!mUiQueue.isEmpty()) {
					mUiQueue.poll().run();
				}
			}
		});
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	private class FooterView extends FrameLayout {
		private boolean mHasDrawn = false;
		private View mStretchyView;

		public FooterView(Context context) {
			super(context);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			setLayoutParams(params);
			setFocusable(true);

			mStretchyView = new View(context);
			mStretchyView.setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, 0));
			addView(mStretchyView);
		}

		public void setHeight(int height) {
			ResizeAnimator.setHeight(mStretchyView, height);
			mHasDrawn = false;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (!mHasDrawn) {
				mHasDrawn = true;
			}
		}
	}
}
