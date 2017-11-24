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

class DateVH(root : View) : ItemVH<String>(root) {

    val dateView: TextView by bindView(R.id.label12)

    init {
        dateView.setOnClickListener(this)
    }

    override fun bindData(v: String) {
        dateView.text = v
        dateView.setTag(v)
    }
}