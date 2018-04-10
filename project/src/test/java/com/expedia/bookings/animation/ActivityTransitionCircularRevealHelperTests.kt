package com.expedia.bookings.animation

import android.content.Intent
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.RouterActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.observers.DisposableCompletableObserver
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class ActivityTransitionCircularRevealHelperTests {
    private lateinit var activityController: ActivityController<RouterActivity>

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        activityController = Robolectric.buildActivity(RouterActivity::class.java)
    }

    @Test
    fun testViewIsVisibleWithBadData() {
        val mockRootView = Mockito.mock(View::class.java)
        val mockIntent = Mockito.mock(Intent::class.java)
        mockRootView.visibility = View.GONE
        ActivityTransitionCircularRevealHelper.startCircularRevealTransitionAnimation(activityController.get(), null, mockIntent, mockRootView)
        Mockito.verify(mockRootView).visibility = View.VISIBLE
    }

    @Test
    fun testViewBackgroundColorIsSecondaryBrandColorAsDefault() {
        val mockView = Mockito.mock(View::class.java)
        assertEquals(R.color.brand_secondary, ActivityTransitionCircularRevealHelper.getViewBackgroundColor(mockView))
    }

    @Test
    fun testObserverCompleted() {
        val disposableCompletableObserver = getMockDisposableCompletableObserver()
        ActivityTransitionCircularRevealHelper.subscribeToAnimationEnd(disposableCompletableObserver)
        val animationListener = ActivityTransitionCircularRevealHelper.getNotifyOnAnimationEndAnimator(activityController.get(), 0)
        animationListener.onAnimationEnd(null)
        assertTrue { ActivityTransitionCircularRevealHelper.animationIsComplete() }
    }

    @Test
    fun testObserverIsCleared() {
        val disposableCompletableObserver = getMockDisposableCompletableObserver()
        ActivityTransitionCircularRevealHelper.subscribeToAnimationEnd(disposableCompletableObserver)
        ActivityTransitionCircularRevealHelper.clearObservers()
        assertTrue { disposableCompletableObserver.isDisposed }
    }

    private fun getMockDisposableCompletableObserver(): DisposableCompletableObserver {
        return object : DisposableCompletableObserver() {
            override fun onComplete() {
            }

            override fun onError(e: Throwable) {
            }
        }
    }
}
