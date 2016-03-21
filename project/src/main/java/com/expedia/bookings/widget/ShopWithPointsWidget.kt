package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeOnCheckChanged
import com.expedia.util.subscribeText
import com.expedia.util.subscribeVisibility
import com.expedia.vm.ShopWithPointsViewModel
import javax.inject.Inject

class ShopWithPointsWidget(context: Context, val attrs: AttributeSet?) : LinearLayout (context, attrs) {

    val loyaltyAppliedHeader: TextView by bindView(R.id.loyalty_applied_header)
    val loyaltyPointsInfo: TextView by bindView(R.id.loyalty_points_info)
    val swpSwitchView: android.widget.Switch by bindView(R.id.swp_switch)
    val swpLogo: ImageView by bindView(R.id.swp_logo)

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set

    init {
        var layoutId = R.layout.widget_shop_with_points
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ShopWithPointsWidget, 0, 0)
        try {
            if (ta.getBoolean(R.styleable.ShopWithPointsWidget_show_in_card_layout, false))
                layoutId = R.layout.widget_shop_with_points_with_card_view

        } finally {
            ta.recycle()
        }

        View.inflate(context, layoutId, this)
        Ui.getApplication(context).hotelComponent().inject(this)
        if (Db.getAbacusResponse().isUserBucketedForTest(AbacusUtils.EBAndroidAppHotelsSearchScreenTest)) {
            loyaltyAppliedHeader.setTextColor(context.resources.getColor(R.color.search_screen_icon_color_v2))
            loyaltyPointsInfo.setTextColor(context.resources.getColor(R.color.search_screen_icon_color_v2))
            swpLogo.setImageResource(R.drawable.swp_grey)
        }

        shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribeVisibility(this)
        shopWithPointsViewModel.pointsDetailStringObservable.subscribeText(loyaltyPointsInfo)
        swpSwitchView.subscribeOnCheckChanged(shopWithPointsViewModel.shopWithPointsToggleObservable)
        shopWithPointsViewModel.swpHeaderStringObservable.subscribeText(loyaltyAppliedHeader)
    }
}
