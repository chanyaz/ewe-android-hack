package com.expedia.bookings.fragment;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.LineOfBusiness;
import com.expedia.bookings.utils.NavUtils;
import com.squareup.phrase.Phrase;

public class BookingUnavailableDialogFragment extends DialogFragment implements OnClickListener {

	public static final String TAG = BookingUnavailableDialogFragment.class.getName();

	private static final String ARG_IS_PLURAL = "ARG_IS_PLURAL";
	private static final String ARG_IS_FLIGHT = "ARG_IS_FLIGHT";

	private boolean mIsFlightLOB;

	public static BookingUnavailableDialogFragment newInstance(boolean isPlural, LineOfBusiness lob) {
		BookingUnavailableDialogFragment fragment = new BookingUnavailableDialogFragment();
		Bundle args = new Bundle();
		args.putBoolean(ARG_IS_PLURAL, isPlural);
		args.putBoolean(ARG_IS_FLIGHT, lob == LineOfBusiness.FLIGHTS ? true : false);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(false);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new Builder(getActivity());
		boolean isPlural = getArguments().getBoolean(ARG_IS_PLURAL);
		mIsFlightLOB = getArguments().getBoolean(ARG_IS_FLIGHT);
		if (mIsFlightLOB) {

			if (isPlural) {
				builder.setMessage(Phrase.from(getActivity(), R.string.error_flights_no_longer_available_TEMPLATE).put("brand", BuildConfig.brand).format());
			}
			else {
				builder.setMessage(Phrase.from(getActivity(), R.string.error_flight_no_longer_available_TEMPLATE)
					.put("brand", BuildConfig.brand).format());
			}

			builder.setNeutralButton(isPlural ? R.string.pick_new_flights : R.string.pick_new_flight, this);
		}
		else {
			builder.setMessage(R.string.error_hotel_no_longer_available);
			builder.setNeutralButton(R.string.pick_new_hotel, this);
		}

		return builder.create();
	}

	//////////////////////////////////////////////////////////////////////////
	// OnClickListener

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (mIsFlightLOB) {
			NavUtils.restartFlightSearch(getActivity());
		}
		else {
			NavUtils.restartHotelSearch(getActivity());
		}
	}
}
