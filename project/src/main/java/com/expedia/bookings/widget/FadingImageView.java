package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FadingImageView extends ImageView {

	private static final int ALPHA_START = 0xFF000000;
	private static final int ALPHA_END = 0x00000000;

	private static final int SAVE_FLAGS = Canvas.HAS_ALPHA_LAYER_SAVE_FLAG;

	private int mStartFadeY;
	private int mEndFadeY;
	private int mFadeSize;

	private boolean mEnabled = true;

	private final Paint mFadePaint;

	private final Rect mBounds;

	public FadingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mFadePaint = new Paint();
		mFadePaint.setXfermode(new PorterDuffXfermode(Mode.XOR));
		mFadePaint.setFilterBitmap(false);

		mBounds = new Rect();
	}

	public void setFadeRange(int startY, int endY) {
		mStartFadeY = startY;
		mEndFadeY = endY;
		mFadeSize = endY - startY;

		mEnabled = true;

		invalidate();
	}

	public void setFadeEnabled(boolean enabled) {
		if (mEnabled != enabled) {
			mEnabled = enabled;
			invalidate();
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (changed) {
			mBounds.left = left;
			mBounds.top = top;
			mBounds.right = right;
			mBounds.bottom = bottom;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Rect rect = mBounds;

		if (!mEnabled || mFadeSize <= 0 || mStartFadeY < rect.top - mFadeSize || mEndFadeY > rect.bottom + mFadeSize) {
			// Short circuit the fade drawing if unnecessary
			super.onDraw(canvas);
		}
		else {
			// Save the previously drawn layer (in the fading area)
			canvas.saveLayer(rect.left, mStartFadeY - rect.top, rect.right, mEndFadeY - rect.top, null, SAVE_FLAGS);

			// Clip to just the area we want to draw (helps performance quite a bit)
			canvas.clipRect(rect.left, mStartFadeY - rect.top, rect.right, rect.bottom);

			// Draw the blurred image
			super.onDraw(canvas);

			// Alpha mask the blurred image
			mFadePaint.setShader(new LinearGradient(0, mStartFadeY - rect.top, 0,
					mEndFadeY - rect.top, ALPHA_START, ALPHA_END, Shader.TileMode.CLAMP));
			canvas.drawRect(rect.left, mStartFadeY - rect.top, rect.right, mEndFadeY - rect.top, mFadePaint);

			// Restore the layer
			canvas.restore();
		}
	}
}
