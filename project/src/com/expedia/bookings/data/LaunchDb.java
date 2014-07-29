package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

public class LaunchDb {

	private static final LaunchDb sDb = new LaunchDb();

	private static final String LAUNCH_DOWNLOAD_KEY = "LAUNCH_DOWNLOAD_KEY";

	private static final int LAST_SEARCH_COLLECTION_INDEX = 0;

	private LaunchDb() {
		// Singleton
	}

	private List<LaunchCollection> mCollections;
	private LaunchCollection mSelectedCollection;
	private LaunchLocation mSelectedPin;
	private static String sYourSearchTitle;
	private static final String YOUR_SEARCH_TILE_ID = "last-search";

	public static void getCollections(Context context) {
		sYourSearchTitle = context.getString(R.string.your_search);
		if (sDb.mCollections == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			// If we are already downloading our singleton is already registered
			if (!bd.isDownloading(LAUNCH_DOWNLOAD_KEY)) {
				Download download = getDownload(context.getApplicationContext());
				bd.startDownload(LAUNCH_DOWNLOAD_KEY, download, mCallback);
			}
		}
		else {
			Db.loadTripBucket(context);
			injectLastSearch(Sp.getParams());
			sDb.mSelectedCollection = sDb.mCollections.get(LAST_SEARCH_COLLECTION_INDEX + 1);
		}
	}

	public static void clear() {
		Events.unregister(sDb);
		sDb.mCollections = null;
	}

	@Produce
	public Events.LaunchCollectionsAvailable produceLaunchCollections() {
		return new Events.LaunchCollectionsAvailable(mCollections, mSelectedCollection, mSelectedPin);
	}

	@Subscribe
	public void onLaunchCollectionClicked(Events.LaunchCollectionClicked event) {
		mSelectedCollection = event.launchCollection;
	}

	@Subscribe
	public void onMapPinClicked(Events.LaunchMapPinClicked event) {
		mSelectedPin = event.launchLocation;
	}

	private static Download<List<LaunchCollection>> getDownload(final Context context) {
		return new Download<List<LaunchCollection>>() {
			@Override
			public List<LaunchCollection> doDownload() {
				List<LaunchCollection> collections = null;
				ExpediaServices services = new ExpediaServices(context);
				LaunchDestinationCollections collectionsResponse;

				Locale current = context.getResources().getConfiguration().locale;
				collectionsResponse = services.getLaunchCollections(current.toString());

				if (collectionsResponse == null) {
					// fallback to default
					collectionsResponse = services.getLaunchCollections("default");
				}

				if (collectionsResponse != null) {
					collections = collectionsResponse.collections;
				}

				return collections;
			}
		};
	}

	public static void injectLastSearch(SearchParams params) {
		// If there is already a "Last Search" collection,
		// nuke it.
		if (!sDb.mCollections.isEmpty() && sDb.mCollections.get(LAST_SEARCH_COLLECTION_INDEX) instanceof LastSearchLaunchCollection) {
			sDb.mCollections.remove(LAST_SEARCH_COLLECTION_INDEX);
		}

		if (params != null && params.hasEnoughInfoForHotelsSearch()) {
			LastSearchLaunchCollection lastSearch = new LastSearchLaunchCollection();
			lastSearch.title = sYourSearchTitle;
			lastSearch.id = YOUR_SEARCH_TILE_ID;
			lastSearch.imageCode = Sp.getParams().getDestination().getAirportCode();

			String locSubtitle = null;
			if (Db.getTripBucket().isEmpty()) {
				locSubtitle = "";
			}
			else {
				if (Db.getTripBucket().size() == 1) {
					locSubtitle = "1 item";
				}
				else if (Db.getTripBucket().size() == 2) {
					locSubtitle = "2 items";
				}
			}

			LastSearchLaunchLocation loc = new LastSearchLaunchLocation();

			loc.imageCode = Sp.getParams().getDestination().getAirportCode();

			loc.location = Sp.getParams().getDestination();
			lastSearch.locations = new ArrayList<LaunchLocation>();
			lastSearch.locations.add(loc);
			lastSearch.title += '\n' + locSubtitle;
			sDb.mCollections.add(LAST_SEARCH_COLLECTION_INDEX, lastSearch);
		}
	}

	private static OnDownloadComplete<List<LaunchCollection>> mCallback = new OnDownloadComplete<List<LaunchCollection>>() {
		@Override
		public void onDownload(List<LaunchCollection> collections) {
			sDb.mCollections = collections;
			if (collections != null && collections.size() > 0) {
				sDb.mSelectedCollection = collections.get(0);
			}
			injectLastSearch(Sp.getParams());
			Events.register(sDb);
		}
	};
}
