package com.expedia.bookings.widget;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.expedia.bookings.R;

/**
 * A RadioGroup with custom drawables, and a nicely animated transition between
 * selected buttons. Designed for use in hotel/flight filters to narrow down
 * the search results.
 *
 * This control paints its background in two layers: the bottom (unselected) layer
 * fills the whole canvas. The top (selected) layer is partially exposed, depending
 * on which CompoundButton child is selected. That exposed part of the top layer
 * can be animated from one region to the next if desired. Both drawables used
 * for drawing the background are expected to fill the canvas (so, probably use
 * 9-patch drawables).
 *
 * <pre>
 * &lt;com.expedia.bookings.widget.SlidingRadioGroup
 *     android:id="@+id/ring"
 *     android:layout_width="180dp"
 *     android:layout_height="180dp"
 *     android:layout_centerHorizontal="true"
 *     android:layout_centerVertical="true"
 *     app:dividerWidth="1dp"
 *     app:selectedDividerColor="#ff596a80"
 *     app:selectedDrawable="@drawable/btn_tablet_filter_pressed"
 *     app:sluggishness="150"
 *     app:unselectedDividerColor="#40999999"
 *     app:unselectedDrawable="@drawable/btn_tablet_filter_normal" &gt;
 *
 *     &lt;RadioButton
 *         android:id="@+id/option_1"
 *         android:layout_width="0dp"
 *         android:layout_height="wrap_content"
 *         android:layout_weight="1"
 *         android:text="option 1"
 *         android:textColor="@color/sliding_group_text" /&gt;
 *
 *     &lt;RadioButton
 *         android:id="@+id/option_2"
 *         android:layout_width="0dp"
 *         android:layout_height="wrap_content"
 *         android:layout_weight="1"
 *         android:text="option 2"
 *         android:textColor="@color/sliding_group_text" /&gt;
 *
 * &lt;/com.expedia.bookings.widget.SlidingRadioGroup&gt;
 * </pre>
 *
 * @author Doug Melton
 */
public class SlidingRadioGroup extends RadioGroup implements RadioGroup.OnCheckedChangeListener {

	private ExposedLayerDrawable mBackground;

	// We want to hook into OnCheckedChangeListener in this View
	private RadioGroup.OnCheckedChangeListener mOnCheckedChangeListener;

	public SlidingRadioGroup(Context context) {
		this(context, null);
	}

