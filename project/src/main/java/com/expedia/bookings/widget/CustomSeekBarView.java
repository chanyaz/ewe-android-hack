package com.expedia.bookings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.expedia.bookings.R;

public class CustomSeekBarView extends android.widget.SeekBar {

	public static final int DEFAULT_BLUE = Color.parseColor("#FF33B5E5");
	public static final int DEFAULT_GRAY = Color.parseColor("#FFD8D8D8");

	private static final int DEFAULT_LINE_HEIGHT_IN_DP = 5;

	// Cached for drawing
	protected final Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	protected final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private float thumbHalfWidth;
	private float thumbHalfHeight;
	protected int barHeight;

	protected Drawable thumb;
	private int upperLimit;
	protected int minValue;
	protected int maxValue;

	public CustomSeekBarView(Context context, AttributeSet attrs) {
		super(context, attrs);

		backgroundPaint.setStyle(Paint.Style.FILL);
		backgroundPaint.setAntiAlias(true);

		linePaint.setStyle(Paint.Style.FILL);
		linePaint.setAntiAlias(true);

		barHeight = dpToPx(context, DEFAULT_LINE_HEIGHT_IN_DP);

		int activeColor = DEFAULT_BLUE;
		int backgroundColor = DEFAULT_GRAY;

		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomSeekBarView, 0, 0);
			try {
				barHeight = a.getDimensionPixelSize(R.styleable.CustomSeekBarView_rsb__barHeight, barHeight);
				activeColor = a.getColor(R.styleable.CustomSeekBarView_rsb__activeColor, activeColor);
				backgroundColor = a.getColor(R.styleable.CustomSeekBarView_rsb__defaultColor, backgroundColor);

				Drawable thumb = new BitmapDrawable(getResources(), getThumbnail());
				if (thumb != null) {
					setThumb(thumb);
				}
			}
			finally {
				a.recycle();
			}
		}

		if (thumb == null) {
			setThumb(ContextCompat.getDrawable(context, R.drawable.rsb__seek_thumb_pressed));
		}

		setActiveColor(activeColor);
		backgroundPaint.setColor(backgroundColor);
		setUpperLimit(1);

		// make focusable. This solves focus handling issues in case EditText
		// widgets are being used along with the seekbar within ScrollViews.
		setFocusable(true);
		setFocusableInTouchMode(true);
	}

	public int getUpperLimit() {
		return upperLimit;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public float getThumbHalfWidth() {
		return thumbHalfWidth;
	}

	public float getThumbHalfHeight() {
		return thumbHalfHeight;
	}

	public void setThumb(Drawable thumb) {
		this.thumb = thumb;
		thumbHalfWidth = thumb.getIntrinsicWidth() / 2.0f;
		thumbHalfHeight = thumb.getIntrinsicHeight() / 2.0f;
		thumb.setBounds(0, 0, thumb.getIntrinsicWidth(), thumb.getIntrinsicHeight());
	}

	private Bitmap getThumbnail() {
		float diameter = getContext().getResources().getDimension(R.dimen.filter_slider_thumbnail_radius);
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


	public void setUpperLimit(int upperLimit) {
		this.upperLimit = upperLimit;
		setMinValue(0);
		setMaxValue(upperLimit);
		setMax(upperLimit);
	}

	public void setActiveColor(int activeColor) {
		linePaint.setColor(activeColor);
	}

	/**
	 * Ensures correct size of the widget.
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = thumb.getIntrinsicWidth() * 2;
		if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(widthMeasureSpec)) {
			width = MeasureSpec.getSize(widthMeasureSpec);
		}

		int height = thumb.getIntrinsicHeight();
		if (MeasureSpec.UNSPECIFIED != MeasureSpec.getMode(heightMeasureSpec)) {
			height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
		}
		setMeasuredDimension(width, height);
	}

	/**
	 * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.
	 *
	 * @param canvas The canvas to draw upon.
	 * @param where  The x-coordinate in screen space where to draw the image.
	 */
	protected void drawThumb(Canvas canvas, float where) {
		canvas.save();
		canvas.translate(where - thumbHalfWidth, 0);
		thumb.draw(canvas);
		canvas.restore();
	}

	/**
	 * Decides if given x-coordinate in screen space needs to be interpreted as "within" the thumb x-coordinate.
	 *
	 * @param touchX The x-coordinate in screen space to check.
	 * @param value  The x-coordinate of the thumb to check.
	 * @return true if x-coordinate is in thumb range, false otherwise.
	 */
	protected boolean isInThumbRange(float touchX, int value) {
		return Math.abs(touchX - valueToScreen(value)) <= thumbHalfWidth;
	}

	/**
	 * Converts a value into screen space.
	 *
	 * @param value The value to convert.
	 * @return The converted value in screen space.
	 */
	protected float valueToScreen(int value) {
		float lineWidth = getWidth() - 2 * thumbHalfWidth;
		float positionOnLine = (value / (float) upperLimit) * lineWidth;
		return thumbHalfWidth + positionOnLine;
	}

	/**
	 * Converts screen space x-coordinates into values.
	 *
	 * @param x The x-coordinate in screen space to convert.
	 * @return The value value.
	 */
	protected int screenToValue(float x) {
		float lineWidth = getWidth() - 2 * thumbHalfWidth;
		float positionOnLine = x - thumbHalfWidth;
		return Math.round((positionOnLine / lineWidth) * upperLimit);
	}

	/**
	 * Sets the currently selected maximum value. The widget will be invalidated and redrawn.
	 *
	 * @param value The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
	 */
	public void setMaxValue(int value) {
		if (value <= minValue) {
			value = minValue + 1;
		}
		else if (value > upperLimit) {
			value = upperLimit;
		}
		maxValue = clamp(value);
		setProgress(value);
		invalidate();
	}

	/**
	 * Sets the currently selected minimum value. The widget will be invalidated and redrawn.
	 *
	 * @param value The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
	 */
	public void setMinValue(int value) {
		if (value >= maxValue) {
			value = maxValue - 1;
		}
		else if (value < 0) {
			value = 0;
		}
		minValue = clamp(value);
		invalidate();
	}

	private int clamp(int x) {
		return Math.max(0, Math.min(x, upperLimit));
	}

	private static int dpToPx(Context context, int dp) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
	}
}
