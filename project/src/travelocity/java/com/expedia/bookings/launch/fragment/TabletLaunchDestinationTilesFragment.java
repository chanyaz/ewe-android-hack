package com.expedia.bookings.launch.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.launch.data.LaunchCollection;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.DestinationCollection;
import com.squareup.otto.Subscribe;

public class TabletLaunchDestinationTilesFragment extends MeasurableFragment {

	private LinearLayout itemsContainer;

	public static TabletLaunchDestinationTilesFragment newInstance() {
		TabletLaunchDestinationTilesFragment frag = new TabletLaunchDestinationTilesFragment();
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_tablet_launch_destination_tiles, container, false);

		itemsContainer = Ui.findView(root, R.id.destinations_container);
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

	@Subscribe
	public void onLaunchCollectionsAvailable(Events.LaunchCollectionsAvailable event) {
		clearCollections();
		if (getActivity() != null && event.collections != null) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			for (LaunchCollection collection : event.collections) {
				addCollection(inflater, collection);
			}
		}
	}

	@Subscribe
	public void onLaunchCollectionClicked(Events.LaunchCollectionClicked launchCollectionClicked) {
		((TabletLaunchControllerFragment) getParentFragment()).switchListFragment(true);
	}

	private void clearCollections() {
		itemsContainer.removeAllViews();
	}

	private void addCollection(LayoutInflater inflater, final LaunchCollection collectionToAdd) {
		final DestinationCollection c = (DestinationCollection) inflater
			.inflate(R.layout.snippet_tablet_launch_destination, itemsContainer, false);
		c.setLaunchCollection(collectionToAdd);

		itemsContainer.addView(c);
		c.setDrawable(collectionToAdd.getImageUrl());
	}
}
