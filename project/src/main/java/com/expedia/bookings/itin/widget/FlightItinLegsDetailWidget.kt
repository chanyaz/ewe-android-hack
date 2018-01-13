package com.expedia.bookings.itin.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.fragment.ScrollableContentDialogFragment
import com.expedia.bookings.itin.adapter.FlightItinLegsDetailAdapter
import com.expedia.bookings.itin.data.FlightItinLegsDetailData
import com.expedia.bookings.itin.vm.FlightItinLegsDetailWidgetViewModel
import com.expedia.bookings.tracking.OmnitureTracking
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable
import com.facebook.FacebookSdk.getApplicationContext

class FlightItinLegsDetailWidget(context: Context?, attrs: AttributeSet?) : LinearLayout(context, attrs) {

    private val recyclerView: RecyclerView by bindView<RecyclerView>(R.id.flight_leg_recycler_view)
    val rulesAndRegulation by bindView<TextView>(R.id.flight_itin_rules_and_restriction)
    val rulesAndRegulationDivider by bindView<View>(R.id.flight_itin_rules_and_restriction_divider)
    val splitTicketDividerView by bindView<View>(R.id.flight_itin_leg_split_ticket_divider)
    val splitTicketText by bindView<TextView>(R.id.flight_itin_leg_split_ticket_text)
    private val DIALOG_TAG = "RULES_AND_RESTRICTION"

    init {
        View.inflate(context, R.layout.flight_itin_legs_detail_widget, this)
    }

    var viewModel: FlightItinLegsDetailWidgetViewModel by notNullAndObservable { vm ->
        vm.updateWidgetRecyclerViewSubject.subscribe { param ->
            setUpRecyclerView(param)
        }
        vm.rulesAndRestrictionDialogTextSubject.subscribe { param ->
            showRulesAndRestrictionDialog(param)
        }
        vm.shouldShowSplitTicketTextSubject.subscribe { param ->
            showSplitTicketText(param)
        }
    }

    private fun showSplitTicketText(show: Boolean) {
        if (show) {
            splitTicketDividerView.visibility = View.VISIBLE
            splitTicketText.visibility = View.VISIBLE
        }
    }

    private fun setUpRecyclerView(list: ArrayList<FlightItinLegsDetailData>) {
        val mAdapter = FlightItinLegsDetailAdapter(context,list);
        val mLayoutManager = LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    private fun showRulesAndRestrictionDialog(value : String) {
        if (value.isNotEmpty()) {
            val fragmentManager = (context as FragmentActivity).supportFragmentManager
            val dialog = ScrollableContentDialogFragment.newInstance(context.resources.getString(R.string.itin_flight_leg_detail_widget_rules_and_restrictions), value)
            rulesAndRegulation.visibility = View.VISIBLE
            rulesAndRegulationDivider.visibility = View.VISIBLE
            AccessibilityUtil.appendRoleContDesc(rulesAndRegulation, rulesAndRegulation.text.toString(), R.string.accessibility_cont_desc_role_button)
            rulesAndRegulation.setOnClickListener {
                dialog.show(fragmentManager, DIALOG_TAG)
                OmnitureTracking.trackFlightItinLegDetailWidgetRulesAndRestrictionsDialogClick()
            }
        }
    }
}
