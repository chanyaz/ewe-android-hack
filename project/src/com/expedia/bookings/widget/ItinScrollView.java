package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.expedia.bookings.R;
import com.expedia.bookings.animation.ResizeAnimation;
import com.expedia.bookings.animation.ResizeAnimation.AnimationStepListener;
import com.expedia.bookings.data.trips.TripComponent;

public class ItinScrollView extends ScrollView {
	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC CONSTANTS
	//////////////////////////////////////////////////////////////////////////////////////

	public static final int MODE_LIST = 0;
	public static final int MODE_DETAIL = 1;

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE MEMBERS
	//////////////////////////////////////////////////////////////////////////////////////

	private ItinItemAdapter mAdapter;

	private LinearLayout mContainer;
	private View mEmptyView;

	private int mMode;
	private int mDetailPosition = -1;

	private int mExpandedCardOriginalSize;
	private int mOriginalScrollY;

	private boolean mDataChanged;

	//////////////////////////////////////////////////////////////////////////////////////
	// CONSTRUCTORS
	//////////////////////////////////////////////////////////////////////////////////////

	public ItinScrollView(Context context) {
		this(context, null);
	}

	public ItinScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);

		final int headerHeight = context.getResources().getDimensionPixelSize(R.dimen.launch_header_height);
		mContainer = new LinearLayout(context);
		mContainer.setOrientation(LinearLayout.VERTICAL);
		mContainer.setPadding(0, headerHeight, 0, 0);

		addView(mContainer);

		mAdapter = new ItinItemAdapter(context);
		mAdapter.registerDataSetObserver(mDataSetObserver);
		setOnScrollListener(null);

		populateList();
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// OVERRIDES
	//////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mMode == MODE_DETAIL) {
			return mContainer.getChildAt(mDetailPosition).dispatchTouchEvent(event);
		}

		return super.onTouchEvent(event);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		if (mMode == MODE_DETAIL) {
			return mContainer.getChildAt(mDetailPosition).dispatchTouchEvent(event);
		}

		return super.onInterceptTouchEvent(event);
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		final int count = mContainer.getChildCount();
		for (int i = 1; i < count; i++) {
			if (getChildAt(i) != null) {
				getChildAt(i).invalidate();
			}
		}

		super.onScrollChanged(x, y, oldx, oldy);
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PUBLIC METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	public void clearItinItems() {
		mAdapter.clearItinItems();
	}

	public void addAllItinItems(List<TripComponent> items) {
		mAdapter.addAllItinItems(items);
	}

	public int getMode() {
		return mMode;
	}

	public void setMode(int mode) {
		switch (mode) {
		default:
		case MODE_LIST: {
			showList();
			break;
		}
		case MODE_DETAIL: {
			showDetails();
			break;
		}
		}
	}

	public void setEmptyView(View emptyView) {
		mEmptyView = emptyView;
		updateEmptyStatus(mAdapter.isEmpty());
	}

	public View getEmptyView() {
		return mEmptyView;
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE METHODS
	//////////////////////////////////////////////////////////////////////////////////////

	private void populateList() {
		mContainer.removeAllViews();

		if (mAdapter == null) {
			return;
		}

		final int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			View view = mAdapter.getView(i, null, mContainer);
			view.setOnClickListener(new ClickListener(i));

			mContainer.addView(view);
		}
	}

	private void showList() {
		mMode = MODE_LIST;

		if (mDetailPosition < 0) {
			return;
		}

		final ItinCard view = (ItinCard) mContainer.getChildAt(mDetailPosition);
		final int animationPosition = mDetailPosition;

		final int startY = getScrollY();
		final int stopY = mOriginalScrollY;
		;

		final ResizeAnimation animation = new ResizeAnimation(view, mExpandedCardOriginalSize);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				view.showSummary(animationPosition == 0);
				view.showDetails(false);
				view.setOnClickListener(new ClickListener(animationPosition));

				scrollTo(0, stopY);
			}
		});
		animation.setAnimationStepListener(new AnimationStepListener() {
			@Override
			public void onAnimationStep(Animation animation, float interpolatedTime) {
				scrollTo(0, (int) (((stopY - startY) * interpolatedTime) + startY));
			}
		});

		view.startAnimation(animation);

		mDetailPosition = -1;
	}

	private void showDetails() {
		showDetails(mDetailPosition);
	}

	private void showDetails(int position) {
		mMode = MODE_DETAIL;
		mDetailPosition = position;

		// Expand detail card
		final ItinCard view = (ItinCard) mContainer.getChildAt(mDetailPosition);

		mExpandedCardOriginalSize = view.getHeight();
		mOriginalScrollY = getScrollY();

		final int startY = getScrollY();
		final int stopY = view.getTop();

		final ResizeAnimation animation = new ResizeAnimation(view, getHeight());
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				view.showSummary(true);
				view.showDetails(true);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				scrollTo(0, stopY);
			}
		});
		animation.setAnimationStepListener(new AnimationStepListener() {
			@Override
			public void onAnimationStep(Animation animation, float interpolatedTime) {
				scrollTo(0, (int) (((stopY - startY) * interpolatedTime) + startY));
			}
		});

		view.startAnimation(animation);
	}

	private void updateEmptyStatus(boolean empty) {
		if (empty) {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.VISIBLE);
				setVisibility(View.GONE);
			}
			else {
				setVisibility(View.VISIBLE);
			}

			if (mDataChanged) {
				this.onLayout(false, getLeft(), getTop(), getRight(), getBottom());
			}
		}
		else {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.GONE);
			}
			setVisibility(View.VISIBLE);
		}
	}

	//////////////////////////////////////////////////////////////////////////////////////
	// LISTENERS
	//////////////////////////////////////////////////////////////////////////////////////

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		public void onChanged() {
			mDataChanged = true;
			updateEmptyStatus(mAdapter.isEmpty());
			populateList();
		}

		public void onInvalidated() {
			mDataChanged = true;
			updateEmptyStatus(mAdapter.isEmpty());
			populateList();
		}
	};

	//////////////////////////////////////////////////////////////////////////////////////
	// PRIVATE CLASSES
	//////////////////////////////////////////////////////////////////////////////////////

	private class ClickListener implements OnClickListener {
		private int mPosition;

		public ClickListener(int position) {
			mPosition = position;
		}

		@Override
		public void onClick(View v) {
			if (mMode == MODE_LIST) {
				showDetails(mPosition);
				v.setOnClickListener(null);
			}
		}
	}
}
