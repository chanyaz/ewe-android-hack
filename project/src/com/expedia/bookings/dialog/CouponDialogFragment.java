package com.expedia.bookings.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.expedia.bookings.R;
import com.expedia.bookings.activity.ExpediaBookingApp;
import com.expedia.bookings.section.AfterChangeTextWatcher;
import com.expedia.bookings.utils.Ui;

/**
 * Shows a coupon entry dialog.
 * 
 * Currently this acts as a very dumb Dialog.  It does not do the applying
 * of the coupon itself.  If one wanted, they could set it up to work that
 * way eventually.
 */
public class CouponDialogFragment extends DialogFragment {

	public static final String TAG = CouponDialogFragment.class.toString();

	private static final String INSTANCE_IS_APPLYING = "INSTANCE_IS_APPLYING";

	private CouponDialogFragmentListener mListener;

	private ViewGroup mProgressContainer;
	private EditText mCouponEditText;
	private Button mApplyButton;

	private boolean mIsApplying;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		mListener = Ui.findFragmentListener(this, CouponDialogFragmentListener.class);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			mIsApplying = savedInstanceState.getBoolean(INSTANCE_IS_APPLYING);
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Create the core View to display
		View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_coupon, null);
		mProgressContainer = Ui.findView(view, R.id.progress_container);
		mCouponEditText = Ui.findView(view, R.id.coupon_edit_text);
		mCouponEditText.addTextChangedListener(mTextWatcher);

		//1753. VSC Default to all caps character.
		if (ExpediaBookingApp.IS_VSC) {
			mCouponEditText.setInputType(InputType.TYPE_TEXT_VARIATION_FILTER
					| InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		}

		// TODO: Add a theme that works back to v8
		ContextThemeWrapper context = new ContextThemeWrapper(getActivity(),
				android.R.style.Theme_Holo_Dialog);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		builder.setTitle(R.string.enter_coupon_code);
		builder.setView(view);
		builder.setPositiveButton(R.string.apply, null);
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onCancelApplyCoupon();
			}
		});

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
				mListener.onApplyCoupon(mCouponEditText.getText().toString());
				mIsApplying = true;
				updateViews();
			}
		});

		updateViews();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(INSTANCE_IS_APPLYING, mIsApplying);
	}

	public void updateViews() {
		mProgressContainer.setVisibility(mIsApplying ? View.VISIBLE : View.GONE);
		mCouponEditText.setEnabled(!mIsApplying);
		mApplyButton.setEnabled(!mIsApplying && !TextUtils.isEmpty(mCouponEditText.getText()));
	}

	private TextWatcher mTextWatcher = new AfterChangeTextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			updateViews();
		}

	};

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface CouponDialogFragmentListener {
		public void onApplyCoupon(String code);

		public void onCancelApplyCoupon();
	}
}
