package com.expedia.bookings.itin.vm

import io.reactivex.subjects.PublishSubject

abstract class ItinToolbarViewModel {

    data class ToolbarParams(
            val title: String,
            val subTitle: String,
            val showShareIcon: Boolean
    )

    val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create<String>()
    val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create<String>()
    val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create<Boolean>()
    val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()

    abstract fun updateWidget(toolbarParams: ToolbarParams)
}