package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.expedia.bookings.widget.RoomTypeFragmentHandler;

public class BookingReceiptFragment extends Fragment implements EventHandler {

	private RoomTypeFragmentHandler mRoomTypeFragmentHandler;

	public static BookingReceiptFragment newInstance() {
		BookingReceiptFragment fragment = new BookingReceiptFragment();
		return fragment;
	}

	public static BookingReceiptFragment newInstance(boolean includeConfirmationInfo) {
		BookingReceiptFragment fragment = new BookingReceiptFragment();
		Bundle arguments = new Bundle();
		arguments.putBoolean(Codes.INCLUDE_CONFIRMATION_INFO, includeConfirmationInfo);
		fragment.setArguments(arguments);
		return fragment;
	}

	//////////////////////////////////////////////////////////////////////////
	// Lifecycle methods

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (mRoomTypeFragmentHandler != null) {
			mRoomTypeFragmentHandler.onAttach();
		}
		((TabletActivity) activity).registerEventHandler(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View receipt = inflater.inflate(R.layout.fragment_receipt, container, false);
		Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		SearchParams searchParams = ((TabletActivity) getActivity()).getSearchParams();
		Rate rate = ((TabletActivity) getActivity()).getRoomRateForBooking();

		mRoomTypeFragmentHandler = new RoomTypeFragmentHandler(((TabletActivity) getActivity()), receipt, property,
				searchParams, rate);

		mRoomTypeFragmentHandler.onCreate(savedInstanceState);
		return receipt;
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mRoomTypeFragmentHandler.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		configureTicket(getView());
		mRoomTypeFragmentHandler.updateRoomDetails(((TabletActivity) getActivity()).getRoomRateForBooking());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mRoomTypeFragmentHandler.saveToBundle(outState);
	}

	//////////////////////////////////////////////////////////////////////////
	// EventHandler implementation

	@Override
	public void handleEvent(int eventCode, Object data) {
		switch (eventCode) {
		case TabletActivity.EVENT_RATE_SELECTED:
			if (mRoomTypeFragmentHandler != null) {
				configureTicket(getView());
				mRoomTypeFragmentHandler.updateRoomDetails(((TabletActivity) getActivity()).getRoomRateForBooking());
			}
			break;
		}
	}

	private void configureTicket(View receipt) {
		Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		SearchParams searchParams = ((TabletActivity) getActivity()).getSearchParams();
		Rate rate = ((TabletActivity) getActivity()).getRoomRateForBooking();
		BookingResponse bookingResponse = ((TabletActivity) getActivity()).getBookingResponse();
		BillingInfo billingInfo = ((TabletActivity) getActivity()).getBillingInfo();

		ViewGroup detailsLayout = (ViewGroup) receipt.findViewById(R.id.details_layout);
		detailsLayout.removeAllViews();
		if (getArguments() != null && getArguments().getBoolean(Codes.INCLUDE_CONFIRMATION_INFO, false)) {
			BookingReceiptUtils.configureTicket(getActivity(), receipt, property, searchParams, rate,
					mRoomTypeFragmentHandler, bookingResponse, billingInfo);
		}
		else {
			BookingReceiptUtils.configureTicket(getActivity(), receipt, property, searchParams, rate,
					mRoomTypeFragmentHandler);
		}

	}
}
