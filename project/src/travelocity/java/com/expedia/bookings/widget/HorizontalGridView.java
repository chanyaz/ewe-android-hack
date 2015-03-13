package com.expedia.bookings.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.expedia.bookings.util.LaunchScreenAnimationUtil;
import com.mobiata.android.util.AndroidUtils;

public class HorizontalGridView extends HorizontalScrollView {
	private BaseAdapter adapter;
	private LinearLayout rootLinearLayout;
	private ColumnProvider columnProvider;

	public HorizontalGridView(Context context) {
		super(context);
	}

	public HorizontalGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public HorizontalGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		rootLinearLayout = new LinearLayout(getContext());
		rootLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		LayoutParams rootLinearLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
			LayoutParams.MATCH_PARENT);
		rootLinearLayout.setLayoutParams(rootLinearLayoutParams);

		TypedArray ta = getContext().obtainStyledAttributes(attrs, new int[] { android.R.attr.rowHeight });
		int rowHeight = ta.getDimensionPixelSize(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		ta.recycle();

		int containerHeight =
			AndroidUtils.getDisplaySize(getContext()).y - LaunchScreenAnimationUtil.getActionBarNavBarSize(
				(Activity) getContext()) - LaunchScreenAnimationUtil.getMarginBottom((Activity) getContext());
		columnProvider = new ColumnProvider(rootLinearLayout, containerHeight / rowHeight);
	}

	private DataSetObserver dataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();

			removeAllViews();
			rootLinearLayout.removeAllViews();

			for (int index = 0; index < adapter.getCount(); index++) {
				columnProvider.addViewInLayout(adapter.getView(index, null, rootLinearLayout));
			}
			addView(rootLinearLayout);
		}
	};

	public void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
		adapter.registerDataSetObserver(dataSetObserver);
	}

	private class ColumnProvider {
		private final ViewGroup parentViewGroup;
		private final int rowCount;
		private final LayoutParams newColumnLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
			LayoutParams.MATCH_PARENT);


		private LinearLayout currentColumnToAddChildrenTo;
		private int childCount = 0;

		protected ColumnProvider(ViewGroup parentViewGroup, int rowCount) {
			this.parentViewGroup = parentViewGroup;
			this.rowCount = rowCount;
		}

		protected void addViewInLayout(View view) {
			if (view != null) {
				if (childCount++ % rowCount == 0) {
					currentColumnToAddChildrenTo = createNewColumn();
					parentViewGroup.addView(currentColumnToAddChildrenTo);
				}
				currentColumnToAddChildrenTo.addView(view);
			}
		}

		private LinearLayout createNewColumn() {
			LinearLayout column = new LinearLayout(getContext());
			column.setOrientation(LinearLayout.VERTICAL);
			column.setLayoutParams(newColumnLayoutParams);
			return column;
		}
	}

}
