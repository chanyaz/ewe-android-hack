package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.FlightTrip;
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
	public void onStart() {
		super.onStart();

		// Fixed inability to cancel by touching outside of dialog
		getDialog().setCanceledOnTouchOutside(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Service.LAYOUT_INFLATER_SERVICE);

		View body = inflater.inflate(R.layout.fragment_dialog_trip_price_breakdown, null);
		Dialog dialog = new Dialog(getActivity(), R.style.ExpediaLoginDialog);
		dialog.requestWindowFeature(STYLE_NO_TITLE);
		dialog.setCancelable(false);
		dialog.setContentView(body);

		SectionFlightTrip travTrip = Ui.findView(body, R.id.traveler_price_breakdown);
		TextView totalPriceBottom = Ui.findView(travTrip, R.id.display_total_price_bottom);
		TextView travTripLabel = Ui.findView(travTrip, R.id.travler_num_and_category);
		View doneBtn = Ui.findView(body, R.id.positive_button);

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		travTrip.bind(trip);

		//TODO: Flights currently only supports single adult travlers this logic must change someday
		String travLabelFormat = getResources().getString(R.string.traveler_num_and_category_TEMPLATE);
		String travLabel = String.format(travLabelFormat, 1, getResources().getString(R.string.adult));
		travTripLabel.setText(travLabel);

		if (trip.getTotalFare() != null) {
			totalPriceBottom.setText(trip.getTotalFare().getFormattedMoney());
		}
		else {
			totalPriceBottom.setText("");
		}

		doneBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

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
