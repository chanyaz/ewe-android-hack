package com.expedia.bookings.widget;

import java.util.concurrent.Semaphore;

import android.annotation.SuppressLint;
import android.content.Context;
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
import com.expedia.bookings.widget.itin.ItinButtonCard;
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
		public void onListModeChanged(boolean isInDetailMode);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	private static final String STATE_DO_AUTOSCROLL = "STATE_DO_AUTOSCROLL";
	private static final String STATE_DEFAULT_SAVESTATE = "STATE_DEFAULT_SAVESTATE";
	private static final String STATE_LAST_ITEM_COUNT = "STATE_LAST_ITEM_COUNT";
	private static final String STATE_SELECTED_CARD_ID = "STATE_SELECTED_CARD_ID";

	public static final int SCROLL_HEADER_HIDDEN = -9999;

	private static final int MODE_LIST = 0;
	private static final int MODE_DETAIL = 1;

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

	private View mLastChild = null;
	private boolean mWasChildConsumedTouch = false;

	private int mScrollState = SCROLL_STATE_IDLE;
	private int mDetailPosition = -1;
	private int mOriginalViewTop;
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

		// We add a dummy footer view, if we dont do this before setAdapter future calls to addFooterView wont
		// have their views accounted for when measuring
		mFooterVisibilityView = new View(getContext());
		AbsListView.LayoutParams spacerViewParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, 1);
		mFooterVisibilityView.setLayoutParams(spacerViewParams);
		addFooterView(mFooterVisibilityView);

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

		//If we are in detail mode, pass all touches to the ItinCard

		if (mMode == MODE_DETAIL) {
			if (mDetailsCard != null) {
				boolean retVal = mDetailsCard.dispatchTouchEvent(event);
				return retVal;
			}
			else {
				return super.onTouchEvent(event);
			}
		}

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

			if (child instanceof ItinButtonCard
					|| (child instanceof ItinCard && ((ItinCard) child).isTouchOnSummaryButtons(childEvent))) {
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

	public boolean isInDetailMode() {
		return mMode == ItinListView.MODE_DETAIL;
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

				//get the new position of the expanded card.
				mDetailPosition = mAdapter.getPosition(expandedCardId);

				if (tripExists && mDetailPosition >= 0) {
					mDetailsCard = (ItinCard) getFreshDetailView(mDetailPosition);
					if (mDetailsCard != null) {
						mDetailsCard.expand(false);
						mDetailsCard.requestLayout();
					}
					else {
						hideDetails(true);
					}
				}
				else {
					hideDetails(true);
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

	public void hideDetails(boolean animate) {
		if (mSimpleMode) {
			setSelectedCardId(null);
			mAdapter.notifyDataSetChanged();
			return;
		}

		if (mMode == MODE_LIST) {
			return;
		}

		if (!mModeSwitchSemaphore.tryAcquire()) {
			return;
		}

		synchronizedHideDetails(animate);
	}

	/**
	 * Returns true if hide details is "done" including done with any animation. This method
	 * is designed to call itself several times (through a posted Runnable).
	 * @param animate
	 * @return
	 */
	private boolean synchronizedHideDetails(boolean animate) {
		if (mDetailPosition < 0 || mDetailsCard == null) {
			mModeSwitchSemaphore.release();
			return true;
		}

		mMode = MODE_LIST;
		if (mOnListModeChangedListener != null) {
			mOnListModeChangedListener.onListModeChanged(isInDetailMode());
		}

		removeFooterView(mFooterView);
		mFooterView = null;

		Animator set = buildContractAnimatorSet();
		set.addListener(mModeSwitchSemListener);
		if (!animate) {
			set.setDuration(0);
		}
		set.start();
		return false;
	}

	private Animator buildContractAnimatorSet() {
		ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCard, mExpandedCardOriginalHeight);
		resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animator) {
				// We are animating the top offset of the detail card from 0 to mOriginalViewTop
				int offset = (int) (mOriginalViewTop * animator.getAnimatedFraction());
				setSelectionFromTop(mDetailPosition, offset);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}
		});

		AnimatorSet detailCollapseAnim = mDetailsCard.collapse(false);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(resizeAnimator, detailCollapseAnim);

		set.addListener(new AnimatorListenerShort() {
			@Override
			public void onAnimationEnd(Animator animator) {
				setSelectionFromTop(mDetailPosition, mOriginalViewTop);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
				mDetailsCard.getLayoutParams().height = mExpandedCardOriginalHeight;
				mDetailsCard.requestLayout();

				clearDetailView();
				invalidateViews();
			}
		});

		return set;
	}

	public void showDetails(String id, boolean animate) {
		showDetails(mAdapter.getPosition(id), animate);
	}

	private void showDetails() {
		showDetails(mDetailPosition, true);
	}

	private void showDetails(final int position, final boolean animate) {
		// Invalid index
		if (position < 0 || position >= mAdapter.getCount()) {
			return;
		}

		if (mSimpleMode) {
			setSelectedCardId(mAdapter.getItem(position).getId());
			mAdapter.notifyDataSetChanged();
			if (position < getFirstVisiblePosition() || position > getLastVisiblePosition()) {
				setSelectionFromTop(position, 0);
			}

			return;
		}

		if (mMode == MODE_DETAIL) {
			return;
		}

		if (!mModeSwitchSemaphore.tryAcquire()) {
			return;
		}

		synchronizedShowDetails(position, animate);
	}

	/**
	 * Returns true if show details is "done" including done with any animation. This method
	 * is designed to call itself several times (through a posted Runnable).
	 * @param position
	 * @param animate
	 * @return
	 */
	@SuppressLint("NewApi")
	private boolean synchronizedShowDetails(final int position, final boolean animate) {
		mMode = MODE_DETAIL;
		mDetailPosition = position;
		setSelectedCardId(mAdapter.getItem(position).getId());

		int lastViewPos = getCount() - 1;
		int firstVisiblePos = getFirstVisiblePosition();
		int lastVisiblePos = getLastVisiblePosition();
		mExpandedCardHeight = Math.max(mExpandedCardHeight, getHeight());

		// If this ListView hasn't drawn any children at all yet, wait until it has.
		if (getChildCount() == 0) {
			post(new Runnable() {
				public void run() {
					synchronizedShowDetails(position, animate);
				}
			});
			return false;
		}

		// If position is somewhere offscreen, the view for this row won't have been created yet.
		// In this case, we need to scroll to a visible position first, and _then_ expand it.
		if (position < firstVisiblePos || position > lastVisiblePos) {
			setSelectionFromTop(position, 0);
			post(new Runnable() {
				public void run() {
					synchronizedShowDetails(position, animate);
				}
			});
			return false;
		}

		// On API < 11, we won't animate the scrolling. We'll just move there right away.
		if (AndroidUtils.getSdkVersion() < 11) {
			// If our expanding views are at the bottom of the list,
			// we need to add a footer view to make room for the expanded view on 2.x.
			setSelectionFromTop(position, 0);
		}

		// If we are not yet scrolled into position, or all rows are on screen, add our footer view.
		if (firstVisiblePos != position || ((getCount() - 1) == (lastVisiblePos - firstVisiblePos))) {
			if (mFooterView == null) {
				mFooterView = new FooterView(getContext(), mExpandedCardHeight);
				addFooterView(mFooterView);
			}
			if (firstVisiblePos != position && AndroidUtils.getSdkVersion() < 11) {
				//If we aren't scrolled to where we need to be, we continue calling showDetails until we are
				postDelayed(new Runnable() {
					public void run() {
						synchronizedShowDetails(position, animate);
					}
				}, 25);
				return false;
			}
		}

		//If we are scrolled down but our footer still hasn't drawn, we wait
		if (lastVisiblePos == lastViewPos && mFooterView != null && !mFooterView.getHasDrawn()) {
			postDelayed(new Runnable() {
				public void run() {
					synchronizedShowDetails(position, animate);
				}
			}, 25);
			return false;
		}

		// By this point, we've done all UI looping and waiting.
		// Things beyond this point will only be executed once per call to showDetails().

		View view = getFreshDetailView(position);
		if (!(view instanceof ItinCard)) {
			mModeSwitchSemaphore.release();
			return true;
		}

		mDetailsCard = (ItinCard) view;
		if (mDetailsCard == null || !mDetailsCard.hasDetails()) {
			mModeSwitchSemaphore.release();
			return true;
		}

		if (mOnListModeChangedListener != null) {
			mOnListModeChangedListener.onListModeChanged(isInDetailMode());
		}

		mExpandedCardOriginalHeight = mDetailsCard.getHeight();
		mOriginalViewTop = mDetailsCard.getTop();

		Animator set = buildExpandAnimatorSet();
		set.addListener(mModeSwitchSemListener);
		if (!animate) {
			set.setDuration(0);
		}
		set.start();
		return false;
	}

	private Animator buildExpandAnimatorSet() {
		ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCard, mExpandedCardHeight);
		if (AndroidUtils.getSdkVersion() >= 11) {
			resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animator) {
					// We are animating the top offset of the detail card from mOriginalViewTop to 0
					int offset = (int) (mOriginalViewTop * (1f - animator.getAnimatedFraction()));
					setSelectionFromTop(mDetailPosition, offset);
					onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
				}

			});
		}

		AnimatorSet expandAnimator = mDetailsCard.expand(false);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(resizeAnimator, expandAnimator);
		set.addListener(new AnimatorListenerShort() {
			@Override
			public void onAnimationStart(Animator animator) {
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				setSelectionFromTop(mDetailPosition, 0);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
				mDetailsCard.getLayoutParams().height = mExpandedCardHeight;
				mDetailsCard.requestLayout();
				trackOmnitureItinExpanded(mDetailsCard);
			}
		});

		return set;
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

	private void trackOmnitureItinExpanded(ItinCard card) {
		if (card == null) {
			return;
		}

		switch (card.getType()) {
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

	//////////////////////////////////////////////////////////////////////////////////////
	// IMPLEMENTATIONS
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ItinCardData data = mAdapter.getItem(position);
		if (view instanceof ItinButtonCard) {
			// Do nothing
		}
		else if (data.hasDetailData()) {
			showDetails(position, true);
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
		hideDetails(true);

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

	private boolean mObservationFlag = false;

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
			if (!mSimpleMode && !mObservationFlag && !TextUtils.isEmpty(mSelectedCardId)) {
				final int position = mAdapter.getPosition(mSelectedCardId);
				if (position != -1 && position != mDetailPosition) {
					mObservationFlag = true;
					Log.i("Attempting to show selected card id: " + mSelectedCardId);
					showDetails(position, false);
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

	private AnimatorListener mModeSwitchSemListener = new AnimatorListenerShort() {

		@Override
		public void onAnimationCancel(Animator arg0) {
			mModeSwitchSemaphore.release();
		}

		@Override
		public void onAnimationEnd(Animator arg0) {
			mModeSwitchSemaphore.release();
		}

	};

	//////////////////////////////////////////////////////////////////////////////////////
	// INNER CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	private class FooterView extends View {
		private boolean mHasDrawn = false;

		public FooterView(Context context, int height) {
			super(context);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(
					LayoutParams.MATCH_PARENT, height);
			setLayoutParams(params);
			setFocusable(true);
		}

		public boolean getHasDrawn() {
			return mHasDrawn;
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
