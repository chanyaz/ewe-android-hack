package com.expedia.bookings.widget;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.utils.FontCache;

/**
 * A view displaying a count with a partially completed circle around it, on a
 * translucent black background.
 * <p/>
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
 *     app:countTextStyle="normal"
 *     app:captionTextColor="#ffffff"
 *     app:captionTextSize="18sp"
 *     app:captionTextStyle="bold"
 *     app:ringThickness="10dp" /&gt;
 * </pre>
 * <p/>
 * <pre>
 *    ringView.setCaption(String.format("of %d hotels", seekBar.getMax()));
 *    ringView.setPercent(1.0f * seekBar.getProgress() / seekBar.getMax());
 *    ringView.setCount(seekBar.getProgress());
 * </pre>
 *
 * @author Doug Melton
 */
public class RingedCountView extends View {
	private static final int NORMAL = 0;
	private static final int BOLD = 1;
	private static final int ITALIC = 2;
	private static final int BLACK = 8;
	private static final int CONDENSED = 16;
	private static final int LIGHT = 32;
	private static final int MEDIUM = 64;
	private static final int THIN = 128;

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
		int countTextStyle = NORMAL;
		int captionTextColor = Color.WHITE;
		int captionTextStyle = NORMAL;
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
			captionTextStyle = a.getInt(R.styleable.RingedCountView_captionTextStyle, captionTextStyle);
			countTextStyle = a.getInt(R.styleable.RingedCountView_countTextStyle, countTextStyle);
			caption = a.getString(R.styleable.RingedCountView_caption);
			percent = a.getFloat(R.styleable.RingedCountView_percent, percent);
			a.recycle();
		}

		mRingDrawable = new RingDrawable();
		setBackgroundColor(backgroundColor);
		setPrimaryColor(primaryColor);
		setSecondaryColor(secondaryColor);
		setCountTextColor(countTextColor);
		setCaptionTextColor(captionTextColor);
		setCaption(caption);
		setPercent(percent);
		setRingThickness(thickness);
		setCountTextSize(countTextSize);
		setCaptionTextSize(captionTextSize);
		setCountTextStyle(countTextStyle);
		setCaptionTextStyle(captionTextStyle);
		setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());

		super.setBackgroundDrawable(mRingDrawable);
	}

	@Override
	public void setBackgroundDrawable(Drawable background) {
		// ignored
	}

	public void setBackgroundColor(int color) {
		mRingDrawable.setBackgroundColor(color);
	}

	public void setPrimaryColor(int color) {
		mRingDrawable.setPrimaryColor(color);
	}

	public void setSecondaryColor(int color) {
		mRingDrawable.setSecondaryColor(color);
	}

	public void setCountTextColor(int color) {
		mRingDrawable.setCountTextColor(color);
	}

	public void setCaptionTextColor(int color) {
		mRingDrawable.setCaptionTextColor(color);
	}

	public void setRingThickness(int thickness) {
		mRingDrawable.setRingThickness(thickness);
	}

	public void setCountTextSize(float pixels) {
		mRingDrawable.setCountTextSize(pixels);
	}

	public void setCaptionTextSize(float pixels) {
		mRingDrawable.setCaptionTextSize(pixels);
	}

	/**
	 * Only NORMAL and BOLD are supported for now.
	 */
	public void setCountTextStyle(int style) {
		mRingDrawable.setCountTextStyle(style);
	}

	/**
	 * Only NORMAL and BOLD are supported for now.
	 */
	public void setCaptionTextStyle(int style) {
		mRingDrawable.setCaptionTextStyle(style);
	}

	/**
	 * Sets the percentage filled of the ring only. Does not change the text.
	 *
	 * @param filled
	 */
	public void setPercent(float filled) {
		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.cancel();
		}
		mRingDrawable.setPercent(filled);
	}

	/**
	 * Returns the currently filled percentage of the ring.
	 *
	 * @return
	 */
	public float getPercent() {
		return mRingDrawable.getPercent();
	}

	/**
	 * Sets the large count text. Does not change the ring percentage. A fractional
	 * amount will cause an odometer style effect. Setting this will override what
	 * was set previously in setCount(String).
	 *
	 * @param count
	 */
	public void setCount(float count) {
		if (mAnimator != null && mAnimator.isRunning()) {
			mAnimator.cancel();
		}
		mRingDrawable.setCount(count);
	}

	/**
	 * Sets the large count text. Setting this will override what was set previously
	 * in setCount(float).
	 *
	 * @param text
	 */
	public void setCountText(String text) {
		mRingDrawable.setCountText(text);
	}

	/**
	 * Animates the count and ring percent to the passed values.
	 *
	 * @param count
	 * @param percent
	 */
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
	 *
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

	@Override
	public void setPadding(int left, int top, int right, int bottom) {
		super.setPadding(left, top, right, bottom);
		if (mRingDrawable != null) {
			mRingDrawable.setPadding(left, top, right, bottom);
		}
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
		private float mRadius, mCx, mCy;
		private Rect mPadding = new Rect();
		private RectF mOval;
		private float mFilledPercent = 0f;

		// Text properties
		private Paint mCountTextPaint;
		private Paint mCaptionTextPaint;
		private float mCount;
		private String mCountText;
		private String mCaption;

		public RingDrawable() {
			super();

			mBackgroundPaint = new Paint();
			mBackgroundPaint.setAntiAlias(true);

			// Ring
			mPrimaryArcPaint = new Paint();
			mPrimaryArcPaint.setAntiAlias(true);
			mPrimaryArcPaint.setStyle(Style.STROKE);

			mSecondaryArcPaint = new Paint();
			mSecondaryArcPaint.setAntiAlias(true);
			mSecondaryArcPaint.setStyle(Style.STROKE);

			// Text
			mCountTextPaint = new Paint();
			mCountTextPaint.setAntiAlias(true);
			mCountTextPaint.setTextAlign(Align.CENTER);

			mCaptionTextPaint = new Paint();
			mCaptionTextPaint.setAntiAlias(true);
			mCaptionTextPaint.setTextAlign(Align.CENTER);
		}

		@Override
		public void setBounds(int left, int top, int right, int bottom) {
			super.setBounds(left, top, right, bottom);
			generateOval();
		}

		public void setPadding(int left, int top, int right, int bottom) {
			mPadding.left = left;
			mPadding.top = top;
			mPadding.right = right;
			mPadding.bottom = bottom;
			generateOval();
		}

		private void generateOval() {
			Rect bounds = getBounds();
			int width = bounds.width() - mPadding.left - mPadding.right;
			int height = bounds.height() - mPadding.top - mPadding.bottom;
			mRadius = Math.min(width, height) * 0.9f / 2f;
			mCx = mPadding.left + width / 2;
			mCy = mPadding.top + height / 2;
			mOval = new RectF(mCx - mRadius, mCy - mRadius, mCx + mRadius, mCy + mRadius);
		}

		public void setBackgroundColor(int color) {
			mBackgroundPaint.setColor(color);
		}

		public void setPrimaryColor(int color) {
			mPrimaryArcPaint.setColor(color);
		}

		public void setSecondaryColor(int color) {
			mSecondaryArcPaint.setColor(color);
		}

		public void setCountTextColor(int color) {
			mCountTextPaint.setColor(color);
		}

		public void setCaptionTextColor(int color) {
			mCaptionTextPaint.setColor(color);
		}

		public void setRingThickness(int thickness) {
			mPrimaryArcPaint.setStrokeWidth(thickness);
			mSecondaryArcPaint.setStrokeWidth(thickness);
		}

		public void setCountTextSize(float pixels) {
			mCountTextPaint.setTextSize(pixels);
		}

		public void setCaptionTextSize(float pixels) {
			mCaptionTextPaint.setTextSize(pixels);
		}

		public void setCountTextStyle(int style) {
			mCountTextPaint.setTypeface(gleanTypeface(style));
		}

		public void setCaptionTextStyle(int style) {
			mCaptionTextPaint.setTypeface(gleanTypeface(style));
		}

		private Typeface gleanTypeface(int style) {
			Typeface tf = null;
			switch (style) {
			case BOLD:
				tf = FontCache.getTypeface(FontCache.Font.ROBOTO_BOLD);
				break;
			default:
				tf = Typeface.DEFAULT;
				break;
			}
			return tf;
		}

		@Keep
		public void setPercent(float filled) {
			mFilledPercent = filled;
			invalidateSelf();
		}

		public float getPercent() {
			return mFilledPercent;
		}

		@Keep
		public void setCount(float count) {
			mCountText = null;
			if (count != mCount) {
				mCount = count;
				invalidateSelf();
			}
		}

		public float getCount() {
			return mCount;
		}

		public void setCountText(String text) {
			mCountText = text;
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
			float textSize = mCountTextPaint.getTextSize();

			float yOffset = mCaption == null ? -((mCountTextPaint.descent() + mCountTextPaint.ascent()) / 2) : 0;

			// Odometer style count
			if (mCountText == null) {
				canvas.save();
				canvas.clipRect(0, mCy - textSize + yOffset, canvas.getWidth(), mCy + 4 + yOffset);
				String countString = Integer.toString(Math.round(mCount));
				float fraction = mCount - Math.round(mCount);
				float y = mCy - fraction * textSize + yOffset;
				canvas.drawText(countString, mCx, y, mCountTextPaint);
				if (y != 0) {
					String nextString = Integer.toString(Math.round(mCount) + 1);
					canvas.drawText(nextString, mCx, y + textSize, mCountTextPaint);
				}
				canvas.restore();
			}

			// Simple user defined count text
			else if (!TextUtils.isEmpty(mCountText)) {
				canvas.drawText(mCountText, mCx, mCy + yOffset, mCountTextPaint);
			}

			// Caption
			if (!TextUtils.isEmpty(mCaption)) {
				canvas.drawText(mCaption, mCx, mCy + mCaptionTextPaint.getTextSize() * 1.6f, mCaptionTextPaint);
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
