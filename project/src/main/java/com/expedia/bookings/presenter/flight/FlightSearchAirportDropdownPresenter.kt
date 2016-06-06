package com.expedia.bookings.presenter.flight

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.ListPopupWindow
import android.text.Html
import android.util.AttributeSet
import android.widget.AdapterView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.server.CrossContextHelper
import com.expedia.bookings.utils.NavUtils
import com.expedia.bookings.utils.RecentAirports
import com.expedia.bookings.widget.FlightRouteAdapter
import com.mobiata.android.BackgroundDownloader
import com.mobiata.flightlib.data.Airport

class FlightSearchAirportDropdownPresenter(context: Context, attrs: AttributeSet): FlightSearchPresenter(context, attrs) {

    private val recentAirports = RecentAirports(context)

    private val originListAdapter by lazy {
        createFlightRouterAdapter(isOrigin = true)
    }

    private val destinationListAdapter by lazy {
        createFlightRouterAdapter(isOrigin = false)
    }

    private val originAirportListPopup by lazy {
        val listPopupWindow = createAirportListPopupWindow(originListAdapter)
        listPopupWindow.setOnItemClickListener(originAirportSelectedListener())
        listPopupWindow
    }

    private val destinationAirportListPopup by lazy {
        val listPopupWindow = createAirportListPopupWindow(destinationListAdapter)
        listPopupWindow.setOnItemClickListener(destinationAirportSelectedListener())
        listPopupWindow
    }

    private val progressDialog by lazy {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage(context.resources.getString(R.string.loading_air_asia_routes))
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog
    }

    private val failedFetchingRoutesAlertDialog by lazy {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(resources.getString(R.string.error_could_not_load_air_asia))
        builder.setNeutralButton(resources.getString(R.string.ok), { dialog, which -> NavUtils.goToLaunchScreen(context) })
        builder.create()
    }

    val mRoutesCallback = BackgroundDownloader.OnDownloadComplete<com.expedia.bookings.data.RoutesResponse> { results ->
        progressDialog.dismiss()
        if (results == null || results.hasErrors()) {
            failedFetchingRoutesAlertDialog.show()
        } else {
            onRoutesLoaded()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val chevronDrawable = ContextCompat.getDrawable(context, R.drawable.search_dropdown)
        originCardView.setEndDrawable(chevronDrawable)
        destinationCardView.setEndDrawable(chevronDrawable)

        if (Db.getFlightRoutes() != null) {
            onRoutesLoaded()
        } else {
            val bd = BackgroundDownloader.getInstance()
            if (!bd.isDownloading(CrossContextHelper.KEY_FLIGHT_ROUTES_DOWNLOAD)) {
                // Try to load the data one more time
                CrossContextHelper.updateFlightRoutesData(context, false)
            }
            progressDialog.show()

            // Attach a callback of our own
            bd.registerDownloadCallback(CrossContextHelper.KEY_FLIGHT_ROUTES_DOWNLOAD, mRoutesCallback)
        }
    }

    override fun showSuggestionState(selectOrigin: Boolean) {
        // do nothing. We're using airport drop downs
    }

    private fun createAirportListPopupWindow(flightRouteAdapter: FlightRouteAdapter): ListPopupWindow {
        val listPopupWindow = ListPopupWindow(context)
        listPopupWindow.anchorView = originCardView
        listPopupWindow.verticalOffset = -originCardView.height
        listPopupWindow.inputMethodMode = ListPopupWindow.INPUT_METHOD_NEEDED
        listPopupWindow.setAdapter(flightRouteAdapter)
        return listPopupWindow
    }

    private fun onRoutesLoaded() {
        originCardView.setOnClickListener { originAirportListPopup.show() }
        destinationCardView.setOnClickListener { destinationAirportListPopup.show() }
    }

    private fun createFlightRouterAdapter(isOrigin: Boolean): FlightRouteAdapter {
        return FlightRouteAdapter(context, Db.getFlightRoutes(), recentAirports.recentSearches,
                isOrigin, true, false, R.layout.material_flights_spinner_airport_dropdown_row)
    }

    private fun originAirportSelectedListener(): AdapterView.OnItemClickListener {

        return AdapterView.OnItemClickListener { parent, view, position, id ->
                val airport = originListAdapter.getAirport(position)
                if (airport != null) {
                    val suggestionV4FromAirport = getSuggestionV4FromAirport(airport)
                    searchViewModel.originLocationObserver.onNext(suggestionV4FromAirport)
                    originAirportListPopup.dismiss()

                    // dictates the available destinations based on selected origin
                    destinationListAdapter.setOrigin(airport.mAirportCode)
                    searchViewModel.clearDestinationLocation()

                    originSuggestionViewModel.suggestionSelectedSubject.onNext(suggestionV4FromAirport)

                    recentAirports.add(airport)
                }
            }
    }

    private fun destinationAirportSelectedListener(): AdapterView.OnItemClickListener {

        return AdapterView.OnItemClickListener { parent, view, position, id ->
                val airport = destinationListAdapter.getAirport(position)
                if (airport != null) {
                    searchViewModel.destinationLocationObserver.onNext(getSuggestionV4FromAirport(airport))
                    recentAirports.add(airport)
                    destinationAirportListPopup.dismiss()
                }
            }
    }

    private fun getSuggestionV4FromAirport(airport: Airport): SuggestionV4 {
        val airportSuggestion = SuggestionV4()
        airportSuggestion.regionNames = SuggestionV4.RegionNames()
        val airportName = "" + Html.fromHtml(context.getString(R.string.dropdown_airport_selection, airport.mAirportCode, airport.mName))
        airportSuggestion.regionNames.displayName = airportName
        airportSuggestion.regionNames.shortName = airportName
        airportSuggestion.hierarchyInfo = SuggestionV4.HierarchyInfo()
        airportSuggestion.hierarchyInfo!!.airport = SuggestionV4.Airport()
        airportSuggestion.hierarchyInfo!!.airport!!.airportCode = airport.mAirportCode
        airportSuggestion.hierarchyInfo?.isChild = false
        airportSuggestion.coordinates = SuggestionV4.LatLng()
        return airportSuggestion
    }
}
