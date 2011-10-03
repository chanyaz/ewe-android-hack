package com.expedia.bookings.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.utils.StrUtils;

public class GuestsDialogFragment extends DialogFragment {

	private static final int MAX_PER_TYPE = 4;
	private static final int MAX_GUESTS = 5;

	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;

	private int mInitialAdults;
	private int mInitialChildren;

	public static GuestsDialogFragment newInstance(int initialAdults, int initialChildren) {
		GuestsDialogFragment dialog = new GuestsDialogFragment();
		dialog.setInitialAdults(initialAdults);
		dialog.setInitialChildren(initialChildren);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		// Inflate the main content
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View parent = inflater.inflate(R.layout.fragment_dialog_guests, null);
		mAdultsNumberPicker = (NumberPicker) parent.findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (NumberPicker) parent.findViewById(R.id.children_number_picker);
		builder.setView(parent);

		// Configure the display values on the pickers
		Resources r = getResources();
		String[] adultDisplayedValues = new String[MAX_PER_TYPE];
		for (int a = 1; a <= MAX_PER_TYPE; a++) {
			adultDisplayedValues[a - 1] = r.getQuantityString(R.plurals.number_of_adults, a, a);
		}
		String[] childDisplayedValues = new String[MAX_PER_TYPE + 1];
		for (int a = 0; a <= MAX_PER_TYPE; a++) {
			childDisplayedValues[a] = r.getQuantityString(R.plurals.number_of_children, a, a);
		}
		mAdultsNumberPicker.setDisplayedValues(adultDisplayedValues);
		mChildrenNumberPicker.setDisplayedValues(childDisplayedValues);

		// Configure initial number picker ranges
		mAdultsNumberPicker.setMinValue(1);
		mChildrenNumberPicker.setMinValue(0);
		updateNumberPickerRanges();

		// Set initial values for pickers
		mAdultsNumberPicker.setValue(mInitialAdults);
		mChildrenNumberPicker.setValue(mInitialChildren);

		// Configure number pickers to dynamically change the layout on value changes
		OnValueChangeListener listener = new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				getDialog().setTitle(getTitleText());
				updateNumberPickerRanges();
			}
		};
		mAdultsNumberPicker.setOnValueChangedListener(listener);
		mChildrenNumberPicker.setOnValueChangedListener(listener);

		// Setup initial value for title (FYI, need to call this or else the title never appears)
		builder.setTitle(getTitleText());

		// Setup button listeners
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				((TabletActivity) getActivity()).setGuests(mAdultsNumberPicker.getValue(),
						mChildrenNumberPicker.getValue());
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}

	public String getTitleText() {
		int adults = mAdultsNumberPicker.getValue();
		int children = mChildrenNumberPicker.getValue();
		return StrUtils.formatGuests(getActivity(), adults, children);
	}

	public void updateNumberPickerRanges() {
		int adults = mAdultsNumberPicker.getValue();
		int children = mChildrenNumberPicker.getValue();
		int total = adults + children;
		int remaining = MAX_GUESTS - total;

		mAdultsNumberPicker.setMaxValue(Math.min(MAX_PER_TYPE, adults + remaining));
		mChildrenNumberPicker.setMaxValue(Math.min(MAX_PER_TYPE, children + remaining));
	}

	private void setInitialAdults(int initialAdults) {
		mInitialAdults = initialAdults;
	}

	private void setInitialChildren(int initialChildren) {
		mInitialChildren = initialChildren;
	}
}
