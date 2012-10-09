package com.expedia.bookings.fragment;

import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.mobiata.android.json.JSONUtils;

public class PriceChangeDialogFragment extends DialogFragment {

	public static final String TAG = PriceChangeDialogFragment.class.getName();

	private static final String ARG_CURRENT_OFFER = "ARG_CURRENT_OFFER";
	private static final String ARG_NEW_OFFER = "ARG_NEW_OFFER";

	private PriceChangeDialogFragmentListener mListener;

	public static PriceChangeDialogFragment newInstance(FlightTrip currentOffer, FlightTrip newOffer) {
		PriceChangeDialogFragment fragment = new PriceChangeDialogFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, ARG_CURRENT_OFFER, currentOffer);
		JSONUtils.putJSONable(args, ARG_NEW_OFFER, newOffer);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		if (activity instanceof PriceChangeDialogFragmentListener) {
			mListener = (PriceChangeDialogFragmentListener) activity;
		}
		else {
			throw new RuntimeException(
					"PriceChangeDialogFragment Activity must implement PriceChangeDialogFragmentListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		final FlightTrip currentOffer = JSONUtils.getJSONable(args, ARG_CURRENT_OFFER, FlightTrip.class);
		final FlightTrip newOffer = JSONUtils.getJSONable(args, ARG_NEW_OFFER, FlightTrip.class);

		// Determine the price change (up or down)
		Money diff = new Money(currentOffer.getTotalFare());
		diff.subtract(newOffer.getTotalFare());

		int strId;
		if (diff.getAmount().compareTo(BigDecimal.ZERO) < 0) {
			strId = R.string.error_flight_price_increased;
		}
		else {
			strId = R.string.error_flight_price_decreased;
		}

		// Reset the price to always be positive for formatting purposes
		diff.setAmount(diff.getAmount().abs());

		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setMessage(getString(strId, diff.getFormattedMoney(), newOffer.getTotalFare().getFormattedMoney()));
		builder.setPositiveButton(R.string.accept, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Update our flight trip info
				Db.getFlightSearch().getSelectedFlightTrip().updateFrom(newOffer);
				Db.kickOffBackgroundSave(getActivity());

				mListener.onAcceptPriceChange();
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onCancelPriceChange();
			}
		});

		return builder.create();
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface PriceChangeDialogFragmentListener {
		public void onAcceptPriceChange();

		public void onCancelPriceChange();
	}
}
