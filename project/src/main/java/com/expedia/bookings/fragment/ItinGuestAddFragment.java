package com.expedia.bookings.fragment;

import java.util.Collection;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.expedia.bookings.BuildConfig;
import com.expedia.bookings.R;
import com.expedia.bookings.data.trips.ItineraryManager;
import com.expedia.bookings.data.trips.Trip;
import com.expedia.bookings.interfaces.LoginExtenderListener;
import com.expedia.bookings.text.HtmlCompat;
import com.expedia.bookings.tracking.OmnitureTracking;
import com.expedia.bookings.utils.FocusViewRunnable;
import com.expedia.bookings.utils.FontCache;
import com.expedia.bookings.utils.FontCache.Font;
import com.expedia.bookings.utils.LoginExtender;
import com.mobiata.android.util.Ui;
import com.squareup.phrase.Phrase;

public class ItinGuestAddFragment extends Fragment implements LoginExtenderListener {

	public static final String TAG = "ItineraryGuestAddDialogFragment";

	public static final String ARG_LOGIN_EXTENDER = "ARG_LOGIN_EXTENDER";
	public static final String STATE_LOGIN_EXTENDER = "STATE_LOGIN_EXTENDER";
	public static final String STATE_LOGIN_EXTENDER_RUNNING = "STATE_LOGIN_EXTENDER_RUNNING";
	public static final String STATE_STATUS_TEXT = "STATE_HEADER_TEXT";
	public static final String isFetchGuestItinFailed = "isFetchGuestItinFailed";
	public static final String isFetchGuestRegisterUserItinFailed = "isFetchGuestRegisterUserItinFailed";

	private Button mFindItinBtn;
	private TextView mStatusMessageTv;
	private EditText mEmailEdit;
	private EditText mItinNumEdit;
	private ViewGroup mExtenderContainer;
	private TextView mUnableToFindItinErrorMsg;
	private LinearLayout mOuterContainer;
	private AddGuestItineraryDialogListener mListener;
	private LoginExtender mLoginExtender;
	private boolean mLoginExtenderRunning = false;
	private String mStatusText;

	public static ItinGuestAddFragment newInstance(LoginExtender extender) {
		ItinGuestAddFragment frag = new ItinGuestAddFragment();
		Bundle args = new Bundle();
		if (extender != null) {
			args.putBundle(STATE_LOGIN_EXTENDER, extender.buildStateBundle());
		}
		frag.setArguments(args);
		return frag;
	}

	public static ItinGuestAddFragment fetchingGuestItinFailedInstance(LoginExtender extender) {
		ItinGuestAddFragment frag = newInstance(extender);
		Bundle arguments = frag.getArguments();
		arguments.putBoolean(isFetchGuestItinFailed, true);
		frag.setArguments(arguments);
		return frag;
	}

	public static ItinGuestAddFragment fetchingRegisteredUserItinFailedInstance(LoginExtender extender) {
		ItinGuestAddFragment frag = newInstance(extender);
		Bundle arguments = frag.getArguments();
		arguments.putBoolean(isFetchGuestRegisterUserItinFailed, true);
		frag.setArguments(arguments);
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_itinerary_add_guest_itin, container, false);

		if (getArguments().containsKey(ARG_LOGIN_EXTENDER)) {
			mLoginExtender = LoginExtender.buildLoginExtenderFromState(getArguments().getBundle(ARG_LOGIN_EXTENDER));
			getArguments().remove(ARG_LOGIN_EXTENDER);
		}

		if (savedInstanceState != null) {
			mLoginExtenderRunning = savedInstanceState.getBoolean(STATE_LOGIN_EXTENDER_RUNNING, false);
			if (savedInstanceState.containsKey(STATE_LOGIN_EXTENDER)) {
				mLoginExtender = LoginExtender
					.buildLoginExtenderFromState(savedInstanceState.getBundle(STATE_LOGIN_EXTENDER));
			}
			if (savedInstanceState.containsKey(STATE_STATUS_TEXT)) {
				mStatusText = savedInstanceState.getString(STATE_STATUS_TEXT);
			}
		}

		mOuterContainer = Ui.findView(view, R.id.outer_container);
		mExtenderContainer = Ui.findView(view, R.id.login_extender_container);
		mUnableToFindItinErrorMsg = Ui.findView(view, R.id.unable_to_find_itin_error_message);
		mStatusMessageTv = Ui.findView(view, R.id.itin_heading_textview);
		mFindItinBtn = Ui.findView(view, R.id.find_itinerary_button);
		mEmailEdit = Ui.findView(view, R.id.email_edit_text);
		mItinNumEdit = Ui.findView(view, R.id.itin_number_edit_text);

