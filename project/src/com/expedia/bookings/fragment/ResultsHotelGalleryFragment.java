package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.interfaces.IResultsHotelGalleryBackClickedListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

public class ResultsHotelGalleryFragment extends Fragment {

	public static ResultsHotelGalleryFragment newInstance() {
		ResultsHotelGalleryFragment frag = new ResultsHotelGalleryFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private ViewGroup mGalleryActionBar;
	private TextView mDoneText;
	private TextView mHotelText;
	private ViewPager mPager;
	private View mBackground;

	private MediaPagerAdapter mAdapter;
	private IResultsHotelGalleryBackClickedListener mHotelGalleryBackClickedListener;

	private static final String INSTANCE_CURRENT_IMAGE = "INSTANCE_CURRENT_IMAGE";
	private static final int NO_IMAGE = 0;
	private int mCurrentImagePosition = NO_IMAGE;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mHotelGalleryBackClickedListener = Ui.findFragmentListener(this, IResultsHotelGalleryBackClickedListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_tablet_hotel_gallery, null);
		mGalleryActionBar = Ui.findView(mRootC, R.id.gallery_action_bar);
		mDoneText = Ui.findView(mRootC, R.id.done_button);
		mHotelText = Ui.findView(mRootC, R.id.photos_for_hotel_text);
		mPager = Ui.findView(mRootC, R.id.pager);
		mPager.setPageMargin((int) getResources().getDimension(R.dimen.tablet_gallery_viewpager_gutter_margin));
		mPager.setClipToPadding(false);
		mPager.setOffscreenPageLimit(5);

		mBackground = Ui.findView(mRootC, R.id.background_view);

		mDoneText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHotelGalleryBackClickedListener.onHotelGalleryBackClicked();
			}
		});

		if (savedInstanceState != null) {
			mCurrentImagePosition = savedInstanceState.getInt(INSTANCE_CURRENT_IMAGE, NO_IMAGE);
		}

		mAdapter = new MediaPagerAdapter(getActivity());
		mPager.setAdapter(mAdapter);
		return mRootC;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		mCurrentImagePosition = mPager.getCurrentItem();
		outState.putInt(INSTANCE_CURRENT_IMAGE, mCurrentImagePosition);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (Db.getHotelSearch().getSelectedProperty() != null) {
			bind(Db.getHotelSearch().getSelectedProperty());
		}
		Events.register(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	private void bind(Property property) {
		String photosForText = getString(R.string.photos_for_TEMPLATE, property.getName());
		mHotelText.setText(photosForText);

		if (property.getMediaList() != null) {
			mAdapter.replaceWith(property.getMediaList());
			mPager.setCurrentItem(mCurrentImagePosition);
			mCurrentImagePosition = NO_IMAGE;
		}
	}

	public void onHotelSelected() {
		bind(Db.getHotelSearch().getSelectedProperty());
	}

	public Rect getCurrentImageLocationInWindow() {
		View root = mPager.getChildAt(mPager.getCurrentItem());
		ImageView image = Ui.findView(root, R.id.image);

		int[] location = new int[2];
		image.getLocationInWindow(location);
		final int x = location[0];
		final int y = location[1];
		return new Rect(x, y, x + image.getWidth(), y + image.getHeight());
	}

	public void setAnimationPercentage(float p, Rect detailsCoords, Rect galleryCoords) {
		mBackground.setAlpha(p);
		mPager.setAlpha(p);
		mGalleryActionBar.setTranslationY(-mGalleryActionBar.getHeight() * (1.0f - p));
	}

	public void setHardwareLayer(int layerValue) {
		mGalleryActionBar.setLayerType(layerValue, null);
		mPager.setLayerType(layerValue, null);
	}

	private static class MediaPagerAdapter extends PagerAdapter {
		private List<Media> mMedia;
		private int mImageWidth;
		private int mImageHeight;

		public MediaPagerAdapter(Context context) {
			Point screen = Ui.getScreenSize(context);
			mImageWidth = (int) (screen.x * 0.60f);
			mImageHeight = (int) (screen.y * 0.60f);
		}

		public void replaceWith(List<Media> media) {
			mMedia = media;
			notifyDataSetChanged();
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {
			View root = Ui.inflate(collection.getContext(), R.layout.snippet_tablet_hotel_gallery_item, null);

			Media media = mMedia.get(position);
			final ImageView image = Ui.findView(root, R.id.image);

			image.setPivotX(0.0f);
			image.setPivotY(0.0f);
			media.loadHighResImage(image, new L2ImageCache.OnBitmapLoaded() {
				@Override
				public void onBitmapLoaded(String url, Bitmap bitmap) {
					LayoutParams params = image.getLayoutParams();
					params.width = mImageWidth;
					params.height = mImageHeight;
					image.setLayoutParams(params);
				}

				@Override
				public void onBitmapLoadFailed(String url) {
					// ignore
				}
			});

			ViewGroup group = (ViewGroup) collection;
			if (position >= group.getChildCount()) {
				group.addView(root);
			}
			else {
				group.addView(root, position);
			}
			return root;
		}

		@Override
		public void destroyItem(ViewGroup collection, int position, Object key) {
			collection.removeView((View) key);
		}

		@Override
		public int getCount() {
			if (mMedia == null) {
				return 0;
			}
			return mMedia.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object key) {
			return view == ((View) key);
		}
	}

	@Subscribe
	public void onEvent(Events.HotelAvailabilityUpdated event) {
		bind(Db.getHotelSearch().getSelectedProperty());
	}
}
