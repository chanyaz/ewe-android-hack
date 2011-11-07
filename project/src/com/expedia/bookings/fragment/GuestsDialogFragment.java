package com.expedia.bookings.fragment;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.TabletActivity;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.StrUtils;

public class GuestsDialogFragment extends DialogFragment {

	private static final String KEY_NUM_ADULTS = "numAdults";
	private static final String KEY_NUM_CHILDREN = "numChildren";

	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;

	private int mInitialAdults;
	private int mInitialChildren;

	public static GuestsDialogFragment newInstance(int initialAdults, int initialChildren) {
		GuestsDialogFragment dialog = new GuestsDialogFragment();
		dialog.setInitialGuests(initialAdults, initialChildren);
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

		// Block NumberPickers from being editable
		mAdultsNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		mChildrenNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// Configure the display values on the pickers
		GuestsPickerUtils.configureDisplayedValues(getActivity(), mAdultsNumberPicker, mChildrenNumberPicker);
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);

		// Set initial values for pickers
		if (savedInstanceState == null) {
			mAdultsNumberPicker.setValue(mInitialAdults);
			mChildrenNumberPicker.setValue(mInitialChildren);
		}
		else {
			mAdultsNumberPicker.setValue(savedInstanceState.getInt(KEY_NUM_ADULTS));
			mChildrenNumberPicker.setValue(savedInstanceState.getInt(KEY_NUM_CHILDREN));
		}

		// Configure number pickers to dynamically change the layout on value changes
		OnValueChangeListener listener = new OnValueChangeListener() {
			public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
				getDialog().setTitle(getTitleText());
				GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
			}
		};
		mAdultsNumberPicker.setOnValueChangedListener(listener);
		mChildrenNumberPicker.setOnValueChangedListener(listener);

		// Setup initial value for title (FYI, need to call this or else the title never appears)
		builder.setTitle(getTitleText());

		// Setup button listeners
		builder.setPositiveButton(R.string.search, new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				TabletActivity activity = (TabletActivity) getActivity();
				activity.setGuests(mAdultsNumberPicker.getValue(),
						mChildrenNumberPicker.getValue());
				activity.startSearch();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);

		return builder.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(KEY_NUM_ADULTS, mAdultsNumberPicker.getValue());
		outState.putInt(KEY_NUM_CHILDREN, mChildrenNumberPicker.getValue());
	}

	public String getTitleText() {
		int adults = mAdultsNumberPicker.getValue();
		int children = mChildrenNumberPicker.getValue();
		return StrUtils.formatGuests(getActivity(), adults, children);
	}

	private void setInitialGuests(int initialAdults, int initialChildren) {
		mInitialAdults = initialAdults;
		mInitialChildren = initialChildren;
	}
}
