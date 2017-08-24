package com.expedia.bookings.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.PicassoScrollListener;
import com.expedia.bookings.enums.ResultsListState;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.StateListenerCollection;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;

public class FruitList extends ListView implements OnScrollListener, IStateProvider<ResultsListState> {
	//Constants
	private static final int DURATION_SNAP_TO_POS = 100;

	//Our heights
	private int mTotalHeight = 0;
	private int mHeaderSpacerHeight = 0;

	//How big is our top spacer - default to 46% of the height
	private float mTopSpacerPercentage = 0.46f;
	private float mTopSpacerPixels = -1f;

	//Spacers, we need our list to fill its height regardless of row number
	private View mHeaderSpacer;
	private View mFooterSpacer;
	private int mHeaderSpacerColor = Color.TRANSPARENT;
	private int mFooterSpacerColor = Color.DKGRAY;

	//Locking
	private boolean mListLockedToTop = false;

	//Start percentage (1f == start at bottom , 0f == start at top)
	private float mPercentage = 1f;

	//Scroll listener for pausing/resuming image downloads while the list flings
	private PicassoScrollListener mPicassoScrollListener;

	/*
	 * CONSTRUCTORS AND INITIALIZERS
	 */

	public FruitList(Context context) {
		super(context);
		initView(context);
	}

	public FruitList(Context context, AttributeSet attrs) {
		super(context, attrs);
		readInAttrs(context, attrs);
		initView(context);
	}

	public FruitList(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		readInAttrs(context, attrs);
		initView(context);
	}

