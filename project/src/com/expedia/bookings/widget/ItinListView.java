package com.expedia.bookings.widget;

import java.util.LinkedList;
import java.util.Queue;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
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
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.LayoutUtils;
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
		public void onListModeChanged(boolean isInDetailMode, boolean animated);
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

	private ItinCard mDetailsCardView;

	private OnItemClickListener mOnItemClickListener;
	private OnScrollListener mOnScrollListener;
	private OnListModeChangedListener mOnListModeChangedListener;
	private OnItinCardClickListener mOnItinCardClickListener;

	private int mMode = MODE_LIST;

	private View mLastChild = null;
	private boolean mWasChildConsumedTouch = false;

	private int mScrollState = SCROLL_STATE_IDLE;
	private int mDetailPosition = -1;
	private boolean mScrollToReleventOnDataSetChange;

	private int mLastItemCount = 0;

	private Semaphore mModeSwitchSemaphore = new Semaphore(1);
	Queue<Runnable> mUiQueue = new LinkedList<Runnable>();

	private FooterView mFooterView;

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

		// We have a footer view taking up blank space presumably so that the last card
		// in the list has room to expand smoothly. We'll increase its height upon showDetails()
		// and decrease its height back to 0 upon hideDetails().
		mFooterView = new FooterView(context);
		addFooterView(mFooterView);

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

		//If we are in detail mode, pass all touches to the ItinCard

		if (isInDetailMode()) {
			if (mDetailsCardView != null) {
				boolean retVal = mDetailsCardView.dispatchTouchEvent(event);
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
		if (!isInDetailMode()) {
			drawPathView(canvas);
		}

		//Draw the views
		super.onDraw(canvas);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

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
			return mAdapter.getView(position, view, this);
		}
		return null;
	}

	private void setSelectedCardId(String cardId) {
		mSelectedCardId = cardId;
		mAdapter.setSelectedCardId(cardId);
	}

	public void hideDetails(final boolean animate) {
		if (mSimpleMode) {
			setSelectedCardId(null);
			mAdapter.notifyDataSetChanged();
			return;
		}

		if (!isInDetailMode()) {
			return;
		}

		if (!mModeSwitchSemaphore.tryAcquire()) {
			mUiQueue.add(new Runnable() {
				public void run() {
					hideDetails(animate);
				}
			});
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
	private void synchronizedHideDetails(boolean animate) {
		if (mDetailPosition < 0 || mDetailsCardView == null) {
			releaseSemaphore();
			return;
		}

		mFooterView.setHeight(0);

		mMode = MODE_LIST;
		if (mOnListModeChangedListener != null) {
			mOnListModeChangedListener.onListModeChanged(isInDetailMode(), animate);
		}

		if (animate) {
			Animator set = buildCollapseAnimatorSet();
			set.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationCancel(Animator arg0) {
					finishCollapse();
					releaseSemaphore();
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					finishCollapse();
					releaseSemaphore();
				}
			});
			set.start();
		}
		else {
			mDetailsCardView.collapse(false);
			finishCollapse();
			releaseSemaphore();
		}
	}

	private Animator buildCollapseAnimatorSet() {
		int collapsedHeight = mDetailsCardView.getCollapsedHeight();
		ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCardView, collapsedHeight);
		resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animator) {
				// We are animating the top offset of the detail card from 0 to mOriginalViewTop
				float fraction = animator.getAnimatedFraction();
				int offset = (int) (mDetailsCardView.getCollapsedTop() * fraction);

				setSelectionFromTop(mDetailPosition, offset);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}
		});

		AnimatorSet collapseAnimator = mDetailsCardView.collapse(true);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(resizeAnimator, collapseAnimator);
		return set;
	}

	private void finishCollapse() {
		setSelectionFromTop(mDetailPosition, mDetailsCardView.getCollapsedTop());
		onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
		ResizeAnimator.setHeight(mDetailsCardView, mDetailsCardView.getCollapsedHeight());

		mDetailPosition = -1;
		mDetailsCardView = null;
		mAdapter.setDetailPosition(-1);
		setSelectedCardId(null);
	}

	public void showDetails(String id, boolean animate) {
		showDetails(mAdapter.getPosition(id), animate);
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

		if (isInDetailMode()) {
			return;
		}

		if (!mModeSwitchSemaphore.tryAcquire()) {
			mUiQueue.add(new Runnable() {
				public void run() {
					showDetails(position, animate);
				}
			});
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
	private void synchronizedShowDetails(final int position, final boolean animate) {
		int firstVisiblePos = getFirstVisiblePosition();
		int lastVisiblePos = getLastVisiblePosition();

		//So this is the hacky magic to get jumping to a particular card working on 2.x
		//We set the DetailPosition in the adapter which changes the result of getItemViewType for that view
		//We then invalidate the list, which forces us to ask the adapter for new views.
		//After this is ready to draw the new stuff, we should have the views we need, so we go ahead and tell the thing
		//to expand for real.
		if (AndroidUtils.getSdkVersion() < 11 && mAdapter.getDetailPosition() != position) {
			mUiQueue.add(new Runnable() {
				public void run() {
					showDetails(position, animate);
				}
			});
			mAdapter.setDetailPosition(position);

			// This ends up triggering onDataSetChanged(), which eventually takes
			// the next runnable off the UiQueue (the one we just added).
			mAdapter.notifyDataSetChanged();

			invalidate();
			releaseSemaphore();
			return;
		}

		// If this ListView hasn't drawn any children at all yet, wait until it has.
		if (getChildCount() == 0) {
			getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				public void onGlobalLayout() {
					if (getChildCount() != 0) {
						getViewTreeObserver().removeGlobalOnLayoutListener(this);
						synchronizedShowDetails(position, animate);
					}
				}
			});
			return;
		}

		if (mFooterView.getHeight() != getHeight() || mFooterView.getHasDrawn()) {
			mFooterView.setHeight(getHeight());
			getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				public void onGlobalLayout() {
					if (mFooterView.getHeight() == getHeight() && mFooterView.getHasDrawn()) {
						getViewTreeObserver().removeGlobalOnLayoutListener(this);
						synchronizedShowDetails(position, animate);
					}
				}
			});
		}

		// Should we pre-scroll this item right to the top instead of expanding it outwards?
		// * If the view is already expanded, just to make sure, scroll it to the top.
		// * If the position is entirely offscreen, expanding won't work right.
		// * If this is non-animated, just go ahead and scroll to the top rite nao!
		boolean preScroll = (mMode == MODE_DETAIL)
				|| (position < firstVisiblePos || position > lastVisiblePos)
				|| !animate;

		if (preScroll && firstVisiblePos != position) {
			setSelectionFromTop(position, 0);
			getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				public void onGlobalLayout() {
					getViewTreeObserver().removeGlobalOnLayoutListener(this);
					synchronizedShowDetails(position, animate);
				}
			});
			return;
		}

		// By this point, we've done all UI looping and waiting.
		// Things beyond this point will only be executed once per call to showDetails().

		mDetailsCardView = (ItinCard) getFreshDetailView(position);
		if (mDetailsCardView == null || !mDetailsCardView.hasDetails()) {
			releaseSemaphore();
			return;
		}

		mDetailPosition = position;
		setSelectedCardId(mAdapter.getItem(position).getId());
		mMode = MODE_DETAIL;

		if (mOnListModeChangedListener != null) {
			mOnListModeChangedListener.onListModeChanged(isInDetailMode(), animate);
		}

		final int expandedHeight = getHeight() + LayoutUtils.getActionBarSize(getContext());
		if (animate) {
			Animator set = buildExpandAnimatorSet(expandedHeight);
			set.addListener(new AnimatorListenerShort() {
				@Override
				public void onAnimationCancel(Animator arg0) {
					finishExpand(expandedHeight);
					releaseSemaphore();
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					finishExpand(expandedHeight);
					releaseSemaphore();
				}
			});
			set.start();
		}
		else {
			finishExpand(expandedHeight);
			mDetailsCardView.expand(false);
			releaseSemaphore();
		}
	}

	private Animator buildExpandAnimatorSet(int expandedHeight) {
		ValueAnimator resizeAnimator = ResizeAnimator.buildResizeAnimator(mDetailsCardView, expandedHeight);
		resizeAnimator.addUpdateListener(new AnimatorUpdateListener() {
			public void onAnimationUpdate(ValueAnimator animator) {
				// We are animating the top offset of the detail card from mOriginalViewTop to 0
				float fraction = animator.getAnimatedFraction();
				float offset = mDetailsCardView.getCollapsedTop() * (1f - fraction);

				setSelectionFromTop(mDetailPosition, (int) offset);
				onScroll(ItinListView.this, getFirstVisiblePosition(), getChildCount(), mAdapter.getCount());
			}
		});

		AnimatorSet expandAnimator = mDetailsCardView.expand(true);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(resizeAnimator, expandAnimator);
		return set;
	}

	private void finishExpand(int expandedHeight) {
		setSelectionFromTop(mDetailPosition, 0);
		ResizeAnimator.setHeight(mDetailsCardView, expandedHeight);
		onScroll(ItinListView.this, mDetailPosition, getChildCount(), mAdapter.getCount());
		trackOmnitureItinExpanded(mDetailsCardView);
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
				@SuppressLint("NewApi")
				@Override
				public void run() {
					if (ItinListView.this != null && !isInDetailMode()) {
						if (AndroidUtils.getSdkVersion() >= 11) {
							smoothScrollToPositionFromTop(pos, 0);
						}
						else {
							setSelectionFromTop(pos, 0);
						}
					}
				}
			});
		}
		return pos;
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
		int selectedPosition = mAdapter.getPosition(mSelectedCardId);

		// If the expanded card is no longer in the dataset, clean things up.
		if (selectedPosition == -1 && mDetailPosition != -1) {
			hideDetails(false);
		}

		// If a card is expanded but at a different index, make sure it's still showing/expanded
		else if (selectedPosition != -1 && mDetailPosition != -1) {
			showDetails(selectedPosition, false);
		}

		// Otherwise if we should scroll to the most relevant card, do that
		else if (mScrollToReleventOnDataSetChange || mLastItemCount <= 0) {
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
		getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			public void onGlobalLayout() {
				getViewTreeObserver().removeGlobalOnLayoutListener(this);
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
