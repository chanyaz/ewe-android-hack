package com.expedia.bookings.widget.rail

import android.content.Context
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import rx.subjects.PublishSubject

/* TabLayout that emits the percentage between tabs 0 and 1, & 1 and 2 */
class PositionObservableTabLayout : TabLayout {
    val singleToReturnScrollObservable = PublishSubject.create<Float>()

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun setScrollPosition(position: Int, positionOffset: Float, updateSelectedText: Boolean) {
        super.setScrollPosition(position, positionOffset, updateSelectedText)
        if (position == 0 ) {
            singleToReturnScrollObservable.onNext(positionOffset)
        }
    }
}