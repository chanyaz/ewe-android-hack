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
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment;
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment.IBackgroundImageReceiverRegistrar;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment.IFlightsFruitScrollUpListViewChangeListener;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment.IHotelsFruitScrollUpListViewChangeListener;
import com.expedia.bookings.fragment.TabletResultsTripControllerFragment;
import com.expedia.bookings.graphics.PercentageFadeColorDrawable;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.interfaces.IBackButtonLockListener;
import com.expedia.bookings.interfaces.IBackgroundImageReceiver;
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.maps.HotelMapFragment;
import com.expedia.bookings.maps.HotelMapFragment.HotelMapFragmentListener;
import com.expedia.bookings.maps.SupportMapFragment.SupportMapFragmentListener;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.utils.FragmentAvailabilityUtils;
import com.expedia.bookings.utils.FragmentAvailabilityUtils.IFragmentAvailabilityProvider;
import com.expedia.bookings.widget.AbSearchInfoButton;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
import com.google.android.gms.maps.CameraUpdateFactory;
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
public class TabletResultsActivity extends SherlockFragmentActivity implements ITabletResultsController,
		IFlightsFruitScrollUpListViewChangeListener, IHotelsFruitScrollUpListViewChangeListener,
		IBackgroundImageReceiverRegistrar, IBackButtonLockListener, IAddToTripListener, IFragmentAvailabilityProvider {

	//State
	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";

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
	private ColumnManager mColumnManager = new ColumnManager(3);
	private GlobalResultsState mState = GlobalResultsState.DEFAULT;
	private String mDestinationCode = "SFO";//The destination code to use for background images...
	private boolean mPreDrawInitComplete = false;
	private boolean mBackButtonLocked = false;

	//ActionBar
	private AbSearchInfoButton mABSearchBtn;
	private PercentageFadeColorDrawable mActionBarBg;
	private PercentageFadeColorDrawable mActionBarBgFlights;
	private PercentageFadeColorDrawable mActionBarBgHotels;

	private ArrayList<IBackgroundImageReceiver> mBackgroundImageReceivers = new ArrayList<IBackgroundImageReceiver>();
	private ArrayList<ITabletResultsController> mTabletResultsControllers = new ArrayList<ITabletResultsController>();
	private ArrayList<IAddToTripListener> mAddToTripListeners = new ArrayList<IAddToTripListener>();

	public enum GlobalResultsState {
		DEFAULT,
		HOTELS,
		FLIGHTS,
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_results);

		//TODO: REMOVE
		Db.saveOrLoadDbForTesting(this);

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);
		mBgDestImageC = Ui.findView(this, R.id.bg_dest_image_overlay);
		mBgDestImageC.setBlockNewEventsEnabled(true);
		mBgDestImageC.setVisibility(View.VISIBLE);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_STATE)) {
			String stateName = savedInstanceState.getString(STATE_CURRENT_STATE);
			mState = GlobalResultsState.valueOf(stateName);
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

		mTabletResultsControllers.add(mTripController);
		mTabletResultsControllers.add(mFlightsController);
		mTabletResultsControllers.add(mHotelsController);

		//We load up the default backgrounds so they are ready to go later if/when we need them
		//this is important, as we need to load images before our memory load gets too heavy
		if (savedInstanceState == null || !Db.getBackgroundImageCache(this).isDefaultInCache()) {
			Db.getBackgroundImageCache(this).loadDefaultsInThread(this);
		}

		//We set up our actionbar background colors.
		mActionBarBgHotels = new PercentageFadeColorDrawable(
				getResources().getColor(R.color.tablet_results_ab_default),
				getResources().getColor(R.color.tablet_results_ab_hotels));
		mActionBarBgFlights = new PercentageFadeColorDrawable(getResources()
				.getColor(R.color.tablet_results_ab_default),
				getResources().getColor(R.color.tablet_results_ab_flights));
		mActionBarBg = mActionBarBgHotels;

		//Ab search button
		mABSearchBtn = (AbSearchInfoButton) getLayoutInflater().inflate(R.layout.actionbar_search_button, null);
		mABSearchBtn.bindFromDb(this);
		mABSearchBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mState == GlobalResultsState.DEFAULT && mABSearchBtn.getAlpha() == 1f) {
					onBackPressed();
				}
			}
		});

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(mActionBarBg);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(mABSearchBtn);

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

		mRootC.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				if (!mPreDrawInitComplete) {
					updateContentSize(mRootC.getWidth(), mRootC.getHeight());
					setGlobalResultsState(mState);
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

	/**
	 * ACTIONBAR COLOR STUFF
	 */

	private void setActionbarColorFromState(GlobalResultsState state) {

		if (state == GlobalResultsState.DEFAULT) {
			mActionBarBg.setPercentage(0f);
			mABSearchBtn.setAlpha(1f);
		}
		else {
			ActionBar actionBar = getSupportActionBar();
			if (state == GlobalResultsState.HOTELS && mActionBarBg != mActionBarBgHotels) {
				mActionBarBg = mActionBarBgHotels;
				actionBar.setBackgroundDrawable(mActionBarBg);
			}
			else if (state == GlobalResultsState.FLIGHTS && mActionBarBg != mActionBarBgFlights) {
				mActionBarBg = mActionBarBgFlights;
				actionBar.setBackgroundDrawable(mActionBarBg);
			}
			mActionBarBg.setPercentage(1f);
			mABSearchBtn.setAlpha(0f);
		}
	}

	private void setActionbarColorForTransition(GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		ActionBar actionBar = getSupportActionBar();
		if (stateOne == GlobalResultsState.DEFAULT && stateTwo == GlobalResultsState.DEFAULT) {
			mActionBarBg.setPercentage(0f);
		}
		else if (stateOne == GlobalResultsState.HOTELS || stateTwo == GlobalResultsState.HOTELS) {
			if (mActionBarBg != mActionBarBgHotels) {
				mActionBarBg = mActionBarBgHotels;
				actionBar.setBackgroundDrawable(mActionBarBg);
			}
			float percentage = stateOne == GlobalResultsState.HOTELS ? 1f : 0f;
			mActionBarBg.setPercentage(percentage);
		}
		else {
			if (mActionBarBg != mActionBarBgFlights) {
				mActionBarBg = mActionBarBgFlights;
				actionBar.setBackgroundDrawable(mActionBarBg);
			}
			float percentage = stateOne == GlobalResultsState.FLIGHTS ? 1f : 0f;
			mActionBarBg.setPercentage(percentage);
		}
	}

	/**
	 * ITabletResultsController STUFF
	 */

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		Log.d("setGlobalResultsState:" + state.name());
		mState = state;
		setActionbarColorFromState(state);

		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.setGlobalResultsState(state);
		}
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		setActionbarColorForTransition(mState, state);
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.setAnimatingTowardsVisibility(state);
		}
	}

	@Override
	public void setHardwareLayerForTransition(int layerType, GlobalResultsState stateOne, GlobalResultsState stateTwo) {
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.setHardwareLayerForTransition(layerType, stateOne, stateTwo);
		}
	}

	@Override
	public void blockAllNewTouches(View requester) {
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.blockAllNewTouches(requester);
		}
	}

	@Override
	public void animateToFlightsPercentage(float percentage) {
		mActionBarBg.setPercentage(1f - percentage);
		mABSearchBtn.setAlpha(percentage);
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.animateToFlightsPercentage(percentage);
		}

	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		mActionBarBg.setPercentage(1f - percentage);
		mABSearchBtn.setAlpha(percentage);
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.animateToHotelsPercentage(percentage);
		}
	}

	@Override
	public void updateContentSize(int totalWidth, int totalHeight) {

		//Setup column manager
		mColumnManager.setTotalWidth(totalWidth);

		//Actionbar search button width
		mABSearchBtn.setWidth(mColumnManager.getColRight(1) - mABSearchBtn.getLeft());
		//Tell the children
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.updateContentSize(totalWidth, totalHeight);
		}
	}

	@Override
	public boolean handleBackPressed() {
		for (ITabletResultsController controller : mTabletResultsControllers) {
			if (controller.handleBackPressed()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void onBackPressed() {
		if (!mBackButtonLocked) {
			if (!handleBackPressed()) {
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
		receiver.bgImageInDbUpdated(mColumnManager.getTotalWidth());
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
			receiver.bgImageInDbUpdated(mColumnManager.getTotalWidth());
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
		return mState == GlobalResultsState.DEFAULT || mState == GlobalResultsState.FLIGHTS;
	}

	private boolean isHotelsListenerEnabled() {
		return mState == GlobalResultsState.DEFAULT || mState == GlobalResultsState.HOTELS;
	}

	@Override
	public void onHotelsStateChanged(State oldState, State newState, float percentage, View requester) {
		Log.d("HotelState.onHotelsStateChanged oldState:" + oldState.name() + " newState:" + newState.name()
				+ " percentage:" + percentage);
		if (isHotelsListenerEnabled()) {
			if (newState == State.TRANSIENT) {
				if (oldState != State.TRANSIENT) {
					blockAllNewTouches(requester);

					//order matters here, because the second will in certain cases squash the first
					setAnimatingTowardsVisibility(GlobalResultsState.DEFAULT);
					setAnimatingTowardsVisibility(GlobalResultsState.HOTELS);

					setHardwareLayerForTransition(View.LAYER_TYPE_HARDWARE, GlobalResultsState.DEFAULT,
							GlobalResultsState.HOTELS);
				}

			}
			else {
				setHardwareLayerForTransition(View.LAYER_TYPE_NONE, GlobalResultsState.DEFAULT,
						GlobalResultsState.HOTELS);

				if (newState == State.LIST_CONTENT_AT_TOP) {
					//We have entered this mode...
					setGlobalResultsState(GlobalResultsState.HOTELS);
				}
				else {
					setGlobalResultsState(GlobalResultsState.DEFAULT);
				}
			}
		}
	}

	@Override
	public void onHotelsPercentageChanged(State state, float percentage) {
		Log.d("HotelState.onHotelsPercentageChanged state:" + state.name() + " percentage:" + percentage);
		if (isHotelsListenerEnabled()) {
			animateToHotelsPercentage(percentage);
		}
	}

	@Override
	public void onFlightsStateChanged(State oldState, State newState, float percentage, View requester) {
		if (isFlightsListenerEnabled()) {
			if (newState == State.TRANSIENT) {
				blockAllNewTouches(requester);
				setAnimatingTowardsVisibility(GlobalResultsState.DEFAULT);
				setAnimatingTowardsVisibility(GlobalResultsState.FLIGHTS);
				setHardwareLayerForTransition(View.LAYER_TYPE_HARDWARE, GlobalResultsState.DEFAULT,
						GlobalResultsState.FLIGHTS);
				animateToFlightsPercentage(percentage);
			}
			else {
				setHardwareLayerForTransition(View.LAYER_TYPE_NONE, GlobalResultsState.DEFAULT,
						GlobalResultsState.FLIGHTS);
				if (newState == State.LIST_CONTENT_AT_TOP) {
					setGlobalResultsState(GlobalResultsState.FLIGHTS);
				}
				else {
					setGlobalResultsState(GlobalResultsState.DEFAULT);
				}
			}
		}
	}

	@Override
	public void onFlightsPercentageChanged(State state, float percentage) {
		if (isFlightsListenerEnabled()) {
			animateToFlightsPercentage(percentage);
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
}
