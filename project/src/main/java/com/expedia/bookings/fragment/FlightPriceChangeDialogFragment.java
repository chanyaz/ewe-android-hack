package com.expedia.bookings.fragment;

import java.math.BigDecimal;

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
import com.expedia.bookings.otto.Events;
import com.mobiata.android.json.JSONUtils;

public class FlightPriceChangeDialogFragment extends DialogFragment {

	public static final String TAG = FlightPriceChangeDialogFragment.class.getName();

	private static final String ARG_CURRENT_OFFER = "ARG_CURRENT_OFFER";
	private static final String ARG_NEW_OFFER = "ARG_NEW_OFFER";

	public static FlightPriceChangeDialogFragment newInstance(FlightTrip currentOffer, FlightTrip newOffer) {
		FlightPriceChangeDialogFragment fragment = new FlightPriceChangeDialogFragment();
		Bundle args = new Bundle();
		JSONUtils.putJSONable(args, ARG_CURRENT_OFFER, currentOffer);
		JSONUtils.putJSONable(args, ARG_NEW_OFFER, newOffer);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onPause() {
		super.onPause();
		Events.unregister(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		Events.register(this);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();

		final FlightTrip currentOffer = JSONUtils.getJSONable(args, ARG_CURRENT_OFFER, FlightTrip.class);
		final FlightTrip newOffer = JSONUtils.getJSONable(args, ARG_NEW_OFFER, FlightTrip.class);

		// Calculate message (based on online booking fees and whether the fare went up/down)
		CharSequence message;

		Money diff = new Money(currentOffer.getTotalPrice());
		diff.subtract(newOffer.getTotalPrice());

		String obFeesFormatted = null;
		Money obFees = newOffer.getOnlineBookingFeesAmount();
		if (obFees != null) {
			obFeesFormatted = obFees.getFormattedMoney();

			// For legacy client reasons, the total fare is updated with the obFees,
			// so in order to determine if a price change + obFees has occurred we
			// have to account for it in the difference in price.
			diff.add(obFees);
		}

		int compareTo = diff.getAmount().compareTo(BigDecimal.ZERO);

		// Reset the price to always be positive for formatting purposes
		diff.setAmount(diff.getAmount().abs());
		String diffFormatted = diff.getFormattedMoney();
		String totalFareFormatted = newOffer.getTotalPrice().getFormattedMoney();
		if (compareTo < 0) {
			if (obFeesFormatted != null) {
				message = getString(R.string.error_flight_price_increased_with_fee, obFeesFormatted, diffFormatted,
						totalFareFormatted);
			}
			else {
				message = getString(R.string.error_flight_price_increased, diffFormatted, totalFareFormatted);
			}
		}
		else if (compareTo > 0) {
			if (obFeesFormatted != null) {
				message = getString(R.string.error_flight_price_decreased_with_fee, obFeesFormatted, diffFormatted,
						totalFareFormatted);
			}
			else {
				message = getString(R.string.error_flight_price_decreased, diffFormatted, totalFareFormatted);
			}
		}
		else if (obFeesFormatted != null) {
			message = getString(R.string.error_flight_added_processing_fee, obFeesFormatted, totalFareFormatted);
		}
		else {
			throw new RuntimeException("Somehow got a PRICE_CHANGE exception with no price changing!");
		}

		AlertDialog.Builder builder = new Builder(getActivity());
		builder.setMessage(message);
		builder.setPositiveButton(R.string.accept, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Update our flight trip info
				Db.getTripBucket().getFlight().getFlightTrip().updateFrom(newOffer);
				Db.saveTripBucket(getActivity());

				Events.post(new Events.PriceChangeDialogAccept());
			}
		});
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Events.post(new Events.PriceChangeDialogCancel());
			}
		});

		return builder.create();
	}

}
