package com.expedia.bookings.invaders;

import android.content.Context;

import com.expedia.bookings.R;

/**
 * Created by jdrotos on 6/16/14.
 */
public class InvaderViewDan extends InvaderView {
	public InvaderViewDan(Context context) {
		super(context);
	}

	@Override
	public int getImageResId(){
		return R.drawable.dan;
	}

	@Override
	public int getKillPoints() {
		return 10;
	}

	@Override
	public int getInvaderColor(){
		return super.getInvaderColor();
		//return Color.parseColor("#66FF0000");
	}
}
