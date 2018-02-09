package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.bindView
import com.expedia.util.subscribeOnClick
import com.expedia.vm.BaseFlightFilterViewModel.CheckedFilterProperties
import io.reactivex.Observer

class LabeledCheckableFilterWithPriceAndLogo<T>(context: Context, attrs: AttributeSet) : LabeledCheckableFilter<T>(context, attrs) {
    val logoImage: ImageView by bindView(R.id.airline_logo_image_view)

    fun bind(filterName: String, filterValue: T, filterResults: CheckedFilterProperties?, showLogo: Boolean, observer: Observer<T>) {
        this.observer = observer
        stopsLabel.text = filterName
        value = filterValue
        resultsLabel.text = Money.getFormattedMoneyFromAmountAndCurrencyCode(filterResults?.minPrice?.roundedAmount, filterResults?.minPrice?.currencyCode, Money.F_NO_DECIMAL).toString()
        if (showLogo) {
            logoImage.visibility = View.VISIBLE
            setAirlineLogo(filterResults?.logo)
        } else {
            logoImage.visibility = View.GONE
        }
        checkBox.isChecked = false
        subscribeOnClick(checkObserver)
    }

    fun bind(filterName: String, filterValue: T, filterResults: CheckedFilterProperties?) {
        stopsLabel.text = filterName
        value = filterValue
        resultsLabel.text = Money.getFormattedMoneyFromAmountAndCurrencyCode(filterResults?.minPrice?.roundedAmount, filterResults?.minPrice?.currencyCode, Money.F_NO_DECIMAL).toString()
        checkBox.isChecked = true
        checkBox.isEnabled = false
        logoImage.visibility = View.GONE
        this.isClickable = false
    }

    fun setAirlineLogo(filterLogoUrl: String?) {
        if (filterLogoUrl != null) {
            PicassoHelper.Builder(logoImage)
                    .setPlaceholder(R.drawable.ic_airline_backup)
                    .build()
                    .load(filterLogoUrl)
        } else {
            PicassoHelper.Builder(logoImage)
                    .build()
                    .load(R.drawable.ic_airline_backup)
        }
    }
}
