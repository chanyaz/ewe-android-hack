package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.expedia.bookings.utils.StrUtils;
import com.expedia.bookings.widget.SimpleNumberPicker;
import com.mobiata.android.util.Ui;

@TargetApi(11)
public class GuestsDialogFragment extends DialogFragment {

	private static final String KEY_NUM_ADULTS = "numAdults";
	private static final String KEY_CHILDREN = "children";

	private SimpleNumberPicker mAdultsNumberPicker;
	private SimpleNumberPicker mChildrenNumberPicker;
	private TextView mSelectChildAgeTextView;
	private View mChildAgesLayout;

	private int mAdultCount;
	private ArrayList<ChildTraveler> mChildren;

	private GuestsDialogFragmentListener mListener;

	public static GuestsDialogFragment newInstance(int initialAdultCount, List<ChildTraveler> initialChildren) {
		GuestsDialogFragment dialog = new GuestsDialogFragment();
		dialog.initializeGuests(initialAdultCount, initialChildren);
		return dialog;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, GuestsDialogFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (!getShowsDialog()) {
			return createView(inflater, container, savedInstanceState);
		}
		else {
			return super.onCreateView(inflater, container, savedInstanceState);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Builder builder = new AlertDialog.Builder(getActivity());

		// Inflate the main content
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		View parent = createView(inflater, null, savedInstanceState);

		// Setup initial value for title (FYI, need to call this or else the title never appears)
		builder.setTitle(getTitleText());

		// Setup button listeners
		builder.setPositiveButton(R.string.search, mOkButtonClickListener);
		builder.setNegativeButton(R.string.cancel, null);

		AlertDialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(true);

		// Set the view of the dialog instead of through the builder, to be able to use
		// the extra viewSpacing* arguments which are not available through the builder.
		int spacing = getResources().getDimensionPixelOffset(R.dimen.dialog_view_spacing);
		dialog.setView(parent, spacing, spacing, spacing, spacing);

		return dialog;
	}

	private View createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the main content
		View parent = inflater.inflate(R.layout.fragment_dialog_guests, container, false);
		mAdultsNumberPicker = (SimpleNumberPicker) parent.findViewById(R.id.adults_number_picker);
		mChildrenNumberPicker = (SimpleNumberPicker) parent.findViewById(R.id.children_number_picker);
		mSelectChildAgeTextView = (TextView) parent.findViewById(R.id.label_select_each_childs_age);
		mChildAgesLayout = parent.findViewById(R.id.child_ages_layout);

		mAdultsNumberPicker.setFormatter(mAdultsNumberPickerFormatter);
		mChildrenNumberPicker.setFormatter(mChildrenNumberPickerFormatter);

		// Set initial values for pickers
		if (savedInstanceState != null) {
			mAdultCount = savedInstanceState.getInt(KEY_NUM_ADULTS);
			mChildren = savedInstanceState.getParcelableArrayList(KEY_CHILDREN);
		}

		mAdultsNumberPicker.setValue(mAdultCount);
		mChildrenNumberPicker.setValue(mChildren.size());
		mAdultsNumberPicker.setOnValueChangeListener(mPersonCountChangeListener);
		mChildrenNumberPicker.setOnValueChangeListener(mPersonCountChangeListener);
		displayGuestCountViews();

		return parent;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(KEY_NUM_ADULTS, mAdultsNumberPicker.getValue());
		outState.putParcelableArrayList(KEY_CHILDREN, mChildren);
	}

	public String getTitleText() {
		int numAdults = mAdultsNumberPicker.getValue();
		int numChildren = mChildrenNumberPicker.getValue();
		return StrUtils.formatGuests(getActivity(), numAdults, numChildren);
	}

	// Creates a copy of the list argument
	private void initializeGuests(int initialAdultCount, List<ChildTraveler> initialChildren) {
		mAdultCount = initialAdultCount;
		if (initialChildren == null) {
			mChildren = new ArrayList<ChildTraveler>();
			return;
		}
		mChildren = new ArrayList<ChildTraveler>(initialChildren.size());
		for (ChildTraveler child : initialChildren) {
			mChildren.add(child);
		}
	}

	private void displayGuestCountViews() {
		Dialog dialog = getDialog();
		if (dialog != null) {
			dialog.setTitle(getTitleText());
		}
		GuestsPickerUtils.configureAndUpdateDisplayedValues(getActivity(), mAdultsNumberPicker, mChildrenNumberPicker);
		GuestsPickerUtils.showOrHideChildAgeSpinners(getActivity(), mChildren, mChildAgesLayout,
				mChildAgeSelectedListener, View.INVISIBLE);
		mChildAgesLayout.setVisibility(mChildren != null && mChildren.size() > 0 ? View.VISIBLE : View.INVISIBLE);

		String labelSelectEachChildsAge = getResources().getQuantityString(R.plurals.select_each_childs_age,
				mChildren.size());
		mSelectChildAgeTextView.setText(labelSelectEachChildsAge);

	}

	// Configure number pickers to dynamically change the layout on value changes
	private final SimpleNumberPicker.OnValueChangeListener mPersonCountChangeListener = new SimpleNumberPicker.OnValueChangeListener() {

		public void onValueChange(SimpleNumberPicker picker, int oldVal, int newVal) {
			mAdultCount = mAdultsNumberPicker.getValue();
			GuestsPickerUtils.resizeChildrenList(getActivity(), mChildren, mChildrenNumberPicker.getValue());
			displayGuestCountViews();

			if (!getShowsDialog()) {
				mListener.onGuestsChanged(mAdultCount, mChildren);
			}
		}

	};

	private final OnItemSelectedListener mChildAgeSelectedListener = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			GuestsPickerUtils.setChildrenFromSpinners(getActivity(), mChildAgesLayout, mChildren);

			if (!getShowsDialog()) {
				mListener.onGuestsChanged(mAdultCount, mChildren);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}

	};

	private final OnClickListener mOkButtonClickListener = new OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			mListener.onGuestsChanged(mAdultCount, mChildren);
		}

	};

	// Number picker formatters
	private final SimpleNumberPicker.Formatter mAdultsNumberPickerFormatter = new SimpleNumberPicker.Formatter() {
		@Override
		public String format(int value) {
			return getActivity().getResources().getQuantityString(R.plurals.number_of_adults, value, value);
		}
	};

	private final SimpleNumberPicker.Formatter mChildrenNumberPickerFormatter = new SimpleNumberPicker.Formatter() {
		@Override
		public String format(int value) {
			return getActivity().getResources().getQuantityString(R.plurals.number_of_children, value, value);
		}
	};

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface GuestsDialogFragmentListener {
		public void onGuestsChanged(int numAdults, ArrayList<ChildTraveler> numChildren);

		public void onGuestsChanged(int numAdults, ArrayList<ChildTraveler> numChildren, boolean infantsInLaps);
	}
}
