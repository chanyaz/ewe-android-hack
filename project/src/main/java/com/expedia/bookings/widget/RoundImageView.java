package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundImageView extends ImageView {



	public RoundImageView(Context context) {
		super(context);
	}

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	private Bitmap mBitmap;
	private Matrix mMatrix;
	private Paint mPaint;

	@Override
	public void setImageBitmap(Bitmap bm) {
		updateDrawingVars(bm);
		super.setImageBitmap(bm);
	}

	@Override
	public void setImageDrawable(Drawable dr) {
		updateDrawingVars(((BitmapDrawable) dr).getBitmap());
		super.setImageDrawable(dr);
	}

	private void updateDrawingVars(Bitmap bm) {
		mBitmap = bm;
		if (mBitmap != null) {
			BitmapShader shader;
			shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setShader(shader);

			mMatrix = new Matrix();

			RectF src = new RectF();
			src.left = 0;
			src.top = 0;
			src.right = mBitmap.getWidth();
			src.bottom = mBitmap.getHeight();

			RectF dst = new RectF(getPaddingLeft(), getPaddingTop(),
				getWidth() - getPaddingRight(),
				getHeight() - getPaddingBottom());
			mMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		updateDrawingVars(mBitmap);
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			canvas.save();
			canvas.concat(mMatrix);
			canvas.drawCircle(mBitmap.getWidth() / 2, mBitmap.getHeight() / 2, mBitmap.getWidth() / 2, mPaint);
			canvas.restore();
		}
	}

}
