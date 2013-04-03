package com.expedia.bookings.graphics;

import android.content.res.Resources;

import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.ExpediaImageManager.ImageType;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

// TODO: Store URLs in ExpediaImageManager
public class DestinationBitmapDrawable extends UrlBitmapDrawable implements Download<String>,
		OnDownloadComplete<String> {

	private String mUrl;

	private ImageType mImageType;
	private String mImageCode;
	private int mWidth;
	private int mHeight;

	/**
	 * Constructor for DESTINATION images
	 */
	public DestinationBitmapDrawable(Resources res, int placeholderResId, String destinationCode, int width,
			int height) {
		super(res, (String) null, placeholderResId);

		mImageType = ImageType.DESTINATION;
		mImageCode = destinationCode;
		mWidth = width;
		mHeight = height;

		retrieveUrl();
	}

	/**
	 * Constructor for CAR images
	 */
	public DestinationBitmapDrawable(Resources res, int placeholderResId, Car.Category category, Car.Type type,
			int width, int height) {
		super(res, (String) null, placeholderResId);

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
		return ExpediaImageManager.getInstance().getExpediaImage(mImageType, mImageCode, mWidth, mHeight);
	}
}
