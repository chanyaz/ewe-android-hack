package com.expedia.bookings.fragment;

import java.util.List;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.StrUtils;
import com.mobiata.android.ImageCache;
import com.mobiata.android.json.JSONUtils;

public class HotelGalleryDialogFragment extends DialogFragment {

	private static final String SELECTED_MEDIA = "SELECTED_MEDIA";

	private Gallery mHotelGallery;
	private ImageView mBigImageView;
	private ImageAdapter mAdapter;
	private LayoutInflater mInflater;
	private Media mSelectedMedia;

	public static HotelGalleryDialogFragment newInstance(Media selectedMedia) {
		HotelGalleryDialogFragment dialog = new HotelGalleryDialogFragment();
		Bundle args = new Bundle();
		args.putString(SELECTED_MEDIA, selectedMedia.toJson().toString());
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		Property property = ((SearchResultsFragmentActivity) getActivity()).mInstance.mProperty;

		if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_MEDIA)) {
			mSelectedMedia = (Media) JSONUtils.parseJSONObjectFromBundle(savedInstanceState, SELECTED_MEDIA,
					Media.class);
		}
		else {
			mSelectedMedia = (Media) JSONUtils.parseJSONObjectFromBundle(getArguments(), SELECTED_MEDIA,
					Media.class);
		}

		View view = mInflater.inflate(R.layout.fragment_hotel_gallery, null);
		mHotelGallery = (Gallery) view.findViewById(R.id.hotel_gallery);
		mBigImageView = (ImageView) view.findViewById(R.id.big_image_view);

		mAdapter = new ImageAdapter();
		mAdapter.setMedia(StrUtils.getUniqueMediaList(property));
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

		Dialog dialog = new Dialog(getActivity(), R.style.Theme_Light_Fullscreen_Panel);
		dialog.requestWindowFeature(STYLE_NO_TITLE);
		dialog.setContentView(view);
		dialog.getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
		return dialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mSelectedMedia != null) {
			outState.putString(SELECTED_MEDIA, mSelectedMedia.toJson().toString());
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
				imageView = new ImageView(getActivity());
				imageView.setLayoutParams(new Gallery.LayoutParams(thumbnailDimensionDp, thumbnailDimensionDp));
				imageView.setScaleType(ScaleType.CENTER_CROP);
				imageView.setBackgroundResource(R.drawable.bg_gallery_item);
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
