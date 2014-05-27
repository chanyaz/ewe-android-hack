package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.interfaces.IResultsHotelGalleryBackClickedListener;
import com.expedia.bookings.otto.Events;
import com.expedia.bookings.utils.Ui;
import com.squareup.otto.Subscribe;

import com.mobiata.android.Log;

public class ResultsHotelGalleryFragment extends Fragment {

	public static ResultsHotelGalleryFragment newInstance() {
		ResultsHotelGalleryFragment frag = new ResultsHotelGalleryFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private TextView mDoneText;
	private TextView mHotelText;
	private ViewPager mPager;

	private MediaPagerAdapter mAdapter;
	private IResultsHotelGalleryBackClickedListener mHotelGalleryBackClickedListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mHotelGalleryBackClickedListener = Ui.findFragmentListener(this, IResultsHotelGalleryBackClickedListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotel_gallery, null);
		mDoneText = Ui.findView(mRootC, R.id.done_button);
		mHotelText = Ui.findView(mRootC, R.id.photos_for_hotel_text);
		mPager = Ui.findView(mRootC, R.id.pager);

		mDoneText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHotelGalleryBackClickedListener.onHotelGalleryBackClicked();
			}
		});

		mAdapter = new MediaPagerAdapter();
		mPager.setAdapter(mAdapter);
		return mRootC;
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
		}
	}

	public void onHotelSelected() {
		bind(Db.getHotelSearch().getSelectedProperty());
	}

	private static class MediaPagerAdapter extends PagerAdapter {
		private List<Media> mMedia;

		public void replaceWith(List<Media> media) {
			Log.stackTrace(5, "replaceWith");
			mMedia = media;
			notifyDataSetChanged();
		}

		@Override
		public int getItemPosition(Object object) {
			return PagerAdapter.POSITION_NONE;
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {
			LayoutInflater inflater = LayoutInflater.from(collection.getContext());
			View root = inflater.inflate(R.layout.snippet_tablet_hotel_gallery_item, null);

			Media media = mMedia.get(position);
			ImageView image = (ImageView) root;
			media.loadHighResImage(image, null);

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
