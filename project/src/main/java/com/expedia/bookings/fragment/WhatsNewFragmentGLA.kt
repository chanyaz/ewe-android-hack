package com.expedia.bookings.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.expedia.model.CustomPojo
import com.expedia.ui.recyclerview.FeedItem
import com.expedia.ui.recyclerview.FeedItemList
import com.expedia.ui.recyclerview.GenericListAdapter
import com.expedia.ui.recyclerview.ItemTypeHandler
import com.google.gson.Gson

/**
 * Created by nbirla on 24/11/17.
 */
class WhatsNewFragmentGLA : Fragment() {

    val listView: RecyclerView by bindView(R.id.list_to_be)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_whats_new, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val data = dummyData()
        val listData = Array<CustomPojo>(data!!.size, { i -> data.get(i) })

        val feedItemList = FeedItemList()

        for(customPojo in listData){
            feedItemList.addItem(FeedItem(ItemTypeHandler.ItemViewType.DATEVH.name, customPojo.monthAndYear))

            for(pojo in customPojo.featureList){
                feedItemList.addItem(FeedItem(ItemTypeHandler.ItemViewType.HEADERVH.name, pojo.featureName))
                var feedItem = FeedItem(ItemTypeHandler.ItemViewType.DESCVH.name, pojo.featureDetails)
                if(listData.indexOf(customPojo) == listData.size - 1  && customPojo.featureList.indexOf(pojo) == customPojo.featureList.size - 1){
                    feedItem.setExpandState(FeedItem.ExpandState.COLLAPSED)
                } else{
                    feedItem.setExpandState(FeedItem.ExpandState.EXPANDED)
                }
                feedItemList.addItem(feedItem)
            }
        }
        val adapter = GenericListAdapter(feedItemList, ItemTypeHandler.CONTRACT);
        listView.layoutManager = LinearLayoutManager(getContext())
        listView.adapter = adapter
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


}