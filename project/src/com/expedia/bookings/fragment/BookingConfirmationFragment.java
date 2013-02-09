package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.widget.MapImageView;
import com.mobiata.android.bitmaps.UrlBitmapDrawable;

public class BookingConfirmationFragment extends Fragment {

	private BookingConfirmationFragmentListener mListener;

	public static BookingConfirmationFragment newInstance() {
		BookingConfirmationFragment fragment = new BookingConfirmationFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof BookingConfirmationFragmentListener)) {
			throw new RuntimeException("BookingConfirmationFragment Activity "
					+ "must implement BookingConfirmationFragmentListener!");
		}

		mListener = (BookingConfirmationFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_confirmation, container, false);
		Property property = Db.getSelectedProperty();
		setupMap(view, property);

		View shareBookingButton = view.findViewById(R.id.share_booking_info_button);
		shareBookingButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mListener.onShareBooking();
			}
		});

		View showOnMapButton = view.findViewById(R.id.show_on_map_button);
		showOnMapButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mListener.onShowOnMap();
			}
		});

		View nextSearchButton = view.findViewById(R.id.start_new_search_button);
		nextSearchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mListener.onNewSearch();
			}
		});

		return view;
	}

	private void setupMap(ViewGroup container, Property property) {
		ViewGroup mapViewLayout = (ViewGroup) container.findViewById(R.id.map_layout);
		if (mapViewLayout == null) {
			return;
		}

		MapImageView miniMap = (MapImageView) mapViewLayout.findViewById(R.id.mini_map);
		miniMap.setCenterPoint(property.getLocation());

		// Thumbnail in the map
		ImageView thumbnail = (ImageView) container.findViewById(R.id.thumbnail_image_view);
		if (thumbnail != null) {
			if (property.getThumbnail() != null) {
				UrlBitmapDrawable.loadImageView(property.getThumbnail().getUrl(), thumbnail);
			}
			else {
				thumbnail.setVisibility(View.GONE);
			}

			// anti-aliasing is not supported on the hardware
			// rendering pipline yet, so rendering the image
			// on a software layer to prevent the jaggies.
			LayoutUtils.sayNoToJaggies(thumbnail);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface BookingConfirmationFragmentListener {
		public void onNewSearch();

		public void onShareBooking();

		public void onShowOnMap();
	}
}
