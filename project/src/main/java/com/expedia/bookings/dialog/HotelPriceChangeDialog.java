package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;

public class HotelPriceChangeDialog extends DialogFragment {
	private static final String ARG_IS_PRICE_HIGHER = "ARG_IS_PRICE_HIGHER";
	private static final String ARG_OLD_TOTAL = "ARG_OLD_TOTAL";
	private static final String ARG_NEW_TOTAL = "ARG_NEW_TOTAL";

	public static HotelPriceChangeDialog newInstance(boolean isPriceHigher, Money oldTotal, Money newTotal) {
		HotelPriceChangeDialog frag = new HotelPriceChangeDialog();

		Bundle args = new Bundle();
		args.putBoolean(ARG_IS_PRICE_HIGHER, isPriceHigher);
		args.putString(ARG_OLD_TOTAL, oldTotal.getFormattedMoney());
		args.putString(ARG_NEW_TOTAL, newTotal.getFormattedMoney());
		frag.setArguments(args);

		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		boolean isPriceHigher = args.getBoolean(ARG_IS_PRICE_HIGHER, false);
		String oldTotal = args.getString(ARG_OLD_TOTAL);
		String newTotal = args.getString(ARG_NEW_TOTAL);

		int priceChangeMessageId;
		if (isPriceHigher) {
			priceChangeMessageId = R.string.the_hotel_raised_the_total_price_TEMPLATE;
		}
		else {
			priceChangeMessageId = R.string.the_hotel_lowered_the_total_price_TEMPLATE;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(getString(priceChangeMessageId, oldTotal,
				newTotal));

		builder.setNeutralButton(com.mobiata.android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				dismiss();
			}
		});

		return builder.create();
	}
}
