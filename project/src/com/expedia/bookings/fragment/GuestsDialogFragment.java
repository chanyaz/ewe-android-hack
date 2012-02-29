package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.SearchResultsFragmentActivity;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.StrUtils;

public class GuestsDialogFragment extends DialogFragment {

	private static final String KEY_NUM_ADULTS = "numAdults";
	private static final String KEY_CHILDREN = "children";

	private NumberPicker mAdultsNumberPicker;
	private NumberPicker mChildrenNumberPicker;
	private TextView mSelectChildAgeTextView;
	private View mChildAgesLayout;

	private int mAdultCount;
	private ArrayList<Integer> mChildren;

	public static GuestsDialogFragment newInstance(int initialAdultCount, List<Integer> initialChildren) {
		GuestsDialogFragment dialog = new GuestsDialogFragment();
		dialog.initializeGuests(initialAdultCount, initialChildren);
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
		mSelectChildAgeTextView = (TextView) parent.findViewById(R.id.label_select_each_childs_age);
		mChildAgesLayout = parent.findViewById(R.id.child_ages_layout);

		// Block NumberPickers from being editable
		mAdultsNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		mChildrenNumberPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		// Configure the display values on the pickers
		GuestsPickerUtils.configureDisplayedValues(getActivity(), mAdultsNumberPicker, mChildrenNumberPicker);
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);

		// Set initial values for pickers
		if (savedInstanceState != null) {
			mAdultCount = savedInstanceState.getInt(KEY_NUM_ADULTS);
			mChildren = savedInstanceState.getIntegerArrayList(KEY_CHILDREN);
		}
		mAdultsNumberPicker.setValue(mAdultCount);
		mChildrenNumberPicker.setValue(mChildren.size());
		mAdultsNumberPicker.setOnValueChangedListener(mPersonCountChangeListener);
		mChildrenNumberPicker.setOnValueChangedListener(mPersonCountChangeListener);
		displayGuestCountViews();

		// Setup initial value for title (FYI, need to call this or else the title never appears)
		builder.setTitle(getTitleText());

		// Setup button listeners
		builder.setPositiveButton(R.string.search, mOkButtonClickListener);
		builder.setNegativeButton(android.R.string.cancel, null);

		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);

		// Set the view of the dialog instead of through the builder, to be able to use
		// the extra viewSpacing* arguments which are not available through the builder.
		int spacing = getResources().getDimensionPixelOffset(R.dimen.dialog_view_spacing);
		dialog.setView(parent, spacing, spacing, spacing, spacing);

		return dialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(KEY_NUM_ADULTS, mAdultsNumberPicker.getValue());
		outState.putIntegerArrayList(KEY_CHILDREN, mChildren);
	}

	public String getTitleText() {
		int numAdults = mAdultsNumberPicker.getValue();
		int numChildren = mChildrenNumberPicker.getValue();
		return StrUtils.formatGuests(getActivity(), numAdults, numChildren);
	}

	// Creates a copy of the list argument
	private void initializeGuests(int initialAdultCount, List<Integer> initialChildren) {
		mAdultCount = initialAdultCount;
		if (initialChildren == null) {
			mChildren = new ArrayList<Integer>();
			return;
		}
		mChildren = new ArrayList<Integer>(initialChildren.size());
		for (int i : initialChildren) {
			mChildren.add(i);
		}
	}

	private void displayGuestCountViews() {
		Dialog dialog = getDialog();
		if (dialog != null) {
			dialog.setTitle(getTitleText());
		}
		GuestsPickerUtils.updateNumberPickerRanges(mAdultsNumberPicker, mChildrenNumberPicker);
		GuestsPickerUtils.showOrHideChildAgeSpinners(getActivity(), mChildren, mChildAgesLayout,
				mChildAgeSelectedListener);
		mChildAgesLayout.setVisibility(mChildren != null && mChildren.size() > 0 ? View.VISIBLE : View.GONE);

		String labelSelectEachChildsAge = getResources().getQuantityString(R.plurals.select_each_childs_age,
				mChildren.size());
		mSelectChildAgeTextView.setText(labelSelectEachChildsAge);

	}

	// Configure number pickers to dynamically change the layout on value changes
	private final OnValueChangeListener mPersonCountChangeListener = new OnValueChangeListener() {

		public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
			mAdultCount = mAdultsNumberPicker.getValue();
			GuestsPickerUtils.resizeChildrenList(getActivity(), mChildren, mChildrenNumberPicker.getValue());
			displayGuestCountViews();
		}

	};

	private final OnItemSelectedListener mChildAgeSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			GuestsPickerUtils.setChildrenFromSpinners(getActivity(), mChildAgesLayout, mChildren);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}

	};

	private final OnClickListener mOkButtonClickListener = new OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			SearchResultsFragmentActivity activity = (SearchResultsFragmentActivity) getActivity();
			activity.setGuests(mAdultCount, mChildren);
			activity.startSearch();
			GuestsPickerUtils.updateDefaultChildAges(activity, mChildren);
		}

	};

}
