package com.expedia.bookings.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
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

		mReceiptWidget = new ReceiptWidget(getActivity(), receipt.findViewById(R.id.receipt), false);

		/*
		 * Configuring the policy cancellation section
		 */
		ConfirmationUtils.determineCancellationPolicy(Db.getSelectedRate(), receipt);

		TextView contactView = (TextView) receipt.findViewById(R.id.contact_text_view);
		String contactText = ConfirmationUtils.determineContactText(getActivity());
		ConfirmationUtils.configureContactView(getActivity(), contactView, contactText);

		mReceiptWidget.updateData(Db.getSelectedProperty(), Db.getSearchParams(), Db.getSelectedRate(),
				Db.getBookingResponse(), Db.getBillingInfo());

		return receipt;
	}
}
