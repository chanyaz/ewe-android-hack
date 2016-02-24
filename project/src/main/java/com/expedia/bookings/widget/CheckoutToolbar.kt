package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import com.expedia.bookings.R
import com.expedia.bookings.interfaces.ToolbarListener
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.util.notNullAndObservable
import com.expedia.vm.CheckoutToolbarViewModel
import kotlin.properties.Delegates

public class CheckoutToolbar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs), ToolbarListener {
    var menuItem: MenuItem by Delegates.notNull()

    var viewModel: CheckoutToolbarViewModel by notNullAndObservable { vm ->
        vm.toolbarTitle.subscribe {
            title = it
        }
        vm.menuTitle.subscribe {
            menuItem.setTitle(it)
        }
        vm.enableMenu.subscribe {
            menuItem.setVisible(it)
        }
    }

    init {
        inflateMenu(R.menu.checkout_menu)
        menuItem = menu.findItem(R.id.menu_done)
        menuItem.setVisible(false)
        menuItem.setOnMenuItemClickListener { it ->
            when (it.title) {
                context.getString(R.string.done) -> {
                    viewModel.doneClicked.onNext(Unit)
                }
                context.getString(R.string.next) -> {
                    viewModel.nextClicked.onNext(Unit)
                }
            }
            true
        }
    }

    override fun setActionBarTitle(title: String?) {
        viewModel.toolbarTitle.onNext(title)
    }

    override fun onWidgetExpanded(cardView: ExpandableCardView?) {

    }

    override fun onWidgetClosed() {

    }

    override fun onEditingComplete() {
        viewModel.enableMenu.onNext(true)
    }

    override fun setMenuLabel(title: String?) {
        viewModel.menuTitle.onNext(title)
    }

    override fun showRightActionButton(enabled: Boolean) {
        viewModel.enableMenu.onNext(enabled)
    }

    override fun setNavArrowBarParameter(arrowDrawableType: ArrowXDrawableUtil.ArrowDrawableType?) {

    }
}