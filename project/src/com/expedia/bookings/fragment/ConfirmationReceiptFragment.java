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
import com.expedia.bookings.utils.ConfirmationUtils;
import com.expedia.bookings.widget.ReceiptWidget;

public class ConfirmationReceiptFragment extends Fragment {

	private ReceiptWidget mReceiptWidget;

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

		mReceiptWidget = new ReceiptWidget(getActivity(), receipt.findViewById(R.id.receipt), false);

		/*
		 * Configuring the policy cancellation section
		 */
		ConfirmationUtils.determineCancellationPolicy(instance.mRate, receipt);

		TextView contactView = (TextView) receipt.findViewById(R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);

		mReceiptWidget.updateData(instance.mProperty, instance.mSearchParams, instance.mRate,
				instance.mBookingResponse, instance.mBillingInfo);

		return receipt;
	}

	//////////////////////////////////////////////////////////////////////////
	// Convenience method

	public ConfirmationFragmentActivity.InstanceFragment getInstance() {
		return ((ConfirmationFragmentActivity) getActivity()).mInstance;
	}
}
