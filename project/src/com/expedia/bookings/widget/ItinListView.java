package com.expedia.bookings.widget;

import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;
import com.expedia.bookings.animation.AnimatorListenerShort;
import com.expedia.bookings.animation.ResizeAnimator;
import com.expedia.bookings.data.trips.ItinCardData;
import com.expedia.bookings.data.trips.ItinCardDataAdapter;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.ItinCard.OnItinCardClickListener;
import com.expedia.bookings.widget.itin.ItinContentGenerator;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

@SuppressWarnings("rawtypes")
public class ItinListView extends ListView implements OnItemClickListener, OnScrollListener, OnItinCardClickListener {
	//////////////////////////////////////////////////////////////////////////////////////
	// INTERFACES
	//////////////////////////////////////////////////////////////////////////////////////

	public interface OnListModeChangedListener {
		public void onListModeChanged(int mode);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final String STATE_DO_AUTOSCROLL = "STATE_DO_AUTOSCROLL";
	private static final String STATE_DEFAULT_SAVESTATE = "STATE_DEFAULT_SAVESTATE";
	private static final String STATE_LAST_ITEM_COUNT = "STATE_LAST_ITEM_COUNT";
	private static final String STATE_SELECTED_CARD_ID = "STATE_SELECTED_CARD_ID";

	public static final int SCROLL_HEADER_HIDDEN = -9999;

	public static final int MODE_LIST = 0;
	public static final int MODE_DETAIL = 1;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinCardDataAdapter mAdapter;

	private String mSelectedCardId;

	private ItinCard mDetailsCard;

	private OnItemClickListener mOnItemClickListener;
	private OnScrollListener mOnScrollListener;
	private OnListModeChangedListener mOnListModeChangedListener;
	private OnItinCardClickListener mOnItinCardClickListener;

	private int mMode = MODE_LIST;

	private View mLastChild;

	private int mScrollState = SCROLL_STATE_IDLE;
	private int mDetailPosition = -1;
	private int mOriginalScrollY;
	private boolean mScrollToReleventOnDataSetChange;

	private int mExpandedCardHeight;
	private int mExpandedCardOriginalHeight;
	private int mLastItemCount = 0;

	private Semaphore mModeSwitchSemaphore = new Semaphore(1);

	private FooterView mFooterView;
	private View mFooterVisibilityView;

	// If true, there's a second pane which handles showing card details.  Don't expand cards when clicked.
	private boolean mSimpleMode = false;

	//Path vars
	private Paint mPathViewPaint;
	private int mPathStartY = -1;
	private int mPathStopY = -1;
	private int mPathX = -1;
	private int mPathColor = Color.WHITE;
	private int mPathWidth = 4;

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

		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ItinListView, 0, 0);
			if (ta.hasValue(R.styleable.ItinListView_pathViewColor)) {
				mPathColor = ta.getColor(R.styleable.ItinListView_pathViewColor, Color.WHITE);
			}
			if (ta.hasValue(R.styleable.ItinListView_pathViewWidth)) {
				mPathWidth = ta.getDimensionPixelSize(R.styleable.ItinListView_pathViewWidth, 2);
			}

