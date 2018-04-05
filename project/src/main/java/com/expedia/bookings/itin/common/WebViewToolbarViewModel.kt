package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

class WebViewToolbarViewModel : NewItinToolbarViewModel {
    override val toolbarTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val toolbarSubTitleSubject: PublishSubject<String> = PublishSubject.create()
    override val shareIconVisibleSubject: PublishSubject<Boolean> = PublishSubject.create()
    override val navigationBackPressedSubject: PublishSubject<Unit> = PublishSubject.create()
    override val shareIconClickedSubject: PublishSubject<Unit> = PublishSubject.create()

    init {
        shareIconVisibleSubject.onNext(true)
    }
}
