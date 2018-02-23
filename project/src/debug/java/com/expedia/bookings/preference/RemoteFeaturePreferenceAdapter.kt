package com.expedia.bookings.preference

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.featureconfig.SatelliteFeatureConfigManager
import com.expedia.bookings.features.Feature
import com.mobiata.android.util.SettingUtils

class RemoteFeaturePreferenceAdapter(val context: Context, val feature: Feature) : RecyclerView.Adapter<RemoteFeaturePreferenceAdapter.ViewHolder>() {
    data class PreferenceViewModel(val name: String, val currentValue: () -> Boolean, val update: ((Boolean) -> Unit)? = null)

    private val preferenceOverrideOnKey = "remoteFeatures-localOverride-On"
    private val preferenceOverrideOffKey = "remoteFeatures-localOverride-Off"

    private val namesAndValues: List<PreferenceViewModel> by lazy {
        listOf(
                PreferenceViewModel("Enabled with Satellite", {
                    SatelliteFeatureConfigManager.isEnabled(context, feature.name)
                }),
                PreferenceViewModel("Turn on Locally", {
                    val set = SettingUtils.getStringSet(context, preferenceOverrideOnKey)
                    set.contains(feature.name)
                }, { isChecked ->
                    val set = SettingUtils.getStringSet(context, preferenceOverrideOnKey).toMutableSet()
                    if (isChecked) {
                        set.add(feature.name)
                    } else {
                        set.remove(feature.name)
                    }
                    SettingUtils.saveStringSet(context, preferenceOverrideOnKey, set)
                }),
                PreferenceViewModel("Turn off Locally", {
                    val set = SettingUtils.getStringSet(context, preferenceOverrideOffKey)
                    set.contains(feature.name)
                }, { isChecked ->
                    val set = SettingUtils.getStringSet(context, preferenceOverrideOffKey).toMutableSet()
                    if (isChecked) {
                        set.add(feature.name)
                    } else {
                        set.remove(feature.name)
                    }
                    SettingUtils.saveStringSet(context, preferenceOverrideOffKey, set)
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
        holder?.enabled?.setOnCheckedChangeListener { _, isChecked ->
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
