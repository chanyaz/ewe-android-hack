package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.expedia.bookings.R
import com.expedia.bookings.adapter.WhatsNewAdapter
import com.expedia.bookings.utils.WhatsNewService
import com.expedia.bookings.utils.bindView
import com.expedia.model.CustomPojo
import com.expedia.model.SuperPojo
import com.google.gson.Gson
import retrofit2.Call
import java.util.concurrent.CountDownLatch

/**
 * Created by nbirla on 20/11/17.
 */
class WhatsNewFragment : Fragment() {

    val listView: RecyclerView by bindView(R.id.list_to_be)
    val whatsNewService by lazy {
        WhatsNewService.create()
    }
    var b: SuperPojo? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_whats_new, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val latch = CountDownLatch(1)
        Thread(Runnable {
            b = getFeatures().execute().body()
            latch.countDown()
        }).start()
        latch.await()
//        val data = b?.features
        val data = dummyData()
        val listData = Array<CustomPojo>(data!!.size, { i -> data.get(i) })
        val adapter = WhatsNewAdapter()
        listView.layoutManager = LinearLayoutManager(getContext())
        listView.adapter = adapter
        adapter.addData(listData.asList())
    }

    private fun dummyData(): Array<CustomPojo>? {
        val data = """
        [
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
                    },
                    {
                        "featureName": "LX on Expedia+",
                        "featureDetails": "You can use you Expedia Loyalty Points now to even book Local Activities. Go Crazy!"
                    }
                ]
            }
        ]
        """
        return Gson().fromJson(data, Array<CustomPojo>::class.java)
    }

    private fun getFeatures(): Call<SuperPojo> {
        return whatsNewService.getFeatures(pos = "US", platform = "iOS", brand = "Expedia", approvalState = "APPROVED")
    }

}