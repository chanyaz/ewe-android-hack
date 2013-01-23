package com.expedia.bookings.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class ItineraryGuestAddDialogFragment extends DialogFragment {

	public static final String TAG = "ItineraryGuestAddDialogFragment";

	private View mFindItinBtn;
	private View mCancelBtn;
	private EditText mEmailEdit;
	private EditText mItinNumEdit;
	private AddGuestItineraryDialogListener mListener;

	public static ItineraryGuestAddDialogFragment newInstance() {
		return new ItineraryGuestAddDialogFragment();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.fragment_add_guest_itinerary, null);

		mFindItinBtn = Ui.findView(view, R.id.find_itinerary_button);
		mCancelBtn = Ui.findView(view, R.id.cancel_button);
		mEmailEdit = Ui.findView(view, R.id.email_edit_text);
		mItinNumEdit = Ui.findView(view, R.id.itin_number_edit_text);

		initOnClicks();

		Dialog dialog = new Dialog(getActivity(), R.style.ExpediaLoginDialog);
		dialog.requestWindowFeature(STYLE_NO_TITLE);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);
		dialog.setContentView(view);

		return dialog;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (isAdded()) {
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		}

		super.onDismiss(dialog);
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
				ItineraryGuestAddDialogFragment.this.dismissAllowingStateLoss();
			}

		});
		mCancelBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mListener != null)
				{
					mListener.onCancel();
				}
				ItineraryGuestAddDialogFragment.this.dismissAllowingStateLoss();
			}
		});
	}

	public void setListener(AddGuestItineraryDialogListener listener) {
		mListener = listener;
	}

	public interface AddGuestItineraryDialogListener {
		public void onFindItinClicked(String email, String itinNumber);

		public void onCancel();
	}

}
