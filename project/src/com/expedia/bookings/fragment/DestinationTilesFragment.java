package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CollectionStack;
import com.expedia.bookings.widget.HorizontalScrollView;
import com.squareup.otto.Subscribe;

public class DestinationTilesFragment extends MeasurableFragment implements HorizontalScrollView.OnScrollListener {

	private static final int ALL_DIVIDERS = LinearLayout.SHOW_DIVIDER_MIDDLE | LinearLayout.SHOW_DIVIDER_BEGINNING | LinearLayout.SHOW_DIVIDER_END;

	private HorizontalScrollView mScrollView;
	private LinearLayout mItemsContainer;

	public static DestinationTilesFragment newInstance() {
		DestinationTilesFragment frag = new DestinationTilesFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_destination_stacks, container, false);

		mScrollView = Ui.findView(root, R.id.destinations_scrollview);
		mScrollView.addOnScrollListener(this);

		mItemsContainer = Ui.findView(root, R.id.destinations_container);
		mItemsContainer.setShowDividers(ALL_DIVIDERS);

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onScrollChanged(HorizontalScrollView scrollView, int x, int y, int oldx, int oldy) {
		int scrollCenter = mScrollView.getWidth() / 2 + x;
		float scrollHalfWidth = mScrollView.getWidth() / 2.0f;
		int paddingLeft = 0;
		int paddingRight = 0;
		int scrollViewLeft = x;
		int scrollViewRight = scrollViewLeft + mScrollView.getWidth();

		if (mItemsContainer.getChildCount() > 0) {
			paddingLeft = mItemsContainer.getChildAt(0).getLeft();
			paddingRight = mItemsContainer.getWidth() - mItemsContainer.getChildAt(mItemsContainer.getChildCount() - 1).getRight();
		}

		for (int i = 0; i < mItemsContainer.getChildCount(); i++) {
			View child = mItemsContainer.getChildAt(i);
			int left = child.getLeft();
			int right = child.getRight();
			if (right < scrollViewLeft) {
				// Not on screen, skip
				continue;
			}
			if (left > scrollViewRight) {
				// The remaining guys are not onscreen, terminate
				break;
			}

			if (child instanceof CollectionStack) {
				CollectionStack stack = (CollectionStack) child;
				int stackCenter = left + (right - left) / 2;
				int distance = (scrollCenter - stackCenter);
				float amount = -1.0f * distance / scrollHalfWidth;
				stack.setStackPosition(amount);
			}
		}
	}

	@Subscribe
	public void onLaunchCollectionsAvailable(Events.LaunchCollectionsAvailable event) {
		clearCollections();
		if (getActivity() != null) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			for (LaunchCollection collection : event.collections) {
				addCollection(inflater, collection, event.selectedCollection);
			}
		}

		mItemsContainer.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				mItemsContainer.getViewTreeObserver().removeOnPreDrawListener(this);
				onScrollChanged(mScrollView, 0, 0, 0, 0);
				return true;
			}
		});
	}

	@Subscribe
	public void onLaunchCollectionClicked(Events.LaunchCollectionClicked event) {
		clearChecks();
		for (int i = 0; i < mItemsContainer.getChildCount(); i++) {
			CollectionStack stack = (CollectionStack) mItemsContainer.getChildAt(i);
			LaunchCollection launchCollection = (LaunchCollection) stack.getTag();
			if (launchCollection.equals(event.launchCollection)) {
				setCheckedCollection(stack);
			}
		}
	}

	private void setCheckedCollection(CollectionStack stack) {
		clearChecks();
		stack.setCheckEnabled(true);
	}

	private void clearCollections() {
		mItemsContainer.removeAllViews();
	}

	private void clearChecks() {
		for (int i = 0; i < mItemsContainer.getChildCount(); i++) {
			CollectionStack c = (CollectionStack) mItemsContainer.getChildAt(i);
			c.setCheckEnabled(false);
		}
	}

	private void addCollection(LayoutInflater inflater, final LaunchCollection collectionToAdd, final LaunchCollection selectedCollection) {
		final CollectionStack c = (CollectionStack) inflater.inflate(R.layout.snippet_destination_stack, mItemsContainer, false);
		c.setStackDrawable(collectionToAdd.getImageUrl());
		c.setText(collectionToAdd.title);
		c.setTag(collectionToAdd);
		c.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Events.post(new Events.LaunchCollectionClicked(collectionToAdd));
				setCheckedCollection(c);
			}
		});
		c.setCheckEnabled(collectionToAdd.equals(selectedCollection));

		mItemsContainer.addView(c);
	}
}
