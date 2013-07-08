package com.expedia.bookings.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
	public void onStart() {
		super.onStart();

		// Fixed inability to cancel by touching outside of dialog
		getDialog().setCanceledOnTouchOutside(true);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());

		View body = inflater.inflate(R.layout.fragment_dialog_trip_price_breakdown, null);
		Dialog dialog = new Dialog(getActivity(), R.style.ExpediaLoginDialog);
		dialog.requestWindowFeature(STYLE_NO_TITLE);
		dialog.setCancelable(false);
		dialog.setContentView(body);

		// Per traveler price

		FlightTrip trip = Db.getFlightSearch().getSelectedFlightTrip();
		LinearLayout travContainer = Ui.findView(body, R.id.traveler_price_container);
		for (int i = 0; i < Db.getFlightSearch().getSearchParams().getNumAdults(); i++) {
			travContainer = (LinearLayout) inflater.inflate(
					R.layout.section_display_flight_trip_price_breakdown_for_traveler, travContainer, true);
			SectionFlightTrip travTrip = (SectionFlightTrip) travContainer.getChildAt(i);
			TextView travTripLabel = Ui.findView(travTrip, R.id.label_traveler_name);
			travTrip.bind(trip);

			String travLabelFormat = getResources().getString(R.string.traveler_num_and_category_TEMPLATE);
			String travLabel = String.format(travLabelFormat, i + 1);
			travTripLabel.setText(travLabel);
		}

		// OB fees

		TextView fees = Ui.findView(body, R.id.display_fees);
		if (trip.getFees() != null) {
			fees.setText(trip.getFees().getFormattedMoney());
		}
		else {
			fees.setText("");
		}

		// LCC card fee
		View divider = Ui.findView(body, R.id.divider_card_fee);
		ViewGroup cardFeeContainer = Ui.findView(body, R.id.container_card_fee);
		TextView cardFees = Ui.findView(body, R.id.display_card_fees);

		Money cardFee = trip.getCardFee(Db.getBillingInfo());

		if (cardFee != null && trip.showFareWithCardFee(getActivity(), Db.getBillingInfo())) {
			divider.setVisibility(View.VISIBLE);
			cardFeeContainer.setVisibility(View.VISIBLE);
			cardFees.setText(cardFee.getFormattedMoney());
		}
		else {
			divider.setVisibility(View.GONE);
			cardFeeContainer.setVisibility(View.GONE);
		}

		// Total price

		TextView totalPriceBottom = Ui.findView(body, R.id.display_total_price_bottom);
		if (trip.getTotalFare() != null) {
			String text;
			if (trip.showFareWithCardFee(getActivity(), Db.getBillingInfo())) {
				text = trip.getTotalFareWithCardFee(Db.getBillingInfo()).getFormattedMoney();
			}
			else {
				text = trip.getTotalFare().getFormattedMoney();
			}
			totalPriceBottom.setText(text);
		}
		else {
			totalPriceBottom.setText("");
		}

		View doneBtn = Ui.findView(body, R.id.positive_button);
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
