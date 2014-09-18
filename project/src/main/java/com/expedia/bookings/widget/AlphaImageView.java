package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * ImageView which lets you set the alpha on its drawing function.
 * 
 * Seems to work better than trying to set the alpha of an ImageView
 * itself.
 */
public class AlphaImageView extends ImageView {

	private static final int OPAQUE = 255;

	// Default to opaque
	private int mAlpha = OPAQUE;

	public AlphaImageView(Context context) {
		super(context);
	}

	public AlphaImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AlphaImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setDrawAlpha(int alpha) {
		mAlpha = alpha;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mAlpha != OPAQUE) {
			canvas.saveLayerAlpha(0, 0, getWidth(), getHeight(), mAlpha, Canvas.HAS_ALPHA_LAYER_SAVE_FLAG);
		}

		super.onDraw(canvas);

		if (mAlpha != OPAQUE) {
			canvas.restore();
		}
	}

}
