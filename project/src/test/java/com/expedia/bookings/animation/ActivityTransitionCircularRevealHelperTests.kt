package com.expedia.bookings.animation

import android.content.Intent
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.activity.RouterActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import io.reactivex.CompletableObserver
import io.reactivex.subjects.CompletableSubject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLog
import kotlin.test.assertEquals

/**
 * Created by cplachta on 2/23/18.
 */
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
    fun testAnimatorCompleted() {
        val mockCompletableSubject = getMockCompletableSubject()
        val mockCompletable = Mockito.mock(CompletableObserver::class.java)
        mockCompletableSubject.subscribe(mockCompletable)

        val animationListener = ActivityTransitionCircularRevealHelper.getNotifyOnAnimationEndAnimator(mockCompletableSubject, activityController.get(), 0)
        animationListener.onAnimationEnd(null)
        Mockito.verify(mockCompletable).onComplete()
    }

    private fun getMockCompletableSubject(): CompletableSubject {
        return CompletableSubject.create()
    }
}
