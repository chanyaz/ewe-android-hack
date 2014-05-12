package com.expedia.bookings.activity;

import android.app.ActionBar;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Gallery;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.HotelImageFragment;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.widget.ImageAdapter;
import com.mobiata.android.Log;
import com.mobiata.android.json.JSONUtils;
import com.mobiata.android.util.AndroidUtils;

public class HotelGalleryActivity extends FragmentActivity {

	private Gallery mGallery;
	private ImageAdapter mGalleryAdapter;
	private Property mProperty;
	private Media mSelectedMedia;
	private ViewPager mViewPager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// #13365: If the Db expired, finish out of this activity
		if (Db.getHotelSearch().getSelectedProperty() == null) {
			Log.i("Detected expired DB, finishing activity.");
			finish();
			return;
		}

		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		setContentView(R.layout.activity_hotel_gallery);

		if (savedInstanceState != null && savedInstanceState.containsKey(Codes.SELECTED_IMAGE)) {
			mSelectedMedia = JSONUtils.parseJSONObjectFromBundle(savedInstanceState, Codes.SELECTED_IMAGE, Media.class);
		}
		else {
			mSelectedMedia = JSONUtils.parseJSONableFromIntent(this.getIntent(), Codes.SELECTED_IMAGE, Media.class);
		}

		mProperty = Db.getHotelSearch().getSelectedProperty();

		// ViewPager

		HotelImagePagerAdapter pagerAdapter = new HotelImagePagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.big_image_pager);
		mViewPager.setAdapter(pagerAdapter);

		if (!ExpediaBookingApp.useTabletInterface(this)) {
			mViewPager.setPageMargin(10);
		}

		// Gallery

		mGalleryAdapter = new ImageAdapter(this);
		mGalleryAdapter.setMedia(mProperty.getMediaList());

		mGallery = (Gallery) findViewById(R.id.hotel_gallery);
		mGallery.setAdapter(mGalleryAdapter);
		mGallery.setCallbackDuringFling(false);
		setGalleryVisibility();

		if (ExpediaBookingApp.useTabletInterface(this)) {
			ActionBar actionBar = getActionBar();
			actionBar.setTitle(Html.fromHtml(getString(R.string.gallery_title_template, mProperty.getName())));
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayUseLogoEnabled(false);
		}
		else if (AndroidUtils.isHoneycombVersionOrHigher()) {
			ActionBar actionBar = getActionBar();
			actionBar.hide();
		}

		int position = (mSelectedMedia == null) ? 0 : mGalleryAdapter.getPositionOfMedia(mSelectedMedia);
		mSelectedMedia = (mSelectedMedia == null) ? (Media) mGalleryAdapter.getItem(0) : mSelectedMedia;
		mGallery.setSelection(position);

		// Event listeners

		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (mSelectedMedia != mGalleryAdapter.getItem(position)) {
					mGallery.setSelection(position);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});

		mGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> l, View imageView, int position, long id) {
				mSelectedMedia = (Media) mGalleryAdapter.getItem(position);
				mViewPager.setCurrentItem(position);
			}
		});

		mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				mSelectedMedia = (Media) mGalleryAdapter.getItem(position);
				mViewPager.setCurrentItem(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mSelectedMedia = (Media) mGalleryAdapter.getItem(0);
				mViewPager.setCurrentItem(0);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		OmnitureTracking.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		OmnitureTracking.onPause();
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

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setGalleryVisibility() {
		if (AndroidUtils.isHoneycombVersionOrHigher()) {
			mGallery.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}

	public class HotelImagePagerAdapter extends FragmentStatePagerAdapter {

		public HotelImagePagerAdapter(android.support.v4.app.FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int position) {
			return HotelImageFragment.newInstance(mProperty.getMedia(position));
		}

		@Override
		public int getCount() {
			return mProperty.getMediaCount();
		}
	}

}
