package com.expedia.bookings.widget;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

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
import android.text.TextUtils;
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
	private Paint mTripFilledPaint;
	private TextPaint mTextPaint;

	private boolean mSpecifiedTextSize = false;

	private FlightLeg mFlightLeg;
	private FlightLeg mFlightLegTwo;

	// Min/max time are the minimum and maximum times for the entire result set (not just this flight)
	private DateTime mMinTime;
	private DateTime mMaxTime;

	// Dimensions loaded in resources
	private float mMinPaddingBetweenLabels;

	// Sometimes we want extra space between our circles and our text
	private float mWaypointTextTopMargin = 0;

	// These are pre-calculated for each draw routine
	private boolean mDirty; // If true, means we need to recalculate everything
	private float mCircleDiameter;

	// If true, the View draws itself differently
	private boolean mIsRoundTrip = false;

	private List<DrawComponent> mDrawComponents = new ArrayList<DrawComponent>();
	float[] pts;

	private enum DrawType {
		AIRPORT_START_END,
		AIRPORT_LAYOVER,
		FLIGHT_LINE,
		EMPTY_SPACE
	}

	private class DrawComponent {
		private DrawType mDrawType;

		private float mStartLeft;

		private float mWidth;

		private String mAirportCode;

		private boolean mCircleFilled;

		private boolean isFlight() {
			return mDrawType == DrawType.FLIGHT_LINE;
		}

		private float getWidth() {
			if (mDrawType == DrawType.AIRPORT_START_END) {
				return mCircleDiameter;
			}
			else {
				return mWidth;
			}
		}

	}

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

		mTripFilledPaint = new Paint();
		mTripFilledPaint.setColor(lineColor);
		mTripFilledPaint.setStrokeWidth(r.getDimension(R.dimen.flight_trip_view_stroke_width));
		mTripFilledPaint.setStyle(Style.FILL_AND_STROKE);
		mTripFilledPaint.setAntiAlias(true);

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

	public void setUp(FlightLeg flightLeg, DateTime minTime, DateTime maxTime) {
		mFlightLeg = flightLeg;
		mMinTime = minTime;
		mMaxTime = maxTime;

		mDirty = true;

		invalidate();
	}

	public void setUp(Flight flight, DateTime minTime, DateTime maxTime) {
		FlightLeg pseudoLeg = new FlightLeg();
		pseudoLeg.addSegment(flight);
		setUp(pseudoLeg, minTime, maxTime);
	}

	public void setUpRoundTrip(FlightLeg flightLeg, FlightLeg legTwo) {
		mIsRoundTrip = true;
		mFlightLeg = flightLeg;
		mFlightLegTwo = legTwo;

		mDirty = true;

		invalidate();
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
			if (mIsRoundTrip) {
				calculateWidthsRoundTrip();
			}
			else {
				calculateWidths();
			}
			mDirty = false;
		}

		long start;
		if (DEBUG) {
			start = System.nanoTime();
		}

		// V2
		float height = getHeight();
		float left = mDrawComponents.get(0).mStartLeft;
		float circleBottom = (height - mWaypointTextTopMargin) / 2.0f;
		float circleCenter = (height - mWaypointTextTopMargin) / 4.0f;
		float halfStrokeWidth = mTripPaint.getStrokeWidth() / 2;
		int numPts = 0;

		for (DrawComponent drawComponent : mDrawComponents) {
			boolean isFlight = drawComponent.isFlight();
			float currWidth = drawComponent.getWidth();

			if (isFlight) {
				pts[numPts] = left - halfStrokeWidth;
				pts[numPts + 1] = circleCenter;
				pts[numPts + 2] = left + currWidth + halfStrokeWidth;
				pts[numPts + 3] = circleCenter;
				numPts += 4;
			}
			else if (drawComponent.mDrawType == DrawType.EMPTY_SPACE) {
				// No drawing takes place, we just advance left for the next component
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
					Paint paint = drawComponent.mCircleFilled ? mTripFilledPaint : mTripPaint;
					canvas.drawOval(mCircleBounds, paint);
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
				String airportCode = drawComponent.mAirportCode;
				if (!TextUtils.isEmpty(airportCode)) {
					canvas.drawText(airportCode, mCircleBounds.centerX(), height - mTextPaint.descent(), mTextPaint);
				}
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

	/**
	 * This method constructs a "hardcoded" set of DrawComponents, such that a beautiful and centered
	 * representation of the round-trip flight is drawn.
	 */
	private void calculateWidthsRoundTrip() {
		int width = getWidth();
		int height = getHeight();
		mCircleDiameter = (height - mWaypointTextTopMargin) / 2.0f;

		mDrawComponents = new ArrayList<DrawComponent>();

		// Arrive and leave from the same airport
		if (mFlightLeg.getAirport(false).equals(mFlightLegTwo.getAirport(true))) {
			DrawComponent start = new DrawComponent();
			start.mDrawType = DrawType.AIRPORT_START_END;
			start.mAirportCode = mFlightLeg.getAirport(true).mAirportCode;
			start.mStartLeft = width * 0.19f;

			float circleAndLineWeight = 0.275f;
			float flightLineWidth = circleAndLineWeight * width - mCircleDiameter;

			DrawComponent startMiddle = new DrawComponent();
			startMiddle.mDrawType = DrawType.FLIGHT_LINE;
			startMiddle.mWidth = flightLineWidth;

			DrawComponent middle = new DrawComponent();
			middle.mDrawType = DrawType.AIRPORT_START_END;
			middle.mCircleFilled = true;
			middle.mAirportCode = mFlightLegTwo.getAirport(true).mAirportCode;

			DrawComponent middleEnd = new DrawComponent();
			middleEnd.mDrawType = DrawType.FLIGHT_LINE;
			middleEnd.mWidth = flightLineWidth;

			DrawComponent end = new DrawComponent();
			end.mDrawType = DrawType.AIRPORT_START_END;
			end.mAirportCode = mFlightLegTwo.getAirport(false).mAirportCode;

			mDrawComponents.add(start);
			mDrawComponents.add(startMiddle);
			mDrawComponents.add(middle);
			mDrawComponents.add(middleEnd);
			mDrawComponents.add(end);
		}

		// For roundtrip flights like SFO -> JFK and EWR -> SFO
		else {
			DrawComponent departStart = new DrawComponent();
			departStart.mDrawType = DrawType.AIRPORT_START_END;
			departStart.mAirportCode = mFlightLeg.getAirport(true).mAirportCode;
			departStart.mStartLeft = width * 0.1485f;

			float circleAndLineWeight = 0.248f;
			float flightLineWidth = circleAndLineWeight * width - mCircleDiameter;

			DrawComponent departLine = new DrawComponent();
			departLine.mDrawType = DrawType.FLIGHT_LINE;
			departLine.mWidth = flightLineWidth;

			DrawComponent departEnd = new DrawComponent();
			departEnd.mDrawType = DrawType.AIRPORT_START_END;
			departEnd.mCircleFilled = true;
			departEnd.mAirportCode = mFlightLeg.getAirport(false).mAirportCode;

			DrawComponent emptyMiddle = new DrawComponent();
			emptyMiddle.mDrawType = DrawType.EMPTY_SPACE;
			emptyMiddle.mWidth = width * 0.0947f;

			DrawComponent returnStart = new DrawComponent();
			returnStart.mDrawType = DrawType.AIRPORT_START_END;
			returnStart.mCircleFilled = true;
			returnStart.mAirportCode = mFlightLegTwo.getAirport(true).mAirportCode;

			DrawComponent returnLine = new DrawComponent();
			returnLine.mDrawType = DrawType.FLIGHT_LINE;
			returnLine.mWidth = flightLineWidth;

			DrawComponent returnEnd = new DrawComponent();
			returnEnd.mDrawType = DrawType.AIRPORT_START_END;
			returnEnd.mAirportCode = mFlightLegTwo.getAirport(false).mAirportCode;

			mDrawComponents.add(departStart);
			mDrawComponents.add(departLine);
			mDrawComponents.add(departEnd);
			mDrawComponents.add(emptyMiddle);
			mDrawComponents.add(returnStart);
			mDrawComponents.add(returnLine);
			mDrawComponents.add(returnEnd);
		}
		pts = new float[getNumPoints()];
	}

	private void calculateWidths() {
		long start;
		if (DEBUG) {
			start = System.nanoTime();
		}

		mDrawComponents = new ArrayList<DrawComponent>();
		DrawComponent firstAirportDC = new DrawComponent();
		firstAirportDC.mDrawType = DrawType.AIRPORT_START_END;
		firstAirportDC.mAirportCode = mFlightLeg.getFirstWaypoint().mAirportCode;
		mDrawComponents.add(firstAirportDC);

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
				text = mFlightLeg.getSegment(a).getDestinationWaypoint().mAirportCode;
			}

			float textWidth = mTextPaint.measureText(text);
			if (textWidth > maxTextWidth) {
				maxTextWidth = textWidth;
			}
		}
		float sidePadding = Math.max(0, (maxTextWidth - circleDiameter) / 2.0f);
		float minLineWidth = Math.max(sidePadding * 2 + mMinPaddingBetweenLabels, circleDiameter);

		// Make sure we've got a min/max time that represents the entire width of the View
		DateTime minTime = (mMinTime != null) ? mMinTime : mFlightLeg.getFirstWaypoint().getBestSearchDateTime();
		DateTime maxTime = (mMaxTime != null) ? mMaxTime : mFlightLeg.getLastWaypoint().getBestSearchDateTime();

		// Calculate the ranges.  If there is a min/max time available,
		// calibrate to that (otherwise assume we have the full width for the trip view)
		long tripRangeMillis = mFlightLeg.getDurationFromWaypoints();
		long totalRangeMillis = maxTime.getMillis() - minTime.getMillis();
		float totalPossibleWidth = width - (2 * sidePadding);
		float totalAvailableWidth = width - (2 * sidePadding) - (2 * circleDiameter); // Discount the start/end circles
		float tripPreferredWidth = (tripRangeMillis / (float) totalRangeMillis) * totalAvailableWidth;
		float tripActualWidth = tripPreferredWidth;

		// Make a first pass over the flight data to calculate the widths.
		// Determine ideal width for each flight/layover + how much extra width can be reallocated
		int numWidths = mFlightLeg.getSegmentCount() * 2 - 1;
		float totalExtraWidth = 0;
		for (int a = 0; a < numWidths; a++) {
			DrawComponent drawComponent = new DrawComponent();

			long startMillis, endMillis;
			boolean isFlight = a % 2 == 0;
			if (isFlight) {
				// Flight
				drawComponent.mDrawType = DrawType.FLIGHT_LINE;
				Flight segment = mFlightLeg.getSegment(a / 2);
				startMillis = segment.getOriginWaypoint().getBestSearchDateTime().getMillis();
				endMillis = segment.getDestinationWaypoint().getBestSearchDateTime().getMillis();
			}
			else {
				// Layover
				drawComponent.mDrawType = DrawType.AIRPORT_LAYOVER;
				Flight flight1 = mFlightLeg.getSegment((a - 1) / 2);
				Flight flight2 = mFlightLeg.getSegment((a + 1) / 2);
				drawComponent.mAirportCode = flight1.getDestinationWaypoint().mAirportCode;
				startMillis = flight1.getDestinationWaypoint().getBestSearchDateTime().getMillis();
				endMillis = flight2.getOriginWaypoint().getBestSearchDateTime().getMillis();
			}
			// Ensure we don't go over/under our max/min (this can happen if one of our intermediate
			// waypoints gets rerouted or the max min values provided are bunk)
			startMillis = startMillis < minTime.getMillis() ? minTime.getMillis() : startMillis;
			endMillis = endMillis > maxTime.getMillis() ? maxTime.getMillis() : endMillis;

			// Calculate the width as fraction of totalWidth, based on its weight (i.e width)
			long durationMillis = endMillis - startMillis;
			drawComponent.mWidth = (durationMillis / (float) totalRangeMillis) * totalAvailableWidth;

			// Now that we know the width, calculate how much we could shrink this width if necessary
			float minWidth = (isFlight) ? minLineWidth : circleDiameter;
			if (drawComponent.mWidth > minWidth) {
				totalExtraWidth += drawComponent.mWidth - minWidth;
			}

			mDrawComponents.add(drawComponent);
		}

		// Make a second pass over the widths.
		// Ensure that all widths meet the minimum required length, taking width where there was extra
		// found before (or increasing the width of the entire graph if there is not enough)
		for (int a = 1; a < numWidths + 1; a++) {
			DrawComponent drawComponent = mDrawComponents.get(a);
			boolean isFlight = (a - 1) % 2 == 0;
			float aMinWidth = (isFlight) ? minLineWidth : circleDiameter;
			if (drawComponent.mWidth < aMinWidth) {
				float aWidthNeeded = aMinWidth - drawComponent.mWidth;
				if (aWidthNeeded >= 0.5) {
					if (totalExtraWidth >= 0.5) {
						float extraWidthUsed = 0;
						for (int b = 1; b < numWidths + 1; b++) {
							if (a == b) {
								continue;
							}

							DrawComponent drawComponent2 = mDrawComponents.get(b);

							float bMinWidth = ((b - 1) % 2 == 0) ? minLineWidth : circleDiameter;
							if (drawComponent2.mWidth > bMinWidth) {
								float bExtraWidth = drawComponent2.mWidth - bMinWidth;
								float bAlteration = (bExtraWidth / totalExtraWidth) * aWidthNeeded;
								bAlteration = Math.min(bAlteration, bExtraWidth);
								drawComponent2.mWidth -= bAlteration;
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

					drawComponent.mWidth = aMinWidth;
				}
			}
		}

		DrawComponent endAirport = new DrawComponent();
		endAirport.mDrawType = DrawType.AIRPORT_START_END;
		endAirport.mAirportCode = mFlightLeg.getAirport(false).mAirportCode;
		mDrawComponents.add(endAirport);

		// Determine where to start the graph
		DateTime firstCal = mFlightLeg.getFirstWaypoint().getBestSearchDateTime();
		long startMillis = firstCal.getMillis() - minTime.getMillis();
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
		DrawComponent component = mDrawComponents.get(0);
		component.mStartLeft = left;

		if (DEBUG) {
			Log.d("FlightTripView calc: " + ((System.nanoTime() - start) / 1000) + " microseconds");
		}
		pts = new float[getNumPoints()];
	}

	/**
	 * Return the number of points required for a Canvas.drawLines() call, based upon the content
	 * of mDrawComponents. Each line is drawn from 4 consecutive floats, x0, y0, x1, y1.
	 */
	private int getNumPoints() {
		int total = 0;
		for (DrawComponent comp : mDrawComponents) {
			if (comp.mDrawType == DrawType.AIRPORT_LAYOVER) {
				total += 2;
			}
			else if (comp.mDrawType == DrawType.FLIGHT_LINE) {
				total += 1;
			}
		}
		return 4 * (total);
	}

}
