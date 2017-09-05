package com.expedia.bookings.launch.fragment

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.activity.ExpediaBookingApp
import com.expedia.bookings.data.HotelSearchParams
import com.expedia.bookings.launch.interfaces.IPhoneLaunchActivityLaunchFragment
import com.expedia.bookings.launch.widget.NewPhoneLaunchWidget
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.utils.navigation.HotelNavUtils
import com.expedia.util.havePermissionToAccessLocation
import com.expedia.util.requestLocationPermission
import com.mobiata.android.Log
import com.mobiata.android.util.NetUtils
import com.squareup.otto.Subscribe
import org.joda.time.LocalDate
import rx.Observer
import rx.Subscription

class NewPhoneLaunchFragment : Fragment(), IPhoneLaunchActivityLaunchFragment {

    val newPhoneLaunchWidget: NewPhoneLaunchWidget by bindView(R.id.new_phone_launch_widget)
    private var locSubscription: Subscription? = null
    private var wasOffline = false

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isResumed && newPhoneLaunchWidget != null) {
            newPhoneLaunchWidget.refreshState()
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is LaunchFragmentListener) {
            val listener: LaunchFragmentListener = context
            listener.onLaunchFragmentAttached(this)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.widget_new_phone_launch, null)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!havePermissionToAccessLocation(activity)) {
            requestLocationPermission(this)
        }
    }

    override fun onStart() {
        super.onStart()
        newPhoneLaunchWidget.initializeProWizard()
    }

    override fun onResume() {
        super.onResume()
        newPhoneLaunchWidget.refreshState()
        newPhoneLaunchWidget.toggleProWizardClickListener(enable = true)
        Events.register(this)
        Events.post(Events.PhoneLaunchOnResume())
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        activity.registerReceiver(broadcastReceiver, filter)
    }

    private fun onReactToUserActive() {
        val permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // show collection data to users as user denied location
            newPhoneLaunchWidget.locationNotAvailable.onNext(Unit)
        } else {
            findLocation()
        }
    }

    private fun findLocation() {
        locSubscription = CurrentLocationObservable.create(activity).subscribe(object : Observer<Location> {
            override fun onCompleted() {
                // ignore
            }

            override fun onError(e: Throwable) {
                newPhoneLaunchWidget.locationNotAvailable.onNext(Unit)
            }

            override fun onNext(currentLocation: Location) {
                newPhoneLaunchWidget.currentLocationSubject.onNext(currentLocation)
            }
        })
    }

    override fun onPause() {
        super.onPause()
        locSubscription?.unsubscribe()
        activity.unregisterReceiver(broadcastReceiver)
        Events.unregister(this)
        newPhoneLaunchWidget.toggleProWizardClickListener(enable = false)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("Detected connectivity change, checking connection...")
            // If we changed state, react
            checkConnection()
        }
    }

    private fun checkConnection() {
        val context = activity
        if (context != null && !NetUtils.isOnline(context) && !ExpediaBookingApp.isAutomation()) {
            wasOffline = true
            newPhoneLaunchWidget.hasInternetConnection.onNext(false)
        } else {
            wasOffline = false
            newPhoneLaunchWidget.hasInternetConnection.onNext(true)
            onReactToUserActive()
        }
    }


    override fun onBackPressed(): Boolean {
        return newPhoneLaunchWidget.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.PERMISSION_REQUEST_LOCATION -> {
                newPhoneLaunchWidget.hasInternetConnection.onNext(true)
                onReactToUserActive()
                return
            }
        }
    }

    // Hotel search in collection location
    @Subscribe
    fun onCollectionLocationSelected(event: Events.LaunchCollectionItemSelected) {
        val location = event.collectionLocation.location
        val params = HotelSearchParams()
        params.query = location.shortName
        params.searchType = HotelSearchParams.SearchType.valueOf(location.type)
        params.regionId = location.id
        params.setSearchLatLon(location.latLong.lat, location.latLong.lng)
        val now = LocalDate.now()
        params.checkInDate = now.plusDays(1)
        params.checkOutDate = now.plusDays(2)
        params.numAdults = 2
        params.children = null
        HotelNavUtils.goToHotels(activity, params, event.animOptions, 0)
    }

    @Subscribe
    @Suppress("UNUSED_PARAMETER")
    fun onPOSChanged(event: Events.PhoneLaunchOnPOSChange) {
        newPhoneLaunchWidget.posChangeSubject.onNext(Unit)
    }

    fun smoothScrollToTop() {
        if (newPhoneLaunchWidget.darkView.alpha == 0f) {
            newPhoneLaunchWidget.launchListWidget.smoothScrollToPosition(0)
        }
    }

    interface LaunchFragmentListener {
        fun onLaunchFragmentAttached(frag: NewPhoneLaunchFragment)
    }

}