		FontCache.setTypeface(mStatusMessageTv, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mFindItinBtn, Font.ROBOTO_REGULAR);
		FontCache.setTypeface(mEmailEdit, Font.ROBOTO_LIGHT);
		FontCache.setTypeface(mItinNumEdit, Font.ROBOTO_LIGHT);

		mFindItinBtn.setEnabled(false);
		mEmailEdit.addTextChangedListener(mTextWatcher);
		mItinNumEdit.addTextChangedListener(mTextWatcher);

		initOnClicks();

		if (!TextUtils.isEmpty(mStatusText)) {
			setStatusText(mStatusText);
		}

		if (getArguments().containsKey(isFetchGuestItinFailed)) {
			getArguments().remove(isFetchGuestItinFailed);
			mUnableToFindItinErrorMsg.setText(getString(R.string.unable_to_find_guest_itinerary));
			mUnableToFindItinErrorMsg.setVisibility(View.VISIBLE);
		}

		if (getArguments().containsKey(isFetchGuestRegisterUserItinFailed)) {
			getArguments().remove(isFetchGuestRegisterUserItinFailed);
			String errorMsg = Phrase.from(getString(R.string.unable_to_find_registered_user_itinerary_template))
				.put("brand",
					BuildConfig.brand).format().toString();
			mUnableToFindItinErrorMsg.setText(errorMsg);
			mUnableToFindItinErrorMsg.setVisibility(View.VISIBLE);
		}

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
		else {
			View focused = this.getView().findFocus();
			if (focused == null || !(focused instanceof EditText)) {
				focused = mEmailEdit;
			}
			if (focused != null && focused instanceof EditText) {
				FocusViewRunnable.focusView(this, focused);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_LOGIN_EXTENDER_RUNNING, mLoginExtenderRunning);
		if (mLoginExtender != null) {
			outState.putBundle(STATE_LOGIN_EXTENDER, mLoginExtender.buildStateBundle());
		}
		if (mStatusText != null) {
			outState.putString(STATE_STATUS_TEXT, mStatusText);
		}
	}

	private void initOnClicks() {
		mFindItinBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (hasFormData()) {
					setStatusText(getString(R.string.enter_itinerary_details));

					String emailAddr = mEmailEdit.getText().toString();
					String itinNumber = mItinNumEdit.getText().toString();
					if (mListener != null) {
						mListener.onFindItinClicked(emailAddr, itinNumber);
					}

					ItineraryManager.getInstance().addGuestTrip(emailAddr, itinNumber);
					runExtenderOrFinish();
					OmnitureTracking.setPendingManualAddGuestItin(emailAddr, itinNumber);
				}
			}

		});
	}

	private boolean hasFormData() {
		boolean hasEmail = mEmailEdit.getText() != null && mEmailEdit.getText().length() > 0;
		boolean hasItin = mItinNumEdit.getText() != null && mItinNumEdit.getText().length() > 0;
		return hasEmail && hasItin;
	}

	public void runExtenderOrFinish() {
		if (mLoginExtender != null) {
			mLoginExtenderRunning = true;
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
			Ui.hideKeyboardIfEditText(getActivity());
			mOuterContainer.setGravity(Gravity.TOP);
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
		boolean tripAdded = false;
		for (Trip trip : trips) {
			if (trip.isGuest() && trip.getTripNumber() != null
				&& trip.getTripNumber().trim().equalsIgnoreCase(itinNumber.trim())) {
				tripAdded = true;
				break;
			}
		}

		if (tripAdded) {
			getActivity().finish();
		}
		else {
			setStatusText(R.string.itinerary_fetch_error);
			enableExtenderState(false);
			mLoginExtenderRunning = false;
		}
	}

	protected void setStatusText(int textResId) {
		setStatusText(getString(textResId));
	}

	protected void setStatusText(final String text) {
		Runnable runner = new Runnable() {
			@Override
			public void run() {
				if (mStatusMessageTv != null) {
					mStatusMessageTv.setText(HtmlCompat.fromHtml(text));
				}
			}
		};
		mStatusText = text;
		if (getActivity() != null && this.isAdded()) {
			getActivity().runOnUiThread(runner);
		}
	}

	@Override
	public void setExtenderStatus(String status) {
		setStatusText(status);
	}

	private TextWatcher mTextWatcher = new TextWatcher() {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Don't care
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// Don't care
		}

		@Override
		public void afterTextChanged(Editable s) {
			if (hasFormData()) {
				mFindItinBtn.setEnabled(true);
			}
			else {
				mFindItinBtn.setEnabled(false);
			}
		}
	};

	public interface AddGuestItineraryDialogListener {
		void onFindItinClicked(String email, String itinNumber);

		void onCancel();
	}
}
