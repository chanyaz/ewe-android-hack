package com.expedia.bookings.launch.activity

import com.expedia.bookings.R
import com.expedia.bookings.onboarding.activity.OnboardingActivity
import com.expedia.bookings.test.robolectric.RobolectricRunner
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric

@RunWith(RobolectricRunner::class)
class OnboardingTest {

    private lateinit var onboardingActivity: OnboardingActivity

    @Before
    fun setup() {
        onboardingActivity = Robolectric.buildActivity(OnboardingActivity::class.java).create().get()
    }

    @Test
    fun arrowContentDescriptionsAreSet() {
        assert(onboardingActivity.button_next.contentDescription == this.onboardingActivity.getString(R.string.next))
        assert(onboardingActivity.button_previous.contentDescription == this.onboardingActivity.getString(R.string.previous))
    }
}
