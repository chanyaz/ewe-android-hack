package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LaunchDb;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.data.SuggestionV2;
import com.expedia.bookings.dialog.NoLocationServicesDialog;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.NavUtils;
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

	private void clearCollections() {
		itemsContainer.removeAllViews();
	}

	private void addCollection(LayoutInflater inflater, final LaunchCollection collectionToAdd) {
		final DestinationCollection c = (DestinationCollection) inflater
			.inflate(R.layout.snippet_tablet_launch_destination, itemsContainer, false);
		c.setLaunchCollection(collectionToAdd);
		c.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				OmnitureTracking.trackTabletLaunchTileSelect(getActivity(), collectionToAdd.id);
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
				else {
					// Only fire event if the destination isn't already selected
					Events.post(new Events.LaunchCollectionClicked(collectionToAdd));
					((TabletLaunchControllerFragment) getParentFragment()).switchListFragment();
				}
			}
		});

		itemsContainer.addView(c);
		c.setDrawable(collectionToAdd.getImageUrl());
	}

}
