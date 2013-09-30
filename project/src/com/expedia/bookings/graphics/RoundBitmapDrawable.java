package com.expedia.bookings.graphics;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

public class RoundBitmapDrawable extends Drawable {
	private Bitmap mBitmap;
	private Matrix mMatrix;
	private Paint mPaint;

	public RoundBitmapDrawable(Context context, int drawableId) {
		mBitmap = BitmapFactory.decodeResource(context.getResources(), drawableId);

		BitmapShader shader;
		shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setShader(shader);

		mMatrix = new Matrix();
	}

	@Override
	public void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		mMatrix = new Matrix();

		if (bounds.right == -1 || bounds.bottom == -1) {
			// Just use the identity matrix
			return;
		}

		RectF src = new RectF();
		src.left = 0;
		src.top = 0;
		src.right = mBitmap.getWidth();
		src.bottom = mBitmap.getHeight();

		RectF dst = new RectF(bounds);
		mMatrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.save();
		canvas.concat(mMatrix);
		canvas.drawCircle(mBitmap.getWidth()/2, mBitmap.getHeight()/2, mBitmap.getWidth()/2, mPaint);
		canvas.restore();
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSPARENT;
	}

	@Override
	public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getIntrinsicWidth() {
		return mBitmap.getWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return mBitmap.getHeight();
	}

}
