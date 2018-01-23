package com.expedia.bookings.utils

import android.app.Activity
import android.content.Context
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.featureconfig.IProductFlavorFeatureConfiguration
import com.expedia.bookings.test.MultiBrand
import com.expedia.bookings.test.RunForBrands
import com.expedia.bookings.test.robolectric.RobolectricRunner
import com.expedia.bookings.test.robolectric.shadows.ShadowAccountManagerEB
import com.expedia.bookings.test.robolectric.shadows.ShadowGCM
import com.expedia.bookings.test.robolectric.shadows.ShadowUserManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.properties.Delegates
import kotlin.test.assertEquals

@Config(shadows = arrayOf(ShadowGCM::class, ShadowUserManager::class, ShadowAccountManagerEB::class))
@RunWith(RobolectricRunner::class)

class RewardsUtilTest {
    val context: Context = RuntimeEnvironment.application
    var util = RewardsUtil
    private var activity: Activity by Delegates.notNull()
    private lateinit var mockedConfig: IProductFlavorFeatureConfiguration

    @Before
    fun before() {
        activity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()
    }

    private fun givenFeatureConfig(isExpediaBrand: Boolean): IProductFlavorFeatureConfiguration {
        mockedConfig = Mockito.mock(IProductFlavorFeatureConfiguration::class.java)
        Mockito.`when`(mockedConfig.isRewardProgramPointsType).thenReturn(isExpediaBrand)
        return mockedConfig
    }

    @Test
    @RunForBrands(brands = arrayOf(MultiBrand.EXPEDIA))
    fun testBuildRewardText() {
        val userPoints = "100.55"
        val rewardText = util.buildRewardText(context, userPoints, givenFeatureConfig(false))
        assertEquals(rewardText, "$userPoints Expedia+ Points")
    }

    @Test
    fun emptyBuildRewardText() {
        val userPoints = ""
        val rewardText = util.buildRewardText(context, userPoints, givenFeatureConfig(false))
        assertEquals(rewardText, "")
    }

    @Test
    fun zeroBuildRewardText() {
        val userPoints = "0"
        val rewardText = util.buildRewardText(context, userPoints, givenFeatureConfig(false))
        assertEquals(rewardText, "")
    }
}
