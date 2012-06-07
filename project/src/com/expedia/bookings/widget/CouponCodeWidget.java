package com.expedia.bookings.widget;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.data.CreateTripResponse;
import com.expedia.bookings.data.Db;
import com.expedia.bookings.data.Money;
import com.expedia.bookings.server.ExpediaServices;

import com.mobiata.android.BackgroundDownloader;
import com.mobiata.android.BackgroundDownloader.Download;
import com.mobiata.android.BackgroundDownloader.OnDownloadComplete;
import com.mobiata.android.Log;

public class CouponCodeWidget {
	private Context mContext;

	private EditText mCouponCode;
	private Button mApply;
	private View mProgressBar;
	private TextView mNewTotal;

	private boolean mApplyClicked = false;
	private boolean mProgressShowing = false;
	private boolean mUseNewTotal = false;

	private static final String KEY_CREATE_TRIP = "KEY_CREATE_TRIP";
	private static final String KEY_APPLY_CLICKED = "KEY_COUPON_APPLY_CLICKED";
	private static final String KEY_USE_NEW_TOTAL = "KEY_COUPON_USE_NEW_TOTAL";
	private static final String KEY_PROGRESS_SHOWING = "KEY_COUPON_PROGRESS_SHOWING";

	public CouponCodeWidget (Context context, View rootView) {
		mContext = context;

		mCouponCode = (EditText) rootView.findViewById(R.id.coupon_code_edittext);
		mApply = (Button) rootView.findViewById(R.id.apply_button);
		mProgressBar = rootView.findViewById(R.id.coupon_progress_bar);
		mNewTotal = (TextView) rootView.findViewById(R.id.new_total_textview);

		mApply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mApplyClicked = true;
				mProgressShowing = true;
				startOrResumeDownload();
			}
		});

		// TODO: on text change listener
	}


	private void startOrResumeDownload() {
		if (mApplyClicked) {
			if (mProgressShowing) {
				mProgressBar.setVisibility(View.VISIBLE);
				mApply.setVisibility(View.GONE);
				mNewTotal.setVisibility(View.GONE);

				BackgroundDownloader bd = BackgroundDownloader.getInstance();
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
			}
		}
		else {
			mProgressBar.setVisibility(View.GONE);
			mApply.setVisibility(View.VISIBLE);
			mNewTotal.setVisibility(View.GONE);
		}
	}

	public void saveInstanceState(Bundle outState) {
		BackgroundDownloader.getInstance().unregisterDownloadCallback(KEY_CREATE_TRIP, mCouponCallback);
		if (outState != null) {
			outState.putBoolean(KEY_APPLY_CLICKED, mApplyClicked);
			outState.putBoolean(KEY_USE_NEW_TOTAL, mUseNewTotal);
			outState.putBoolean(KEY_PROGRESS_SHOWING, mProgressShowing);
		}
	}

	public void restoreInstanceState(Bundle inState) {
		if (inState != null) {
			mApplyClicked = inState.getBoolean(KEY_APPLY_CLICKED, false);
			mUseNewTotal = inState.getBoolean(KEY_USE_NEW_TOTAL, false);
			mProgressShowing = inState.getBoolean(KEY_PROGRESS_SHOWING, false);
		}
		startOrResumeDownload();
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
			if (response == null || response.hasErrors()) {
				// TODO: use R.string
				mNewTotal.setText("Invalid Coupon");
			}
			else {
				mUseNewTotal = true;
				// TODO: use proper total based on POS
				mNewTotal.setText(mContext.getString(R.string.new_total) + "\n" + response.getNewRate().getTotalPriceWithMandatoryFees().getFormattedMoney());
				Db.setCreateTripResponse(response);
			}
		}
	};

}
