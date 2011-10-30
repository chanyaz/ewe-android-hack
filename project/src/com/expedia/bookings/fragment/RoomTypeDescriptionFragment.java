package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.EventManager.EventHandler;

public class RoomTypeDescriptionFragment extends Fragment implements EventHandler {
	public static RoomTypeDescriptionFragment newInstance() {
		RoomTypeDescriptionFragment fragment = new RoomTypeDescriptionFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		((TabletActivity) activity).registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_room_type_description, container, false);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		updateViews();
	}

	protected void updateViews() {
		if (getView() == null) {
			return;
		}

		PropertyInfoResponse propertyInfoResponse = ((TabletActivity) getActivity()).getInfoForProperty();
		Rate rate = ((TabletActivity) getActivity()).getRoomRateForBooking();
		String roomTypeDescription = null;
		if (propertyInfoResponse == null) {
			roomTypeDescription = ((TabletActivity) getActivity()).getPropertyInfoQueryStatus();
		}
		else {
			if (propertyInfoResponse.hasErrors()) {
				roomTypeDescription = propertyInfoResponse.getErrors().get(0).getPresentableMessage(getActivity());
			}
			else {
				roomTypeDescription = propertyInfoResponse.getPropertyInfo().getRoomLongDescription(rate);
			}
		}

		TextView roomTypeDescriptionTitleTextView = (TextView) getView().findViewById(R.id.room_type_description_title_view);
		roomTypeDescriptionTitleTextView.setText(rate.getRatePlanName());
		TextView roomTypeDescriptionTextView = (TextView) getView().findViewById(R.id.room_type_description_text_view);
		roomTypeDescriptionTextView.setText(roomTypeDescription);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_RATE_SELECTED:
		case TabletActivity.EVENT_PROPERTY_INFO_QUERY_STARTED:
		case TabletActivity.EVENT_PROPERTY_INFO_QUERY_COMPLETE:
		case TabletActivity.EVENT_PROPERTY_INFO_QUERY_ERROR:
			updateViews();
			break;
		}
	}

}
