package com.expedia.bookings.activity;

import java.util.List;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.LayoutInflater;
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
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.mobiata.android.ImageCache;
import com.mobiata.android.ImageCache.OnImageLoaded;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;

public class HotelGalleryActivity extends FragmentActivity {

	private Gallery mHotelGallery;
	private ImageAdapter mAdapter;
	private Property mProperty;
	private Media mSelectedMedia;
	private ViewPager mPager;
	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = (Context) this;

		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_hotel_gallery);

		if (savedInstanceState != null && savedInstanceState.containsKey(Codes.SELECTED_IMAGE)) {
			mSelectedMedia = JSONUtils.parseJSONObjectFromBundle(savedInstanceState, Codes.SELECTED_IMAGE, Media.class);
		}
		else {
			mSelectedMedia = JSONUtils.parseJSONableFromIntent(this.getIntent(), Codes.SELECTED_IMAGE, Media.class);
		}

		mProperty = Db.getSelectedProperty();

		// setup the ViewPager
		HotelImagePagerAdapter pagerAdapter = new HotelImagePagerAdapter(getSupportFragmentManager());
		mPager = (ViewPager) findViewById(R.id.big_image_pager);
		mPager.setAdapter(pagerAdapter);

		if (!AndroidUtils.isTablet(mContext)) {
			mPager.setPageMargin(10);
		}

		if (AndroidUtils.isTablet(mContext)) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle(Html.fromHtml(getString(R.string.gallery_title_template, mProperty.getName())));
			actionBar.setDisplayHomeAsUpEnabled(true);
		}
		else if (AndroidUtils.isHoneycombVersionOrHigher()) {
			ActionBar actionBar = getActionBar();
			actionBar.hide();
		}

		mAdapter = new ImageAdapter();
		mAdapter.setMedia(mProperty.getMediaList());

		mHotelGallery = (Gallery) findViewById(R.id.hotel_gallery);
		mHotelGallery.setAdapter(mAdapter);
		mHotelGallery.setCallbackDuringFling(false);
		if (AndroidUtils.isHoneycombVersionOrHigher()) {
			mHotelGallery.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}

		int position = (mSelectedMedia == null) ? 0 : mAdapter.getPositionOfMedia(mSelectedMedia);
		mSelectedMedia = (mSelectedMedia == null) ? (Media) mAdapter.getItem(0) : mSelectedMedia;
		mHotelGallery.setSelection(position);

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
	}

	public Media getHotelMedia(int position) {
		return (Media) mAdapter.getItem(position);
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
				convertView = getLayoutInflater().inflate(R.layout.gallery_item, null);
				imageView = (ImageView) convertView.findViewById(R.id.image);
			}

			if (AndroidUtils.isTablet(mContext) && !ImageCache.loadImage(mMedia.get(position).getUrl(), imageView)) {
				imageView.setImageResource(R.drawable.ic_row_thumb_placeholder);
			}

			return convertView;
		}
	}

	private static final String IMAGE_POSITION = "POSITION";

	public class HotelImagePagerAdapter extends FragmentStatePagerAdapter {

		public HotelImagePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position) {
			return HotelImageFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return mHotelGallery.getCount();
		}
	}

	public static class HotelImageFragment extends Fragment {

		public static HotelImageFragment newInstance(int position) {
			HotelImageFragment imageFragment = new HotelImageFragment();
			Bundle args = new Bundle();
			args.putInt(IMAGE_POSITION, position);
			imageFragment.setArguments(args);
			return imageFragment;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.fragment_pager_hotel_image, container, false);
			int position = getArguments().getInt(IMAGE_POSITION);
			final ImageView imageView = (ImageView) view.findViewById(R.id.big_image_view);
			final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.hotel_image_progress_bar);
			Media hotelMedia = ((HotelGalleryActivity) getActivity()).getHotelMedia(position);
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

	}
}
