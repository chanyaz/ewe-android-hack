package com.expedia.bookings.onboarding.adapter

import android.content.Context
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.enums.OnboardingPagerState
import com.expedia.bookings.utils.Akeakamai
import com.mobiata.android.util.AndroidUtils
import com.squareup.picasso.Picasso

class OnboardingPagerAdapter(val context: Context) : PagerAdapter() {

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return `object` == view
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any {
        val pagerEnum = OnboardingPagerState.values()[position]
        val background = LayoutInflater.from(context).inflate(R.layout.page_onboarding, container, false) as ImageView
        Picasso.with(context).load(getResizeImageUrl(pagerEnum.backgroundUrl)).placeholder(pagerEnum.placeholderResId).into(background)
        background.setColorFilter(ContextCompat.getColor(context, R.color.onboarding_page_background_gradient), PorterDuff.Mode.SRC_ATOP)
        container?.addView(background)
        return background
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return OnboardingPagerState.values().size
    }

    private fun getResizeImageUrl(url: String): String {
        val akeakamai = Akeakamai(url)
        akeakamai.resizeExactly(AndroidUtils.getDisplaySize(context).x, AndroidUtils.getDisplaySize(context).y)
        return akeakamai.build()
    }
}
