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
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.tracking.flight.FlightsV2Tracking
import com.expedia.bookings.utils.FlightV2Utils

class FlightsRouteHappyGuideFragment : DialogFragment() {

    private val ABACUS_VARIANT = "abacusVariant"
    private val LINE_OF_BUSINESS = "lineOfBusiness"

    fun newInstance(abacusVariant: Int, lob: LineOfBusiness): FlightsRouteHappyGuideFragment {
        val dialogFragment = FlightsRouteHappyGuideFragment()
        val args = Bundle()
        args.putInt(ABACUS_VARIANT, abacusVariant)
        args.putString(LINE_OF_BUSINESS, lob.name)
        dialogFragment.arguments = args
        return dialogFragment
    }

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
    @VisibleForTesting
    internal lateinit var dismissButton: Button

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = FlightV2Utils.getRichContentSharedPref(requireContext())
        val counter = sharedPref.getInt("counter", 1) - 1
        val args = arguments
        val lob = args?.getString(LINE_OF_BUSINESS) ?: "FLIGHTS_V2"
        val abacusVariant = args?.getInt(ABACUS_VARIANT) ?: 3
        dismissButton.setOnClickListener {
            dismiss()
            trackGuideScreenClosed(counter, lob)
        }
        richGuideAnimation(abacusVariant)
        trackGuideScreenShown(counter, lob)
    }

    private fun trackGuideScreenClosed(counter: Int, lob: String) {
        if (lob == "PACKAGES") {
            // TODO: implement tracking for packages
        } else {
            FlightsV2Tracking.trackGuideScreenClosed(counter)
        }
    }

    private fun trackGuideScreenShown(counter: Int, lob: String) {
        if (lob == "PACKAGES") {
            // TODO: implement tracking for packages
        } else {
            FlightsV2Tracking.trackGuideScreenShown(counter)
        }
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

    private fun richGuideAnimation(abacusVariant: Int) {
        val fadeInAnimator = fadeInAnimator()
        val fadeOutAnimator = fadeOutAnimator()
        val amenitiesFadeInAnimator = amenitiesFadeInAnimator()

        if (abacusVariant == AbacusVariant.ONE.value) {
            titleLabel.text = resources.getString(R.string.flight_route_happy_guide_title_v1)
            infoLabel.visibility = View.VISIBLE
            amenitiesLottieView.visibility = View.VISIBLE
            amenitiesLottieView.playAnimation()
            amenitiesFadeInAnimator.start()
        } else if (abacusVariant == AbacusVariant.TWO.value) {
            titleLabel.text = resources.getString(R.string.flight_route_happy_guide_title_v2)
            moreInfoLabel.visibility = View.VISIBLE
            ratingsLottieView.visibility = View.VISIBLE
            ratingsLottieView.playAnimation()
            fadeInAnimator.start()
        } else {
            titleLabel.text = resources.getString(R.string.flight_route_happy_guide_title_v3)
            infoLabel.visibility = View.VISIBLE
            moreInfoLabel.visibility = View.VISIBLE
            ratingsLottieView.visibility = View.VISIBLE
            dotsLottieView.visibility = View.VISIBLE
            ratingsLottieView.playAnimation()
            dotsLottieView.playAnimation()
            fadeInAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    fadeOutAnimator.start()
                }
            })
            fadeOutAnimator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    ratingsLottieView.visibility = View.INVISIBLE
                    amenitiesLottieView.visibility = View.VISIBLE
                    amenitiesLottieView.playAnimation()
                    amenitiesFadeInAnimator.start()
                }
            })
            fadeInAnimator.start()
        }
    }

    private fun fadeInAnimator(): ValueAnimator {
        val fadeInAnimator = ValueAnimator.ofFloat(0f, 1f)
        fadeInAnimator.duration = 300
        fadeInAnimator.startDelay = 600
        fadeInAnimator.cancel()
        fadeInAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            ratingLabel.alpha = animatedValue
        }
        return fadeInAnimator
    }

    private fun fadeOutAnimator(): ValueAnimator {
        val fadeOutAnimator = ValueAnimator.ofFloat(1f, 0f)
        fadeOutAnimator.duration = 300
        fadeOutAnimator.startDelay = 1800
        fadeOutAnimator.cancel()
        fadeOutAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            ratingsLottieView.alpha = animatedValue
            ratingLabel.alpha = animatedValue
        }
        return fadeOutAnimator
    }

    private fun amenitiesFadeInAnimator(): ValueAnimator {
        val amenitiesFadeInAnimator = ValueAnimator.ofFloat(0f, 1f)
        amenitiesFadeInAnimator.duration = 300
        amenitiesFadeInAnimator.startDelay = 100
        amenitiesFadeInAnimator.cancel()
        amenitiesFadeInAnimator.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            animateLabels(animatedValue)
        }
        return amenitiesFadeInAnimator
    }

    private fun animateLabels(animatedValue: Float) {
        wifiLabel.alpha = animatedValue
        entertainmentLabel.alpha = animatedValue
        powerLabel.alpha = animatedValue
    }
}
