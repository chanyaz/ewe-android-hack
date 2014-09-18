package com.expedia.bookings.invaders;

import android.content.Context;
import android.util.AttributeSet;

import com.expedia.bookings.R;

/**
 * Created by jdrotos on 6/16/14.
 */
public class InvaderLaserView extends LaserView {
	public InvaderLaserView(Context context) {
		super(context);
		init();
	}

	public InvaderLaserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public InvaderLaserView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	@Override
	protected void init() {
		this.setImageResource(R.drawable.icon_email);
	}
}
