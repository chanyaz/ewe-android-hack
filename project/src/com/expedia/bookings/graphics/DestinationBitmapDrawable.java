package com.expedia.bookings.graphics;

import android.content.res.Resources;
import android.text.TextUtils;

import com.expedia.bookings.data.Car;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.data.ExpediaImageManager.ImageType;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

public class DestinationBitmapDrawable extends UrlBitmapDrawable implements Download<ExpediaImage>,
		OnDownloadComplete<ExpediaImage> {

	private String mUrl;
	private String mSharableUrl; // Sharable Url to low res image. Used while sharing in Facebook.

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
		retrieveSharableUrl();
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
		retrieveSharableUrl();
	}

	@Override
	protected String getUrl() {
		return mUrl;
	}

	public String getSharableUrl() {
		return mSharableUrl;
	}

	//////////////////////////////////////////////////////////////////////////
	// URL Retrieval

	private void retrieveUrl() {
		ExpediaImage image = ExpediaImageManager.getInstance().getExpediaImage(mImageType, mImageCode,
				mWidth, mHeight, false);
		if (image != null) {
			onDownload(image);
		}
		else {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			String key = ExpediaImageManager.getImageKey(mImageType, mImageCode, mWidth, mHeight);
			bd.startDownload(key, this, this);
		}
	}

	/**
	 * We have to make a quick call to fetch the lowest available resolution image for the particular destination.
	 * Low res images are preferred to be used to show while posting on Facebook.
	 */
	private void retrieveSharableUrl() {
		// Set the width and height to 0, so that the api returns the lowest res image file.
		ExpediaImage image = ExpediaImageManager.getInstance().getExpediaImage(ImageType.DESTINATION,
				mImageCode, 0, 0, false);
		if (image != null) {
			mSharableUrlCallBack.onDownload(image);
		}
		else {
			BackgroundDownloader bd = BackgroundDownloader.getInstance();
			String key = ExpediaImageManager.getImageKey(ImageType.DESTINATION, mImageCode, 0, 0);
			bd.startDownload(key, mSharableUrlDownload, mSharableUrlCallBack);
		}
	}

	@Override
	public void onDownload(ExpediaImage image) {
		// Don't bother reloading the image if the URL hasn't changed
		if (image != null && !TextUtils.equals(mUrl, image.getUrl())) {
			mUrl = image.getUrl();

			retrieveImage(true);
		}
	}

	@Override
	public ExpediaImage doDownload() {
		return ExpediaImageManager.getInstance().getExpediaImage(mImageType, mImageCode, mWidth, mHeight, true);
	}

	private final Download<ExpediaImage> mSharableUrlDownload = new Download<ExpediaImage>() {

		@Override
		public ExpediaImage doDownload() {
			return ExpediaImageManager.getInstance().getExpediaImage(ImageType.DESTINATION, mImageCode, 0, 0, true);
		}
	};

	private final OnDownloadComplete<ExpediaImage> mSharableUrlCallBack = new OnDownloadComplete<ExpediaImage>() {

		@Override
		public void onDownload(ExpediaImage results) {
			if (results != null) {
				mSharableUrl = results.getUrl();
			}
		}
	};

}
