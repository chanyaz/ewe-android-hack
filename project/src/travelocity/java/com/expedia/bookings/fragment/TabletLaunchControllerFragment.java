package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.LayoutUtils;
import com.squareup.otto.Subscribe;

public class TabletLaunchControllerFragment extends AbsTabletLaunchControllerFragment implements
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	private TabletLaunchDestinationTilesFragment mTilesFragment;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState != null) {
			FragmentManager fm = getChildFragmentManager();
			mTilesFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_TILES);
		}

		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSearchBarC.getLayoutParams();
		params.topMargin = LayoutUtils.getActionBarSize(getActivity()) + getResources()
			.getDimensionPixelSize(R.dimen.tablet_launch_search_bar_top_margin);
		mSearchBarC.setLayoutParams(params);

		return mRootC;
	}

	//IFragmentAvailabilityProvider

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		switch (tag) {
		case FRAG_TAG_TILES:
			return mTilesFragment;
		case FRAG_TAG_WAYPOINT:
			return mWaypointFragment;
		default:
			return null;
		}
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		switch (tag) {
		case FRAG_TAG_TILES:
			return TabletLaunchDestinationTilesFragment.newInstance();
		case FRAG_TAG_WAYPOINT:
			return TabletWaypointFragment.newInstance(true);
		default:
			return null;
		}
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		// Ignore
	}

	protected void setFragmentState(LaunchState state) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean showFrags = true;
		if (state == LaunchState.NO_CONNECTIVITY) {
			showFrags = false;
		}

		mTilesFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(showFrags, FRAG_TAG_TILES, manager, transaction, this, R.id.tiles_container,
				false);
		mWaypointFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(showFrags, FRAG_TAG_WAYPOINT, manager, transaction, this, R.id.waypoint_container,
				false);
		transaction.commit();
	}

	@Override
	public boolean isMeasurable() {
		return mTilesFragment != null && mTilesFragment.isMeasurable();
	}

	/*
	 * Otto events
	 */

	@Subscribe
	public void onSearchSuggestionSelected(Events.SearchSuggestionSelected event) {
		if (event.suggestion != null) {
			if (event.isFromSavedParamsAndBucket) {
				Sp.loadSearchParamsFromDisk(getActivity());
			}
			else {
				Sp.getParams().restoreToDefaults();
				Sp.getParams().setDestination(event.suggestion);
				// Deleting the trip bucket removes air attach qualification
				// so lets just clear it out and leave air attach alone
				// Db.deleteTripBucket(getActivity());
				Db.getTripBucket().clear();
				Db.saveTripBucket(getActivity());
			}
			doSearch();
		}
	}

}
