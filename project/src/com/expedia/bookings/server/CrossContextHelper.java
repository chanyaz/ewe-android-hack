package com.expedia.bookings.server;

import android.content.Context;

import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelOffersResponse;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;

/**
 * This class is for downloads that may occur across contexts - e.g., a download
 * that can start in one context but may end in another.
 */
public class CrossContextHelper {

	// Shared background downloader keys
	public static final String KEY_INFO_DOWNLOAD = "com.expedia.bookings.hotel.info";

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

}
