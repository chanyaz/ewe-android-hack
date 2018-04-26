package com.expedia.bookings.itin.common

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

abstract class AbstractToolbar(context: Context, attr: AttributeSet?) : Toolbar(context, attr) {

    val toolbarTitleText: TextView by bindView(R.id.itin_toolbar_title)
    val toolbarSubTitleText: TextView by bindView(R.id.itin_toolbar_subtitle)
    val toolbarShareIcon: TextView by bindView(R.id.itin_share_button)

    var viewModel: NewItinToolbarViewModel by notNullAndObservable {
        viewModel.toolbarTitleSubject.subscribe {
            toolbarTitleText.text = it
        }
        viewModel.toolbarSubTitleSubject.subscribe {
            toolbarSubTitleText.text = it
            toolbarSubTitleText.visibility = View.VISIBLE
        }
        viewModel.shareIconVisibleSubject.subscribeVisibility(toolbarShareIcon)
    }

    init {
        View.inflate(context, R.layout.widget_itin_toolbar, this)
        setNavigation()
        this.setNavigationOnClickListener {
            viewModel.navigationBackPressedSubject.onNext(Unit)
        }
        toolbarShareIcon.setTintedDrawable(context.getDrawable(R.drawable.ic_itin_share), ContextCompat.getColor(context, R.color.itin_toolbar_text))
        toolbarShareIcon.setOnClickListener {
            viewModel.shareIconClickedSubject.onNext(Unit)
        }
    }

    abstract fun setNavigation()
}
