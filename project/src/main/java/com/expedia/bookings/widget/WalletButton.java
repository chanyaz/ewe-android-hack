package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration;
import com.expedia.bookings.utils.WalletUtils;
import com.mobiata.android.util.Ui;

public class WalletButton extends RelativeLayout {

	protected View mButton;
	private ProgressBar mProgressBar;
	private View mPromo;

	// Allow promo portion to be disabled completely (since we only show it on hotels)
	private boolean mPromoVisible;

	public WalletButton(Context context) {
		super(context);
	}

	public WalletButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public WalletButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		mButton = Ui.findView(this, R.id.wallet_button);
		mProgressBar = Ui.findView(this, R.id.wallet_progress_bar);
		mPromo = Ui.findView(this, R.id.wallet_promo);

		updatePromoVisibility();
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		mButton.setEnabled(enabled);

		// If the button is disabled, automatically show the progress bar; otherwise, hide it
		mProgressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
	}

	public void setPromoVisible(boolean visible) {
		mPromoVisible = visible;

		updatePromoVisibility();
	}

	private void updatePromoVisibility() {
		if (mPromo != null) {
			mPromo.setVisibility(mPromoVisible && WalletUtils.offerGoogleWalletCoupon(getContext()) ? View.VISIBLE
					: View.GONE);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		mButton.dispatchTouchEvent(ev);
		if (ProductFlavorFeatureConfiguration.getInstance().isGoogleWalletPromoEnabled()) {
			mPromo.dispatchTouchEvent(ev);
		}
		return true;
	}
}
