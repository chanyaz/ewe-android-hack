package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Media;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.DbPropertyHelper;
import com.expedia.bookings.widget.AdapterView;
import com.expedia.bookings.widget.AdapterView.OnItemClickListener;
import com.expedia.bookings.widget.AdapterView.OnItemSelectedListener;
import com.expedia.bookings.widget.Gallery;

public class HotelDetailsMiniGalleryFragment extends Fragment {

	private static final String INSTANCE_GALLERY_FLIPPING = "INSTANCE_GALLERY_FLIPPING";
	private static final String INSTANCE_GALLERY_POSITION = "INSTANCE_GALLERY_POSITION";

	public static final String ARG_FROM_LAUNCH = "ARG_FROM_LAUNCH";

	private static final int MAX_IMAGES_LOADED = 5;

	private HotelMiniGalleryFragmentListener mListener;

	private Gallery mGallery;
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

		if (!(activity instanceof HotelMiniGalleryFragmentListener)) {
			throw new RuntimeException(
					"HotelDetailsMiniGalleryFragment Activity must implement HotelMiniGalleryFragmentListener!");
		}

		mListener = (HotelMiniGalleryFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_mini_gallery, container, false);
		mGallery = (Gallery) view.findViewById(R.id.images_gallery);

		if (savedInstanceState != null) {
			mGalleryFlipping = savedInstanceState.getBoolean(INSTANCE_GALLERY_FLIPPING, true);
			mGalleryPosition = savedInstanceState.getInt(INSTANCE_GALLERY_POSITION, 0);
		}

		populateViews();

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_GALLERY_FLIPPING, mGallery.isFlipping());
		outState.putInt(INSTANCE_GALLERY_POSITION, mGallery.getSelectedItemPosition());
	}

	public void populateViews() {
		final List<Media> media = new ArrayList<Media>();

		Property property = DbPropertyHelper.getBestMediaProperty();
		if (property != null && property.getMediaCount() > 0) {
			media.addAll(property.getMediaList());
		}

		if (media.size() == 0) {
			return;
		}
		mGallery.setMedia(media);

		mGallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Property selected = Db.getSelectedProperty();
				if (selected != null) {
					mListener.onMiniGalleryItemClicked(selected, parent.getSelectedItem());
				}
			}
		});

		mGallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// Pre-load images around the currently selected image, until we have MAX_IMAGES_LOADED
				// loading.
				int left = position;
				int right = position;
				int loaded = 1;
				int len = media.size();
				boolean hasMore = true;
				Context context = getActivity();
				while (loaded < MAX_IMAGES_LOADED && hasMore) {
					hasMore = false;
					if (left > 0) {
						left--;
						media.get(left).preloadHighResImage(context, null);
						loaded++;
						hasMore = true;
					}
					if (loaded == MAX_IMAGES_LOADED) {
						break;
					}
					if (right < len - 1) {
						right++;
						media.get(right).preloadHighResImage(context, null);
						loaded++;
						hasMore = true;
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Do nothing
			}
		});

		if (mGalleryPosition > 0 && media.size() > mGalleryPosition) {
			mGallery.setSelection(mGalleryPosition);
		}

		if (mGalleryFlipping) {
			mGallery.startFlipping();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelMiniGalleryFragmentListener {
		public void onMiniGalleryItemClicked(Property property, Object item);
	}
}
