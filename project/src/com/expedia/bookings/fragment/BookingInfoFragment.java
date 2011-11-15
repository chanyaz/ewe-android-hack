package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.data.BillingInfo;
import com.expedia.bookings.data.BookingResponse;
import com.expedia.bookings.data.Codes;
import com.expedia.bookings.data.Property;
import com.expedia.bookings.data.PropertyInfoResponse;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.SearchParams;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.widget.RoomTypeFragmentHandler;

public class BookingInfoFragment extends Fragment implements EventHandler {

	private View mCompleteBookingInfoButton;
	private RoomTypeFragmentHandler mRoomTypeFragmentHandler;
	private BitmapDrawable mListShadowDrawable;

	public static BookingInfoFragment newInstance() {
		BookingInfoFragment fragment = new BookingInfoFragment();
		return fragment;
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
		Property property = ((TabletActivity) getActivity()).getPropertyToDisplay();
		SearchParams searchParams = ((TabletActivity) getActivity()).getSearchParams();
		Rate rate = ((TabletActivity) getActivity()).getRoomRateForBooking();
		
		mListShadowDrawable = new BitmapDrawable(BitmapFactory.decodeResource(getResources(),
				R.drawable.list_stroke_shadow));
		mListShadowDrawable.setTileModeY(TileMode.REPEAT);

		View view = inflater.inflate(R.layout.fragment_booking, container, false);
		
		view.findViewById(R.id.availability_list_shadow).setBackgroundDrawable(mListShadowDrawable);
		mCompleteBookingInfoButton = view.findViewById(R.id.complete_booking_info_button);
		mCompleteBookingInfoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((TabletActivity) getActivity()).completeBookingInfo();
			}
		});

		mRoomTypeFragmentHandler = new RoomTypeFragmentHandler(((TabletActivity) getActivity()), view, property,
				searchParams, rate);
		mRoomTypeFragmentHandler.onCreate(savedInstanceState);

		/*
		 * Configuring the policy cancellation section
		 */
		ConfirmationUtils.determineCancellationPolicy(rate, view);

		TextView contactView = (TextView) view.findViewById(R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);
		
		updateRoomDescription(view);
		configureTicket(view);
		mRoomTypeFragmentHandler.updateRoomDetails(((TabletActivity) getActivity()).getRoomRateForBooking());
		return view;
	}

	@Override
	public void onDetach() {
		mRoomTypeFragmentHandler.onDetach();
		((TabletActivity) getActivity()).unregisterEventHandler(this);
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

		case TabletActivity.EVENT_RATE_SELECTED:
			if (mRoomTypeFragmentHandler != null) {
				configureTicket(getView());
				mRoomTypeFragmentHandler.updateRoomDetails(((TabletActivity) getActivity()).getRoomRateForBooking());
			}
		case TabletActivity.EVENT_PROPERTY_INFO_QUERY_STARTED:
		case TabletActivity.EVENT_PROPERTY_INFO_QUERY_COMPLETE:
		case TabletActivity.EVENT_PROPERTY_INFO_QUERY_ERROR:
			updateRoomDescription(getView());
			break;
		}

	}

	private void updateRoomDescription(View view) {
		if (view == null) {
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

		TextView roomTypeDescriptionTitleTextView = (TextView) view.findViewById(
				R.id.room_type_description_title_view);
		roomTypeDescriptionTitleTextView.setText(rate.getRatePlanName());
		TextView roomTypeDescriptionTextView = (TextView) view.findViewById(R.id.room_type_description_text_view);
		roomTypeDescriptionTextView.setText(roomTypeDescription);
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
