package com.expedia.bookings.itin.common

import com.expedia.bookings.itin.utils.ItinShareTextGenerator
import io.reactivex.subjects.PublishSubject

abstract class ItinToolbarViewModel : NewItinToolbarViewModel {

    data class ToolbarParams(
            val title: String,
            val subTitle: String,
            val showShareIcon: Boolean
    )

    override val toolbarTitleSubject = PublishSubject.create<String>()
    override val toolbarSubTitleSubject = PublishSubject.create<String>()
    override val shareIconVisibleSubject = PublishSubject.create<Boolean>()
    override val navigationBackPressedSubject = PublishSubject.create<Unit>()
    override val shareIconClickedSubject = PublishSubject.create<Unit>()
    override val itinShareTextGeneratorSubject = PublishSubject.create<ItinShareTextGenerator>()

    abstract fun updateWidget(toolbarParams: ToolbarParams)
}
