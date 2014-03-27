package com.expedia.bookings.data;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.expedia.bookings.data.ExpediaImageManager.ImageType;

@Table(name = "ExpediaImages")
public class ExpediaImage extends Model {
	// Fields we search on

	@Column(name = "ImageType")
	private ImageType mImageType;

	@Column(name = "ImageCode")
	private String mImageCode;

	@Column(name = "Width")
	private int mWidth;

	@Column(name = "Height")
	private int mHeight;

	// Fields that are returned by responses from the server

	@Column(name = "Timestamp")
	private long mTimestamp;

	@Column(name = "Url")
	private String mUrl;

	@Column(name = "CacheKey")
	private String mCacheKey;

	public ExpediaImage() {
		super();
	}

	public ExpediaImage(ImageType imageType, String imageCode, int width, int height) {
		super();

		mImageType = imageType;
		mImageCode = imageCode;
		mWidth = width;
		mHeight = height;
	}

	public void setBackgroundImageResponse(ExpediaImageResponse response) {
		mTimestamp = response.getTimestamp().getMillis();
		mUrl = response.getImageUrl();
		mCacheKey = response.getCacheKey();
	}

	public long getTimestamp() {
		return mTimestamp;
	}

	public String getUrl() {
		return mUrl;
	}

	public String getCacheKey() {
		return mCacheKey;
	}

	public static ExpediaImage getImage(ImageType imageType, String imageCode, int width, int height) {
		return new Select()
				.from(ExpediaImage.class)
				.where("ImageType = ? AND ImageCode = ? AND Width = ? AND Height = ?", imageType, imageCode, width,
						height)
				.executeSingle();
	}

	/////////////////////////////////////////
	// Thumbor

	// TODO !!!TEST TEST TEST URL!!! DO NOT SHIP WITH THIS URL
	private static final String THUMBOR_SERVICE_URL_TEMPLATE = "http://ewetest.img.mobiata.com/unsafe/%s/smart/%s";

	public String getThumborUrl(final int width, final int height) {
		String dimensionsStrParam = String.valueOf(width) + "x" + String.valueOf(height);
		String imgKey = mUrl.substring(7, mUrl.length()); // "http://" is 7 chars
		return String.format(THUMBOR_SERVICE_URL_TEMPLATE, dimensionsStrParam, imgKey);
	}

}
