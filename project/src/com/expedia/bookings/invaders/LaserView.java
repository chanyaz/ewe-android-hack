package com.expedia.bookings.invaders;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;

/**
 * Created by jdrotos on 6/16/14.
 */
public class LaserView extends ImageView {
	public LaserView(Context context) {
		super(context);
		init();
	}

	public LaserView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LaserView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	protected void init(){
		//this.setBackgroundColor(Color.YELLOW);
		this.setImageResource(R.drawable.ic_tablet_hotel_details_star_filled);
	}
}
