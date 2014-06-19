package com.expedia.bookings.invaders;

import android.content.Context;

import com.expedia.bookings.R;

/**
 * Created by jdrotos on 6/16/14.
 */
public class InvaderViewJoel extends InvaderView {
	public InvaderViewJoel(Context context) {
		super(context);
	}

	@Override
	public int getImageResId(){
		return R.drawable.joel;
	}

	@Override
	public int getKillPoints() {
		return 50;
	}

	@Override
	public int getInvaderColor(){
		return super.getInvaderColor();
	}
}
