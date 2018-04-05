package com.expedia.bookings.widget;

import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.AccessibilityUtil;
import com.squareup.phrase.Phrase;

public class FilterRangeSeekBar extends CustomSeekBarView {

	protected FilterRangeSeekBarTouchHelper mTouchHelper;

	// Internal view state
	private Thumb pressedThumb = null;

	protected OnRangeSeekBarChangeListener listener;

	public String a11yStartName;
	public String currentA11yStartValue;

	public String a11yEndName;
	public String currentA11yEndValue;

	public FilterRangeSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchHelper = new FilterRangeSeekBarTouchHelper(this);
		setKeyProgressIncrement(1);
		ViewCompat.setAccessibilityDelegate(this, mTouchHelper);
	}

	@Override
	public boolean dispatchHoverEvent(MotionEvent event) {
		// Always attempt to dispatch hover events to accessibility first.
		return (mTouchHelper != null && mTouchHelper.dispatchHoverEvent(event)) || super.dispatchHoverEvent(event);
	}

	/**
	 * Callback listener interface to notify about changed range values.
	 */
	public interface OnRangeSeekBarChangeListener {
		void onRangeSeekBarValuesChanged(FilterRangeSeekBar bar, int minValue, int maxValue, Thumb thumb);

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
			if (listener != null) {
				listener.onRangeSeekBarValuesChanged(this, getMinValue(), getMaxValue(), pressedThumb);
			}
			requestFocus(pressedThumb);
			pressedThumb = null;
			invalidate();
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
		if (AccessibilityUtil.isTalkBackEnabled(getContext()) && mTouchHelper != null) {
			return mTouchHelper.getThumb();
		}
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

	public enum Thumb {
		MIN,
		MAX
	}

	//MARK :- Accessibility

	protected void updateValueBy(int factor, Thumb thumb) {
		switch (thumb) {
		case MIN:
			setMinValue(getMinValue() + factor);
			break;
		case MAX:
			setMaxValue(getMaxValue() + factor);
			break;
		}
		if (listener != null) {
			listener.onRangeSeekBarValuesChanged(FilterRangeSeekBar.this, getMinValue(), getMaxValue(), thumb);
		}
		requestFocus(pressedThumb);
	}

	private void requestFocus(Thumb pressedThumb) {
		if (mTouchHelper != null) {
			mTouchHelper
				.invalidateVirtualView(pressedThumb == Thumb.MIN ? FilterRangeSeekBarTouchHelper.MIN_VALUE_THUMB_ID
					: FilterRangeSeekBarTouchHelper.MAX_VALUE_THUMB_ID);
		}
	}

	private void setLeftThumbBound(Rect bounds) {
		bounds.left = (int) (valueToScreen(minValue) - getThumbHalfWidth());
		bounds.right = bounds.left + (int) (getThumbHalfWidth() * 2);
		bounds.top = 0;
		bounds.bottom = bounds.top + (int) (getThumbHalfHeight() * 2);
	}

	private void setRightThumbBound(Rect bounds) {
		bounds.left = (int) (valueToScreen(maxValue) - getThumbHalfWidth());
		bounds.right = bounds.left + (int) (getThumbHalfWidth() * 2);
		bounds.top = 0;
		bounds.bottom = bounds.top + (int) (getThumbHalfHeight() * 2);
	}

	public String getAccessibilityText(Thumb thumb) {
		String name = thumb == Thumb.MIN ? a11yStartName : a11yEndName;
		String value = thumb == Thumb.MIN ? currentA11yStartValue : currentA11yEndValue;
		return Phrase.from(getContext().getString(R.string.accessibility_slider_cont_desc_name_role_value_TEMPLATE))
			.put("name", name == null ? "" : name)
			.put("value", value == null ? "" : value)
			.format().toString();
	}

	protected class FilterRangeSeekBarTouchHelper extends ExploreByTouchHelper {
		private final Rect mTempRect = new Rect();
		private final static int MIN_VALUE_THUMB_ID = 0;
		private final static int MAX_VALUE_THUMB_ID = 1;

		FilterRangeSeekBarTouchHelper(View forView) {
			super(forView);
		}

		@Override
		protected int getVirtualViewAt(float x, float y) {
			// We also perform hit detection in onTouchEvent(), and we can
			// reuse that logic here. This will ensure consistency whether
			// accessibility is on or off.
			final Rect bounds = mTempRect;

			setLeftThumbBound(bounds);
			if (bounds.contains((int) x, (int) y)) {
				return MIN_VALUE_THUMB_ID;
			}

			setRightThumbBound(bounds);
			if (bounds.contains((int) x, (int) y)) {
				return MAX_VALUE_THUMB_ID;
			}
			return ExploreByTouchHelper.INVALID_ID;
		}

		@Override
		protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
			virtualViewIds.add(MIN_VALUE_THUMB_ID);
			virtualViewIds.add(MAX_VALUE_THUMB_ID);
		}

		@Override
		protected void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
			if (virtualViewId != MIN_VALUE_THUMB_ID && virtualViewId != MAX_VALUE_THUMB_ID) {
				throw new IllegalArgumentException("Invalid virtual view id");
			}
			event.getText().add(getAccessibilityText(virtualViewId == MIN_VALUE_THUMB_ID ? Thumb.MIN : Thumb.MAX));
		}

		@Override
		protected void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
			if (virtualViewId != MIN_VALUE_THUMB_ID && virtualViewId != MAX_VALUE_THUMB_ID) {
				throw new IllegalArgumentException("Invalid virtual view id");
			}
			node.setText(getAccessibilityText(virtualViewId == MIN_VALUE_THUMB_ID ? Thumb.MIN : Thumb.MAX));

			final Rect bounds = mTempRect;
			if (virtualViewId == MIN_VALUE_THUMB_ID) {
				setLeftThumbBound(bounds);
			}
			else {
				setRightThumbBound(bounds);
			}
			node.setBoundsInParent(bounds);
		}

		@Override
		protected boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
			return true;
		}

		@Override
		public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
			super.onInitializeAccessibilityNodeInfo(host, info);
			info.setScrollable(true);
			info.setEnabled(true);
			info.setClassName(FilterRangeSeekBar.class.getName());

			info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD);
			info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD);
			info.addAction(AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SET_PROGRESS);

			info.addAction(new AccessibilityNodeInfoCompat.AccessibilityActionCompat(R.id.seekbar_breakout_set_level,
				getContext().getString(R.string.title_seek_bar_edit)));
		}

		@Override
		public boolean performAccessibilityAction(View host, int action, Bundle args) {
			switch (action) {
			case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD:
				updateValueBy(1, getThumb());
				return true;
			case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD:
				updateValueBy(-1, getThumb());
				return true;
			case R.id.seekbar_breakout_set_level:
				post(new Runnable() {
					@Override
					public void run() {
						new SeekBarDialogManager(getThumb()).showDialog();
					}
				});
				return true;
			default:
				return super.performAccessibilityAction(host, action, args);
			}
		}

		protected Thumb getThumb() {
			return getAccessibilityFocusedVirtualViewId() == MIN_VALUE_THUMB_ID ? Thumb.MIN : Thumb.MAX;
		}
	}

	// Deals with opening the dialog from the menu item and controlling the dialog lifecycle.
	private class SeekBarDialogManager {
		private final Thumb pressedThumb;

		private View mRootView;
		private AlertDialog mDialog;

		SeekBarDialogManager(Thumb pressedThumb) {
			this.pressedThumb = pressedThumb;
		}

		private int getValue() {
			return pressedThumb == Thumb.MIN ? getMinValue() : getMaxValue();
		}

		void setValue(int value) {
			if (pressedThumb == Thumb.MIN) {
				setMinValue(value);
				if (getMaxValue() <= getMinValue()) {
					setMaxValue(value + 1);
				}
			}
			else {
				setMaxValue(value);
			}
			if (listener != null) {
				listener
					.onRangeSeekBarValuesChanged(FilterRangeSeekBar.this, getMinValue(), getMaxValue(), pressedThumb);
			}
		}

		private int getMinPercent() {
			return pressedThumb == Thumb.MIN ? 0 : (getMinValue() * 100 / getUpperLimit()) + 1;
		}

		private int getMaxPercent() {
			return pressedThumb == Thumb.MIN ? 99 : 100;
		}

		private int realToPercent() {
			float real = getValue();
			float min = 0;
			float max = getUpperLimit();
			return (int) (100.0f * (real - min) / (max - min));
		}

		private int percentToReal(int percentValue) {
			float min = 0;
			float max = getUpperLimit();
			return (int) (min + (percentValue / 100.0f) * (max - min));
		}

		private String getFormattedInstructionString() {
			return Phrase.from(getContext(), R.string.value_seek_bar_dialog_instructions_TEMPLATE)
				.put("min_value", getMinPercent())
				.put("max_value", getMaxPercent())
				.format()
				.toString();
		}

		private void showDialog() {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			mRootView = inflater.inflate(R.layout.seekbar_level_dialog, null);

			final AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
				.setView(mRootView)
				.setTitle(getContext().getString(R.string.title_seek_bar_edit))
				.setPositiveButton(android.R.string.ok, null)
				.setNegativeButton(android.R.string.cancel, null)
				.setCancelable(true);

			mDialog = builder.create();
			mDialog.show();

			final Button cancelButton = mDialog.getButton(DialogInterface.BUTTON_NEGATIVE);

			// We'd like to keep focus off of the text field until the user activates it.
			final Button okButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
			okButton.setFocusableInTouchMode(true);
			okButton.requestFocus();

			final TextView instructionsView = (TextView) mRootView.findViewById(R.id.instructions);
			instructionsView.setText(getFormattedInstructionString());

			// Fill in the text field and restore normal input focus behavior when it gets focus.
			final EditText percentage = (EditText) mRootView.findViewById(R.id.seek_bar_level);
			percentage.setText(String.format(Locale.getDefault(), "%d", realToPercent()));
			percentage.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						okButton.setFocusableInTouchMode(false);
					}
				}
			});
			percentage.setOnEditorActionListener(new TextView.OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						submitDialog();
						return true;
					}
					return false;
				}
			});

			// Use our own custom listener to prevent the dialog from closing if there's an error.
			okButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					submitDialog();
				}
			});
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dismissDialog();
				}
			});
		}

		private void dismissDialog() {
			if (mDialog == null) {
				return;
			}
			mDialog.dismiss();
			sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED);
		}

		private void submitDialog() {
			if (mRootView == null || mDialog == null) {
				return;
			}

			final EditText percentage = (EditText) mRootView.findViewById(R.id.seek_bar_level);
			try {
				int percentValue = Integer.parseInt(percentage.getText().toString());
				if (percentValue < getMinPercent() || percentValue > getMaxPercent()) {
					throw new IndexOutOfBoundsException();
				}
				setValue(percentToReal(percentValue));
				dismissDialog();
			}
			catch (NumberFormatException | IndexOutOfBoundsException ex) {
				// Set the error text popup.
				percentage.setError(getFormattedInstructionString());
			}
		}
	}
}
