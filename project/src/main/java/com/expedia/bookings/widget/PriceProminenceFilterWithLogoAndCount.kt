package com.expedia.bookings.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.bitmaps.PicassoHelper
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Money
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.abacus.AbacusVariant
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.utils.bindView
import com.expedia.vm.BaseFlightFilterViewModel.CheckedFilterProperties
import com.squareup.phrase.Phrase
import io.reactivex.Observer

class PriceProminenceFilterWithLogoAndCount<T>(context: Context, attrs: AttributeSet) : LabeledCheckableFilter<T>(context, attrs) {
    val logoImage: ImageView by bindView(R.id.airline_logo_image_view)
    val countLabel: TextView by bindView(R.id.count_label)
    val filterPriceLogoTestVariant = Db.sharedInstance.abacusResponse.variateForTest(AbacusUtils.EBAndroidAppFlightsFiltersPriceAndLogo)
    var resultCount: Int = 0

    fun bind(filterName: String, filterValue: T, filterResults: CheckedFilterProperties?, showLogo: Boolean, observer: Observer<T>) {
        this.observer = observer
        stopsLabel.text = filterName
        value = filterValue
        resultCount = filterResults?.count ?: 0
        resultsLabel.text = Money.getFormattedMoneyFromAmountAndCurrencyCode(filterResults?.minPrice?.roundedAmount, filterResults?.minPrice?.currencyCode, Money.F_NO_DECIMAL).toString()
        if (showLogo) {
            logoImage.visibility = View.VISIBLE
            setAirlineLogo(filterResults?.logo)
        } else {
            logoImage.visibility = View.GONE
        }
        showCountLabel(filterResults?.count ?: 0)
        checkBox.isChecked = false
        refreshContentDescription()
        subscribeOnClick(checkObserver)
    }

    fun bind(filterName: String, filterValue: T, filterResults: CheckedFilterProperties?) {
        stopsLabel.text = filterName
        value = filterValue
        resultCount = filterResults?.count ?: 0
        resultsLabel.text = Money.getFormattedMoneyFromAmountAndCurrencyCode(filterResults?.minPrice?.roundedAmount, filterResults?.minPrice?.currencyCode, Money.F_NO_DECIMAL).toString()
        checkBox.isChecked = true
        checkBox.isEnabled = false
        logoImage.visibility = View.GONE
        showCountLabel(filterResults?.count ?: 0)
        setDisabledContentDescription()
        this.isClickable = false
    }

    fun showCountLabel(count: Int) {
        if (filterPriceLogoTestVariant == AbacusVariant.TWO.value && count != 0) {
            countLabel.text = getCountLabel(count)
            countLabel.visibility = View.VISIBLE
        } else {
            countLabel.visibility = View.GONE
        }
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

    fun getCountLabel(count: Int): String {
        when (count) {
            1 -> return resources.getString(R.string.flight_one_filter_result_description)
            else -> return Phrase.from(context, R.string.flight_filter_result_count_TEMPLATE).put("count", count).format().toString()
        }
    }

    override fun refreshContentDescription() {
        val contentDesc: StringBuilder
        if (filterPriceLogoTestVariant == AbacusVariant.TWO.value) {
            contentDesc = StringBuilder(Phrase.from(context, R.string.flight_filter_checkbox_with_price_and_count_cont_desc_TEMPLATE)
                    .put("filter_name", stopsLabel.text)
                    .put("filter_results", resultCount).put("price", resultsLabel.text)
                    .format().toString())
        } else {
            contentDesc = StringBuilder(Phrase.from(context, R.string.flight_filter_checkbox_with_price_cont_desc_TEMPLATE)
                    .put("filter_name", stopsLabel.text)
                    .put("price", resultsLabel.text)
                    .format().toString())
        }

        if (checkBox.isChecked) {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_checked))
        } else {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_unchecked))
        }
        contentDescription = contentDesc
    }

    override fun setDisabledContentDescription() {
        val contentDesc: StringBuilder
        if (filterPriceLogoTestVariant == AbacusVariant.TWO.value) {
            contentDesc = StringBuilder(Phrase.from(context, R.string.flight_filter_checkbox_disabled_with_price_and_count_cont_desc_TEMPLATE)
                    .put("filter_name", stopsLabel.text)
                    .put("filter_results", resultCount).put("price", resultsLabel.text)
                    .format().toString())
        } else {
            contentDesc = StringBuilder(Phrase.from(context, R.string.flight_filter_checkbox_disabled_with_price__cont_desc_TEMPLATE)
                    .put("filter_name", stopsLabel.text)
                    .put("price", resultsLabel.text)
                    .format().toString())
        }
        if (checkBox.isChecked) {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_checked))
        } else {
            contentDesc.append(context.getString(R.string.accessibility_cont_desc_role_checkbox_unchecked))
        }
        contentDescription = contentDesc
    }
}
