package com.expedia.bookings.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;

/**
 * A view displaying a count with a partially completed circle around it, on a
 * translucent black background.
 * 
 * <pre>
 * &lt;com.expedia.bookings.widget.RingedCountView
 *     android:id="@+id/ring"
 *     android:layout_width="180dp"
 *     android:layout_height="180dp"
 *     android:layout_centerHorizontal="true"
 *     android:layout_centerVertical="true"
 *     app:primaryColor="#ffffff"
 *     app:secondaryColor="#66ace3"
 *     app:countTextColor="#ffffff"
 *     app:countTextSize="56sp"
 *     app:captionTextColor="#ffffff"
 *     app:captionTextSize="18sp"
 *     app:ringThickness="10dp" /&gt;
 * </pre>
 * 
 * <pre>
 *    ringView.setCaption(String.format("of %d hotels", seekBar.getMax()));
 *    ringView.setPercent(1.0f * seekBar.getProgress() / seekBar.getMax());
 *    ringView.setCount(seekBar.getProgress());
 * </pre>
 * 
 * @author Doug Melton
 */
public class RingedCountView extends View {

	private Animator mAnimator;
	private RingDrawable mRingDrawable;

	public RingedCountView(Context context) {
		super(context);
		init(context, null);
	}

	public RingedCountView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RingedCountView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		int backgroundColor = Color.argb(0x00, 0x00, 0x00, 0x00);
		int countTextColor = Color.WHITE;
		int captionTextColor = Color.WHITE;
		int primaryColor = Color.WHITE;
		int secondaryColor = Color.argb(0xff, 0x66, 0xAC, 0xE3);
		int thickness = 20;
		float countTextSize = 112f;
		float captionTextSize = 36f;
		String caption = null;
		float percent = 0.0f;

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingedCountView);
			backgroundColor = a.getColor(R.styleable.RingedCountView_backgroundColor, backgroundColor);
			countTextColor = a.getColor(R.styleable.RingedCountView_countTextColor, countTextColor);
			captionTextColor = a.getColor(R.styleable.RingedCountView_captionTextColor, captionTextColor);
			primaryColor = a.getColor(R.styleable.RingedCountView_primaryColor, primaryColor);
			secondaryColor = a.getColor(R.styleable.RingedCountView_secondaryColor, secondaryColor);
			thickness = a.getDimensionPixelSize(R.styleable.RingedCountView_ringThickness, thickness);
			countTextSize = a.getDimension(R.styleable.RingedCountView_countTextSize, countTextSize);
			captionTextSize = a.getDimension(R.styleable.RingedCountView_captionTextSize, captionTextSize);
			caption = a.getString(R.styleable.RingedCountView_caption);
			percent = a.getFloat(R.styleable.RingedCountView_percent, percent);
			a.recycle();
		}

		mRingDrawable = new RingDrawable(backgroundColor, countTextColor, captionTextColor,
				primaryColor, secondaryColor, thickness, countTextSize, captionTextSize);
		setCaption(caption);
		setPercent(percent);

		super.setBackgroundDrawable(mRingDrawable);
	}

	@Override
	public void setBackgroundDrawable(Drawable background) {
		// ignored
	}

	/**
	 * Sets the percentage filled of the ring only. Does not change the text.
	 * @param filled
	 */
	@TargetApi(11)
	public void setPercent(float filled) {
		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.cancel();
		}
		mRingDrawable.setPercent(filled);
	}

	/**
	 * Returns the currently filled percentage of the ring.
	 * @return
	 */
	public float getPercent() {
		return mRingDrawable.getPercent();
	}

	/**
	 * Sets the large count text. Does not change the ring percentage. A fractional
	 * amount will cause an odometer style effect.
	 * @param count
	 */
	@TargetApi(11)
	public void setCount(float count) {
		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.cancel();
		}
		mRingDrawable.setCount(count);
	}

	/**
	 * Animates the count and ring percent to the passed values.
	 * @param count
	 * @param float percent
	 */
	@TargetApi(11)
	public void animateTo(int count, float percent) {
		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.cancel();
		}

		// 200ms per number different, capping out at 1200ms
		int duration = Math.min(1200, Math.abs(count - (int) mRingDrawable.getCount()) * 200);
		Animator countAnim = ObjectAnimator.ofFloat(mRingDrawable, "count", getCount(), count)
				.setDuration(duration);

		Animator ringAnim = ObjectAnimator.ofFloat(mRingDrawable, "percent", getPercent(), percent)
				.setDuration(200);

		AnimatorSet set = new AnimatorSet();
		set.play(countAnim).with(ringAnim);

		mAnimator = set;
		mAnimator.start();
	}

	/**
	 * Returns the current count text. Possibly fractional
	 * if the count is being animated at this moment.
	 */
	public float getCount() {
		return mRingDrawable.getCount();
	}

	/**
	 * Sets the caption displayed under the count (i.e. "of 23 hotels").
	 * @param caption
	 */
	public void setCaption(String caption) {
		mRingDrawable.setCaption(caption);
	}

	/**
	 * Returns the caption displayed under the count (i.e. "of 23 hotels").
	 */
	public String getCaption() {
		return mRingDrawable.getCaption();
	}

	/////////////////////////////////////////////////////////////////////////////////////
	// RingDrawable
	// A circular drawable with a ring that is a partially "filled"
	// with a count and caption in the middle.
	/////////////////////////////////////////////////////////////////////////////////////
	private static class RingDrawable extends Drawable {

		private Paint mBackgroundPaint;

		// Ring properties
		private Paint mPrimaryArcPaint;
		private Paint mSecondaryArcPaint;
		float mRadius, mCx, mCy;
		private RectF mOval;
		private float mFilledPercent = 0f;

		// Text properties
		private Paint mCountTextPaint;
		private Paint mCaptionTextPaint;
		private float mCountTextSize;
		private float mCaptionTextSize;
		private float mCount;
		private String mCaption;

		public RingDrawable(int backgroundColor, int countTextColor, int captionTextColor,
				int primaryColor, int secondaryColor,
				int strokeWidth, float primaryTextSize, float secondaryTextSize) {
			super();

			mBackgroundPaint = new Paint();
			mBackgroundPaint.setColor(backgroundColor);
			mBackgroundPaint.setAntiAlias(true);

			// Ring
			mPrimaryArcPaint = new Paint();
			mPrimaryArcPaint.setColor(primaryColor);
			mPrimaryArcPaint.setAntiAlias(true);
			mPrimaryArcPaint.setStrokeWidth(strokeWidth);
			mPrimaryArcPaint.setStyle(Style.STROKE);

			mSecondaryArcPaint = new Paint();
			mSecondaryArcPaint.setColor(secondaryColor);
			mSecondaryArcPaint.setAntiAlias(true);
			mSecondaryArcPaint.setStrokeWidth(strokeWidth);
			mSecondaryArcPaint.setStyle(Style.STROKE);

			// Text
			mCountTextSize = primaryTextSize;
			mCaptionTextSize = secondaryTextSize;

			mCountTextPaint = new Paint();
			mCountTextPaint.setColor(countTextColor);
			mCountTextPaint.setAntiAlias(true);
			mCountTextPaint.setTextAlign(Align.CENTER);
			mCountTextPaint.setTextSize(mCountTextSize);

			mCaptionTextPaint = new Paint();
			mCaptionTextPaint.setColor(countTextColor);
			mCaptionTextPaint.setAntiAlias(true);
			mCaptionTextPaint.setTextAlign(Align.CENTER);
			mCaptionTextPaint.setTextSize(mCaptionTextSize);
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			super.setBounds(left, top, right, bottom);

			int minHeightWidth = Math.min(right - left, bottom - top);
			mRadius = minHeightWidth * 0.9f / 2f;
			mCx = (right + left) / 2;
			mCy = (bottom + top) / 2;
			mOval = new RectF(mCx - mRadius, mCy - mRadius, mCx + mRadius, mCy + mRadius);
		}

		public void setPercent(float filled) {
			mFilledPercent = filled;
			invalidateSelf();
		}

		public float getPercent() {
			return mFilledPercent;
		}

		public void setCount(float count) {
			if (count != mCount) {
				mCount = count;
				invalidateSelf();
			}
		}

		public float getCount() {
			return mCount;
		}

		public void setCaption(String caption) {
			if ((caption != null || mCaption != null)
					&& (caption == null || !caption.equals(mCaption))) {
				mCaption = caption;
				invalidateSelf();
			}
		}

		public String getCaption() {
			return mCaption;
		}

		@Override
		public void draw(Canvas canvas) {
			// The translucent background
			canvas.drawCircle(mCx, mCy, mRadius, mBackgroundPaint);

			drawRing(canvas);
			drawText(canvas);
		}

		private void drawRing(Canvas canvas) {
			// The "filled" part of the ring
			float filledSweep = mFilledPercent * 360f;
			float filledAngle = -90f;
			canvas.drawArc(mOval, filledAngle, filledSweep, false, mPrimaryArcPaint);

			// The "empty" part of the ring
			float emptySweep = 360f - filledSweep;
			float emptyAngle = filledAngle + filledSweep;
			canvas.drawArc(mOval, emptyAngle, emptySweep, false, mSecondaryArcPaint);
		}

		private void drawText(Canvas canvas) {
			// Odometer style count
			float yOffset = mCaption == null ? -((mCountTextPaint.descent() + mCountTextPaint.ascent()) / 2) : 0;
			canvas.save();
			canvas.clipRect(0, mCy - mCountTextSize + yOffset, canvas.getWidth(), mCy + 4 + yOffset, Op.REPLACE);
			String countString = Integer.toString(Math.round(mCount));
			float fraction = mCount - Math.round(mCount);
			float y = mCy - fraction * mCountTextSize + yOffset;
			canvas.drawText(countString, mCx, y, mCountTextPaint);
			if (y != 0) {
				String nextString = Integer.toString(Math.round(mCount) + 1);
				canvas.drawText(nextString, mCx, y + mCountTextSize, mCountTextPaint);
			}
			canvas.restore();

			// Caption
			if (mCaption != null) {
				canvas.drawText(mCaption, mCx, mCy + mCaptionTextSize * 1.6f, mCaptionTextPaint);
			}
		}

		@Override
		public int getOpacity() {
			return PixelFormat.TRANSLUCENT;
		}

		@Override
		public void setAlpha(int alpha) {
			// Not implemented
		}

		@Override
		public void setColorFilter(ColorFilter cf) {
			// Not implemented
		}

	}
}
