package com.expedia.bookings.rail.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.rail.data.RailTravelMediumDrawableProvider
import com.expedia.bookings.data.rail.responses.RailLegOption
import kotlin.properties.Delegates

class RailResultsTimelineWidget(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    val separatorDrawable: Drawable?
    val iconRect: Rect
    var horizontalSpacing by Delegates.notNull<Int>()
    var drawableHeight by Delegates.notNull<Int>()
    var caretPadding by Delegates.notNull<Int>()

    private var leg by Delegates.notNull<RailLegOption>()

    init {
        separatorDrawable = ContextCompat.getDrawable(context, R.drawable.caret)
        horizontalSpacing = resources.getDimension(R.dimen.rail_timeline_spacing).toInt()
        iconRect = Rect(0, 0, 0, 0)
    }

    fun updateLeg(leg: RailLegOption) {
        this.leg = leg
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawableHeight = measuredHeight
        caretPadding = (measuredHeight.toFloat() / 5.0f).toInt()

        iconRect.set(0, 0, 0, drawableHeight)
        var first = true

        leg.travelSegmentList.forEach { segment ->
            if (!first) {
                // put down a separator first as long as we're not the first icon being drawn
                iconRect.right = iconRect.left + (((separatorDrawable?.intrinsicWidth?.toFloat()?.div(separatorDrawable.intrinsicHeight.toFloat()))?.times(drawableHeight - (2 * caretPadding)))?.toInt() ?: 0)
                iconRect.top = caretPadding
                iconRect.bottom = measuredHeight - caretPadding
                separatorDrawable?.bounds = iconRect
                separatorDrawable?.draw(canvas)
                iconRect.left = iconRect.right + horizontalSpacing
            }
            val relevantDrawable = ContextCompat.getDrawable(context, RailTravelMediumDrawableProvider.findMappedDrawable(segment.travelMedium.travelMediumCode))
            iconRect.top = 0
            iconRect.bottom = measuredHeight
            iconRect.right = iconRect.left + ((relevantDrawable?.intrinsicWidth?.toFloat()?.div(relevantDrawable.intrinsicHeight.toFloat())?.times(drawableHeight.toFloat())?.toInt()) ?: 0)
            relevantDrawable?.bounds = iconRect
            relevantDrawable?.draw(canvas)

            iconRect.left = iconRect.right + horizontalSpacing
            first = false
        }
    }
}
