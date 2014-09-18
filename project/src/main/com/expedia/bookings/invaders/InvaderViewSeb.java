package com.expedia.bookings.invaders;

import android.content.Context;

import com.expedia.bookings.R;

/**
 * Created by jdrotos on 6/16/14.
 */
public class InvaderViewSeb extends InvaderView {
	public InvaderViewSeb(Context context) {
		super(context);
	}

	@Override
	public int getImageResId(){
		return R.drawable.seb;
	}

	@Override
	public int getKillPoints() {
		return 20;
	}
}
