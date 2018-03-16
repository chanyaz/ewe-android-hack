package com.expedia.bookings.preference

import android.app.Dialog
import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager
import com.expedia.bookings.fragment.ExpediaSupportFragmentTestUtil
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import kotlin.test.assertNull

@RunWith(RobolectricRunner::class)
class EBPreferencesFragmentTest {

    private val context = RuntimeEnvironment.application
    private lateinit var sut: EBPreferencesFragment

    @Before
    fun setUp() {
        sut = EBPreferencesFragmentThatDoesNotExitApp()
    }

    @Test
    fun satelliteCacheIsInvalidated_whenChangingApi() {
        saveFeatureIdsToSharedPreferences()
        ExpediaSupportFragmentTestUtil.startFragment(sut, R.style.Theme_Phone_Preferences)

        val serverSwitchDialog = sut.showChangeServerDialog("Production")
        val okButton = serverSwitchDialog.getButton(Dialog.BUTTON_POSITIVE)
        okButton.performClick()

        val sharedPreferences = context.getSharedPreferences(SatelliteFeatureConfigManager.PREFS_FILE_NAME, Context.MODE_PRIVATE)
        val stringSet = sharedPreferences.getStringSet(SatelliteFeatureConfigManager.PREFS_SUPPORTED_FEATURE_SET, null)
        assertNull(stringSet)
    }

    private fun saveFeatureIdsToSharedPreferences() {
        val editor = context.getSharedPreferences(SatelliteFeatureConfigManager.PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
        val featureIds = listOf("12345", "54321")
        editor.putStringSet(SatelliteFeatureConfigManager.PREFS_SUPPORTED_FEATURE_SET, featureIds.toSet())
        editor.apply()
    }

    class EBPreferencesFragmentThatDoesNotExitApp : EBPreferencesFragment() {

        override fun restartApp() {
        }
    }
}
