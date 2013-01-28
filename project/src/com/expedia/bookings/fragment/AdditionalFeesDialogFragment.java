package com.expedia.bookings.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.WebViewActivity;

public class AdditionalFeesDialogFragment extends DialogFragment {

	private static final String ARG_BAGGAGE_FEES_URL = "ARG_BAGGAGE_FEES_URL";
	private static final String ARG_OB_FEES_URL = "ARG_OB_FEES_URL";

	public static AdditionalFeesDialogFragment newInstance(String baggageFeesUrl, String obFeesUrl) {
		AdditionalFeesDialogFragment fragment = new AdditionalFeesDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_BAGGAGE_FEES_URL, baggageFeesUrl);
		args.putString(ARG_OB_FEES_URL, obFeesUrl);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		final int numUrls = 2;
		final int[] itemNamesResIds = new int[numUrls];
		final String[] itemNames = new String[numUrls];
		final String[] itemUrls = new String[numUrls];

		itemNamesResIds[0] = R.string.baggage_fees;
		itemNames[0] = getString(R.string.baggage_fees);
		itemUrls[0] = args.getString(ARG_BAGGAGE_FEES_URL);

		itemNamesResIds[1] = R.string.payment_processing_fees;
		itemNames[1] = getString(R.string.payment_processing_fees);
		itemUrls[1] = args.getString(ARG_OB_FEES_URL);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getString(R.string.additional_fees));
		builder.setItems(itemNames, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = WebViewActivity.getIntent(getActivity(), itemUrls[which],
						R.style.FlightTheme, itemNamesResIds[which], true);
				getActivity().startActivity(intent);
			}
		});
		return builder.create();
	}

}
