package com.expedia.bookings.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.jhlabs.map.Point2D;

public class FlightLineView extends View {
	public FlightLineView(Context context) {
		super(context);
		init(context, null, 0);
	}

	public FlightLineView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0);
	}

	public FlightLineView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs, defStyle);
	}

	private Path mLinePath;
	private Paint mLinePaint;

	private BitmapDrawable mPlaneDrawable;
	private Paint mErasePaint;
	private Point2D.Double mPlanePosition;
	private float mPlaneRotation;

	private void init(Context context, AttributeSet attrs, int defStyle) {
		final float density = context.getResources().getDisplayMetrics().density;
		mLinePaint = new Paint();
		mLinePaint.setColor(0xBBFFFFFF);
		mLinePaint.setStyle(Paint.Style.STROKE);
		mLinePaint.setStrokeWidth(4 * density);
		mLinePaint.setAntiAlias(true);
		mLinePaint.setStrokeCap(Paint.Cap.ROUND);

		mPlaneDrawable = new BitmapDrawable(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_tablet_flight_plane));
	}

	public void setFlightLinePoints(Point2D.Double[] points) {
		mLinePath = new Path();

		for (int i = 0; i < points.length; i++) {
			Point2D.Double point = points[i];
			if (i == 0) {
				mLinePath.moveTo((float) point.x, (float) point.y);
			}
			if (i < points.length - 1) {
				Point2D.Double next = points[i + 1];
				mLinePath.quadTo((float) point.x, (float) point.y, (float) next.x, (float) next.y);
			}
			else {
				mLinePath.lineTo((float) point.x, (float) point.y);
			}
		}
		final int whichPoint = points.length * 3 / 4;
		mPlanePosition = points[whichPoint];

		int planew = mPlaneDrawable.getIntrinsicWidth() / 2;
		int planeh = mPlaneDrawable.getIntrinsicHeight() / 2;
		mPlaneDrawable.setBounds((int) mPlanePosition.x - planew, (int) mPlanePosition.y - planeh, (int) mPlanePosition.x + planew, (int) mPlanePosition.y + planeh);

		for (int i = whichPoint; i > 0; i--) {
			if (distance(mPlanePosition, points[i]) > planew) {
				mPlaneRotation = angle(mPlanePosition, points[i]);
				break;
			}
		}

		invalidate();
	}

	public void setupErasePaint(Drawable drawable) {
		if (mPlanePosition != null) {
			int left = (int) (mPlanePosition.x - mPlaneDrawable.getIntrinsicWidth() / 2);
			int top = (int) (mPlanePosition.y - mPlaneDrawable.getIntrinsicHeight() / 2);
			Bitmap bitmap = Bitmap.createBitmap(mPlaneDrawable.getIntrinsicWidth(), mPlaneDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);

			// Draw the bg to our little bitmap
			canvas.save();
			canvas.translate(-left, -top);
			drawable.draw(canvas);
			canvas.restore();

			BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

			mErasePaint = new Paint();
			mErasePaint.setAntiAlias(true);
			mErasePaint.setShader(shader);
		}
	}

	@Override
	public void onDraw(Canvas c) {
		if (mLinePath != null) {
			c.drawPath(mLinePath, mLinePaint);
		}

		if (mPlanePosition != null && mErasePaint != null) {
			c.drawCircle((float) mPlanePosition.x, (float) mPlanePosition.y, mPlaneDrawable.getIntrinsicWidth() / 2, mErasePaint);
			c.save();
			c.rotate(mPlaneRotation, (float) mPlanePosition.x, (float) mPlanePosition.y);
			mPlaneDrawable.draw(c);
			c.restore();
		}
	}

	private float distance(Point2D.Double p1, Point2D.Double p2) {
		return (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
	}

	private float angle(Point2D.Double p1, Point2D.Double p2) {
		return (float) Math.toDegrees(Math.atan2(p2.y - p1.y, p2.x - p1.x));
	}
}
