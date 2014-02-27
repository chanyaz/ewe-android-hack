package com.expedia.bookings.fragment;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.expedia.bookings.bitmaps.DestinationImageCache;
import com.expedia.bookings.data.ExpediaImage;
import com.expedia.bookings.data.ExpediaImageManager;
import com.expedia.bookings.fragment.base.MeasurableFragment;
import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.Log;
import com.mobiata.android.bitmaps.L2ImageCache;
import com.mobiata.android.util.AndroidUtils;

/**
 * ResultsBackgroundImageFragment: The fragment that acts as a background image for the whole
 * results activity designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class ResultsBackgroundImageFragment extends MeasurableFragment {

	private static final int FADE_ANIM_DURATION = 1200;

	private static final String KEY_IMG_DL = "KEY_IMG_DL";

	private static final String ARG_DEST_CODE = "ARG_DEST_CODE";
	private static final String ARG_BLUR = "ARG_BLUR";

	private String mDestinationCode;
	private boolean mBlur;

	private ImageView mImageView;

	private String mImgUrl; // Store the url if we need to hit the network
	private Bitmap mBgBitmap; // We temporarily store a bitmap here if we have not yet initialized

	public static ResultsBackgroundImageFragment newInstance(String destination, boolean blur) {
		ResultsBackgroundImageFragment fragment = new ResultsBackgroundImageFragment();
		Bundle args = new Bundle();
		args.putString(ARG_DEST_CODE, destination);
		args.putBoolean(ARG_BLUR, blur);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Fragment arguments
		Bundle args = getArguments();
		mDestinationCode = args.getString(ARG_DEST_CODE);
		mBlur = args.getBoolean(ARG_BLUR);

		// Check the availability of the destination image in the background
		BackgroundDownloader downloader = BackgroundDownloader.getInstance();
		if (!downloader.isDownloading(KEY_IMG_DL)) {
			downloader.startDownload(KEY_IMG_DL, mDownload, mCallback);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mImageView = new ImageView(getActivity());
		mImageView.setScaleType(ScaleType.FIT_XY);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mImageView.setLayoutParams(params);

		if (mBgBitmap != null) {
			handleBitmap(mBgBitmap, false);
		}

		return mImageView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (BackgroundDownloader.getInstance().isDownloading(KEY_IMG_DL)) {
			BackgroundDownloader.getInstance().registerDownloadCallback(KEY_IMG_DL, mCallback);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (getActivity() != null && getActivity().isFinishing()) {
			BackgroundDownloader.getInstance().cancelDownload(KEY_IMG_DL);
		}
		else {
			BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_IMG_DL);
		}
	}

	///////////////////////////////////////////////////////////////
	// Download

	private final BackgroundDownloader.Download<Bitmap> mDownload = new BackgroundDownloader.Download<Bitmap>() {
		@Override
		public Bitmap doDownload() {
			// Screen size
			Point p = AndroidUtils.getScreenSize(getActivity());

			// Grab the image metadata, go to the network if need be
			ExpediaImageManager imageManager = ExpediaImageManager.getInstance();
			ExpediaImage expImage = imageManager.getDestinationImage(mDestinationCode, p.x, p.y, true);

			// Attempt to grab the image from either memory or disk
			L2ImageCache cache = DestinationImageCache.getInstance();
			String url = expImage.getUrl();

			Log.d("DestinationImageCache", "ResultsBackgroundImageFragment - loading " + mDestinationCode + " " + url + " blur=" + mBlur);

			Bitmap bitmap;
			if (mBlur) {
				bitmap = cache.getBlurredImage(url, true);
			}
			else {
				bitmap = cache.getImage(url, true);
			}

			if (bitmap != null) {
				return bitmap;
			}
			else {
				// We need to request the image from the network, store the URL for the request
				mImgUrl = url;
				return null;
			}
		}
	};

	private final BackgroundDownloader.OnDownloadComplete<Bitmap> mCallback = new BackgroundDownloader.OnDownloadComplete<Bitmap>() {
		@Override
		public void onDownload(Bitmap bitmap) {
			if (bitmap == null) {
				// We still don't have the image, so let's grab it from the network
				L2ImageCache cache = DestinationImageCache.getInstance();
				L2ImageCache.OnImageLoaded callback = new L2ImageCache.OnImageLoaded() {
					@Override
					public void onImageLoaded(String url, Bitmap bitmap) {
						handleBitmap(bitmap, true);
					}

					@Override
					public void onImageLoadFailed(String url) {
						Log.e("unable to dl image");
					}
				};

				if (mBlur) {
					cache.loadBlurredImage(mImgUrl, mImgUrl, callback);
				}
				else {
					cache.loadImage(mImgUrl, callback);
				}
			}
			else {
				handleBitmap(bitmap, false);
			}
		}
	};

	private void handleBitmap(Bitmap bitmap, boolean fade) {
		if (bitmap != null) {
			if (mImageView != null) {
				mImageView.setImageBitmap(bitmap);
				if (fade) {
					ObjectAnimator.ofFloat(mImageView, "alpha", 0.0f, 1.0f).setDuration(FADE_ANIM_DURATION).start();
				}
			}
			else {
				mBgBitmap = bitmap; // Store the Bitmap to get picked up in onCreateView
			}
		}
		else {
			Log.v("bitmap null null null");
		}
	}

}