			ta.recycle();
		}

		mAdapter = new ItinCardDataAdapter(context);
		mAdapter.setOnItinCardClickListener(this);
		mAdapter.syncWithManager();

		if (AndroidUtils.getSdkVersion() < 11) {
			// We add a dummy footer view, if we dont do this before setAdapter future calls to addFooterView wont
			// have their views accounted for when measuring
			mFooterVisibilityView = new View(getContext());
			AbsListView.LayoutParams spacerViewParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 1);
			mFooterVisibilityView.setLayoutParams(spacerViewParams);
			addFooterView(mFooterVisibilityView);
		}

		setAdapter(mAdapter);
		setOnItemClickListener(null);
		setOnScrollListener(null);

		mPathViewPaint = new Paint();
		mPathViewPaint.setColor(mPathColor);
		mPathViewPaint.setStrokeWidth(mPathWidth);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

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
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		final int scrollY = getScrollY();

		super.onWindowFocusChanged(hasWindowFocus);

		scrollTo(0, scrollY);
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
		if (mMode == MODE_DETAIL) {
			if (mDetailsCard != null) {
				boolean retVal = mDetailsCard.dispatchTouchEvent(event);
				return retVal;
			}
			else {
				return super.onTouchEvent(event);
			}
		}

		View child = findMotionView((int) event.getY());
		if (child != null) {
			MotionEvent childEvent = MotionEvent.obtain(event);
			childEvent.offsetLocation(0, -child.getTop());

			if (mLastChild != null && mLastChild != child) {
				MotionEvent actionOutEvent = MotionEvent.obtain(childEvent);
				actionOutEvent.offsetLocation(0, mLastChild.getHeight());
				mLastChild.dispatchTouchEvent(actionOutEvent);
				actionOutEvent.recycle();

				Log.t("Tried moving outside last child");
			}

			mLastChild = child;

			if (child.dispatchTouchEvent(childEvent)) {
				childEvent.recycle();
				return true;
			}

			childEvent.recycle();
		}

		try {
			//This sometimes throws ArrayIndexOutOfBounds on 2.x. when we are fast scrolling. Cause unclear.
			return super.onTouchEvent(event);
		}
		catch (ArrayIndexOutOfBoundsException ex) {
			Log.w("ArrayIndexOutOfBoundsException in ItinListView.onTouchEvent()", ex);
		}
		return false;
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
		//Draw the path behind the views
		if (mMode == MODE_LIST) {
			drawPathView(canvas);
		}

		//Draw the views
		super.onDraw(canvas);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void setExpandedCardHeight(int height) {
		mExpandedCardHeight = height;
	}

	public int getMode() {
		return mMode;
	}

	public void setMode(int mode) {
		switch (mode) {
		case MODE_DETAIL: {
			showDetails();
			break;
		}
		case MODE_LIST:
		default: {
			hideDetails();
			break;
		}
		}
	}

	public void setOnListModeChangedListener(OnListModeChangedListener onListModeChangedListener) {
		mOnListModeChangedListener = onListModeChangedListener;
	}

	public void setOnItinCardClickListener(OnItinCardClickListener onItinCardClickListener) {
		mOnItinCardClickListener = onItinCardClickListener;
	}

	public void syncWithManager() {
		if (mAdapter != null) {
			// Grab the data of the expanded card before sync, otherwise there may be a disparity between the index of
			// the expanded card and the data that exists in the adapter (think about if a trip gets removed or a new
			// trip is added)
			ItinCardData expandedCardData = null;
			if (mDetailPosition != -1) {
				expandedCardData = mAdapter.getItem(mDetailPosition);
			}

			mAdapter.syncWithManager();

			if (expandedCardData != null) {
				String expandedCardId = expandedCardData.getTripComponent().getUniqueId();

				boolean tripExists = false;
				for (ItinCardData updatedCard : ItineraryManager.getInstance().getItinCardData()) {
					String updatedCardId = updatedCard.getTripComponent().getUniqueId();
					if (expandedCardId.equals(updatedCardId)) {
						tripExists = true;
						break;
					}
				}

				if (tripExists) {
					mDetailsCard = (ItinCard) getFreshDetailView(mDetailPosition);
					mDetailsCard.expand(false);
					mDetailsCard.requestLayout();
				}
				else {
					hideDetails();
				}
			}
		}
	}

	/**
	 * Calling this function will cause the list to be scrolled to the most relevant position the next time the data set changes
	 * if the previous data set contained 0 items. So when we first load up itins, scroll to our good position, otherwise dont
	 */
	public void enableScrollToRevelentWhenDataSetChanged() {
		mScrollToReleventOnDataSetChange = true;
	}

	public void setSimpleMode(boolean enabled) {
		mSimpleMode = enabled;
		mAdapter.setSimpleMode(enabled);
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

	//Draw helper for the line background view
	private void drawPathView(Canvas canvas) {
		if (mPathViewPaint != null) {
			updateLinePosition();

			if (mPathStartY >= 0 && mPathStopY > mPathStartY && mPathX >= 0) {
				canvas.drawLine(mPathX, mPathStartY, mPathX, mPathStopY, mPathViewPaint);
			}
		}
	}

	private void updateLinePosition() {
		//Set x pos to the middle
		mPathX = getWidth() / 2;

		//Find where the line should start
		int firstChildIndex = 0;
		View firstChildView = this.getChildAt(firstChildIndex);
		if (this.getFirstVisiblePosition() == 0 && firstChildView != null) {
			//If the first visible view is 0, then this is the top, so line stops behind it
			mPathStartY = Math.max(0, firstChildView.getTop() + (firstChildView.getHeight() / 2));
		}
		else {
			//Otherwise we have veiws above, so we go to the top
			mPathStartY = 0;
		}

		//Find where the line should end
		if (getLastVisiblePosition() < (getCount() - 1 - getFooterViewsCount())) {
			//If there are views below the screen fold, go all the way to the bottom.
			mPathStopY = getHeight();
		}
		else {
			//Otherwise find the last view, and stop behind that dude
			int lastChildIndex = getChildCount() - 1;
			View lastChildView = this.getChildAt(lastChildIndex);
			while (lastChildIndex > firstChildIndex && !(lastChildView instanceof ItinCard)) {
				//Sometimes we have footers, so we need to get the last ItinCard by looping 
				//Note: Usually we dont enter the loop, when we do it should usually run one time
				lastChildIndex--;
				lastChildView = getChildAt(lastChildIndex);
			}
			if (lastChildView != null) {
				mPathStopY = lastChildView.getTop() + (lastChildView.getHeight() / 2);
			}
			else {
				mPathStopY = getHeight();
			}
		}
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

	private View getFreshDetailView(int position) {
		int start = getFirstVisiblePosition();
		View view = getChildAt(position - start);
		if (view != null) {
			if (AndroidUtils.getSdkVersion() < 11) {
				//This could use some more investigation and possibly a more all around solution
				// 2.x needs this because otherwise all of the listview rows use the same view and they all expand which bones our animation
				// 4.x breaks from this because the adapter decides that it should call measure mid animation - the last details card was collapsed
				//	   after we used it, so it thinks that the height of the thing should be non-expanded height our animation gets boned
				mAdapter.setDetailPosition(position);
			}

			return mAdapter.getView(position, view, this);
		}
		return null;
	}

	private void setSelectedCardId(String cardId) {
		mSelectedCardId = cardId;
		mAdapter.setSelectedCardId(cardId);
	}

	private void clearDetailView() {
		mDetailPosition = -1;
		mDetailsCard = null;
		mAdapter.setDetailPosition(-1);
		setSelectedCardId(null);
	}

	public boolean hideDetails() {
		if (mSimpleMode) {
			setSelectedCardId(null);
			mAdapter.notifyDataSetChanged();

			return false;
		}

		boolean releaseSemHere = true;
		boolean semGot = false;
		try {
			if (mModeSwitchSemaphore.tryAcquire()) {
				semGot = true;
				if (mDetailPosition < 0 || mDetailsCard == null) {
					return false;
				}

				mMode = MODE_LIST;
				if (mOnListModeChangedListener != null) {
					mOnListModeChangedListener.onListModeChanged(mMode);
				}

				if (AndroidUtils.getSdkVersion() < 11) {
					removeFooterView(mFooterView);
					mFooterView = null;
				}

				final int startY = getScrollY();
				final int stopY = mOriginalScrollY;

				ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCard,
						mExpandedCardOriginalHeight);
				resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
					@Override
					public void onAnimationUpdate(ValueAnimator arg0) {
						scrollTo(0, (int) (((stopY - startY) * arg0.getAnimatedFraction()) + startY));
						onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
					}
				});

				AnimatorSet detailExpandAnim = mDetailsCard.collapse(false);
				AnimatorSet set = new AnimatorSet();
				set.playTogether(resizeAnimator, detailExpandAnim);

				set.addListener(new AnimatorListenerShort() {

					@Override
					public void onAnimationEnd(Animator arg0) {
						scrollTo(0, stopY);
						onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
						mDetailsCard.getLayoutParams().height = mExpandedCardOriginalHeight;
						mDetailsCard.requestLayout();

						clearDetailView();
						invalidateViews();
					}

				});

				set.addListener(mModeSwitchSemListener);
				set.start();
				releaseSemHere = false;
				return true;
			}
		}
		finally {
			if (releaseSemHere && semGot) {
				mModeSwitchSemaphore.release();
			}
		}
		return false;
	}

	public void showDetails(String id) {
		showDetails(mAdapter.getPosition(id));
	}

	private void showDetails() {
		showDetails(mDetailPosition);
	}

	private void showDetails(boolean animate) {
		showDetails(mDetailPosition, animate);
	}

	private boolean showDetails(int position) {
		return showDetails(position, true);
	}

	@SuppressLint("NewApi")
	private boolean showDetails(int position, boolean animate) {
		if (mSimpleMode) {
			setSelectedCardId(mAdapter.getItem(position).getId());
			mAdapter.notifyDataSetChanged();

			return false;
		}

		boolean releaseSemHere = true;
		boolean semGot = false;
		try {
			if (mModeSwitchSemaphore.tryAcquire()) {
				semGot = true;

				View view = getFreshDetailView(position);
				if (!(view instanceof ItinCard)) {
					return false;
				}

				mDetailsCard = (ItinCard) view;
				if (mDetailsCard == null || !mDetailsCard.hasDetails()) {
					return false;
				}

				mDetailPosition = position;
				mMode = MODE_DETAIL;
				setSelectedCardId(mAdapter.getItem(position).getId());
				if (mOnListModeChangedListener != null) {
					mOnListModeChangedListener.onListModeChanged(mMode);
				}

				mExpandedCardHeight = mExpandedCardHeight > getHeight() ? mExpandedCardHeight : getHeight();
				mExpandedCardOriginalHeight = mDetailsCard.getHeight();
				mOriginalScrollY = getScrollY();

				final int startY = getScrollY();
				final int stopY = mDetailsCard.getTop();

				ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCard, mExpandedCardHeight);
				if (AndroidUtils.getSdkVersion() >= 11) {
					resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator arg0) {
							scrollTo(0, (int) (((stopY - startY) * arg0.getAnimatedFraction()) + startY));
							onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
						}

					});
				}
				else {
					//If our expanding views are at the bottom of the list  we need to add a footer view to make room for the expanded view on 2.x
					this.setSelectionFromTop(mDetailPosition, 0);

					int lastViewPos = getCount() - 1;
					int firstVisiblePos = getFirstVisiblePosition();
					int lastVisiblePos = getLastVisiblePosition();

					//If we are not yet scrolled into position, or all rows are on screen, add our footer view
					if (firstVisiblePos != mDetailPosition || ((getCount() - 1) == (lastVisiblePos - firstVisiblePos))) {
						if (mFooterView == null) {
							//footerview calls showDetails again in onDraw
							mFooterView = new FooterView(getContext());
							AbsListView.LayoutParams spacerViewParams = new AbsListView.LayoutParams(
									LayoutParams.MATCH_PARENT, mExpandedCardHeight);
							mFooterView.setLayoutParams(spacerViewParams);
							mFooterView.setFocusable(true);
							addFooterView(mFooterView);
						}
						if (firstVisiblePos != mDetailPosition) {
							//If we aren't scrolled to where we need to be, we continue calling showDetails until we are
							Runnable showDetailsRunner = new Runnable() {
								@Override
								public void run() {
									showDetails();
								}
							};
							this.postDelayed(showDetailsRunner, 25);
							return false;
						}
					}

					//If we are scrolled down but our footer still hasn't drawn, we wait
					if (lastVisiblePos == lastViewPos && mFooterView != null && !mFooterView.getHasDrawn()) {
						return false;
					}
				}

				AnimatorSet set = new AnimatorSet();
				AnimatorSet detailExpandAnim = mDetailsCard.expand(false);
				set.playTogether(resizeAnimator, detailExpandAnim);
				set.addListener(new AnimatorListener() {

					@Override
					public void onAnimationCancel(Animator arg0) {
					}

					@Override
					public void onAnimationEnd(Animator arg0) {
						mDetailsCard.getLayoutParams().height = mExpandedCardHeight;
						mDetailsCard.requestLayout();

						if (mDetailsCard != null) {
							switch (mDetailsCard.getType()) {
							case CAR:
								OmnitureTracking.trackItinCar(getContext());
								break;
							case FLIGHT:
								OmnitureTracking.trackItinFlight(getContext());
								break;
							case HOTEL:
								OmnitureTracking.trackItinHotel(getContext());
								break;
							case ACTIVITY:
								OmnitureTracking.trackItinActivity(getContext());
								break;
							}
						}
					}

					@Override
					public void onAnimationRepeat(Animator arg0) {
					}

					@Override
					public void onAnimationStart(Animator arg0) {
						onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
					}

				});

				set.addListener(mModeSwitchSemListener);
				if (!animate) {
					set.setDuration(0);
				}
				set.start();
				releaseSemHere = false;
				return true;
			}
		}
		finally {
			if (releaseSemHere && semGot) {
				mModeSwitchSemaphore.release();
			}
		}
		return false;
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
		if (mAdapter != null) {
			final int pos = mAdapter.getMostRelevantCardPosition();
			if (pos >= 0) {
				Runnable runner = new Runnable() {
					@SuppressLint("NewApi")
					@Override
					public void run() {
						if (ItinListView.this != null && ItinListView.this.mMode == MODE_LIST) {
							if (AndroidUtils.getSdkVersion() >= 11) {
								ItinListView.this.smoothScrollToPositionFromTop(pos, 0);
							}
							else {
								ItinListView.this.setSelectionFromTop(pos, 0);
							}
						}
					}
				};
				ItinListView.this.post(runner);
			}
			return pos;
		}
		return -1;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ItinCardData data = mAdapter.getItem(position);
		Intent clickIntent = data.getClickIntent(getContext());
		if (data.hasDetailData()) {
			showDetails(position);
		}
		else if (clickIntent != null) {
			getContext().startActivity(clickIntent);
		}
		else if (!TextUtils.isEmpty(data.getDetailsUrl())) {
			Context context = getContext();
			WebViewActivity.IntentBuilder builder = new WebViewActivity.IntentBuilder(context);
			builder.setUrl(data.getDetailsUrl());
			builder.setTitle(R.string.itinerary);
			builder.setTheme(R.style.ItineraryTheme);
			builder.setInjectExpediaCookies(true);
			builder.setAllowMobileRedirects(false);
			context.startActivity(builder.getIntent());
		}

		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(parent, view, position, id);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
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

		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onCloseButtonClicked();
		}
	}

	@Override
	public void onShareButtonClicked(ItinContentGenerator<?> generator) {
		if (mOnItinCardClickListener != null) {
			mOnItinCardClickListener.onShareButtonClicked(generator);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASS INSTANCES
	//////////////////////////////////////////////////////////////////////////////////////

	private Runnable mSelectCardRunnable;

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			if (mScrollToReleventOnDataSetChange || mLastItemCount <= 0) {
				if (scrollToMostRelevantCard() >= 0) {
					mScrollToReleventOnDataSetChange = false;
				}
			}

			mLastItemCount = mAdapter.getCount();

			// We want to immediately display the selected card if there is one (on the first time
			// we get data)
			//
			// This code is kind of a nightmarish hack due to waiting on different events to occur.
			// First it loops until the Adapter has populated the ListView.  Once that happens, we
			// scroll the row into position (it won't work if it's not partially visible).  After that,
			// we show the details (without animating).
			if (!mSimpleMode && mSelectCardRunnable == null && !TextUtils.isEmpty(mSelectedCardId)) {
				final int position = mAdapter.getPosition(mSelectedCardId);
				if (position != -1 && position != mDetailPosition) {
					Log.i("Attempting to show selected card id: " + mSelectedCardId);

					// We need to wait until the view is actually populated; so we run a postqueue until it shows up
					mSelectCardRunnable = new Runnable() {
						@Override
						public void run() {
							if (getChildCount() > 0) {
								ItinListView.this.setSelectionFromTop(position, 0);
								post(new Runnable() {
									@Override
									public void run() {
										showDetails(position, false);
									}
								});
							}
							else {
								postDelayed(this, 25);
							}
						}
					};
					mSelectCardRunnable.run();
				}
			}

			//We want our background line to be refreshed, but not until after the list draws, 250 will usually
			//be the right amount of time to delay, this isn't a great solution, but the line is totally non-critical
			Runnable lineUpdateRunner = new Runnable() {
				@Override
				public void run() {
					onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
				}
			};
			ItinListView.this.postDelayed(lineUpdateRunner, 250);
		}
	};

	private AnimatorListener mModeSwitchSemListener = new AnimatorListener() {

		@Override
		public void onAnimationCancel(Animator arg0) {
			mModeSwitchSemaphore.release();
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			mModeSwitchSemaphore.release();
		}

		@Override
		public void onAnimationRepeat(Animator arg0) {
		}

		@Override
		public void onAnimationStart(Animator arg0) {
		}

	};

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	private class FooterView extends View {
		private boolean mHasDrawn = false;

		public FooterView(Context context) {
			super(context);
		}

		public boolean getHasDrawn() {
			return mHasDrawn;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			if (!mHasDrawn) {
				mHasDrawn = true;
				showDetails();
			}
		}

	}

}
