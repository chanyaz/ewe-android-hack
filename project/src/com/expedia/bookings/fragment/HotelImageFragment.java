package com.expedia.bookings.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.expedia.bookings.R;
import com.expedia.bookings.bitmaps.L2ImageCache;
import com.expedia.bookings.data.Media;
import com.mobiata.android.json.JSONUtils;

public class HotelImageFragment extends Fragment {

	private static final String MEDIA = "MEDIA";

	public static HotelImageFragment newInstance(Media media) {
		HotelImageFragment imageFragment = new HotelImageFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, MEDIA, media);
		imageFragment.setArguments(args);
		return imageFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pager_hotel_image, container, false);
		final ImageView imageView = (ImageView) view.findViewById(R.id.big_image_view);
		final ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.hotel_image_progress_bar);

		Media hotelMedia = JSONUtils.getJSONable(getArguments(), MEDIA, Media.class);
		hotelMedia.loadHighResImage(imageView, new L2ImageCache.OnBitmapLoaded() {

			@Override
			public void onBitmapLoaded(String url, Bitmap bitmap) {
				progressBar.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onBitmapLoadFailed(String url) {
				progressBar.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
			}
		});

		return view;
	}

}
