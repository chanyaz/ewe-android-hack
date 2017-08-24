package com.expedia.bookings.dialog;

import android.content.res.Resources;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.expedia.bookings.R;
import com.mobiata.android.app.SimpleDialogFragment;

public class VipBadgeClickListener implements View.OnClickListener {

	private final FragmentManager mFragmentManager;

	final String mTitle;
	final String mMessage;

	public VipBadgeClickListener(Resources res, FragmentManager manager) {
		mTitle = res.getString(R.string.vip_access);
		mMessage = res.getString(R.string.vip_access_message);
		mFragmentManager = manager;
	}

	@Override
	public void onClick(View v) {
		SimpleDialogFragment df = SimpleDialogFragment.newInstance(mTitle, mMessage);
		df.show(mFragmentManager, "vipAccess");
	}
}
