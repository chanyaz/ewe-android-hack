package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;

import com.expedia.android.rangeseekbar.RangeSeekBar;
import com.expedia.bookings.R;

public class FilterRangeSeekBar extends RangeSeekBar {
	public FilterRangeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setThumb(new BitmapDrawable(getResources(), getThumbnail()));
	}

	public Bitmap getThumbnail() {
		float diameter = getContext().getResources().getDimension(R.dimen.car_time_slider_thumbnail_radius);
		float density = getContext().getResources().getDisplayMetrics().density;
		float radius = diameter / 2;

		float shadowConstant = 2 * density;
		float shadowOffset = density;
		int width = (int) (diameter + shadowConstant * 2);
		int height = (int) (width + shadowOffset * 2);

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.FILL);
		paint.setShadowLayer(shadowConstant, 0, shadowOffset, 0x33000000);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		canvas.drawCircle(width / 2, (height - shadowOffset) / 2, radius, paint);

		return bitmap;
	}
}
