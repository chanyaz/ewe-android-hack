package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class FilterRangeSeekBar extends CustomSeekBarView {

	// Internal view state
	private Thumb pressedThumb = null;

	protected OnRangeSeekBarChangeListener listener;

	public FilterRangeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Callback listener interface to notify about changed range values.
	 */
	public interface OnRangeSeekBarChangeListener {
		void onRangeSeekBarValuesChanged(FilterRangeSeekBar bar, int minValue, int maxValue);

		void onRangeSeekBarDragChanged(FilterRangeSeekBar bar, int minValue, int maxValue);
	}

	public OnRangeSeekBarChangeListener getListener() {
		return listener;
	}

	/**
	 * Registers given listener callback to notify about changed selected values.
	 *
	 * @param listener The listener to notify about changed selected values.
	 */
	public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener listener) {
		this.listener = listener;
	}

	/**
	 * Handles thumb selection and movement. Notifies listener callback on certain events.
	 */
	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		if (pressedThumb == null) {
			pressedThumb = findPressedThumb(event.getX());

			// Only handle thumb presses.
			if (pressedThumb == null) {
				return super.onTouchEvent(event);
			}
		}

		final int action = event.getAction();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			updateSelectedFromTouchEvent(event, pressedThumb);
			setPressed(true);
			invalidate();
			if (getParent() != null) {
				getParent().requestDisallowInterceptTouchEvent(true);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			updateSelectedFromTouchEvent(event, pressedThumb);
			invalidate();

			if (listener != null) {
				listener.onRangeSeekBarDragChanged(this, getMinValue(), getMaxValue());
			}
			break;
		default:
			updateSelectedFromTouchEvent(event, pressedThumb);
			setPressed(false);
			pressedThumb = null;
			invalidate();

			if (listener != null) {
				listener.onRangeSeekBarValuesChanged(this, getMinValue(), getMaxValue());
			}
			break;
		}
		return true;
	}

	private void updateSelectedFromTouchEvent(MotionEvent event, Thumb pressed) {
		final float x = event.getX();

		if (Thumb.MIN.equals(pressed)) {
			setMinValue(screenToValue(x));
		}
		else if (Thumb.MAX.equals(pressed)) {
			setMaxValue(screenToValue(x));
		}
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

		drawThumb(canvas, rectf.left);
		drawThumb(canvas, rectf.right);
	}

	/**
	 * Decides which (if any) thumb is touched by the given x-coordinate.
	 *
	 * @param touchX The x-coordinate of a touch event in screen space.
	 * @return The pressed thumb or null if none has been touched.
	 */
	private Thumb findPressedThumb(float touchX) {
		Thumb result = null;
		boolean minThumbPressed = isInThumbRange(touchX, minValue);
		boolean maxThumbPressed = isInThumbRange(touchX, maxValue);

		if (minThumbPressed && maxThumbPressed) {
			// if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag.
			// This avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
			result = (touchX / getWidth() > 0.5f) ? Thumb.MIN : Thumb.MAX;
		}
		else if (minThumbPressed) {
			result = Thumb.MIN;
		}
		else if (maxThumbPressed) {
			result = Thumb.MAX;
		}

		return result;
	}

	// Util
	private int clamp(int x) {
		return Math.max(0, Math.min(x, getUpperLimit()));
	}

	private enum Thumb {
		MIN,
		MAX
	}
}
