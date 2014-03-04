package com.expedia.bookings.test.tests.pageModels.tablet;

import java.util.List;

import android.database.Cursor;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterViewAnimator;
import android.widget.AdapterViewFlipper;

import com.google.android.apps.common.testing.ui.espresso.action.AdapterViewProtocol;
import com.google.android.apps.common.testing.ui.espresso.action.AdapterViewProtocol.AdaptedData;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;

import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static com.google.common.base.Preconditions.checkArgument;

public class CursorAdapterViewProtocol implements AdapterViewProtocol {
	private static final int FULLY_RENDERED_PERCENTAGE_CUTOFF = 90;

	public Object getDataFromCursor(CursorAdapter cursorAdapter, Cursor cursor) {
		return cursorAdapter.convertToString(cursor);
	}

	@Override
	public Iterable<AdaptedData> getDataInAdapterView(AdapterView<? extends Adapter> adapterView) {
		CursorAdapter adapter = (CursorAdapter) adapterView.getAdapter();

		List<AdaptedData> datas = Lists.newArrayList();
		for (int i = 0; i < adapterView.getCount(); i++) {
			Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
			datas.add(new AdaptedData.Builder()
					.withData(getDataFromCursor(adapter, cursor))
					.withOpaqueToken(i)
					.build());
		}
		return datas;
	}

	@Override
	public Optional<AdaptedData> getDataRenderedByView(AdapterView<? extends Adapter> adapterView, View descendantView) {
		if (adapterView == descendantView.getParent()) {
			int position = adapterView.getPositionForView(descendantView);
			if (position != AdapterView.INVALID_POSITION) {
				CursorAdapter adapter = (CursorAdapter) adapterView.getAdapter();
				Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
				return Optional.of(new AdaptedData.Builder()
						.withData(getDataFromCursor(adapter, cursor))
						.withOpaqueToken(Integer.valueOf(position))
						.build());
			}
		}
		return Optional.absent();
	}

	@Override
	public void makeDataRenderedWithinAdapterView(AdapterView<? extends Adapter> adapterView, AdaptedData data) {
		checkArgument(data.opaqueToken instanceof Integer, "Not my data: %s", data);
		int position = ((Integer) data.opaqueToken).intValue();

		boolean moved = false;
		// set selection should always work, we can give a little better experience if per subtype
		// though.
		if (Build.VERSION.SDK_INT > 7) {
			if (adapterView instanceof AbsListView) {
				if (Build.VERSION.SDK_INT > 10) {
					((AbsListView) adapterView).smoothScrollToPositionFromTop(position,
						adapterView.getPaddingTop(), 0);
				} else {
					((AbsListView) adapterView).smoothScrollToPosition(position);
				}
				moved = true;
			}
			if (Build.VERSION.SDK_INT > 10) {
				if (adapterView instanceof AdapterViewAnimator) {
					if (adapterView instanceof AdapterViewFlipper) {
						((AdapterViewFlipper) adapterView).stopFlipping();
					}
					((AdapterViewAnimator) adapterView).setDisplayedChild(position);
					moved = true;
				}
			}
		}
		if (!moved) {
			adapterView.setSelection(position);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean isDataRenderedWithinAdapterView(AdapterView<? extends Adapter> adapterView, AdaptedData adaptedData) {
		checkArgument(adaptedData.opaqueToken instanceof Integer, "Not my data: %s", adaptedData);
		int dataPosition = ((Integer) adaptedData.opaqueToken).intValue();

		if (Range.closed(adapterView.getFirstVisiblePosition(), adapterView.getLastVisiblePosition()).contains(dataPosition)) {
			if (adapterView.getFirstVisiblePosition() == adapterView.getLastVisiblePosition()) {
				// thats a huge element.
				return true;
			} else {
				return isElementFullyRendered(adapterView, dataPosition - adapterView.getFirstVisiblePosition());
			}
		} else {
			return false;
		}
	}

	private boolean isElementFullyRendered(AdapterView<? extends Adapter> adapterView, int childAt) {
		View element = adapterView.getChildAt(childAt);
		// Occassionally we'll have to fight with smooth scrolling logic on our definition of when
		// there is extra scrolling to be done. In particular if the element is the first or last
		// element of the list, the smooth scroller may decide that no work needs to be done to scroll
		// to the element if a certain percentage of it is on screen. Ugh. Sigh. Yuck.

		return isDisplayingAtLeast(FULLY_RENDERED_PERCENTAGE_CUTOFF).matches(element);
	}
}
