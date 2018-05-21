package com.expedia.bookings.preference

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.TextView
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.utils.bindView
import com.mobiata.android.util.SettingUtils

class TripsScenarioSelectFragment : Fragment() {
    private val recyclerView: RecyclerView by bindView(R.id.trip_scenarios_select_recycler_view)
    private val scenarios: MutableList<TripMockScenarios.Scenarios> = mutableListOf()

    override fun onStart() {
        super.onStart()
        activity.title = "Select trip scenario"
        TripMockScenarios.Scenarios.values().forEach {
            scenarios.add(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.debug_trip_scenarios_select, null)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = TripsScenariosAdapter(context, scenarios)
        }
    }
}

class TripsScenariosAdapter(private val context: Context, private val scenarios: List<TripMockScenarios.Scenarios>) : RecyclerView.Adapter<TripsScenariosAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_selectable_list_item, parent, false) as CheckedTextView
        return ViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return scenarios.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = scenarios[position].name
        holder.itemView.setOnClickListener {
            SettingUtils.save(context, TripMockScenarios.TRIP_SCENARIOS_KEY, scenarios[position].filename)
            Toast.makeText(context, "Mock set: " + scenarios[position], Toast.LENGTH_SHORT).show()
        }
    }

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}
