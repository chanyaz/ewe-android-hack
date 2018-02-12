package com.expedia.bookings.preference

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.features.Feature
import com.expedia.bookings.features.Features
import kotlin.reflect.full.memberProperties

class RemoteFeaturePreferencesFragment : Fragment(), RemoteFeaturePreferencesAdapter.OnFeatureClickedListener {
    override fun onStart() {
        super.onStart()
        activity.title = "Remote Feature Toggles"
    }

    override fun onResume() {
        super.onResume()
        (view as? RecyclerView)?.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (inflater == null) {
            return null
        }

        val view = inflater.inflate(R.layout.preference_recyclerview, container, false)

        if (view is RecyclerView) {
            view.layoutManager = LinearLayoutManager(context)
            //view.itemAnimator = DefaultItemAnimator()
            view.adapter = RemoteFeaturePreferencesAdapter(this)
        }
        return view
    }

    override fun featureClicked(name: String) {
        val remoteFeaturePreferenceFragment = RemoteFeaturePreferenceFragment()
        val args = Bundle()
        args.putString("name", name)
        remoteFeaturePreferenceFragment.arguments = args
        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, remoteFeaturePreferenceFragment)
                .addToBackStack(RemoteFeaturePreferenceFragment::class.java.name)
                .commit()
    }
}

fun Features.namesAndFeatures(): List<Pair<String, Feature>> {
    return this::class.memberProperties.mapNotNull {
        val feature = it.getter.call(this) as? Feature
        if (feature != null) {
            Pair(it.name, feature)
        } else {
            null
        }
    }
}

class RemoteFeaturePreferencesAdapter(private val listener: OnFeatureClickedListener) : RecyclerView.Adapter<RemoteFeaturePreferencesAdapter.ViewHolder>() {

    interface OnFeatureClickedListener {
        fun featureClicked(name: String)
    }

    private val namesAndFeatures by lazy {
        Features.all.namesAndFeatures()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.row_feature_preference, parent, false)
        return RemoteFeaturePreferencesAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return namesAndFeatures.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val (name, feature) = namesAndFeatures[position]
        holder?.itemView?.setOnClickListener {
            listener.featureClicked(name)
        }
        holder?.name?.text = name
        holder?.enabled?.isChecked = feature.enabled()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView by lazy { itemView.findViewById(R.id.name) as TextView }
        val enabled: CheckBox by lazy { itemView.findViewById(R.id.enabled) as CheckBox }
    }
}
