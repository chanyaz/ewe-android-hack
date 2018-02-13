package com.expedia.bookings.preference

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.features.Features

class RemoteFeaturesListAdapter(private val listener: OnFeatureClickedListener) : RecyclerView.Adapter<RemoteFeaturesListAdapter.ViewHolder>() {

    interface OnFeatureClickedListener {
        fun featureClicked(name: String)
    }

    private val namesAndFeatures by lazy {
        Features.all.namesAndFeatures()
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.row_feature_preference, parent, false)
        return RemoteFeaturesListAdapter.ViewHolder(view)
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
