package com.expedia.bookings.widget;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Transformation;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.expedia.bookings.R;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.Log;

public class Gallery extends AbsSpinner implements OnGestureListener {

	private static final String TAG = "Gallery";

	private static final boolean localLOGV = false;

	/**
	 * Duration in milliseconds from the start of a scroll during which we're
	 * unsure whether the user is scrolling or flinging.
	 */
	private static final int SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT = 250;

	/**
	 * The default flip interval, if flipping through gallery.
	 */
	private static final int DEFAULT_FLIP_INTERVAL = 4000;

	/**
	 * Horizontal spacing between items.
	 */
	private int mSpacing = 0;

	/**
	 * How long the transition animation should run when a child view changes
	 * position, measured in milliseconds.
	 */
	private int mAnimationDuration = 400;

	/**
	 * The alpha of items that are not selected.
	 */
	private float mUnselectedAlpha;

	/**
	 * The rate at which the Gallery flips items.
	 */
	private int mFlipInterval = 0;

	/**
	 * Left most edge of a child seen so far during layout.
	 */
	private int mLeftMost;

	/**
	 * Right most edge of a child seen so far during layout.
	 */
	private int mRightMost;

	private int mGravity;

	/**
	 * Helper for detecting touch gestures.
	 */
	private GestureDetector mGestureDetector;

	/**
	 * The position of the item that received the user's down touch.
	 */
	private int mDownTouchPosition;

	/**
	 * The view of the item that received the user's down touch.
	 */
	private View mDownTouchView;

	/**
	 * Executes the delta scrolls from a fling or scroll movement. 
	 */
	private FlingRunnable mFlingRunnable = new FlingRunnable();

	/**
	 * Sets mSuppressSelectionChanged = false. This is used to set it to false
	 * in the future. It will also trigger a selection changed.
	 */
	private Runnable mDisableSuppressSelectionChangedRunnable = new Runnable() {
		public void run() {
			mSuppressSelectionChanged = false;
			selectionChanged();
		}
	};

	/**
	 * When fling runnable runs, it resets this to false. Any method along the
	 * path until the end of its run() can set this to true to abort any
	 * remaining fling. For example, if we've reached either the leftmost or
	 * rightmost item, we will set this to true.
	 */
	private boolean mShouldStopFling;

	/**
	 * The currently selected item's child.
	 */
	private View mSelectedChild;

	/**
	 * Whether to continuously callback on the item selected listener during a
	 * fling.
	 */
	private boolean mShouldCallbackDuringFling = true;

	/**
	 * Whether to callback when an item that is not selected is clicked.
	 */
	private boolean mShouldCallbackOnUnselectedItemClick = true;

	/**
	 * If true, do not callback to item selected listener. 
	 */
	private boolean mSuppressSelectionChanged;

	/**
	 * If true, we have received the "invoke" (center or enter buttons) key
	 * down. This is checked before we action on the "invoke" key up, and is
	 * subsequently cleared.
	 */
	private boolean mReceivedInvokeKeyDown;

	private AdapterContextMenuInfo mContextMenuInfo;

	/**
	 * If true, this onScroll is the first for this user's drag (remember, a
	 * drag sends many onScrolls).
	 */
	private boolean mIsFirstScroll;

	public Gallery(Context context) {
		this(context, null);
	}

