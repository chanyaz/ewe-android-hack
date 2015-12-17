package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.expedia.bookings.R;

public class HorizontalGridView extends ScrollView {
	private BaseAdapter adapter;
	private LinearLayout rootLinearLayout;
	private DataSetObserver dataSetObserver = new DataSetObserver() {
		@Override
		public void onChanged() {
			super.onChanged();

			horizontalScrollView.removeAllViews();
			rootLinearLayout.removeAllViews();
			childViews.clear();
			lineProvider.addDummyExtraSpace();
			lineProvider.reset();
			for (int index = 0; index < adapter.getCount(); index++) {
				View newView = adapter.getView(index, null, rootLinearLayout);
				childViews.add(newView);
				lineProvider.addViewInLayout(newView);
			}
			horizontalScrollView.addView(rootLinearLayout);
		}
	};
	private LineProvider lineProvider;
	private List<View> childViews;
	private HorizontalScrollView horizontalScrollView;

	public HorizontalGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		childViews = new ArrayList<>();

		LayoutParams rootLinearLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
			LayoutParams.MATCH_PARENT);

		horizontalScrollView = new HorizontalScrollView(getContext());
		horizontalScrollView.setLayoutParams(rootLinearLayoutParams);
		horizontalScrollView.setHorizontalScrollBarEnabled(false);
		horizontalScrollView.setOverScrollMode(OVER_SCROLL_NEVER);
		addView(horizontalScrollView);

		rootLinearLayout = new LinearLayout(getContext());
		rootLinearLayout.setLayoutParams(rootLinearLayoutParams);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			rootLinearLayout.setOrientation(LinearLayout.VERTICAL);
			lineProvider = new RowProvider(rootLinearLayout);
		}
		else {
			rootLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
			lineProvider = new ColumnProvider(rootLinearLayout);
		}
	}

	public void setAdapter(BaseAdapter adapter) {
		this.adapter = adapter;
		adapter.registerDataSetObserver(dataSetObserver);
	}

	public List<View> getChildViews() {
		return childViews;
	}

	/**
	 * Rows and columns both can be considered as lines
	 */
	private abstract class LineProvider {
		private static final int ROW_OR_COLUMN_COUNT = 2;
		protected final ViewGroup parentViewGroup;
		protected LayoutParams newColumnLayoutParams;

		private LinearLayout currentViewToAddChildrenTo;
		private int childCount = 0;

		protected LineProvider(ViewGroup parentViewGroup) {
			this.parentViewGroup = parentViewGroup;
		}

		protected void reset() {
			childCount = 0;
		}

		protected void addViewInLayout(View view) {
			if (view != null) {
				if (childCount++ % ROW_OR_COLUMN_COUNT == 0) {
					currentViewToAddChildrenTo = createNewView();
					parentViewGroup.addView(currentViewToAddChildrenTo);
				}
				currentViewToAddChildrenTo.addView(view);
			}
		}


		protected abstract void addDummyExtraSpace();

		protected abstract LinearLayout createNewView();
	}

	private class ColumnProvider extends LineProvider {

		protected ColumnProvider(ViewGroup parentViewGroup) {
			super(parentViewGroup);
			newColumnLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT);
		}

		protected void addDummyExtraSpace() {
			View dummyExtraSpaceColumn = new View(getContext());
			final LayoutParams dummyExtraSpaceColumnLayoutParams = new LayoutParams(
				getResources().getDimensionPixelSize(
					R.dimen.destination_list_bg_title_overlay_width),
				LayoutParams.MATCH_PARENT);
			dummyExtraSpaceColumn.setLayoutParams(dummyExtraSpaceColumnLayoutParams);
			parentViewGroup.addView(dummyExtraSpaceColumn);
		}

		protected LinearLayout createNewView() {
			LinearLayout column = new LinearLayout(getContext());
			column.setOrientation(LinearLayout.VERTICAL);
			column.setLayoutParams(newColumnLayoutParams);
			return column;
		}
	}


	private class RowProvider extends LineProvider {

		protected RowProvider(ViewGroup parentViewGroup) {
			super(parentViewGroup);
			newColumnLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		}

		@Override
		protected void addDummyExtraSpace() {
		}

		protected LinearLayout createNewView() {
			LinearLayout column = new LinearLayout(getContext());
			column.setOrientation(LinearLayout.HORIZONTAL);
			column.setLayoutParams(newColumnLayoutParams);
			return column;
		}
	}

}
