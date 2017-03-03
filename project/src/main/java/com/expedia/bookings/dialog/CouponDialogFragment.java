package com.expedia.bookings.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

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
	public void onAttach(Context context) {
		super.onAttach(context);

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
		View view = Ui.inflate(getActivity(), R.layout.dialog_coupon, null);
		mProgressContainer = Ui.findView(view, R.id.progress_container);
		mCouponEditText = Ui.findView(view, R.id.coupon_edit_text);

		mCouponEditText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// According to docs, this is only non-null if user clicked "enter"
				if (event != null || actionId == EditorInfo.IME_ACTION_DONE) {
					applyCoupon();
					return true;
				}

				return false;
			}
		});

		// TODO: Add a theme that works back to v8
		ContextThemeWrapper context;
		if (ExpediaBookingApp.useTabletInterface()) {
			// Tablet
			context = new ContextThemeWrapper(getActivity(), R.style.V2_Dialog_Coupon_Tablet);
		}
		else {
			context = new ContextThemeWrapper(getActivity(), R.style.V2_Dialog_Coupon_Phone);
		}
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

		Dialog dialog = builder.create();

		// #2083: Show keyboard whenever the coupon displays
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		AlertDialog dialog = (AlertDialog) getDialog();
		mApplyButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
		mApplyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				applyCoupon();
			}
		});

		updateViews();
	}

	@Override
	public void onResume() {
		super.onResume();

		mCouponEditText.addTextChangedListener(mTextWatcher);
	}

	@Override
	public void onPause() {
		super.onPause();

		mCouponEditText.removeTextChangedListener(mTextWatcher);
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

	private void applyCoupon() {
		String couponCode = mCouponEditText.getText().toString();
		if (!TextUtils.isEmpty(couponCode)) {
			mListener.onApplyCoupon(couponCode);
			mIsApplying = true;
			updateViews();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Listener

	public interface CouponDialogFragmentListener {
		void onApplyCoupon(String code);

		void onCancelApplyCoupon();
	}
}
