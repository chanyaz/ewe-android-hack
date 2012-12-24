package com.expedia.bookings.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;

/**
 * An abstract base class for spinner widgets. SDK users will probably not
 * need to use this class.
 *
 * @attr ref android.R.styleable#AbsSpinner_entries
 */
public abstract class AbsSpinner extends AdapterView<SpinnerAdapter> {
	SpinnerAdapter mAdapter;

	int mHeightMeasureSpec;
	int mWidthMeasureSpec;
	boolean mBlockLayoutRequests;

	int mSelectionLeftPadding = 0;
	int mSelectionTopPadding = 0;
	int mSelectionRightPadding = 0;
	int mSelectionBottomPadding = 0;
	final Rect mSpinnerPadding = new Rect();

	final RecycleBin mRecycler = new RecycleBin();
	private DataSetObserver mDataSetObserver;

	/** Temporary frame to hold a child View's frame rectangle */
	private Rect mTouchFrame;

	public AbsSpinner(Context context) {
		super(context);
		initAbsSpinner();
	}

	public AbsSpinner(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AbsSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initAbsSpinner();

		// For our purposes, we aren't going to define spinner entries in XML, so just
		// ignore this part.
		/*
		   TypedArray a = context.obtainStyledAttributes(attrs,
		   com.android.internal.R.styleable.AbsSpinner, defStyle, 0);

		   CharSequence[] entries = a.getTextArray(R.styleable.AbsSpinner_entries);
		   if (entries != null) {
		   ArrayAdapter<CharSequence> adapter =
		   new ArrayAdapter<CharSequence>(context,
		   R.layout.simple_spinner_item, entries);
		   adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
		   setAdapter(adapter);
		   }

		   a.recycle();
		   */
	}

	/**
	 * Common code for different constructor flavors
	 */
	private void initAbsSpinner() {
		setFocusable(true);
		setWillNotDraw(false);
	}

	/**
	 * The Adapter is used to provide the data which backs this Spinner.
	 * It also provides methods to transform spinner items based on their position
	 * relative to the selected item.
	 * @param adapter The SpinnerAdapter to use for this Spinner
	 */
	@Override
	public void setAdapter(SpinnerAdapter adapter) {
		if (null != mAdapter) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
			resetList();
		}

		mAdapter = adapter;

		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		if (mAdapter != null) {
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
			checkFocus();

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

			int position = mItemCount > 0 ? 0 : INVALID_POSITION;

			setSelectedPositionInt(position);
			setNextSelectedPositionInt(position);

			if (mItemCount == 0) {
				// Nothing selected
				checkSelectionChanged();
			}

		}
		else {
			checkFocus();
			resetList();
			// Nothing selected
			checkSelectionChanged();
		}

