package com.expedia.bookings.trace.fragment

import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceCategory
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.expedia.bookings.R
import com.expedia.bookings.utils.ClipboardUtils
import com.expedia.bookings.trace.data.DebugTracePreference
import com.expedia.bookings.preference.BasePreferenceFragment
import com.expedia.bookings.trace.data.DebugTraceData
import com.expedia.bookings.trace.util.ServerDebugTraceUtil
import com.mobiata.android.SocialUtils
import com.squareup.phrase.Phrase
import io.reactivex.disposables.Disposable

class ServerDebugTracingPreferenceFragment : BasePreferenceFragment() {

    private var selectedPreferences = ArrayList<DebugTracePreference>()

    private var togglePreference: Preference? = null
    private var preferenceCategory: PreferenceCategory? = null
    private var debugTracePreferences = ArrayList<DebugTracePreference>()
    private var observableToDispose = ArrayList<Disposable>()

    override fun onStart() {
        super.onStart()
        activity.title = context.getString(R.string.server_debug_trace)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        observableToDispose.add(ServerDebugTraceUtil.fetchingSubject.subscribe {
            togglePreference?.summary = context.getString(R.string.requesting_token)
        })

        observableToDispose.add(ServerDebugTraceUtil.successSubject.subscribe {
            togglePreference?.summary = context.getString(R.string.enabled_click_to_disable)
        })

        observableToDispose.add(ServerDebugTraceUtil.errorSubject.subscribe { error ->
            togglePreference?.summary = Phrase.from(context, R.string.error_template)
                    .put("error", error.message ?: context.getString(R.string.unknown_error))
                    .format().toString()
        })

        observableToDispose.add(ServerDebugTraceUtil.disabledSubject.subscribe {
            togglePreference?.summary = context.getString(R.string.disabled_click_to_enable)
        })

        setupInitialView()
    }

    override fun onDestroy() {
        super.onDestroy()
        for (preference in debugTracePreferences) {
            preference.debugTraceData.selected = false
        }
        observableToDispose.forEach { disposable ->
            disposable.dispose()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)
        menu?.clear()
        if (selectedPreferences.isEmpty()) {
            menu?.add(context.getString(R.string.clear_all))
        } else {
            menu?.add(context.getString(R.string.copy))
            menu?.add(context.getString(R.string.email))
            menu?.add(context.getString(R.string.delete))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.title) {
            context.getString(R.string.clear_all) -> {
                clearAllPreferences()
                return true
            }
            context.getString(R.string.copy) -> {
                copyClipboardSelectedPreferences()
                Toast.makeText(context, context.getString(R.string.selected_data_copied), Toast.LENGTH_SHORT).show()
                return true
            }
            context.getString(R.string.email) -> {
                emailSelectedPreferences()
                return true
            }
            context.getString(R.string.delete) -> {
                deleteSelectedPreferences()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        when {
            (preference.key == context.getString(R.string.preference_toggle_server_debug_tracing)) -> {
                ServerDebugTraceUtil.toggleDebugTrace()
                return true
            }
            (preference is DebugTracePreference) -> {
                if (selectedPreferences.contains(preference)) {
                    preference.updateSelected(false)
                    selectedPreferences.remove(preference)
                } else {
                    preference.updateSelected(true)
                    selectedPreferences.add(preference)
                }
                return true
            }
            else -> {
                return super.onPreferenceTreeClick(preference)
            }
        }
    }

    private fun setupInitialView() {
        addPreferencesFromResource(R.xml.preferences_server_debug_tracing)
        val toggle = findPreference(context.getString(R.string.preference_toggle_server_debug_tracing))

        if (toggle == null) {
            Toast.makeText(context, context.getString(R.string.cant_find_debug_trace_toggle), Toast.LENGTH_LONG).show()
        } else {
            togglePreference = toggle

            if (ServerDebugTraceUtil.isDebugTracingAvailable()) {
                toggle.summary = context.getString(R.string.enabled_click_to_disable)
            } else {
                toggle.summary = context.getString(R.string.disabled_click_to_enable)
            }
        }

        if (ServerDebugTraceUtil.debugTraceData.isNotEmpty()) {
            preferenceCategory = PreferenceCategory(context)
            preferenceCategory?.title = context.getString(R.string.debug_trace_ids)
            preferenceCategory?.layoutResource = R.layout.preference_category_title
            preferenceScreen.addPreference(preferenceCategory)

            for (data in ServerDebugTraceUtil.debugTraceData) {
                val traceData = DebugTraceData(data.first, data.second)
                val preference = DebugTracePreference(context, traceData)
                preferenceCategory?.addPreference(preference)
                debugTracePreferences.add(preference)
            }
        }
    }

    private fun selectedPreferenceToString(): String {
        val sb = StringBuilder()
        for (preference in selectedPreferences) {
            sb.append(preference.debugTraceData.url)
            sb.append(System.lineSeparator())
            sb.append(context.getString(R.string.trace_id))
            sb.append(preference.debugTraceData.traceId)
            sb.append(System.lineSeparator())
        }

        return sb.toString()
    }

    private fun clearAllPreferences() {
        preferenceScreen.removeAll()
        ServerDebugTraceUtil.debugTraceData.clear()
        setupInitialView()
    }

    private fun copyClipboardSelectedPreferences() {
        val preferenceString = selectedPreferenceToString()
        ClipboardUtils.setText(context, preferenceString)
    }

    private fun emailSelectedPreferences() {
        val preferenceString = selectedPreferenceToString()
        try {
            SocialUtils.email(context, context.getString(R.string.server_debug_trace), preferenceString)
        } catch (e: Exception) {
            Toast.makeText(context, Phrase.from(context, R.string.error_template)
                    .put("error", e.message ?: context.getString(R.string.unknown_error))
                    .format().toString(), Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteSelectedPreferences() {
        for (preference in selectedPreferences) {
            preferenceCategory?.removePreference(preference)
            val traceDataPair = Pair(preference.debugTraceData.url, preference.debugTraceData.traceId)
            ServerDebugTraceUtil.debugTraceData.remove(traceDataPair)
        }
        if (preferenceCategory?.preferenceCount == 0) {
            preferenceScreen.removePreference(preferenceCategory)
            preferenceCategory = null
        }
        selectedPreferences.clear()
    }
}
