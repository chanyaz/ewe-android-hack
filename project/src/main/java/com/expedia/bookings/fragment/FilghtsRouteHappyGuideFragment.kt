package com.expedia.bookings.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant

class FilghtsRouteHappyGuideFragment : DialogFragment() {

    private val abacusVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsRichContent)
    @VisibleForTesting
    internal lateinit var wifiLabel: TextView
    @VisibleForTesting
    internal lateinit var entertainmentLabel: TextView
    @VisibleForTesting
    internal lateinit var powerLabel: TextView
    @VisibleForTesting
    internal lateinit var ratingLabel: TextView
    @VisibleForTesting
    internal lateinit var titleLabel: TextView
    @VisibleForTesting
    internal lateinit var infoLabel: TextView
    @VisibleForTesting
    internal lateinit var moreInfoLabel: TextView
    @VisibleForTesting
    internal lateinit var amenitiesLottieView: LottieAnimationView
    @VisibleForTesting
    internal lateinit var ratingsLottieView: LottieAnimationView
    @VisibleForTesting
    internal lateinit var dotsLottieView: LottieAnimationView
    private lateinit var dismissButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val routeHappyDialog = inflater.inflate(R.layout.flights_route_happy_guide, container, false)
        dismissButton = routeHappyDialog.findViewById<Button>(R.id.dismiss_button)
        wifiLabel = routeHappyDialog.findViewById<TextView>(R.id.wifi_label)
        entertainmentLabel = routeHappyDialog.findViewById<TextView>(R.id.entertainment_label)
        powerLabel = routeHappyDialog.findViewById<TextView>(R.id.power_label)
        ratingLabel = routeHappyDialog.findViewById<TextView>(R.id.rating_label)
        titleLabel = routeHappyDialog.findViewById<TextView>(R.id.title)
        infoLabel = routeHappyDialog.findViewById<TextView>(R.id.info)
        moreInfoLabel = routeHappyDialog.findViewById<TextView>(R.id.more_info)
        amenitiesLottieView = routeHappyDialog.findViewById<LottieAnimationView>(R.id.rich_content_animation_amenities)
        ratingsLottieView = routeHappyDialog.findViewById<LottieAnimationView>(R.id.rich_content_animation_ratings)
        dotsLottieView = routeHappyDialog.findViewById<LottieAnimationView>(R.id.rich_content_animation_dots)
        return routeHappyDialog
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dismissButton.setOnClickListener {
            dismiss()
        }
        richGuideAnimation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setOnShowListener {
            // Force the window to match parent's width. This must be done after it's shown.
            dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        }
        return dialog
    }

    private fun richGuideAnimation() {
        val amenitiesFadeInAnimator = amenitiesFadeInAnimator()
        val amenitiesFadeOutAnimator = amenitiesFadeOutAnimator()
        val routeScoreFadeInAnimator = routeScoreFadeInAnimator()

        if (abacusVariant == AbacusVariant.ONE.value) {
            titleLabel.text = context.resources.getString(R.string.flight_route_happy_guide_title_v1)
            infoLabel.visibility = View.VISIBLE
            amenitiesLottieView.visibility = View.VISIBLE
            amenitiesFadeInAnimator.start()
        } else if (abacusVariant == AbacusVariant.TWO.value) {
            titleLabel.text = context.resources.getString(R.string.flight_route_happy_guide_title_v2)
            moreInfoLabel.visibility = View.VISIBLE
            ratingsLottieView.visibility = View.VISIBLE
            ratingsLottieView.playAnimation()
            routeScoreFadeInAnimator.start()
        } else {
            titleLabel.text = context.resources.getString(R.string.flight_route_happy_guide_title_v3)
            infoLabel.visibility = View.VISIBLE
            moreInfoLabel.visibility = View.VISIBLE
            amenitiesLottieView.visibility = View.VISIBLE
            dotsLottieView.visibility = View.VISIBLE
            amenitiesFadeInAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    amenitiesFadeOutAnimator.start()
                }
            })
            amenitiesFadeOutAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    amenitiesLottieView.visibility = View.INVISIBLE
                    ratingsLottieView.visibility = View.VISIBLE
                    ratingsLottieView.playAnimation()
                    routeScoreFadeInAnimator.start()
                }
            })
            amenitiesFadeInAnimator.start()
        }
    }

    private fun amenitiesFadeInAnimator(): ValueAnimator {
        val amenitiesFadeInAnimator = ValueAnimator.ofFloat(0f, 1f)
        amenitiesFadeInAnimator.duration = 300
        amenitiesFadeInAnimator.startDelay = 600
        amenitiesFadeInAnimator.cancel()
        amenitiesFadeInAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            animateLabels(animatedValue)
        }
        return amenitiesFadeInAnimator
    }

    private fun amenitiesFadeOutAnimator(): ValueAnimator {
        val amenitiesFadeOutAnimator = ValueAnimator.ofFloat(1f, 0f)
        amenitiesFadeOutAnimator.duration = 300
        amenitiesFadeOutAnimator.startDelay = 1800
        amenitiesFadeOutAnimator.cancel()
        amenitiesFadeOutAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            animateLabels(animatedValue)
            amenitiesLottieView.alpha = animatedValue
        }
        return amenitiesFadeOutAnimator
    }

    private fun routeScoreFadeInAnimator(): ValueAnimator {
        val routeScoreFadeInAnimator = ValueAnimator.ofFloat(0f, 1f)
        routeScoreFadeInAnimator.duration = 300
        routeScoreFadeInAnimator.startDelay = 100
        routeScoreFadeInAnimator.cancel()
        routeScoreFadeInAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            ratingLabel.alpha = animatedValue
        }
        return routeScoreFadeInAnimator
    }

    private fun animateLabels(animatedValue: Float) {
        wifiLabel.alpha = animatedValue
        entertainmentLabel.alpha = animatedValue
        powerLabel.alpha = animatedValue
    }
}
