package com.expedia.bookings.activity;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.json.JSONUtils;

public class PhoneHotelGalleryActivity extends Activity {

	private Gallery mHotelGallery;
	private ImageAdapter mAdapter;
	private Property mProperty;
	private Media mSelectedMedia;
	private ViewPager mPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_phone_hotel_gallery);

		if (savedInstanceState != null && savedInstanceState.containsKey(Codes.PROPERTY)
				&& savedInstanceState.containsKey(Codes.SELECTED_IMAGE)) {
			mProperty = JSONUtils.parseJSONObjectFromBundle(savedInstanceState, Codes.PROPERTY, Property.class);
			mSelectedMedia = JSONUtils.parseJSONObjectFromBundle(savedInstanceState, Codes.SELECTED_IMAGE, Media.class);
		}
		else {
			mProperty = JSONUtils.parseJSONableFromIntent(getIntent(), Codes.PROPERTY, Property.class);
			mSelectedMedia = JSONUtils.parseJSONableFromIntent(getIntent(), Codes.SELECTED_IMAGE, Media.class);
		}

		mAdapter = new ImageAdapter((Context) this);
		mAdapter.setMedia(mProperty.getMediaList());

		// setup the ViewPager
		HotelImagePagerAdapter pagerAdapter = new HotelImagePagerAdapter((Context) this, mAdapter);
		mPager = (ViewPager) findViewById(R.id.big_image_pager);
		mPager.setAdapter(pagerAdapter);

		mHotelGallery = (Gallery) findViewById(R.id.hotel_gallery);
		mHotelGallery.setAdapter(mAdapter);
		mHotelGallery.setCallbackDuringFling(false);
		//mHotelGallery.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);

		/*
		 * setup all the event listeners
		 */

		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (mSelectedMedia != mAdapter.getItem(position)) {
					mHotelGallery.setSelection(position);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mHotelGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View imageView, int position, long id) {
				mSelectedMedia = (Media) mAdapter.getItem(position);
				mPager.setCurrentItem(position);
			}
		});

		mHotelGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				mSelectedMedia = (Media) mAdapter.getItem(position);
				mPager.setCurrentItem(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mSelectedMedia = (Media) mAdapter.getItem(0);
				mPager.setCurrentItem(0);
			}
		});

		int position = (mSelectedMedia == null) ? 0 : mAdapter.getPositionOfMedia(mSelectedMedia);
		mSelectedMedia = (mSelectedMedia == null) ? (Media) mAdapter.getItem(0) : mSelectedMedia;
		mHotelGallery.setSelection(position);
	}

	public Media getHotelMedia(int position) {
		return (Media) mAdapter.getItem(position);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mProperty != null) {
			outState.putString(Codes.PROPERTY, mProperty.toString());
		}
		if (mSelectedMedia != null) {
			outState.putString(Codes.SELECTED_IMAGE, mSelectedMedia.toString());
		}
	}

	private class ImageAdapter extends BaseAdapter {
		private List<Media> mMedia;
		private LayoutInflater mInflater;

		public ImageAdapter(Context context) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void setMedia(List<Media> media) {
			mMedia = media;
			notifyDataSetChanged();
		}

		public int getPositionOfMedia(Media media) {
			for (int i = 0; i < mMedia.size(); i++) {
				if (mMedia.get(i).getUrl().equals(media.getUrl())) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getCount() {
			return mMedia.size();
		}

		@Override
		public Object getItem(int position) {
			return mMedia.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = (ImageView) convertView;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.phone_gallery_item, null);
				imageView = (ImageView) convertView.findViewById(R.id.image);
			}

			return convertView;
		}
	}

	private static final String IMAGE_POSITION = "POSITION";

	public class HotelImagePagerAdapter extends PagerAdapter {
		LayoutInflater mInflater;
		ImageAdapter mAdapter;

		public HotelImagePagerAdapter (Context context, ImageAdapter a) {
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mAdapter = a;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(View collection, int position) {
			View view = mInflater.inflate(R.layout.phone_pager_hotel_image, (ViewGroup) collection, false);
			((ViewPager) collection).addView(view);
			final ImageView imageView = (ImageView) view.findViewById(R.id.big_image_view);
			final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.hotel_image_progress_bar);
			Media hotelMedia = (Media) mAdapter.getItem(position);
			hotelMedia.loadHighResImage(imageView, new OnImageLoaded() {

				@Override
				public void onImageLoaded(String url, Bitmap bitmap) {
					progressBar.setVisibility(View.GONE);
					imageView.setVisibility(View.VISIBLE);
				}

				@Override
				public void onImageLoadFailed(String url) {
					progressBar.setVisibility(View.GONE);
					imageView.setVisibility(View.VISIBLE);
				}
			});

			return view;
		}

		@Override
		public int getCount() {
			return mAdapter.getCount();
		}

		@Override
		public void destroyItem(View collection, int position, Object view) {
			((ViewPager) collection).removeView((View) view);
		}

		// The rest are fairly useless

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public void startUpdate(View arg0) {
		}

		@Override
		public void finishUpdate(View arg0) {
		}
	}
}
