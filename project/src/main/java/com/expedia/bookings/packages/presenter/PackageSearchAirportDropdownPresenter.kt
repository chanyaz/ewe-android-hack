package com.expedia.bookings.packages.presenter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.AdapterView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.SearchSuggestion
import com.expedia.bookings.server.CrossContextHelper
import com.expedia.bookings.utils.AirportDropDownUtils
import com.expedia.bookings.utils.FlightsV2DataUtil
import com.expedia.bookings.utils.RecentAirports
import com.expedia.bookings.widget.FlightRouteAdapter
import com.mobiata.android.BackgroundDownloader

class PackageSearchAirportDropdownPresenter(context: Context, attrs: AttributeSet) : PackageSearchPresenter(context, attrs) {

    private val recentAirports = RecentAirports(context)

    private val originListAdapter by lazy {
        createFlightRouterAdapter(isOrigin = true)
    }

    private val destinationListAdapter by lazy {
        createFlightRouterAdapter(isOrigin = false)
    }

    private val originAirportListPopup by lazy {
        val listPopupWindow = AirportDropDownUtils.createAirportListPopupWindow(context, originCardView)
        listPopupWindow.setAdapter(originListAdapter)
        listPopupWindow.setOnItemClickListener(originAirportSelectedListener())
        listPopupWindow
    }

    private val destinationAirportListPopup by lazy {
        val listPopupWindow = AirportDropDownUtils.createAirportListPopupWindow(context, originCardView)
        listPopupWindow.setAdapter(destinationListAdapter)
        listPopupWindow.setOnItemClickListener(destinationAirportSelectedListener())
        listPopupWindow
    }

    private val progressDialog by lazy {
        AirportDropDownUtils.fetchingRoutesProgressDialog(context)
    }

    val mRoutesCallback = BackgroundDownloader.OnDownloadComplete<com.expedia.bookings.data.RoutesResponse> { results ->
        progressDialog.dismiss()
        if (results == null || results.hasErrors()) {
            AirportDropDownUtils.failedFetchingRoutesAlertDialog(context).show()
        } else {
            onRoutesLoaded()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        val chevronDrawable = ContextCompat.getDrawable(context, R.drawable.search_dropdown)
        originCardView.setEndDrawable(chevronDrawable)
        destinationCardView.setEndDrawable(chevronDrawable)

        if (Db.sharedInstance.flightRoutes != null) {
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

    private fun onRoutesLoaded() {
        originCardView.setOnClickListener { originAirportListPopup.show() }
        destinationCardView.setOnClickListener { destinationAirportListPopup.show() }
    }

    private fun createFlightRouterAdapter(isOrigin: Boolean): FlightRouteAdapter {
        return FlightRouteAdapter(context, Db.sharedInstance.flightRoutes, recentAirports.recentSearches,
                isOrigin, true, false, R.layout.material_flights_spinner_airport_dropdown_row)
    }

    private fun originAirportSelectedListener(): AdapterView.OnItemClickListener {

        return AdapterView.OnItemClickListener { _, _, position, _ ->
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

        return AdapterView.OnItemClickListener { _, _, position, _ ->
                val airport = destinationListAdapter.getAirport(position)
                if (airport != null) {
                    searchViewModel.destinationLocationObserver.onNext(FlightsV2DataUtil.getSuggestionV4FromAirport(context, airport))
                    recentAirports.add(airport)
                    destinationAirportListPopup.dismiss()
                }
            }
    }
}
