package com.expedia.bookings.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.annotation.VisibleForTesting
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.PathInterpolator
import com.expedia.bookings.R
import com.mobiata.android.Log
import io.reactivex.CompletableObserver
import io.reactivex.subjects.CompletableSubject

/**
 * Created by cplachta on 2/16/18.
 */
class ActivityTransitionCircularRevealHelper {
    companion object {
        @JvmField val ARG_CIRCULAR_REVEAL_X = "ARG_CIRCULAR_REVEAL_X"
        @JvmField val ARG_CIRCULAR_REVEAL_Y = "ARG_CIRCULAR_REVEAL_Y"
        @JvmField val ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR = "ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR"

        private val PATH_CONTROL_X1 = 0.60f
        private val PATH_CONTROL_Y1 = 0.0f
        private val PATH_CONTROL_X2 = 0.30f
        private val PATH_CONTROL_Y2 = 1.0f
        private val CIRCULAR_REVEAL_DURATION: Long = 600
        private lateinit var doneAnimatingCompletable: CompletableSubject

        fun getSceneTransitionAnimationAndSubscribe(activity: AppCompatActivity, sharedView: View, transitionName: String = "transition", completableObserver: CompletableObserver): ActivityOptionsCompat {
            doneAnimatingCompletable = CompletableSubject.create()
            doneAnimatingCompletable.subscribe(completableObserver)
            return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedView, transitionName)
        }

        fun getViewBackgroundColor(view: View): Int {
            val backgroundDrawable = view.background
            return if (backgroundDrawable is ColorDrawable) {
                backgroundDrawable.color
            } else {
                R.color.brand_secondary
            }
        }

        fun startCircularRevealTransitionAnimation(activity: AppCompatActivity, savedInstanceState: Bundle?, intent: Intent, rootLayout: View) {
            if (shouldStartCircularReveal(savedInstanceState, intent)) {
                val revealX = intent.getIntExtra(ARG_CIRCULAR_REVEAL_X, 0)
                val revealY = intent.getIntExtra(ARG_CIRCULAR_REVEAL_Y, 0)
                val backgroundColor = intent.getIntExtra(ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR, R.color.brand_secondary)

                val previousWindowBackgroundColor = getViewBackgroundColor(activity.window.decorView)
                val notifyOnAnimationEndAnimator = getNotifyOnAnimationEndAnimator(doneAnimatingCompletable, activity, previousWindowBackgroundColor)

                activity.postponeEnterTransition()
                setWindowBackgroundColor(activity, backgroundColor)
                rootLayout.visibility = View.INVISIBLE
                rootLayout.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        rootLayout.viewTreeObserver.removeOnPreDrawListener(this)
                        activity.startPostponedEnterTransition()
                        revealActivity(rootLayout, revealX, revealY, notifyOnAnimationEndAnimator)
                        return true
                    }
                })
            } else {
                rootLayout.visibility = View.VISIBLE
            }
        }

        private fun setWindowBackgroundColor(activity: AppCompatActivity, backgroundColor: Int) {
            activity.window.decorView.setBackgroundColor(backgroundColor)
        }

        private fun shouldStartCircularReveal(savedInstanceState: Bundle?, intent: Intent): Boolean {
            return savedInstanceState == null &&
                    intent.hasExtra(ARG_CIRCULAR_REVEAL_X) &&
                    intent.hasExtra(ARG_CIRCULAR_REVEAL_Y) &&
                    intent.hasExtra(ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR)
        }

        private fun revealActivity(rootLayout: View, x: Int, y: Int, animator: Animator.AnimatorListener, interpolator: TimeInterpolator = PathInterpolator(PATH_CONTROL_X1, PATH_CONTROL_Y1, PATH_CONTROL_X2, PATH_CONTROL_Y2)) {
            val cx = rootLayout.width / 2
            val cy = rootLayout.height / 2
            val finalRadius = Math.hypot(cx.toDouble(), cy.toDouble()).toFloat()

            // create the animator for this view (the start radius is zero)
            val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0.0f, finalRadius)
            circularReveal.duration = CIRCULAR_REVEAL_DURATION
            circularReveal.interpolator = interpolator
            circularReveal.addListener(animator)

            // make the view visible and start the animation
            rootLayout.visibility = View.VISIBLE
            circularReveal.start()
        }

        @VisibleForTesting
        fun getNotifyOnAnimationEndAnimator(completableSubject: CompletableSubject, activity: AppCompatActivity, previousWindowBackgroundColor: Int): Animator.AnimatorListener {
            return object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animator: Animator?) {
                    try {
                        setWindowBackgroundColor(activity, previousWindowBackgroundColor)
                        completableSubject.onComplete()
                    } catch (ex: UninitializedPropertyAccessException) {
                        Log.e("Calling this without initialization prevents the previous activity from finishing: " + ex.message)
                    }
                }
            }
        }
    }
}
