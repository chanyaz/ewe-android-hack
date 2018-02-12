package com.expedia.bookings.preference

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager
import com.expedia.bookings.features.Feature
import com.expedia.bookings.features.Features
import com.mobiata.android.util.SettingUtils

class RemoteFeaturePreferenceFragment : Fragment() {

    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = arguments.getString("name")
    }

    override fun onStart() {
        super.onStart()
        activity.title = name
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
            val feature = feature()
            if (feature != null) {
                view.adapter = RemoteFeaturePreferenceAdapter(context, name, feature)
            }
        }
        return view
    }

    private fun feature(): Feature? {
        return Features.all.namesAndFeatures().find { it.first == name }?.second
    }
}

class RemoteFeaturePreferenceAdapter(val context: Context, val key: String, val feature: Feature) : RecyclerView.Adapter<RemoteFeaturePreferenceAdapter.ViewHolder>() {
    data class PreferenceViewModel(val name: String, val currentValue: () -> Boolean, val update: ((Boolean) -> Unit)? = null)

    private val namesAndValues: List<PreferenceViewModel> by lazy {
        listOf(
                PreferenceViewModel("Enabled with Satellite", {
                    SatelliteFeatureConfigManager.isEnabled(context, key)
                }),
                PreferenceViewModel("Turn on Locally", {
                    SettingUtils.get(context, key, false)
                }, { isChecked ->
                    SettingUtils.save(context, key, isChecked)
                }),
                PreferenceViewModel("Final Result", {
                    feature.enabled()
                })
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.row_feature_preference, parent, false)
        return RemoteFeaturePreferenceAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return namesAndValues.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val (name, currentValue, checkedChanged) = namesAndValues[position]
        holder?.name?.text = name
        holder?.enabled?.isClickable = checkedChanged != null
        holder?.enabled?.isChecked = currentValue()
        holder?.enabled?.setOnCheckedChangeListener { view, isChecked ->
            checkedChanged?.invoke(isChecked)
            Handler(Looper.getMainLooper()).post {
                notifyDataSetChanged()
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView by lazy { itemView.findViewById(R.id.name) as TextView }
        val enabled: CheckBox by lazy { itemView.findViewById(R.id.enabled) as CheckBox }
    }
}
