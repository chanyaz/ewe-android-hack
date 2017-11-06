package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.view.menu.ActionMenuItemView
import android.support.v7.view.menu.MenuBuilder
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.interfaces.ToolbarListener
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.isSecureIconEnabled
import com.expedia.util.endlessObserver
import com.expedia.util.notNullAndObservable
import com.expedia.vm.CheckoutToolbarViewModel
import rx.Observable
import kotlin.properties.Delegates

class CheckoutToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs), ToolbarListener, MenuBuilder.Callback {
    var menuItem: MenuItem by Delegates.notNull()
    var currentFocus: View? = null
    var toolbarNavIcon = ArrowXDrawableUtil.getNavigationIconDrawable(getContext(), ArrowXDrawableUtil.ArrowDrawableType.BACK)

    var viewModel: CheckoutToolbarViewModel by notNullAndObservable { vm ->
        vm.toolbarTitle.subscribe {
            title = it
        }
        vm.toolbarSubtitle.subscribe {
            subtitle = it
        }
        if (isSecureIconEnabled(context)) {
            vm.toolbarTitle.subscribe(vm.toolbarCustomTitle)
            vm.hideToolbarTitle.subscribe {
                title = ""
            }
        }
        vm.menuTitle.subscribe {
            menuItem.title = it
        }
        vm.menuVisibility.subscribe {
            menuItem.isVisible = it
        }
        vm.enableMenuItem.subscribe { enable ->
            val view = findViewById<ActionMenuItemView?>(R.id.menu_done)
            if (view != null) {
                view.alpha = if (enable) 1f else 0.15f
            }
            menuItem.isVisible = true
            menuItem.isEnabled = enable
        }
        vm.nextClicked.subscribe {
            setNextFocus()
        }
        vm.visibleMenuWithTitleDone.subscribe {
            menuItem.isVisible = true
            menuItem.title = context.getString(R.string.done)
        }

        vm.currentFocus.subscribe {
            currentFocus = it
        }

        vm.toolbarNavIcon.subscribe {
            setNavArrowBarParameter(it)
        }

        vm.formFilledIn.subscribe { isFilledIn ->
            vm.menuTitle.onNext(if (isFilledIn) context.getString(R.string.done) else context.getString(R.string.next))
        }

        vm.toolbarNavIconContentDesc.subscribe {
            navigationContentDescription = it
        }

        Observable.combineLatest(vm.menuVisibility, vm.formFilledIn, { menuVisibility, formFilledIn -> Pair(menuVisibility, formFilledIn) })
                .filter { it.first }
                .subscribe {
                    AccessibilityUtil.setMenuItemContentDescription(this, if (it.second) context.getString(R.string.done_cont_desc) else context.getString(R.string.next_cont_desc))
                }

    }

    init {
        inflateMenu(R.menu.checkout_menu)
        setMenuCallbacks(null, this)
        menuItem = menu.findItem(R.id.menu_done)
        menuItem.isVisible = false
        menuItem.setOnMenuItemClickListener { menuItem ->
            viewModel.onMenuItemClicked(menuItem.title)
        }

        toolbarNavIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        navigationIcon = toolbarNavIcon
        setNavigationContentDescription(R.string.toolbar_nav_icon_cont_desc)
    }

    val toggleMenuObserver = endlessObserver<Boolean> { visible ->
        menu.setGroupVisible(R.id.package_change_menu, visible)
    }

    override fun setActionBarTitle(title: String?) {
        viewModel.toolbarTitle.onNext(title ?: "")
    }

    override fun onWidgetExpanded(cardView: ExpandableCardView) {
        viewModel.expanded.onNext(cardView)
    }

    override fun onWidgetClosed() {
        viewModel.closed.onNext(Unit)
    }

    override fun onEditingComplete() {
        viewModel.menuVisibility.onNext(true)
    }

    override fun setMenuLabel(title: String?) {
        viewModel.menuTitle.onNext(title ?: "")
    }

    override fun showRightActionButton(enabled: Boolean) {
        viewModel.menuVisibility.onNext(enabled)
    }

    override fun setNavArrowBarParameter(arrowDrawableType: ArrowXDrawableUtil.ArrowDrawableType) {
        toolbarNavIcon.parameter = arrowDrawableType.type.toFloat()
    }

    override fun setCurrentViewFocus(view: View) {
        viewModel.currentFocus.onNext(view)
    }

    private fun setNextFocus() {
        val nextFocusId = currentFocus?.nextFocusDownId ?: 0
        var nextView = currentFocus?.focusSearch(View.FOCUS_FORWARD)
        if (nextView?.id != nextFocusId) {
            if (nextFocusId != -1) {
                nextView = currentFocus?.rootView?.findViewById(nextFocusId)
            }
        }
        nextView?.requestFocus()
    }

    override fun enableRightActionButton(enable: Boolean) {
        viewModel.enableMenuItem.onNext(enable)
    }

    override fun onMenuModeChange(menu: MenuBuilder?) {
        viewModel.overflowClicked.onNext(Unit)
    }

    override fun onMenuItemSelected(menu: MenuBuilder?, item: MenuItem?): Boolean {
        return false
    }

}
