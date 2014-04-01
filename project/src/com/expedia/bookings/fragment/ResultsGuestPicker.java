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

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.widget.RingedCountView;
import com.mobiata.android.util.Ui;

/**
 * Results loading fragment for Tablet
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsGuestPicker extends Fragment {

	private static final String STATE_ADULT_COUNT = "STATE_ADULT_COUNT";
	private static final String STATE_CHILDREN = "STATE_CHILDREN";

	public static final int MAX_ADULTS = 6;
	public static final int MAX_CHILDREN = 6;

	private ViewGroup mRootC;
	private RingedCountView mAdultRing;
	private RingedCountView mChildRing;
	private View mAdultMinus;
	private View mAdultPlus;
	private View mChildMinus;
	private View mChildPlus;

	private int mAdultCount;
	private ArrayList<ChildTraveler> mChildren = new ArrayList<ChildTraveler>();

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
		mAdultRing = Ui.findView(mRootC, R.id.adults_ring);
		mAdultMinus = Ui.findView(mRootC, R.id.adults_minus);
		mAdultPlus = Ui.findView(mRootC, R.id.adults_plus);

		mChildRing = Ui.findView(mRootC, R.id.children_ring);
		mChildMinus = Ui.findView(mRootC, R.id.children_minus);
		mChildPlus = Ui.findView(mRootC, R.id.children_plus);

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
				//TODO: WE NEED A WAY TO DETERMINE CHILD AGE
				addChild(10);
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

	private void initializeGuests(int initialAdultCount, List<ChildTraveler> initialChildren) {
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

	public void addAdult() {
		if (mAdultCount < MAX_ADULTS) {
			mAdultCount++;
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void removeAdult() {
		if (mAdultCount > 1) {
			mAdultCount--;
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void addChild(int age) {
		if (mChildren.size() < MAX_CHILDREN) {
			mChildren.add(new ChildTraveler(age, false));
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void removeChild(int index) {
		if (index >= 0 && index < mChildren.size()) {
			mChildren.remove(index);
			mListener.onGuestsChanged(mAdultCount, mChildren);
			bind();
		}
	}

	public void bind() {
		if (mRootC != null) {
			mAdultRing.setCaption(
				getResources().getQuantityString(R.plurals.number_of_adults_TEMPLATE, mAdultCount, mAdultCount));
			mAdultRing.animateTo(mAdultCount, mAdultCount / (float) MAX_ADULTS);

			mChildRing.setCaption(
				getResources().getQuantityString(R.plurals.number_of_children, mChildren.size(), mChildren.size()));
			mChildRing.animateTo(mChildren.size(), mChildren.size() / (float) MAX_CHILDREN);
		}
	}

}
