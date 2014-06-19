package com.expedia.bookings.invaders;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.ScreenPositionUtils;

/**
 * Created by jdrotos on 6/16/14.
 */
public class ShipView extends ImageView {
	public ShipView(Context context) {
		super(context);
		init();
	}

	public ShipView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ShipView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setImageResource(R.drawable.bear_head);
	}

	public Point getGlobalLaserPoint() {
		Rect pos = ScreenPositionUtils.getGlobalScreenPosition(this);
		int x = pos.centerX();
		int y = pos.top;
		return new Point(x, y);
	}
}
