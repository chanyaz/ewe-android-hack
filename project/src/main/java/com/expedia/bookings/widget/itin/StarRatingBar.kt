package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import kotlin.properties.Delegates

class StarRatingBar(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val starSpacing: Float
    private var starSpacingPaint: Paint by Delegates.notNull()
    private var starDrawable: Drawable
    private var rating: Float = 0f
    private var intrinsicHeight: Int = 0
    private var intrinsicWidth: Int = 0
    private var starColor: Int = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StarRatingBar, 0, 0)
        starSpacing = a.getDimension(R.styleable.StarRatingBar_star_spacing, -1f)
        starColor = a.getColor(R.styleable.StarRatingBar_star_color, Color.parseColor("#F1B906"))
        starDrawable = a.getDrawable(R.styleable.StarRatingBar_star_drawable)
        intrinsicHeight = starDrawable.intrinsicHeight
        intrinsicWidth = starDrawable.intrinsicWidth
        a.recycle()

        starSpacingPaint = Paint()
        starSpacingPaint.isAntiAlias = true
        starSpacingPaint.color = Color.TRANSPARENT

        starDrawable.colorFilter = PorterDuffColorFilter(starColor, PorterDuff.Mode.SRC_IN)
    }

    override fun onDraw(canvas: Canvas) {
        var left = 0
        for (index in 1..(Math.ceil(rating.toDouble()).toInt())) {
            drawStar(canvas, left)
            left += intrinsicWidth
            drawSpacing(canvas, left)
            left += starSpacing.toInt()
        }
    }

    private fun drawStar(canvas: Canvas, left: Int) {
        starDrawable.setBounds(left, 0, left + starDrawable.intrinsicWidth, intrinsicHeight)
        starDrawable.draw(canvas)
    }

    private fun drawSpacing(canvas: Canvas, left: Int) {
        canvas.drawRect(left.toFloat(), 0f, left + starSpacing.toFloat(), intrinsicHeight.toFloat(), starSpacingPaint)
    }

    fun setRating(starRating: Float) {
        rating = starRating
        requestLayout()
    }

    fun getRating(): Float {
        return rating
    }

    fun getStarSpacing(): Float {
        return starSpacing
    }

    fun getStarDrawable(): Drawable {
        return starDrawable
    }

    fun setStarColor(color: Int) {
        if (starColor != color) {
            starColor = color
            starDrawable = starDrawable.mutate()
            starDrawable.colorFilter = PorterDuffColorFilter(starColor, PorterDuff.Mode.SRC_IN)
            invalidate()
        }
    }

    fun getStarColor(): Int {
        return starColor
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = starDrawable.intrinsicWidth
        var canvasWidth = rating * width + (Math.ceil(rating.toDouble()).toInt() - 1) * starSpacing
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = Math.min(canvasWidth.toInt(), View.MeasureSpec.getSize(widthMeasureSpec))
        }
        var height = starDrawable.intrinsicHeight
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }
}
