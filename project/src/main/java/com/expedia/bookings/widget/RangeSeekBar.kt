package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Parcelable
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import com.expedia.bookings.R
import java.math.BigDecimal

/**
 * MODIFIED RANGE SEEK BAR
 * Widget that lets users select a minimum and maximum value on a given numerical range. The range value types can be one of Long, Double, Integer, Float, Short, Byte or BigDecimal.
 *
 * Improved [MotionEvent] handling for smoother use, anti-aliased painting for improved aesthetics.

 * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
 * *
 * @author Peter Sinnott (psinnott@gmail.com)
 * *
 * @author Thomas Barrasso (tbarrasso@sevenplusandroid.org)
 * *
 * *
 * @param
 * *            The Number type of the range values. One of Long, Double, Integer, Float, Short, Byte or BigDecimal.
 */
public class RangeSeekBar<T : Number>
/**
 * Creates a new RangeSeekBar.

 * @param absoluteMinValue
 * *            The minimum value of the selectable range.
 * *
 * @param absoluteMaxValue
 * *            The maximum value of the selectable range.
 * *
 * @param context
 * *
 * @throws IllegalArgumentException
 * *             Will be thrown if min/max value type is not one of Long, Double, Integer, Float, Short, Byte or BigDecimal.
 */
@Throws(IllegalArgumentException::class)
constructor(public val absoluteMinValue: Int, public val absoluteMaxValue: Int, context: Context) : ImageView(context) {
    private var textPaint: Paint? = null
    private var canvasPaint: Paint? = null
    private val imageWidth = getContext().getResources().getDimension(R.dimen.car_time_slider_rectangle_width).toInt()
    private val imageHeight = getContext().getResources().getDimension(R.dimen.car_time_slider_rectangle_height).toInt()
    private val thumbnailRadius = getContext().getResources().getDimension(R.dimen.car_time_slider_thumbnail_radius).toInt()
    private val cornerRadius = getContext().getResources().getDimension(R.dimen.car_time_slider_corner_radius).toInt()
    private var rectF: RectF? = null
    private var imageCanvas: Canvas? = null
    private var canvasBitmap: Bitmap? = null
    private var thumbPadding: Int = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var minThumbImage: Bitmap? = null
    private var maxThumbImage: Bitmap? = null
    private var thumbPressedImage : Bitmap? = null
    private var thumbWidth = 0.0f
    private var thumbHalfWidth = 0.0f
    private var thumbHalfHeight = 0.0f
    private var lineHeight = 0.0f
    private var padding = 0.0f
    private val numberType: NumberType
    private val absoluteMinValuePrim: Double
    private val absoluteMaxValuePrim: Double
    private var normalizedMinValue = 0.0
    private var normalizedMaxValue = 1.0
    private var pressedThumb: Thumb? = null
    private var notifyWhileDragging = false
    private var listener: OnRangeSeekBarChangeListener<T>? = null

    private var mDownMotionX: Float = 0.toFloat()
    private var mActivePointerId = INVALID_POINTER_ID

    /**
     * On touch, this offset plus the scaled value from the position of the touch will form the progress value. Usually 0.
     */
    var mTouchProgressOffset: Float = 0.toFloat()

    private var mScaledTouchSlop: Int = 0
    private var mIsDragging: Boolean = false

    init {
        setupThumbnail()
        minThumbImage = getThumbnail(absoluteMinValue.toString(), false)
        setupThumbnail()
        maxThumbImage = getThumbnail(absoluteMaxValue.toString(), false)
        setupThumbnail()
        thumbPressedImage = getThumbnail("", true)

        thumbWidth = minThumbImage!!.getWidth().toFloat()
        thumbHalfWidth = 0.5f * thumbWidth
        thumbHalfHeight = 0.5f * minThumbImage!!.getHeight()
        lineHeight = 0.3f * thumbHalfHeight
        padding = thumbHalfWidth

        absoluteMinValuePrim = absoluteMinValue.toDouble()
        absoluteMaxValuePrim = absoluteMaxValue.toDouble()
        numberType = NumberType.fromNumber(absoluteMinValue)

        // make RangeSeekBar focusable. This solves focus handling issues in case EditText widgets are being used along with the RangeSeekBar within ScollViews.
        setFocusable(true)
        setFocusableInTouchMode(true)
        init()
    }

    private fun init() {
        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop()
    }

    public fun isNotifyWhileDragging(): Boolean {
        return notifyWhileDragging
    }

    public fun setupThumbnail() {
        textPaint = Paint(Paint.LINEAR_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG)
        textPaint!!.setColor(getResources().getColor(R.color.hotels_primary_color))
        textPaint!!.setTextAlign(Paint.Align.CENTER)
        textPaint!!.setTextSize(getResources().getDimension(R.dimen.car_time_slider_text_size))
        textPaint!!.setAntiAlias(true)
        textPaint!!.setStyle(Paint.Style.FILL)
        textPaint!!.setFakeBoldText(false)
        textPaint!!.setStrokeWidth(getResources().getDimension(R.dimen.car_time_slider_text_container))

        canvasPaint = Paint()
        canvasPaint!!.setAntiAlias(true)
        canvasPaint!!.setColor(Color.WHITE)
        canvasPaint!!.setStyle(Paint.Style.FILL)

        val rect = Rect(0, 0, imageWidth, imageHeight)
        rectF = RectF(rect)

        canvasBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)

        imageCanvas = Canvas(canvasBitmap)
    }

    public fun getThumbnail(text: String, isTouching: Boolean): Bitmap? {
        imageCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        if (isTouching) {
            imageCanvas!!.drawCircle((imageWidth / 2).toFloat(), (imageHeight / 2).toFloat(), (thumbnailRadius / 2).toFloat(), canvasPaint)
        } else {
            imageCanvas!!.drawRoundRect(rectF, cornerRadius.toFloat(), cornerRadius.toFloat(), canvasPaint)

            val xPos = (imageWidth / 2)
            val yPos = (imageHeight / 2 - ((textPaint!!.descent() + textPaint!!.ascent()) / 2)).toInt()

            imageCanvas!!.drawText(text, xPos.toFloat(), yPos.toFloat(), textPaint)
        }

        return canvasBitmap
    }


    /**
     * Should the widget notify the listener callback while the user is still dragging a thumb? Default is false.

     * @param flag
     */
    public fun setNotifyWhileDragging(flag: Boolean) {
        this.notifyWhileDragging = flag
    }

    /**
     * Returns the currently selected min value.

     * @return The currently selected min value.
     */
    public fun getSelectedMinValue(): T {
        return normalizedToValue(normalizedMinValue)
    }

    /**
     * Sets the currently selected minimum value. The widget will be invalidated and redrawn.

     * @param value
     * *            The Number value to set the minimum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public fun setSelectedMinValue(value: T) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0.0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMinValue(0.0)
        } else {
            setNormalizedMinValue(valueToNormalized(value))
        }
    }

    /**
     * Returns the currently selected max value.

     * @return The currently selected max value.
     */
    public fun getSelectedMaxValue(): T {
        return normalizedToValue(normalizedMaxValue)
    }

    /**
     * Sets the currently selected maximum value. The widget will be invalidated and redrawn.

     * @param value
     * *            The Number value to set the maximum value to. Will be clamped to given absolute minimum/maximum range.
     */
    public fun setSelectedMaxValue(value: T) {
        // in case absoluteMinValue == absoluteMaxValue, avoid division by zero when normalizing.
        if (0.0 == (absoluteMaxValuePrim - absoluteMinValuePrim)) {
            setNormalizedMaxValue(1.0)
        } else {
            setNormalizedMaxValue(valueToNormalized(value))
        }
    }

    /**
     * Registers given listener callback to notify about changed selected values.

     * @param listener
     * *            The listener to notify about changed selected values.
     */
    public fun setOnRangeSeekBarChangeListener(listener: OnRangeSeekBarChangeListener<T>) {
        this.listener = listener
    }

    /**
     * Handles thumb selection and movement. Notifies listener callback on certain events.
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (!isEnabled())
            return false

        val pointerIndex: Int

        val action = event.getAction()
        when (action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_DOWN -> {
                // Remember where the motion event started
                mActivePointerId = event.getPointerId(event.getPointerCount() - 1)
                pointerIndex = event.findPointerIndex(mActivePointerId)
                mDownMotionX = event.getX(pointerIndex)

                pressedThumb = evalPressedThumb(mDownMotionX)

                // Only handle thumb presses.
                if (pressedThumb == null)
                    return super.onTouchEvent(event)

                setPressed(true)
                invalidate()
                onStartTrackingTouch()
                trackTouchEvent(event)
                attemptClaimDrag()
            }
            MotionEvent.ACTION_MOVE -> if (pressedThumb != null) {

                if (mIsDragging) {
                    trackTouchEvent(event)
                } else {
                    // Scroll to follow the motion event
                    pointerIndex = event.findPointerIndex(mActivePointerId)
                    val x = event.getX(pointerIndex)

                    if (Math.abs(x - mDownMotionX) > mScaledTouchSlop) {
                        setPressed(true)
                        invalidate()
                        onStartTrackingTouch()
                        trackTouchEvent(event)
                        attemptClaimDrag()
                    }
                }

                if (notifyWhileDragging && listener != null) {
                    listener!!.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue())
                }
            }
            MotionEvent.ACTION_UP -> {
                if (mIsDragging) {
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                    setPressed(false)
                } else {
                    // Touch up when we never crossed the touch slop threshold
                    // should be interpreted as a tap-seek to that location.
                    onStartTrackingTouch()
                    trackTouchEvent(event)
                    onStopTrackingTouch()
                }

                pressedThumb = null
                invalidate()
                if (listener != null) {
                    listener!!.onRangeSeekBarValuesChanged(this, getSelectedMinValue(), getSelectedMaxValue())
                }
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                val index = event.getPointerCount() - 1
                // final int index = ev.getActionIndex();
                mDownMotionX = event.getX(index)
                mActivePointerId = event.getPointerId(index)
                invalidate()
            }
            MotionEvent.ACTION_POINTER_UP -> {
                onSecondaryPointerUp(event)
                invalidate()
            }
            MotionEvent.ACTION_CANCEL -> {
                if (mIsDragging) {
                    onStopTrackingTouch()
                    setPressed(false)
                }
                invalidate() // see above explanation
            }
        }
        return true
    }

    private fun onSecondaryPointerUp(ev: MotionEvent) {
        val pointerIndex = (ev.getAction() and ACTION_POINTER_INDEX_MASK) shr ACTION_POINTER_INDEX_SHIFT

        val pointerId = ev.getPointerId(pointerIndex)
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose
            // a new active pointer and adjust accordingly.
            val newPointerIndex = if (pointerIndex == 0) 1 else 0
            mDownMotionX = ev.getX(newPointerIndex)
            mActivePointerId = ev.getPointerId(newPointerIndex)
        }
    }

    private fun trackTouchEvent(event: MotionEvent) {
        val pointerIndex = event.findPointerIndex(mActivePointerId)
        val x = event.getX(pointerIndex)

        if (Thumb.MIN == pressedThumb) {
            setNormalizedMinValue(screenToNormalized(x))
        } else if (Thumb.MAX == pressedThumb) {
            setNormalizedMaxValue(screenToNormalized(x))
        }
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private fun attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true)
        }
    }

    /**
     * This is called when the user has started touching this widget.
     */
    fun onStartTrackingTouch() {
        mIsDragging = true
    }

    /**
     * This is called when the user either releases his touch or the touch is canceled.
     */
    fun onStopTrackingTouch() {
        mIsDragging = false
    }

    /**
     * Ensures correct size of the widget.
     */
    @Synchronized
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = 200
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(widthMeasureSpec)) {
            width = View.MeasureSpec.getSize(widthMeasureSpec)
        }
        var height = minThumbImage!!.getHeight()
        if (View.MeasureSpec.UNSPECIFIED != View.MeasureSpec.getMode(heightMeasureSpec)) {
            height = Math.min(height, View.MeasureSpec.getSize(heightMeasureSpec))
        }
        setMeasuredDimension(width, height)
    }

    /**
     * Draws the widget on the given canvas.
     */
    @Synchronized
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // draw seek bar background line
        val rect = RectF(padding, 0.5f * (getHeight() - lineHeight), getWidth() - padding, 0.5f * (getHeight() + lineHeight))
        paint.setStyle(Style.FILL)
        paint.setColor(Color.WHITE)
        paint.setAntiAlias(true)
        canvas.drawRect(rect, paint)

        // draw seek bar active range line
        rect.left = normalizedToScreen(normalizedMinValue)
        rect.right = normalizedToScreen(normalizedMaxValue)

        paint.setStyle(Style.FILL)
        paint.setColor(getResources().getColor(R.color.hotels_primary_color))
        paint.setAntiAlias(true)
        canvas.drawRect(rect, paint)

        // draw minimum thumb
        drawThumb(normalizedToScreen(normalizedMinValue), Thumb.MIN == pressedThumb, canvas, true)

        // draw maximum thumb
        drawThumb(normalizedToScreen(normalizedMaxValue), Thumb.MAX == pressedThumb, canvas, false)
    }

    /**
     * Overridden to save instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the [.setId] method. Other members of this class than the normalized min and max values don't need to be saved.
     */
    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
        bundle.putParcelable("SUPER", super.onSaveInstanceState())
        bundle.putDouble("MIN", normalizedMinValue)
        bundle.putDouble("MAX", normalizedMaxValue)
        return bundle
    }

    /**
     * Overridden to restore instance state when device orientation changes. This method is called automatically if you assign an id to the RangeSeekBar widget using the [.setId] method.
     */
    override fun onRestoreInstanceState(parcel: Parcelable) {
        val bundle = parcel as Bundle
        super.onRestoreInstanceState(bundle.getParcelable<Parcelable>("SUPER"))
        normalizedMinValue = bundle.getDouble("MIN")
        normalizedMaxValue = bundle.getDouble("MAX")
    }

    /**
     * Draws the "normal" resp. "pressed" thumb image on specified x-coordinate.

     * @param screenCoord
     * *            The x-coordinate in screen space where to draw the image.
     * *
     * @param pressed
     * *            Is the thumb currently in "pressed" state?
     * *
     * @param canvas
     * *            The canvas to draw upon.
     */
    private fun drawThumb(screenCoord: Float, pressed: Boolean, canvas: Canvas, isMin : Boolean) {
        canvas.drawBitmap(if (pressed) thumbPressedImage else if (isMin) minThumbImage else maxThumbImage, screenCoord - thumbHalfWidth, ((0.5f * getHeight()) - thumbHalfHeight).toFloat(), paint)
    }

    /**
     * Decides which (if any) thumb is touched by the given x-coordinate.

     * @param touchX
     * *            The x-coordinate of a touch event in screen space.
     * *
     * @return The pressed thumb or null if none has been touched.
     */
    private fun evalPressedThumb(touchX: Float): Thumb {
        var result: Thumb? = null
        val minThumbPressed = isInThumbRange(touchX, normalizedMinValue)
        val maxThumbPressed = isInThumbRange(touchX, normalizedMaxValue)
        if (minThumbPressed && maxThumbPressed) {
            // if both thumbs are pressed (they lie on top of each other), choose the one with more room to drag. this avoids "stalling" the thumbs in a corner, not being able to drag them apart anymore.
            result = if ((touchX / getWidth() > 0.5f)) Thumb.MIN else Thumb.MAX
        } else if (minThumbPressed) {
            result = Thumb.MIN
        } else if (maxThumbPressed) {
            result = Thumb.MAX
        }
        return result!!
    }

    /**
     * Decides if given x-coordinate in screen space needs to be interpreted as "within" the normalized thumb x-coordinate.

     * @param touchX
     * *            The x-coordinate in screen space to check.
     * *
     * @param normalizedThumbValue
     * *            The normalized x-coordinate of the thumb to check.
     * *
     * @return true if x-coordinate is in thumb range, false otherwise.
     */
    private fun isInThumbRange(touchX: Float, normalizedThumbValue: Double): Boolean {
        return Math.abs(touchX - normalizedToScreen(normalizedThumbValue)) <= thumbHalfWidth
    }

    /**
     * Sets normalized min value to value so that 0 <= value <= normalized max value <= 1. The View will get invalidated when calling this method.

     * @param value
     * *            The new normalized min value to set.
     */
    public fun setNormalizedMinValue(value: Double) {
        normalizedMinValue = Math.max(0.0, Math.min(1.0, Math.min(value, normalizedMaxValue)))
        invalidate()
    }

    /**
     * Sets normalized max value to value so that 0 <= normalized min value <= value <= 1. The View will get invalidated when calling this method.

     * @param value
     * *            The new normalized max value to set.
     */
    public fun setNormalizedMaxValue(value: Double) {
        normalizedMaxValue = Math.max(0.0, Math.min(1.0, Math.max(value, normalizedMinValue)))
        invalidate()
    }

    /**
     * Converts a normalized value to a Number object in the value space between absolute minimum and maximum.

     * @param normalized
     * *
     * @return
     */
    @SuppressWarnings("unchecked")
    private fun normalizedToValue(normalized: Double): T {
        return numberType.toNumber(absoluteMinValuePrim + normalized * (absoluteMaxValuePrim - absoluteMinValuePrim)) as T
    }

    /**
     * Converts the given Number value to a normalized double.

     * @param value
     * *            The Number value to normalize.
     * *
     * @return The normalized double.
     */
    private fun valueToNormalized(value: T): Double {
        if (0.0 == absoluteMaxValuePrim - absoluteMinValuePrim) {
            // prevent division by zero, simply return 0.
            return 0.0
        }
        return (value.toDouble() - absoluteMinValuePrim) / (absoluteMaxValuePrim - absoluteMinValuePrim)
    }

    /**
     * Converts a normalized value into screen space.

     * @param normalizedCoord
     * *            The normalized value to convert.
     * *
     * @return The converted value in screen space.
     */
    private fun normalizedToScreen(normalizedCoord: Double): Float {
        return (padding + normalizedCoord * (getWidth() - 2 * padding)).toFloat()
    }

    /**
     * Converts screen space x-coordinates into normalized values.

     * @param screenCoord
     * *            The x-coordinate in screen space to convert.
     * *
     * @return The normalized value.
     */
    private fun screenToNormalized(screenCoord: Float): Double {
        val width = getWidth()
        if (width <= 2 * padding) {
            // prevent division by zero, simply return 0.
            return 0.0
        } else {
            val result = ((screenCoord - padding) / (width - 2 * padding)).toDouble()
            return Math.min(1.0, Math.max(0.0, result))
        }
    }

    /**
     * Callback listener interface to notify about changed range values.

     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     * *
     * *
     * @param
     * *            The Number type the RangeSeekBar has been declared with.
     */
    public interface OnRangeSeekBarChangeListener<T> {
        public fun onRangeSeekBarValuesChanged(bar: RangeSeekBar<*>, minValue: T, maxValue: T)
    }

    /**
     * Thumb constants (min and max).
     */
    private enum class Thumb {
        MIN,
        MAX
    }

    /**
     * Utility enumaration used to convert between Numbers and doubles.

     * @author Stephan Tittel (stephan.tittel@kom.tu-darmstadt.de)
     */
    private enum class NumberType {
        LONG,
        DOUBLE,
        INTEGER,
        FLOAT,
        SHORT,
        BYTE,
        BIG_DECIMAL;

        public fun toNumber(value: Double): Number {
            when (this) {
                LONG -> return value.toLong()
                DOUBLE -> return value.toDouble()
                INTEGER -> return value.toInt()
                FLOAT -> return value.toFloat()
                SHORT -> return value.toShort()
                BYTE -> return value.toByte()
                BIG_DECIMAL -> return BigDecimal(value)
            }
        }

        companion object {

            @Throws(IllegalArgumentException::class)
            public fun <E : Number> fromNumber(value: E): NumberType {
                if (value is Long) {
                    return LONG
                }
                if (value is Double) {
                    return DOUBLE
                }
                if (value is Int) {
                    return INTEGER
                }
                if (value is Float) {
                    return FLOAT
                }
                if (value is Short) {
                    return SHORT
                }
                if (value is Byte) {
                    return BYTE
                }
                if (value is BigDecimal) {
                    return BIG_DECIMAL
                }
                throw IllegalArgumentException("Number class '" + value.javaClass.getName() + "' is not supported")
            }
        }
    }

    companion object {
        /**
         * An invalid pointer id.
         */
        public val INVALID_POINTER_ID: Int = 255

        // Localized constants from MotionEvent for compatibility
        // with API < 8 "Froyo".
        public val ACTION_POINTER_INDEX_MASK: Int = 65280
        public val ACTION_POINTER_INDEX_SHIFT: Int = 8
    }
}
