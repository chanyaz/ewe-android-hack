package com.expedia.bookings.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.expedia.bookings.data.LaunchCollection;
import com.expedia.bookings.data.LaunchDestinationCollections;
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

	private LaunchDb() {
		// Singleton
	}

	private List<LaunchCollection> mCollections;
	private LaunchCollection mSelectedCollection;

	public static void getCollections(Context context) {
		if (sDb.mCollections == null) {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			// If we are already downloading our singleton is already registered
			if (!bd.isDownloading(LAUNCH_DOWNLOAD_KEY)) {
				Download download = getDownload(context.getApplicationContext());
				bd.startDownload(LAUNCH_DOWNLOAD_KEY, download, mCallback);
			}
		}
	}

	public static void clear() {
		Events.unregister(sDb);
		sDb.mCollections = null;
	}

	@Produce
	public Events.LaunchCollectionsAvailable produceLaunchCollections() {
		return new Events.LaunchCollectionsAvailable(mCollections, mSelectedCollection);
	}

	@Subscribe
	public void onLaunchCollectionClicked(Events.LaunchCollectionClicked event) {
		mSelectedCollection = event.launchCollection;
	}

	private static Download<List<LaunchCollection>> getDownload(final Context context) {
		return new Download<List<LaunchCollection>>() {
			@Override
			public List<LaunchCollection> doDownload() {
				List<LaunchCollection> collections = new ArrayList<>();
				ExpediaServices services = new ExpediaServices(context);

				LaunchDestinationCollections manifest = services.getLaunchCollections();
				if (manifest != null && manifest.collectionIdentifiers != null) {
					for (String collectionId : manifest.collectionIdentifiers) {
						LaunchCollection collection = services.getLaunchCollection(collectionId);
						if (collection != null) {
							collections.add(collection);
						}
					}
				}

				return collections;
			}
		};
	}

	private static OnDownloadComplete<List<LaunchCollection>> mCallback = new OnDownloadComplete<List<LaunchCollection>>() {
		@Override
		public void onDownload(List<LaunchCollection> collections) {
			sDb.mCollections = collections;
			if (collections.size() > 0) {
				sDb.mSelectedCollection = collections.get(0);
			}
			Events.register(sDb);
		}
	};
}
