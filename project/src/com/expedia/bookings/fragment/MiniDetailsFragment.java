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
import com.expedia.bookings.data.Property;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.HotelCollageHandler;
import com.expedia.bookings.widget.HotelCollageHandler.OnCollageImageClickedListener;

public class MiniDetailsFragment extends Fragment implements EventHandler {

	public static MiniDetailsFragment newInstance() {
		return new MiniDetailsFragment();
	}

	private TextView mNameTextView;
	private TextView mLocationTextView;
	private RatingBar mRatingBar;
	private Button mSeeDetailsButton;
	
	private HotelCollageHandler mCollageHandler;

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_mini_details, container, false);

		mNameTextView = (TextView) view.findViewById(R.id.name_text_view);
		mLocationTextView = (TextView) view.findViewById(R.id.location_text_view);
		mRatingBar = (RatingBar) view.findViewById(R.id.hotel_rating_bar);
		mSeeDetailsButton = (Button) view.findViewById(R.id.see_details_button);
		mCollageHandler = new HotelCollageHandler(view, mOnImageClickedListener);

		mSeeDetailsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((TabletActivity) getActivity()).moreDetailsForPropertySelected();
			}
		});
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateViews();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) getActivity()).registerEventHandler(this);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}
	
	//////////////////////////////////////////////////////////////////////////
	// Callbacks

	private OnCollageImageClickedListener mOnImageClickedListener = new OnCollageImageClickedListener() {
		
		@Override
		public void onImageClicked(String url) {
			((TabletActivity) getActivity()).moreDetailsForPropertySelected();
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Views

	private void updateViews() {
		updateViews(((TabletActivity) getActivity()).getPropertyToDisplay());
	}

	private void updateViews(Property property) {
		// don't update views if there is no
		// view attached.
		if (getView() != null && property != null) {
			mNameTextView.setText(property.getName());
			mLocationTextView.setText(StrUtils.formatAddress(property.getLocation()).replace("\n", ", "));
			mRatingBar.setRating((float) property.getHotelRating());
			mCollageHandler.updateCollage(property);
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_PROPERTY_SELECTED:
			updateViews((Property) data);
			break;
		}
	}
}
