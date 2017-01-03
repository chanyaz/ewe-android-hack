package com.expedia.bookings.launch.fragment;

import android.app.ActionBar;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Sp;
import com.expedia.bookings.enums.LaunchState;
import com.expedia.bookings.fragment.TabletWaypointFragment;
import com.expedia.bookings.graphics.SvgDrawable;
import com.expedia.bookings.interfaces.ISingleStateListener;
import com.expedia.bookings.interfaces.helpers.SingleStateListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.Ui;
import com.expedia.bookings.widget.TouchableFrameLayout;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParser;
import com.squareup.otto.Subscribe;

/**
 * Created by dmelton on 6/6/14.
 */
public class TabletLaunchControllerFragment extends AbsTabletLaunchControllerFragment implements
	FragmentAvailabilityUtils.IFragmentAvailabilityProvider {

	// Containers
	private TouchableFrameLayout mPinDetailC;

	private TabletLaunchDestinationTilesFragment mTilesFragment;

	private View mGlobeBackground;

	/*
	 * Fragment Lifecycle
	 */

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		if (savedInstanceState != null) {
			FragmentManager fm = getChildFragmentManager();
			mTilesFragment = FragmentAvailabilityUtils.getFrag(fm, FRAG_TAG_TILES);
		}

		mPinDetailC = Ui.findView(mRootC, R.id.pin_detail_container);
		mGlobeBackground = Ui.findView(mRootC, R.id.globe_background);

		// Fit width
		Matrix viewport = new Matrix();
		Point screen = Ui.getScreenSize(getActivity());
		float scale = screen.x / 3000.0f;
		viewport.preScale(scale, scale);

		SVG globeSvg = SVGParser.getSVGFromResource(getResources(), R.raw.map_tablet_launch);
		SvgDrawable globeSvgDrawable = new SvgDrawable(globeSvg, viewport);
		mGlobeBackground.setBackgroundDrawable(globeSvgDrawable);

		registerStateListener(mDetailsStateListener, false);

		return mRootC;
	}

	//IFragmentAvailabilityProvider

	@Override
	public Fragment getExistingLocalInstanceFromTag(String tag) {
		switch (tag) {
		case FRAG_TAG_MAP:
			return mMapFragment;
		case FRAG_TAG_TILES:
			return mTilesFragment;
		case FRAG_TAG_WAYPOINT:
			return mWaypointFragment;
		case FRAG_TAG_PIN:
			return mPinFragment;
		default:
			return null;
		}
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		switch (tag) {
		case FRAG_TAG_MAP:
			return TabletLaunchMapFragment.newInstance();
		case FRAG_TAG_TILES:
			return TabletLaunchDestinationTilesFragment.newInstance();
		case FRAG_TAG_WAYPOINT:
			return TabletWaypointFragment.newInstance(true);
		case FRAG_TAG_PIN:
			return TabletLaunchPinDetailFragment.newInstance();
		default:
			return null;
		}
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		//ignore
	}

	protected void setFragmentState(LaunchState state) {
		FragmentManager manager = getChildFragmentManager();
		FragmentTransaction transaction = manager.beginTransaction();

		boolean showFrags = true;
		if (state == LaunchState.CHECKING_GOOGLE_PLAY_SERVICES || state == LaunchState.NO_CONNECTIVITY) {
			showFrags = false;
		}

		mMapFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(showFrags, FRAG_TAG_MAP, manager, transaction, this, R.id.map_container, false);
		mTilesFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(showFrags, FRAG_TAG_TILES, manager, transaction, this, R.id.tiles_container,
				false);
		mWaypointFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(showFrags, FRAG_TAG_WAYPOINT, manager, transaction, this, R.id.waypoint_container,
				false);
		mPinFragment = FragmentAvailabilityUtils
			.setFragmentAvailability(showFrags, FRAG_TAG_PIN, manager, transaction, this, R.id.pin_detail_container,
				false);

		transaction.commit();
	}

	//MeasurableFragment

	@Override
	public boolean isMeasurable() {
		return mMapFragment != null && mMapFragment.isMeasurable()
			&& mTilesFragment != null && mTilesFragment.isMeasurable();
	}

	private SingleStateListener<LaunchState> mDetailsStateListener = new SingleStateListener<>(
		LaunchState.OVERVIEW, LaunchState.DETAILS, true, new ISingleStateListener() {

		private float mSearchBarY;

		@Override
		public void onStateTransitionStart(boolean isReversed) {
			mSearchBarY = mRootC.getHeight() - mSearchBarC.getTop();

			ActionBar ab = getActivity().getActionBar();
			mAbText1 = Ui.findView(ab.getCustomView(), R.id.text1);
			mAbText2 = Ui.findView(ab.getCustomView(), R.id.text2);
			getActivity().invalidateOptionsMenu();
			mSearchBarC.setVisibility(View.VISIBLE);
			mTilesC.setVisibility(View.VISIBLE);
			mPinDetailC.setVisibility(View.VISIBLE);
			mPinDetailC.setConsumeTouch(!isReversed);
			mPinDetailC.getBackground().setAlpha(isReversed ? 255 : 0);

			if (isReversed) {
				mSearchBarC.setTranslationY(mSearchBarY);
				mTilesC.setTranslationY(mSearchBarY);
			}
		}

		@Override
		public void onStateTransitionUpdate(boolean isReversed, float percentage) {
			// Slide the tiles and search bar down off the bottom of the screen
			mSearchBarC.setTranslationY(percentage * mSearchBarY);
			mTilesC.setTranslationY(percentage * mSearchBarY);
			mAbText1.setAlpha(1f - percentage);
			mAbText2.setAlpha(percentage);
			mPinDetailC.getBackground().setAlpha((int) (255f * percentage));
		}

		@Override
		public void onStateTransitionEnd(boolean isReversed) {

		}

		@Override
		public void onStateFinalized(boolean isReversed) {
			ActionBar ab = getActivity().getActionBar();
			ab.setDisplayHomeAsUpEnabled(!isReversed);
			ab.setHomeButtonEnabled(!isReversed);

			mPinDetailC.setConsumeTouch(!isReversed);
			mPinDetailC.getBackground().setAlpha(isReversed ? 0 : 255);

			if (isReversed) {
				// details hidden
				mSearchBarC.setVisibility(View.VISIBLE);
				mTilesC.setVisibility(View.VISIBLE);
				mPinDetailC.setVisibility(View.INVISIBLE);
			}
			else {
				// details showing
				mSearchBarC.setVisibility(View.INVISIBLE);
				mTilesC.setVisibility(View.INVISIBLE);
				mPinDetailC.setVisibility(View.VISIBLE);
			}

			getActivity().invalidateOptionsMenu();
		}
	}
	);

	/*
	 * Otto events
	 */
	@Subscribe
	public void onMapPinClicked(Events.LaunchMapPinClicked event) {
		setLaunchState(LaunchState.DETAILS, true);
	}

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
