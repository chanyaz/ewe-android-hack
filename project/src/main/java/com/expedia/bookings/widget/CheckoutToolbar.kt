package com.expedia.bookings.widget

import android.content.Context
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import com.expedia.bookings.R
import com.expedia.util.notNullAndObservable
import com.expedia.vm.CheckoutToolbarViewModel
import kotlin.properties.Delegates

public class CheckoutToolbar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs) {
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
        menuItem.setOnMenuItemClickListener {
            viewModel.itemClicked.onNext(Unit)
            true
        }
    }
}