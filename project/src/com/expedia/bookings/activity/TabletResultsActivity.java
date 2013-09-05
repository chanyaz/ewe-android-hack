package com.expedia.bookings.activity;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BackgroundImageCache;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment;
import com.expedia.bookings.fragment.ResultsBlurBackgroundImageFragment;
import com.expedia.bookings.fragment.ResultsFlightFiltersFragment;
import com.expedia.bookings.fragment.ResultsFlightListFragment;
import com.expedia.bookings.fragment.ResultsFlightMapFragment;
import com.expedia.bookings.fragment.ResultsHotelListFragment;
import com.expedia.bookings.fragment.ResultsTripOverviewFragment;
import com.expedia.bookings.maps.SupportMapFragment;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FixedTranslationFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.IFruitScrollUpListViewChangeListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * TabletResultsActivity: The results activity designed for tablet results 2013
 * 
 * This activity was designed to largely act as an animation controller, we expect
 * the fragments contained within to manage their own data and state, however
 * this is a good place to set up arguments and wiring.
 * 
 * NOTE: We make extensive use of hardware layer rendering for animations, so fragments
 * contained within should keep this in mind. If for example a fragment contained within
 * detects how much of itself is on screen and draws differently, this will hose our animations
 * because it will be copying the fragment into GPU memory at every single draw pass.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsActivity extends SherlockFragmentActivity implements SupportMapFragmentListener {

	//State
	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";

	//Tags
	private static final String FRAG_TAG_HOTEL_LIST = "FRAG_TAG_HOTEL_LIST";
	private static final String FRAG_TAG_FLIGHT_LIST = "FRAG_TAG_FLIGHT_LIST";
	private static final String FRAG_TAG_TRIP_OVERVIEW = "FRAG_TAG_TRIP_OVERVIEW";
	private static final String FRAG_BLURRED_BG = "FRAG_BLURRED_BG";
	private static final String FRAG_TAG_FLIGHT_MAP = "FRAG_TAG_FLIGHT_MAP";
	private static final String FRAG_TAG_FLIGHT_FILTERS = "FRAG_TAG_FLIGHT_FILTERS";
	private static final String FRAG_TAG_BG_ONE = "FRAG_TAG_BG_ONE";
	private static final String FRAG_TAG_BG_TWO = "FRAG_TAG_BG_TWO";

	//Containers..
	private ViewGroup mRootC;
	private BlockEventFrameLayout mBgHotelMapC;
	private BlockEventFrameLayout mBgDestImageC;
	private BlockEventFrameLayout mFlightMapC;
	private BlockEventFrameLayout mTripOverviewC;
	private BlockEventFrameLayout mFlightFiltersC;
	private BlockEventFrameLayout mHotelListC;
	private BlockEventFrameLayout mFlightListC;
	private FixedTranslationFrameLayout mBlurredBackgroundC;

	//Fragments
	private SupportMapFragment mMapFragment;
	private ResultsBackgroundImageFragment mBackgroundImageFrag;
	private ResultsHotelListFragment mHotelListFrag;
	private ResultsFlightListFragment mFlightListFrag;
	private ResultsFlightMapFragment mFlightMapFrag;
	private ResultsTripOverviewFragment mTripOverviewFrag;
	private ResultsFlightFiltersFragment mFlightFilterFrag;
	private ResultsBlurBackgroundImageFragment mBlurredBackgroundFrag;

	//Anim Helper vars
	private float mPrevHotelsPercentage = 1f;
	private float mPrevFlightsPercentage = 1f;

	//Other
	private ArrayList<BlockEventFrameLayout> mContainers = new ArrayList<BlockEventFrameLayout>();
	private ArrayList<ArrayList<ViewGroup>> mColumnViews = new ArrayList<ArrayList<ViewGroup>>();
	private ColumnManager mColumnManager = new ColumnManager(3);
	private State mState = State.DEFAULT;
	private String mDestinationCode = "SFO";//The destination code to use for background images...

	private enum State {
		DEFAULT,
		HOTELS,
		FLIGHTS
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_results);

		//root
		mRootC = Ui.findView(this, R.id.root_layout);

		//Background
		mBgHotelMapC = Ui.findView(this, R.id.bg_hotel_map);
		mBgDestImageC = Ui.findView(this, R.id.bg_dest_image_overlay);

		//Columns
		mFlightMapC = Ui.findView(this, R.id.column_three_flight_map);
		mTripOverviewC = Ui.findView(this, R.id.column_three_trip_pane);
		mFlightFiltersC = Ui.findView(this, R.id.column_one_flight_filters);
		mHotelListC = Ui.findView(this, R.id.column_one_hotel_list);
		mFlightListC = Ui.findView(this, R.id.column_two_flight_list);
		mBlurredBackgroundC = Ui.findView(this, R.id.column_three_blurred_bg);

		mContainers.add(mBgHotelMapC);
		mContainers.add(mBgDestImageC);
		mContainers.add(mFlightMapC);
		mContainers.add(mTripOverviewC);
		mContainers.add(mFlightFiltersC);
		mContainers.add(mHotelListC);
		mContainers.add(mFlightListC);
		mContainers.add(mBlurredBackgroundC);

		ArrayList<ViewGroup> columnOne = new ArrayList<ViewGroup>();
		ArrayList<ViewGroup> columnTwo = new ArrayList<ViewGroup>();
		ArrayList<ViewGroup> columnThree = new ArrayList<ViewGroup>();
		columnOne.add(mFlightFiltersC);
		columnOne.add(mHotelListC);
		columnTwo.add(mFlightListC);
		columnThree.add(mFlightMapC);
		columnThree.add(mTripOverviewC);
		columnThree.add(mBlurredBackgroundC);
		mColumnViews.add(columnOne);
		mColumnViews.add(columnTwo);
		mColumnViews.add(columnThree);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_STATE)) {
			String stateName = savedInstanceState.getString(STATE_CURRENT_STATE);
			mState = State.valueOf(stateName);
		}

		//We load up the default backgrounds so they are ready to go later if/when we need them
		//this is important, as we need to load images before our memory load gets too heavy
		if (savedInstanceState == null || !Db.getBackgroundImageCache(this).isDefaultInCache()) {
			Db.getBackgroundImageCache(this).loadDefaultsInThread(this);
		}

		mRootC.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				mRootC.getViewTreeObserver().removeOnPreDrawListener(this);
				mColumnManager.setTotalWidth(mRootC.getWidth());
				updateColumnWidths();

				startupSetState(mState);
				return true;
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_CURRENT_STATE, mState.name());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (bd.isDownloading(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY)) {
			BackgroundDownloader.getInstance().registerDownloadCallback(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY,
					mBackgroundImageInfoDownloadCallback);
		}
		else if (bd.isDownloading(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY)) {
			BackgroundDownloader.getInstance().registerDownloadCallback(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY,
					mBackgroundImageFileDownloadCallback);
		}
		else if (Db.getBackgroundImageInfo() == null) {
			bd.startDownload(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY, mBackgroundImageInfoDownload,
					mBackgroundImageInfoDownloadCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (!isFinishing()) {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY);
			BackgroundDownloader.getInstance().unregisterDownloadCallback(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
		}
		else {
			BackgroundDownloader.getInstance().cancelDownload(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY);
			BackgroundDownloader.getInstance().cancelDownload(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
		}
	}

	private void updateColumnWidths() {
		for (int i = 0; i < mColumnViews.size(); i++) {
			for (int j = 0; j < mColumnViews.get(i).size(); j++) {
				FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) mColumnViews.get(i).get(j)
						.getLayoutParams();
				params.leftMargin = mColumnManager.getColLeft(i);
				params.width = mColumnManager.getColWidth(i);
				mColumnViews.get(i).get(j).setLayoutParams(params);
			}
		}
	}

	private void startupSetState(State state) {
		if (state == State.FLIGHTS) {
			mPrevHotelsPercentage = 1f;
			mPrevFlightsPercentage = 0f;
		}
		else if (state == State.HOTELS) {
			mPrevHotelsPercentage = 0f;
			mPrevFlightsPercentage = 1f;
		}
		setState(state);
	}

	private void setState(State state) {
		mState = state;
		setTouchState(state);
		setVisibilityState(state);
		setFragmentState(state);
	}

	private void setFragmentState(State state) {

		//All of the fragment adds/removes come through this method, and we want to make sure our last call
		//is complete before moving forward, so this is important
		getSupportFragmentManager().executePendingTransactions();

		//We will be adding all of our add/removes to this transaction
		FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();

		boolean hotelListAvailable = true;
		boolean flightListAvailable = true;
		boolean backgroundImageAvailable = true;
		boolean flightMapAvailable = true;
		boolean flightFiltersAvailable = true;
		boolean hotelMapAvailable = true;
		boolean tripOverviewAvailable = true;
		boolean blurredBackgroundAvailable = true;

		if (state == State.FLIGHTS) {
			hotelMapAvailable = false;
		}
		else if (state == State.HOTELS) {
			flightMapAvailable = false;
			flightFiltersAvailable = false;
		}

		//Hotel list
		setHotelListFragmentAvailability(hotelListAvailable, transaction);

		//Flight list
		setFlightListFragmentAvailability(flightListAvailable, transaction);

		//Background destination image fragment
		setBackgroundImageFragmentAvailability(backgroundImageAvailable, transaction);

		//Flight map
		setFlightMapFragmentAvailability(flightMapAvailable, transaction);

		//Flight filters
		setFlightFilterFragmentAvailability(flightFiltersAvailable, transaction);

		//Hotel Map
		setHotelsMapFragmentAvailability(hotelMapAvailable, transaction);

		//Trip Overview
		setTripOverviewFragmentAvailability(tripOverviewAvailable, transaction);

		//Blurrred Background (for behind trip overview)
		setBlurredBackgroundFragmentAvailability(blurredBackgroundAvailable, transaction);

		transaction.commit();

	}

	private void blockAllNewTouchesInOtherContainers(BlockEventFrameLayout vg) {
		for (BlockEventFrameLayout container : mContainers) {
			if (container != vg) {
				container.setBlockNewEventsEnabled(true);
			}
		}
	}

	private void setTouchState(State state) {
		//We never interact with this container
		mBlurredBackgroundC.setBlockNewEventsEnabled(true);

		switch (state) {
		case DEFAULT: {
			mBgHotelMapC.setBlockNewEventsEnabled(true);
			mBgDestImageC.setBlockNewEventsEnabled(true);
			mFlightMapC.setBlockNewEventsEnabled(true);
			mTripOverviewC.setBlockNewEventsEnabled(false);
			mFlightFiltersC.setBlockNewEventsEnabled(true);
			mHotelListC.setBlockNewEventsEnabled(false);
			mFlightListC.setBlockNewEventsEnabled(false);
			break;
		}
		case FLIGHTS: {
			mBgHotelMapC.setBlockNewEventsEnabled(true);
			mBgDestImageC.setBlockNewEventsEnabled(true);
			mFlightMapC.setBlockNewEventsEnabled(false);
			mTripOverviewC.setBlockNewEventsEnabled(true);
			mFlightFiltersC.setBlockNewEventsEnabled(false);
			mHotelListC.setBlockNewEventsEnabled(true);
			mFlightListC.setBlockNewEventsEnabled(false);
			break;
		}
		case HOTELS: {
			mBgHotelMapC.setBlockNewEventsEnabled(false);
			mBgDestImageC.setBlockNewEventsEnabled(true);
			mFlightMapC.setBlockNewEventsEnabled(true);
			mTripOverviewC.setBlockNewEventsEnabled(true);
			mFlightFiltersC.setBlockNewEventsEnabled(true);
			mHotelListC.setBlockNewEventsEnabled(false);
			mFlightListC.setBlockNewEventsEnabled(true);
			break;
		}
		}
	}

	private void setAnimatingTowardsVisibility(State state) {
		switch (state) {
		case DEFAULT: {
			mBgDestImageC.setVisibility(View.VISIBLE);
			mTripOverviewC.setVisibility(View.VISIBLE);
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
			mHotelListC.setVisibility(View.VISIBLE);
			mFlightListC.setVisibility(View.VISIBLE);
			break;
		}
		case FLIGHTS: {
			mFlightMapC.setVisibility(View.VISIBLE);
			mFlightFiltersC.setVisibility(View.VISIBLE);
			mFlightListC.setVisibility(View.VISIBLE);
			break;
		}
		case HOTELS: {
			mBgHotelMapC.setVisibility(View.VISIBLE);
			mHotelListC.setVisibility(View.VISIBLE);
			break;
		}
		}
	}

	private void setVisibilityState(State state) {
		switch (state) {
		case DEFAULT: {
			mBgHotelMapC.setVisibility(View.GONE);
			mBgDestImageC.setVisibility(View.VISIBLE);
			mFlightMapC.setVisibility(View.GONE);
			mTripOverviewC.setVisibility(View.VISIBLE);
			mBlurredBackgroundC.setVisibility(View.VISIBLE);
			mFlightFiltersC.setVisibility(View.GONE);
			mHotelListC.setVisibility(View.VISIBLE);
			mFlightListC.setVisibility(View.VISIBLE);
			break;
		}
		case FLIGHTS: {
			mBgHotelMapC.setVisibility(View.GONE);
			mBgDestImageC.setVisibility(View.GONE);
			mFlightMapC.setVisibility(View.VISIBLE);
			mTripOverviewC.setVisibility(View.GONE);
			mBlurredBackgroundC.setVisibility(View.GONE);
			mFlightFiltersC.setVisibility(View.VISIBLE);
			mHotelListC.setVisibility(View.GONE);
			mFlightListC.setVisibility(View.VISIBLE);
			break;
		}
		case HOTELS: {
			mBgHotelMapC.setVisibility(View.VISIBLE);
			mBgDestImageC.setVisibility(View.GONE);
			mFlightMapC.setVisibility(View.GONE);
			mTripOverviewC.setVisibility(View.GONE);
			mBlurredBackgroundC.setVisibility(View.GONE);
			mFlightFiltersC.setVisibility(View.GONE);
			mHotelListC.setVisibility(View.VISIBLE);
			mFlightListC.setVisibility(View.GONE);
			break;
		}
		}
	}

	/**
	 * HOTEL SCROLL TRANSITION STUFF
	 */

	private void setHotelsAnimationPercentage(final float percentage) {
		slideOffPercentage(1f - percentage, 1);
		mBgDestImageC.setAlpha(percentage);
		//TODO: Shrink the hotel map so we are not rendering behind the hotel list? This is tricky because we dont want the map to jump...

		mPrevHotelsPercentage = percentage;
	}

	private void setHotelsAnimationHardwareRendering(boolean useHardware) {
		int layerValue = useHardware ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mBgDestImageC.setLayerType(layerValue, null);
		for (int colNum = 1; colNum < mColumnViews.size(); colNum++) {
			for (int viewNum = 0; viewNum < mColumnViews.get(colNum).size(); viewNum++) {
				mColumnViews.get(colNum).get(viewNum).setLayerType(layerValue, null);
			}
		}
	}

	/**
	 * FLIGHT SCROLL TRANSITION STUFF
	 */
	private void setFlightsAnimationPercentage(final float percentage) {

		mHotelListC.setAlpha(percentage);
		mFlightFiltersC.setAlpha(1f - percentage);
		mFlightMapC.setAlpha(1f - percentage);
		float tripPaneTranslation = (1f - percentage) * mTripOverviewC.getWidth();
		mTripOverviewC.setTranslationX(tripPaneTranslation);
		mBlurredBackgroundC.setTranslationX(tripPaneTranslation);
		float filterPaneTopTranslation = percentage * mFlightListFrag.getTopSpaceListView().getHeaderSpacerHeight();
		mFlightFiltersC.setTranslationY(filterPaneTopTranslation);

		mPrevFlightsPercentage = percentage;
	}

	private void setFlightsAnimationHardwareRendering(boolean useHardware) {
		int layerValue = useHardware ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;
		mFlightMapC.setLayerType(layerValue, null);
		mTripOverviewC.setLayerType(layerValue, null);
		mBlurredBackgroundC.setLayerType(layerValue, null);
		mFlightFiltersC.setLayerType(layerValue, null);
		mHotelListC.setLayerType(layerValue, null);
	}

	private void slideOffPercentage(float percentage, int firstColumn) {
		int parentWidth = mRootC.getWidth();
		for (int colNum = firstColumn; colNum < mColumnViews.size(); colNum++) {
			for (int viewNum = 0; viewNum < mColumnViews.get(colNum).size(); viewNum++) {
				ViewGroup column = mColumnViews.get(colNum).get(viewNum);
				int colDist = parentWidth - column.getLeft();
				column.setTranslationX(colDist * percentage);
			}
		}
	}

	/**
	 * HERE BE HELPER FUNCTIONS WHERE WE ATTACH AND DETACH FRAGMENTS
	 */

	private FragmentTransaction setHotelListFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mHotelListFrag == null || !mHotelListFrag.isAdded()) {
				if (mHotelListFrag == null) {
					mHotelListFrag = Ui.findSupportFragment(this, FRAG_TAG_HOTEL_LIST);
				}
				if (mHotelListFrag == null) {
					//mHotelListFrag = ColorFragment.newInstance(Color.GRAY);//new ResultsHotelListFragment();
					mHotelListFrag = new ResultsHotelListFragment();
				}
				if (!mHotelListFrag.isAdded()) {
					transaction.add(R.id.column_one_hotel_list, mHotelListFrag, FRAG_TAG_HOTEL_LIST);
				}
				mHotelListFrag.setChangeListener(new IFruitScrollUpListViewChangeListener() {

					private boolean isEnabled() {
						return mState == State.DEFAULT || mState == State.HOTELS;
					}

					@Override
					public void onStateChanged(com.expedia.bookings.widget.FruitScrollUpListView.State oldState,
							com.expedia.bookings.widget.FruitScrollUpListView.State newState, float percentage) {
						if (isEnabled()) {
							if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.TRANSIENT) {
								blockAllNewTouchesInOtherContainers(mHotelListC);
								setAnimatingTowardsVisibility(State.HOTELS);
								setAnimatingTowardsVisibility(State.DEFAULT);
								setHotelsAnimationHardwareRendering(true);
							}
							else {
								setHotelsAnimationHardwareRendering(false);
								if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.LIST_CONTENT_AT_TOP) {
									//We have entered this mode...
									setState(State.HOTELS);
								}
								else {
									setState(State.DEFAULT);
								}
							}
						}
					}

					@Override
					public void onPercentageChanged(com.expedia.bookings.widget.FruitScrollUpListView.State state,
							float percentage) {
						if (isEnabled()) {
							setHotelsAnimationPercentage(percentage);
						}
					}

				});
			}
		}
		else {
			if (mHotelListFrag == null) {
				mHotelListFrag = Ui.findSupportFragment(this, FRAG_TAG_HOTEL_LIST);
			}
			if (mHotelListFrag != null) {
				transaction.remove(mHotelListFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightListFragmentAvailability(boolean available, FragmentTransaction transaction) {
		if (available) {
			if (mFlightListFrag == null || !mFlightListFrag.isAdded()) {

				if (mFlightListFrag == null) {
					mFlightListFrag = Ui.findSupportFragment(this, FRAG_TAG_FLIGHT_LIST);
				}
				if (mFlightListFrag == null) {
					mFlightListFrag = new ResultsFlightListFragment();
				}
				if (!mFlightListFrag.isAdded()) {
					transaction.add(R.id.column_two_flight_list, mFlightListFrag, FRAG_TAG_FLIGHT_LIST);
				}

				mFlightListFrag.setChangeListener(new IFruitScrollUpListViewChangeListener() {

					private boolean isEnabled() {
						return mState == State.DEFAULT || mState == State.FLIGHTS;
					}

					@Override
					public void onStateChanged(com.expedia.bookings.widget.FruitScrollUpListView.State oldState,
							com.expedia.bookings.widget.FruitScrollUpListView.State newState, float percentage) {
						if (isEnabled()) {
							if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.TRANSIENT) {
								blockAllNewTouchesInOtherContainers(mFlightListC);
								setAnimatingTowardsVisibility(State.DEFAULT);
								setAnimatingTowardsVisibility(State.FLIGHTS);
								setFlightsAnimationHardwareRendering(true);
								setFlightsAnimationPercentage(percentage);
							}
							else {
								setFlightsAnimationHardwareRendering(false);
								if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.LIST_CONTENT_AT_TOP) {
									//We have entered this mode...
									setState(State.FLIGHTS);
								}
								else {
									setState(State.DEFAULT);
								}
							}
						}

					}

					@Override
					public void onPercentageChanged(com.expedia.bookings.widget.FruitScrollUpListView.State state,
							float percentage) {
						if (isEnabled()) {
							setFlightsAnimationPercentage(percentage);
						}

					}

				});

			}
		}
		else {
			if (mFlightListFrag == null) {
				mFlightListFrag = Ui.findSupportFragment(this, FRAG_TAG_FLIGHT_LIST);
			}
			if (mFlightListFrag != null) {
				transaction.remove(mFlightListFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setBackgroundImageFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mBackgroundImageFrag == null || !mBackgroundImageFrag.isAdded()) {
				if (mBackgroundImageFrag == null) {
					mBackgroundImageFrag = Ui.findSupportFragment(this, FRAG_TAG_BG_TWO);
				}
				if (mBackgroundImageFrag == null) {
					mBackgroundImageFrag = ResultsBackgroundImageFragment.newInstance("SFO");
				}
				if (!mBackgroundImageFrag.isAdded()) {
					transaction.add(R.id.bg_dest_image_overlay, mBackgroundImageFrag, FRAG_TAG_BG_TWO);
				}
				mBackgroundImageFrag.loadBitmapFromDb();
			}
		}
		else {
			//Remove fragments from layouts
			if (mBackgroundImageFrag == null) {
				mBackgroundImageFrag = Ui.findSupportFragment(this, FRAG_TAG_BG_TWO);
			}
			if (mBackgroundImageFrag != null) {
				transaction.remove(mBackgroundImageFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightMapFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mFlightMapFrag == null || !mFlightMapFrag.isAdded()) {
				if (mFlightMapFrag == null) {
					mFlightMapFrag = Ui.findSupportFragment(this, FRAG_TAG_FLIGHT_MAP);
				}
				if (mFlightMapFrag == null) {
					mFlightMapFrag = ResultsFlightMapFragment.newInstance();
				}
				if (!mFlightMapFrag.isAdded()) {
					transaction.add(R.id.column_three_flight_map, mFlightMapFrag, FRAG_TAG_FLIGHT_MAP);
				}
			}
		}
		else {
			if (mFlightMapFrag == null) {
				mFlightMapFrag = Ui.findSupportFragment(this, FRAG_TAG_FLIGHT_MAP);
			}
			if (mFlightMapFrag != null) {
				transaction.remove(mFlightMapFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setTripOverviewFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mTripOverviewFrag == null || !mTripOverviewFrag.isAdded()) {
				if (mTripOverviewFrag == null) {
					mTripOverviewFrag = Ui.findSupportFragment(this, FRAG_TAG_TRIP_OVERVIEW);
				}
				if (mTripOverviewFrag == null) {
					mTripOverviewFrag = ResultsTripOverviewFragment.newInstance();
				}
				if (!mTripOverviewFrag.isAdded()) {
					transaction.add(R.id.column_three_trip_pane, mTripOverviewFrag, FRAG_TAG_TRIP_OVERVIEW);
				}
			}
		}
		else {
			if (mTripOverviewFrag == null) {
				mTripOverviewFrag = Ui.findSupportFragment(this, FRAG_TAG_TRIP_OVERVIEW);
			}
			if (mTripOverviewFrag != null) {
				transaction.remove(mTripOverviewFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setBlurredBackgroundFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mBlurredBackgroundFrag == null || !mBlurredBackgroundFrag.isAdded()) {
				if (mBlurredBackgroundFrag == null) {
					mBlurredBackgroundFrag = Ui.findSupportFragment(this, FRAG_BLURRED_BG);
				}
				if (mBlurredBackgroundFrag == null) {
					mBlurredBackgroundFrag = ResultsBlurBackgroundImageFragment.newInstance();
				}
				if (!mBlurredBackgroundFrag.isAdded()) {
					transaction.add(R.id.column_three_blurred_bg, mBlurredBackgroundFrag, FRAG_BLURRED_BG);
				}
				mBlurredBackgroundFrag.loadBitmapFromDb(mColumnManager.getColWidth(2), mRootC.getHeight(),
						mColumnManager.getTotalWidth(), mRootC.getHeight());
			}
		}
		else {
			if (mBlurredBackgroundFrag == null) {
				mBlurredBackgroundFrag = Ui.findSupportFragment(this, FRAG_BLURRED_BG);
			}
			if (mBlurredBackgroundFrag != null) {
				transaction.remove(mBlurredBackgroundFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightFilterFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mFlightFilterFrag == null || !mFlightFilterFrag.isAdded()) {
				if (mFlightFilterFrag == null) {
					mFlightFilterFrag = Ui.findSupportFragment(this, FRAG_TAG_FLIGHT_FILTERS);
				}
				if (mFlightFilterFrag == null) {
					mFlightFilterFrag = ResultsFlightFiltersFragment.newInstance();
				}
				if (!mFlightFilterFrag.isAdded()) {
					transaction.add(R.id.column_one_flight_filters, mFlightFilterFrag, FRAG_TAG_FLIGHT_FILTERS);
				}
			}
		}
		else {
			if (mFlightFilterFrag == null) {
				mFlightFilterFrag = Ui.findSupportFragment(this, FRAG_TAG_FLIGHT_FILTERS);
			}
			if (mFlightFilterFrag != null) {
				transaction.remove(mFlightFilterFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelsMapFragmentAvailability(boolean available, FragmentTransaction transaction) {
		//More initialization in onMapLayout
		if (available) {
			if (mMapFragment == null || !mMapFragment.isAdded()) {

				if (mMapFragment == null) {
					mMapFragment = Ui.findSupportFragment(this, FRAG_TAG_BG_ONE);
				}
				if (mMapFragment == null) {
					mMapFragment = SupportMapFragment.newInstance();
				}
				if (!mMapFragment.isAdded()) {
					transaction.add(R.id.bg_hotel_map, mMapFragment, FRAG_TAG_BG_ONE);
				}
			}
		}
		else {
			if (mMapFragment == null) {
				mMapFragment = Ui.findSupportFragment(this, FRAG_TAG_BG_ONE);
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
	 * BACKGROUND IMAGE DOWNLOAD STUFF (If at all possible we want this to be moved to an earlier point in the app, so the images are ready when we get here)
	 */

	private static final String BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY = "BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY";
	private static final String BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY = "BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY";

	private Download<ExpediaImage> mBackgroundImageInfoDownload = new Download<ExpediaImage>() {
		@Override
		public ExpediaImage doDownload() {
			ExpediaServices services = new ExpediaServices(TabletResultsActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(BACKGROUND_IMAGE_INFO_DOWNLOAD_KEY, services);
			Point size = AndroidUtils.getScreenSize(TabletResultsActivity.this);
			return ExpediaImageManager.getInstance().getDestinationImage(mDestinationCode, size.x, size.y, true);
		}
	};

	private OnDownloadComplete<ExpediaImage> mBackgroundImageInfoDownloadCallback = new OnDownloadComplete<ExpediaImage>() {
		@Override
		public void onDownload(ExpediaImage image) {
			Log.i("Finished background image info download!");

			if (image == null) {
				Log.e("Errors downloading background image info");
			}
			else {
				// We convert this back to a BackgroundImageResponse for the sake of compatibility,
				// but at some point in the future we should really fix this up.
				BackgroundImageResponse response = new BackgroundImageResponse();
				response.setImageUrl(image.getUrl());
				response.setCacheKey(image.getCacheKey());

				Db.setBackgroundImageInfo(response);

				if (!TextUtils.isEmpty(image.getCacheKey())) {
					BackgroundImageCache cache = Db.getBackgroundImageCache(TabletResultsActivity.this);
					if (!cache.hasKeyAndBlurredKey(image.getCacheKey())) {
						BackgroundDownloader bd = BackgroundDownloader.getInstance();
						bd.cancelDownload(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY);
						bd.startDownload(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY, mBackgroundImageFileDownload,
								mBackgroundImageFileDownloadCallback);
					}
					else {
						updateBackgroundImages();
					}
				}
			}
		}
	};

	private Download<Bitmap> mBackgroundImageFileDownload = new Download<Bitmap>() {
		@Override
		public Bitmap doDownload() {
			ExpediaServices services = new ExpediaServices(TabletResultsActivity.this);
			BackgroundDownloader.getInstance().addDownloadListener(BACKGROUND_IMAGE_FILE_DOWNLOAD_KEY, services);

			try {
				URL dlUrl = new URL(Db.getBackgroundImageInfo().getImageUrl());
				Bitmap dledBmap = BitmapFactory.decodeStream((InputStream) dlUrl.getContent());
				return dledBmap;
			}
			catch (Exception ex) {
				Log.e("Exception downloading Bitmap", ex);
			}

			return null;
		}
	};

	private OnDownloadComplete<Bitmap> mBackgroundImageFileDownloadCallback = new OnDownloadComplete<Bitmap>() {
		@Override
		public void onDownload(Bitmap response) {
			Log.i("Finished background image file download!");

			// If the response is null, fake an error response (for the sake of cleaner code)
			if (response != null) {
				BackgroundImageCache cache = Db.getBackgroundImageCache(TabletResultsActivity.this);
				cache.putBitmap(Db.getBackgroundImageKey(), response, true, TabletResultsActivity.this);
				new BackgroundImageUpdateTask().execute(cache);
			}
			else {
				Log.e("Image download returned null.");
			}
		}
	};

	private void updateBackgroundImages() {
		if (mBlurredBackgroundFrag != null) {
			mBlurredBackgroundFrag.loadBitmapFromDb(mColumnManager.getColWidth(2), mRootC.getHeight(),
					mColumnManager.getTotalWidth(), mRootC.getHeight());
		}
		if (mBackgroundImageFrag != null) {
			mBackgroundImageFrag.loadBitmapFromDb();
		}
	}

	private class BackgroundImageUpdateTask extends AsyncTask<BackgroundImageCache, Object, Object> {

		@Override
		protected Object doInBackground(BackgroundImageCache... params) {
			if (params[0].isAddingBitmap()) {
				//If we got nothing after 10 seconds, then lets not even bother
				params[0].waitForAddingBitmap(10000);
			}
			return null;
		}

		protected void onPostExecute(Object result) {
			updateBackgroundImages();
		}
	}
}
