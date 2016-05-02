package com.expedia.bookings.widget.rail

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.rail.responses.RailSearchResponse
import java.util.HashMap
import java.util.Random
import kotlin.properties.Delegates

class RailResultsTimelineWidget(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        final val KEY_UK_TRAIN = "UK_TRAIN"
        final val KEY_LONDON_UNDERGROUND = "UK_UNDERGROUND"
        final val KEY_WALKING = "WALKING"
    }

    val separatorDrawable: Drawable
    var horizontalSpacing by Delegates.notNull<Int>()
    var drawableHeight by Delegates.notNull<Int>()
    var caretPadding by Delegates.notNull<Int>()
    val iconMap = HashMap<String, Drawable>()

    private var leg by Delegates.notNull<RailSearchResponse.LegOption>()

    init {
        separatorDrawable = ContextCompat.getDrawable(context, R.drawable.caret)

        iconMap.put(KEY_UK_TRAIN, ContextCompat.getDrawable(context, R.drawable.national_rail_icon))
        iconMap.put(KEY_LONDON_UNDERGROUND, ContextCompat.getDrawable(context, R.drawable.london_tube))
        iconMap.put(KEY_WALKING, ContextCompat.getDrawable(context, R.drawable.walking))

        horizontalSpacing = resources.getDimension(R.dimen.rail_timeline_spacing).toInt()
    }

    fun updateLeg(leg: RailSearchResponse.LegOption) {
        this.leg = leg
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        drawableHeight = measuredHeight
        caretPadding = (measuredHeight.toFloat() / 5.0f).toInt()

        var iconRect = Rect(0, 0, 0, drawableHeight)
        var first = true


        leg.travelSegmentList.forEach {
            if (!first) {
                // put down a separator first as long as we're not the first icon being drawn
                iconRect.right = iconRect.left + (( separatorDrawable.intrinsicWidth.toFloat() / separatorDrawable.intrinsicHeight.toFloat()) * (drawableHeight - (2 * caretPadding)).toFloat()).toInt()
                iconRect.top = caretPadding
                iconRect.bottom = measuredHeight - caretPadding
                separatorDrawable.bounds = iconRect
                separatorDrawable.draw(canvas)
                iconRect.left = iconRect.right + horizontalSpacing
            }
            var relevantDrawable = findMappedDrawable()
            iconRect.top = 0;
            iconRect.bottom = measuredHeight
            iconRect.right = iconRect.left + (( relevantDrawable.intrinsicWidth.toFloat() / relevantDrawable.intrinsicHeight.toFloat()) * drawableHeight.toFloat()).toInt()
            relevantDrawable.bounds = iconRect
            relevantDrawable.draw(canvas)

            iconRect.left = iconRect.right + horizontalSpacing
            first = false;
        }
    }

    private fun findMappedDrawable(): Drawable {
        val randomNum = Random().nextInt(3) //TODO use the leg to determine which drawable to use
        when (randomNum) {
            0 -> return iconMap[KEY_UK_TRAIN]!!
            1 -> return iconMap[KEY_WALKING]!!
            else -> return iconMap[KEY_LONDON_UNDERGROUND]!!
        }
    }
}

