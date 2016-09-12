package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.widget.RatingBar
import com.expedia.bookings.R

class BoxRatingBar(context: Context, attrs: AttributeSet) : RatingBar(context, attrs) {
    private val dividerWidth: Float
    private val cornerRadius: Float
    private val enabledPaint = Paint()
    private val disabledPaint = Paint()
    private val dividerPaint = Paint()
    private val rect = RectF()

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val a = context.obtainStyledAttributes(attrs, R.styleable.BoxRatingBar, 0, 0)
        val enabledBoxColor = a.getColor(R.styleable.BoxRatingBar_enabled_box_color, Color.parseColor("#DBDBDB"))
        val disabledBoxColor = a.getColor(R.styleable.BoxRatingBar_disabled_box_color, Color.parseColor("#F6AE24"))
        dividerWidth = a.getDimension(R.styleable.BoxRatingBar_divider_width, 2f)
        cornerRadius = a.getDimension(R.styleable.BoxRatingBar_corner_radius, 10f)
        a.recycle()

        enabledPaint.isAntiAlias = true
        enabledPaint.color = enabledBoxColor

        disabledPaint.isAntiAlias = true
        disabledPaint.color = disabledBoxColor

        dividerPaint.isAntiAlias = true
        dividerPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }

    override fun onDraw(canvas: Canvas) {
        /**
         * First it will draw a round rectangle with enabled rating color and will only have radius on left
         * The size of the rectangle will be equal to the rating.
         * Say rating is 3/5, then draw rect of size 3.
         *  __________
         * |__________|

         * Then it will draw a round rectangle with disabled rating color and will only have radius on right
         * This rectangle will start drawing next to the enabled rectangle
         * The size of the rectangle will be equal to the total star rating - rating
         * Say rating is 3/5, then draw rect of size 2 next to enabled rectangle.
         *  __________ ______
         * |__________|______|

         * Then draw separator on canvas
         *  _   _   _   _   _
         * |_| |_| |_| |_| |_|
         */

        canvas.save()
        drawEnabledRating(canvas)
        drawDisabledRating(canvas)
        drawSeparator(canvas)
        canvas.restore()
    }

    private fun drawEnabledRating(canvas: Canvas) {
        rect.left = 0f
        rect.right = canvas.width * rating / numStars
        rect.top = 0f
        rect.bottom = canvas.height.toFloat()

        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, enabledPaint)

        /**
         * If rating is not full then draw rectangle with sharp edges to remove right corner
         * the left side of rectangle is left point + corner radius, so it is do not overlap the corner radius
         * the right side of rectangle is equal to right point, so it will overlap the corner and make it sharp edge
         */
        if (rating != numStars.toFloat()) {
            rect.left = rect.left + cornerRadius
            canvas.drawRect(rect, enabledPaint)
        }
    }

    private fun drawDisabledRating(canvas: Canvas) {
        /**
         * If rating is full then do not draw disabled rectangle
         * Say rating is 3/5, then only draw disable rating.
         */
        if (rating != numStars.toFloat()) {
            rect.left = canvas.width * rating / numStars
            rect.right = canvas.width.toFloat()
            rect.top = 0f
            rect.bottom = canvas.height.toFloat()

            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, disabledPaint)

            rect.right = rect.right - cornerRadius
            canvas.drawRect(rect, disabledPaint)
        }
    }

    private fun drawSeparator(canvas: Canvas) {
        val widthOfEachBox = (canvas.width - (numStars - 1) * dividerWidth) / numStars

        for (index in 1..(numStars - 1)) {
            rect.left = index * widthOfEachBox + (index - 1) * dividerWidth
            rect.right = rect.left + dividerWidth
            rect.top = 0f
            rect.bottom = canvas.height.toFloat()

            canvas.drawRect(rect, dividerPaint)
        }
    }
}
