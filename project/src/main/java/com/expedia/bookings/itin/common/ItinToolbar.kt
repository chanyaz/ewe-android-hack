package com.expedia.bookings.itin.common

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.itin.scopes.ToolBarViewModelSetter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinToolbar(context: Context, attr: AttributeSet?) : Toolbar(context, attr), ToolBarViewModelSetter {
    override fun setUpViewModel(vm: NewItinToolbarViewModel) {
        viewModel = vm
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val toolbarTitleText: TextView by bindView(R.id.itin_toolbar_title)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val toolbarSubTitleText: TextView by bindView(R.id.itin_toolbar_subtitle)
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val toolbarShareIcon: TextView by bindView(R.id.itin_share_button)

    var viewModel: NewItinToolbarViewModel by notNullAndObservable {
        viewModel.toolbarTitleSubject.subscribe {
            toolbarTitleText.text = it
        }
        viewModel.toolbarSubTitleSubject.subscribe {
            toolbarSubTitleText.text = it
        }
        viewModel.shareIconVisibleSubject.subscribeVisibility(toolbarShareIcon)
    }

    init {
        View.inflate(context, R.layout.widget_itin_toolbar, this)

        this.navigationIcon = context.getDrawable(R.drawable.ic_arrow_back_white_24dp)
        this.navigationContentDescription = context.getText(R.string.toolbar_nav_icon_cont_desc)
        this.setNavigationOnClickListener {
            viewModel.navigationBackPressedSubject.onNext(Unit)
        }
        toolbarShareIcon.setTintedDrawable(context.getDrawable(R.drawable.ic_itin_share), ContextCompat.getColor(context, R.color.itin_toolbar_text))
        toolbarShareIcon.setOnClickListener {
            viewModel.shareIconClickedSubject.onNext(Unit)
        }
    }
}
