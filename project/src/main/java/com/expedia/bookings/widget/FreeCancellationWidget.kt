package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeText
import com.expedia.vm.FreeCancellationViewModel

class FreeCancellationWidget(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    val freeCancellationText: TextView by bindView(R.id.free_cancellation_description)

    val toolbar: Toolbar by bindView(R.id.toolbar)

    val statusBarHeight by lazy { Ui.getStatusBarHeight(context) }

    init {
        View.inflate(context, R.layout.free_cancellation_view, this)
        this.orientation = VERTICAL
        toolbar.setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc)
        toolbar.setPadding(0, statusBarHeight, 0, 0)
    }

    val viewModel by lazy {
        val vm = FreeCancellationViewModel()
        vm.freeCancellationTextObservable.subscribeText(freeCancellationText)
        vm
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        toolbar.setNavigationOnClickListener {
            viewModel.closeFreeCancellationObservable.onNext(Unit)
        }
    }
}
