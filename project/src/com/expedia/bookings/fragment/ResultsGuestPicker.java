package com.expedia.bookings.fragment;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.GuestsPickerUtils;
import com.mobiata.android.util.Ui;

import static com.expedia.bookings.utils.FontCache.Font;

/**
 * Results loading fragment for Tablet
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsGuestPicker extends Fragment {

	private static final String STATE_ADULT_COUNT = "STATE_ADULT_COUNT";
	private static final String STATE_CHILDREN = "STATE_CHILDREN";

	public static final int MAX_ADULTS = 6;
	public static final int MAX_CHILDREN = 4;
	public static final int MAX_TRAVELERS = 6;

	private ViewGroup mRootC;
	private TextView mAdultText;
	private TextView mChildText;
	private View mAdultMinus;
	private View mAdultPlus;
	private View mChildMinus;
	private View mChildPlus;
	private View mChildAgesLayout;
	private TextView mHeaderTextView;
	private TextView mInfantAlertTextView;

	private int mAdultCount;
	private ArrayList<ChildTraveler> mChildren = new ArrayList<ChildTraveler>();
	private boolean mInfantsInLaps;

	private GuestsDialogFragment.GuestsDialogFragmentListener mListener;

	public static ResultsGuestPicker newInstance(int initialAdultCount, List<ChildTraveler> initialChildren) {
		ResultsGuestPicker frag = new ResultsGuestPicker();
		frag.initializeGuests(initialAdultCount, initialChildren);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, GuestsDialogFragment.GuestsDialogFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (savedInstanceState != null) {
			mAdultCount = savedInstanceState.getInt(STATE_ADULT_COUNT);
			mChildren = savedInstanceState.getParcelableArrayList(STATE_CHILDREN);
		}

		mRootC = (ViewGroup) inflater.inflate(R.layout.fragment_results_guests, null);
		mAdultText = Ui.findView(mRootC, R.id.adult_count_text);
		mAdultMinus = Ui.findView(mRootC, R.id.adults_minus);
		mAdultPlus = Ui.findView(mRootC, R.id.adults_plus);

		mChildText = Ui.findView(mRootC, R.id.child_count_text);
		mChildMinus = Ui.findView(mRootC, R.id.children_minus);
		mChildPlus = Ui.findView(mRootC, R.id.children_plus);

		mChildAgesLayout = Ui.findView(mRootC, R.id.child_ages_layout);
		mChildAgesLayout.setVisibility(getChildAgesVisibility());

		mHeaderTextView = Ui.findView(mRootC, R.id.tablet_guest_picker_header);
		mInfantAlertTextView = Ui.findView(mRootC, R.id.tablet_lap_infant_alert);

		FontCache.setTypeface(mHeaderTextView, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mAdultText, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mChildText, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mInfantAlertTextView, Font.ROBOTO_LIGHT);

		TextView doneButton = Ui.findView(mRootC, R.id.tablet_guest_picker_done_button);
		FontCache.setTypeface(doneButton, Font.ROBOTO_LIGHT);
		doneButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// Do something crazy
			}
		});

		mAdultMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				removeAdult();
			}
		});

		mAdultPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				addAdult();
			}
		});

		mChildMinus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				removeChild(mChildren.size() - 1);
			}
		});

		mChildPlus.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mChildren.size() < MAX_CHILDREN && canAddAnotherTraveler()) {
					addChild(10);
				}
			}
		});

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		bind();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList(STATE_CHILDREN, mChildren);
		outState.putInt(STATE_ADULT_COUNT, mAdultCount);
	}

	public void initializeGuests(int initialAdultCount, List<ChildTraveler> initialChildren) {
		mAdultCount = initialAdultCount;
		if (initialChildren == null) {
			mChildren = new ArrayList<ChildTraveler>();
			return;
		}
		mChildren = new ArrayList<ChildTraveler>(initialChildren.size());
		for (ChildTraveler c : initialChildren) {
			mChildren.add(c);
		}
	}

	public boolean canAddAnotherTraveler() {
		return mAdultCount + mChildren.size() < MAX_TRAVELERS;
	}

	public void addAdult() {
		if (mAdultCount < MAX_ADULTS && canAddAnotherTraveler()) {
			mAdultCount++;
			toggleInfantSeatingStates();
			mListener.onGuestsChanged(mAdultCount, mChildren, mInfantsInLaps);
			bind();
		}
	}

	public void removeAdult() {
		if (mAdultCount > 1) {
			mAdultCount--;
			toggleInfantSeatingStates();
			mListener.onGuestsChanged(mAdultCount, mChildren, mInfantsInLaps);
			bind();
		}
	}

	public void addChild(int age) {
		if (mChildren.size() < MAX_CHILDREN && canAddAnotherTraveler()) {
			mChildren.add(new ChildTraveler(age, false));
			toggleInfantSeatingStates();
			mListener.onGuestsChanged(mAdultCount, mChildren, mInfantsInLaps);
			bind();
		}
	}

	public void removeChild(int index) {
		if (index >= 0 && index < mChildren.size()) {
			mChildren.remove(index);
			toggleInfantSeatingStates();
			mListener.onGuestsChanged(mAdultCount, mChildren, mInfantsInLaps);
			bind();
		}
	}

	public void bind() {
		if (mRootC != null) {
			mAdultText.setText(getResources().getQuantityString(R.plurals.number_of_adults, mAdultCount, mAdultCount));
			mChildText.setText(getResources().getQuantityString(R.plurals.number_of_children, mChildren.size(), mChildren.size()));
			setHeaderString();
			GuestsPickerUtils.setChildSpinnerPositions(mRootC, mChildren);
			GuestsPickerUtils.showOrHideChildAgeSpinners(getActivity(), mChildren, mRootC, mChildAgeSelectedListener);
			mChildAgesLayout.setVisibility(getChildAgesVisibility());
		}
	}

	private int getChildAgesVisibility() {
		return mChildren.size() > 0 ? View.VISIBLE : View.INVISIBLE;
	}

	private final AdapterView.OnItemSelectedListener mChildAgeSelectedListener = new AdapterView.OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			GuestsPickerUtils.setChildrenFromSpinners(getActivity(), mChildAgesLayout, mChildren);
			GuestsPickerUtils.updateDefaultChildTravelers(getActivity(), mChildren);
			toggleInfantSeatingStates();
			mListener.onGuestsChanged(mAdultCount, mChildren, mInfantsInLaps);
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	};

	public void toggleInfantSeatingStates() {
		if (GuestsPickerUtils.moreInfantsThanAvailableLaps(mAdultCount, mChildren)) {
			mInfantAlertTextView.setVisibility(View.VISIBLE);
			mInfantsInLaps = false;
		}
		else {
			mInfantAlertTextView.setVisibility(View.GONE);
			mInfantsInLaps = true;
		}
	}

	private void setHeaderString() {
		StringBuilder sb = new StringBuilder();
		sb.append(mAdultText.getText());
		if (mChildren.size() > 0) {
			sb.append(", ");
			sb.append(mChildText.getText());
		}
		mHeaderTextView.setText(sb.toString());
	}

}
