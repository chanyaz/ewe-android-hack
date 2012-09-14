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

	private boolean mEnabled = true;

	private Paint mFadePaint;

	public FadingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mFadePaint = new Paint();
		mFadePaint.setXfermode(new PorterDuffXfermode(Mode.XOR));
		mFadePaint.setFilterBitmap(false);
	}

	public void setFadeRange(int startY, int endY) {
		mStartFadeY = startY;
		mEndFadeY = endY;

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
	protected void onDraw(Canvas canvas) {
		Rect rect = canvas.getClipBounds();
		int size = mEndFadeY - mStartFadeY;

		if (!mEnabled || size <= 0 || mStartFadeY < rect.top - size || mEndFadeY > rect.bottom + size) {
			// Short circuit the fade drawing if unnecessary
			super.onDraw(canvas);
		}
		else {
			// Save the previously drawn layer (in the fading area)
			canvas.saveLayer(rect.left, rect.top + mStartFadeY, rect.right, rect.top + mEndFadeY, null, SAVE_FLAGS);

			// Clip to just the area we want to draw (helps performance quite a bit)
			canvas.clipRect(rect.left, rect.top + mStartFadeY, rect.right, rect.bottom);

			// Draw the blurred image
			super.onDraw(canvas);

			// Alpha mask the blurred image
			mFadePaint.setShader(new LinearGradient(0, rect.top + mStartFadeY, 0, rect.top
					+ mEndFadeY, ALPHA_START, ALPHA_END, Shader.TileMode.CLAMP));
			canvas.drawRect(rect.left, rect.top + mStartFadeY, rect.right, rect.top + mEndFadeY, mFadePaint);

			// Restore the layer
			canvas.restore();
		}
	}
}
