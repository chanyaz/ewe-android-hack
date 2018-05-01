package com.expedia.bookings.launch.widget

import android.graphics.PorterDuff
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.launch.vm.BigImageLaunchViewModel
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.LoadingViewHolder
import com.expedia.bookings.widget.TextView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

open class BigImageLaunchViewHolder(private val view: View, private val vm: BigImageLaunchViewModel, private val shouldStartAtBeginningOfLoadingAnimation: Boolean) : LoadingViewHolder(view) {
    protected val titleView: TextView by bindView(R.id.big_image_title)
    protected val iconImageView: ImageView by bindView(R.id.big_image_icons)
    protected val bgImageView: ImageView by bindView(R.id.background_image_view)
    protected val subTitleView: TextView by bindView(R.id.big_image_subtitle)
    private var backgroundLoaded: Boolean = false

    private val backgroundImageLoadingCallback = object : Callback {
        override fun onSuccess() {
            updateCardState()
        }

        override fun onError() {
            setFallbackBackgroundImage()
            updateCardState()
        }

        fun updateCardState() {
            backgroundLoaded = true
            cancelAnimation()
            finishLoadingCard()
        }
    }

    init {
        subscribeToImageUrlUpdate()
    }

    fun loadCard() {
        if (vm.backgroundUrl != null) {
            loadBackgroundImageIntoView()
        } else {
            setFallbackBackgroundImage()
            finishLoadingCard()
        }
    }

    fun startLoadingAnimation() {
        if (!backgroundLoaded) {
            setAnimator(AnimUtils.setupLoadingAnimation(backgroundImageView, shouldStartAtBeginningOfLoadingAnimation))
        }
    }

    private fun subscribeToImageUrlUpdate() {
        vm.backgroundUrlChangeSubject.subscribe { url ->
            if (url != null) {
                vm.backgroundUrl = url
                loadCard()
            }
        }
    }

    private fun setFallbackBackgroundImage() {
        bgImageView.setImageResource(vm.backgroundImageFailureFallback)
    }

    @VisibleForTesting
    protected open fun loadBackgroundImageIntoView() {
        Picasso.with(view.context).load(vm.backgroundUrl).into(bgImageView, backgroundImageLoadingCallback)
    }

    private fun finishLoadingCard() {
        titleView.setText(vm.titleId)
        subTitleView.setText(vm.subtitleId)
        iconImageView.setImageDrawable(ContextCompat.getDrawable(view.context, vm.iconId))
        bgImageView.setColorFilter(ContextCompat.getColor(view.context, vm.bgGradientId), PorterDuff.Mode.SRC_ATOP)
    }
}
