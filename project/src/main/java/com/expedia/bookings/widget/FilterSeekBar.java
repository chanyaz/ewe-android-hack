package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FilterSeekBar extends CustomSeekBarView {

	private OnSeekBarChangeListener listener;

	public FilterSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public int getMaxValue() {
		return maxValue;
	}

	/**
	 * Callback listener interface to notify about changed range values.
	 */
	public interface OnSeekBarChangeListener {

		/**
		 * Notification that the progress level has changed. Clients can use the fromUser parameter
		 * to distinguish user-initiated changes from those that occurred programmatically.
		 */
		void onProgressChanged(FilterSeekBar seekBar, int progress, boolean fromUser);

		/**
		 * Notification that the user has started/finished a touch gesture. Clients may want to use this
		 * to disable/re-enable advancing the seekbar.
		 */
		void onStartTrackingTouch(FilterSeekBar seekBar);
		void onStopTrackingTouch(FilterSeekBar seekBar);
	}

	public OnSeekBarChangeListener getListener() {
		return listener;
	}

	/**
	 * Registers given listener callback to notify about changed selected values.
	 *
	 * @param listener The listener to notify about changed selected values.
	 */
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
		this.listener = listener;
	}

	/**
	 * Handles thumb selection and movement. Notifies listener callback on certain events.
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			updateSelectedFromTouchEvent(event);
			setPressed(true);
			invalidate();
			if (getParent() != null) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			updateSelectedFromTouchEvent(event);
			invalidate();

			if (listener != null) {
				listener.onProgressChanged(this, getMaxValue(), true);
			}
			break;
		default:
			updateSelectedFromTouchEvent(event);
			setPressed(false);
			invalidate();

			if (listener != null) {
				listener.onProgressChanged(this, getMaxValue(), true);
			}
			break;
		}
		return true;
	}

	/**
	 * Draws the widget on the given canvas.
	 */
	@Override
	public void onDraw(@NonNull Canvas canvas) {
		super.onDraw(canvas);

		RectF rectf = new RectF();
		rectf.top = (getHeight() - barHeight) / 2.0f;
		rectf.bottom = (getHeight() + barHeight) / 2.0f;

		// draw seek bar background line
		rectf.left = getThumbHalfWidth();
		rectf.right = getWidth() - getThumbHalfWidth();

		canvas.drawRoundRect(rectf, barHeight / 2.0f, barHeight / 2.0f, backgroundPaint);

		// draw seek bar active range line
		rectf.left = valueToScreen(minValue);
		rectf.right = valueToScreen(maxValue);

		canvas.drawRoundRect(rectf, barHeight / 2.0f, barHeight / 2.0f, linePaint);

		drawThumb(canvas, rectf.right);
	}


	public Drawable getThumb() {
		return thumb;
	}

	private void updateSelectedFromTouchEvent(MotionEvent event) {
		final float x = event.getX();
		setMaxValue(screenToValue(x));
	}
}
