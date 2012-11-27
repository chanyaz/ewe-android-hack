package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * A version of an ImageView that can block unnecessary layouts, in
 * the case that the ImageView has a set width/height and is only having
 * its image changed.
 * 
 * It can automatically detect if it has a fixed size, or you can specify
 * that it has a fixed size after layout (i.e., if the layout pass sets a size
 * and it never changes afterwards).
 * 
 * Inspired by this post: https://plus.google.com/113058165720861374515/posts/iTk4PjgeAWX
 * And this: https://gist.github.com/cf945f200dfdb5a08c19
 */
public class OptimizedImageView extends ImageView {

	private boolean mIsFixedSize = false;
	private boolean mMeasuredExactly = false;
	private boolean mBlockMeasurement = false;

	public OptimizedImageView(Context context) {
		super(context);
	}

	public OptimizedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public OptimizedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void setFixedSize(boolean isFixedSize) {
		mIsFixedSize = isFixedSize;
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		mBlockMeasurement = true;
		super.setImageDrawable(drawable);
		mBlockMeasurement = false;
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		mBlockMeasurement = true;
		super.setImageBitmap(bm);
		mBlockMeasurement = false;
	}

	@Override
	public void requestLayout() {
		if (!mBlockMeasurement || !(mMeasuredExactly || mIsFixedSize)) {
			super.requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int widthMeasureSpecMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMeasureSpecMode = MeasureSpec.getMode(heightMeasureSpec);
		mMeasuredExactly = widthMeasureSpecMode == MeasureSpec.EXACTLY && heightMeasureSpecMode == MeasureSpec.EXACTLY;
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
