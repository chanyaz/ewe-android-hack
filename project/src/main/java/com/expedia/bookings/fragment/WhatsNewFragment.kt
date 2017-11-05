package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.CustomListAdapter
import com.expedia.model.CustomPojo
import com.google.gson.Gson

class WhatsNewFragment : Fragment() {

    val listView: ListView by bindView(R.id.list_to_be)


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_whats_new, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = dummyData()
        val listData = Array<CustomPojo>(data!!.size, { i ->  data.get(i)})
        val adapter = CustomListAdapter(data = listData, context = this.context)
        listView.adapter = adapter
    }

    /*private fun dummyData(): Array<CustomPojo>? {
        val data = """
        [
            {
                "monthAndYear": "July, 2017",
                "featureList": [
                    "We've made flight searches so much faster, that you'll get younger every time you click on a search. Well not really, but it's still pretty fast!!"
                ]
            },
            {
                "monthAndYear": "April, 2017",
                "featureList": [
                    "Now, get even better fares with SubPub"
                ]
            },
            {
                "monthAndYear": "August, 2017",
                "featureList": [
                    "You can use your Expedia loyalty points now to even book Local Activities! Go crazy!! ",
                    "Your favorite travel app now gives you the option to purchase business class tickets! Book your next trip now! "
                ]
            },
            {
                "monthAndYear": "September, 2017",
                "featureList": [
                    "Get a quick history of all searches you've made recently to continue from where you left off, right on your app's home screen! "
                ]
            }
        ]
        """
        return Gson().fromJson(data, Array<CustomPojo>::class.java)
    }*/
    private fun dummyData(): Array<CustomPojo>? {
        val data = """
        [
            {
                "monthAndYear": "July, 2017",
                "featureList": []
            },
            {
                "monthAndYear": "April, 2017",
                "featureList": []
            },
            {
                "monthAndYear": "August, 2017",
                "featureList": [
                    {
                        "featureName": "Business class Tickets!!",
                        "featureDetails": "Now you get the conveience of booking business class tickets right from the your favorite travel app!"
                    }
                ]
            },
            {
                "monthAndYear": "September, 2017",
                "featureList": [
                    {
                        "featureName": "Upsell!!",
                        "featureDetails": "Expedia apps now give you the option to see the difference in prices that you are paying and the prices for an upgraded seat!"
                    }
                ]
            }
        ]
        """
        return Gson().fromJson(data, Array<CustomPojo>::class.java)
    }
}