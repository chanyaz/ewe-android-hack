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

class RemoteFeaturesListFragment : Fragment(), RemoteFeaturesListAdapter.OnFeatureClickedListener {
    override fun onStart() {
        super.onStart()
        activity?.title = "Remote Feature Toggles"
    }

    override fun onResume() {
        super.onResume()
        (view as? RecyclerView)?.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.debug_recyclerview, container, false)

        if (view is RecyclerView) {
            view.layoutManager = LinearLayoutManager(context)
            view.adapter = RemoteFeaturesListAdapter(this)
        }
        return view
    }

    override fun featureClicked(feature: Feature) {
        val remoteFeaturePreferenceFragment = RemoteFeaturePreferenceFragment()
        val args = Bundle()
        args.putString("name", feature.name)
        remoteFeaturePreferenceFragment.arguments = args
        fragmentManager
                ?.beginTransaction()
                ?.replace(R.id.fragment_container, remoteFeaturePreferenceFragment)
                ?.addToBackStack(RemoteFeaturePreferenceFragment::class.java.name)
                ?.commit()
    }
}
