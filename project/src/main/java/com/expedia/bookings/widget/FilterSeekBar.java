package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.SeekBar;

import com.expedia.bookings.R;
import com.squareup.phrase.Phrase;

public class FilterSeekBar extends CustomSeekBarView {

	private OnSeekBarChangeListener listener;
	private String a11yName;
	private String currentA11yValue;

	public FilterSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setMaxValue(progress);
				if (listener != null) {
					listener.onProgressChanged(FilterSeekBar.this, getMaxValue(), true);
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
	}

	public int getMaxValue() {
		return maxValue;
	}

	public String getA11yName() {
		return a11yName;
	}

	public void setA11yName(String a11yName) {
		this.a11yName = a11yName;
	}

	public String getCurrentA11yValue() {
		return currentA11yValue;
	}

	public void setCurrentA11yValue(String currentA11yValue) {
		this.currentA11yValue = currentA11yValue;
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

	@Override
	public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
		super.onInitializeAccessibilityNodeInfo(info);
		String infoText = Phrase.from(getContext().getString(R.string.accessibility_seekbar_cont_desc_name_role_value_TEMPLATE))
								.put("name", getA11yName())
								.put("value", getCurrentA11yValue())
								.format().toString();
		info.setText(infoText);
	}

	public Drawable getThumb() {
		return thumb;
	}

	private void updateSelectedFromTouchEvent(MotionEvent event) {
		final float x = event.getX();
		setMaxValue(screenToValue(x));
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
	}
}
