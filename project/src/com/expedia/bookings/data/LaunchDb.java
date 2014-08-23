package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;

import com.expedia.bookings.R;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.SpannableBuilder;
import com.expedia.bookings.utils.StrUtils;
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
		Events.register(this);
	}

	private List<LaunchCollection> mCollections;
	private LaunchCollection mSelectedCollection;
	private LaunchLocation mSelectedPin;
	private LastSearchLaunchCollection mYourSearchCollection;
	private static final String YOUR_SEARCH_TILE_ID = "last-search";

	public static void getCollections(Context context) {
		sDb.mYourSearchCollection = generateYourSearchCollection(context, Sp.getParams());
		if (sDb.mCollections == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			// If we are already downloading our singleton is already registered
			if (!bd.isDownloading(LAUNCH_DOWNLOAD_KEY)) {
				Download download = getDownload(context.getApplicationContext());
				bd.startDownload(LAUNCH_DOWNLOAD_KEY, download, mCallback);
			}
		}
		else {
			injectLastSearch(sDb.mYourSearchCollection);
		}
	}

	public static void clear() {
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

	private static LastSearchLaunchCollection generateYourSearchCollection(Context context, SearchParams params) {
		LastSearchLaunchCollection lastSearch = null;
		if (params != null && params.hasEnoughInfoForHotelsSearch()) {
			lastSearch = new LastSearchLaunchCollection();
			lastSearch.id = YOUR_SEARCH_TILE_ID;
			if (!TextUtils.isEmpty(params.getDestination().getImageCode())) {
				lastSearch.launchImageCode = params.getDestination().getImageCode();
			}
			lastSearch.imageCode = params.getDestination().getAirportCode();

			LastSearchLaunchLocation loc = new LastSearchLaunchLocation();
			loc.imageCode = params.getDestination().getAirportCode();
			loc.location = params.getDestination();

			lastSearch.locations = new ArrayList<LaunchLocation>();
			lastSearch.locations.add(loc);

			Db.loadTripBucket(context);
			String title = "";
			if (Db.getTripBucket().isEmpty()) {
				title = StrUtils.formatCity(params.getDestination());
			}
			else {
				int tbCount = Db.getTripBucket().size();
				title = context.getResources().getQuantityString(R.plurals.num_items_TEMPLATE, tbCount, tbCount);
			}

			SpannableBuilder span = new SpannableBuilder();
			span.append(title);
			span.append("\n");

			TextAppearanceSpan subtitleSpan = new TextAppearanceSpan(context, R.style.V2_TextAppearance_Launch_YourSearchSubtitle);
			span.append(context.getString(R.string.your_search).toUpperCase(), subtitleSpan, FontCache.getSpan(FontCache.Font.ROBOTO_MEDIUM));

			lastSearch.stylizedTitle = span.build();
			lastSearch.title = lastSearch.stylizedTitle.toString();
		}
		return lastSearch;
	}

	private static void injectLastSearch(LastSearchLaunchCollection collection) {
		// If there is already a "Last Search" collection, nuke it.
		if (sDb.mCollections != null && !sDb.mCollections.isEmpty() && sDb.mCollections.get(LAST_SEARCH_COLLECTION_INDEX) instanceof LastSearchLaunchCollection) {
			sDb.mCollections.remove(LAST_SEARCH_COLLECTION_INDEX);
		}
		if (collection != null && sDb.mCollections != null) {
			sDb.mCollections.add(LAST_SEARCH_COLLECTION_INDEX, collection);
		}
	}

	private static OnDownloadComplete<List<LaunchCollection>> mCallback = new OnDownloadComplete<List<LaunchCollection>>() {
		@Override
		public void onDownload(List<LaunchCollection> collections) {
			sDb.mCollections = collections;
			if (collections != null && collections.size() > 0) {
				sDb.mSelectedCollection = collections.get(0);
			}
			injectLastSearch(sDb.mYourSearchCollection);
			Events.post(sDb.produceLaunchCollections());
		}
	};
}
