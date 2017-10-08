package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CustomListAdapter
import com.expedia.model.CustomPojo

class WhatsNewFragment : Fragment() {

    val latest_features: TextView by bindView(R.id.latest_features_of_app)
    val listView: ListView by bindView(R.id.list_to_be)
    val values = arrayOf("Android", "iPhone", "WindowsMobile",
        "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
        "Linux", "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux",
        "OS/2", "Ubuntu", "Windows7", "Max OS X", "Linux", "OS/2",
        "Android", "iPhone", "WindowsMobile")


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_whats_new, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        latest_features.text = "Whats new , Huh!"
        val listData = Array<CustomPojo>(values.size, { i ->  CustomPojo(name = values.get(i))})
        val adapter = CustomListAdapter(data = listData, context = this.context)
        listView.adapter = adapter
    }
}