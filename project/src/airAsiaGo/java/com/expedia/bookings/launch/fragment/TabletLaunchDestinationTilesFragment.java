package com.expedia.bookings.launch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.launch.data.LaunchCollection;
import com.expedia.bookings.launch.data.LaunchDb;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.dialog.NoLocationServicesDialog;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.CollectionStack;
import com.expedia.bookings.widget.HorizontalScrollView;
import com.squareup.otto.Subscribe;

public class TabletLaunchDestinationTilesFragment extends MeasurableFragment
	implements HorizontalScrollView.OnScrollListener {

	private HorizontalScrollView mScrollView;
	private LinearLayout mItemsContainer;
	private CollectionStack mSelectedStack;

	public static TabletLaunchDestinationTilesFragment newInstance() {
		TabletLaunchDestinationTilesFragment frag = new TabletLaunchDestinationTilesFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_tablet_launch_destination_tiles, container, false);

		mScrollView = Ui.findView(root, R.id.destinations_scrollview);
		mScrollView.addOnScrollListener(this);
		mItemsContainer = Ui.findView(root, R.id.destinations_container);

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
	public void onStop() {
		super.onStop();
		cleanupCollections();
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
			paddingRight =
				mItemsContainer.getWidth() - mItemsContainer.getChildAt(mItemsContainer.getChildCount() - 1).getRight();
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
				int stackHalfWidth = (right - left) / 2;
				int stackCenter = left + stackHalfWidth;
				int distance = scrollCenter - stackCenter;
				float amount = distance / (scrollHalfWidth + stackHalfWidth);
				stack.setStackPosition(amount);
			}
		}
	}

	@Subscribe
	public void onLaunchCollectionsAvailable(Events.LaunchCollectionsAvailable event) {
		clearCollections();
		if (getActivity() != null && event.collections != null) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			for (LaunchCollection collection : event.collections) {
				addCollection(inflater, collection);
			}
			onLaunchCollectionClicked(new Events.LaunchCollectionClicked(event.selectedCollection));
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
		mSelectedStack = stack;
		stack.setCheckEnabled(true);
	}

	private void clearCollections() {
		mItemsContainer.removeAllViews();
		mSelectedStack = null;
	}

	private void clearChecks() {
		for (int i = 0; i < mItemsContainer.getChildCount(); i++) {
			CollectionStack c = (CollectionStack) mItemsContainer.getChildAt(i);
			c.setCheckEnabled(false);
		}
	}

	private void addCollection(LayoutInflater inflater, final LaunchCollection collectionToAdd) {
		final CollectionStack c = (CollectionStack) inflater
			.inflate(R.layout.snippet_destination_stack, mItemsContainer, false);
		c.setLaunchCollection(collectionToAdd);
		c.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				OmnitureTracking.trackTabletLaunchTileSelect(collectionToAdd.id);
				if (collectionToAdd.id.equals(LaunchDb.YOUR_SEARCH_TILE_ID)) {
					Events.post(new Events.SearchSuggestionSelected(collectionToAdd.locations.get(0).location, true));
				}
				else if (collectionToAdd.id.equals(LaunchDb.CURRENT_LOCATION_SEARCH_TILE_ID)) {
					if (null == collectionToAdd.locations || collectionToAdd.locations.isEmpty()) {
						// Show the message to user to enable location
						NoLocationServicesDialog dialog = NoLocationServicesDialog.newInstance();
						dialog.show(getFragmentManager(), "NO_LOCATION_FRAG");
					}
					else {
						//Deeplink the current location to Hotel Mode
						SuggestionV2 destination = collectionToAdd.locations.get(0).location;
						destination.setResultType(SuggestionV2.ResultType.CURRENT_LOCATION);
						SearchParams mSearchParams = new SearchParams();
						mSearchParams.setDestination(destination);
						NavUtils.goToTabletResults(getActivity(), mSearchParams, LineOfBusiness.HOTELS);
					}
				}
				else if (mSelectedStack != (CollectionStack) view) {
					// Only fire event if the stack isn't already selected
					Events.post(new Events.LaunchCollectionClicked(collectionToAdd));
					setCheckedCollection(c);
				}
			}
		});
		mItemsContainer.addView(c);
	}

	private void cleanupCollections() {
		if (mItemsContainer != null) {
			for (int i = 0; i < mItemsContainer.getChildCount(); i++) {
				CollectionStack stack = (CollectionStack) mItemsContainer.getChildAt(i);
				stack.cleanup();
			}
		}
	}
}
