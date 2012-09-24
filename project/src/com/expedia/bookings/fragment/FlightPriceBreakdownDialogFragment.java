package com.expedia.bookings.fragment;

import java.math.BigDecimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.section.SectionFlightTrip;
import com.expedia.bookings.utils.Ui;

/**
 * We display the price breakdown of the Db.getFlightSearch().getSelectedFlightTrip() also known as the currently selected trip
 *
 */
public class FlightPriceBreakdownDialogFragment extends DialogFragment {

	public static FlightPriceBreakdownDialogFragment newInstance() {
		FlightPriceBreakdownDialogFragment frag = new FlightPriceBreakdownDialogFragment();
		Bundle args = new Bundle();
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Service.LAYOUT_INFLATER_SERVICE);

		View body = inflater.inflate(R.layout.fragment_dialog_trip_price_breakdown, null);
		SectionFlightTrip travTrip = Ui.findView(body, R.id.traveler_price_breakdown);
		SectionFlightTrip totalPrice = Ui.findView(body, R.id.total_price_section);
		TextView expediaFees = Ui.findView(body, R.id.expedia_fee);
		TextView travTripLabel = Ui.findView(travTrip, R.id.travler_num_and_category);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		travTrip.bind(trip);
		totalPrice.bind(trip);

		//TODO: Flights currently only supports single adult travlers this logic must change someday
		String travLabelFormat = getResources().getString(R.string.traveler_num_and_category_TEMPLATE);
		String travLabel = String.format(travLabelFormat, 1, getResources().getString(R.string.adult));
		travTripLabel.setText(travLabel);

		//TODO: Currently we only support the US point of sale, in the future we will have real fees and different currencies
		Money expediaFeeAmount = new Money();
		expediaFeeAmount.setAmount(new BigDecimal(0));
		expediaFeeAmount.setCurrency("USD");
		expediaFees.setText(expediaFeeAmount.getFormattedMoney());

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		AlertDialog dialog = builder.setCancelable(false)
				.setView(body)
				.setPositiveButton(R.string.button_done, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				}).create();
		return dialog;

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);

	}
}
