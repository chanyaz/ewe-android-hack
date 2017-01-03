package com.expedia.bookings.launch.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.TabletWaypointFragment;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.launch.util.LaunchScreenAnimationUtil;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

public class TabletLaunchControllerFragment extends AbsTabletLaunchControllerFragment implements
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider {
	protected static final String FRAG_TAG_LIST = "FRAG_TAG_LIST";
	private TabletLaunchDestinationListFragment mListFragment;
	private TabletLaunchDestinationTilesFragment mTilesFragment;
	private View mListDetailContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState != null) {
			FragmentManager fm = getChildFragmentManager();
			mListFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_LIST);
			mTilesFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_TILES);
		}
		FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mSearchBarC.getLayoutParams();
		params.topMargin = LaunchScreenAnimationUtil.getActionBarNavBarSize(getActivity()) + getResources()
			.getDimensionPixelSize(R.dimen.search_bar_margin_top);
		mSearchBarC.setLayoutParams(params);
		mListDetailContainer = Ui.findView(mRootC, R.id.list_detail_container);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mStateManager.getState() == LaunchState.DESTINATION_LIST) {
			switchListFragment(true);
		}
	}

	//IFragmentAvailabilityProvider
	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		switch (tag) {
		case FRAG_TAG_LIST:
			return mListFragment;
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
		case FRAG_TAG_LIST:
			return TabletLaunchDestinationListFragment.newInstance();
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

	@Override
	public BackManager getBackManager() {
		if (getLaunchState() == LaunchState.DESTINATION_LIST) {
			return mBackManager;
		}
		return super.getBackManager();
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			if (getLaunchState() == LaunchState.DESTINATION_LIST) {
				switchListFragment(false);
				return true;
			}
			return false;
		}
	};

	protected void setFragmentState(LaunchState state) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean showFrags = true;
		if (state == LaunchState.NO_CONNECTIVITY) {
			showFrags = false;
		}

		mListFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(showFrags, FRAG_TAG_LIST, manager, transaction, this, R.id.list_detail_container,
				false);
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

		return mListFragment != null && mTilesFragment != null && mTilesFragment.isMeasurable();
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

	public void switchListFragment(boolean isDestinationList) {
		if (isDestinationList) {
			mListDetailContainer.setVisibility(View.VISIBLE);
			mTilesFragment.getView().setVisibility(View.GONE);
			setLaunchState(LaunchState.DESTINATION_LIST, false);
		}
		else {
			mListDetailContainer.setVisibility(View.GONE);
			mTilesFragment.getView().setVisibility(View.VISIBLE);
			LaunchCard.clearHistory();
			setLaunchState(LaunchState.OVERVIEW, false);
		}
		switchActionbar(isDestinationList);
	}

	private void switchActionbar(boolean isDestinationList) {
		ActionBar actionBar = getActivity().getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(isDestinationList);
		actionBar.setHomeButtonEnabled(isDestinationList);

		if (isDestinationList) {
			mAbText1.setAlpha(0f);
			mAbText2.setAlpha(1f);
		}
		else {
			mAbText1.setAlpha(1f);
			mAbText2.setAlpha(0f);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (android.R.id.home == item.getItemId()) {
			((TabletLaunchControllerFragment) getParentFragment()).switchListFragment(false);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean shouldDisplayMenu() {
		return (mStateManager.getState() == LaunchState.OVERVIEW
			|| mStateManager.getState() == LaunchState.DESTINATION_LIST) && !mStateManager.isAnimating();
	}
}
