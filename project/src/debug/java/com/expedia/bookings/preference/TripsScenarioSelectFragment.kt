package com.expedia.bookings.preference

import android.app.AlertDialog
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
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.TripFolder
import com.expedia.bookings.itin.tripstore.utils.ITripsJsonFileUtils
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.google.gson.Gson
import io.reactivex.subjects.PublishSubject
import java.io.InputStreamReader

class TripsScenarioSelectFragment : Fragment() {
    private val recyclerView: RecyclerView by bindView(R.id.trip_scenarios_select_recycler_view)
    private val scenarios: MutableList<TripMockScenarios.Scenarios> = mutableListOf()
    lateinit var fileUtils: ITripsJsonFileUtils
    val viewModel: TripScenariosViewModel by lazy {
        TripScenariosViewModel()
    }

    override fun onStart() {
        super.onStart()
        activity?.title = "Select mock trip scenario"
        TripMockScenarios.Scenarios.values().forEach {
            scenarios.add(it)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.debug_trip_scenarios_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Ui.getApplication(context).defaultTripComponents()
        fileUtils = Ui.getApplication(context).tripComponent().tripFolderJsonFileUtils()

        viewModel.closeFragmentSubject.subscribe {
            viewModel.restartAppSubject.onNext(Unit)
            activity?.onBackPressed()
        }
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = TripsScenariosAdapter(context, scenarios, viewModel, fileUtils)
        }
    }
}

class TripsScenariosAdapter(private val context: Context, private val scenarios: List<TripMockScenarios.Scenarios>, private val viewModel: TripScenariosViewModel, private val fileUtils: ITripsJsonFileUtils) : RecyclerView.Adapter<TripsScenariosAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val textView = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_selectable_list_item, parent, false) as CheckedTextView
        return ViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return scenarios.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val scenario = scenarios[position]
        holder.textView.text = scenario.name
        holder.itemView.setOnClickListener {

            fileUtils.deleteAllFiles()

            val gson = Gson()
            val arrayOfFolders = gson.fromJson(InputStreamReader(context.assets.open("api/trips/tripfolders/${scenario.filename}.json")), Array<TripFolder>::class.java).toList()
            arrayOfFolders.forEach { folder ->
                fileUtils.writeToFile(folder.tripFolderId, gson.toJson(folder))
            }

            showAlertDialogForAppRestart(position)
        }
    }

    private fun showAlertDialogForAppRestart(position: Int) {
        val builder = AlertDialog.Builder(context, R.style.AccountDialogTheme)
        builder.setTitle("Mock trip scenario set")
        builder.setMessage(scenarios[position].name + "\n\nApp needs to restart.")
        builder.setPositiveButton(R.string.ok) { _, _ ->
            viewModel.closeFragmentSubject.onNext(Unit)
        }
        builder.create().show()
    }

    class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)
}

class TripScenariosViewModel {
    val closeFragmentSubject: PublishSubject<Unit> = PublishSubject.create()
    val restartAppSubject: PublishSubject<Unit> = PublishSubject.create()
}
