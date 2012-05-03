package com.expedia.bookings.widget;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightTripView extends View {

	private Paint mTripPaint;
	private Paint mTempPaint;

	private FlightLeg mFlightLeg;

	// Min/max time are the minimum and maximum times for the entire result set (not just this flight)
	private Calendar mMinTime;
	private Calendar mMaxTime;

	public FlightTripView(Context context, AttributeSet attrs) {
		super(context, attrs);

		mTripPaint = new Paint();
		mTripPaint.setColor(context.getResources().getColor(R.color.flight_trip));
		mTripPaint.setStrokeWidth(10);
		mTripPaint.setStyle(Style.STROKE);
		mTripPaint.setAntiAlias(true);

		mTempPaint = new Paint();
		mTempPaint.setColor(Color.RED);
	}

	public void setUp(FlightLeg flightLeg, Calendar minTime, Calendar maxTime) {
		mFlightLeg = flightLeg;
		mMinTime = minTime;
		mMaxTime = maxTime;

		invalidate();
	}

	// TODO: OPTIMIZE DRAWING CODE!
	// This is all written for EASE of coding at the moment.  Once that's all been figured out,
	// optimize some of the calls

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Calculate the bounds based on the min
		// ADD THIS WHEN LEG INFO IS CORRECT -  Because it's random right now, we end up with wildly
		// bad answers here.
		/*
		long minTime = DateTimeUtils.getTimeInGMT(mMinTime).getTime();
		long maxTime = DateTimeUtils.getTimeInGMT(mMaxTime).getTime();
		long startTime = DateTimeUtils.getTimeInGMT(mFlightLeg.getSegment(0).mOrigin.getMostRelevantDateTime())
				.getTime();
		long endTime = DateTimeUtils.getTimeInGMT(
				mFlightLeg.getSegment(mFlightLeg.getSegmentCount() - 1).mDestination.getMostRelevantDateTime())
				.getTime();
		long duration = maxTime - minTime;

		float left = ((float) (startTime - minTime) / (float) duration) * canvas.getWidth();
		float right = ((float) (endTime - minTime) / (float) duration) * canvas.getWidth();
		*/

		float left = 0;
		float right = canvas.getWidth();

		RectF legBounds = new RectF(left, 0, right, canvas.getHeight());
		drawLeg(canvas, legBounds);
	}

	// Draws the current FlightLeg within specified bounds
	private void drawLeg(Canvas canvas, RectF bounds) {
		// Info on the leg
		int numSegments = mFlightLeg.getSegmentCount();

		// Drawing info
		RectF[] waypoints = new RectF[numSegments + 1];

		float halfStrokeWidth = mTripPaint.getStrokeWidth() / 2;
		float width = bounds.width();
		float height = bounds.height();
		float centerY = bounds.centerY();

		// Starting point
		waypoints[0] = drawWaypoint(canvas, new RectF(0, 0, height, height));

		// Layovers
		if (numSegments > 1) {
			long startTime = DateTimeUtils.getTimeInGMT(mFlightLeg.getSegment(0).mOrigin.getMostRelevantDateTime())
					.getTime();
			long endTime = DateTimeUtils.getTimeInGMT(
					mFlightLeg.getSegment(numSegments - 1).mDestination.getMostRelevantDateTime()).getTime();
			long duration = endTime - startTime;

			for (int a = 1; a < numSegments; a++) {
				Flight lastFlight = mFlightLeg.getSegment(a - 1);
				Flight nextFlight = mFlightLeg.getSegment(a);

				long layoverStart = DateTimeUtils.getTimeInGMT(lastFlight.mDestination.getMostRelevantDateTime())
						.getTime();
				long layoverEnd = DateTimeUtils.getTimeInGMT(nextFlight.mOrigin.getMostRelevantDateTime()).getTime();
				float left = ((float) (layoverStart - startTime) / (float) duration) * width;
				float right = ((float) (layoverEnd - startTime) / (float) duration) * width;

				waypoints[a] = drawWaypoint(canvas, new RectF(left, 0, right, height));
			}
		}

		// Ending point
		waypoints[numSegments] = drawWaypoint(canvas, new RectF(width - height, 0, width, height));

		// Draw lines between each waypoint
		for (int a = 1; a < waypoints.length; a++) {
			canvas.drawLine(waypoints[a - 1].right - halfStrokeWidth, centerY, waypoints[a].left + halfStrokeWidth,
					centerY, mTripPaint);
		}
	}

	/**
	 * Draws a waypoint in the bounds requested.
	 * 
	 * @param canvas the canvas to draw to
	 * @param bounds the bounds for the waypoint
	 * @return the bounds of the waypoint ultimately drawn
	 */
	private RectF drawWaypoint(Canvas canvas, RectF bounds) {
		float halfStrokeWidth = mTripPaint.getStrokeWidth() / 2;
		float width = bounds.width();
		float height = bounds.height();
		float halfHeight = height / 2;

		if (width < height) {
			float diff = (height - width) / 2;
			bounds = new RectF(bounds.left - diff, bounds.top, bounds.right + diff, bounds.bottom);
			width = height;
		}

		if (width == height) {
			RectF circle = new RectF(bounds.left + halfStrokeWidth, bounds.top + halfStrokeWidth, bounds.right
					- halfStrokeWidth, bounds.bottom - halfStrokeWidth);
			canvas.drawOval(circle, mTripPaint);
			return bounds;
		}
		else {
			// Draw end arcs
			RectF left = new RectF(bounds.left + halfStrokeWidth, bounds.top + halfStrokeWidth, bounds.left + height
					- halfStrokeWidth, bounds.bottom - halfStrokeWidth);
			RectF right = new RectF(bounds.right - height + halfStrokeWidth, bounds.top + halfStrokeWidth, bounds.right
					- halfStrokeWidth, bounds.bottom - halfStrokeWidth);

			canvas.drawArc(left, 90, 180, false, mTripPaint);
			canvas.drawArc(right, 270, 180, false, mTripPaint);

			// Draw lines between arcs
			canvas.drawLine(left.right - halfHeight, halfStrokeWidth, right.left + halfHeight, halfStrokeWidth,
					mTripPaint);
			canvas.drawLine(left.right - halfHeight, height - halfStrokeWidth, right.left + halfHeight, height
					- halfStrokeWidth, mTripPaint);
			return bounds;
		}
	}
}
