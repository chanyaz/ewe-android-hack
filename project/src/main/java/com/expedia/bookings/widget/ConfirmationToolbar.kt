package com.expedia.bookings.widget

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.Toolbar
import android.util.AttributeSet
import android.view.MenuItem
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.NavUtils

class ConfirmationToolbar(context: Context, attrs: AttributeSet?) : Toolbar(context, attrs) {

    val menuItem: MenuItem by lazy {
        val item = menu.findItem(R.id.menu_share)
        val variateForTest = Db.getAbacusResponse().variateForTest(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing)
        if (variateForTest == AbacusUtils.DefaultTwoVariant.VARIANT2.ordinal) {
            item.icon = null
        }
        AccessibilityUtil.setMenuItemContentDescription(this, context.getString(R.string.share_action_content_description))
        item
    }

    init {
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightsConfirmationItinSharing)) {
            inflateMenu(R.menu.confirmation_menu)
            menuItem.setOnMenuItemClickListener {
//            TODO: use same share method as Itins
                false
            }
        }

        setNavigationOnClickListener {
            NavUtils.goToLaunchScreen(context)
        }

        val navIcon = ArrowXDrawableUtil.getNavigationIconDrawable(context, ArrowXDrawableUtil.ArrowDrawableType.CLOSE)
        navIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        navigationIcon = navIcon
        setNavigationContentDescription(R.string.toolbar_nav_icon_close_cont_desc)
    }
}
