package com.expedia.bookings.graphics;

import android.content.Context;

import com.expedia.bookings.data.BackgroundImageResponse;
import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.ExpediaImageManager.ImageType;
import com.expedia.bookings.server.ExpediaServices;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

// TODO: Store URLs in ExpediaImageManager
public class DestinationBitmapDrawable extends UrlBitmapDrawable implements Download<String>,
		OnDownloadComplete<String> {

	private String mUrl;

	private Context mContext;

	private ImageType mImageType;
	private String mImageCode;
	private int mWidth;
	private int mHeight;

	/**
	 * Constructor for DESTINATION images
	 */
	public DestinationBitmapDrawable(Context context, int placeholderResId, String destinationCode, int width,
			int height) {
		super(context.getResources(), (String) null, placeholderResId);

		mContext = context;

		mImageType = ImageType.DESTINATION;
		mImageCode = destinationCode;
		mWidth = width;
		mHeight = height;

		retrieveUrl();
	}

	/**
	 * Constructor for CAR images
	 */
	public DestinationBitmapDrawable(Context context, int placeholderResId, Car.Category category, Car.Type type,
			int width, int height) {
		super(context.getResources(), (String) null, placeholderResId);

		mContext = context;

		mImageType = ImageType.CAR;
		mImageCode = ExpediaImageManager.getImageCode(category, type);
		mWidth = width;
		mHeight = height;

		retrieveUrl();
	}

	@Override
	protected String getUrl() {
		return mUrl;
	}

	//////////////////////////////////////////////////////////////////////////
	// URL Retrieval

	private void retrieveUrl() {
		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		String key = this.toString();
		bd.startDownload(key, this, this);
	}

	@Override
	public void onDownload(String results) {
		mUrl = results;

		retrieveImage(true);
	}

	@Override
	public String doDownload() {
		ExpediaServices services = new ExpediaServices(mContext);
		BackgroundImageResponse response = services.getExpediaImage(mImageType, mImageCode, mWidth, mHeight);
		return response.getImageUrl();
	}
}