	public Gallery(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.galleryStyle);
	}

	public Gallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mGestureDetector = new GestureDetector(context, this);
		mGestureDetector.setIsLongpressEnabled(true);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Gallery, defStyle, 0);

		int index = a.getInt(R.styleable.Gallery_android_gravity, -1);
		if (index >= 0) {
			setGravity(index);
		}

		int animationDuration = a.getInt(R.styleable.Gallery_android_animationDuration, -1);
		if (animationDuration > 0) {
			setAnimationDuration(animationDuration);
		}

		int spacing = a.getDimensionPixelOffset(R.styleable.Gallery_android_spacing, 0);
		setSpacing(spacing);

		float unselectedAlpha = a.getFloat(R.styleable.Gallery_android_unselectedAlpha, 0.5f);
		setUnselectedAlpha(unselectedAlpha);

		int flipInterval = a.getInt(R.styleable.Gallery_android_flipInterval, DEFAULT_FLIP_INTERVAL);
		setFlipInterval(flipInterval);

		a.recycle();

		// We draw the selected item last (because otherwise the item to the
		// right overlaps it)
		setChildrenDrawingOrderEnabled(true);

		setStaticTransformationsEnabled(true);
	}

	public void setUrls(List<String> urls) {
		Adapter currAdapter = getAdapter();
		if (currAdapter != null && !(currAdapter instanceof ImageAdapter)) {
			throw new UnsupportedOperationException("Can't use Gallery with both an Adapter and a list of URLs.");
		}

		if (currAdapter != null) {
			ImageAdapter adapter = (ImageAdapter) currAdapter;
			adapter.setUrls(urls);
		}
		else {
			ImageAdapter adapter = new ImageAdapter(getContext(), urls);
			setAdapter(adapter);
		}
	}

	/**
	 * Whether or not to callback on any {@link #getOnItemSelectedListener()}
	 * while the items are being flinged. If false, only the final selected item
	 * will cause the callback. If true, all items between the first and the
	 * final will cause callbacks.
	 * 
	 * @param shouldCallback Whether or not to callback on the listener while
	 *            the items are being flinged.
	 */
	public void setCallbackDuringFling(boolean shouldCallback) {
		mShouldCallbackDuringFling = shouldCallback;
	}

	/**
	 * Whether or not to callback when an item that is not selected is clicked.
	 * If false, the item will become selected (and re-centered). If true, the
	 * {@link #getOnItemClickListener()} will get the callback.
	 * 
	 * @param shouldCallback Whether or not to callback on the listener when a
	 *            item that is not selected is clicked.
	 * @hide
	 */
	public void setCallbackOnUnselectedItemClick(boolean shouldCallback) {
		mShouldCallbackOnUnselectedItemClick = shouldCallback;
	}

	/**
	 * Sets how long the transition animation should run when a child view
	 * changes position. Only relevant if animation is turned on.
	 * 
	 * @param animationDurationMillis The duration of the transition, in
	 *        milliseconds.
	 * 
	 * @attr ref android.R.styleable#Gallery_animationDuration
	 */
	public void setAnimationDuration(int animationDurationMillis) {
		mAnimationDuration = animationDurationMillis;
	}

	/**
	 * Sets the spacing between items in a Gallery
	 * 
	 * @param spacing The spacing in pixels between items in the Gallery
	 * 
	 * @attr ref android.R.styleable#Gallery_spacing
	 */
	public void setSpacing(int spacing) {
		mSpacing = spacing;
	}

	/**
	 * Sets the alpha of items that are not selected in the Gallery.
	 * 
	 * @param unselectedAlpha the alpha for the items that are not selected.
	 * 
	 * @attr ref android.R.styleable#Gallery_unselectedAlpha
	 */
	public void setUnselectedAlpha(float unselectedAlpha) {
		mUnselectedAlpha = unselectedAlpha;
	}

	/**
	 * Sets the flip interval between items in the Gallery
	 * 
	 * @param flipInterval the flip interval between items in the Gallery
	 */
	public void setFlipInterval(int flipInterval) {
		mFlipInterval = flipInterval;
	}

	@Override
	protected boolean getChildStaticTransformation(View child, Transformation t) {

		t.clear();
		t.setAlpha(child == mSelectedChild ? 1.0f : mUnselectedAlpha);

		return true;
	}

	@Override
	protected int computeHorizontalScrollExtent() {
		// Only 1 item is considered to be selected
		return 1;
	}

	@Override
	protected int computeHorizontalScrollOffset() {
		// Current scroll position is the same as the selected position
		return mSelectedPosition;
	}

	@Override
	protected int computeHorizontalScrollRange() {
		// Scroll range is the same as the item count
		return mItemCount;
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
		return p instanceof LayoutParams;
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		/*
		 * Gallery expects Gallery.LayoutParams.
		 */
		return new Gallery.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);

		/*
		 * Remember that we are in layout to prevent more layout request from
		 * being generated.
		 */
		mInLayout = true;
		layout(0, false);
		mInLayout = false;
	}

	int getChildHeight(View child) {
		return child.getMeasuredHeight();
	}

	/**
	 * Tracks a motion scroll. In reality, this is used to do just about any
	 * movement to items (touch scroll, arrow-key scroll, set an item as selected).
	 * 
	 * @param deltaX Change in X from the previous event.
	 */
	void trackMotionScroll(int deltaX) {

		if (getChildCount() == 0) {
			return;
		}

		boolean toLeft = deltaX < 0;

		int limitedDeltaX = getLimitedMotionScrollAmount(toLeft, deltaX);
		if (limitedDeltaX != deltaX) {
			// The above call returned a limited amount, so stop any scrolls/flings
			mFlingRunnable.endFling(false);
			onFinishedMovement();
		}

		offsetChildrenLeftAndRight(limitedDeltaX);

		detachOffScreenChildren(toLeft);

		if (toLeft) {
			// If moved left, there will be empty space on the right
			fillToGalleryRight();
		}
		else {
			// Similarly, empty space on the left
			fillToGalleryLeft();
		}

		// Clear unused views
		mRecycler.clear();

		setSelectionToCenterChild();

		invalidate();
	}

	int getLimitedMotionScrollAmount(boolean motionToLeft, int deltaX) {
		int extremeItemPosition = motionToLeft ? mItemCount - 1 : 0;
		View extremeChild = getChildAt(extremeItemPosition - mFirstPosition);

		if (extremeChild == null) {
			return deltaX;
		}

		int extremeChildCenter = getCenterOfView(extremeChild);
		int galleryCenter = getCenterOfGallery();

		if (motionToLeft) {
			if (extremeChildCenter <= galleryCenter) {

				// The extreme child is past his boundary point!
				return 0;
			}
		}
		else {
			if (extremeChildCenter >= galleryCenter) {

				// The extreme child is past his boundary point!
				return 0;
			}
		}

		int centerDifference = galleryCenter - extremeChildCenter;

		return motionToLeft
				? Math.max(centerDifference, deltaX)
				: Math.min(centerDifference, deltaX);
	}

	/**
	 * Offset the horizontal location of all children of this view by the
	 * specified number of pixels.
	 * 
	 * @param offset the number of pixels to offset
	 */
	private void offsetChildrenLeftAndRight(int offset) {
		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).offsetLeftAndRight(offset);
		}
	}

	/**
	 * @return The center of this Gallery.
	 */
	private int getCenterOfGallery() {
		return (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
	}

	/**
	 * @return The center of the given view.
	 */
	private static int getCenterOfView(View view) {
		return view.getLeft() + view.getWidth() / 2;
	}

	/**
	 * Detaches children that are off the screen (i.e.: Gallery bounds).
	 * 
	 * @param toLeft Whether to detach children to the left of the Gallery, or
	 *            to the right.
	 */
	private void detachOffScreenChildren(boolean toLeft) {
		int numChildren = getChildCount();
		int firstPosition = mFirstPosition;
		int start = 0;
		int count = 0;

		if (toLeft) {
			final int galleryLeft = getPaddingLeft();
			for (int i = 0; i < numChildren; i++) {
				final View child = getChildAt(i);
				if (child.getRight() >= galleryLeft) {
					break;
				}
				else {
					count++;
					mRecycler.put(firstPosition + i, child);
				}
			}
		}
		else {
			final int galleryRight = getWidth() - getPaddingRight();
			for (int i = numChildren - 1; i >= 0; i--) {
				final View child = getChildAt(i);
				if (child.getLeft() <= galleryRight) {
					break;
				}
				else {
					start = i;
					count++;
					mRecycler.put(firstPosition + i, child);
				}
			}
		}

		detachViewsFromParent(start, count);

		if (toLeft) {
			mFirstPosition += count;
		}
	}

	/**
	 * Scrolls the items so that the selected item is in its 'slot' (its center
	 * is the gallery's center).
	 */
	private void scrollIntoSlots() {

		if (getChildCount() == 0 || mSelectedChild == null)
			return;

		int selectedCenter = getCenterOfView(mSelectedChild);
		int targetCenter = getCenterOfGallery();

		int scrollAmount = targetCenter - selectedCenter;
		if (scrollAmount != 0) {
			mFlingRunnable.startUsingDistance(scrollAmount);
		}
		else {
			onFinishedMovement();
		}
	}

	private void onFinishedMovement() {
		if (mSuppressSelectionChanged) {
			mSuppressSelectionChanged = false;

			// We haven't been callbacking during the fling, so do it now
			super.selectionChanged();
		}
		setScrolling(false);
		invalidate();
	}

	@Override
	void selectionChanged() {
		if (!mSuppressSelectionChanged) {
			super.selectionChanged();
		}
	}

	/**
	 * Looks for the child that is closest to the center and sets it as the
	 * selected child.
	 */
	private void setSelectionToCenterChild() {

		View selView = mSelectedChild;
		if (mSelectedChild == null)
			return;

		int galleryCenter = getCenterOfGallery();

		// Common case where the current selected position is correct
		if (selView.getLeft() <= galleryCenter && selView.getRight() >= galleryCenter) {
			return;
		}

		// TODO better search
		int closestEdgeDistance = Integer.MAX_VALUE;
		int newSelectedChildIndex = 0;
		for (int i = getChildCount() - 1; i >= 0; i--) {

			View child = getChildAt(i);

			if (child.getLeft() <= galleryCenter && child.getRight() >= galleryCenter) {
				// This child is in the center
				newSelectedChildIndex = i;
				break;
			}

			int childClosestEdgeDistance = Math.min(Math.abs(child.getLeft() - galleryCenter),
					Math.abs(child.getRight() - galleryCenter));
			if (childClosestEdgeDistance < closestEdgeDistance) {
				closestEdgeDistance = childClosestEdgeDistance;
				newSelectedChildIndex = i;
			}
		}

		int newPos = mFirstPosition + newSelectedChildIndex;

		if (newPos != mSelectedPosition) {
			setSelectedPositionInt(newPos);
			setNextSelectedPositionInt(newPos);
			checkSelectionChanged();
		}
	}

	/**
	 * Creates and positions all views for this Gallery.
	 * <p>
	 * We layout rarely, most of the time {@link #trackMotionScroll(int)} takes
	 * care of repositioning, adding, and removing children.
	 * 
	 * @param delta Change in the selected position. +1 means the selection is
	 *            moving to the right, so views are scrolling to the left. -1
	 *            means the selection is moving to the left.
	 */
	void layout(int delta, boolean animate) {

		int childrenLeft = mSpinnerPadding.left;
		int childrenWidth = getRight() - getLeft() - mSpinnerPadding.left - mSpinnerPadding.right;

		// Retain the current offset (if Gallery is mid-scroll/fling)
		int currOffset = 0;
		if (mSelectedChild != null) {
			int selectedCenter = getCenterOfView(mSelectedChild);
			int targetCenter = getCenterOfGallery();
			currOffset = selectedCenter - targetCenter;
		}

		if (mDataChanged) {
			handleDataChanged();
		}

		// Handle an empty gallery by removing all views.
		if (mItemCount == 0) {
			resetList();
			return;
		}

		// Update to the new selected position.
		if (mNextSelectedPosition >= 0) {
			setSelectedPositionInt(mNextSelectedPosition);
		}

		// All views go in recycler while we are in layout
		recycleAllViews();

		// Clear out old views
		//removeAllViewsInLayout();
		detachAllViewsFromParent();

		/*
		 * These will be used to give initial positions to views entering the
		 * gallery as we scroll
		 */
		mRightMost = 0;
		mLeftMost = 0;

		// Make selected view and center it

		/*
		 * mFirstPosition will be decreased as we add views to the left later
		 * on. The 0 for x will be offset in a couple lines down.
		 */
		mFirstPosition = mSelectedPosition;
		View sel = makeAndAddView(mSelectedPosition, 0, 0, true);

		// Put the selected child in the center
		int selectedOffset = childrenLeft + (childrenWidth / 2) - (sel.getWidth() / 2);
		sel.offsetLeftAndRight(selectedOffset);

		offsetChildrenLeftAndRight(currOffset);

		fillToGalleryRight();
		fillToGalleryLeft();

		// Flush any cached views that did not get reused above
		mRecycler.clear();

		invalidate();
		checkSelectionChanged();

		mDataChanged = false;
		mNeedSync = false;
		setNextSelectedPositionInt(mSelectedPosition);

		updateSelectedItemMetadata();
	}

	private void fillToGalleryLeft() {
		int itemSpacing = mSpacing;
		int galleryLeft = getPaddingLeft();

		// Set state for initial iteration
		View prevIterationView = getChildAt(0);
		int curPosition;
		int curRightEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition - 1;
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
		}
		else {
			// No children available!
			curPosition = 0;
			curRightEdge = getRight() - getLeft() - getPaddingRight();
			mShouldStopFling = true;
		}

		while (curRightEdge > galleryLeft && curPosition >= 0) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
					curRightEdge, false);

			// Remember some state
			mFirstPosition = curPosition;

			// Set state for next iteration
			curRightEdge = prevIterationView.getLeft() - itemSpacing;
			curPosition--;
		}
	}

	private void fillToGalleryRight() {
		int itemSpacing = mSpacing;
		int galleryRight = getRight() - getLeft() - getPaddingRight();
		int numChildren = getChildCount();
		int numItems = mItemCount;

		// Set state for initial iteration
		View prevIterationView = getChildAt(numChildren - 1);
		int curPosition;
		int curLeftEdge;

		if (prevIterationView != null) {
			curPosition = mFirstPosition + numChildren;
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
		}
		else {
			mFirstPosition = curPosition = mItemCount - 1;
			curLeftEdge = getPaddingLeft();
			mShouldStopFling = true;
		}

		while (curLeftEdge < galleryRight && curPosition < numItems) {
			prevIterationView = makeAndAddView(curPosition, curPosition - mSelectedPosition,
					curLeftEdge, true);

			// Set state for next iteration
			curLeftEdge = prevIterationView.getRight() + itemSpacing;
			curPosition++;
		}
	}

	/**
	 * Obtain a view, either by pulling an existing view from the recycler or by
	 * getting a new one from the adapter. If we are animating, make sure there
	 * is enough information in the view's layout parameters to animate from the
	 * old to new positions.
	 * 
	 * @param position Position in the gallery for the view to obtain
	 * @param offset Offset from the selected position
	 * @param x X-coordintate indicating where this view should be placed. This
	 *        will either be the left or right edge of the view, depending on
	 *        the fromLeft paramter
	 * @param fromLeft Are we posiitoning views based on the left edge? (i.e.,
	 *        building from left to right)?
	 * @return A view that has been added to the gallery
	 */
	private View makeAndAddView(int position, int offset, int x,
			boolean fromLeft) {

		View child;

		if (!mDataChanged) {
			child = mRecycler.get(position);
			if (child != null) {
				// Can reuse an existing view
				int childLeft = child.getLeft();

				// Remember left and right edges of where views have been placed
				mRightMost = Math.max(mRightMost, childLeft
						+ child.getMeasuredWidth());
				mLeftMost = Math.min(mLeftMost, childLeft);

				// Position the view
				setUpChild(child, offset, x, fromLeft);

				return child;
			}
		}

		// Nothing found in the recycler -- ask the adapter for a view
		child = getAdapter().getView(position, null, this);

		// Position the view
		setUpChild(child, offset, x, fromLeft);

		return child;
	}

	/**
	 * Helper for makeAndAddView to set the position of a view and fill out its
	 * layout paramters.
	 * 
	 * @param child The view to position
	 * @param offset Offset from the selected position
	 * @param x X-coordintate indicating where this view should be placed. This
	 *        will either be the left or right edge of the view, depending on
	 *        the fromLeft paramter
	 * @param fromLeft Are we posiitoning views based on the left edge? (i.e.,
	 *        building from left to right)?
	 */
	private void setUpChild(View child, int offset, int x, boolean fromLeft) {

		// Respect layout params that are already in the view. Otherwise
		// make some up...
		Gallery.LayoutParams lp = (Gallery.LayoutParams)
				child.getLayoutParams();
		if (lp == null) {
			lp = (Gallery.LayoutParams) generateDefaultLayoutParams();
		}

		addViewInLayout(child, fromLeft ? -1 : 0, lp);

		child.setSelected(offset == 0);

		// Get measure specs
		int childHeightSpec = ViewGroup.getChildMeasureSpec(mHeightMeasureSpec,
				mSpinnerPadding.top + mSpinnerPadding.bottom, lp.height);
		int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec,
				mSpinnerPadding.left + mSpinnerPadding.right, lp.width);

		// Measure child
		child.measure(childWidthSpec, childHeightSpec);

		int childLeft;
		int childRight;

		// Position vertically based on gravity setting
		int childTop = calculateTop(child, true);
		int childBottom = childTop + child.getMeasuredHeight();

		int width = child.getMeasuredWidth();
		if (fromLeft) {
			childLeft = x;
			childRight = childLeft + width;
		}
		else {
			childLeft = x - width;
			childRight = x;
		}

		child.layout(childLeft, childTop, childRight, childBottom);
	}

	/**
	 * Figure out vertical placement based on mGravity
	 * 
	 * @param child Child to place
	 * @return Where the top of the child should be
	 */
	private int calculateTop(View child, boolean duringLayout) {
		int myHeight = duringLayout ? getMeasuredHeight() : getHeight();
		int childHeight = duringLayout ? child.getMeasuredHeight() : child.getHeight();

		int childTop = 0;

		switch (mGravity) {
		case Gravity.TOP:
			childTop = mSpinnerPadding.top;
			break;
		case Gravity.CENTER_VERTICAL:
			int availableSpace = myHeight - mSpinnerPadding.bottom
					- mSpinnerPadding.top - childHeight;
			childTop = mSpinnerPadding.top + (availableSpace / 2);
			break;
		case Gravity.BOTTOM:
			childTop = myHeight - mSpinnerPadding.bottom - childHeight;
			break;
		}
		return childTop;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// Give everything to the gesture detector
		boolean retValue = mGestureDetector.onTouchEvent(event);

		int action = event.getAction();
		if (action == MotionEvent.ACTION_UP) {
			// Helper method for lifted finger
			onUp();
		}
		else if (action == MotionEvent.ACTION_CANCEL) {
			onCancel();
		}

		return retValue;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onSingleTapUp(MotionEvent e) {

		if (mDownTouchPosition >= 0) {

			// An item tap should make it selected, so scroll to this child.
			scrollToChild(mDownTouchPosition - mFirstPosition);

			// Also pass the click so the client knows, if it wants to.
			if (mShouldCallbackOnUnselectedItemClick || mDownTouchPosition == mSelectedPosition) {
				performItemClick(mDownTouchView, mDownTouchPosition, getAdapter()
						.getItemId(mDownTouchPosition));
			}

			return true;
		}

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		if (!mShouldCallbackDuringFling) {
			// We want to suppress selection changes

			// Remove any future code to set mSuppressSelectionChanged = false
			removeCallbacks(mDisableSuppressSelectionChangedRunnable);

			// This will get reset once we scroll into slots
			if (!mSuppressSelectionChanged)
				mSuppressSelectionChanged = true;
		}

		// Fling the gallery!
		mFlingRunnable.startUsingVelocity((int) -velocityX);
		setScrolling(true);

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

		if (localLOGV)
			Log.v(TAG, String.valueOf(e2.getX() - e1.getX()));

		/*
		 * Now's a good time to tell our parent to stop intercepting our events!
		 * The user has moved more than the slop amount, since GestureDetector
		 * ensures this before calling this method. Also, if a parent is more
		 * interested in this touch's events than we are, it would have
		 * intercepted them by now (for example, we can assume when a Gallery is
		 * in the ListView, a vertical scroll would not end up in this method
		 * since a ListView would have intercepted it by now).
		 */
		getParent().requestDisallowInterceptTouchEvent(true);

		// As the user scrolls, we want to callback selection changes so related-
		// info on the screen is up-to-date with the gallery's selection
		if (!mShouldCallbackDuringFling) {
			if (mIsFirstScroll) {
				/*
				 * We're not notifying the client of selection changes during
				 * the fling, and this scroll could possibly be a fling. Don't
				 * do selection changes until we're sure it is not a fling.
				 */
				if (!mSuppressSelectionChanged)
					mSuppressSelectionChanged = true;
				postDelayed(mDisableSuppressSelectionChangedRunnable, SCROLL_TO_FLING_UNCERTAINTY_TIMEOUT);
			}
		}
		else {
			if (mSuppressSelectionChanged)
				mSuppressSelectionChanged = false;
		}
		
		if (mIsFirstScroll) {
			setScrolling(true);
		}

		// Track the motion
		trackMotionScroll(-1 * (int) distanceX);

		mIsFirstScroll = false;
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean onDown(MotionEvent e) {

		// Kill any existing fling/scroll
		mFlingRunnable.stop(false);

		// Get the item's view that was touched
		mDownTouchPosition = pointToPosition((int) e.getX(), (int) e.getY());

		if (mDownTouchPosition >= 0) {
			mDownTouchView = getChildAt(mDownTouchPosition - mFirstPosition);
			mDownTouchView.setPressed(true);
		}

		// Reset the multiple-scroll tracking state
		mIsFirstScroll = true;

		// Must return true to get matching events for this down event.
		return true;
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_UP.
	 */
	void onUp() {

		if (mFlingRunnable.mScroller.isFinished()) {
			scrollIntoSlots();
		}

		dispatchUnpress();
	}

	/**
	 * Called when a touch event's action is MotionEvent.ACTION_CANCEL.
	 */
	void onCancel() {
		onUp();
	}

	/**
	 * {@inheritDoc}
	 */
	public void onLongPress(MotionEvent e) {

		if (mDownTouchPosition < 0) {
			return;
		}

		performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		long id = getItemIdAtPosition(mDownTouchPosition);
		dispatchLongPress(mDownTouchView, mDownTouchPosition, id);
	}

	// Unused methods from GestureDetector.OnGestureListener below

	/**
	 * {@inheritDoc}
	 */
	public void onShowPress(MotionEvent e) {
	}

	// Unused methods from GestureDetector.OnGestureListener above

	private void dispatchPress(View child) {

		if (child != null) {
			child.setPressed(true);
		}

		setPressed(true);
	}

	private void dispatchUnpress() {

		for (int i = getChildCount() - 1; i >= 0; i--) {
			getChildAt(i).setPressed(false);
		}

		setPressed(false);
	}

	@Override
	public void dispatchSetSelected(boolean selected) {
		/*
		 * We don't want to pass the selected state given from its parent to its
		 * children since this widget itself has a selected state to give to its
		 * children.
		 */
	}

	@Override
	protected void dispatchSetPressed(boolean pressed) {

		// Show the pressed state on the selected child
		if (mSelectedChild != null) {
			mSelectedChild.setPressed(pressed);
		}
	}

	@Override
	protected ContextMenuInfo getContextMenuInfo() {
		return mContextMenuInfo;
	}

	@Override
	public boolean showContextMenuForChild(View originalView) {

		final int longPressPosition = getPositionForView(originalView);
		if (longPressPosition < 0) {
			return false;
		}

		final long longPressId = getAdapter().getItemId(longPressPosition);
		return dispatchLongPress(originalView, longPressPosition, longPressId);
	}

	@Override
	public boolean showContextMenu() {

		if (isPressed() && mSelectedPosition >= 0) {
			int index = mSelectedPosition - mFirstPosition;
			View v = getChildAt(index);
			return dispatchLongPress(v, mSelectedPosition, mSelectedRowId);
		}

		return false;
	}

	private boolean dispatchLongPress(View view, int position, long id) {
		boolean handled = false;

		if (mOnItemLongClickListener != null) {
			handled = mOnItemLongClickListener.onItemLongClick(this, mDownTouchView,
					mDownTouchPosition, id);
		}

		if (!handled) {
			mContextMenuInfo = new AdapterContextMenuInfo(view, position, id);
			handled = super.showContextMenuForChild(this);
		}

		if (handled) {
			performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
		}

		return handled;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// Gallery steals all key events
		return event.dispatch(this, null, null);
	}

	/**
	 * Handles left, right, and clicking
	 * @see android.view.View#onKeyDown
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {

		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (movePrevious()) {
				playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
			}
			return true;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (moveNext()) {
				playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
			}
			return true;

		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			mReceivedInvokeKeyDown = true;
			// fallthrough to default handling
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER: {

			if (mReceivedInvokeKeyDown) {
				if (mItemCount > 0) {

					dispatchPress(mSelectedChild);
					postDelayed(new Runnable() {
						public void run() {
							dispatchUnpress();
						}
					}, ViewConfiguration.getPressedStateDuration());

					int selectedIndex = mSelectedPosition - mFirstPosition;
					performItemClick(getChildAt(selectedIndex), mSelectedPosition, getAdapter()
							.getItemId(mSelectedPosition));
				}
			}

			// Clear the flag
			mReceivedInvokeKeyDown = false;

			return true;
		}
		}

		return super.onKeyUp(keyCode, event);
	}

	boolean movePrevious() {
		if (mItemCount > 0 && mSelectedPosition > 0) {
			scrollToChild(mSelectedPosition - mFirstPosition - 1);
			return true;
		}
		else {
			return false;
		}
	}

	boolean moveNext() {
		if (mItemCount > 0 && mSelectedPosition < mItemCount - 1) {
			scrollToChild(mSelectedPosition - mFirstPosition + 1);
			return true;
		}
		else {
			return false;
		}
	}

	private boolean scrollToChild(int childPosition) {
		View child = getChildAt(childPosition);

		if (child != null) {
			int distance = getCenterOfGallery() - getCenterOfView(child);
			mFlingRunnable.startUsingDistance(distance);
			setScrolling(true);
			return true;
		}

		return false;
	}

	@Override
	void setSelectedPositionInt(int position) {
		super.setSelectedPositionInt(position);

		// Updates any metadata we keep about the selected item.
		updateSelectedItemMetadata();
	}

	private void updateSelectedItemMetadata() {

		View oldSelectedChild = mSelectedChild;

		View child = mSelectedChild = getChildAt(mSelectedPosition - mFirstPosition);
		if (child == null) {
			return;
		}

		child.setSelected(true);
		child.setFocusable(true);

		if (hasFocus()) {
			child.requestFocus();
		}

		// We unfocus the old child down here so the above hasFocus check
		// returns true
		if (oldSelectedChild != null) {

			// Make sure its drawable state doesn't contain 'selected'
			oldSelectedChild.setSelected(false);

			// Make sure it is not focusable anymore, since otherwise arrow keys
			// can make this one be focused
			oldSelectedChild.setFocusable(false);
		}

	}

	/**
	 * Describes how the child views are aligned.
	 * @param gravity
	 * 
	 * @attr ref android.R.styleable#Gallery_gravity
	 */
	public void setGravity(int gravity) {
		if (mGravity != gravity) {
			mGravity = gravity;
			requestLayout();
		}
	}

	@Override
	protected int getChildDrawingOrder(int childCount, int i) {
		int selectedIndex = mSelectedPosition - mFirstPosition;

		// Just to be safe
		if (selectedIndex < 0)
			return i;

		if (i == childCount - 1) {
			// Draw the selected child last
			return selectedIndex;
		}
		else if (i >= selectedIndex) {
			// Move the children to the right of the selected child earlier one
			return i + 1;
		}
		else {
			// Keep the children to the left of the selected child the same
			return i;
		}
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		/*
		 * The gallery shows focus by focusing the selected item. So, give
		 * focus to our selected item instead. We steal keys from our
		 * selected item elsewhere.
		 */
		if (gainFocus && mSelectedChild != null) {
			mSelectedChild.requestFocus(direction);
		}

	}

	/**
	 * Responsible for fling behavior. Use {@link #startUsingVelocity(int)} to
	 * initiate a fling. Each frame of the fling is handled in {@link #run()}.
	 * A FlingRunnable will keep re-posting itself until the fling is done.
	 *
	 */
	private class FlingRunnable implements Runnable {
		/**
		 * Tracks the decay of a fling scroll
		 */
		private Scroller mScroller;

		/**
		 * X value reported by mScroller on the previous fling
		 */
		private int mLastFlingX;

		public FlingRunnable() {
			mScroller = new Scroller(getContext());
		}

		private void startCommon() {
			// Remove any pending flings
			removeCallbacks(this);
		}

		public void startUsingVelocity(int initialVelocity) {
			if (initialVelocity == 0)
				return;

			startCommon();

			int initialX = initialVelocity < 0 ? Integer.MAX_VALUE : 0;
			mLastFlingX = initialX;
			mScroller.fling(initialX, 0, initialVelocity, 0,
					0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
			post(this);
		}

		public void startUsingDistance(int distance) {
			startUsingDistance(distance, mAnimationDuration);
		}

		public void startUsingDistance(int distance, int animationDuration) {
			if (distance == 0)
				return;

			startCommon();

			mLastFlingX = 0;
			mScroller.startScroll(0, 0, -distance, 0, animationDuration);
			post(this);
		}

		public void stop(boolean scrollIntoSlots) {
			removeCallbacks(this);
			endFling(scrollIntoSlots);
		}

		private void endFling(boolean scrollIntoSlots) {
			/*
			 * Force the scroller's status to finished (without setting its
			 * position to the end)
			 */
			mScroller.forceFinished(true);

			if (scrollIntoSlots)
				scrollIntoSlots();
		}

		public void run() {

			if (mItemCount == 0) {
				endFling(true);
				return;
			}

			mShouldStopFling = false;

			final Scroller scroller = mScroller;
			boolean more = scroller.computeScrollOffset();
			final int x = scroller.getCurrX();

			// Flip sign to convert finger direction to list items direction
			// (e.g. finger moving down means list is moving towards the top)
			int delta = mLastFlingX - x;

			// Pretend that each frame of a fling scroll is a touch scroll
			if (delta > 0) {
				// Moving towards the left. Use first view as mDownTouchPosition
				mDownTouchPosition = mFirstPosition;

				// Don't fling more than 1 screen
				delta = Math.min(getWidth() - getPaddingLeft() - getPaddingRight() - 1, delta);
			}
			else {
				// Moving towards the right. Use last view as mDownTouchPosition
				int offsetToLast = getChildCount() - 1;
				mDownTouchPosition = mFirstPosition + offsetToLast;

				// Don't fling more than 1 screen
				delta = Math.max(-(getWidth() - getPaddingRight() - getPaddingLeft() - 1), delta);
			}

			trackMotionScroll(delta);

			if (more && !mShouldStopFling) {
				mLastFlingX = x;
				post(this);
			}
			else {
				endFling(true);
			}
		}

	}

	/**
	 * Gallery extends LayoutParams to provide a place to hold current
	 * Transformation information along with previous position/transformation
	 * info.
	 * 
	 */
	public static class LayoutParams extends ViewGroup.LayoutParams {
		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int w, int h) {
			super(w, h);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}

	/**
	 * This is a special adapter that you can use with lists of URLs.  It is automatically
	 * 
	 */
	private class ImageAdapter extends BaseAdapter {

		private LayoutInflater mInflater;
		private List<String> mUrls;

		private final LinearLayout.LayoutParams LAYOUT_WIDE = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		private final LinearLayout.LayoutParams LAYOUT_TALL = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.FILL_PARENT);

		public ImageAdapter(Context context, List<String> urls) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mUrls = urls;
		}

		public void setUrls(List<String> urls) {
			mUrls = urls;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mUrls.size();
		}

		@Override
		public Object getItem(int position) {
			return mUrls.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.gallery_image, parent, false);

				holder = new Holder();
				holder.item = (ImageView) convertView.findViewById(R.id.gallery_item_image_view);

				convertView.setTag(holder);
			}
			else {
				holder = (Holder) convertView.getTag();
			}

			ImageView imageView = holder.item;
			String url = (String) getItem(position);

			// Don't depend on the callback to set the ImageView - just refresh the adapter with new data
			OnImageLoaded callback = new OnImageLoaded() {
				public void onImageLoaded(String url, Bitmap bitmap) {
					notifyDataSetChanged();
				}
			};

			if (ImageCache.containsImage(url)) {
				Bitmap bitmap = ImageCache.getImage(url);
				if (bitmap.getWidth() > bitmap.getHeight()) {
					imageView.setLayoutParams(LAYOUT_WIDE);
					imageView.setBackgroundDrawable(null);
				}
				else {
					imageView.setLayoutParams(LAYOUT_TALL);
					imageView.setBackgroundResource(R.drawable.bg_gallery_item);
				}

				imageView.setImageBitmap(bitmap);
			}
			else {
				// Set a placeholder image while we load the image
				imageView.setImageResource(R.drawable.ic_image_placeholder);
				imageView.setLayoutParams(LAYOUT_WIDE);
				imageView.setBackgroundDrawable(null);
				ImageCache.loadImage(toString() + url, url, callback);
			}

			return convertView;
		}

		private class Holder {
			public ImageView item;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Automatic rotation section

	private boolean mRunning = false;
	private boolean mStarted = false;
	private boolean mVisible = false;
	private boolean mUserPresent = true;
	private boolean mScrolling = false;
	private boolean mRegisteredReceiver = false;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_SCREEN_OFF.equals(action)) {
				mUserPresent = false;
				updateRunning();
			}
			else if (Intent.ACTION_USER_PRESENT.equals(action)) {
				mUserPresent = true;
				updateRunning();
			}
		}
	};

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// Listen for broadcasts related to user-presence
		final IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_USER_PRESENT);
		getContext().registerReceiver(mReceiver, filter);
		mRegisteredReceiver = true;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mVisible = false;

		if (mRegisteredReceiver) {
			getContext().unregisterReceiver(mReceiver);
			mRegisteredReceiver = false;
		}
		updateRunning();
	}

	@Override
	protected void onWindowVisibilityChanged(int visibility) {
		super.onWindowVisibilityChanged(visibility);
		mVisible = visibility == VISIBLE;
		updateRunning();
	}

	public void setScrolling(boolean isScrolling) {
		mScrolling = isScrolling;
		updateRunning();
	}

	public void startFlipping() {
		mStarted = true;
		updateRunning();
	}

	public void stopFlipping() {
		mStarted = false;
		updateRunning();
	}

	private void updateRunning() {
		boolean running = mVisible && mStarted && mUserPresent && !mScrolling;
		if (running != mRunning) {
			if (running) {
				Message msg = mHandler.obtainMessage(FLIP_MSG);
				mHandler.sendMessageDelayed(msg, mFlipInterval);
			}
			else {
				mHandler.removeMessages(FLIP_MSG);
			}
			mRunning = running;
		}

		Log.d("updateRunning() mVisible=" + mVisible + ", mStarted=" + mStarted
				+ ", mUserPresent=" + mUserPresent + ", mRunning=" + mRunning + ", mScrolling=" + mScrolling);
	}

	// This method isn't perfect; in particular, it assumes that you are scrolling to an item
	// that has the same width as the current item.
	//
	// It also should not be run while the user is scrolling/flinging.  It could account for that, but I
	// see no need for that right now.
	private void showNext() {
		int count = getCount();
		if (count <= 1) {
			return;
		}

		int currPos = getSelectedItemPosition();
		int nextPos = (currPos + 1) % count;
		int diff = currPos - nextPos;
		int offset = diff * (mSelectedChild.getWidth() + mSpacing);

		// We actually need the animation time to be longer for longer flips.  It turns out
		// that if you try to flip by 30 images at once in 400 ms, it doesn't work.  So
		// we extend the animation time more for each additional image past the first
		// (but below the flipping interval).
		int duration = mAnimationDuration + (mAnimationDuration * Math.abs(diff) / 2);
		if (duration > mFlipInterval) {
			duration -= mAnimationDuration;
			if (duration < 0) {
				duration = mFlipInterval / 2;
			}
		}

		mFlingRunnable.startUsingDistance(offset, duration);
	}

	private final int FLIP_MSG = 1;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == FLIP_MSG) {
				if (mRunning) {
					showNext();
					msg = obtainMessage(FLIP_MSG);
					sendMessageDelayed(msg, mFlipInterval);
				}
			}
		}
	};
}