		requestLayout();
	}

	/**
	 * Clear out all children from the list
	 */
	void resetList() {
		mDataChanged = false;
		mNeedSync = false;

		removeAllViewsInLayout();
		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;

		setSelectedPositionInt(INVALID_POSITION);
		setNextSelectedPositionInt(INVALID_POSITION);
		invalidate();
	}

	/**
	 * @see android.view.View#measure(int, int)
	 *
	 * Figure out the dimensions of this Spinner. The width comes from
	 * the widthMeasureSpec as Spinnners can't have their width set to
	 * UNSPECIFIED. The height is based on the height of the selected item
	 * plus padding.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize;
		int heightSize;

		mSpinnerPadding.left = getPaddingLeft() > mSelectionLeftPadding ? getPaddingLeft()
			: mSelectionLeftPadding;
		mSpinnerPadding.top = getPaddingTop() > mSelectionTopPadding ? getPaddingTop()
			: mSelectionTopPadding;
		mSpinnerPadding.right = getPaddingRight() > mSelectionRightPadding ? getPaddingRight()
			: mSelectionRightPadding;
		mSpinnerPadding.bottom = getPaddingBottom() > mSelectionBottomPadding ? getPaddingBottom()
			: mSelectionBottomPadding;

		if (mDataChanged) {
			handleDataChanged();
		}

		int preferredHeight = 0;
		int preferredWidth = 0;
		boolean needsMeasuring = true;

		int selectedPosition = getSelectedItemPosition();
		if (selectedPosition >= 0 && mAdapter != null && selectedPosition < mAdapter.getCount()) {
			// Try looking in the recycler. (Maybe we were measured once already)
			View view = mRecycler.get(selectedPosition);
			if (view == null) {
				// Make a new one
				view = mAdapter.getView(selectedPosition, null, this);
			}

			if (view != null) {
				// Put in recycler for re-measuring and/or layout
				mRecycler.put(selectedPosition, view);
			}

			if (view != null) {
				if (view.getLayoutParams() == null) {
					mBlockLayoutRequests = true;
					view.setLayoutParams(generateDefaultLayoutParams());
					mBlockLayoutRequests = false;
				}
				measureChild(view, widthMeasureSpec, heightMeasureSpec);

				preferredHeight = getChildHeight(view) + mSpinnerPadding.top + mSpinnerPadding.bottom;
				preferredWidth = getChildWidth(view) + mSpinnerPadding.left + mSpinnerPadding.right;

				needsMeasuring = false;
			}
		}

		if (needsMeasuring) {
			// No views -- just use padding
			preferredHeight = mSpinnerPadding.top + mSpinnerPadding.bottom;
			if (widthMode == MeasureSpec.UNSPECIFIED) {
				preferredWidth = mSpinnerPadding.left + mSpinnerPadding.right;
			}
		}

		preferredHeight = Math.max(preferredHeight, getSuggestedMinimumHeight());
		preferredWidth = Math.max(preferredWidth, getSuggestedMinimumWidth());

		heightSize = resolveSize(preferredHeight, heightMeasureSpec);
		widthSize = resolveSize(preferredWidth, widthMeasureSpec);

		setMeasuredDimension(widthSize, heightSize);
		mHeightMeasureSpec = heightMeasureSpec;
		mWidthMeasureSpec = widthMeasureSpec;
	}

	int getChildHeight(View child) {
		return child.getMeasuredHeight();
	}

	int getChildWidth(View child) {
		return child.getMeasuredWidth();
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
	}

	void recycleAllViews() {
		final int childCount = getChildCount();
		final AbsSpinner.RecycleBin recycleBin = mRecycler;
		final int position = mFirstPosition;

		// All views go in recycler
		for (int i = 0; i < childCount; i++) {
			View v = getChildAt(i);
			int index = position + i;
			recycleBin.put(index, v);
		}
	}

	/**
	 * Jump directly to a specific item in the adapter data.
	 */
	public void setSelection(int position, boolean animate) {
		// Animate only if requested position is already on screen somewhere
		boolean shouldAnimate = animate && mFirstPosition <= position &&
			position <= mFirstPosition + getChildCount() - 1;
		setSelectionInt(position, shouldAnimate);
	}

	@Override
	public void setSelection(int position) {
		setNextSelectedPositionInt(position);
		requestLayout();
		invalidate();
	}


	/**
	 * Makes the item at the supplied position selected.
	 *
	 * @param position Position to select
	 * @param animate Should the transition be animated
	 *
	 */
	void setSelectionInt(int position, boolean animate) {
		if (position != mOldSelectedPosition) {
			mBlockLayoutRequests = true;
			int delta  = position - mSelectedPosition;
			setNextSelectedPositionInt(position);
			layout(delta, animate);
			mBlockLayoutRequests = false;
		}
	}

	abstract void layout(int delta, boolean animate);

	@Override
	public View getSelectedView() {
		if (mItemCount > 0 && mSelectedPosition >= 0) {
			return getChildAt(mSelectedPosition - mFirstPosition);
		}
		else {
			return null;
		}
	}

	/**
	 * Override to prevent spamming ourselves with layout requests
	 * as we place views
	 *
	 * @see android.view.View#requestLayout()
	 */
	@Override
	public void requestLayout() {
		if (!mBlockLayoutRequests) {
			super.requestLayout();
		}
	}

	@Override
	public SpinnerAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public int getCount() {
		return mItemCount;
	}

	/**
	 * Maps a point to a position in the list.
	 *
	 * @param x X in local coordinate
	 * @param y Y in local coordinate
	 * @return The position of the item which contains the specified point, or
	 *         {@link #INVALID_POSITION} if the point does not intersect an item.
	 */
	public int pointToPosition(int x, int y) {
		Rect frame = mTouchFrame;
		if (frame == null) {
			mTouchFrame = new Rect();
			frame = mTouchFrame;
		}

		final int count = getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			View child = getChildAt(i);
			if (child.getVisibility() == View.VISIBLE) {
				child.getHitRect(frame);
				if (frame.contains(x, y)) {
					return mFirstPosition + i;
				}
			}
		}
		return INVALID_POSITION;
	}

	static class SavedState extends BaseSavedState {
		long selectedId;
		int position;

		/**
		 * Constructor called from {@link AbsSpinner#onSaveInstanceState()}
		 */
		SavedState(Parcelable superState) {
			super(superState);
		}

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(Parcel in) {
			super(in);
			selectedId = in.readLong();
			position = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeLong(selectedId);
			out.writeInt(position);
		}

		@Override
		public String toString() {
			return "AbsSpinner.SavedState{"
				+ Integer.toHexString(System.identityHashCode(this))
				+ " selectedId=" + selectedId
				+ " position=" + position + "}";
		}

		public static final Parcelable.Creator<SavedState> CREATOR
			= new Parcelable.Creator<SavedState>() {
				public SavedState createFromParcel(Parcel in) {
					return new SavedState(in);
				}

				public SavedState[] newArray(int size) {
					return new SavedState[size];
				}
			};
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState ss = new SavedState(superState);
		ss.selectedId = getSelectedItemId();
		if (ss.selectedId >= 0) {
			ss.position = getSelectedItemPosition();
		}
		else {
			ss.position = INVALID_POSITION;
		}
		return ss;
	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;

		super.onRestoreInstanceState(ss.getSuperState());

		if (ss.selectedId >= 0) {
			mDataChanged = true;
			mNeedSync = true;
			mSyncRowId = ss.selectedId;
			mSyncPosition = ss.position;
			mSyncMode = SYNC_SELECTED_POSITION;
			requestLayout();
		}
	}

	class RecycleBin {
		private final SparseArray<View> mScrapHeap = new SparseArray<View>();

		public void put(int position, View v) {
			mScrapHeap.put(position, v);
		}

		View get(int position) {
			// System.out.print("Looking for " + position);
			View result = mScrapHeap.get(position);
			if (result != null) {
				// System.out.println(" HIT");
				mScrapHeap.delete(position);
			}
			else {
				// System.out.println(" MISS");
			}
			return result;
		}

		void clear() {
			final SparseArray<View> scrapHeap = mScrapHeap;
			final int count = scrapHeap.size();
			for (int i = 0; i < count; i++) {
				final View view = scrapHeap.valueAt(i);
				if (view != null) {
					removeDetachedView(view, true);
				}
			}
			scrapHeap.clear();
		}
	}
}
