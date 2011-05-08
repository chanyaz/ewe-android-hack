package com.expedia.bookings.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchActivity.MapViewListener;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.ImageCache;
import com.mobiata.android.LocationServices;
import com.mobiata.android.util.NetUtils;
import com.mobiata.hotellib.app.SearchListener;
import com.mobiata.hotellib.data.Filter;
import com.mobiata.hotellib.data.SearchParams;
import com.mobiata.hotellib.data.SearchParams.SearchType;
import com.mobiata.hotellib.data.SearchResponse;
import com.mobiata.hotellib.data.ServerError;
import com.mobiata.hotellib.data.Session;
import com.mobiata.hotellib.server.ExpediaServices;

public class HoneycombSearchActivity extends ActivityGroup implements ISearchActivity {
	private static final String KEY_SEARCH = "KEY_SEARCH";
	private static final int DIALOG_LOCATION_SUGGESTIONS = 0;
	private static final int DIALOG_LOADING = 1;

	private final Context mContext = this;

	private LocalActivityManager mLocalActivityManager;
	private Map<String, View> mActivityViews;

	private List<SearchListener> mSearchListeners;
	private MapViewListener mMapViewListener;
	private SearchParams mSearchParams;
	private Filter mFilter;
	private Session mSession;

	private List<Address> mAddresses;
	private SearchResponse mSearchResponse;

	private Thread mGeocodeThread;
	private Handler mHandler = new Handler();

	// Threads / callbacks

	private BackgroundDownloader mSearchDownloader = BackgroundDownloader.getInstance();

