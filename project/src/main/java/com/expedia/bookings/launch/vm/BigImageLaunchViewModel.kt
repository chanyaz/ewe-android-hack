package com.expedia.bookings.launch.vm

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.expedia.bookings.R
import io.reactivex.subjects.BehaviorSubject

class BigImageLaunchViewModel(@DrawableRes val iconId: Int,
                              @ColorRes val bgGradientId: Int,
                              @StringRes val titleId: Int,
                              @StringRes val subtitleId: Int,
                              @DrawableRes val backgroundImageFailureFallback: Int = R.drawable.bg_itin_placeholder_cloud) {
    var backgroundUrl: String? = null
    val backgroundUrlChangeSubject: BehaviorSubject<String> = BehaviorSubject.create<String>()
}
