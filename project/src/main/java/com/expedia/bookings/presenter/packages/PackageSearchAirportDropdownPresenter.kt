package com.expedia.bookings.presenter.packages

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.ListPopupWindow
import android.util.AttributeSet
import android.widget.AdapterView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.server.CrossContextHelper
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.navigation.NavUtils
import com.expedia.bookings.utils.RecentAirports
import com.expedia.bookings.widget.FlightRouteAdapter
import com.mobiata.android.BackgroundDownloader

class PackageSearchAirportDropdownPresenter(context: Context, attrs: AttributeSet): PackageSearchPresenter(context, attrs) {

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
//        swapFlightsLocationsButton.visibility = View.GONE
//        val dividerParams = flightsSearchDivider.layoutParams as ViewGroup.MarginLayoutParams
//        val paddingRight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, resources.displayMetrics).toInt()
//        val paddingLeft = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, resources.displayMetrics).toInt()
//
//        dividerParams.setMargins(paddingLeft, 0, paddingRight, 0)
//        flightsSearchDivider.layoutParams = dividerParams

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

    override fun back(): Boolean {
        if (destinationAirportListPopup.isShowing || originAirportListPopup.isShowing) {
            destinationAirportListPopup.dismiss()
            originAirportListPopup.dismiss()
            return true
        }
        return super.back()
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
                    val suggestionV4FromAirport = FlightsV2DataUtil.getSuggestionV4FromAirport(context, airport)
                    searchViewModel.originLocationObserver.onNext(suggestionV4FromAirport)
                    originAirportListPopup.dismiss()

                    // dictates the available destinations based on selected origin
                    destinationListAdapter.setOrigin(airport.mAirportCode)
                    searchViewModel.clearDestinationLocation()

                    val searchSuggestion = SearchSuggestion(suggestionV4FromAirport)
                    originSuggestionViewModel.suggestionSelectedSubject.onNext(searchSuggestion)

                    recentAirports.add(airport)
                }
            }
    }

    private fun destinationAirportSelectedListener(): AdapterView.OnItemClickListener {

        return AdapterView.OnItemClickListener { parent, view, position, id ->
                val airport = destinationListAdapter.getAirport(position)
                if (airport != null) {
                    searchViewModel.destinationLocationObserver.onNext(FlightsV2DataUtil.getSuggestionV4FromAirport(context, airport))
                    recentAirports.add(airport)
                    destinationAirportListPopup.dismiss()
                }
            }
    }

}
