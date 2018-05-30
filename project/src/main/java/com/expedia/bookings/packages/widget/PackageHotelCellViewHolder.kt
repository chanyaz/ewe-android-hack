package com.expedia.bookings.packages.widget

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeVisibility
import com.expedia.bookings.packages.vm.PackageHotelViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.shared.AbstractHotelResultCellViewHolder

class PackageHotelCellViewHolder(root: ViewGroup) : AbstractHotelResultCellViewHolder(root) {
    val unrealDealMessageContainer: LinearLayout by bindView(R.id.unreal_deal_container)
    val unrealDealMessage: TextView by bindView(R.id.unreal_deal_message)
    val packagePriceType: TextView by bindView(R.id.package_price_type)
    val packageIncludesTaxesAndFeesMessage: TextView by bindView(R.id.package_includes_taxes)

    init {
        bindViewModel()
    }

    fun bindViewModel() {
        viewModel as PackageHotelViewModel
        viewModel.unrealDealMessageObservable.subscribeText(unrealDealMessage)
        viewModel.unrealDealMessageVisibilityObservable.subscribeVisibility(unrealDealMessageContainer)
        viewModel.shouldDisplayPricingViews.subscribe { shouldDisplay ->
            packagePriceType.visibility = if (shouldDisplay) View.VISIBLE else View.GONE
            packageIncludesTaxesAndFeesMessage.visibility = if (shouldDisplay && PointOfSale.getPointOfSale().supportsPackagesHSRIncludesHeader()) View.VISIBLE else View.GONE
        }
    }

    override fun createHotelViewModel(context: Context): PackageHotelViewModel {
        return PackageHotelViewModel(context)
    }
}
