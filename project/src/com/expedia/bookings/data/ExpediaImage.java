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

	public void setBackgroundImageResponse(BackgroundImageResponse response) {
		mTimestamp = response.getTimestamp();
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
}