	private void readInAttrs(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FruitList);
			setHeaderSpacerColor(a.getColor(R.styleable.FruitList_headerSpacerColor, mHeaderSpacerColor));
			setFooterSpacerColor(a.getColor(R.styleable.FruitList_footerSpacerColor, mFooterSpacerColor));
			if (a.hasValue(R.styleable.FruitList_headerSpaceSize)) {
				setTopSpacePixels(a.getDimension(R.styleable.FruitList_headerSpaceSize, mTopSpacerPixels));
			}
			else {
				setTopSpacePercentage(
					a.getFraction(R.styleable.FruitList_headerSpacePercentage, 1, 1, mTopSpacerPercentage));
			}
			a.recycle();
		}
	}

	private void initView(Context context) {
		initSpacers(context);
		addHeaderView(mHeaderSpacer);
		addFooterView(mFooterSpacer);

		setOnScrollListener(this);
		registerStateListener(new StateListenerLogger<ResultsListState>(), false);
	}

	private void initSpacers(Context context) {
		mHeaderSpacer = new View(context);
		mFooterSpacer = new View(context);
		mHeaderSpacer.setBackgroundColor(mHeaderSpacerColor);
		mFooterSpacer.setBackgroundColor(mFooterSpacerColor);
		updateSpacerSizes();
	}

	/*
	 * ATTACHED STATE
	 */

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();

		sizeOrDataChanged();
	}

	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		Adapter adapter = getAdapter();
		if (adapter != null) {
			adapter.unregisterDataSetObserver(mDataSetObserver);
		}
	}

	/*
	 * COLOR SETTERS
	 */

	public void setHeaderSpacerColor(int color) {
		mHeaderSpacerColor = color;
		if (mHeaderSpacer != null) {
			mHeaderSpacer.setBackgroundColor(mHeaderSpacerColor);
		}
	}

	public void setFooterSpacerColor(int color) {
		mFooterSpacerColor = color;
		if (mFooterSpacer != null) {
			mFooterSpacer.setBackgroundColor(mFooterSpacerColor);
		}
	}

	/*
	 * TOP SPACE SETTERS
	 */

	public void setTopSpacePercentage(float percentage) {
		mTopSpacerPercentage = percentage;
		mTopSpacerPixels = -1f;
		updateSpacerSizes();
	}

	public void setTopSpacePixels(float pixels) {
		mTopSpacerPixels = pixels;
		mTopSpacerPercentage = -1f;
		updateSpacerSizes();
	}

	/*
	 * LOCKING
	 */

	public void setListLockedToTop(boolean locked) {
		if (mListLockedToTop && !locked && getFirstVisiblePosition() <= getHeaderViewsCount()) {
			//When we
			setSelectionFromTop(getHeaderViewsCount(), 0);
		}
		mListLockedToTop = locked;
		sizeOrDataChanged();
	}

	/*
	 * ADAPTER HANDLING AND LISTENING
	 */
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (adapter != null) {
			adapter.registerDataSetObserver(mDataSetObserver);
		}
		sizeOrDataChanged();
	}

	private final DataSetObserver mDataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			sizeOrDataChanged();
		}
	};

	/*
	 * HEADER AND FOOTER SIZING METHODS
	 */

	public int getHeaderSpacerHeight() {
		return mHeaderSpacerHeight;
	}

	private void sizeOrDataChanged() {
		updateSpacerSizes();
		updateBackgroundColor();
	}

	private void updateSpacerSizes() {
		setHeaderSpacerHeight(calculateHeaderSpacerHeight());
		setFooterSpacerHeight(calculateFooterSpacerHeight());
	}


	@SuppressLint("NewApi")
	private void updateBackgroundColor() {
		if (mListLockedToTop) {
			//We use the footer color here, since the footer color is chosen to be the fill color.
			setBackgroundColor(mFooterSpacerColor);
		}
		else {
			setBackground(null);
		}
	}

	private void setHeaderSpacerHeight(int height) {
		mHeaderSpacerHeight = height;
		setSpacerViewHeight(mHeaderSpacer, height);
	}

	private void setFooterSpacerHeight(int height) {
		setSpacerViewHeight(mFooterSpacer, height);
	}

	private int calculateHeaderSpacerHeight() {
		if (mListLockedToTop || mTotalHeight <= 0) {
			return 0;
		}
		else {
			if (mTopSpacerPixels >= 0) {
				return (int) mTopSpacerPixels;
			}
			else {
				return (int) (mTopSpacerPercentage * mTotalHeight);
			}
		}
	}

	private int calculateContentHeight() {
		if (getRowCount() <= 0) {
			return 0;
		}
		else {
			int rowsCount = getRowCount();
			int singleRowHeight = getRowHeight(false);
			int allRowsHeight = (rowsCount * (singleRowHeight + getDividerHeight())) + getDividerHeight();
			return allRowsHeight;
		}
	}

	private int calculateFooterSpacerHeight() {
		if (mTotalHeight <= 0) {
			return 0;
		}
		else {
			int contentHeight = calculateContentHeight();
			return Math.max(mTotalHeight - contentHeight, 0);
		}
	}

	private void setSpacerViewHeight(View view, int height) {
		if (view != null) {
			ViewGroup.LayoutParams params = view.getLayoutParams();
			if (params == null) {
				params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			}
			if (params.height != height) {
				params.height = height;
				view.setLayoutParams(params);
			}
		}
	}

	/*
	 * OVERSCROLL
	 */

	private int mLastOverscrollMode = OVER_SCROLL_ALWAYS;

	private void updateOverscrollMode(float percentage) {
		int overscroll = percentage <= 0 ? OVER_SCROLL_ALWAYS : OVER_SCROLL_NEVER;
		if (overscroll != mLastOverscrollMode) {
			setOverScrollMode(overscroll);
			mLastOverscrollMode = overscroll;
		}
	}

	/*
	 * ROW METHODS
	 */

	public int getRowCount() {
		int count = getAdapter() != null ? getAdapter().getCount() : getCount();
		return count - getFooterViewsCount() - getHeaderViewsCount();
	}

	public int getRowHeight(boolean withDividerHeight) {
		View row = getOneRow();
		if (row != null) {
			int retHeight = row.getHeight();
			retHeight += withDividerHeight ? getDividerHeight() : 0;
			return retHeight;
		}
		return -1;
	}

	private View getOneRow() {
		View row = null;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child != mHeaderSpacer && child != mFooterSpacer) {
				row = child;
				break;
			}
		}
		return row;
	}

	/*
	 * PERCENTAGE METHODS
	 */

	private ResultsListState mTransStart;
	private ResultsListState mTransEnd;
	private boolean mTransForward = true;
	private float mLastReportedPercentage = 1f;//NOTE: we just care that this is > 0 because we default down

	public void setLastReportedPercentage(float percentage) {
		mLastReportedPercentage = percentage;
	}

	private void reactToPercentage(final float percentage, boolean force) {
		if (percentage != mLastReportedPercentage || force) {
			if (percentage != 0f && percentage != 1f) {
				if (mTransStart == null && mTransEnd == null) {
					if (mLastReportedPercentage <= 0f) {
						mTransStart = ResultsListState.AT_TOP;
						mTransEnd = ResultsListState.AT_BOTTOM;
						mTransForward = true;
					}
					else {
						mTransStart = ResultsListState.AT_BOTTOM;
						mTransEnd = ResultsListState.AT_TOP;
						mTransForward = false;
					}
					startStateTransition(mTransStart, mTransEnd);
				}
				updateStateTransition(mTransStart, mTransEnd, mTransForward ? percentage : 1f - percentage);
			}
			else {
				if (mTransStart != null && mTransEnd != null) {
					updateStateTransition(mTransStart, mTransEnd, mTransForward ? percentage : 1f - percentage);
				}
				attemptFinishingTransition(percentage);
			}
			mLastReportedPercentage = percentage;
		}
	}

	private void attemptFinishingTransition(float percentage) {
		if (!isUserInteraction()) {
			if (mTransStart != null && mTransEnd != null) {
				endStateTransition(mTransStart, mTransEnd);
				mTransStart = null;
				mTransEnd = null;
			}
			finalizeState(percentage >= 1f ? ResultsListState.AT_BOTTOM : ResultsListState.AT_TOP);
		}
	}

	public void gotoTop(int duration) {
		setScrollDownPercentage(0f, duration);
	}

	private void setListScroll(float percentage, boolean fromOutside) {
		if (percentage != getScrollDownPercentage()) {
			if (percentage == 1f) {
				setSelectionFromTop(0, 0);
			}
			else if (!(fromOutside && mIsTouching)) {
				int scrollY = (int) (percentage * mHeaderSpacerHeight) + getDividerHeight();
				setSelectionFromTop(getHeaderViewsCount(), scrollY);
			}
			if (!fromOutside) {
				reactToPercentage(percentage, true);
			}
		}
	}

	public void setScrollDownPercentage(float percentage, int duration) {
		setScrollDownPercentage(percentage, duration, true);
	}

	private void setScrollDownPercentage(float percentage, int duration, boolean fromOutside) {
		if (percentage != getScrollDownPercentage()) {
			if (duration <= 0) {
				setListScroll(percentage, fromOutside);
			}
			else {
				animateToScrollDownPercentage(percentage, duration);
			}
		}
		else {
			if (!fromOutside) {
				reactToPercentage(percentage, true);
			}
		}
	}

	private ValueAnimator mPercentageAnimator;
	private boolean mAnimationEnded = false;

	private boolean isAnimatingScroll() {
		return mPercentageAnimator != null && mPercentageAnimator.isRunning();
	}

	private void animateToScrollDownPercentage(final float percentage, int duration) {
		if (mPercentageAnimator != null && mPercentageAnimator.isStarted()) {
			mPercentageAnimator.cancel();
		}
		mPercentageAnimator = ValueAnimator.ofFloat(getScrollDownPercentage(), percentage);
		mPercentageAnimator.setDuration(duration);
		mPercentageAnimator.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				if (!mAnimationEnded) {
					setListScroll((Float) animation.getAnimatedValue(), false);
				}
			}
		});
		mPercentageAnimator.addListener(new AnimatorListenerAdapter() {
			boolean cancelled = false;

			@Override
			public void onAnimationStart(Animator arg0) {
				mAnimationEnded = false;
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
				mPercentageAnimator = null;
				cancelled = true;
				mAnimationEnded = true;
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				mAnimationEnded = true;
				mPercentageAnimator = null;
				if (!cancelled) {
					setListScroll(percentage, false);
				}
			}
		});
		mPercentageAnimator.start();
	}

	public float getScrollDownPercentage() {
		float retVal = mPercentage;
		if (mHeaderSpacer != null && mHeaderSpacerHeight > 0) {
			if (getFirstVisiblePosition() == 0) {
				int spaceAbove = mHeaderSpacer.getBottom();
				if (spaceAbove > 0) {
					retVal = (float) spaceAbove / mHeaderSpacerHeight;
				}
				else if (spaceAbove == mHeaderSpacerHeight) {
					retVal = 1f;
				}
				else {
					retVal = 0f;
				}
			}
			else {
				retVal = 0f;
			}
		}
		else if (mListLockedToTop) {
			return 0f;
		}
		retVal = Math.max(Math.min(retVal, 1f), 0f);
		mPercentage = retVal;
		return retVal;
	}

	/*
	 * MEASUREMENT OVERRIDES
	 */

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			mTotalHeight = b - t;
			sizeOrDataChanged();
		}
		super.onLayout(changed, l, t, r, b);
	}

	/*
	 * DRAWING
	 */
	int mLastDrawRowHeight = -1;

	@Override
	public void onDraw(Canvas canvas) {
		int rowHeight = getRowHeight(false);
		if (rowHeight != mLastDrawRowHeight) {
			//This ensures the footerspacer is the correct size (as it depends on content height)
			mLastDrawRowHeight = rowHeight;
			sizeOrDataChanged();
		}
		super.onDraw(canvas);
	}

	/*
	 * TOUCH AND SCROLL HANDLING
	 */

	private boolean mIsTouching = false;
	private boolean mIsFlinging = false;
	private int mFlingFirstVisiblePosition = -1;

	@Override
	public boolean onTouchEvent(MotionEvent me) {
		boolean superVal = super.onTouchEvent(me);
		int action = me.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			mIsTouching = true;
			if (isAnimatingScroll()) {
				mPercentageAnimator.cancel();
			}
		}
		else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
			mIsTouching = false;
			snapToPos();
		}
		return superVal;
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		float perc = getScrollDownPercentage();
		int headerViewCount = getHeaderViewsCount();
		if (mIsFlinging && !mIsTouching && mFlingFirstVisiblePosition > headerViewCount
			&& firstVisibleItem <= headerViewCount && firstVisibleItem < mFlingFirstVisiblePosition) {
			//So we are flinging, but we dont want people to fling past the top if we started below it, so we go to the top
			gotoTop(0);
			perc = 0;
		}
		else if (isUserInteraction()) {
			reactToPercentage(perc, false);
		}
		updateOverscrollMode(perc);
	}

	@Override
	public void onScrollStateChanged(AbsListView listView, int scrollState) {
		if (mPicassoScrollListener != null) {
			mPicassoScrollListener.onScrollStateChanged(listView, scrollState);
		}

		boolean wasFlinging = mIsFlinging;
		mIsFlinging = scrollState == OnScrollListener.SCROLL_STATE_FLING;

		if (mIsFlinging && !wasFlinging) {
			mFlingFirstVisiblePosition = getFirstVisiblePosition();
		}
		else if (!mIsFlinging && wasFlinging) {
			mFlingFirstVisiblePosition = -1;
		}

		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
			snapToPos();
		}
	}

	private void snapToPos() {
		snapToPos(DURATION_SNAP_TO_POS);
	}

	private void snapToPos(int duration) {
		if (!isScrollPotentiallyChanging()) {
			float perc = getScrollDownPercentage();
			if (perc <= 1f && perc >= 0.5) {
				setScrollDownPercentage(1f, duration, false);
			}
			else if (perc < 0.5 && perc >= 0) {
				setScrollDownPercentage(0f, duration, false);
			}
		}
	}

	/*
	 * OTHER HELPERS
	 */

	public int getMaxDistanceFromTop() {
		return mHeaderSpacerHeight;
	}

	public boolean isUserInteraction() {
		return mIsTouching || mIsFlinging;
	}

	public boolean isScrollPotentiallyChanging() {
		return isUserInteraction() || isAnimatingScroll();
	}

	public void addPicassoScrollListener(PicassoScrollListener scrollListener) {
		mPicassoScrollListener = scrollListener;
	}
	/*
	 * STATE MANAGEMENT
	 */

	private StateListenerCollection<ResultsListState> mStateListeners = new StateListenerCollection<ResultsListState>();

	@Override
	public void startStateTransition(ResultsListState stateOne, ResultsListState stateTwo) {
		mStateListeners.startStateTransition(stateOne, stateTwo);
	}

	@Override
	public void updateStateTransition(ResultsListState stateOne, ResultsListState stateTwo,
		float percentage) {
		mStateListeners.updateStateTransition(stateOne, stateTwo, percentage);
	}

	@Override
	public void endStateTransition(ResultsListState stateOne, ResultsListState stateTwo) {
		mStateListeners.endStateTransition(stateOne, stateTwo);
	}

	@Override
	public void finalizeState(ResultsListState state) {
		mStateListeners.finalizeState(state);
	}

	@Override
	public void registerStateListener(IStateListener<ResultsListState> listener, boolean fireFinalizeState) {
		mStateListeners.registerStateListener(listener, fireFinalizeState);
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsListState> listener) {
		mStateListeners.unRegisterStateListener(listener);
	}
}
