package com.expedia.bookings.itin.helpers

import com.expedia.bookings.itin.common.NewItinToolbarViewModel
import io.reactivex.subjects.PublishSubject

class MockItinToolbarViewModel : NewItinToolbarViewModel {

    override val toolbarTitleSubject = PublishSubject.create<String>()
    override val toolbarSubTitleSubject = PublishSubject.create<String>()
    override val shareIconVisibleSubject = PublishSubject.create<Boolean>()
    override val navigationBackPressedSubject = PublishSubject.create<Unit>()
    override val shareIconClickedSubject = PublishSubject.create<Unit>()
}
