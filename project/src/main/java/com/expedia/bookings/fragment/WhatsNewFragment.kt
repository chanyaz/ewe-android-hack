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
import com.google.gson.Gson

class WhatsNewFragment : Fragment() {

    val latest_features: TextView by bindView(R.id.latest_features_of_app)
    val listView: ListView by bindView(R.id.list_to_be)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_whats_new, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        latest_features.text = "Whats new , Huh!"
        val data = dummyData()
        val listData = Array<CustomPojo>(data!!.size, { i ->  data.get(i)})
        val adapter = CustomListAdapter(data = listData, context = this.context)
        listView.adapter = adapter
    }

    private fun dummyData(): Array<CustomPojo>? {
        val data = """
        [
            {
                "monthAndYear": "July, 2017",
                "featureList": [
                    "Upsell Options New"
                ]
            },
            {
                "monthAndYear": "April, 2017",
                "featureList": [
                    "Now, get even better fares with SubPub"
                ]
            }
        ]
        """
        return Gson().fromJson(data, Array<CustomPojo>::class.java)
    }
}