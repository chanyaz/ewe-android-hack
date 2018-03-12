package com.expedia.bookings.activity

import android.support.constraint.ConstraintLayout
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.animation.ActivityTransitionCircularRevealHelper
import com.expedia.bookings.featureconfig.ProductFlavorFeatureConfiguration
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLog
import org.robolectric.shadows.ShadowLooper
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricRunner::class)
class RouterActivityTest {

    private lateinit var mockRouterActivity: RouterActivity

    @Before
    fun setUp() {
        ShadowLog.stream = System.out
        val activityController = Robolectric.buildActivity(MockRouterActivity::class.java)
        mockRouterActivity = activityController.get()
        mockRouterActivity.setTheme(R.style.SplashTheme)
        activityController.setup()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
    }

    @Test
    fun splashAnimationShouldShowWhenEnabledInFeatureConfig() {

        if (ProductFlavorFeatureConfiguration.getInstance().isSplashLoadingAnimationEnabled) {
            val nextIntentStarted = ShadowApplication.getInstance().nextStartedActivity
            assertTrue {
                nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_X) &&
                        nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_Y) &&
                        nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR)
            }
        } else {
            val nextIntentStarted = ShadowApplication.getInstance().nextStartedActivity
            assertFalse { nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_X) }
            assertFalse { nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_Y) }
            assertFalse { nextIntentStarted.hasExtra(ActivityTransitionCircularRevealHelper.ARG_CIRCULAR_REVEAL_BACKGROUND_COLOR) }
        }
    }

    private class MockRouterActivity : RouterActivity() {

        override fun setupActivityForAnimationsAndBeginAnimation() {
            rootLayout = Mockito.mock(ConstraintLayout::class.java)
        }

        override fun notifyAnimationsThatDataHasLoaded() {
            if (ProductFlavorFeatureConfiguration.getInstance().isSplashLoadingAnimationEnabled) {
                val mockView = Mockito.mock(View::class.java)
                launchNextActivityWithLoadingAnimationScreen(mockView)
            } else {
                launchNextActivityWithStaticScreen()
            }
        }

        override fun facebookInstallTracking() {
            // Let's not install the facebook tracking. It causes problems when running on Robolectric
        }
    }
}
