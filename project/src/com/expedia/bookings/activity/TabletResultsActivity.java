package com.expedia.bookings.activity;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BackgroundImageCache;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.enums.ResultsState;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment.IBackgroundImageReceiverRegistrar;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment.IFlightsFruitScrollUpListViewChangeListener;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment.IHotelsFruitScrollUpListViewChangeListener;
import com.expedia.bookings.fragment.TabletResultsTripControllerFragment;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IBackManageable;
import com.expedia.bookings.interfaces.IBackgroundImageReceiver;
import com.expedia.bookings.interfaces.IMeasurementListener;
import com.expedia.bookings.interfaces.IMeasurementProvider;
import com.expedia.bookings.interfaces.IStateListener;
import com.expedia.bookings.interfaces.IStateProvider;
import com.expedia.bookings.interfaces.helpers.BackManager;
import com.expedia.bookings.interfaces.helpers.StateListenerLogger;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.utils.GridManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.expedia.bookings.widget.TabletResultsActionBarView;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;
import com.mobiata.android.util.Ui;

/**
 * TabletResultsActivity: The results activity designed for tablet results 2013
 * 
 * This activity was designed keep track of global results state e.g. Are we in flights/hotels/overview mode?
 * Furthermore is houses (and sets up plumbing between) our various ITabletResultsControllers.
 * 
 * The ITabletResultsControllers control whole UI flows. So anything to do with hotels, is housed within
 * the ITabletResultsController instance fragment, which is in control over everything on screen when our
 * GlobalResultsState is set to HOTEL.
 * 
 * At the time of this writting (9/5/2013) this is also in control of background images, but hopefully this
 * will be offloaded to elsewhere in the app eventually (if for nothing other than performance/ load time reasons).
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class TabletResultsActivity extends SherlockFragmentActivity implements
		IFlightsFruitScrollUpListViewChangeListener, IHotelsFruitScrollUpListViewChangeListener,
		IBackgroundImageReceiverRegistrar, IBackButtonLockListener, IAddToTripListener, IFragmentAvailabilityProvider,
		IStateProvider<ResultsState>, IMeasurementProvider, IBackManageable {

	//State
	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";
	private static final String STATE_DEBUG_DATA_LOADED = "STATE_DEBUG_DATA_LOADING";

	//Tags
	private static final String FTAG_FLIGHTS_CONTROLLER = "FTAG_FLIGHTS_CONTROLLER";
	private static final String FTAG_HOTELS_CONTROLLER = "FTAG_HOTELS_CONTROLLER";
	private static final String FTAG_TRIP_CONTROLLER = "FTAG_TRIP_CONTROLLER";
	private static final String FTAG_BACKGROUND_IMAGE = "FTAG_BACKGROUND_IMAGE";

	//Containers..
	private ViewGroup mRootC;
	private BlockEventFrameLayout mBgDestImageC;

	//Fragments
	private ResultsBackgroundImageFragment mBackgroundImageFrag;
	private TabletResultsFlightControllerFragment mFlightsController;
	private TabletResultsHotelControllerFragment mHotelsController;
	private TabletResultsTripControllerFragment mTripController;

	//Other
	private GridManager mGrid = new GridManager();
	private ResultsState mState = ResultsState.OVERVIEW;
	private String mDestinationCode = "SFO";//The destination code to use for background images...
	private boolean mPreDrawInitComplete = false;
	private boolean mBackButtonLocked = false;
	private boolean mTestDataLoaded = false;

	//ActionBar
	private TabletResultsActionBarView mActionBarView;

	private ArrayList<IBackgroundImageReceiver> mBackgroundImageReceivers = new ArrayList<IBackgroundImageReceiver>();
	private ArrayList<IAddToTripListener> mAddToTripListeners = new ArrayList<IAddToTripListener>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_results);

		//TODO: REMOVE
		if (savedInstanceState == null || !savedInstanceState.getBoolean(STATE_DEBUG_DATA_LOADED, false)) {
			Db.saveOrLoadDbForTesting(this);
			mTestDataLoaded = true;
		}

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);
		mBgDestImageC = Ui.findView(this, R.id.bg_dest_image_overlay);
		mBgDestImageC.setBlockNewEventsEnabled(true);
		mBgDestImageC.setVisibility(View.VISIBLE);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_STATE)) {
			String stateName = savedInstanceState.getString(STATE_CURRENT_STATE);
			mState = ResultsState.valueOf(stateName);
		}

		//Add default fragments
		FragmentManager manager = getSupportFragmentManager();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		mBackgroundImageFrag = (ResultsBackgroundImageFragment) FragmentAvailabilityUtils.setFragmentAvailability(true,
				FTAG_BACKGROUND_IMAGE,
				manager, transaction, this, R.id.bg_dest_image_overlay, false);
		mTripController = (TabletResultsTripControllerFragment) FragmentAvailabilityUtils.setFragmentAvailability(true,
				FTAG_TRIP_CONTROLLER,
				manager, transaction, this, R.id.full_width_trip_controller_container, false);
		mFlightsController = (TabletResultsFlightControllerFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				true,
				FTAG_FLIGHTS_CONTROLLER, manager, transaction, this,
				R.id.full_width_flights_controller_container, false);
		mHotelsController = (TabletResultsHotelControllerFragment) FragmentAvailabilityUtils.setFragmentAvailability(
				true,
				FTAG_HOTELS_CONTROLLER, manager, transaction, this,
				R.id.full_width_hotels_controller_container, false);
		transaction.commit();
		manager.executePendingTransactions();//These must be finished before we continue..

		//We load up the default backgrounds so they are ready to go later if/when we need them
		//this is important, as we need to load images before our memory load gets too heavy
		if (savedInstanceState == null || !Db.getBackgroundImageCache(this).isDefaultInCache()) {
			Db.getBackgroundImageCache(this).loadDefaultsInThread(this);
		}

		//Ab search button
		mActionBarView = new TabletResultsActionBarView(this);
		mActionBarView.bindFromDb(this);
		mActionBarView.setSearchBarOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mState == ResultsState.OVERVIEW) {
					onBackPressed();
				}
			}
		});
		//Register the actionbar - this never gets unregistered because it is paired with
		//the activity. Further it is not a fragment, so this is an easy place to set the listeners
		registerStateListener(mActionBarView.mStateHelper, true);
		registerMeasurementListener(mActionBarView, false);

		ActionBar actionBar = getSupportActionBar();
		mActionBarView.attachToActionBar(actionBar);

		//TODO: This is just for logging so it can be removed if we want to turn off state logging.
		registerStateListener(new StateListenerLogger<ResultsState>(), true);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_CURRENT_STATE, mState.name());
		outState.putBoolean(STATE_DEBUG_DATA_LOADED, mTestDataLoaded);
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

		mRootC.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (!mPreDrawInitComplete) {
					updateContentSize(mRootC.getWidth(), mRootC.getHeight());
					finalizeState(mState);
					mPreDrawInitComplete = true;
				}

				//Wait for the defaults or the actual image, but we must have some image.
				BackgroundImageCache cache = Db.getBackgroundImageCache(TabletResultsActivity.this);
				String key = Db.getBackgroundImageKey();
				if (cache.getBitmap(key, TabletResultsActivity.this) != null
						&& cache.getBlurredBitmap(key, TabletResultsActivity.this) != null) {
					mRootC.getViewTreeObserver().removeOnPreDrawListener(this);
					updateBackgroundImages();
				}
				return true;
			}
		});
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		if (!mBackButtonLocked) {
			if (!mBackManager.doOnBackPressed()) {
				super.onBackPressed();
			}
		}
	}

	/**
	 * HERE BE BACKGROUND IMAGE STUFF
	 */
	@Override
	public void registerBgImageReceiver(IBackgroundImageReceiver receiver) {
		mBackgroundImageReceivers.add(receiver);
		receiver.bgImageInDbUpdated(mGrid.getTotalWidth());
	}

	@Override
	public void unRegisterBgImageReceiver(IBackgroundImageReceiver receiver) {
		mBackgroundImageReceivers.remove(receiver);
	}

	@Override
	public Fragment getExisitingLocalInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHTS_CONTROLLER) {
			frag = mFlightsController;
		}
		else if (tag == FTAG_HOTELS_CONTROLLER) {
			frag = mHotelsController;
		}
		else if (tag == FTAG_TRIP_CONTROLLER) {
			frag = mTripController;
		}
		else if (tag == FTAG_BACKGROUND_IMAGE) {
			frag = mBackgroundImageFrag;
		}
		return frag;
	}

	@Override
	public Fragment getNewFragmentInstanceFromTag(String tag) {
		Fragment frag = null;
		if (tag == FTAG_FLIGHTS_CONTROLLER) {
			frag = new TabletResultsFlightControllerFragment();
		}
		else if (tag == FTAG_HOTELS_CONTROLLER) {
			frag = new TabletResultsHotelControllerFragment();
		}
		else if (tag == FTAG_TRIP_CONTROLLER) {
			frag = new TabletResultsTripControllerFragment();
		}
		else if (tag == FTAG_BACKGROUND_IMAGE) {
			frag = ResultsBackgroundImageFragment.newInstance("SFO");
		}
		return frag;
	}

	@Override
	public void doFragmentSetup(String tag, Fragment frag) {
		if (tag == FTAG_TRIP_CONTROLLER) {
			mAddToTripListeners.add((IAddToTripListener) frag);
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
				updateBackgroundImages();
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
		for (IBackgroundImageReceiver receiver : mBackgroundImageReceivers) {
			receiver.bgImageInDbUpdated(mGrid.getTotalWidth());
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

	private boolean isFlightsListenerEnabled() {
		return mState == ResultsState.OVERVIEW || mState == ResultsState.FLIGHTS;
	}

	private boolean isHotelsListenerEnabled() {
		return mState == ResultsState.OVERVIEW || mState == ResultsState.HOTELS;
	}

	@Override
	public void onHotelsStateChanged(State oldState, State newState, float percentage, View requester) {
		Log.d("HotelState.onHotelsStateChanged oldState:" + oldState.name() + " newState:" + newState.name()
				+ " percentage:" + percentage);
		if (isHotelsListenerEnabled()) {
			if (newState == State.TRANSIENT) {
				if (oldState != State.TRANSIENT) {

					startStateTransition(ResultsState.OVERVIEW, ResultsState.HOTELS);
				}

			}
			else {
				endStateTransition(ResultsState.OVERVIEW, ResultsState.HOTELS);

				if (newState == State.LIST_CONTENT_AT_TOP) {
					//We have entered this mode...
					finalizeState(ResultsState.HOTELS);
				}
				else {
					finalizeState(ResultsState.OVERVIEW);
				}
			}
		}
	}

	@Override
	public void onHotelsPercentageChanged(State state, float percentage) {
		Log.d("HotelState.onHotelsPercentageChanged state:" + state.name() + " percentage:" + percentage);
		if (isHotelsListenerEnabled()) {
			updateStateTransition(ResultsState.OVERVIEW, ResultsState.HOTELS, percentage);
		}
	}

	@Override
	public void onFlightsStateChanged(State oldState, State newState, float percentage, View requester) {
		if (isFlightsListenerEnabled()) {
			if (newState == State.TRANSIENT) {

				startStateTransition(ResultsState.OVERVIEW, ResultsState.FLIGHTS);

				updateStateTransition(ResultsState.OVERVIEW, ResultsState.FLIGHTS, percentage);
			}
			else {
				endStateTransition(ResultsState.OVERVIEW, ResultsState.FLIGHTS);
				if (newState == State.LIST_CONTENT_AT_TOP) {
					finalizeState(ResultsState.FLIGHTS);
				}
				else {
					finalizeState(ResultsState.OVERVIEW);
				}
			}
		}
	}

	@Override
	public void onFlightsPercentageChanged(State state, float percentage) {
		if (isFlightsListenerEnabled()) {
			updateStateTransition(ResultsState.OVERVIEW, ResultsState.FLIGHTS, percentage);
		}
	}

	@Override
	public void setBackButtonLockState(boolean locked) {
		mBackButtonLocked = locked;
	}

	/**
	 * IAddToTripListener Stuff
	 */

	@Override
	public void beginAddToTrip(Object data, Rect globalCoordinates, int shadeColor) {
		for (IAddToTripListener listener : mAddToTripListeners) {
			listener.beginAddToTrip(data, globalCoordinates, shadeColor);
		}

	}

	@Override
	public void performTripHandoff() {
		for (IAddToTripListener listener : mAddToTripListeners) {
			listener.performTripHandoff();
		}
	}

	/*
	 * State management
	 */

	private ArrayList<IStateListener<ResultsState>> mStateChangeListeners = new ArrayList<IStateListener<ResultsState>>();

	@Override
	public void startStateTransition(ResultsState stateOne, ResultsState stateTwo) {
		for (IStateListener<ResultsState> listener : mStateChangeListeners) {
			listener.onStateTransitionStart(stateOne, stateTwo);
		}
	}

	@Override
	public void updateStateTransition(ResultsState stateOne, ResultsState stateTwo, float percentage) {
		for (IStateListener<ResultsState> listener : mStateChangeListeners) {
			listener.onStateTransitionUpdate(stateOne, stateTwo, percentage);
		}
	}

	@Override
	public void endStateTransition(ResultsState stateOne, ResultsState stateTwo) {
		for (IStateListener<ResultsState> listener : mStateChangeListeners) {
			listener.onStateTransitionEnd(stateOne, stateTwo);
		}
	}

	@Override
	public void finalizeState(ResultsState state) {
		mState = state;
		for (IStateListener<ResultsState> listener : mStateChangeListeners) {
			listener.onStateFinalized(state);
		}
	}

	@Override
	public void registerStateListener(IStateListener<ResultsState> listener, boolean fireFinalizeState) {
		mStateChangeListeners.add(listener);
		if (fireFinalizeState) {
			listener.onStateFinalized(mState);
		}
	}

	@Override
	public void unRegisterStateListener(IStateListener<ResultsState> listener) {
		mStateChangeListeners.remove(listener);
	}

	/*
	 * IMeasurementProvider
	 */

	private int mLastReportedWidth = -1;
	private int mLastReportedHeight = -1;
	private ArrayList<IMeasurementListener> mMeasurementListeners = new ArrayList<IMeasurementListener>();

	@Override
	public void updateContentSize(int totalWidth, int totalHeight) {

		if (totalWidth != mLastReportedWidth || totalHeight != mLastReportedHeight) {
			boolean isLandscape = totalWidth > totalHeight;

			mLastReportedWidth = totalWidth;
			mLastReportedHeight = totalHeight;

			//Setup grid manager
			mGrid.setGridSize(1, 3);
			mGrid.setDimensions(totalWidth, totalHeight);

			for (IMeasurementListener listener : mMeasurementListeners) {
				listener.onContentSizeUpdated(totalWidth, totalHeight, isLandscape);
			}
		}
	}

	@Override
	public void registerMeasurementListener(IMeasurementListener listener, boolean fireListener) {
		mMeasurementListeners.add(listener);
		if (fireListener && mLastReportedWidth >= 0 && mLastReportedHeight >= 0) {
			listener.onContentSizeUpdated(mLastReportedWidth, mLastReportedHeight,
					mLastReportedWidth > mLastReportedHeight);
		}
	}

	@Override
	public void unRegisterMeasurementListener(IMeasurementListener listener) {
		mMeasurementListeners.remove(listener);
	}

	/*
	 * BACK STACK MANAGEMENT
	 */

	@Override
	public BackManager getBackManager() {
		return mBackManager;
	}

	private BackManager mBackManager = new BackManager(this) {

		@Override
		public boolean handleBackPressed() {
			//Our children may do something on back pressed, but if we are left in charge we do nothing
			return false;
		}

	};
}
