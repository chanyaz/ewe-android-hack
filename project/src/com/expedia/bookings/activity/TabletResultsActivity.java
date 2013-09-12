package com.expedia.bookings.activity;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.expedia.bookings.R;
import com.expedia.bookings.data.BackgroundImageCache;
import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
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
import com.expedia.bookings.interfaces.ITabletResultsController;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.ColumnManager;
import com.expedia.bookings.widget.BlockEventFrameLayout;
import com.expedia.bookings.widget.FruitScrollUpListView.State;
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
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.View;
import android.view.ViewGroup;

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
		IBackgroundImageReceiverRegistrar, IBackButtonLockListener, IAddToTripListener {

	public interface IBackgroundImageReceiver {
		/**
		 * Tell the listeners we have valid bg images in Db. We tell the listeners the total width/height
		 * incase they are overlays and need to do clipping
		 * 
		 * @param totalRootViewWidth
		 * @param totalRootViewHeight
		 */
		public void bgImageInDbUpdated(int totalRootViewWidth);
	}

	//State
	private static final String STATE_CURRENT_STATE = "STATE_CURRENT_STATE";

	//Tags
	private static final String FRAG_TAG_FLIGHTS_CONTROLLER = "FRAG_TAG_FLIGHTS_CONTROLLER";
	private static final String FRAG_TAG_HOTELS_CONTROLLER = "FRAG_TAG_HOTELS_CONTROLLER";
	private static final String FRAG_TAG_TRIP_CONTROLLER = "FRAG_TAG_TRIP_CONTROLLER";
	private static final String FRAG_TAG_BACKGROUND_IMAGE = "FRAG_TAG_BACKGROUND_IMAGE";

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

	private PercentageFadeColorDrawable mActionBarBg;
	private PercentageFadeColorDrawable mActionBarBgFlights;
	private PercentageFadeColorDrawable mActionBarBgHotels;

	private ArrayList<IBackgroundImageReceiver> mBackgroundImageReceivers = new ArrayList<IBackgroundImageReceiver>();
	private ArrayList<ITabletResultsController> mTabletResultsControllers = new ArrayList<ITabletResultsController>();
	private ArrayList<IAddToTripListener> mAddToTripListeners = new ArrayList<IAddToTripListener>();

	public enum GlobalResultsState
	{
		DEFAULT,
		HOTELS,
		FLIGHTS,
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tablet_results);

		//Containers
		mRootC = Ui.findView(this, R.id.root_layout);
		mBgDestImageC = Ui.findView(this, R.id.bg_dest_image_overlay);
		mBgDestImageC.setBlockNewEventsEnabled(true);
		mBgDestImageC.setVisibility(View.VISIBLE);

		if (savedInstanceState != null && savedInstanceState.containsKey(STATE_CURRENT_STATE)) {
			String stateName = savedInstanceState.getString(STATE_CURRENT_STATE);
			mState = GlobalResultsState.valueOf(stateName);
		}

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

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setBackgroundDrawable(mActionBarBg);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(STATE_CURRENT_STATE, mState.name());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();

		//Add default fragments
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		setBackgroundImageFragmentAvailability(true, transaction);
		setFlightsControllerAvailability(true, transaction);
		setHotelsControllerAvailability(true, transaction);
		setTripControllerAvailability(true, transaction);
		transaction.commit();
		getSupportFragmentManager().executePendingTransactions();//These must be finished before we continue..

		mTabletResultsControllers.add(mFlightsController);
		mTabletResultsControllers.add(mHotelsController);
		mTabletResultsControllers.add(mTripController);

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
					updateColumnWidths(mRootC.getWidth());
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
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.animateToFlightsPercentage(percentage);
		}

	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
		mActionBarBg.setPercentage(1f - percentage);
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.animateToHotelsPercentage(percentage);
		}
	}

	@Override
	public void updateColumnWidths(int totalWidth) {
		mColumnManager.setTotalWidth(totalWidth);
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.updateColumnWidths(totalWidth);
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

	/**
	 * HERE BE HELPER FUNCTIONS WHERE WE ATTACH AND DETACH FRAGMENTS
	 */

	private FragmentTransaction setBackgroundImageFragmentAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mBackgroundImageFrag == null || !mBackgroundImageFrag.isAdded()) {
				if (mBackgroundImageFrag == null) {
					mBackgroundImageFrag = Ui.findSupportFragment(this, FRAG_TAG_BACKGROUND_IMAGE);
				}
				if (mBackgroundImageFrag == null) {
					mBackgroundImageFrag = ResultsBackgroundImageFragment.newInstance("SFO");
				}
				if (!mBackgroundImageFrag.isAdded()) {
					transaction.add(R.id.bg_dest_image_overlay, mBackgroundImageFrag, FRAG_TAG_BACKGROUND_IMAGE);
				}
			}
		}
		else {
			//Remove fragments from layouts
			if (mBackgroundImageFrag == null) {
				mBackgroundImageFrag = Ui.findSupportFragment(this, FRAG_TAG_BACKGROUND_IMAGE);
			}
			if (mBackgroundImageFrag != null) {
				transaction.remove(mBackgroundImageFrag);
			}
		}
		return transaction;
	}

	private FragmentTransaction setFlightsControllerAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mFlightsController == null || !mFlightsController.isAdded()) {
				if (mFlightsController == null) {
					mFlightsController = Ui.findSupportFragment(this, FRAG_TAG_FLIGHTS_CONTROLLER);
				}
				if (mFlightsController == null) {
					mFlightsController = new TabletResultsFlightControllerFragment();
				}
				if (!mFlightsController.isAdded()) {
					transaction.add(R.id.full_width_flights_controller_container, mFlightsController,
							FRAG_TAG_FLIGHTS_CONTROLLER);
				}
			}
		}
		else {
			if (mFlightsController == null) {
				mFlightsController = Ui.findSupportFragment(this, FRAG_TAG_FLIGHTS_CONTROLLER);
			}
			if (mFlightsController != null) {
				transaction.remove(mFlightsController);
			}
		}
		return transaction;
	}

	private FragmentTransaction setHotelsControllerAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mHotelsController == null || !mHotelsController.isAdded()) {
				if (mHotelsController == null) {
					mHotelsController = Ui.findSupportFragment(this, FRAG_TAG_HOTELS_CONTROLLER);
				}
				if (mHotelsController == null) {
					mHotelsController = new TabletResultsHotelControllerFragment();
				}
				if (!mHotelsController.isAdded()) {
					transaction.add(R.id.full_width_hotels_controller_container, mHotelsController,
							FRAG_TAG_HOTELS_CONTROLLER);
				}
			}
		}
		else {
			if (mHotelsController == null) {
				mHotelsController = Ui.findSupportFragment(this, FRAG_TAG_HOTELS_CONTROLLER);
			}
			if (mHotelsController != null) {
				transaction.remove(mHotelsController);
			}
		}
		return transaction;
	}

	private FragmentTransaction setTripControllerAvailability(boolean available,
			FragmentTransaction transaction) {
		if (available) {
			if (mTripController == null || !mTripController.isAdded()) {
				if (mTripController == null) {
					mTripController = Ui.findSupportFragment(this, FRAG_TAG_TRIP_CONTROLLER);
				}
				if (mTripController == null) {
					mTripController = new TabletResultsTripControllerFragment();
				}
				if (!mTripController.isAdded()) {
					transaction.add(R.id.full_width_trip_controller_container, mTripController,
							FRAG_TAG_TRIP_CONTROLLER);
				}
				mAddToTripListeners.add(mTripController);
			}
		}
		else {
			if (mTripController == null) {
				mTripController = Ui.findSupportFragment(this, FRAG_TAG_TRIP_CONTROLLER);
			}
			if (mTripController != null) {
				mAddToTripListeners.remove(mTripController);
				transaction.remove(mTripController);
			}
		}
		return transaction;
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
			if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.TRANSIENT) {
				blockAllNewTouches(requester);

				//order matters here, because the second will in certain cases squash the first
				setAnimatingTowardsVisibility(GlobalResultsState.DEFAULT);
				setAnimatingTowardsVisibility(GlobalResultsState.HOTELS);

				setHardwareLayerForTransition(View.LAYER_TYPE_HARDWARE, GlobalResultsState.DEFAULT,
						GlobalResultsState.HOTELS);

			}
			else {
				setHardwareLayerForTransition(View.LAYER_TYPE_NONE, GlobalResultsState.DEFAULT,
						GlobalResultsState.HOTELS);

				if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.LIST_CONTENT_AT_TOP) {
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
			if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.TRANSIENT) {
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
				if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.LIST_CONTENT_AT_TOP) {
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
	public void guiElementInPosition() {
		for (IAddToTripListener listener : mAddToTripListeners) {
			listener.guiElementInPosition();
		}

	}
}
