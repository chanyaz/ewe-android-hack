package com.expedia.bookings.fragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ITabletResultsController;
import com.expedia.bookings.activity.TabletResultsActivity.GlobalResultsState;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

/**
 *  TabletResultsHotelControllerFragment: designed for tablet results 2013
 *  This controls all the fragments relating to HOTELS results
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsHotelControllerFragment extends Fragment implements SupportMapFragmentListener,
		ITabletResultsController {

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

	//Tags
	private static final String FRAG_TAG_HOTEL_LIST = "FRAG_TAG_HOTEL_LIST";
	private static final String FRAG_TAG_HOTEL_MAP = "FRAG_TAG_HOTEL_MAP";

	//Containers
	private ViewGroup mRootC;
	private BlockEventFrameLayout mHotelListC;
	private BlockEventFrameLayout mBgHotelMapC;

	//Fragments
	private SupportMapFragment mMapFragment;
	private ResultsHotelListFragment mHotelListFrag;

	//Other
	private GlobalResultsState mGlobalState;
	private IHotelsFruitScrollUpListViewChangeListener mListener;
	private ColumnManager mColumnManager = new ColumnManager(3);

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

		return view;
	}

	private void setTouchState(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mBgHotelMapC.setBlockNewEventsEnabled(true);
			mHotelListC.setBlockNewEventsEnabled(false);
			break;
		}
		case HOTELS: {
			mBgHotelMapC.setBlockNewEventsEnabled(false);
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

	private void setVisibilityState(GlobalResultsState state) {
		switch (state) {
		case DEFAULT: {
			mBgHotelMapC.setVisibility(View.INVISIBLE);
			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		case HOTELS: {
			mBgHotelMapC.setVisibility(View.VISIBLE);
			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		default: {
			mBgHotelMapC.setVisibility(View.GONE);
			mHotelListC.setVisibility(View.GONE);
			break;
		}
		}
	}

	private void setFragmentState(GlobalResultsState state) {

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		getChildFragmentManager().executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = this.getChildFragmentManager().beginTransaction();

		boolean hotelListAvailable = true;
		boolean hotelMapAvailable = true;

		if (state == GlobalResultsState.FLIGHTS) {
			hotelMapAvailable = false;
		}

		//Hotel list
		setHotelListFragmentAvailability(hotelListAvailable, transaction);

		//Hotel Map
		setHotelsMapFragmentAvailability(hotelMapAvailable, transaction);

		transaction.commit();

	}

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

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		mGlobalState = state;
		setTouchState(state);
		setVisibilityState(state);
		setFragmentState(state);
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

		setContainerWidth(mHotelListC, mColumnManager.getColWidth(0), mColumnManager.getColLeft(0));
		setContainerWidth(mBgHotelMapC, mColumnManager.getColRight(2), 0);

	}

	private void setContainerWidth(ViewGroup container, int width, int leftMargin) {
		FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) container.getLayoutParams();
		if (params == null) {
			params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		params.width = width;
		params.leftMargin = leftMargin;
		container.setLayoutParams(params);
	}
}
