package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ITabletResultsController;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.fragment.ResultsHotelListFragment.ISortAndFilterListener;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.mobiata.android.util.Ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 *  TabletResultsHotelControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to HOTELS results
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class TabletResultsHotelControllerFragment extends Fragment implements SupportMapFragmentListener,
		ITabletResultsController, ISortAndFilterListener {

	public interface IHotelsFruitScrollUpListViewChangeListener {
		public void onHotelsStateChanged(State oldState, State newState, float percentage, View requester);

		public void onHotelsPercentageChanged(State state, float percentage);
	}

	private IFruitScrollUpListViewChangeListener mFruitProxy = new IFruitScrollUpListViewChangeListener() {
		@Override
		public void onStateChanged(State oldState, State newState, float percentage) {
			if (mListener != null) {
				mListener.onHotelsStateChanged(oldState, newState, percentage, mHotelListC);
			}
		}

		@Override
		public void onPercentageChanged(State state, float percentage) {
			if (mListener != null) {
				mListener.onHotelsPercentageChanged(state, percentage);
			}
		}
	};

	private enum HotelsState {
		DEFAULT, FILTERS
	}

	//State
	private static final String STATE_HOTELS_STATE = "STATE_HOTELS_STATE";
	private static final String STATE_GLOBAL_STATE = "STATE_GLOBAL_STATE";

	//Tags
	private static final String FRAG_TAG_HOTEL_LIST = "FRAG_TAG_HOTEL_LIST";
	private static final String FRAG_TAG_HOTEL_FILTERS = "FRAG_TAG_HOTEL_FILTERS";
	private static final String FRAG_TAG_HOTEL_FILTERED_COUNT = "FRAG_TAG_HOTEL_FILTERED_COUNT";
	private static final String FRAG_TAG_HOTEL_MAP = "FRAG_TAG_HOTEL_MAP";

	//Containers
	private ViewGroup mRootC;
	private BlockEventFrameLayout mHotelListC;
	private BlockEventFrameLayout mBgHotelMapC;
	private BlockEventFrameLayout mHotelFiltersC;
	private BlockEventFrameLayout mHotelFilteredCountC;

	//Fragments
	private SupportMapFragment mMapFragment;
	private ResultsHotelListFragment mHotelListFrag;
	private ResultsHotelsFiltersFragment mHotelFiltersFrag;
	private ResultsHotelsFilterCountFragment mHotelFilteredCountFrag;

	//Other
	private GlobalResultsState mGlobalState = GlobalResultsState.DEFAULT;
	private HotelsState mHotelsState = HotelsState.DEFAULT;
	private IHotelsFruitScrollUpListViewChangeListener mListener;
	private ColumnManager mColumnManager = new ColumnManager(3);

	//Animation
	private ValueAnimator mHotelsStateAnimator;
	private HotelsState mDestinationHotelsState;
	private static final int STATE_CHANGE_ANIMATION_DURATION = 150;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, IHotelsFruitScrollUpListViewChangeListener.class, true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_tablet_results_hotels, null, false);

		mRootC = Ui.findView(view, R.id.root_layout);
		mHotelListC = Ui.findView(view, R.id.column_one_hotel_list);
		mBgHotelMapC = Ui.findView(view, R.id.bg_hotel_map);
		mHotelFiltersC = Ui.findView(view, R.id.column_one_hotel_filters);
		mHotelFilteredCountC = Ui.findView(view, R.id.column_three_hotel_filtered_count);

		if (savedInstanceState != null) {
			mGlobalState = GlobalResultsState.valueOf(savedInstanceState.getString(STATE_GLOBAL_STATE,
					GlobalResultsState.DEFAULT.name()));
			mHotelsState = HotelsState.valueOf(savedInstanceState.getString(STATE_HOTELS_STATE,
					HotelsState.DEFAULT.name()));
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_HOTELS_STATE, mHotelsState.name());
		outState.putString(STATE_GLOBAL_STATE, mGlobalState.name());
	}

	/**
	 * HOTELS STATE 
	 */

	private void setHotelsState(HotelsState state, boolean animate) {
		if (!animate) {
			if (mHotelsStateAnimator != null && mHotelsStateAnimator.isStarted()) {
				mHotelsStateAnimator.cancel();
			}
			finalizeHotelsState(state);
		}
		else {
			if (mHotelsStateAnimator == null) {
				float startValue = state == HotelsState.FILTERS ? 0f : 1f;
				final float endValue = state == HotelsState.FILTERS ? 1f : 0f;
				mDestinationHotelsState = state;
				mHotelsStateAnimator = ValueAnimator.ofFloat(startValue, endValue).setDuration(STATE_CHANGE_ANIMATION_DURATION);
				mHotelsStateAnimator.addUpdateListener(new AnimatorUpdateListener() {

					@Override
					public void onAnimationUpdate(ValueAnimator arg0) {
						setHotelsFiltersShownPercentage((Float) arg0.getAnimatedValue());
					}

				});
				mHotelsStateAnimator.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator arg0) {
						setHotelsFiltersAnimationHardwareRendering(false);
						finalizeHotelsState(mDestinationHotelsState);
					}
				});

				mHotelListFrag.setListLockedToTop(true);
				mHotelFiltersC.setVisibility(View.VISIBLE);
				mHotelFilteredCountC.setVisibility(View.VISIBLE);
				setHotelsFiltersAnimationHardwareRendering(true);
				mHotelsStateAnimator.start();
			}
			else if (mDestinationHotelsState != state) {
				mDestinationHotelsState = state;
				mHotelsStateAnimator.reverse();
			}
		}
	}

	private void finalizeHotelsState(HotelsState state) {
		mHotelListFrag.setListLockedToTop(state == HotelsState.FILTERS);
		switch (state) {
		case DEFAULT: {
			setHotelsFiltersShownPercentage(0f);
			setVisibilityState(mGlobalState, state);
			mHotelListFrag.setSortAndFilterButtonText(getString(R.string.sort_and_filter));
			break;
		}
		case FILTERS: {
			setVisibilityState(mGlobalState, state);
			setHotelsFiltersShownPercentage(1f);
			mHotelListFrag.setSortAndFilterButtonText(getString(R.string.done));
			break;
		}
		}
		mHotelsState = state;
		mDestinationHotelsState = null;
		mHotelsStateAnimator = null;
		setTouchState(mGlobalState, state);
	}

	private void setHotelsFiltersAnimationHardwareRendering(boolean useHardwareLayer) {
		int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mHotelListC.setLayerType(layerValue, null);
		mHotelFiltersC.setLayerType(layerValue, null);
		mHotelFilteredCountC.setLayerType(layerValue, null);
	}

	private void setHotelsFiltersShownPercentage(float percentage) {
		mHotelListC.setTranslationX(percentage * mColumnManager.getColLeft(1));
		
		float filtersLeft = mColumnManager.getColLeft(0) - ((1f - percentage) * mColumnManager.getColWidth(0));
		mHotelFiltersC.setTranslationX(filtersLeft);
		
		float filteredCountLeft = mColumnManager.getColWidth(2) * (1f - percentage);
		mHotelFilteredCountC.setTranslationX(filteredCountLeft);
	}

	/**
	 * STATE HELPERS
	 */

	private void setTouchState(GlobalResultsState globalState, HotelsState hotelsState) {
		switch (globalState) {
		case HOTELS: {
			if (hotelsState == HotelsState.DEFAULT) {
				mBgHotelMapC.setBlockNewEventsEnabled(false);
			}
			else {
				mBgHotelMapC.setBlockNewEventsEnabled(true);
			}
			mHotelListC.setBlockNewEventsEnabled(false);
			break;
		}
		case DEFAULT: {
			mBgHotelMapC.setBlockNewEventsEnabled(true);
			mHotelListC.setBlockNewEventsEnabled(false);
			break;
		}
		default: {
			mBgHotelMapC.setBlockNewEventsEnabled(true);
			mHotelListC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setVisibilityState(GlobalResultsState globalState, HotelsState hotelsState) {
		switch (globalState) {
		case HOTELS: {
			if (hotelsState == HotelsState.FILTERS) {
				mHotelFiltersC.setVisibility(View.VISIBLE);
				mHotelFilteredCountC.setVisibility(View.VISIBLE);
			}
			else {
				mHotelFiltersC.setVisibility(View.INVISIBLE);
				mHotelFilteredCountC.setVisibility(View.INVISIBLE);
			}
			mBgHotelMapC.setVisibility(View.VISIBLE);
			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		case DEFAULT: {
			mBgHotelMapC.setVisibility(View.INVISIBLE);
			mHotelListC.setVisibility(View.VISIBLE);
			mHotelFiltersC.setVisibility(View.GONE);
			mHotelFilteredCountC.setVisibility(View.GONE);
			break;
		}
		default: {
			mBgHotelMapC.setVisibility(View.GONE);
			mHotelListC.setVisibility(View.GONE);
			mHotelFiltersC.setVisibility(View.GONE);
			mHotelFilteredCountC.setVisibility(View.GONE);
			break;
		}
		}
	}

	private void setFragmentState(GlobalResultsState globalState, HotelsState hotelsState) {

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		getChildFragmentManager().executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();

		boolean hotelListAvailable = true;
		boolean hotelMapAvailable = true;
		boolean hotelFiltersAvailable = true;
		boolean hotelFilteredCountAvailable = true;

		if (globalState != GlobalResultsState.HOTELS) {
			hotelMapAvailable = false;
			hotelFiltersAvailable = false;
			hotelFilteredCountAvailable = false;
		}

		//Hotel list
		setHotelListFragmentAvailability(hotelListAvailable, transaction);

		//Hotel Map
		setHotelsMapFragmentAvailability(hotelMapAvailable, transaction);

		//Hotel filters
		setHotelFiltersFragmentAvailability(hotelFiltersAvailable, transaction);

		//Hotel Filtered count fragment
		setHotelFilteredCountFragmentAvailability(hotelFilteredCountAvailable, transaction);

		transaction.commit();

	}

	/**
	 * FRAGMENT HELPERS
	 */

	private FragmentTransaction setHotelListFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mHotelListFrag == null || !mHotelListFrag.isAdded()) {
				if (mHotelListFrag == null) {
					mHotelListFrag = (ResultsHotelListFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_HOTEL_LIST);
				}
				if (mHotelListFrag == null) {
					mHotelListFrag = new ResultsHotelListFragment();
				}
				if (!mHotelListFrag.isAdded()) {
					transaction.add(R.id.column_one_hotel_list, mHotelListFrag, FRAG_TAG_HOTEL_LIST);
				}
				mHotelListFrag.setChangeListener(mFruitProxy);
			}
		}
		else {
			if (mHotelListFrag == null) {
				mHotelListFrag = (ResultsHotelListFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_HOTEL_LIST);
			}
			if (mHotelListFrag != null) {
				transaction.remove(mHotelListFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelFiltersFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mHotelFiltersFrag == null || !mHotelFiltersFrag.isAdded()) {
				if (mHotelFiltersFrag == null) {
					mHotelFiltersFrag = (ResultsHotelsFiltersFragment) getChildFragmentManager().findFragmentByTag(
							FRAG_TAG_HOTEL_FILTERS);
				}
				if (mHotelFiltersFrag == null) {
					mHotelFiltersFrag = new ResultsHotelsFiltersFragment();
				}
				if (!mHotelFiltersFrag.isAdded()) {
					transaction.add(R.id.column_one_hotel_filters, mHotelFiltersFrag, FRAG_TAG_HOTEL_FILTERS);
				}
			}
		}
		else {
			if (mHotelFiltersFrag == null) {
				mHotelFiltersFrag = (ResultsHotelsFiltersFragment) getChildFragmentManager().findFragmentByTag(
						FRAG_TAG_HOTEL_FILTERS);
			}
			if (mHotelFiltersFrag != null) {
				transaction.remove(mHotelFiltersFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelFilteredCountFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mHotelFilteredCountFrag == null || !mHotelFilteredCountFrag.isAdded()) {
				if (mHotelFilteredCountFrag == null) {
					mHotelFilteredCountFrag = (ResultsHotelsFilterCountFragment) getChildFragmentManager()
							.findFragmentByTag(
									FRAG_TAG_HOTEL_FILTERED_COUNT);
				}
				if (mHotelFilteredCountFrag == null) {
					mHotelFilteredCountFrag = new ResultsHotelsFilterCountFragment();
				}
				if (!mHotelFilteredCountFrag.isAdded()) {
					transaction.add(R.id.column_three_hotel_filtered_count, mHotelFilteredCountFrag,
							FRAG_TAG_HOTEL_FILTERED_COUNT);
				}
			}
		}
		else {
			if (mHotelFilteredCountFrag == null) {
				mHotelFilteredCountFrag = (ResultsHotelsFilterCountFragment) getChildFragmentManager()
						.findFragmentByTag(
								FRAG_TAG_HOTEL_FILTERED_COUNT);
			}
			if (mHotelFilteredCountFrag != null) {
				transaction.remove(mHotelFilteredCountFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelsMapFragmentAvailability(boolean available, FragmentTransaction transaction) {
		//More initialization in onMapLayout
		if (available) {
			if (mMapFragment == null || !mMapFragment.isAdded()) {

				if (mMapFragment == null) {
					mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FRAG_TAG_HOTEL_MAP);
				}
				if (mMapFragment == null) {
					mMapFragment = SupportMapFragment.newInstance();
				}
				if (!mMapFragment.isAdded()) {
					transaction.add(R.id.bg_hotel_map, mMapFragment, FRAG_TAG_HOTEL_MAP);
				}
			}
		}
		else {
			if (mMapFragment == null) {
				mMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentByTag(FRAG_TAG_HOTEL_MAP);
			}
			if (mMapFragment != null) {
				transaction.remove(mMapFragment);
			}
		}
		return transaction;
	}

	/**
	 * MAP LISTENER, WE SHOULD SET THESE COORDINATES TO WHEREVER THE HOTEL SEARCH IS ....
	 */
	@Override
	public void onMapLayout() {
		if (mMapFragment != null) {
			mMapFragment.setInitialCameraPosition(CameraUpdateFactory.newLatLngBounds(
					SupportMapFragment.getAmericaBounds(),
					0));
		}
	}

	/**
	 * ITabletResultsController Functions
	 */

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		mGlobalState = state;
		HotelsState tmpHotelsState = state != GlobalResultsState.HOTELS ? HotelsState.DEFAULT : mHotelsState;

		setTouchState(state, tmpHotelsState);
		setVisibilityState(state, tmpHotelsState);
		setFragmentState(state, tmpHotelsState);

		setHotelsState(tmpHotelsState, false);
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		case HOTELS: {
			mBgHotelMapC.setVisibility(View.VISIBLE);
			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		}
	}

	@Override
	public void setHardwareLayerFlightsTransition(boolean useHardwareLayer) {
		int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mHotelListC.setLayerType(layerValue, null);
	}

	@Override
	public void setHardwareLayerHotelsTransition(boolean useHardwareLayer) {
		int layerValue = useHardwareLayer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mBgHotelMapC.setLayerType(layerValue, null);
	}

	@Override
	public void blockAllNewTouches(View requester) {
		if (mBgHotelMapC != requester) {
			mBgHotelMapC.setBlockNewEventsEnabled(true);
		}
		if (mHotelListC != requester) {
			mHotelListC.setBlockNewEventsEnabled(true);
		}
		if (mHotelFiltersC != requester) {
			mHotelFiltersC.setBlockNewEventsEnabled(true);
		}
		if (mHotelFilteredCountC != requester) {
			mHotelFilteredCountC.setBlockNewEventsEnabled(true);
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		mHotelListC.setAlpha(percentage);
	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		mBgHotelMapC.setAlpha(1f - percentage);
	}

	@Override
	public void updateColumnWidths(int totalWidth) {
		mColumnManager.setTotalWidth(totalWidth);

		mColumnManager.setContainerToColumn(mHotelListC, 0);
		mColumnManager.setContainerToColumn(mHotelFiltersC, 0);
		mColumnManager.setContainerToColumn(mHotelFilteredCountC, 2);
		mColumnManager.setContainerToColumnSpan(mBgHotelMapC, 0, 2);

	}

	@Override
	public boolean handleBackPressed() {
		if (mGlobalState == GlobalResultsState.HOTELS) {
			if (mHotelsStateAnimator != null) {
				//If we are in the middle of state transition, just reverse it
				this.setHotelsState(mHotelsState, true);
				return true;
			}
			else {
				if (mHotelsState == HotelsState.FILTERS) {
					this.setHotelsState(HotelsState.DEFAULT, true);
					return true;
				}
				else if (mHotelsState == HotelsState.DEFAULT) {
					mHotelListFrag.gotoBottomPosition();
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * ISortAndFilterListener Functions
	 */

	@Override
	public void onSortAndFilterClicked() {
		if (mHotelsState == HotelsState.DEFAULT) {
			setHotelsState(HotelsState.FILTERS, true);
		}
		else {
			setHotelsState(HotelsState.DEFAULT, true);
		}
	}

}
