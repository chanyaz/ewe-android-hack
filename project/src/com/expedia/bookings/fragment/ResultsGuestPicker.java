package com.expedia.bookings.fragment;

import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.ChildTraveler;
import com.expedia.bookings.data.pos.PointOfSale;
import com.expedia.bookings.widget.GuestPicker;
import com.mobiata.android.util.Ui;

/**
 * Results loading fragment for Tablet
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ResultsGuestPicker extends Fragment implements GuestPicker.GuestPickerListener {

	private ViewGroup mRootC;
	private GuestPicker mGuestPicker;

	private TextView mInfantAlertTextView;

	private int mInitialAdultCount;
	private List<ChildTraveler> mInitialChildren;

	private GuestPickerFragmentListener mListener;

	public interface GuestPickerFragmentListener {
		public void onGuestsChanged(int numAdults, List<ChildTraveler> children, boolean infantsInLaps);
		public void onGuestsChanged(int numAdults, List<ChildTraveler> children);
	}

	public static ResultsGuestPicker newInstance(int initialAdultCount, List<ChildTraveler> initialChildren) {
		ResultsGuestPicker frag = new ResultsGuestPicker();
		frag.initializeGuests(initialAdultCount, initialChildren);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mListener = Ui.findFragmentListener(this, GuestPickerFragmentListener.class);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mRootC = Ui.inflate(inflater, R.layout.fragment_results_guests, null);
		mGuestPicker = Ui.findView(mRootC, R.id.guest_picker);
		mGuestPicker.setListener(this);

		if (mInitialChildren != null) {
			initializeGuests(mInitialAdultCount, mInitialChildren);
			mInitialAdultCount = -1;
			mInitialChildren = null;
		}

		mInfantAlertTextView = Ui.findView(mRootC, R.id.tablet_lap_infant_alert);

		return mRootC;
	}

	@Override
	public void onResume() {
		super.onResume();
		bind();
	}

	@Override
	public void onGuestsChanged(int numAdults, List<ChildTraveler> children) {
		toggleInfantSeatingStates();
		mListener.onGuestsChanged(numAdults, children);
	}

	public void initializeGuests(int initialAdultCount, List<ChildTraveler> initialChildren) {
		if (mGuestPicker != null) {
			mGuestPicker.initializeGuests(initialAdultCount, initialChildren);
		}
		else {
			mInitialAdultCount = initialAdultCount;
			mInitialChildren = initialChildren;
		}
	}

	public void bind() {
		if (mRootC != null) {
			mGuestPicker.bind();
		}
	}

	public void toggleInfantSeatingStates() {
		// Let's not show the alert if flights is not supported for the current POS
		if (mGuestPicker.moreInfantsThanAvailableLaps() && PointOfSale.getPointOfSale().isFlightSearchEnabledTablet()) {
			mInfantAlertTextView.setVisibility(View.VISIBLE);
		}
		else {
			mInfantAlertTextView.setVisibility(View.GONE);
		}
	}

	public String getHeaderString() {
		if (mGuestPicker == null) {
			return null;
		}
		return mGuestPicker.getHeaderString();
	}

}
