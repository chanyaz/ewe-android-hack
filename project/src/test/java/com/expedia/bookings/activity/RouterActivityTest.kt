package com.expedia.bookings.activity

import android.support.constraint.ConstraintLayout
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.animation.ActivityTransitionCircularRevealHelper
import com.expedia.bookings.appstartup.persistence.MockSharedPreferencesSplashScreenAnimationProvider
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLog
import org.robolectric.shadows.ShadowLooper
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RouterActivityTest {

    private lateinit var mockRouterActivity: MockRouterActivity
    private lateinit var activityController: ActivityController<MockRouterActivity>

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        activityController = Robolectric.buildActivity(MockRouterActivity::class.java)
        mockRouterActivity = activityController.get()
        mockRouterActivity.setTheme(R.style.SplashTheme)
    }

    @Test
    fun splashAnimationShouldShowWhenEnabled() {
        mockRouterActivity.splashLoadingAnimationShouldRun = true
        activityController.setup()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val nextIntentStarted = ShadowApplication.getInstance().nextStartedActivity

        assertTrue {
            nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_X) &&
                    nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_Y) &&
                    nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR)
        }
    }

    @Test
    fun splashAnimationShouldNotShowWhenNotEnabled() {
        mockRouterActivity.splashLoadingAnimationShouldRun = false
        activityController.setup()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val nextIntentStarted = ShadowApplication.getInstance().nextStartedActivity
        assertFalse { nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_X) }
        assertFalse { nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_Y) }
        assertFalse { nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR) }
    }

    private class MockRouterActivity : RouterActivity() {
        override fun setIfSplashLoadingAnimationShouldRun() {
            splashScreenAnimationProvider = MockSharedPreferencesSplashScreenAnimationProvider()
        }

        override fun setupActivityForSplashLoadingAnimationAndPlayAnimation() {
            rootLayout = Mockito.mock(ConstraintLayout::class.java)
        }

        override fun notifySplashLoadingAnimationsThatDataHasLoaded() {
            if (splashLoadingAnimationShouldRun) {
                val mockView = Mockito.mock(View::class.java)
                launchNextActivityWithSplashLoadingAnimation(mockView)
            } else {
                launchNextActivityWithStaticScreen()
            }
        }

        override fun facebookInstallTracking() {
            // Let's not install the facebook tracking. It causes problems when running on Robolectric
        }
    }
}
