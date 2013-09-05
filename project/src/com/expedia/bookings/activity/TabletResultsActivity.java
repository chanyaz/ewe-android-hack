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
import com.expedia.bookings.fragment.ResultsBackgroundImageFragment.IBackgroundImageReceiverRegistrar;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment;
import com.expedia.bookings.fragment.TabletResultsFlightControllerFragment.IFlightsFruitScrollUpListViewChangeListener;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment;
import com.expedia.bookings.fragment.TabletResultsHotelControllerFragment.IHotelsFruitScrollUpListViewChangeListener;
import com.expedia.bookings.fragment.TabletResultsTripControllerFragment;
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
		IBackgroundImageReceiverRegistrar {

	public interface IBackgroundImageReceiver {
		/**
		 * Tell the listeners we have valid bg images in Db. We tell the listeners the total width/height
		 * incase they are overlays and need to do clipping
		 * 
		 * @param totalRootViewWidth
		 * @param totalRootViewHeight
		 */
		public void bgImageInDbUpdated(int totalRootViewWidth, int totalRootViewHeight);
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

	private ArrayList<IBackgroundImageReceiver> mBackgroundImageReceivers = new ArrayList<IBackgroundImageReceiver>();
	private ArrayList<ITabletResultsController> mTabletResultsControllers = new ArrayList<ITabletResultsController>();

	public enum GlobalResultsState
	{
		DEFAULT,
		HOTELS,
		FLIGHTS
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

		//Add default fragments
		FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
		setBackgroundImageFragmentAvailability(true, transaction);
		setFlightsControllerAvailability(true, transaction);
		setHotelsControllerAvailability(true, transaction);
		setTripControllerAvailability(true, transaction);
		transaction.commit();

		mTabletResultsControllers.add(mFlightsController);
		mTabletResultsControllers.add(mHotelsController);
		mTabletResultsControllers.add(mTripController);

		mRootC.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				mRootC.getViewTreeObserver().removeOnPreDrawListener(this);
				updateColumnWidths(mRootC.getWidth());
				setGlobalResultsState(mState);
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

	/**
	 * ITabletResultsController STUFF
	 */

	@Override
	public void setGlobalResultsState(GlobalResultsState state) {
		mState = state;
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.setGlobalResultsState(state);
		}
	}

	@Override
	public void setAnimatingTowardsVisibility(GlobalResultsState state) {
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.setAnimatingTowardsVisibility(state);
		}
	}

	@Override
	public void setHardwareLayerFlightsTransition(boolean useHardwareLayer) {
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.setHardwareLayerFlightsTransition(useHardwareLayer);
		}

	}

	@Override
	public void setHardwareLayerHotelsTransition(boolean useHardwareLayer) {
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.setHardwareLayerHotelsTransition(useHardwareLayer);
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
		for (ITabletResultsController controller : mTabletResultsControllers) {
			controller.animateToFlightsPercentage(percentage);
		}

	}

	@Override
	public void animateToHotelsPercentage(float percentage) {
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

	/**
	 * HERE BE BACKGROUND IMAGE STUFF
	 */
	@Override
	public void registerBgImageReceiver(IBackgroundImageReceiver receiver) {
		mBackgroundImageReceivers.add(receiver);
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
			}
		}
		else {
			if (mTripController == null) {
				mTripController = Ui.findSupportFragment(this, FRAG_TAG_TRIP_CONTROLLER);
			}
			if (mTripController != null) {
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
		int height = mRootC.getHeight();
		if (height <= 0) {
			height = 5000;//arbitrarily large
		}
		for (IBackgroundImageReceiver receiver : mBackgroundImageReceivers) {
			receiver.bgImageInDbUpdated(mColumnManager.getTotalWidth(), height);
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
		if (isHotelsListenerEnabled()) {
			if (newState == com.expedia.bookings.widget.FruitScrollUpListView.State.TRANSIENT) {
				blockAllNewTouches(requester);
				setAnimatingTowardsVisibility(GlobalResultsState.HOTELS);
				setAnimatingTowardsVisibility(GlobalResultsState.DEFAULT);
				setHardwareLayerHotelsTransition(true);
			}
			else {
				setHardwareLayerHotelsTransition(false);
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
				setHardwareLayerFlightsTransition(true);
				animateToFlightsPercentage(percentage);
			}
			else {
				setHardwareLayerFlightsTransition(false);
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

}
