package com.expedia.bookings.server;

import android.content.Context;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightRoutes;
import com.expedia.bookings.data.RoutesResponse;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

/**
 * This class is for downloads that may occur across contexts - e.g., a download
 * that can start in one context but may end in another.
 */
public class CrossContextHelper {

	// Shared background downloader keys
	public static final String KEY_INFO_DOWNLOAD = "com.expedia.bookings.hotel.info";
	public static final String KEY_FLIGHT_ROUTES_DOWNLOAD = "com.expedia.bookings.flights.routes";

	public static Download<RoutesResponse> getFlightRoutesDownload(final Context context, final String key) {
		return new Download<RoutesResponse>() {
			@Override
			public RoutesResponse doDownload() {
				// If we have no data yet, try loading from the cache; if it's not expired, use it
				boolean success = Db.loadCachedFlightRoutes(context);
				if (success) {
					FlightRoutes routes = Db.getFlightRoutes();

					if (routes.isExpired()) {
						Db.deleteCachedFlightRoutes(context);
					}
					else {
						RoutesResponse response = new RoutesResponse(routes);
						return response;
					}
				}

				ExpediaServices services = new ExpediaServices(context);
				BackgroundDownloader.getInstance().addDownloadListener(key, services);
				return services.flightRoutes();

			}
		};
	}

	public static void updateFlightRoutesData(Context context, boolean clearOldData) {
		Log.i("Updating AirAsia flight route data...");

		final Context appContext = context.getApplicationContext();

		OnDownloadComplete<RoutesResponse> callback = new OnDownloadComplete<RoutesResponse>() {
			@Override
			public void onDownload(RoutesResponse results) {
				if (results != null && !results.hasErrors()) {
					Db.setFlightRoutes(results.getFlightRoutes());

					if (!results.wasLoadedFromDisk()) {
						Db.kickOffBackgroundFlightRouteSave(appContext);
					}
				}
			}
		};

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		// Delete old data
		bd.cancelDownload(KEY_FLIGHT_ROUTES_DOWNLOAD);
		Db.setFlightRoutes(null);

		// If this file exists, we may load from it instead
		if (clearOldData) {
			Db.deleteCachedFlightRoutes(appContext);
		}

		// Load new ones
		bd.startDownload(KEY_FLIGHT_ROUTES_DOWNLOAD, getFlightRoutesDownload(appContext, KEY_FLIGHT_ROUTES_DOWNLOAD),
				callback);
	}
}
