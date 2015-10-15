package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.graphics.drawable.BitmapDrawable
import com.mobiata.android
import android.view.View.MeasureSpec
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import com.expedia.bookings.R
import com.mobiata.android
import com.mobiata.android.Log
import com.mobiata.android.graphics
import kotlin.properties.Delegates

public class StarRatingBar(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val starSpacing: Float
    private var starSpacingPaint: Paint by Delegates.notNull()
    private var starDrawable: Drawable
    private var rating: Float = 0f
    private var intrinsicHeight: Int = 0
    private var intrinsicWidth: Int = 0


    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StarRatingBar, 0, 0)
        starSpacing = a.getDimension(R.styleable.StarRatingBar_star_spacing, -1f)
        val starColor = a.getColor(R.styleable.StarRatingBar_star_color, Color.parseColor("#F1B906"))
        starDrawable = a.getDrawable(R.styleable.StarRatingBar_star_drawable)
        intrinsicHeight = starDrawable.getIntrinsicHeight()
        intrinsicWidth = starDrawable.getIntrinsicWidth()
        a.recycle()
        initPaints(starColor)
    }

    override fun onDraw(canvas: Canvas) {
        var left = 0
        for (index in 1..(Math.ceil(rating.toDouble()).toInt())) {
            drawStar(canvas, left)
            left = left + intrinsicWidth
            drawSpacing(canvas, left)
            left = left + starSpacing.toInt()
        }
    }

    private fun initPaints(starColor:Int) {
        starSpacingPaint = Paint()
        starSpacingPaint.isAntiAlias = true
        starSpacingPaint.setColor(Color.TRANSPARENT)

        starDrawable.setColorFilter(PorterDuffColorFilter(starColor, PorterDuff.Mode.SRC_IN))
    }


    private fun drawStar(canvas: Canvas, left: Int) {
        starDrawable.setBounds(left, 0, left + starDrawable.getIntrinsicWidth(), intrinsicHeight);
        starDrawable.draw(canvas)
    }

    private fun drawSpacing(canvas: Canvas, left: Int) {
        canvas.drawRect(left.toFloat(), 0f, left + starSpacing.toFloat(), intrinsicHeight.toFloat(), starSpacingPaint)
    }

    public fun setRating(starRating: Float) {
        rating = starRating
        requestLayout()
    }

    public fun getRating(): Float {
        return rating
    }

    public fun getStarSpacing(): Float {
        return starSpacing
    }

    public fun getStarDrawable(): Drawable {
        return starDrawable
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = starDrawable.getIntrinsicWidth()
        var canvasWidth = rating * width + (Math.ceil(rating.toDouble()).toInt() - 1) * starSpacing
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = Math.min(canvasWidth.toInt(), View.MeasureSpec.getSize(widthMeasureSpec))
        }
        var height = starDrawable.getIntrinsicHeight()
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }
}
