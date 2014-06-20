package com.expedia.bookings.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.bitmaps.L2ImageCache.OnBitmapLoaded;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.util.AndroidUtils;

/**
 * Manages images retrieved from Expedia's database.
 * <p/>
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
	}

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
	 * @param imageType  the type of image
	 * @param imageCode  the image code (determined by image type + other factors)
	 * @param width      the desired width (not guaranteed to be exact on return)
	 * @param height     the desired height (not guaranteed to be exact on return)
	 * @param useNetwork if true, will request .  If you set this to true, you better be operating on a non-UI thread!
	 * @return the URL if available, or null if not
	 */
	public ExpediaImage getExpediaImage(ImageType imageType, String imageCode, int width, int height,
										boolean useNetwork, L2ImageCache cache) {
		if (TextUtils.isEmpty(imageCode)) {
			throw new RuntimeException("Can't pass null as imageCode");
		}

		// The key should be unique for each request
		String cacheUrl = getImageKey(imageType, imageCode, width, height);

		ExpediaImage image = mCachedImageUrls.get(cacheUrl);
		if (image == null || image.getTimestamp() + EXPIRATION < System.currentTimeMillis()) {
			if (useNetwork) {
				// Try to retrieve the latest from disk
				image = ExpediaImage.getImage(imageType, imageCode, width, height);

				// If disk is non-existent or old, then resort to network
				if (image == null || image.getTimestamp() + EXPIRATION < System.currentTimeMillis()) {
					ExpediaServices services = new ExpediaServices(mContext);
					ExpediaImageResponse newResponse = services.getExpediaImage(imageType, imageCode, width, height);

					// We'll fall back to the old URL if we don't get a new one
					if (newResponse == null) {
						Log.e("ExpediaImageManager.getExpediaImage response is null. IOException?");
					}
					else if (newResponse.hasErrors()) {
						Log.e("ExpediaImageManager.getExpediaImage response has errors.");
						for (ServerError error : newResponse.getErrors()) {
							Log.e("ExpImage error: " + error.toJson().toString());
						}
					}
					else {
						// See if we should expire the old cached image out of our image cache
						if (image != null && !image.getCacheKey().equals(newResponse.getCacheKey())) {
							cache.removeImage(image.getUrl());
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
	 * Defaults to expiring image from general-purpose cache
	 */
	public ExpediaImage getExpediaImage(ImageType imageType, String imageCode, int width, int height,
										boolean useNetwork) {
		return getExpediaImage(imageType, imageCode, width, height, useNetwork, L2ImageCache.sGeneralPurpose);
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
		return getExpediaImage(ImageType.DESTINATION, destinationCode, width, height, useNetwork,
			L2ImageCache.sDestination);
	}

	//////////////////////////////////////////////////////////////////////////
	// Utility

	public static String getImageKey(ImageType imageType, String imageCode, int width, int height) {
		return imageType + ":" + imageCode + ":" + width + "x" + height;
	}

	public static String getImageCode(Car.Category category, Car.Type type) {
		return category.toString().replace("_", "") + "_" + type.toString().replace("_", "");
	}

	public static class ImageParams {

		private int mWidth;
		private int mHeight;
		private boolean mBlur;
		private String mDestinationId;

		public int getWidth() {
			return mWidth;
		}

		public ImageParams setWidth(int x) {
			mWidth = x;
			return this;
		}

		public int getHeight() {
			return mHeight;
		}

		public ImageParams setHeight(int y) {
			mHeight = y;
			return this;

		}

		public boolean isBlur() {
			return mBlur;
		}

		public ImageParams setBlur(boolean blur) {
			mBlur = blur;
			return this;
		}

		public String getDestinationId() {
			return mDestinationId;
		}

		public ImageParams setDestinationId(String destinationId) {
			mDestinationId = destinationId;
			return this;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Bitmap download utility helper methods!! WIN WIN WIN


	/**
	 * This is a specialized helper method designed for a specific use-case. This method will grab the
	 * full-screen destination background image. This method is particularly useful for the phone checkout
	 * flow. This method makes many assumptions:
	 * <p/>
	 * 1. The image is the resolution of the full screen size via AndroidUtils.getDisplaySize()
	 * 2. The ExpediaImage metadata has been downloading from the network and is present in memory
	 * 3. The Bitmap is present in the memory cache
	 * <p/>
	 * With these assumptions in mind, use the method appropriately.
	 *
	 * @param context
	 * @param flightSearch
	 * @param blur
	 * @return
	 */
	public Bitmap getDestinationBitmap(Context context, FlightSearch flightSearch, boolean blur) {
		String url = getDestinationBitmapKey(context, flightSearch);
		boolean checkDisk = true;
		return L2ImageCache.sDestination.getImage(url, blur, checkDisk);
	}

	public String getDestinationBitmapKey(Context context, FlightSearch flightSearch) {
		Point p = AndroidUtils.getDisplaySize(context);
		int temp;
		if (p.x > p.y) {
			temp = p.x;
			p.x = p.y;
			p.y = temp;
		}

		final String airportCode = flightSearch.getSearchParams().getArrivalLocation().getDestinationId();
		ExpediaImage expImage = getDestinationImage(airportCode, p.x, p.y, false); // should be in memory
		if (expImage == null) {
			Log.e("ExpediaImageManager - Expected ExpediaImage data in memory but not present for airport=" + airportCode
				+ " width=" + p.x + " height=" + p.y);
			return null;
		}
		String url = expImage.getThumborUrl(p.x, p.y);
		return url;
	}

	/**
	 * This helper method will set the ImageView Bitmap to the full-screen destination Bitmap sitting in memory.
	 *
	 * @param context
	 * @param imageView
	 * @param flightSearch
	 * @param blur
	 */
	public void setDestinationBitmap(Context context, ImageView imageView, FlightSearch flightSearch, boolean blur) {
		Bitmap bitmap = getDestinationBitmap(context, flightSearch, blur);
		if (bitmap == null) {
			bitmap = L2ImageCache.sDestination.getImage(context.getResources(), R.drawable.default_flights_background, blur);
		}
		imageView.setImageBitmap(bitmap);
	}

	/**
	 * Use within the flights phone flow.
	 */
	public void loadDestinationBitmap(Context context, FlightSearch flightSearch, final boolean blur) {
		final String airportCode = flightSearch.getSearchParams().getArrivalLocation().getDestinationId();
		loadDestinationBitmap(context, airportCode, blur, new OnBitmapLoaded() {
			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				// Phone flow does not require callbacks
			}

			@Override
			public void onBitmapLoadFailed(String url) {
				// Phone flow does not require callback
			}
		});
	}

	/**
	 * Use within the flights phone flow.
	 */
	public void loadDestinationBitmap(Context context, final String airportCode, final boolean blur, final OnBitmapLoaded callback) {
		// the params
		final String bgdKey = generateBackgroundDownloaderKey(airportCode, blur);
		final Point p = AndroidUtils.getDisplaySize(context);

		doImageDownload(airportCode, p.x, p.y, blur, bgdKey, callback);
	}

	/**
	 * Use within the tablet flow
	 */
	public void loadDestinationBitmap(final ImageParams params, final L2ImageCache.OnBitmapLoaded callback) {
		final String airportCode = !TextUtils.isEmpty(params.getDestinationId()) ? params.getDestinationId() : Sp.getParams().getDestination().getAirportCode();
		final String bgdKey = generateBackgroundDownloaderKey(airportCode, params.isBlur());

		doImageDownload(airportCode, params.getWidth(), params.getHeight(), params.isBlur(), bgdKey, callback);
	}

	private void doImageDownload(final String airportCode, final int width, final int height, final boolean blur,
								 final String bgdKey, final OnBitmapLoaded callback) {

		// Start background download
		BackgroundDownloader.getInstance().startDownload(bgdKey, new BackgroundDownloader.Download<Object[]>() {
			@Override
			public Object[] doDownload() {
				// Grab the ExpediaImage metadata
				ExpediaImage expImage = getDestinationImage(airportCode, width, height, true);

				if (expImage == null) {
					// This shouldn't happen, but sometimes server requests fail
					return null;
				}

				// Image url - use Thumbor for correct size
				String url = expImage.getThumborUrl(width, height);

				Bitmap bitmap = L2ImageCache.sDestination.getImage(url, blur, true);
				if (bitmap != null) {
					// Pass the results along to the OnDownloadComplete to invoke callback.
					// We want the callback to be invoked on the UI thread, as the given
					// callback generally modifies Views.
					Object[] results = new Object[2];
					results[0] = url;
					results[1] = bitmap;
					return results;
				}
				else {
					L2ImageCache.sDestination.loadImage(url, blur, callback);
				}

				return null;
			}
		}, new BackgroundDownloader.OnDownloadComplete<Object[]>() {
			@Override
			public void onDownload(Object... results) {
				if (results != null) {
					String url = (String) results[0];
					Bitmap bitmap = (Bitmap) results[1];
					Log.i("ExpediaImageManager - url=" + url + " found in destination cache.");
					callback.onBitmapLoaded(url, bitmap);
				}
			}
		});
	}

	public boolean isDownloadingDestinationImage(boolean blur) {
		return BackgroundDownloader.getInstance().isDownloading(generateBackgroundDownloaderKey(blur));
	}

	public void cancelDownloadingDestinationImage(FlightSearch flightSearch, boolean blur) {
		BackgroundDownloader.getInstance().cancelDownload(generateBackgroundDownloaderKey(flightSearch, blur));
	}

	public void cancelDownloadingDestinationImage(boolean blur) {
		BackgroundDownloader.getInstance().cancelDownload(generateBackgroundDownloaderKey(blur));
	}

	private static final String BGD_DESTINATION_DL_KEY_BASE = "BGD_DESTINATION_DL_KEY_BASE";

	private static String generateBackgroundDownloaderKey(FlightSearch flightSearch, boolean blur) {
		return generateBackgroundDownloaderKey(flightSearch.getSearchParams().getArrivalLocation().getDestinationId(), blur);
	}

	private static String generateBackgroundDownloaderKey(boolean blur) {
		return generateBackgroundDownloaderKey(Sp.getParams().getDestination().getAirportCode(), blur);
	}

	private static String generateBackgroundDownloaderKey(final String airportCode, final boolean blur) {
		return BGD_DESTINATION_DL_KEY_BASE + "-" + airportCode + "-" + String.valueOf(blur);
	}

}
