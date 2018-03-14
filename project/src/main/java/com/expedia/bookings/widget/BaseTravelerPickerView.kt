package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.expedia.vm.BaseTravelerPickerViewModel

abstract class BaseTravelerPickerView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    abstract fun getViewModel(): BaseTravelerPickerViewModel
}
