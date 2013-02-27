package com.expedia.bookings.fragment;

import java.util.Collection;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.fragment.LoginFragment.LoginExtender;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.widget.ItineraryLoaderLoginExtender.LoginExtenderListener;
import com.mobiata.android.util.Ui;

public class ItineraryGuestAddFragment extends Fragment implements LoginExtenderListener {

	public static final String TAG = "ItineraryGuestAddDialogFragment";

	public static final String ARG_LOGIN_EXTENDER = "ARG_LOGIN_EXTENDER";
	public static final String STATE_LOGIN_EXTENDER = "STATE_LOGIN_EXTENDER";
	public static final String STATE_LOGIN_EXTENDER_RUNNING = "STATE_LOGIN_EXTENDER_RUNNING";

	private Button mFindItinBtn;
	private TextView mStatusMessageTv;
	private EditText mEmailEdit;
	private EditText mItinNumEdit;
	private ViewGroup mExtenderContainer;
	private LinearLayout mOuterContainer;
	private AddGuestItineraryDialogListener mListener;
	private LoginExtender mLoginExtender;
	private boolean mLoginExtenderRunning = false;

	public static ItineraryGuestAddFragment newInstance(LoginExtender extender) {
		ItineraryGuestAddFragment frag = new ItineraryGuestAddFragment();
		Bundle args = new Bundle();
		if (extender != null) {
			args.putParcelable(ARG_LOGIN_EXTENDER, extender);
		}
		frag.setArguments(args);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itinerary_add_guest_itin, container, false);

		if (getArguments().containsKey(ARG_LOGIN_EXTENDER)) {
			mLoginExtender = getArguments().getParcelable(ARG_LOGIN_EXTENDER);
			getArguments().remove(ARG_LOGIN_EXTENDER);
		}

		if (savedInstanceState != null) {
			mLoginExtenderRunning = savedInstanceState.getBoolean(STATE_LOGIN_EXTENDER_RUNNING, false);
			if (savedInstanceState.containsKey(STATE_LOGIN_EXTENDER)) {
				mLoginExtender = savedInstanceState.getParcelable(STATE_LOGIN_EXTENDER);
			}
		}

		mOuterContainer = Ui.findView(view, R.id.outer_container);
		mExtenderContainer = Ui.findView(view, R.id.login_extender_container);
		mStatusMessageTv = Ui.findView(view, R.id.itin_heading_textview);
		mFindItinBtn = Ui.findView(view, R.id.find_itinerary_button);
		mEmailEdit = Ui.findView(view, R.id.email_edit_text);
		mItinNumEdit = Ui.findView(view, R.id.itin_number_edit_text);

		FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mFindItinBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mEmailEdit, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mItinNumEdit, Font.ROBOTO_LIGHT);

		initOnClicks();

		return view;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mLoginExtender != null) {
			mLoginExtender.cleanUp();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mLoginExtender != null && mLoginExtenderRunning) {
			runExtenderOrFinish();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_LOGIN_EXTENDER, mLoginExtenderRunning);
	}

	private void initOnClicks() {
		mFindItinBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String emailAddr = mEmailEdit.getText().toString();
				String itinNumber = mItinNumEdit.getText().toString();
				if (mListener != null) {
					mListener.onFindItinClicked(emailAddr, itinNumber);
				}

				ItineraryManager.getInstance().addGuestTrip(emailAddr, itinNumber, true);
				runExtenderOrFinish();

				// TODO: move this to the listener
				OmnitureTracking.trackItinAdd(getActivity());
			}

		});
	}

	public void hideKeyboard() {
		View focused = this.getActivity().getCurrentFocus();
		if (focused instanceof EditText) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
		}
	}

	public void runExtenderOrFinish() {
		if (mLoginExtender != null) {
			enableExtenderState(true);
			mLoginExtender.onLoginComplete(getActivity(), this, mExtenderContainer);
		}
		else {
			getActivity().finish();
		}
	}

	public void enableExtenderState(boolean enabled) {
		if (enabled) {
			mFindItinBtn.setVisibility(View.GONE);
			mEmailEdit.setEnabled(false);
			mItinNumEdit.setEnabled(false);
			hideKeyboard();
			mOuterContainer.setGravity(Gravity.CENTER);
			mExtenderContainer.setVisibility(View.VISIBLE);
		}
		else {
			mFindItinBtn.setVisibility(View.VISIBLE);
			mEmailEdit.setEnabled(true);
			mItinNumEdit.setEnabled(true);
			mOuterContainer.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
			mExtenderContainer.setVisibility(View.GONE);
		}
	}

	public void setListener(AddGuestItineraryDialogListener listener) {
		mListener = listener;
	}

	@Override
	public void loginExtenderWorkComplete(LoginExtender extender) {
		//We look in the itin manager and check if our new one is present.
		String itinNumber = mItinNumEdit.getText().toString();
		Collection<Trip> trips = ItineraryManager.getInstance().getTrips();
		for (Trip trip : trips) {
			if (trip.isGuest() && trip.getTripNumber() != null
					&& trip.getTripNumber().trim().equalsIgnoreCase(itinNumber.trim())) {
				getActivity().finish();
			}
		}
		mStatusMessageTv.setText(R.string.itinerary_fetch_error);
		enableExtenderState(false);
	}

	public interface AddGuestItineraryDialogListener {
		public void onFindItinClicked(String email, String itinNumber);

		public void onCancel();
	}
}
