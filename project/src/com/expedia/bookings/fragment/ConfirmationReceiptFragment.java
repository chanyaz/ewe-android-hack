package com.expedia.bookings.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ConfirmationFragmentActivity;
import com.expedia.bookings.activity.ConfirmationFragmentActivity.InstanceFragment;
import com.expedia.bookings.utils.BookingReceiptUtils;
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.widget.RoomTypeWidget;

public class ConfirmationReceiptFragment extends Fragment {

	private RoomTypeWidget mRoomTypeWidget;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View receipt = inflater.inflate(R.layout.fragment_confirmation_receipt, container, false);

		InstanceFragment instance = getInstance();

		mRoomTypeWidget = new RoomTypeWidget(getActivity(), false);
		mRoomTypeWidget.updateRate(instance.mRate);

		/*
		 * Configuring the policy cancellation section
		 */
		ConfirmationUtils.determineCancellationPolicy(instance.mRate, receipt);

		TextView contactView = (TextView) receipt.findViewById(R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);

		configureTicket(receipt);

		return receipt;
	}

	//////////////////////////////////////////////////////////////////////////
	// Views stuff

	private void configureTicket(View receipt) {
		InstanceFragment instance = getInstance();

		ViewGroup detailsLayout = (ViewGroup) receipt.findViewById(R.id.details_layout);
		detailsLayout.removeAllViews();
		BookingReceiptUtils.configureTicket(getActivity(), receipt, instance.mProperty, instance.mSearchParams,
				instance.mRate, mRoomTypeWidget, instance.mBookingResponse, instance.mBillingInfo);
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public ConfirmationFragmentActivity.InstanceFragment getInstance() {
		return ((ConfirmationFragmentActivity) getActivity()).mInstance;
	}
}
