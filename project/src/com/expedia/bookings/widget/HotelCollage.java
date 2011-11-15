package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;

public class HotelCollage {

	private ArrayList<ImageView> mPropertyImageViews;
	private List<Media> mPropertyMediaList;
	private TextView mPromoDescriptionTextView;
	private Context mContext;
	
	private OnCollageImageClickedListener mListener;

	private int mCurrentIndex;

	public HotelCollage(Context context, View view, OnCollageImageClickedListener listener) {
		mListener = listener;
		mContext = context;

		mPropertyImageViews = new ArrayList<ImageView>();
		mPropertyMediaList = new ArrayList<Media>();

		addViewToListIfExists(R.id.property_image_view_1, view);
		addViewToListIfExists(R.id.property_image_view_2, view);
		addViewToListIfExists(R.id.property_image_view_3, view);
		addViewToListIfExists(R.id.property_image_view_4, view);
		
		mPromoDescriptionTextView = (TextView) view.findViewById(R.id.promo_description_text_view);

		// Setup the background images
		for (int i = 0; i < mPropertyImageViews.size(); i++) {
			mPropertyImageViews.get(i).setBackgroundResource(R.drawable.blank_placeholder);
		}

		// clicking on any image in the hotel details should open up
		// the hotel gallery dialog
		for (ImageView imageView : mPropertyImageViews) {
			imageView.setOnClickListener(mCollageImageClickedListener);
		}
	}

	private void addViewToListIfExists(int viewId, View view) {
		ImageView imageView = (ImageView) view.findViewById(viewId);
		if (imageView != null) {
			mPropertyImageViews.add(imageView);
		}
	}

	public void updateCollage(Property property) {
		// set the default thumbnails for all images
		mPropertyMediaList.clear();
		for (int i = 0; i < mPropertyImageViews.size(); i++) {
			mPropertyImageViews.get(i).setImageDrawable(null);
		}

		mCurrentIndex = 0;

		// Load the property urls
		if (property.getMediaCount() == 0 || property.getMediaList() == null) {
			return;
		}

		mPropertyMediaList = StrUtils.getUniqueMediaList(property);

		// Configure views on top of the gallery
		Rate lowestRate = property.getLowestRate();
		String promoDescription = lowestRate.getPromoDescription();
		if (promoDescription != null && promoDescription.length() > 0) {
			mPromoDescriptionTextView.setVisibility(View.VISIBLE);
			mPromoDescriptionTextView.setText(Html.fromHtml(promoDescription));
		} else {
			mPromoDescriptionTextView.setVisibility(View.GONE);
		}

		
		// Start the cascade of loading images
		Media media = mPropertyMediaList.get(mCurrentIndex);
		ImageCache.loadImage(media.getHighResUrl(),
				media.getImageLoadedCallback(mPropertyImageViews.get(mCurrentIndex), mOnImageLoaded));
	}

	private OnClickListener mCollageImageClickedListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				int index = mPropertyImageViews.indexOf(v);
				if (index != -1 || index > (mPropertyMediaList.size() - 1)) {
					mListener.onImageClicked(mPropertyMediaList.get(index));
				}
			}
		}
	};

	public interface OnCollageImageClickedListener {
		public void onImageClicked(Media media);
	}

	//////////////////////////////////////////////////////////////////////////
	// Fades images in

	// The amount of time the image takes to fade in (when it's loaded), in ms
	private static final int FADE_TIME = 200;

	// The pause between loading an image and starting to download the next one, in ms
	// (FADE_TIME may be running at this point on a previous image)
	private static final int FADE_PAUSE = 30;

	private final OnImageLoaded mOnImageLoaded = new OnImageLoaded() {
		public void onImageLoaded(String url, Bitmap bitmap) {
			Drawable[] layers = new Drawable[2];
			layers[0] = new ColorDrawable(Color.TRANSPARENT);
			layers[1] = new BitmapDrawable(bitmap);
			TransitionDrawable drawable = new TransitionDrawable(layers);

			mPropertyImageViews.get(mCurrentIndex).setImageDrawable(drawable);
			drawable.startTransition(FADE_TIME);

			loadNextImage();
		}

		public void onImageLoadFailed(String url) {
			// Do nothing
		}
	};

	private void loadNextImage() {
		mCurrentIndex++;
		if (mCurrentIndex < mPropertyMediaList.size() && mCurrentIndex < mPropertyImageViews.size()) {
			final Media media = mPropertyMediaList.get(mCurrentIndex);
			mHandler.postDelayed(new Runnable() {
				public void run() {
					ImageCache.loadImage(media.getHighResUrl(),
							media.getImageLoadedCallback(mPropertyImageViews.get(mCurrentIndex), mOnImageLoaded));
				}
			}, FADE_PAUSE);
		}
	}

	private Handler mHandler = new Handler();
}
