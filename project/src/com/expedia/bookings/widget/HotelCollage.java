package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;

public class HotelCollage {

	private static final int MAX_NUM_IMAGES = 4;

	private ArrayList<ImageView> mPropertyImageViews;
	private ArrayList<String> mPropertyUrls;

	private OnCollageImageClickedListener mListener;

	private int mCurrentIndex;

	public HotelCollage(View view, OnCollageImageClickedListener listener) {
		mListener = listener;

		mPropertyImageViews = new ArrayList<ImageView>(MAX_NUM_IMAGES);
		mPropertyUrls = new ArrayList<String>(MAX_NUM_IMAGES);

		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_1));
		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_2));
		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_3));
		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_4));

		// Setup the background images
		for (int i = 0; i < MAX_NUM_IMAGES; i++) {
			mPropertyImageViews.get(i).setBackgroundResource(R.drawable.blank_placeholder);
		}

		// clicking on any image in the hotel details should open up
		// the hotel gallery dialog
		for (ImageView imageView : mPropertyImageViews) {
			imageView.setOnClickListener(mCollageImageClickedListener);
		}
	}

	public void updateCollage(Property property) {
		// set the default thumbnails for all images
		mPropertyUrls.clear();
		for (int i = 0; i < MAX_NUM_IMAGES; i++) {
			mPropertyImageViews.get(i).setImageDrawable(null);
		}

		mCurrentIndex = 0;

		// Load the property urls
		List<String> imageUrls = StrUtils.getImageUrls(property);
		for (int i = 0; i < imageUrls.size() && i < MAX_NUM_IMAGES; i++) {
			String imageUrl = imageUrls.get(i);
			mPropertyUrls.add(imageUrl);
		}

		// Start the cascade of loading images
		ImageCache.loadImage(mPropertyUrls.get(mCurrentIndex), mOnImageLoaded);
	}

	private OnClickListener mCollageImageClickedListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				int index = mPropertyImageViews.indexOf(v);
				if (index != -1) {
					mListener.onImageClicked(mPropertyUrls.get(index));
				}
			}
		}
	};

	public interface OnCollageImageClickedListener {
		public void onImageClicked(String url);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fades images in

	private static final int FADE_TIME = 500;
	private static final int FADE_PAUSE = 100;

	private final OnImageLoaded mOnImageLoaded = new OnImageLoaded() {
		public void onImageLoaded(String url, Bitmap bitmap) {
			Drawable[] layers = new Drawable[2];
			layers[0] = new ColorDrawable(Color.TRANSPARENT);
			layers[1] = new BitmapDrawable(bitmap);
			TransitionDrawable drawable = new TransitionDrawable(layers);
			mPropertyImageViews.get(mCurrentIndex).setImageDrawable(drawable);
			drawable.startTransition(FADE_TIME);

			mCurrentIndex++;
			if (mCurrentIndex < mPropertyUrls.size() && mCurrentIndex < mPropertyImageViews.size()) {
				mHandler.postDelayed(new Runnable() {
					public void run() {
						ImageCache.loadImage(mPropertyUrls.get(mCurrentIndex), mOnImageLoaded);
					}
				}, FADE_PAUSE);
			}
		}
	};

	private Handler mHandler = new Handler();
}
