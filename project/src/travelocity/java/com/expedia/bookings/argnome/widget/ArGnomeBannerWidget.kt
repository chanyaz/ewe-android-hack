package com.expedia.bookings.argnome.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.RelativeLayout
import com.expedia.bookings.R
import com.expedia.bookings.vm.ArGnomeBannerViewModel
import com.expedia.bookings.widget.interfaces.ArGnomeWidgetBase
import kotlinx.android.synthetic.travelocity.ar_gnome_banner_widget.view.*

class ArGnomeBannerWidget(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs), ArGnomeWidgetBase {

    companion object {
        const val INITIAL_BANNER_DELAY_MILLIS: Long = 3000
    }

    private var gnomeHandler = Handler()
    private var bannerRanFirstTime = false
    var viewModel: ArGnomeBannerViewModel

    init {
        View.inflate(context, R.layout.ar_gnome_banner_widget, this)
        viewModel = ArGnomeBannerViewModel(context)

        bottom_sheet_view.setOnClickListener {
            //TODO Interstitial hookup
        }
    }

    override fun onParentScrolledToTop() {
        if (viewModel.shouldShowGnomeAnimations && !gnomeSlideIn.isRunning && !bannerSlideIn.isRunning) {
            gnome_image_view.visibility = View.VISIBLE
            runFirstTimeGnomeBannerAnimation(0)
        }
    }

    override fun onParentScrolledAwayFromTop() {
        if (viewModel.shouldShowGnomeAnimations && !bannerSlideOut.isRunning && !gnomeSlideOut.isRunning) {
            cancelGnomeIntroAnimation()
            startGnomeBannerFadeOutAnimation()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (viewModel.shouldShowGnomeAnimations) {
            runFirstTimeGnomeBannerAnimation(INITIAL_BANNER_DELAY_MILLIS)
        }
    }

    private fun runFirstTimeGnomeBannerAnimation(delay: Long) {
        gnome_image_view.visibility = View.GONE

        gnomeHandler.postDelayed({
            bottom_sheet_view.visibility = View.VISIBLE
            getBottomSheetAndGnomeAnimatorSet().start()
            getFadeInAnimatorSet().start()
            bannerRanFirstTime = true
        }, delay)
    }

    private val bannerSlideIn: ObjectAnimator by lazy {
        val bannerSlideIn = ObjectAnimator.ofFloat(bottom_sheet_view, "translationY", resources.getDimension(R.dimen.gnome_banner_height), 0f)
        bannerSlideIn.interpolator = AccelerateInterpolator()
        bannerSlideIn.duration = 300
        bannerSlideIn
    }

    private val bannerSlideOut: ObjectAnimator by lazy {
        val bannerSlideOut = ObjectAnimator.ofFloat(bottom_sheet_view, "translationY", 0f, resources.getDimension(R.dimen.gnome_banner_height))
        bannerSlideOut.interpolator = AccelerateInterpolator()
        bannerSlideOut.duration = 300

        bannerSlideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                bottom_sheet_view.visibility = View.GONE
                top_title_text_view.visibility = View.GONE
                secondary_title_text_view.visibility = View.GONE
                button_frame_layout.visibility = View.GONE
            }
        })
        bannerSlideOut
    }

    private val gnomeSlideIn: ObjectAnimator by lazy {
        val gnomeSlideIn = ObjectAnimator.ofFloat(gnome_image_view, "translationY", resources.getDimension(R.dimen.gnome_height), 0f)
        gnomeSlideIn.interpolator = OvershootInterpolator()
        gnomeSlideIn.duration = 400
        gnomeSlideIn.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(p0: Animator?) {
                gnome_image_view.visibility = View.VISIBLE
            }
        })
        gnomeSlideIn
    }

    private val gnomeSlideOut: ObjectAnimator by lazy {
        val gnomeSlideOut = ObjectAnimator.ofFloat(gnome_image_view, "translationY", 0f, resources.getDimension(R.dimen.gnome_height))
        gnomeSlideOut.interpolator = AccelerateInterpolator()
        gnomeSlideOut.duration = 300
        gnomeSlideOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                gnome_image_view.visibility = View.GONE
            }
        })
        gnomeSlideOut
    }

    private fun getBottomSheetAndGnomeAnimatorSet(): AnimatorSet {
        val bottomSheetAndGnomeAnimatorSet = AnimatorSet()
        bottomSheetAndGnomeAnimatorSet.playSequentially(bannerSlideIn, gnomeSlideIn)

        bottomSheetAndGnomeAnimatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator?) {
                top_title_text_view.visibility = View.VISIBLE
                secondary_title_text_view.visibility = View.VISIBLE
                button_frame_layout.visibility = View.VISIBLE
            }
        })

        return bottomSheetAndGnomeAnimatorSet
    }

    private fun getFadeInAnimatorSet(): AnimatorSet {
        val fadeInAnimatorSet = AnimatorSet()
        fadeInAnimatorSet.playTogether(
                ObjectAnimator.ofFloat(top_title_text_view, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(secondary_title_text_view, "alpha", 0f, 1f),
                ObjectAnimator.ofFloat(button_frame_layout, "alpha", 0f, 1f))

        fadeInAnimatorSet.duration = 400
        fadeInAnimatorSet.startDelay = 500

        return fadeInAnimatorSet
    }

    private fun getFadeOutAnimatorSet(): AnimatorSet {
        val fadeOutAnimatorSet = AnimatorSet()
        fadeOutAnimatorSet.playTogether(
                ObjectAnimator.ofFloat(top_title_text_view, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(secondary_title_text_view, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(button_frame_layout, "alpha", 1f, 0f))

        fadeOutAnimatorSet.duration = 200

        return fadeOutAnimatorSet
    }

    private fun startGnomeBannerFadeOutAnimation() {
        val gnomeBannerFadeOutAnimatorSet = AnimatorSet()
        gnomeBannerFadeOutAnimatorSet.playTogether(getFadeOutAnimatorSet(), gnomeSlideOut, bannerSlideOut)
        gnomeBannerFadeOutAnimatorSet.start()
    }

    private fun cancelGnomeIntroAnimation() {
        gnomeHandler.removeCallbacksAndMessages(null)

        if (!bannerRanFirstTime) {
            top_title_text_view.visibility = View.GONE
            secondary_title_text_view.visibility = View.GONE
            button_frame_layout.visibility = View.GONE
            bannerRanFirstTime = true
        }
    }
}
