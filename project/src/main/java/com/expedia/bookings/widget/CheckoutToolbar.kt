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
        vm.menuVisibility.subscribe {
            menuItem.setVisible(it)
        }
        vm.enableMenuItem.subscribe { enable ->
            getChildAt(0).alpha = if (enable) 1f else 0.15f
            menuItem.setVisible(true)
            menuItem.setEnabled(enable)
        }
        vm.nextClicked.subscribe {
            setNextFocus()
        }
        vm.visibleMenuWithTitleDone.subscribe {
            menuItem.setVisible(true)
            menuItem.setTitle(context.getString(R.string.done))
        }

        vm.editText.subscribe {
            currentEditText = it
            setMenuTitle()
        }

        vm.toolbarNavIcon.subscribe{
            setNavArrowBarParameter(it)
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
        menu.setGroupVisible(R.id.package_change_menu, visible)
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
        viewModel.menuVisibility.onNext(true)
    }

    override fun setMenuLabel(title: String?) {
        viewModel.menuTitle.onNext(title)
    }

    override fun showRightActionButton(enabled: Boolean) {
        viewModel.menuVisibility.onNext(enabled)
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
        v?.requestFocus() ?: currentEditText?.focusSearch(View.FOCUS_DOWN)?.requestFocus()
    }

    private fun setMenuTitle()
    {
        var right: View? = currentEditText?.focusSearch(View.FOCUS_RIGHT)
        val hasNextFocusRight = (right != null);

        var up: View? = currentEditText?.focusSearch(View.FOCUS_UP)
        val hasNextFocusUp = (up != null);

        var below: View? = currentEditText?.focusSearch(View.FOCUS_DOWN)
        val hasNextFocusDown = (below != null);

        val doesNotHaveFocus = !hasNextFocusDown && ((hasNextFocusRight && hasNextFocusUp) || !hasNextFocusRight)

        viewModel.menuTitle.onNext(if (doesNotHaveFocus) context.getString(R.string.done) else context.getString(R.string.next))
    }
}