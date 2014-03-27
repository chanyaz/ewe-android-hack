package com.expedia.bookings.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.text.format.DateUtils;

import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.bitmaps.TwoLevelImageCache;

/**
 * Manages images retrieved from Expedia's database.
 *
 * Implementation note: currently, the way that Expedia images are sized is
 * based on the size of the device, not the desired display size of the image.
 * Once they fix that, we should stop defaulting to using the full-size
 * images all the time.
 */
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

	private static final long EXPIRATION = DateUtils.DAY_IN_MILLIS;

	// Fast, in-memory cache (so we don't always have to go to disk)
	private Map<String, ExpediaImage> mCachedImageUrls = new ConcurrentHashMap<String, ExpediaImage>();

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
	public ExpediaImage getExpediaImage(ImageType imageType, String imageCode, int width, int height,
			boolean useNetwork) {
		// The key should be unique for each request
		String cacheUrl = getImageKey(imageType, imageCode, width, height);

		ExpediaImage image = mCachedImageUrls.get(cacheUrl);
		if (image == null || image.getTimestamp() + EXPIRATION < System.currentTimeMillis()) {
			if (useNetwork) {
				// Try to retrieve the latest from disk
				image = ExpediaImage.getImage(imageType, imageCode, width, height);

				// If disk is nonexistant or  old, then resort to network
				if (image == null || image.getTimestamp() + EXPIRATION < System.currentTimeMillis()) {
					ExpediaServices services = new ExpediaServices(mContext);
					ExpediaImageResponse newResponse = services.getExpediaImage(imageType, imageCode, width, height);

					// We'll fall back to the old URL if we don't get a new one
					if (newResponse != null && !newResponse.hasErrors()) {
						// See if we should expire the old cached image out of our image cache
						if (image != null && !image.getCacheKey().equals(newResponse.getCacheKey())) {
							TwoLevelImageCache.removeImage(image.getUrl());
						}

						if (image == null) {
							image = new ExpediaImage(imageType, imageCode, width, height);
						}

						image.setBackgroundImageResponse(newResponse);

						image.save();
					}
				}

				// If we loaded image data (either from disk or network), put it into the memory cache
				if (image != null) {
					mCachedImageUrls.put(cacheUrl, image);
				}
			}
		}

		// We might end up falling back to an old response, which is fine;
		// we're not going for 100% accurate data here.
		return image;
	}

	/**
	 * Retrieves a car image.
	 */
	public ExpediaImage getCarImage(Car.Category category, Car.Type type, int width, int height,
			boolean useNetwork) {
		return getExpediaImage(ImageType.CAR, getImageCode(category, type), width, height, useNetwork);
	}

	/**
	 * Retrieves a destination image.
	 */
	public ExpediaImage getDestinationImage(String destinationCode, int width, int height, boolean useNetwork) {
		return getExpediaImage(ImageType.DESTINATION, destinationCode, width, height, useNetwork);
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility

	public static String getImageKey(ImageType imageType, String imageCode, int width, int height) {
		return imageType + ":" + imageCode + ":" + width + "x" + height;
	}

	public static String getImageCode(Car.Category category, Car.Type type) {
		return category.toString().replace("_", "") + "_" + type.toString().replace("_", "");
	}
}
