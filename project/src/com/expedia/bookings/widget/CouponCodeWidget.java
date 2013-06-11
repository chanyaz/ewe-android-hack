package com.expedia.bookings.widget;

import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.data.Rate;
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.utils.BookingInfoUtils;

public class CouponCodeWidget {
	private Context mContext;

	private TextView mCouponCodeCollapsedText;
	private View mCouponContainer;
	private EditText mCouponCode;
	private TextView mApply;
	private View mProgressBar;
	private TextView mNewTotal;

	private boolean mCollapsed = true;
	private boolean mTextEmpty = true;
	private boolean mProgressShowing = false;
	private boolean mUseNewTotal = false;
	private boolean mError = false;

	// This variable represents when the user has clicked "apply" and
	// then not edited the coupon afterwards; the application may or
	// may not have been successful in the end, but if someone else
	// changes the text we should clear existing coupons.
	private boolean mTriedToApplyCoupon = false;

	private CouponCodeWidgetListener mListener;
	private View mFieldAboveCouponCode;
	private View mFieldBelowCouponCode;
	private int mFieldAboveCouponCodeId;
	private int mFieldBelowCouponCodeId;

	private static final String KEY_COLLAPSED = "KEY_COUPON_COLLAPSED";
	private static final String KEY_TEXT_EMPTY = "KEY_COUPON_TEXT_EMPTY";
	private static final String KEY_TRIED_TO_APPLY_COUPON = "KEY_TRIED_TO_APPLY_COUPON";
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
				BookingInfoUtils.focusAndOpenKeyboard(mContext, mCouponCode);
			}
		});

		mApply.setEnabled(false);
		mApply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mTriedToApplyCoupon = true;
				mListener.onApplyClicked();
			}
		});
	}

	private final TextWatcher couponWatcher = new TextWatcher() {
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
			mApply.setEnabled(!(mTextEmpty = TextUtils.isEmpty(s)));

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

		if (mError) {
			mProgressBar.setVisibility(View.GONE);
			mApply.setVisibility(View.GONE);
			mNewTotal.setVisibility(View.VISIBLE);
			mNewTotal.setText(mContext.getString(R.string.coupon_error));
			return;
		}

		if (mTriedToApplyCoupon) {
			if (mProgressShowing) {
				mProgressBar.setVisibility(View.VISIBLE);
				mApply.setVisibility(View.GONE);
				mNewTotal.setVisibility(View.GONE);
			}
			else if (mUseNewTotal) {
				mProgressBar.setVisibility(View.GONE);
				mApply.setVisibility(View.GONE);
				mNewTotal.setVisibility(View.VISIBLE);
				setNewTotal();
			}
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
			outState.putBoolean(KEY_TEXT_EMPTY, mTextEmpty);
			outState.putBoolean(KEY_TRIED_TO_APPLY_COUPON, mTriedToApplyCoupon);
			outState.putBoolean(KEY_USE_NEW_TOTAL, mUseNewTotal);
			outState.putBoolean(KEY_PROGRESS_SHOWING, mProgressShowing);
			outState.putBoolean(KEY_ERROR, mError);
		}
	}

	public void restoreInstanceState(Bundle inState) {
		if (inState != null) {
			mCollapsed = inState.getBoolean(KEY_COLLAPSED, true);
			mTextEmpty = inState.getBoolean(KEY_TEXT_EMPTY, true);
			mTriedToApplyCoupon = inState.getBoolean(KEY_TRIED_TO_APPLY_COUPON, false);
			mUseNewTotal = inState.getBoolean(KEY_USE_NEW_TOTAL, false);
			mProgressShowing = inState.getBoolean(KEY_PROGRESS_SHOWING, false);
			mError = inState.getBoolean(KEY_ERROR, false);
			mApply.setEnabled(!mTextEmpty);
		}
		update();
	}

	public void startTextWatcher() {
		mCouponCode.addTextChangedListener(couponWatcher);
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

	private void setNewTotal() {
		CreateTripResponse response = Db.getCreateTripResponse();
		if (response != null) {
			Money m = response.getNewRate().getDisplayTotalPrice();
			mNewTotal.setText(mContext.getString(R.string.new_total) + "\n" + m.getFormattedMoney());
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Fragment control

	public void resetState() {
		mTriedToApplyCoupon = false;
		mUseNewTotal = false;
		mProgressShowing = false;
		mError = false;
		update();
	}

	public void setLoading(boolean enabled) {
		mProgressShowing = enabled;
		update();
	}

	public void onApplyCoupon(Rate newRate) {
		mUseNewTotal = true;
		setNewTotal();
		update();
	}

	public void onApplyCouponError(List<ServerError> errors) {
		mError = true;

		int errorResId = R.string.coupon_error;
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
				errorResId = R.string.invalid_coupon;
			}
			else if (isExpired) {
				errorResId = R.string.expired_coupon;
			}
		}

		mNewTotal.setText(errorResId);
	}

	//////////////////////////////////////////////////////////////////////////
	// Interface

	public interface CouponCodeWidgetListener {
		public void onCouponCodeChanged(String couponCode);

		public void onApplyClicked();
	}
}
