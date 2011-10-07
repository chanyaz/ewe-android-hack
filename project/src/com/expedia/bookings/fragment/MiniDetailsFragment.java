package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.activity.TabletActivity.EventHandler;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.utils.StrUtils;

public class MiniDetailsFragment extends Fragment implements EventHandler {

	public static MiniDetailsFragment newInstance() {
		return new MiniDetailsFragment();
	}

	private TextView mNameTextView;
	private TextView mLocationTextView;
	private RatingBar mRatingBar;
	private Button mSeeDetailsButton;

	private Property mProperty;

	private boolean mInitialized = false;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		((TabletActivity) activity).registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mini_details, container, false);

		mNameTextView = (TextView) view.findViewById(R.id.name_text_view);
		mLocationTextView = (TextView) view.findViewById(R.id.location_text_view);
		mRatingBar = (RatingBar) view.findViewById(R.id.hotel_rating_bar);
		mSeeDetailsButton = (Button) view.findViewById(R.id.see_details_button);
		
		mSeeDetailsButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				((TabletActivity) getActivity()).moreDetailsForPropertySelected(mProperty); 
			}
		});

		mInitialized = true;

		updateViews();

		return view;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	//////////////////////////////////////////////////////////////////////////
	// Views

	public void updateViews() {
		if (mInitialized) {
			mNameTextView.setText(mProperty.getName());
			mLocationTextView.setText(StrUtils.formatAddress(mProperty.getLocation()).replace("\n", ", "));
			mRatingBar.setRating((float) mProperty.getHotelRating());
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			mProperty = (Property) data;
			updateViews();
			break;
		}
	}
}