	public SlidingRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		Drawable unselectedDrawable = null;
		Drawable selectedDrawable = null;
		int unselectedDividerColor = 0;
		int selectedDividerColor = 0;
		float dividerWidth = 0;
		int sluggishness = 200;

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingRadioGroup);
			unselectedDrawable = a.getDrawable(R.styleable.SlidingRadioGroup_unselectedDrawable);
			selectedDrawable = a.getDrawable(R.styleable.SlidingRadioGroup_selectedDrawable);
			unselectedDividerColor = a.getColor(R.styleable.SlidingRadioGroup_unselectedDividerColor,
					unselectedDividerColor);
			selectedDividerColor = a.getColor(R.styleable.SlidingRadioGroup_selectedDividerColor, selectedDividerColor);
			dividerWidth = a.getDimension(R.styleable.SlidingRadioGroup_dividerWidth, dividerWidth);
			sluggishness = a.getInteger(R.styleable.SlidingRadioGroup_sluggishness, sluggishness);
			a.recycle();
		}

		Paint unselectedDivider = new Paint();
		unselectedDivider.setColor(unselectedDividerColor);
		unselectedDivider.setStrokeWidth(dividerWidth);

		Paint selectedDivider = new Paint();
		selectedDivider.setColor(selectedDividerColor);
		selectedDivider.setStrokeWidth(dividerWidth);

		mBackground = new ExposedLayerDrawable(
				unselectedDrawable, selectedDrawable,
				unselectedDivider, selectedDivider,
				sluggishness);

		super.setBackgroundDrawable(mBackground);

		// This overrides the radiogroup onCheckListener
		super.setOnCheckedChangeListener(this);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		updateExposedRect(false);
	}

	@Override
	public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
		mOnCheckedChangeListener = listener;
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateExposedRect(true);
		if (mOnCheckedChangeListener != null) {
			mOnCheckedChangeListener.onCheckedChanged(group, checkedId);
		}
	}

	@Override
	public void check(int id) {
		super.check(id);
		updateExposedRect(true);
	}

	@Override
	public void clearCheck() {
		super.clearCheck();
		updateExposedRect(true);
	}

	private void updateExposedRect(boolean animate) {
		mBackground.setExposedRect(getCheckedChild(), animate);
		invalidateDrawable(mBackground);
	}

	private View getCheckedChild() {
		for (int i = 0; i < getChildCount(); i++) {
			CompoundButton child = (CompoundButton) getChildAt(i);
			if (child.isChecked()) {
				return child;
			}
		}
		return null;
	}

	private class ExposedLayerDrawable extends Drawable {

		private final Drawable mUnselected;
		private final Paint mUnselectedDivider;

		private final Drawable mSelected;
		private final Paint mSelectedDivider;

		private final Rect mRectExposed;

		private final Rect mRectFrom;
		private final Rect mRectTo;

		private final int mSluggishness;

		private Animator mAnimator;

		public ExposedLayerDrawable(Drawable unselected, Drawable selected,
									Paint unselectedDivider, Paint selectedDivider, int sluggishness) {
			mRectExposed = new Rect();
			mRectFrom = new Rect();
			mRectTo = new Rect();

			// It's not invalid for these to be null
			mUnselected = unselected;
			mSelected = selected;

			// These will/should never be null
			mUnselectedDivider = unselectedDivider;
			mSelectedDivider = selectedDivider;

			mSluggishness = sluggishness;
		}

		@Override
		protected void onBoundsChange(Rect bounds) {
			super.onBoundsChange(bounds);

			if (mUnselected != null) {
				mUnselected.setBounds(bounds);
			}
			if (mSelected != null) {
				mSelected.setBounds(bounds);
			}
		}

		public void setExposedRect(View child, boolean animate) {
			if (child != null) {
				mRectTo.left = child.getLeft() - 1;
				mRectTo.top = child.getTop();
				mRectTo.bottom = child.getBottom();
				mRectTo.right = child.getRight() + 1;
			}
			else {
				mRectTo.left = -1;
				mRectTo.top = 0;
				mRectTo.bottom = getHeight();
				mRectTo.right = -1;
			}

			if (mAnimator != null && mAnimator.isRunning()) {
				// Another animator is running. Just updating mRectTo is sufficient.
			}
			else if (animate) {
				mRectFrom.set(mRectExposed);
				// This will call setRectTransit()
				mAnimator = ObjectAnimator.ofFloat(this, "rectTransit", 0f, 1f).setDuration(mSluggishness);
				mAnimator.start();
			}
			else {
				mRectExposed.set(mRectTo);
				invalidateSelf();
			}
		}

		// This is used by ObjectAnimator.ofFloat(this, "rectTransit", ...) above.
		@SuppressWarnings("unused")
		public void setRectTransit(float percent) {
			mRectExposed.top = transit(mRectFrom.top, mRectTo.top, percent);
			mRectExposed.left = transit(mRectFrom.left, mRectTo.left, percent);
			mRectExposed.bottom = transit(mRectFrom.bottom, mRectTo.bottom, percent);
			mRectExposed.right = transit(mRectFrom.right, mRectTo.right, percent);
			invalidateSelf();
		}

		private int transit(int from, int to, float percent) {
			return (int) ((to - from) * percent + from);
		}

		@Override
		public void draw(Canvas canvas) {
			if (mUnselected != null) {
				mUnselected.draw(canvas);
			}
			drawDividers(canvas, mUnselectedDivider);
			canvas.save();
			canvas.clipRect(mRectExposed);
			if (mSelected != null) {
				mSelected.draw(canvas);
			}
			drawDividers(canvas, mSelectedDivider);
			canvas.restore();
		}

		private void drawDividers(Canvas canvas, Paint paint) {
			// Short circuit if the divider width == 0
			if (paint.getStrokeWidth() == 0) {
				return;
			}

			// We're going to cheat here a little and examine the enclosing View's children
			int count = getChildCount();
			for (int i = 0; i < count - 1; i++) {
				float x = getChildAt(i).getRight();
				canvas.drawLine(x, 0, x, canvas.getHeight(), paint);
			}
		}

		@Override
		public int getOpacity() {
			return 0;
		}

		@Override
		public void setAlpha(int alpha) {
			// not supported
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			// not supported
		}
	}
}
