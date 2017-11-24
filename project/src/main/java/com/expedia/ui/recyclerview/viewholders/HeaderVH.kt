package com.expedia.ui.recyclerview.viewholders

import android.view.View
import android.widget.TextView
import butterknife.InjectView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.ui.recyclerview.ItemVH

/**
 * Created by nbirla on 24/11/17.
 */
class HeaderVH(root : View) : ItemVH<String>(root) {

    val headerView: TextView by bindView(R.id.label_header)

    init {
        headerView.setOnClickListener(this)
    }

    override fun bindData(v: String) {
        headerView.text = v
        headerView.setTag(v)
    }
}