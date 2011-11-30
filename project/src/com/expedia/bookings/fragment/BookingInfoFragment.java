package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.BookingFragmentActivity;
import com.expedia.bookings.activity.BookingFragmentActivity.InstanceFragment;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.widget.RoomTypeFragmentHandler;

public class BookingInfoFragment extends Fragment implements EventHandler {

	private View mCompleteBookingInfoButton;
	private RoomTypeFragmentHandler mRoomTypeFragmentHandler;

	public static BookingInfoFragment newInstance() {
		BookingInfoFragment fragment = new BookingInfoFragment();
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		((BookingFragmentActivity) activity).mEventManager.registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_booking, container, false);

		mCompleteBookingInfoButton = view.findViewById(R.id.complete_booking_info_button);
		mCompleteBookingInfoButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				((BookingFragmentActivity) getActivity()).enterBookingInfo();
			}
		});

		mRoomTypeFragmentHandler = new RoomTypeFragmentHandler(getActivity(), view, getInstance().mProperty,
				getInstance().mSearchParams, getInstance().mRate);
		mRoomTypeFragmentHandler.onCreate(savedInstanceState);

		/*
		 * Configuring the policy cancellation section
		 */
		ConfirmationUtils.determineCancellationPolicy(getInstance().mRate, view);

		TextView contactView = (TextView) view.findViewById(R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);

		updateRoomDescription(view);
		configureTicket(view);
		mRoomTypeFragmentHandler.updateRoomDetails(getInstance().mRate, getInstance().mPropertyInfoResponse,
				getInstance().mPropertyInfoStatus);
		return view;
	}

	@Override
	public void onDetach() {
		((BookingFragmentActivity) getActivity()).mEventManager.unregisterEventHandler(this);
		super.onDetach();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mRoomTypeFragmentHandler.saveToBundle(outState);
	}

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {

		case BookingFragmentActivity.EVENT_RATE_SELECTED:
			if (mRoomTypeFragmentHandler != null) {
				configureTicket(getView());
				mRoomTypeFragmentHandler.updateRoomDetails(getInstance().mRate, getInstance().mPropertyInfoResponse,
						getInstance().mPropertyInfoStatus);
			}
			updateRoomDescription(getView());
			break;
		case BookingFragmentActivity.EVENT_PROPERTY_INFO_QUERY_STARTED:
			updateRoomDescription(getView());
			break;
		case BookingFragmentActivity.EVENT_PROPERTY_INFO_QUERY_COMPLETE:
			updateRoomDescription(getView());
			mRoomTypeFragmentHandler.onPropertyInfoDownloaded(getInstance().mPropertyInfoResponse);
			break;
		case BookingFragmentActivity.EVENT_PROPERTY_INFO_QUERY_ERROR:
			mRoomTypeFragmentHandler.showCheckInCheckoutDetails(null);
			updateRoomDescription(getView());
			break;
		}

	}

	private void updateRoomDescription(View view) {
		if (view == null) {
			return;
		}

		PropertyInfoResponse propertyInfoResponse = getInstance().mPropertyInfoResponse;
		Rate rate = getInstance().mRate;
		String roomTypeDescription = null;
		ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.room_description_progress_bar);
		TextView roomTypeDescriptionTitleTextView = (TextView) view.findViewById(R.id.room_type_description_title_view);
		roomTypeDescriptionTitleTextView.setText(rate.getRatePlanName());

		if (propertyInfoResponse == null) {
			if (getInstance().mPropertyInfoStatus != null) {
				roomTypeDescription = getInstance().mPropertyInfoStatus;
				progressBar.setVisibility(View.GONE);
			}
			else {
				progressBar.setVisibility(View.VISIBLE);
			}
		}
		else {
			progressBar.setVisibility(View.GONE);

			if (propertyInfoResponse.hasErrors()) {
				roomTypeDescription = propertyInfoResponse.getErrors().get(0).getPresentableMessage(getActivity());
			}
			else {
				roomTypeDescription = propertyInfoResponse.getPropertyInfo().getRoomLongDescription(rate);
			}
		}

		TextView roomTypeDescriptionTextView = (TextView) view.findViewById(R.id.room_type_description_text_view);
		roomTypeDescriptionTextView.setText(roomTypeDescription);
	}

	private void configureTicket(View receipt) {
		InstanceFragment instance = getInstance();
		ViewGroup detailsLayout = (ViewGroup) receipt.findViewById(R.id.details_layout);
		detailsLayout.removeAllViews();
		BookingReceiptUtils.configureTicket(getActivity(), receipt, instance.mProperty, instance.mSearchParams,
				instance.mRate, mRoomTypeFragmentHandler);
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public BookingFragmentActivity.InstanceFragment getInstance() {
		return ((BookingFragmentActivity) getActivity()).mInstance;
	}
}
