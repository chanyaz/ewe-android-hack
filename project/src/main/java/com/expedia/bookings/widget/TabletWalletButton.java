package com.expedia.bookings.widget;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import com.expedia.bookings.R;

public class TabletWalletButton extends WalletButton {

	private boolean mIsPromoVisible = false;

	public TabletWalletButton(Context context) {
		super(context);
	}

	public TabletWalletButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TabletWalletButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void setPromoVisible(boolean visible) {
		super.setPromoVisible(visible);
		if (mIsPromoVisible != visible) {
			Drawable bgDrawable = visible ? getResources().getDrawable(R.drawable.bg_checkout_information_single_no_shadow) :
				getResources().getDrawable(R.drawable.bg_checkout_information_single);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				mButton.findViewById(R.id.button).setBackgroundDrawable(bgDrawable);
			}
			else {
				mButton.findViewById(R.id.button).setBackground(bgDrawable);
			}
			mIsPromoVisible = visible;
		}
	}
}
