package com.expedia.bookings.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.widget.MapImageView;
import com.mobiata.android.ImageCache;
import com.mobiata.android.Log;
import com.mobiata.android.services.GoogleServices;
import com.mobiata.android.services.GoogleServices.MapType;
import com.mobiata.android.util.Ui;

public class HotelDetailsMiniMapFragment extends Fragment {

	private MapImageView mStaticMapImageView;

	private HotelMiniMapFragmentListener mListener;

	public static HotelDetailsMiniMapFragment newInstance() {
		return new HotelDetailsMiniMapFragment();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (!(activity instanceof HotelMiniMapFragmentListener)) {
			throw new RuntimeException(
					"HotelDetailsMiniMapFragment Activity must implement HotelMiniMapFragmentListener!");
		}

		mListener = (HotelMiniMapFragmentListener) activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_hotel_details_mini_map, container, false);
		mStaticMapImageView = Ui.findView(view, R.id.mini_map);
		mStaticMapImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onMiniMapClicked();
			}
		});

		SearchParams searchParams = Db.getSearchParams();
		Property searchProperty = Db.getSelectedProperty();
		if (searchParams != null && searchProperty != null) {
			mStaticMapImageView.setCenterPoint(searchProperty.getLocation());

			// Fill in the POI / current location point appropriately
			switch (searchParams.getSearchType()) {
			case POI:
			case ADDRESS:
			case MY_LOCATION:
				mStaticMapImageView.setPoiPoint(searchParams.getSearchLatitude(), searchParams.getSearchLongitude());
				break;
			}
		}

		return view;
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface HotelMiniMapFragmentListener {
		public void onMiniMapClicked();
	}
}