	private Download mSearchDownload = new Download() {
		@Override
		public Object doDownload() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					showDialog(DIALOG_LOADING);
				}
			});

			ExpediaServices services = new ExpediaServices(HoneycombSearchActivity.this, mSession);
			mSearchDownloader.addDownloadListener(KEY_SEARCH, services);
			return services.search(mSearchParams, 0);
		}
	};

	private OnDownloadComplete mSearchCallback = new OnDownloadComplete() {
		@Override
		public void onDownload(Object results) {
			// Clear the old listener so we don't end up with a memory leak
			mSearchResponse = (SearchResponse) results;

			if (mSearchResponse != null && !mSearchResponse.hasErrors()) {
				mSearchResponse.setFilter(mFilter);
				mSession = mSearchResponse.getSession();

				// ImageCache.getInstance().recycleCache(true);
				broadcastSearchCompleted(mSearchResponse);

			}
			else {
				// Handling for particular errors
				boolean handledError = false;
				if (mSearchResponse != null && mSearchResponse.hasErrors()) {
					ServerError errorOne = mSearchResponse.getErrors().get(0);
					Toast.makeText(mContext, errorOne.getPresentableMessage(mContext), Toast.LENGTH_SHORT).show();
					handledError = true;
				}

				if (!handledError) {
					String text = mContext.getString(R.string.progress_search_failed);
					Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
				}
			}

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					removeDialog(DIALOG_LOADING);
				}
			});
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_honeycomb);

		FrameLayout mainContentLayout = (FrameLayout) findViewById(R.id.main_content_layout);
		FrameLayout popupContentLayout = (FrameLayout) findViewById(R.id.popup_content_layout);

		mLocalActivityManager = getLocalActivityManager();
		setLayoutActivity(popupContentLayout, SearchListActivity.class);
		setLayoutActivity(mainContentLayout, SearchMapActivity.class);

		mFilter = new Filter();
		
		mSearchParams = new SearchParams();
		mSearchParams.setSearchType(SearchType.FREEFORM);
		mSearchParams.setFreeformLocation("Moscone, San Francisco");
		mSearchParams.setNumAdults(1);

		startSearch();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return super.onRetainNonConfigurationInstance();
	}

	// Dialogs

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_LOCATION_SUGGESTIONS: {
			final int size = mAddresses.size();
			final CharSequence[] freeformLocations = new CharSequence[mAddresses.size()];
			for (int i = 0; i < size; i++) {
				freeformLocations[i] = LocationServices.formatAddress(mAddresses.get(i));
			}

			AlertDialog.Builder builder = new Builder(this);
			builder.setTitle(R.string.ChooseLocation);
			builder.setItems(freeformLocations, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Address address = mAddresses.get(which);
					mSearchParams.setFreeformLocation(LocationServices.formatAddress(address));

					setSearchParams(address.getLatitude(), address.getLongitude());

					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					startSearchDownloader();
				}
			});
			builder.setNegativeButton(android.R.string.cancel, new Dialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					String text = mContext.getString(R.string.NoGeocodingResults);
					Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
				}
			});
			builder.setOnCancelListener(new OnCancelListener() {
				public void onCancel(DialogInterface dialog) {
					removeDialog(DIALOG_LOCATION_SUGGESTIONS);
					String text = mContext.getString(R.string.NoGeocodingResults);
					Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
				}
			});
			return builder.create();
		}
		case DIALOG_LOADING: {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setIndeterminate(true);
			dialog.setMessage("Searching for Hotels...");
			return dialog;
		}
		}

		return super.onCreateDialog(id);
	}

	private void setLayoutActivity(FrameLayout layout, Class<?> activity) {
		if (mActivityViews == null) {
			mActivityViews = new HashMap<String, View>();
		}

		Intent intent = new Intent(this, activity);
		String tag = activity.getCanonicalName();

		final Window w = mLocalActivityManager.startActivity(tag, intent);
		final View wd = w != null ? w.getDecorView() : null;
		if (layout != null && layout.getTag() != null) {
			String layoutTag = (String) layout.getTag();
			View view = mActivityViews.get(layoutTag);
			if (view != null) {
				layout.removeView(view);
			}
		}

		if (wd != null) {
			wd.setVisibility(View.VISIBLE);
			wd.setFocusableInTouchMode(true);
			((ViewGroup) wd).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

			if (wd.getParent() != null) {
				((FrameLayout) wd.getParent()).removeView(wd);
			}

			layout.addView(wd);
			mActivityViews.put(tag, wd);
		}
	}

	// ISearchActivity

	@Override
	public void addSearchListener(SearchListener searchListener) {
		if (mSearchListeners == null) {
			mSearchListeners = new ArrayList<SearchListener>();
		}

		if (!mSearchListeners.contains(searchListener)) {
			mSearchListeners.add(searchListener);
		}
	}

	@Override
	public SearchParams getSearchParams() {
		return mSearchParams;
	}

	@Override
	public Session getSession() {
		return mSession;
	}

	@Override
	public void setMapViewListener(MapViewListener mapViewListener) {
		mMapViewListener = mapViewListener;
	}

	// Private methods

	private void broadcastSearchCompleted(SearchResponse searchResponse) {
		mSearchResponse = searchResponse;
		if (mSearchListeners != null) {
			for (SearchListener searchListener : mSearchListeners) {
				searchListener.onSearchCompleted(searchResponse);
			}
		}
	}

	public void setSearchParams(Double latitde, Double longitude) {
		if (mSearchParams == null) {
			mSearchParams = new SearchParams();
		}

		mSearchParams.setSearchLatLon(latitde, longitude);
	}

	public void setSearchParamsForFreeform() {
		if (!NetUtils.isOnline(this)) {
			String text = getString(R.string.error_no_internet);
			Toast.makeText(this, text, Toast.LENGTH_LONG).show();
			return;
		}

		if (mGeocodeThread != null) {
			mGeocodeThread.interrupt();
		}
		mGeocodeThread = new Thread(new Runnable() {
			@Override
			public void run() {
				mAddresses = LocationServices.geocode(mContext, mSearchParams.getFreeformLocation());
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (mAddresses != null && mAddresses.size() > 1) {
							showDialog(DIALOG_LOCATION_SUGGESTIONS);
						}
						else if (mAddresses != null && mAddresses.size() > 0) {
							Address address = mAddresses.get(0);
							setSearchParams(address.getLatitude(), address.getLongitude());
							startSearchDownloader();
						}
						else {
							String text = mContext.getString(R.string.geolocation_failed);
							Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
						}
					}
				});
			}
		});
		mGeocodeThread.start();
	}

	public void startSearch() {
		mSearchDownloader.cancelDownload(KEY_SEARCH);

		switch (mSearchParams.getSearchType()) {
		case FREEFORM: {
			setSearchParamsForFreeform();
			break;
		}
		case PROXIMITY: {
			startSearchDownloader();
			break;
		}
		}
	}

	private void startSearchDownloader() {
		if (!NetUtils.isOnline(this)) {
			String text = mContext.getString(R.string.error_no_internet);
			Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
			return;
		}

		mSearchDownloader.cancelDownload(KEY_SEARCH);
		mSearchDownloader.startDownload(KEY_SEARCH, mSearchDownload, mSearchCallback);
	}
}