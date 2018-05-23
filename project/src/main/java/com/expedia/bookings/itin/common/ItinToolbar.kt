package com.expedia.bookings.itin.common

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.Toolbar
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.itin.scopes.ToolBarViewModelSetter
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

class ItinToolbar(context: Context, attr: AttributeSet?) : Toolbar(context, attr), ToolBarViewModelSetter {
    override fun setUpViewModel(vm: NewItinToolbarViewModel) {
        viewModel = vm
    }

    val toolbarTitleText: TextView by bindView(R.id.itin_toolbar_title)
    val toolbarSubTitleText: TextView by bindView(R.id.itin_toolbar_subtitle)
    val toolbarShareIcon: TextView by bindView(R.id.itin_share_button)

    var viewModel: NewItinToolbarViewModel by notNullAndObservable {
        viewModel.toolbarTitleSubject.subscribeText(toolbarTitleText)
        viewModel.toolbarSubTitleSubject.subscribeTextAndVisibility(toolbarSubTitleText)
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
