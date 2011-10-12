package com.expedia.bookings.fragment;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;

public class HotelGalleryDialogFragment extends DialogFragment {

	private static final String SELECTED_IMAGE_URL = "SELECTED_IMAGE_URL";

	private Gallery mHotelGallery;
	private ImageView mBigImageView;
	private ImageAdapter mAdapter;
	private LayoutInflater mInflater;
	private String mSelectedImageUrl;

	public static HotelGalleryDialogFragment newInstance(String selectedImageUrl) {
		HotelGalleryDialogFragment dialog = new HotelGalleryDialogFragment();
		Bundle args = new Bundle();
		args.putString(SELECTED_IMAGE_URL, selectedImageUrl);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_IMAGE_URL)) {
			mSelectedImageUrl = savedInstanceState.getString(SELECTED_IMAGE_URL);
		}
		else {
			mSelectedImageUrl = getArguments().getString("SELECTED_IMAGE_URL");
		}

		View view = mInflater.inflate(R.layout.fragment_hotel_gallery, null);
		mHotelGallery = (Gallery) view.findViewById(R.id.hotel_gallery);
		mBigImageView = (ImageView) view.findViewById(R.id.big_image_view);

		mAdapter = new ImageAdapter();
		mAdapter.setUrls(StrUtils.getImageUrls(((TabletActivity) getActivity()).getPropertyToDisplay()));
		mHotelGallery.setAdapter(mAdapter);

		mHotelGallery.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> l, View arg1, int position, long id) {
				mSelectedImageUrl = (String) mAdapter.getItem(position);
				ImageCache.loadImage(mSelectedImageUrl, mBigImageView);
			}
		});
		
		mHotelGallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
				mSelectedImageUrl = (String) mAdapter.getItem(position);
				ImageCache.loadImage(mSelectedImageUrl, mBigImageView);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				mSelectedImageUrl = (String) mAdapter.getItem(0);
				ImageCache.loadImage(mSelectedImageUrl, mBigImageView);
			}
		});

		int position = (mSelectedImageUrl == null) ? 0 : mAdapter.getPositionOfImage(mSelectedImageUrl);
		mSelectedImageUrl = (mSelectedImageUrl == null) ? (String) mAdapter.getItem(0) : mSelectedImageUrl;

		mHotelGallery.setSelection(position);

		Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view);
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mSelectedImageUrl != null) {
			outState.putString(SELECTED_IMAGE_URL, mSelectedImageUrl);
		}
	}

	private class ImageAdapter extends BaseAdapter {
		private List<String> mUrls;

		public void setUrls(List<String> urls) {
			mUrls = urls;
			notifyDataSetChanged();
		}

		public int getPositionOfImage(String url) {
			return mUrls.indexOf(url);
		}

		@Override
		public int getCount() {
			return mUrls.size();
		}

		@Override
		public Object getItem(int position) {
			return mUrls.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = (ImageView) convertView;
			if (convertView == null) {
				imageView = new ImageView(getActivity());
				imageView.setLayoutParams(new Gallery.LayoutParams(150, 100));
				imageView.setScaleType(ScaleType.CENTER_CROP);
				convertView = imageView;
			}

			boolean imageSet = ImageCache.loadImage((String) mUrls.get(position), imageView);
			if (!imageSet) {
				imageView.setImageResource(R.drawable.ic_row_thumb_placeholder);
			}

			return convertView;
		}

	}

}
