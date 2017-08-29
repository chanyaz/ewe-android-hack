package com.expedia.bookings.rail.widget

import android.content.Context
import android.support.design.widget.TabLayout
import android.util.AttributeSet
import rx.subjects.PublishSubject

/* TabLayout that emits the percentage between tabs 0 and 1, & 1 and 2 */
class PositionObservableTabLayout(context: Context?, attrs: AttributeSet?) : TabLayout(context, attrs) {
    val singleToReturnScrollObservable = PublishSubject.create<Float>()

    override fun setScrollPosition(position: Int, positionOffset: Float, updateSelectedText: Boolean) {
        super.setScrollPosition(position, positionOffset, updateSelectedText)
        if (position == 0 ) {
            singleToReturnScrollObservable.onNext(positionOffset)
        }
    }
}