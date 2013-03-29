package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Money;

public class HotelPriceChangeDialog extends DialogFragment {
	private boolean mIsPriceHigher;
	private Money mOldTotal;
	private Money mNewTotal;

	public HotelPriceChangeDialog(boolean isPriceHigher, Money oldTotal, Money newTotal) {
		mIsPriceHigher = isPriceHigher;
		mOldTotal = oldTotal;
		mNewTotal = newTotal;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int priceChangeMessageId;
		if (mIsPriceHigher) {
			priceChangeMessageId = R.string.the_hotel_raised_the_total_price_TEMPLATE;
		}
		else {
			priceChangeMessageId = R.string.the_hotel_lowered_the_total_price_TEMPLATE;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setMessage(getString(priceChangeMessageId, mOldTotal.getFormattedMoney(),
				mNewTotal.getFormattedMoney()));

		builder.setNeutralButton(com.mobiata.android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				dismiss();
			}
		});

		return builder.create();
	}
}
