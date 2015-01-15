package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.widget.RecyclerGallery;
import com.mobiata.android.util.Ui;

public class HotelDetailsMiniGalleryFragment extends Fragment {

	private static final String INSTANCE_GALLERY_FLIPPING = "INSTANCE_GALLERY_FLIPPING";
	private static final String INSTANCE_GALLERY_POSITION = "INSTANCE_GALLERY_POSITION";

	public static final String ARG_FROM_LAUNCH = "ARG_FROM_LAUNCH";

	private RecyclerGallery.GalleryItemClickListner mListener;

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
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, RecyclerGallery.GalleryItemClickListner.class);
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
		outState.putBoolean(INSTANCE_GALLERY_FLIPPING, mGallery.isFlipping());
		outState.putInt(INSTANCE_GALLERY_POSITION, mGallery.getSelectedItem());
	}

	public void populateViews(Property property) {
		final List<Media> media = new ArrayList<Media>();

		if (property != null && property.getMediaCount() > 0) {
			media.addAll(property.getMediaList());
		}

		if (media.size() == 0) {
			return;
		}
		mGallery.setDataSource(media);

		mGallery.setOnItemClickListener(mListener);

		if (mGalleryPosition > 0 && media.size() > mGalleryPosition) {
			mGallery.scrollToPosition(mGalleryPosition);
		}

		if (mGalleryFlipping) {
			mGallery.startFlipping();
		}
	}


}
