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
import kotlin.properties.Delegates

public class LXOfferDescription(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs), View.OnClickListener {

    val offerDescription: TextView by lazy {
        findViewById(R.id.description) as TextView
    }

    val readMore : ImageButton by lazy {
        findViewById(R.id.read_more) as ImageButton
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
        offerDescription.setText(description)
    }

    @Subscribe fun onOfferExpanded(@Suppress("UNUSED_PARAMETER") event : Events.LXOfferExpanded) {
        readMore.clearAnimation()
        setClickable(false)

        offerDescription.setMaxLines(maxLineCount)
        Ui.runOnNextLayout(offerDescription, {
            val textLayout = offerDescription.getLayout()
            if (textLayout != null) {
                val lines = textLayout.getLineCount()
                if (lines > maxLineCount) {
                    readMore.setVisibility(View.VISIBLE)
                    setClickable(true)
                }
                else {
                    readMore.setVisibility(View.GONE)
                    setClickable(false)
                }
            }
        })

    }

    override fun onClick(v: View) {
        if (readMore.getVisibility() == View.VISIBLE) {
            val totalLineCount = offerDescription.getLineCount()
            val displayedLineCount = offerDescription.getMaxLines()
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
        super<LinearLayout>.onAttachedToWindow()
        Events.register(this)
    }

    override fun onDetachedFromWindow() {
        super<LinearLayout>.onDetachedFromWindow()
        Events.unregister(this)
    }
}
