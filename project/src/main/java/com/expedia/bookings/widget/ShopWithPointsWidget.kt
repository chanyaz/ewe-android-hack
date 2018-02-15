package com.expedia.bookings.widget

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.SwitchCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.extensions.subscribeOnCheckChanged
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.vm.ShopWithPointsViewModel
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class ShopWithPointsWidget(context: Context, val attrs: AttributeSet?) : LinearLayout (context, attrs) {

    val loyaltyAppliedHeader: TextView by bindView(R.id.loyalty_applied_header)
    val loyaltyPointsInfo: TextView by bindView(R.id.loyalty_points_info)
    val swpSwitchView: SwitchCompat by bindView(R.id.swp_switch)

    lateinit var shopWithPointsViewModel: ShopWithPointsViewModel
        @Inject set

    var subscription: Disposable

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
        loyaltyAppliedHeader.setTextColor(ContextCompat.getColor(context, R.color.hotelsv2_loyalty_applied_text_color))
        loyaltyPointsInfo.setTextColor(ContextCompat.getColor(context, R.color.hotelsv2_loyalty_applied_text_color))

        subscription = shopWithPointsViewModel.isShopWithPointsAvailableObservable.subscribeVisibility(this)
        shopWithPointsViewModel.pointsDetailStringObservable.subscribeText(loyaltyPointsInfo)
        swpSwitchView.subscribeOnCheckChanged(shopWithPointsViewModel.shopWithPointsToggleObservable)
        shopWithPointsViewModel.swpHeaderStringObservable.subscribeText(loyaltyAppliedHeader)
    }
}
