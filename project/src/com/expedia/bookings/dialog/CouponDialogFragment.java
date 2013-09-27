package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.Ui;

public class CouponDialogFragment extends DialogFragment {

	public static final String TAG = CouponDialogFragment.class.toString();

	private ViewGroup mProgressContainer;
	private EditText mCouponEditText;
	private Button mApplyButton;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Create the core View to display
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_coupon, null);
		mProgressContainer = Ui.findView(view, R.id.progress_container);
		mCouponEditText = Ui.findView(view, R.id.coupon_edit_text);

		// TODO: Add a theme that works back to v8
		ContextThemeWrapper context = new ContextThemeWrapper(getActivity(),
				android.R.style.Theme_Holo_Dialog);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(R.string.enter_coupon_code);
		builder.setView(view);
		builder.setPositiveButton(R.string.apply, null);
		builder.setNegativeButton(R.string.cancel, null);

		return builder.create();
	}

	@Override
	public void onStart() {
		super.onStart();

		AlertDialog dialog = (AlertDialog) getDialog();
		mApplyButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		mApplyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setApplying(true);
			}
		});
	}

	public void setApplying(boolean isApplying) {
		mProgressContainer.setVisibility(isApplying ? View.VISIBLE : View.GONE);
		mCouponEditText.setEnabled(!isApplying);
		mApplyButton.setEnabled(!isApplying);
	}
}
