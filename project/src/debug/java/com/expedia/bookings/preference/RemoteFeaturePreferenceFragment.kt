package com.expedia.bookings.preference

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.features.Feature
import com.expedia.bookings.features.Features
import com.expedia.bookings.preference.extensions.features

class RemoteFeaturePreferenceFragment : Fragment() {

    private var name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = arguments?.getString("name")
    }

    override fun onStart() {
        super.onStart()
        activity?.title = name
    }

    override fun onResume() {
        super.onResume()
        (view as? RecyclerView)?.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.debug_recyclerview, container, false)

        if (view is RecyclerView) {
            view.layoutManager = LinearLayoutManager(context)
            val feature = findFeature()
            if (feature != null && context != null) {
                view.adapter = RemoteFeaturePreferenceAdapter(context!!, feature)
            }
        }
        return view
    }

    private fun findFeature(): Feature? {
        return Features.all.features().find { it.name == name }
    }
}
