package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import com.expedia.bookings.R
import com.expedia.bookings.interfaces.ToolbarListener
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.CheckoutToolbarViewModel
import kotlin.properties.Delegates

class CheckoutToolbar(context: Context, attrs: AttributeSet) : Toolbar(context, attrs), ToolbarListener {
    var menuItem: MenuItem by Delegates.notNull()
    var currentEditText: EditText? = null
    var toolbarNavIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK);

    var viewModel: CheckoutToolbarViewModel by notNullAndObservable { vm ->
        vm.toolbarTitle.subscribe {
            title = it
        }
        vm.toolbarSubtitle.subscribe {
            subtitle = it
        }
        vm.menuTitle.subscribe {
            menuItem.setTitle(it)
        }
        vm.enableMenu.subscribe {
            menuItem.setVisible(it)
        }

        vm.nextClicked.subscribe {
            setNextFocus()
        }
        vm.enableMenuDone.subscribe { enable ->
            if (enable) {
                menuItem.setVisible(true)
                menuItem.setTitle(context.getString(R.string.done))
            }
        }

        vm.editText.subscribe {
            currentEditText = it
            setMenuTitle()
        }
    }

    init {
        inflateMenu(R.menu.checkout_menu)
        menuItem = menu.findItem(R.id.menu_done)
        menuItem.setVisible(false)
        menuItem.setOnMenuItemClickListener { it ->
            when (it.title) {
                context.getString(R.string.coupon_submit_button) -> {
                    viewModel.doneClicked.onNext(Unit)
                }
                context.getString(R.string.done) -> {
                    viewModel.doneClicked.onNext(Unit)
                }
                context.getString(R.string.next) -> {
                    viewModel.nextClicked.onNext(Unit)
                }
            }
            true
        }

        toolbarNavIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        navigationIcon = toolbarNavIcon
    }

    val toggleMenuObserver = endlessObserver<Boolean> { visible ->
        if (!visible) {
            overflowIcon?.alpha = (255f * 0.25).toInt()
            menu.setGroupEnabled(R.id.package_change_menu, false)
        } else {
            overflowIcon?.alpha = 255
            menu.setGroupEnabled(R.id.package_change_menu, true)
        }
    }

    override fun setActionBarTitle(title: String?) {
        viewModel.toolbarTitle.onNext(title)
    }

    override fun onWidgetExpanded(cardView: ExpandableCardView?) {
        viewModel.expanded.onNext(cardView)
    }

    override fun onWidgetClosed() {
        viewModel.closed.onNext(Unit)
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

    override fun setNavArrowBarParameter(arrowDrawableType: ArrowXDrawableUtil.ArrowDrawableType) {
        toolbarNavIcon.parameter = arrowDrawableType.type.toFloat()
    }

    override fun editTextFocus(editText: EditText?) {
        viewModel.editText.onNext(editText)
    }

    private fun setNextFocus()
    {
        var v: View? = currentEditText?.focusSearch(View.FOCUS_RIGHT)
        v?.requestFocus() ?: currentEditText?.focusSearch(View.FOCUS_DOWN)
    }

    private fun setMenuTitle()
    {
        var v: View? = currentEditText?.focusSearch(View.FOCUS_DOWN)
        val hasNextFocus = (v != null);
        viewModel.menuTitle.onNext(if (hasNextFocus) context.getString(R.string.next) else context.getString(R.string.done) )
    }
}