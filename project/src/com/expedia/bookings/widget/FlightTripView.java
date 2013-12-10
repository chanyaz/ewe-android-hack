package com.expedia.bookings.widget;

import java.util.Calendar;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
import com.expedia.bookings.utils.FontCache;
import com.mobiata.android.Log;
import com.mobiata.flightlib.data.Flight;

public class FlightTripView extends View {

	private static final boolean DEBUG = false;

	private static final int TYPEFACE_MEDIUM = 0;

	private Paint mTripPaint;
	private TextPaint mTextPaint;

	private boolean mSpecifiedTextSize = false;

	private FlightLeg mFlightLeg;

	// Min/max time are the minimum and maximum times for the entire result set (not just this flight)
	private Calendar mMinTime;
	private Calendar mMaxTime;

	// Dimensions loaded in resources
	private float mMinPaddingBetweenLabels;

	// Sometimes we want extra space between our circles and our text
	private float mWaypointTextTopMargin = 0;

	// These are pre-calculated for each draw routine
	private boolean mDirty; // If true, means we need to recalculate everything
	private float mCircleDiameter;
	private int mNumWidths;
	private float[] mWidths;
	private float mStartLeft;

	// Pre-allocate for rendering
	private RectF mCircleBounds;
	private RectF mLeftArcBounds;
	private RectF mRightArcBounds;

	public FlightTripView(Context context, AttributeSet attrs) {
		super(context, attrs);

		Resources r = context.getResources();

		int lineColor = r.getColor(R.color.flight_trip);
		int textColor = r.getColor(R.color.airport_text);
		float textSize = -1.0f;
		int typeface = -1;

		if (attrs != null) {
			TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FlightTripView, 0, 0);
			lineColor = ta.getColor(R.styleable.FlightTripView_flightLineColor, r.getColor(R.color.flight_trip));
			textColor = ta.getColor(R.styleable.FlightTripView_waypointTextColor, r.getColor(R.color.airport_text));
			textSize = ta.getDimension(R.styleable.FlightTripView_waypointTextSize, -1.0f);
			typeface = ta.getInt(R.styleable.FlightTripView_waypointTextTypeface, -1);
			mWaypointTextTopMargin = ta.getDimension(R.styleable.FlightTripView_waypointTextTopMargin, 0);
			ta.recycle();
		}

		mMinPaddingBetweenLabels = r.getDimension(R.dimen.flight_line_min_padding_between_labels);

		mTripPaint = new Paint();
		mTripPaint.setColor(lineColor);
		mTripPaint.setStrokeWidth(r.getDimension(R.dimen.flight_trip_view_stroke_width));
		mTripPaint.setStyle(Style.STROKE);
		mTripPaint.setAntiAlias(true);

		mTextPaint = new TextPaint();
		mTextPaint.setTextAlign(Align.CENTER);
		mTextPaint.setColor(textColor);
		if (typeface == TYPEFACE_MEDIUM) {
			mTextPaint.setTypeface(FontCache.getTypeface(FontCache.Font.ROBOTO_MEDIUM));
		}
		else {
			mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		}
		if (textSize != -1.0f) {
			mSpecifiedTextSize = true;
			mTextPaint.setTextSize(textSize);
		}
		mTextPaint.setAntiAlias(true);

