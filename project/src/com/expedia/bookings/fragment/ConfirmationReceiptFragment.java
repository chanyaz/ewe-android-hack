package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.BookingFragmentActivity;
import com.expedia.bookings.activity.ConfirmationFragmentActivity;
import com.expedia.bookings.activity.ConfirmationFragmentActivity.InstanceFragment;
import com.expedia.bookings.fragment.EventManager.EventHandler;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.widget.RoomTypeFragmentHandler;

public class ConfirmationReceiptFragment extends Fragment implements EventHandler {

	private RoomTypeFragmentHandler mRoomTypeFragmentHandler;

	public static ConfirmationReceiptFragment newInstance() {
		ConfirmationReceiptFragment fragment = new ConfirmationReceiptFragment();
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
		((ConfirmationFragmentActivity) activity).mEventManager.registerEventHandler(this);
	}
	
	@Override
	public void onDetach() {
		((ConfirmationFragmentActivity) getActivity()).mEventManager.unregisterEventHandler(this);
		super.onDetach();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View receipt = inflater.inflate(R.layout.fragment_confirmation_receipt, container, false);

		InstanceFragment instance = getInstance();

		mRoomTypeFragmentHandler = new RoomTypeFragmentHandler(getActivity(), receipt, instance.mProperty,
				instance.mSearchParams, instance.mRate);

		mRoomTypeFragmentHandler.onCreate(savedInstanceState);

		/*
		 * Configuring the policy cancellation section
		 */
		ConfirmationUtils.determineCancellationPolicy(instance.mRate, receipt);

		TextView contactView = (TextView) receipt.findViewById(R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);

		configureTicket(receipt);
		mRoomTypeFragmentHandler.updateRoomDetails(instance.mRate, instance.mPropertyInfoResponse,
				instance.mPropertyInfoStatus);

		return receipt;
	}

	@Override
	public void onResume() {
		super.onResume();
		configureTicket(getView());
		mRoomTypeFragmentHandler.updateRoomDetails(getInstance().mRate, getInstance().mPropertyInfoResponse,
				getInstance().mPropertyInfoStatus);
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
			break;
		case BookingFragmentActivity.EVENT_PROPERTY_INFO_QUERY_COMPLETE:
			mRoomTypeFragmentHandler.onPropertyInfoDownloaded(getInstance().mPropertyInfoResponse);
			break;
		case BookingFragmentActivity.EVENT_PROPERTY_INFO_QUERY_ERROR:
			mRoomTypeFragmentHandler.showDetails(getInstance().mPropertyInfoStatus);
			mRoomTypeFragmentHandler.showCheckInCheckoutDetails(null);
			break;
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Views stuff

	private void configureTicket(View receipt) {
		InstanceFragment instance = getInstance();

		ViewGroup detailsLayout = (ViewGroup) receipt.findViewById(R.id.details_layout);
		detailsLayout.removeAllViews();
		BookingReceiptUtils.configureTicket(getActivity(), receipt, instance.mProperty, instance.mSearchParams,
				instance.mRate, mRoomTypeFragmentHandler, instance.mBookingResponse, instance.mBillingInfo);
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public ConfirmationFragmentActivity.InstanceFragment getInstance() {
		return ((ConfirmationFragmentActivity) getActivity()).mInstance;
	}
}
