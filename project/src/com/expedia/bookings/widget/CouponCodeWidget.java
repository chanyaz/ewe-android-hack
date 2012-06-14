package com.expedia.bookings.widget;

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
import com.expedia.bookings.data.ServerError;
import com.expedia.bookings.server.ExpediaServices;
import com.expedia.bookings.utils.LocaleUtils;

import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;

public class CouponCodeWidget {
	private Context mContext;

	private EditText mCouponCode;
	private TextView mApply;
	private View mProgressBar;
	private TextView mNewTotal;

	private boolean mTextEmpty = true;
	private boolean mApplyClicked = false;
	private boolean mProgressShowing = false;
	private boolean mUseNewTotal = false;
	private boolean mError = false;

	private CouponCodeAppliedListener mListener;

	private static final String KEY_CREATE_TRIP = "KEY_CREATE_TRIP";
	private static final String KEY_TEXT_EMPTY = "KEY_COUPON_TEXT_EMPTY";
	private static final String KEY_APPLY_CLICKED = "KEY_COUPON_APPLY_CLICKED";
	private static final String KEY_USE_NEW_TOTAL = "KEY_COUPON_USE_NEW_TOTAL";
	private static final String KEY_PROGRESS_SHOWING = "KEY_COUPON_PROGRESS_SHOWING";
	private static final String KEY_ERROR = "KEY_COUPON_ERROR";

	public CouponCodeWidget (Context context, View rootView) {
		mContext = context;

		mCouponCode = (EditText) rootView.findViewById(R.id.coupon_code_edittext);
		mApply = (TextView) rootView.findViewById(R.id.apply_button);
		mProgressBar = rootView.findViewById(R.id.coupon_progress_bar);
		mNewTotal = (TextView) rootView.findViewById(R.id.new_total_textview);

		mApply.setEnabled(false);
		mApply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mApplyClicked = true;
				mProgressShowing = true;
				startOrResumeDownload();
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
			if (mApplyClicked) {
				mApplyClicked = false;
				mUseNewTotal = false;
				mProgressShowing = false;
				mError = false;
				Db.setCreateTripResponse(null);
				startOrResumeDownload();
			}
		}
	};

	private void startOrResumeDownload() {
		if (mError) {
			mProgressBar.setVisibility(View.GONE);
			mApply.setVisibility(View.GONE);
			mNewTotal.setVisibility(View.VISIBLE);
			mNewTotal.setText(mContext.getString(R.string.coupon_error));
			return;
		}

		BackgroundDownloader bd = BackgroundDownloader.getInstance();
		if (mApplyClicked) {
			if (mProgressShowing) {
				mProgressBar.setVisibility(View.VISIBLE);
				mApply.setVisibility(View.GONE);
				mNewTotal.setVisibility(View.GONE);

				if (bd.isDownloading(KEY_CREATE_TRIP)) {
					bd.registerDownloadCallback(KEY_CREATE_TRIP, mCouponCallback);
				}
				else {
					bd.startDownload(KEY_CREATE_TRIP, mCouponDownload, mCouponCallback);
				}
			}
			else if (mUseNewTotal) {
				mProgressBar.setVisibility(View.GONE);
				mApply.setVisibility(View.GONE);
				mNewTotal.setVisibility(View.VISIBLE);
				setNewTotal();
			}
		}
		else {
			if (bd.isDownloading(KEY_CREATE_TRIP)) {
				bd.cancelDownload(KEY_CREATE_TRIP);
			}
			mProgressBar.setVisibility(View.GONE);
			mApply.setVisibility(View.VISIBLE);
			mNewTotal.setVisibility(View.GONE);
		}
	}

	public void saveInstanceState(Bundle outState) {
		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_CREATE_TRIP, mCouponCallback);
		if (outState != null) {
			outState.putBoolean(KEY_TEXT_EMPTY, mTextEmpty);
			outState.putBoolean(KEY_APPLY_CLICKED, mApplyClicked);
			outState.putBoolean(KEY_USE_NEW_TOTAL, mUseNewTotal);
			outState.putBoolean(KEY_PROGRESS_SHOWING, mProgressShowing);
			outState.putBoolean(KEY_ERROR, mError);
		}
	}

	public void restoreInstanceState(Bundle inState) {
		if (inState != null) {
			mTextEmpty = inState.getBoolean(KEY_TEXT_EMPTY, true);
			mApplyClicked = inState.getBoolean(KEY_APPLY_CLICKED, false);
			mUseNewTotal = inState.getBoolean(KEY_USE_NEW_TOTAL, false);
			mProgressShowing = inState.getBoolean(KEY_PROGRESS_SHOWING, false);
			mError = inState.getBoolean(KEY_ERROR, false);
			mApply.setEnabled(!mTextEmpty);
		}
		startOrResumeDownload();
	}

	public void startTextWatcher() {
		mCouponCode.addTextChangedListener(couponWatcher);
	}

	public void setCouponCodeAppliedListener(CouponCodeAppliedListener listener) {
		mListener = listener;
	}

	private final Download<CreateTripResponse> mCouponDownload = new Download<CreateTripResponse>() {
		@Override
		public CreateTripResponse doDownload() {
			String code = mCouponCode.getText().toString();
			ExpediaServices services = new ExpediaServices(mContext);
			BackgroundDownloader.getInstance().addDownloadListener(KEY_CREATE_TRIP, services);
			return services.createTripWithCoupon(code, Db.getSearchParams(), Db.getSelectedProperty(), Db.getSelectedRate());
		}
	};

	private final OnDownloadComplete<CreateTripResponse> mCouponCallback = new OnDownloadComplete<CreateTripResponse>() {
		@Override
		public void onDownload(CreateTripResponse response) {
			mProgressShowing = false;
			mProgressBar.setVisibility(View.GONE);
			mNewTotal.setVisibility(View.VISIBLE);
			if (response == null) {
				mError = true;
				mNewTotal.setText(mContext.getString(R.string.coupon_error));
			}
			else if (response.hasErrors()) {
				mError = true;
				boolean isExpired = false;
				boolean isInvalid = false;
				for (ServerError error : response.getErrors()) {
					switch (error.getErrorCode()) {
					case INVALID_INPUT_COUPON_CODE:
						isExpired = true;
						break;
					case INVALID_INPUT:
						isInvalid = true;
						break;
					case APPLY_COUPON_ERROR:
						isInvalid = true;
						break;
					}
				}
				if (isInvalid) {
					mNewTotal.setText(mContext.getString(R.string.invalid_coupon));
				}
				else if (isExpired) {
					mNewTotal.setText(mContext.getString(R.string.expired_coupon));
				}
				else {
					mNewTotal.setText(mContext.getString(R.string.coupon_error));
				}
			}
			else {
				mUseNewTotal = true;
				Db.setCreateTripResponse(response);
				setNewTotal();
				if (mListener != null) {
					mListener.couponCodeApplied();
				}
			}
		}
	};

	private void setNewTotal() {
		CreateTripResponse response = Db.getCreateTripResponse();
		if (response != null) {
			Money m;
			if (LocaleUtils.shouldDisplayMandatoryFees(mContext)) {
				m = response.getNewRate().getTotalPriceWithMandatoryFees();
			}
			else {
				m = response.getNewRate().getTotalAmountAfterTax();
			}
			mNewTotal.setText(mContext.getString(R.string.new_total) + "\n" + m.getFormattedMoney());
		}
	}

	public interface CouponCodeAppliedListener {
		public void couponCodeApplied();
	}
}
