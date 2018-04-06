package com.expedia.bookings.launch.widget

import android.app.Activity
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.launch.vm.BigImageLaunchViewModel
import com.expedia.bookings.test.robolectric.RobolectricRunner
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import kotlin.test.assertEquals

@RunWith(RobolectricRunner::class)
class BigImageLaunchViewHolderTest {

    lateinit var bigImageLaunchViewHolderUnderTest: MockBigImageLaunchViewHolder
    lateinit var vm: BigImageLaunchViewModel
    val activity = Robolectric.buildActivity(Activity::class.java).create().get()
    lateinit var view: View

    @Before
    fun before() {
        view = LayoutInflater.from(activity)
                .inflate(R.layout.big_image_launch_card, null)
    }

    @Test
    fun backgroundFallbackImageIsUsed_givenImageUrlIsNull() {
        vm = getMockBigImageLaunchViewModel()
        vm.backgroundUrl = null
        bigImageLaunchViewHolderUnderTest = MockBigImageLaunchViewHolder(view, vm, true)
        bigImageLaunchViewHolderUnderTest.loadCard()

        assertEquals(ContextCompat.getDrawable(activity, R.drawable.bg_itin_placeholder_cloud), bigImageLaunchViewHolderUnderTest.getMockBgImageView().drawable)
    }

    @Test
    fun bigImageLaunchCardContentIsSet_givenImageUrlIsNull() {
        vm = getMockBigImageLaunchViewModel()
        vm.backgroundUrl = null
        bigImageLaunchViewHolderUnderTest = MockBigImageLaunchViewHolder(view, vm, true)
        bigImageLaunchViewHolderUnderTest.loadCard()

        assertEquals("Member Pricing", bigImageLaunchViewHolderUnderTest.getMockTitleView().text)
        assertEquals("Discounts off select hotels", bigImageLaunchViewHolderUnderTest.getMockSubtitleView().text)
        assertEquals(ContextCompat.getDrawable(activity, R.drawable.ic_member_deals_icon), bigImageLaunchViewHolderUnderTest.getMockIconImageView().drawable)
    }

    @Test
    fun imageUrlIsUpdated_givenPOSChange() {
        vm = getMockBigImageLaunchViewModel()
        MockBigImageLaunchViewHolder(view, vm, true)

        vm.backgroundUrlChangeSubject.onNext("some url")
        assertEquals("some url", vm.backgroundUrl)
    }

    private fun getMockBigImageLaunchViewModel(icon: Int = R.drawable.ic_member_deals_icon,
                                               bgGradient: Int = R.color.member_deals_background_gradient,
                                               titleId: Int = R.string.member_deal_title,
                                               subtitleId: Int = R.string.member_deal_subtitle,
                                               backgroundImageFailureFallback: Int = R.drawable.bg_itin_placeholder_cloud): BigImageLaunchViewModel {
        return BigImageLaunchViewModel(icon, bgGradient, titleId, subtitleId, backgroundImageFailureFallback)
    }

    open class MockBigImageLaunchViewHolder(view: View, vm: BigImageLaunchViewModel, shouldStartAtBeginningOfLoadingAnimation: Boolean) : BigImageLaunchViewHolder(view, vm, shouldStartAtBeginningOfLoadingAnimation) {

        fun getMockTitleView(): TextView {
            return titleView
        }

        fun getMockBgImageView(): ImageView {
            return bgImageView
        }

        fun getMockIconImageView(): ImageView {
            return iconImageView
        }

        fun getMockSubtitleView(): TextView {
            return subTitleView
        }

        override fun loadBackgroundImageIntoView() {
        }
    }
}
