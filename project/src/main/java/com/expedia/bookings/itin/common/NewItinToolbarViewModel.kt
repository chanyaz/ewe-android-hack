package com.expedia.bookings.itin.common

import io.reactivex.subjects.PublishSubject

interface NewItinToolbarViewModel {
    val toolbarTitleSubject: PublishSubject<String>
    val toolbarSubTitleSubject: PublishSubject<String>
    val shareIconVisibleSubject: PublishSubject<Boolean>
    val navigationBackPressedSubject: PublishSubject<Unit>
    val shareIconClickedSubject: PublishSubject<Unit>
}
