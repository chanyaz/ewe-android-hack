package com.expedia.bookings.server;

import android.content.Context;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
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

	public static Download<HotelOffersResponse> getHotelOffersDownload(final Context context, final String key) {
		return new Download<HotelOffersResponse>() {
			@Override
			public HotelOffersResponse doDownload() {
				ExpediaServices services = new ExpediaServices(context);
				BackgroundDownloader.getInstance().addDownloadListener(key, services);
				return services.availability(Db.getHotelSearch().getSearchParams(), Db.getHotelSearch()
						.getSelectedProperty());
			}
		};
	}

	public static Download<RoutesResponse> getFlightRoutesDownload(final Context context, final String key) {
		return new Download<RoutesResponse>() {
			@Override
			public RoutesResponse doDownload() {
				ExpediaServices services = new ExpediaServices(context);
				BackgroundDownloader.getInstance().addDownloadListener(key, services);
				return services.flightRoutes();
			}
		};
	}

	public static void updateFlightRoutesData(Context context) {
		Log.i("Updating AirAsia flight route data...");

		final Context appContext = context.getApplicationContext();

		OnDownloadComplete<RoutesResponse> callback = new OnDownloadComplete<RoutesResponse>() {
			@Override
			public void onDownload(RoutesResponse results) {
				if (results != null && !results.hasErrors()) {
					Db.setFlightRoutes(results.getFlightRoutes());
				}
			}
		};

		BackgroundDownloader bd = BackgroundDownloader.getInstance();

		bd.cancelDownload(KEY_FLIGHT_ROUTES_DOWNLOAD);
		bd.startDownload(KEY_FLIGHT_ROUTES_DOWNLOAD, getFlightRoutesDownload(appContext, KEY_FLIGHT_ROUTES_DOWNLOAD),
				callback);
	}
}
