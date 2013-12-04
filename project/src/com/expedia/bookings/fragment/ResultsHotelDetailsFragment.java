package com.expedia.bookings.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.interfaces.IAddToTripListener;
import com.expedia.bookings.utils.LayoutUtils;
import com.expedia.bookings.utils.ScreenPositionUtils;
import com.expedia.bookings.widget.ScrollView;
import com.mobiata.android.Log;
import com.mobiata.android.util.Ui;

/**
 * ResultsFlightFiltersFragment: The filters fragment designed for tablet results 2013
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ResultsHotelDetailsFragment extends Fragment {

	public static ResultsHotelDetailsFragment newInstance() {
		ResultsHotelDetailsFragment frag = new ResultsHotelDetailsFragment();
		return frag;
	}

	private ViewGroup mRootC;
	private ViewGroup mHotelHeaderContainer;
	private Button mAddToTripButton;

	private IAddToTripListener mAddToTripListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mAddToTripListener = Ui.findFragmentListener(this, IAddToTripListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_tablet_hotel_details, null);
		mHotelHeaderContainer = Ui.findView(mRootC, R.id.hotel_header_container);
		mAddToTripButton = Ui.findView(mRootC, R.id.button_add_to_trip);

		mAddToTripButton.setPivotY(0f);
		mAddToTripButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mAddToTripListener.beginAddToTrip(getSelectedData(), getDestinationRect(), 0);
			}

		});

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		Db.getHotelSearch().getSelectedProperty()
		setupViews(mRootC);
	}

	private void setupViews(View view) {
		setupAmenities(view, Db.getHotelSearch().getSelectedProperty());
	}

	public void setTransitionToAddTripPercentage(float percentage) {
		//TODO
		//		if (mAddToTripButton != null) {
		//			mAddToTripButton.setScaleY(1f - percentage);
		//		}
	}

	public void setTransitionToAddTripHardwareLayer(int layerType) {
		//TODO
		//		if (mAddToTripButton != null) {
		//			mAddToTripButton.setLayerType(layerType, null);
		//		}
	}

	public Object getSelectedData() {
		return "SOME DATA";
	}

	public Rect getDestinationRect() {
		return ScreenPositionUtils.getGlobalScreenPosition(mHotelHeaderContainer);
	}

	private void setupAmenities(View view, Property property) {
		if (property == null) {
			return;
		}

		// Disable some aspects of the horizontal scrollview so it looks pretty
		HorizontalScrollView amenitiesScrollView = (HorizontalScrollView) view.findViewById(R.id.amenities_scroll_view);
		amenitiesScrollView.setHorizontalScrollBarEnabled(false);
		amenitiesScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);

		ViewGroup amenitiesContainer = (ViewGroup) view.findViewById(R.id.amenities_table_row);
		amenitiesContainer.removeAllViews();
		LayoutUtils.addAmenities(getActivity(), property, amenitiesContainer);

		// Hide the text that indicated no amenities because there are amenities
		view.findViewById(R.id.amenities_none_text).setVisibility(View.GONE);
		if (property.hasAmenities()) {
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.VISIBLE);
		}
		else {
			view.findViewById(R.id.amenities_scroll_view).setVisibility(View.GONE);
		}

	}
}
