package com.expedia.bookings.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.expedia.bookings.R;
import com.mobiata.android.util.Ui;

public class WalletButton extends RelativeLayout {

	private View mButton;
	private ProgressBar mProgressBar;

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
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		mButton.setOnClickListener(l);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		mButton.setEnabled(enabled);

		// If the button is disabled, automatically show the progress bar; otherwise, hide it
		mProgressBar.setVisibility(enabled ? View.GONE : View.VISIBLE);
	}
}
