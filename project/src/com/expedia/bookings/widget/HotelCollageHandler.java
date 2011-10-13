package com.expedia.bookings.widget;

import java.util.ArrayList;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.mobiata.android.ImageCache;

public class HotelCollageHandler {

	private static final int MAX_NUM_IMAGES = 4;

	private ArrayList<ImageView> mPropertyImageViews;
	private ArrayList<String> mPropertyUrls;

	private OnCollageImageClickedListener mListener;

	public HotelCollageHandler(View view, OnCollageImageClickedListener listener) {
		mListener = listener;

		mPropertyImageViews = new ArrayList<ImageView>(MAX_NUM_IMAGES);
		mPropertyUrls = new ArrayList<String>(MAX_NUM_IMAGES);

		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_1));
		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_2));
		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_3));
		mPropertyImageViews.add((ImageView) view.findViewById(R.id.property_image_view_4));

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
			mPropertyImageViews.get(i).setImageResource(R.drawable.ic_row_thumb_placeholder);
		}

		for (int i = 0; i < property.getMediaCount() && i < MAX_NUM_IMAGES; i++) {
			ImageCache.loadImage(property.getMedia(i).getUrl(), mPropertyImageViews.get(i));
			mPropertyUrls.add(property.getMedia(i).getUrl());
		}
	}

	private OnClickListener mCollageImageClickedListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				int index = mPropertyImageViews.indexOf(v);
				mListener.onImageClicked(mPropertyUrls.get(index));
			}
		}
	};

	public interface OnCollageImageClickedListener {
		public void onImageClicked(String url);
	}

}
