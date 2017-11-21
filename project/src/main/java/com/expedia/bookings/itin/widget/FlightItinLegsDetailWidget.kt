package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.itin.adapter.FlightItinLegsDetailAdapter
import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import com.expedia.bookings.itin.vm.FlightItinLegsDetailWidgetViewModel
import com.expedia.bookings.utils.bindView
import com.expedia.util.notNullAndObservable
import com.facebook.FacebookSdk.getApplicationContext

class FlightItinLegsDetailWidget(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val recyclerView: RecyclerView by bindView<RecyclerView>(R.id.flight_leg_recycler_view)

    init {
        View.inflate(context, R.layout.flight_itin_legs_detail_widget, this)
    }

    var viewModel: FlightItinLegsDetailWidgetViewModel by notNullAndObservable { vm ->
        vm.updateWidgetRecyclerViewSubjet.subscribe { param ->
            setUpRecyclerView(param)
        }
    }

    private fun setUpRecyclerView(list: ArrayList<FlightItinLegsDetailData>) {
        val mAdapter = FlightItinLegsDetailAdapter(context,list);
        val mLayoutManager = LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }
}