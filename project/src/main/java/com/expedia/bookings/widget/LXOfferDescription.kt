package com.expedia.bookings.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.Ui
import com.squareup.otto.Subscribe

class LXOfferDescription(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), View.OnClickListener {

    val offerDescription: TextView by lazy {
        findViewById<TextView>(R.id.description)
    }

    val readMore : ImageButton by lazy {
        findViewById<ImageButton>(R.id.read_more)
    }

    val maxLineCount : Int = 2

    val ANIMATION_DURATION : Long = 100

    init {
        View.inflate(context, R.layout.widget_lx_offer_description, this)
    }

    override fun onFinishInflate() {
        setOnClickListener(this)
    }

    fun bindData(description: String) {
        offerDescription.text = description
    }

    @Subscribe fun onOfferExpanded(@Suppress("UNUSED_PARAMETER") event : Events.LXOfferExpanded) {
        readMore.clearAnimation()
        isClickable = false

        offerDescription.maxLines = maxLineCount
        Ui.runOnNextLayout(offerDescription, {
            val textLayout = offerDescription.layout
            if (textLayout != null) {
                val lines = textLayout.lineCount
                if (lines > maxLineCount) {
                    readMore.visibility = View.VISIBLE
                    isClickable = true
                }
                else {
                    readMore.visibility = View.GONE
                    isClickable = false
                }
            }
        })

    }

    override fun onClick(v: View) {
        if (readMore.visibility == View.VISIBLE) {
            val totalLineCount = offerDescription.lineCount
            val displayedLineCount = offerDescription.maxLines
            if (displayedLineCount < totalLineCount) {
                AnimUtils.rotate(readMore)
                val animation = ObjectAnimator.ofInt(offerDescription, "maxLines", totalLineCount)
                animation.setDuration(ANIMATION_DURATION).start()
            } else {
                AnimUtils.reverseRotate(readMore)
                val animation = ObjectAnimator.ofInt(offerDescription, "maxLines", maxLineCount)
                animation.setDuration(ANIMATION_DURATION).start()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Events.register(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Events.unregister(this)
    }
}
