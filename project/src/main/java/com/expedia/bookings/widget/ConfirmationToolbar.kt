package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v4.view.MenuItemCompat.setActionView
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import kotlin.properties.Delegates

class ConfirmationToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs) {

    var menuItem: MenuItem by Delegates.notNull()

    init {
        inflateMenu(R.menu.confirmation_menu)
        menuItem = menu.findItem(R.id.menu_share)
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing)) {
            menuItem.icon = null
        }
        AccessibilityUtil.setMenuItemContentDescription(this, context.getString(R.string.share_action_content_description))

        menuItem.setOnMenuItemClickListener {
//            TODO: use same share method as Itins
            false
        }
        setNavigationOnClickListener {
//            TODO: send user back to launch screen (not itin screen)
        }

        val navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        navigationIcon = navIcon
        setNavigationContentDescription(R.string.toolbar_nav_icon_close_cont_desc)
    }
}
