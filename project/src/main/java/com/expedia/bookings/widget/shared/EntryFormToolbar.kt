package com.expedia.bookings.widget.shared

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import com.expedia.bookings.R
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.util.notNullAndObservable
import com.expedia.vm.EntryFormToolbarViewModel
import kotlin.properties.Delegates

class EntryFormToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs) {
    var menuItem: MenuItem by Delegates.notNull()
    var toolbarNavIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)

    var viewModel: EntryFormToolbarViewModel by notNullAndObservable { vm ->
        vm.formFilledIn.subscribe { isFilledIn ->
            if (isFilledIn) {
                updateMenuTitle(R.string.done, R.string.done_cont_desc)
            } else {
                updateMenuTitle(R.string.next, R.string.next_cont_desc)
            }
        }
    }

    init {
        setNavigationOnClickListener {
            val activity = context as AppCompatActivity
            activity.onBackPressed()
        }

        toolbarNavIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        navigationIcon = toolbarNavIcon

        inflateMenu(R.menu.checkout_menu)
        menuItem = menu.findItem(R.id.menu_done)
        menuItem.isVisible = true
        updateMenuTitle(R.string.done, R.string.done_cont_desc)
        menuItem.setOnMenuItemClickListener { item ->
            when (item.title) {
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

    private fun updateMenuTitle(titleId: Int, contDescId: Int) {
        menuItem.title = context.getString(titleId)
        MenuItemCompat.setContentDescription(menuItem, context.getString(contDescId))
    }
}
