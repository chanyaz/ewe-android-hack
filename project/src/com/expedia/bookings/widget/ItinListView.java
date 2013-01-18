package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.expedia.bookings.animation.ExpandAnimation;
import com.expedia.bookings.data.trips.TripComponent;

public class ItinListView extends ListView implements OnItemClickListener, OnScrollListener {
	public static final int MODE_LIST = 0;
	public static final int MODE_DETAIL = 1;

	private ItinItemAdapter mAdapter;

	private OnItemClickListener mOnItemClickListener;
	private OnScrollListener mOnScrollListener;

	private int mMode;
	private int mDetailPosition;

	public ItinListView(Context context) {
		this(context, null);
	}

	public ItinListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		setOnItemClickListener(null);
		mContent.setOrientation(LinearLayout.VERTICAL);
		addView(mContent);

		setOnScrollListener(null);
	}

	public void addAllItinItems(List<TripComponent> items) {
		if (mAdapter == null) {
			mAdapter = new ItinItemAdapter(getContext());
			setAdapter(mAdapter);
		}

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
			showDetail();
			break;
		}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		showDetail(position);

		if (mOnItemClickListener != null) {
			mOnItemClickListener.onItemClick(parent, view, position, id);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		for (int i = firstVisibleItem; i < firstVisibleItem + visibleItemCount; i++) {
			if (getChildAt(i) != null) {
				getChildAt(i).invalidate();
			}
		}

		if (mOnScrollListener != null) {
			mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (mOnScrollListener != null) {
			mOnScrollListener.onScrollStateChanged(view, scrollState);
		}
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

	private void showList() {
		mMode = MODE_LIST;
	}

	private void showDetail() {
		showDetail(mDetailPosition);
	}

	private void showDetail(int position) {
		mMode = MODE_DETAIL;
		mDetailPosition = position;

		final int start = getFirstVisiblePosition();
		final int count = getChildCount();

		// pass scrolling to child scrollview
	}

		if (position >= start && position < start + count) {
		mContent.removeAllViews();
		mContent.addView(new View(getContext()), new LayoutParams(LayoutParams.MATCH_PARENT, mLaunchHeaderHeight));

			if (view != null) {
				view.startAnimation(new ExpandAnimation(view, getHeight()));
			}
		}

		final int size = mAdapter.getCount();
		for (int i = 0; i < size; i++) {
			final int position = i;

			View view = mAdapter.getView(i, null, mContent);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
		smoothScrollToPosition(position);
				}
			});

			mContent.addView(view);
		}
	}

	@Override
	protected void onScrollChanged(int x, int y, int oldx, int oldy) {
		Log.t("Scrolling");

		final int size = mContent.getChildCount();
		for (int i = 1; i < size; i++) {
			((ItinCard) mContent.getChildAt(i)).updateTypeIconPosition();
		}

		super.onScrollChanged(x, y, oldx, oldy);
	}

	private DataSetObserver mDataSetObserver = new DataSetObserver() {
		public void onChanged() {
			bindDataSet();
		};
	};

}
