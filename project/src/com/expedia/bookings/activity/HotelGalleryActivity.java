package com.expedia.bookings.activity;

import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.json.JSONUtils;

public class HotelGalleryActivity extends Activity {
	private Gallery mHotelGallery;
	private ImageView mBigImageView;
	private ImageAdapter mAdapter;
	private Property mProperty;
	private Media mSelectedMedia;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.fragment_hotel_gallery);

		if (savedInstanceState != null && savedInstanceState.containsKey(Codes.PROPERTY)
				&& savedInstanceState.containsKey(Codes.SELECTED_IMAGE)) {
			mProperty = JSONUtils.parseJSONObjectFromBundle(savedInstanceState, Codes.PROPERTY, Property.class);
			mSelectedMedia = JSONUtils.parseJSONObjectFromBundle(savedInstanceState, Codes.SELECTED_IMAGE, Media.class);
		}
		else {
			mProperty = JSONUtils.parseJSONableFromIntent(getIntent(), Codes.PROPERTY, Property.class);
			mSelectedMedia = JSONUtils.parseJSONableFromIntent(getIntent(), Codes.SELECTED_IMAGE, Media.class);
		}

		ActionBar actionBar = getActionBar();
		actionBar.setTitle(Html.fromHtml(getString(R.string.gallery_title_template, mProperty.getName())));
		actionBar.setDisplayHomeAsUpEnabled(true);

		mHotelGallery = (Gallery) findViewById(R.id.hotel_gallery);
		mBigImageView = (ImageView) findViewById(R.id.big_image_view);

		mAdapter = new ImageAdapter();
		mAdapter.setMedia(StrUtils.getUniqueMediaList(mProperty));
		mHotelGallery.setAdapter(mAdapter);
		mHotelGallery.setCallbackDuringFling(false);

		mHotelGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> l, View imageView, int position, long id) {
				mSelectedMedia = (Media) mAdapter.getItem(position);
				mSelectedMedia.loadHighResImage(mBigImageView, null);
			}
		});

		mHotelGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				mSelectedMedia = (Media) mAdapter.getItem(position);
				mSelectedMedia.loadHighResImage(mBigImageView, null);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mSelectedMedia = (Media) mAdapter.getItem(0);
				mSelectedMedia.loadHighResImage(mBigImageView, null);
			}
		});

		int position = (mSelectedMedia == null) ? 0 : mAdapter.getPositionOfImage(mSelectedMedia);
		mSelectedMedia = (mSelectedMedia == null) ? (Media) mAdapter.getItem(0) : mSelectedMedia;

		mHotelGallery.setSelection(position);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home: {
			onBackPressed();
			return true;
		}
		default: {
			return super.onOptionsItemSelected(item);
		}
		}
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

		public void setMedia(List<Media> media) {
			mMedia = media;
			notifyDataSetChanged();
		}

		public int getPositionOfImage(Media media) {
			return mMedia.indexOf(media);
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
				int thumbnailDimensionDp = (int) Math.ceil(getResources().getDisplayMetrics().density * 150);
				imageView = new ImageView(HotelGalleryActivity.this);
				imageView.setLayoutParams(new Gallery.LayoutParams(thumbnailDimensionDp, thumbnailDimensionDp));
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setBackgroundResource(R.drawable.bg_gallery_photo);
				convertView = imageView;
			}

			Media media = mMedia.get(position);
			boolean imageSet = ImageCache.loadImage(media.getUrl(), imageView);
			if (!imageSet) {
				imageView.setImageResource(R.drawable.ic_row_thumb_placeholder);
			}

			return convertView;
		}

	}
}