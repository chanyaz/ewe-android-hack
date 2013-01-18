package com.expedia.bookings.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.mobiata.android.Log;

public class ItinListView extends ScrollView {
	public static final int MODE_LIST = 0;
	public static final int MODE_DETAIL = 1;

	private Adapter mAdapter;

	private LinearLayout mContent;
	private int mLaunchHeaderHeight;

	private int mMode;
	private int mCurrentDetailPosition;

	public ItinListView(Context context) {
		this(context, null);
	}

	public ItinListView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContent = new LinearLayout(context);
		mContent.setOrientation(LinearLayout.VERTICAL);
		addView(mContent);

		mLaunchHeaderHeight = context.getResources().getDimensionPixelSize(R.dimen.launch_header_height);
	}

	public void setAdapter(Adapter adapter) {
		mAdapter = adapter;
		mAdapter.registerDataSetObserver(mDataSetObserver);

		bindDataSet();
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

	private void showList() {

	}

	private void showDetail() {
		showDetail(mCurrentDetailPosition);
	}

	private void showDetail(int position) {
		View view = mContent.getChildAt(position + 1);
		view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getHeight()));

		smoothScrollTo(0, view.getTop());

		// pass scrolling to child scrollview
	}

	private void bindDataSet() {
		mContent.removeAllViews();
		mContent.addView(new View(getContext()), new LayoutParams(LayoutParams.MATCH_PARENT, mLaunchHeaderHeight));

		if (mAdapter == null) {
			return;
		}

		final int size = mAdapter.getCount();
		for (int i = 0; i < size; i++) {
			final int position = i;

			View view = mAdapter.getView(i, null, mContent);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					showDetail(position);
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