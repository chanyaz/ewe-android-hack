package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.mobiata.android.ImageCache.OnImageLoaded;

public class HotelCollage {

	private ArrayList<ImageView> mPropertyImageViews;
	private List<Media> mPropertyMediaList;
	private TextView mPromoDescriptionTextView;

	private OnCollageImageClickedListener mListener;

	private int mCurrentIndex;

	public HotelCollage(View view, OnCollageImageClickedListener listener) {
		mListener = listener;

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
		mCurrentIndex = 0;
		mPropertyMediaList.clear();
		if (property.getMediaList() != null) {
			mPropertyMediaList.addAll(property.getMediaList());
		}

		// remove any pending messages in the queue since new ones
		// will be scheduled for the cascading effect
		mHandler.removeMessages(LOAD_IMAGE);

		// set the default thumbnails for all images
		for (int i = 0; i < mPropertyImageViews.size(); i++) {
			mPropertyImageViews.get(i).setImageDrawable(null);
			mPropertyImageViews.get(i).setOnTouchListener(null);
		}

		// Configure views on top of the gallery
		Rate lowestRate = property.getLowestRate();
		String promoDescription = lowestRate == null ? null : lowestRate.getPromoDescription();
		if (promoDescription != null && promoDescription.length() > 0) {
			mPromoDescriptionTextView.setVisibility(View.VISIBLE);
			mPromoDescriptionTextView.setText(Html.fromHtml(promoDescription));
		}
		else {
			mPromoDescriptionTextView.setVisibility(View.GONE);
		}

		// Start the cascade of loading images
		if (property.getMediaCount() > 0) {
			startLoadingImages();
		}
	}

	private OnClickListener mCollageImageClickedListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mListener != null) {
				int index = mPropertyImageViews.indexOf(v);
				if (index != -1 && index < mPropertyMediaList.size()) {
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
			mPropertyImageViews.get(mCurrentIndex).setOnTouchListener(onImageTouch);

			drawable.startTransition(FADE_TIME);

			loadNextImage();
		}

		public void onImageLoadFailed(String url) {
			// Do nothing
		}
	};

	private final OnTouchListener onImageTouch = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				((ImageView) v).setColorFilter(0x82000000, PorterDuff.Mode.SRC_ATOP);
			}
			else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
				((ImageView) v).setColorFilter(0x00000000, PorterDuff.Mode.SRC_ATOP);
			}
			return false;
		}
	};

	private static final int LOAD_IMAGE = 1;

	private void startLoadingImages() {
		mCurrentIndex = -1;
		loadNextImage();
	}

	private void loadNextImage() {
		if (mCurrentIndex + 1 < mPropertyMediaList.size() && mCurrentIndex + 1 < mPropertyImageViews.size()) {
			mCurrentIndex++;
			mHandler.sendEmptyMessageDelayed(LOAD_IMAGE, FADE_PAUSE);
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case LOAD_IMAGE:
				Media media = mPropertyMediaList.get(mCurrentIndex);
				media.loadHighResImage(mPropertyImageViews.get(mCurrentIndex), mOnImageLoaded);
				break;

			default:
				super.handleMessage(msg);
			}
		}

	};
}
