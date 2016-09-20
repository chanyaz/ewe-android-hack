package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.HotelMedia;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.widget.RecyclerGallery;
import com.mobiata.android.util.Ui;

public class HotelDetailsMiniGalleryFragment extends Fragment {

	private static final String INSTANCE_GALLERY_FLIPPING = "INSTANCE_GALLERY_FLIPPING";
	private static final String INSTANCE_GALLERY_POSITION = "INSTANCE_GALLERY_POSITION";

	public static final String ARG_FROM_LAUNCH = "ARG_FROM_LAUNCH";

	private RecyclerGallery.GalleryItemListener mListener;

	private RecyclerGallery mGallery;

	private boolean mGalleryFlipping = true;
	private int mGalleryPosition = 0;

	public static HotelDetailsMiniGalleryFragment newInstance(boolean fromLaunch) {
		HotelDetailsMiniGalleryFragment fragment = new HotelDetailsMiniGalleryFragment();

		Bundle args = new Bundle();
		args.putBoolean(ARG_FROM_LAUNCH, fromLaunch);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		mListener = Ui.findFragmentListener(this, RecyclerGallery.GalleryItemListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_mini_gallery, container, false);

		mGallery = (RecyclerGallery) view.findViewById(R.id.images_gallery);

		if (savedInstanceState != null) {
			mGalleryFlipping = savedInstanceState.getBoolean(INSTANCE_GALLERY_FLIPPING, true);
			mGalleryPosition = savedInstanceState.getInt(INSTANCE_GALLERY_POSITION, 0);
		}

		Property property = Db.getHotelSearch().getSelectedProperty();
		populateViews(property);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(INSTANCE_GALLERY_POSITION, mGallery.getSelectedItem());
	}

	public void populateViews(Property property) {
		final List<HotelMedia> hotelMedia = new ArrayList<HotelMedia>();

		if (property != null && property.getMediaCount() > 0) {
			hotelMedia.addAll(property.getMediaList());
		}

		if (hotelMedia.size() == 0) {
			return;
		}
		mGallery.setDataSource(hotelMedia);

		mGallery.setOnItemClickListener(mListener);

		if (mGalleryPosition > 0 && hotelMedia.size() > mGalleryPosition) {
			mGallery.scrollToPosition(mGalleryPosition);
		}
	}
}
