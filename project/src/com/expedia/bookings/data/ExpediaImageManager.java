package com.expedia.bookings.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.bitmaps.TwoLevelImageCache;

public class ExpediaImageManager {

	public enum ImageType {
		DESTINATION("DESTINATION"),
		CAR("CAR"),
		ACTIVITY("ACTIVITY");

		private String mIdentifier;

		private ImageType(String identifier) {
			mIdentifier = identifier;
		}

		public String getIdentifier() {
			return mIdentifier;
		}
	};

	private static final ExpediaImageManager sManager = new ExpediaImageManager();

	private Context mContext;

	private ExpediaImageManager() {
		// Should never be called
	}

	public static ExpediaImageManager getInstance() {
		return sManager;
	}

	public static void init(Context context) {
		// Make sure this is the Application context so we don't leak
		sManager.mContext = context.getApplicationContext();
	}

	//////////////////////////////////////////////////////////////////////////
	// Retrieval

	private static final long EXPIRATION = 1000 * 60 * 60 * 24; // 24 hours

	private Map<String, BackgroundImageResponse> mCachedImageUrls = new ConcurrentHashMap<String, BackgroundImageResponse>();

	/**
	 * Retrieves Expedia image URLs.
	 *  
	 * @param imageType the type of image
	 * @param imageCode the image code (determined by image type + other factors)
	 * @param width the desired width (not guaranteed to be exact on return)
	 * @param height the desired height (not guaranteed to be exact on return)
	 * @param useNetwork if true, will request .  If you set this to true, you better be operating on a non-UI thread!
	 * @return the URL if available, or null if not
	 */
	public BackgroundImageResponse getExpediaImage(ImageType imageType, String imageCode, int width, int height,
			boolean useNetwork) {
		// The key should be unique for each request
		String cacheUrl = imageType + ":" + imageCode + ":" + width + "x" + height;

		BackgroundImageResponse response = mCachedImageUrls.get(cacheUrl);
		if (response == null || response.getTimestamp() + EXPIRATION < System.currentTimeMillis()) {
			if (useNetwork) {
				ExpediaServices services = new ExpediaServices(mContext);
				BackgroundImageResponse newResponse = services.getExpediaImage(imageType, imageCode, width, height);

				// We'll fall back to the old URL if we don't get a new one
				if (newResponse != null && !newResponse.hasErrors()) {
					// See if we should expire the old cached image out of our image cache
					if (response != null && !response.getCacheKey().equals(newResponse.getCacheKey())) {
						TwoLevelImageCache.removeImage(response.getImageUrl());
					}

					response = newResponse;
					mCachedImageUrls.put(cacheUrl, newResponse);
				}
			}
		}

		// We might end up falling back to an old response, which is fine;
		// we're not going for 100% accurate data here.
		if (response != null) {
			return response;
		}

		return null;
	}

	public BackgroundImageResponse getCarImage(Car.Category category, Car.Type type, int width, int height,
			boolean useNetwork) {
		return getExpediaImage(ImageType.CAR, getImageCode(category, type), width, height, useNetwork);
	}

	public BackgroundImageResponse getDestinationImage(String destinationCode, int width, int height, boolean useNetwork) {
		return getExpediaImage(ImageType.DESTINATION, destinationCode, width, height, useNetwork);
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility

	public static String getImageCode(Car.Category category, Car.Type type) {
		return category.toString().replace("_", "") + "_" + type.toString().replace("_", "");
	}
}
