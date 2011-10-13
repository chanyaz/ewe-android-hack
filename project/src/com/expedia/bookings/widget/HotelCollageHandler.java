package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.StrUtils;
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
		
		List<String> imageUrls = StrUtils.getImageUrls(property);
		for (int i = 0; i < imageUrls.size() && i < MAX_NUM_IMAGES; i++) {
			String imageUrl = imageUrls.get(i);
			ImageCache.loadImage(imageUrl, mPropertyImageViews.get(i));
			mPropertyUrls.add(imageUrl);
		}
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

}
