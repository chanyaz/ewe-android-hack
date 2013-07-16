package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;

public class CouponCodeWidget {
	private Context mContext;

	private TextView mCouponCodeCollapsedText;
	private View mCouponContainer;
	private EditText mCouponCode;
	private TextView mApply;
	private View mProgressBar;
	private TextView mNewTotal;

	private boolean mCollapsed = true;
	private boolean mProgressShowing = false;
	private boolean mUseNewTotal = false;

	private int mErrorResId = 0;

	private CouponCodeWidgetListener mListener;
	private View mFieldAboveCouponCode;
	private View mFieldBelowCouponCode;
	private int mFieldAboveCouponCodeId;
	private int mFieldBelowCouponCodeId;

	private static final String KEY_COLLAPSED = "KEY_COUPON_COLLAPSED";
	private static final String KEY_TEXT_EMPTY = "KEY_COUPON_TEXT_EMPTY";
	private static final String KEY_USE_NEW_TOTAL = "KEY_COUPON_USE_NEW_TOTAL";
	private static final String KEY_PROGRESS_SHOWING = "KEY_COUPON_PROGRESS_SHOWING";
	private static final String KEY_ERROR = "KEY_COUPON_ERROR";

	public CouponCodeWidget(Context context, View rootView) {
		mContext = context;

		mCouponCodeCollapsedText = (TextView) rootView.findViewById(R.id.coupon_code_collapsed_text);
		mCouponContainer = rootView.findViewById(R.id.coupon_code_container);
		mCouponCode = (EditText) rootView.findViewById(R.id.coupon_code_edittext);
		mApply = (TextView) rootView.findViewById(R.id.apply_button);
		mProgressBar = rootView.findViewById(R.id.coupon_progress_bar);
		mNewTotal = (TextView) rootView.findViewById(R.id.new_total_textview);

		mCouponCodeCollapsedText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCollapsed = false;
				update();
				mCouponCode.clearFocus();
				focusAndOpenKeyboard(mContext, mCouponCode);
			}
		});

		mApply.setEnabled(false);
		mApply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onApplyClicked();
			}
		});
	}

	private final TextWatcher mCouponWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			// Do nothing
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			// Do nothing
		}

		@Override
		public void afterTextChanged(Editable s) {
			mApply.setEnabled(!TextUtils.isEmpty(s));

			mListener.onCouponCodeChanged(s.toString());
		}
	};

	private void update() {
		if (mCollapsed) {
			mCouponCodeCollapsedText.setVisibility(View.VISIBLE);
			mCouponContainer.setVisibility(View.GONE);

			if (mFieldAboveCouponCode != null && mFieldBelowCouponCode != null) {
				mFieldAboveCouponCode.setNextFocusDownId(mFieldBelowCouponCodeId);
				mFieldAboveCouponCode.setNextFocusRightId(mFieldBelowCouponCodeId);

				mCouponCode.setEnabled(false);
				mCouponCode.setFocusable(false);
				mCouponCode.setFocusableInTouchMode(false);
				mCouponCode.setNextFocusUpId(0);
				mCouponCode.setNextFocusLeftId(0);

				mFieldBelowCouponCode.setNextFocusUpId(mFieldAboveCouponCodeId);
				mFieldBelowCouponCode.setNextFocusLeftId(mFieldAboveCouponCodeId);
			}
			return;
		}
		else {
			mCouponCodeCollapsedText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			mCouponCodeCollapsedText.setTextColor(mContext.getResources().getColor(R.color.text_dark));
			mCouponContainer.setVisibility(View.VISIBLE);

			if (mFieldAboveCouponCode != null && mFieldBelowCouponCode != null) {
				mFieldAboveCouponCode.setNextFocusDownId(R.id.coupon_code_edittext);
				mFieldAboveCouponCode.setNextFocusRightId(R.id.coupon_code_edittext);

				mCouponCode.setEnabled(true);
				mCouponCode.setFocusable(true);
				mCouponCode.setFocusableInTouchMode(true);
				mCouponCode.setNextFocusUpId(mFieldAboveCouponCodeId);
				mCouponCode.setNextFocusLeftId(mFieldAboveCouponCodeId);
				mCouponCode.setNextFocusDownId(mFieldBelowCouponCodeId);
				mCouponCode.setNextFocusRightId(mFieldBelowCouponCodeId);

				mFieldBelowCouponCode.setNextFocusUpId(R.id.coupon_code_edittext);
				mFieldBelowCouponCode.setNextFocusLeftId(R.id.coupon_code_edittext);
			}
		}

		if (mErrorResId != 0) {
			mProgressBar.setVisibility(View.GONE);
			mApply.setVisibility(View.GONE);
			mNewTotal.setVisibility(View.VISIBLE);
			mNewTotal.setText(mErrorResId);
		}
		else if (mProgressShowing) {
			mProgressBar.setVisibility(View.VISIBLE);
			mApply.setVisibility(View.GONE);
			mNewTotal.setVisibility(View.GONE);
		}
		else if (mUseNewTotal) {
			mProgressBar.setVisibility(View.GONE);
			mApply.setVisibility(View.GONE);
			mNewTotal.setVisibility(View.VISIBLE);

			CreateTripResponse response = Db.getHotelSearch().getCreateTripResponse();
			Money m = response.getNewRate().getDisplayTotalPrice();
			mNewTotal.setText(mContext.getString(R.string.new_total) + "\n" + m.getFormattedMoney());
		}
		else {
			mProgressBar.setVisibility(View.GONE);
			mApply.setVisibility(View.VISIBLE);
			mNewTotal.setVisibility(View.GONE);
		}
	}

	public void saveInstanceState(Bundle outState) {
		if (outState != null) {
			outState.putBoolean(KEY_COLLAPSED, mCollapsed);
			outState.putBoolean(KEY_TEXT_EMPTY, TextUtils.isEmpty(mCouponCode.getText()));
			outState.putBoolean(KEY_USE_NEW_TOTAL, mUseNewTotal);
			outState.putBoolean(KEY_PROGRESS_SHOWING, mProgressShowing);
			outState.putInt(KEY_ERROR, mErrorResId);
		}
	}

	public void restoreInstanceState(Bundle inState) {
		if (inState != null) {
			mCollapsed = inState.getBoolean(KEY_COLLAPSED, true);
			mUseNewTotal = inState.getBoolean(KEY_USE_NEW_TOTAL, false);
			mProgressShowing = inState.getBoolean(KEY_PROGRESS_SHOWING, false);
			mErrorResId = inState.getInt(KEY_ERROR, 0);

			mApply.setEnabled(!inState.getBoolean(KEY_TEXT_EMPTY, true));
		}
		update();
	}

	public void startTextWatcher() {
		mCouponCode.addTextChangedListener(mCouponWatcher);
	}

	public void setListener(CouponCodeWidgetListener listener) {
		mListener = listener;
	}

	public void setFieldAboveCouponCode(View v, int id) {
		mFieldAboveCouponCode = v;
		mFieldAboveCouponCodeId = id;
	}

	public void setFieldBelowCouponCode(View v, int id) {
		mFieldBelowCouponCode = v;
		mFieldBelowCouponCodeId = id;
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void resetState() {
		mUseNewTotal = false;
		mProgressShowing = false;
		mErrorResId = 0;
		update();
	}

	public void setLoading(boolean enabled) {
		mProgressShowing = enabled;
		update();
	}

	public void onApplyCoupon(Rate newRate) {
		mUseNewTotal = true;
		update();
	}

	public void onApplyCouponError(List<ServerError> errors) {
		mErrorResId = R.string.coupon_error;
		if (errors != null) {
			boolean isExpired = false;
			boolean isInvalid = false;
			for (ServerError error : errors) {
				switch (error.getErrorCode()) {
				case INVALID_INPUT_COUPON_CODE:
					isExpired = true;
					break;
				case INVALID_INPUT:
				case APPLY_COUPON_ERROR:
					isInvalid = true;
					break;
				}
			}

			if (isInvalid) {
				mErrorResId = R.string.invalid_coupon;
			}
			else if (isExpired) {
				mErrorResId = R.string.expired_coupon;
			}
		}

		update();
	}

	private static void focusAndOpenKeyboard(Context context, View view) {
		view.requestFocus();
		InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(view, 0);
	}

	//////////////////////////////////////////////////////////////////////////
	// Interface

	public interface CouponCodeWidgetListener {
		public void onCouponCodeChanged(String couponCode);

		public void onApplyClicked();
	}
}
