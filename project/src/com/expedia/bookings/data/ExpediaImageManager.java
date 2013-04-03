package com.expedia.bookings.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;

import com.expedia.bookings.server.ExpediaServices;

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

	private Map<String, BackgroundImageResponse> mCachedImageUrls = new ConcurrentHashMap<String, BackgroundImageResponse>();

	public String getExpediaImage(ImageType imageType, String imageCode, int width, int height) {
		String cacheUrl = getCacheUrl(imageType, imageCode);

		if (!mCachedImageUrls.containsKey(cacheUrl)) {
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundImageResponse response = services.getExpediaImage(imageType, imageCode, width, height);
			mCachedImageUrls.put(cacheUrl, response);
		}

		return mCachedImageUrls.get(cacheUrl).getImageUrl();
	}

	private String getCacheUrl(ImageType imageType, String imageCode) {
		return imageType.getIdentifier() + ":" + imageCode;
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility

	public static String getImageCode(Car.Category category, Car.Type type) {
		return category.toString().replace("_", "") + "_" + type.toString().replace("_", "");
	}
}
