package com.expedia.bookings.launch.widget

import android.support.v7.widget.RecyclerView
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.bindView

class BrandHeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private val brandHeaderView: View by bindView(R.id.brand_header_layout)

    init {
        if (AbacusFeatureConfigManager.isBucketedForTest(view.context, AbacusUtils.EBAndroidAppBrandColors)) {
            brandHeaderView.setBackgroundResource(R.color.brand_primary)
        }
    }
}
