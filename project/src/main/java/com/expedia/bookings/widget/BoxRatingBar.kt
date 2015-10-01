package com.expedia.bookings.widget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.RatingBar
import com.expedia.bookings.R
import kotlin.properties.Delegates

public class BoxRatingBar(context: Context, attrs: AttributeSet) : RatingBar(context, attrs) {
    private val dividerWidth: Float
    private val cornerRadius: Float
    private var enabledPaint: Paint by Delegates.notNull()
    private var disabledPaint: Paint by Delegates.notNull()
    private var dividerPaint: Paint by Delegates.notNull()

    init {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        val a = context.obtainStyledAttributes(attrs, R.styleable.BoxRatingBar, 0, 0)
        val enabledBoxColor = a.getColor(R.styleable.BoxRatingBar_enabled_box_color, Color.parseColor("#DBDBDB"))
        val disabledBoxColor = a.getColor(R.styleable.BoxRatingBar_disabled_box_color, Color.parseColor("#F6AE24"))
        dividerWidth = a.getDimension(R.styleable.BoxRatingBar_divider_width, 2f)
        cornerRadius = a.getDimension(R.styleable.BoxRatingBar_corner_radius, 10f)
        a.recycle()

        initPaints(enabledBoxColor, disabledBoxColor)
    }

    private fun initPaints(enabledColor: Int, disabledColor: Int) {
        enabledPaint = Paint()
        enabledPaint.isAntiAlias = true
        enabledPaint.color = enabledColor

        disabledPaint = Paint()
        disabledPaint.isAntiAlias = true
        disabledPaint.color = disabledColor

        dividerPaint = Paint()
        dividerPaint.isAntiAlias = true
        dividerPaint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
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
        drawEnabledRating(canvas)
        drawDisabledRating(canvas)
        drawSeparator(canvas)
        canvas.restore()
    }

    private fun drawEnabledRating(canvas: Canvas) {
        val left = 0f
        val right = canvas.width * rating / numStars
        val top = 0f
        val bottom = canvas.height.toFloat()

        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, enabledPaint)

        /**
         * If rating is not full then draw rectangle with sharp edges to remove right corner
         * the left side of rectangle is left point + corner radius, so it is do not overlap the corner radius
         * the right side of rectangle is equal to right point, so it will overlap the corner and make it sharp edge
         */
        if (rating != numStars.toFloat()) {
            canvas.drawRect(left + cornerRadius, top, right, bottom, enabledPaint)
        }
    }

    private fun drawDisabledRating(canvas: Canvas) {
        /**
         * If rating is full then do not draw disabled rectangle
         * Say rating is 3/5, then only draw disable rating.
         */
        if (rating != numStars.toFloat()) {
            val left = canvas.width * rating / numStars
            val right = canvas.width.toFloat()
            val top = 0f
            val bottom = canvas.height.toFloat()

            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, disabledPaint)
            canvas.drawRect(left, top, right - cornerRadius, bottom, disabledPaint)
        }
    }

    private fun drawSeparator(canvas: Canvas) {
        val widthOfEachBox = (canvas.width - (numStars - 1) * dividerWidth) / numStars

        for (index in 1..(numStars - 1)) {
            val left = index * widthOfEachBox + (index - 1) * dividerWidth
            val right = left + dividerWidth
            val top = 0f
            val bottom = canvas.height.toFloat()

            canvas.drawRect(left, top, right, bottom, dividerPaint)
        }
    }
}