		mCircleBounds = new RectF();
		mLeftArcBounds = new RectF();
		mRightArcBounds = new RectF();
	}

	public void setUp(FlightLeg flightLeg, Calendar minTime, Calendar maxTime) {
		mFlightLeg = flightLeg;
		mMinTime = minTime;
		mMaxTime = maxTime;

		mDirty = true;

		invalidate();
	}

	public void setUp(Flight flight, Calendar minTime, Calendar maxTime) {
		FlightLeg pseudoLeg = new FlightLeg();
		pseudoLeg.addSegment(flight);
		setUp(pseudoLeg, minTime, maxTime);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mDirty = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mFlightLeg == null) {
			// If we don't have this setup, don't bother drawing anything!
			return;
		}

		if (mDirty) {
			calculateWidths();
			mDirty = false;
		}

		long start;
		if (DEBUG) {
			start = System.nanoTime();
		}

		// Draw each waypoint and flight line
		float height = getHeight();
		float left = mStartLeft;
		float circleBottom = (height - mWaypointTextTopMargin) / 2.0f;
		float circleCenter = (height - mWaypointTextTopMargin) / 4.0f;
		float halfStrokeWidth = mTripPaint.getStrokeWidth() / 2;
		float[] pts = new float[4 * (mNumWidths * 2 - mFlightLeg.getSegmentCount() + 1)]; // Use upper bound # of lines
		int numPts = 0;
		for (int a = -1; a <= mNumWidths; a++) {
			boolean isFlight = a % 2 == 0;
			float currWidth = (a >= 0 && a < mNumWidths) ? mWidths[a] : mCircleDiameter;

			if (isFlight) {
				pts[numPts] = left - halfStrokeWidth;
				pts[numPts + 1] = circleCenter;
				pts[numPts + 2] = left + currWidth + halfStrokeWidth;
				pts[numPts + 3] = circleCenter;
				numPts += 4;
			}
			else {
				// Draw circle
				mCircleBounds.left = left + halfStrokeWidth;
				mCircleBounds.top = mTripPaint.getStrokeWidth() / 2.0f;
				mCircleBounds.right = left + currWidth - halfStrokeWidth;
				mCircleBounds.bottom = circleBottom - halfStrokeWidth;
				float bHeight = mCircleBounds.height();
				if (mCircleBounds.width() - bHeight < .1f) {
					// Draw a circle (as an oval)
					canvas.drawOval(mCircleBounds, mTripPaint);
				}
				else {
					// Draw the arcs
					mLeftArcBounds.left = mCircleBounds.left;
					mLeftArcBounds.top = mCircleBounds.top;
					mLeftArcBounds.right = mCircleBounds.left + bHeight;
					mLeftArcBounds.bottom = mCircleBounds.bottom;

					mRightArcBounds.left = mCircleBounds.right - bHeight;
					mRightArcBounds.top = mCircleBounds.top;
					mRightArcBounds.right = mCircleBounds.right;
					mRightArcBounds.bottom = mCircleBounds.bottom;

					canvas.drawArc(mLeftArcBounds, 90, 180, false, mTripPaint);
					canvas.drawArc(mRightArcBounds, 270, 180, false, mTripPaint);

					// Draw the lines between the arcs
					float halfHeight = bHeight / 2;
					pts[numPts] = mLeftArcBounds.right - halfHeight - 1;
					pts[numPts + 1] = halfStrokeWidth;
					pts[numPts + 2] = mRightArcBounds.left + halfHeight + 1;
					pts[numPts + 3] = halfStrokeWidth;
					numPts += 4;

					pts[numPts] = mLeftArcBounds.right - halfHeight - 1;
					pts[numPts + 1] = bHeight + halfStrokeWidth;
					pts[numPts + 2] = mRightArcBounds.left + halfHeight + 1;
					pts[numPts + 3] = bHeight + halfStrokeWidth;
					numPts += 4;
				}

				// Draw the airport code
				String airportCode;
				if (a == -1) {
					airportCode = mFlightLeg.getFirstWaypoint().mAirportCode;
				}
				else {
					airportCode = mFlightLeg.getSegment(a / 2).mDestination.mAirportCode;
				}
				canvas.drawText(airportCode, mCircleBounds.centerX(), height - mTextPaint.descent(), mTextPaint);
			}
			left += currWidth;
		}

		// We draw all the lines at once later - this really optimizes rendering performance
		// on GPU-based OSes.
		canvas.drawLines(pts, 0, numPts, mTripPaint);

		if (DEBUG) {
			Log.d("FlightTripView render: " + ((System.nanoTime() - start) / 1000) + " microseconds");
		}
	}

	private void calculateWidths() {
		long start;
		if (DEBUG) {
			start = System.nanoTime();
		}

		// Setup our bounds based on the view's height/width and other factors
		int width = getWidth();
		int height = getHeight();
		float circleDiameter = (height - mWaypointTextTopMargin) / 2.0f;

		// F856: Make sure that the font padding is accounted for in the text size
		if (!mSpecifiedTextSize) {
			float fontPadding = (mTextPaint.descent() - mTextPaint.ascent()) - mTextPaint.getTextSize();
			mTextPaint.setTextSize(circleDiameter - fontPadding);
		}

		// Determine the widest text, base the side padding (and min line width) on that
		float maxTextWidth = 0;
		for (int a = -1; a < mFlightLeg.getSegmentCount(); a++) {
			String text;
			if (a == -1) {
				text = mFlightLeg.getFirstWaypoint().mAirportCode;
			}
			else {
				text = mFlightLeg.getSegment(a).mDestination.mAirportCode;
			}

			float textWidth = mTextPaint.measureText(text);
			if (textWidth > maxTextWidth) {
				maxTextWidth = textWidth;
			}
		}
		float sidePadding = Math.max(0, (maxTextWidth - circleDiameter) / 2.0f);
		float minLineWidth = Math.max(sidePadding * 2 + mMinPaddingBetweenLabels, circleDiameter);

		// Make sure we've got a min/max time that represents the entire width of the View
		Calendar minTime = (mMinTime != null) ? mMinTime : mFlightLeg.getFirstWaypoint().getBestSearchDateTime();
		Calendar maxTime = (mMaxTime != null) ? mMaxTime : mFlightLeg.getLastWaypoint().getBestSearchDateTime();

		// Calculate the ranges.  If there is a min/max time available,
		// calibrate to that (otherwise assume we have the full width for the trip view)
		long tripRangeMillis = mFlightLeg.getDuration();
		long totalRangeMillis = maxTime.getTimeInMillis() - minTime.getTimeInMillis();
		float totalPossibleWidth = width - (2 * sidePadding);
		float totalAvailableWidth = width - (2 * sidePadding) - (2 * circleDiameter); // Discount the start/end circles
		float tripPreferredWidth = (tripRangeMillis / (float) totalRangeMillis) * (float) totalAvailableWidth;
		float tripActualWidth = tripPreferredWidth;

		// Determine ideal width for each flight/layover + how much extra width can be reallocated
		int numWidths = mFlightLeg.getSegmentCount() * 2 - 1;
		float[] widths = new float[numWidths];
		float totalExtraWidth = 0;
		for (int a = 0; a < numWidths; a++) {
			long startMillis, endMillis;
			boolean isFlight = a % 2 == 0;
			if (isFlight) {
				// Flight
				Flight segment = mFlightLeg.getSegment(a / 2);
				startMillis = segment.mOrigin.getBestSearchDateTime().getTimeInMillis();
				endMillis = segment.mDestination.getBestSearchDateTime().getTimeInMillis();
			}
			else {
				// Layover
				Flight flight1 = mFlightLeg.getSegment((a - 1) / 2);
				Flight flight2 = mFlightLeg.getSegment((a + 1) / 2);
				startMillis = flight1.mDestination.getBestSearchDateTime().getTimeInMillis();
				endMillis = flight2.mOrigin.getBestSearchDateTime().getTimeInMillis();
			}
			long durationMillis = endMillis - startMillis;
			widths[a] = (durationMillis / (float) totalRangeMillis) * totalAvailableWidth;

			// Now that we know the width, calculate how much we could shrink this width if necessary
			float minWidth = (isFlight) ? minLineWidth : circleDiameter;
			if (widths[a] > minWidth) {
				totalExtraWidth += widths[a] - minWidth;
			}
		}

		// Ensure that all widths meet the minimum required length, taking width where there was extra
		// found before (or increasing the width of the entire graph if there is not enough)
		for (int a = 0; a < numWidths; a++) {
			boolean isFlight = a % 2 == 0;
			float aMinWidth = (isFlight) ? minLineWidth : circleDiameter;
			if (widths[a] < aMinWidth) {
				float aWidthNeeded = aMinWidth - widths[a];
				if (aWidthNeeded >= 0.5) {
					if (totalExtraWidth >= 0.5) {
						float extraWidthUsed = 0;
						for (int b = 0; b < numWidths; b++) {
							if (a == b) {
								continue;
							}

							float bMinWidth = (b % 2 == 0) ? minLineWidth : circleDiameter;
							if (widths[b] > bMinWidth) {
								float bExtraWidth = widths[b] - bMinWidth;
								float bAlteration = (bExtraWidth / totalExtraWidth) * aWidthNeeded;
								bAlteration = Math.min(bAlteration, bExtraWidth);
								widths[b] -= bAlteration;
								extraWidthUsed += bAlteration;
							}
						}

						totalExtraWidth -= extraWidthUsed;
						tripActualWidth += aWidthNeeded - extraWidthUsed;
					}
					else {
						// There is not enough available space, we just increase the size.
						// This will force us to slide the entire graph left/right by some
						// amount to minimize the impact of increasing the graph size
						tripActualWidth += aWidthNeeded;
					}

					widths[a] = aMinWidth;
				}
			}
		}

		// Determine where to start the graph
		long startMillis = mFlightLeg.getFirstWaypoint().getBestSearchDateTime().getTimeInMillis()
				- minTime.getTimeInMillis();
		float left = (startMillis / (float) totalRangeMillis) * totalAvailableWidth + sidePadding;
		if (tripActualWidth > tripPreferredWidth + .1) {
			left -= (tripActualWidth - tripPreferredWidth) / 2.0;
		}

		// Make sure we're not overstepping our bounds left/right still
		if (left - sidePadding + tripActualWidth + (circleDiameter * 2) > totalPossibleWidth + 0.5f) {
			left -= (left - sidePadding + tripActualWidth + (circleDiameter * 2)) - totalPossibleWidth;
		}
		if (left < sidePadding) {
			left = sidePadding;
		}

		// Put our calculations into vars for later use
		mCircleDiameter = circleDiameter;
		mNumWidths = numWidths;
		mWidths = widths;
		mStartLeft = left;

		if (DEBUG) {
			Log.d("FlightTripView calc: " + ((System.nanoTime() - start) / 1000) + " microseconds");
		}
	}
}
