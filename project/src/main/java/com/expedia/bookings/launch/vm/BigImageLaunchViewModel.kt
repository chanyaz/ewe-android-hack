package com.expedia.bookings.launch.vm

import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.expedia.bookings.R
import rx.subjects.BehaviorSubject

class BigImageLaunchViewModel(@DrawableRes val icon: Int, @ColorRes val bgGradient: Int, @StringRes val titleId: Int, @StringRes val subtitleId: Int) {

    var backgroundResId: Int? = null
    var backgroundUrl: String? = null
    var backgroundFallback: Int = R.color.gray6
    val backgroundUrlChangeSubject = BehaviorSubject.create<String>()

}
