package com.expedia.bookings.widget.itin;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityOptionsCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.data.abacus.AbacusUtils;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataAdapter;
import com.expedia.bookings.data.trips.ItinCardDataRails;
import com.expedia.bookings.data.trips.TripComponent;
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager;
import com.expedia.bookings.itin.activity.FlightItinDetailsActivity;
import com.expedia.bookings.itin.activity.HotelItinDetailsActivity;
import com.expedia.bookings.itin.activity.LegacyItinCardDataActivity;
import com.mobiata.android.Log;

@SuppressWarnings("rawtypes")
public class ItinListView extends ListView implements OnItemClickListener {

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final String STATE_DO_AUTOSCROLL = "STATE_DO_AUTOSCROLL";
	private static final String STATE_DEFAULT_SAVESTATE = "STATE_DEFAULT_SAVESTATE";
	private static final String STATE_LAST_ITEM_COUNT = "STATE_LAST_ITEM_COUNT";

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinCardDataAdapter mAdapter;

	private OnItemClickListener mOnItemClickListener;

	private View mLastChild = null;
	private boolean mWasChildConsumedTouch = false;

	private boolean mScrollToRelevantOnDataSetChange;

	private int mLastItemCount = 0;

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
		mAdapter.syncWithManager();

		setAdapter(mAdapter);
		setOnItemClickListener(null);
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
		bundle.putBoolean(STATE_DO_AUTOSCROLL, mScrollToRelevantOnDataSetChange);
		bundle.putInt(STATE_LAST_ITEM_COUNT, mLastItemCount);
		return bundle;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		if (state instanceof Bundle && ((Bundle) state).containsKey(STATE_DEFAULT_SAVESTATE)) {
			Bundle bundle = (Bundle) state;
			super.onRestoreInstanceState(bundle.getParcelable(STATE_DEFAULT_SAVESTATE));
			mScrollToRelevantOnDataSetChange = bundle.getBoolean(STATE_DO_AUTOSCROLL, true);
			mLastItemCount = bundle.getInt(STATE_LAST_ITEM_COUNT, 0);
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
				child.dispatchTouchEvent(childEvent);
				mLastChild = child;
			}
			else {
				super.onTouchEvent(event);
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
				super.onTouchEvent(event);
			}
			mLastChild = null;
		}
		else {
			if (isChildConsumedTouch && mWasChildConsumedTouch) {
				if (child == mLastChild) {
					child.dispatchTouchEvent(childEvent);
				}
				else {
					alterEventActionAndSendToView(downChildEvent, MotionEvent.ACTION_CANCEL, mLastChild);
					alterEventActionAndSendToView(childEvent, MotionEvent.ACTION_DOWN, child);
					mLastChild = child;
				}
			}
			else if (!isChildConsumedTouch && !mWasChildConsumedTouch) {
				super.onTouchEvent(event);
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
		return onTouchEvent(ev);
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
	public void enableScrollToRelevantWhenDataSetChanged() {
		mScrollToRelevantOnDataSetChange = true;
	}

	public ItinCardData getItinCardData(int position) {
		return mAdapter.getItem(position);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	//Touch Helpers

	private void alterEventActionAndSendToView(MotionEvent event, int action, View view) {
		event.setAction(action);
		view.dispatchTouchEvent(event);
	}

	private void alterEventActionAndFireTouchEvent(MotionEvent event, int action) {
		event.setAction(action);
		super.onTouchEvent(event);
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

	public void showDetails(String id) {
		showDetails(mAdapter.getPosition(id));
	}

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
	 * fAsks the adapter for the most relevant card and scrolls to it.
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

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASS INSTANCES
	//////////////////////////////////////////////////////////////////////////////////////

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		public void onChanged() {
			onDataSetChanged();
		}
	};

	private void onDataSetChanged() {
		if (mScrollToRelevantOnDataSetChange || mLastItemCount <= 0) {
			if (scrollToMostRelevantCard() >= 0) {
				mScrollToRelevantOnDataSetChange = false;
			}
		}

		mLastItemCount = mAdapter.getCount();
	}
}
