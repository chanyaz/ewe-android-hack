package com.expedia.bookings.screengrab

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab

@RunWith(JUnit4::class)
class LaunchScreenScreenshots : BaseScreenshots() {
    @Test
    fun takeLaunchScreenScreenshots() {
        Screengrab.screenshot("Launch_LaunchScreen")
    }
}
