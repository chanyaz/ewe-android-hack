package com.expedia.ui.recyclerview.viewholders

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import butterknife.InjectView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.ui.recyclerview.FeedItem
import com.expedia.ui.recyclerview.ItemVH

/**
 * Created by nbirla on 24/11/17.
 */

class DescVH(root : View) : ItemVH<String>(root) {

    val descView: TextView by bindView(R.id.label_desc)
    val sep: LinearLayout by bindView(R.id.vertical_sep)

    init {
        descView.setOnClickListener(this)
    }

    override fun bindData(v: String) {
        descView.text = v
        descView.setTag(v)

        if(getFeedItem()!!.getExpandState() == FeedItem.ExpandState.EXPANDED){
            sep.visibility = View.VISIBLE
        } else{
            sep.visibility = View.GONE
        }
    }
}