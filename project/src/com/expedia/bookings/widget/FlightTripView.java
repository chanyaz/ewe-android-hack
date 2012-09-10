package com.expedia.bookings.widget;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.expedia.bookings.R;
import com.expedia.bookings.data.FlightLeg;
import com.mobiata.flightlib.data.Flight;
import com.mobiata.flightlib.utils.DateTimeUtils;

public class FlightTripView extends View {

	// Determines how much padding is added on each side to account for
	// the airport code drawing
	private static final int PADDING = 30;

	private Paint mTripPaint;
	private Paint mTextPaint;

	private FlightLeg mFlightLeg;

	// Min/max time are the minimum and maximum times for the entire result set (not just this flight)
	private Calendar mMinTime;
	private Calendar mMaxTime;

	public FlightTripView(Context context, AttributeSet attrs) {
		super(context, attrs);

		Resources r = context.getResources();

		mTripPaint = new Paint();
		mTripPaint.setColor(context.getResources().getColor(R.color.flight_trip));
		mTripPaint.setStrokeWidth(r.getDimension(R.dimen.flight_trip_view_stroke_width));
		mTripPaint.setStyle(Style.STROKE);
		mTripPaint.setAntiAlias(true);

		mTextPaint = new TextPaint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(r.getColor(R.color.airport_text));
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setShadowLayer(.1f, 0, 1, r.getColor(R.color.airport_text_shadow));
	}

	public void setUp(FlightLeg flightLeg, Calendar minTime, Calendar maxTime) {
		mFlightLeg = flightLeg;
		mMinTime = minTime;
		mMaxTime = maxTime;

		invalidate();
	}

	public void setUp(Flight flight, Calendar minTime, Calendar maxTime) {
		FlightLeg pseudoLeg = new FlightLeg();
		pseudoLeg.addSegment(flight);
		setUp(pseudoLeg, minTime, maxTime);
	}

	// TODO: OPTIMIZE DRAWING CODE!
	// This is all written for EASE of coding at the moment.  Once that's all been figured out,
	// optimize some of the calls

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int width = getWidth();

		// Calculate the bounds based on the min
		float left, right;
		if (mMinTime != null && mMaxTime != null) {
			long minTime = DateTimeUtils.getTimeInGMT(mMinTime).getTime();
			long maxTime = DateTimeUtils.getTimeInGMT(mMaxTime).getTime();
			long startTime = DateTimeUtils.getTimeInGMT(mFlightLeg.getSegment(0).mOrigin.getMostRelevantDateTime())
					.getTime();
			long endTime = DateTimeUtils.getTimeInGMT(
					mFlightLeg.getSegment(mFlightLeg.getSegmentCount() - 1).mDestination.getMostRelevantDateTime())
					.getTime();
			long duration = maxTime - minTime;

			left = ((float) (startTime - minTime) / (float) duration) * width;
			right = ((float) (endTime - minTime) / (float) duration) * width;
		}
		else {
			left = 0;
			right = width;
		}

		// F662: Ensure that the width is at least as tall as the height
		// (otherwise drawing doesn't work properly at the moment)
		float diff = right - left;
		int height = getHeight();
		if (diff < height) {
			float missingWidth = height - diff;

			left -= missingWidth / 2.0f;
			right += missingWidth / 2.0f;
		}

		// Fuzz the edges a bit to make sure that we can draw the full airport code without falling
		// off the edge of the canvas.
		//
		// TODO: Does this need to be scaled by density of screen?
		if (left < PADDING) {
			diff = PADDING - left;
			left = PADDING;
			right = Math.min(right + diff, width - PADDING);
		}

		if (right > width - PADDING) {
			diff = width - PADDING - right;
			right = width - PADDING;
			left = Math.max(left + diff, PADDING);
		}

		RectF legBounds = new RectF(left, 0, right, getHeight());
		drawLeg(canvas, legBounds);
	}

	// Draws the current FlightLeg within specified bounds
	private void drawLeg(Canvas canvas, RectF bounds) {
		// The line has bounds above the airport codes
		RectF lineBounds = new RectF(bounds);
		lineBounds.bottom /= 2;
		RectF airportBounds = new RectF(bounds);
		airportBounds.top = airportBounds.bottom / 2;

		// Info on the leg
		int numSegments = mFlightLeg.getSegmentCount();

		// Drawing info
		RectF[] waypoints = new RectF[numSegments + 1];

		float halfStrokeWidth = mTripPaint.getStrokeWidth() / 2;
		float width = lineBounds.width();
		float height = lineBounds.height();
		float centerY = lineBounds.centerY();

		// Starting point
		waypoints[0] = drawWaypoint(canvas, new RectF(lineBounds.left, 0, height + lineBounds.left, height));

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

				waypoints[a] = drawWaypoint(canvas, new RectF(left + lineBounds.left, 0, right + lineBounds.left,
						height));
			}
		}

		// Ending point
		waypoints[numSegments] = drawWaypoint(canvas, new RectF(width - height + lineBounds.left, 0, width
				+ lineBounds.left, height));

		// Draw lines between each waypoint
		for (int a = 1; a < waypoints.length; a++) {
			canvas.drawLine(waypoints[a - 1].right - halfStrokeWidth, centerY, waypoints[a].left + halfStrokeWidth,
					centerY, mTripPaint);
		}

		// Draw an airport at each waypoint
		for (int a = 0; a < waypoints.length; a++) {
			String airportCode;
			if (a == 0) {
				airportCode = mFlightLeg.getSegment(a).mOrigin.mAirportCode;
			}
			else {
				airportCode = mFlightLeg.getSegment(a - 1).mDestination.mAirportCode;
			}

			// Adjust the top/bottom bounds to match where the airports should be drawn
			waypoints[a].top = airportBounds.top;
			waypoints[a].bottom = airportBounds.bottom;
			drawAirportCode(canvas, waypoints[a], airportCode);
		}
	}

	/**
	 * Draws a waypoint in the bounds requested.
	 * 
	 * If the bounds are too small (in other words, you could not draw a complete circle) it will
	 * expand the bounds to fit.  The height will remain constant but the width will expand.
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

	/**
	 * Draws an airport code at the specified location.
	 * 
	 * It isn't necessarily drawn *within* the bounds horizontally - rather, it's centered
	 * on those bounds.
	 * 
	 * @param canvas
	 * @param bounds
	 * @param airportCode
	 */
	private void drawAirportCode(Canvas canvas, RectF bounds, String airportCode) {
		// Setup the text to draw in the entire height of the bounds, minus any extra space needed for the
		// shadow layer
		mTextPaint.setTextSize(bounds.height());

		canvas.drawText(airportCode, bounds.centerX(), bounds.bottom, mTextPaint);
	}
}
