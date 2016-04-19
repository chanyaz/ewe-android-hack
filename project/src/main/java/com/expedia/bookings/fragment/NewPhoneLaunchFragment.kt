package com.expedia.bookings.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.bookings.interfaces.IPhoneLaunchActivityLaunchFragment
import com.expedia.bookings.location.CurrentLocationObservable
import com.expedia.bookings.otto.Events
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.NewPhoneLaunchWidget
import com.expedia.util.havePermissionToAccessLocation
import com.expedia.util.requestLocationPermission
import rx.Observer
import rx.Subscription

class NewPhoneLaunchFragment : Fragment(), IPhoneLaunchActivityLaunchFragment {

    val newPhoneLaunchWidget: NewPhoneLaunchWidget by bindView(R.id.new_phone_launch_widget)
    private var locSubscription: Subscription? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.widget_new_phone_launch, null)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!havePermissionToAccessLocation(activity)) {
            requestLocationPermission(this)
        } else {
            newPhoneLaunchWidget.showLoadingAnimationOnList()
            onReactToUserActive()
        }

    }

    override fun onResume() {
        super.onResume()
        Events.register(this)
        // TODO below event is gonna tell user is in launch screen and refresh the hotel list if user is cming after ceratin time
        Events.post(Events.PhoneLaunchOnResume())
    }

    private fun onReactToUserActive() {
        val permissionCheck = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION)

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            // show collection data to users as user denied location
            Events.post(Events.LaunchLocationFetchError())
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
                Events.post(Events.LaunchLocationFetchError())
            }

            override fun onNext(currentLocation: Location) {
                Events.post(Events.LaunchLocationFetchComplete(currentLocation))
            }
        })
    }

    override fun onPause() {
        super.onPause()
        locSubscription?.unsubscribe()
        Events.unregister(this)
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Constants.PERMISSION_REQUEST_LOCATION -> {
                newPhoneLaunchWidget.showLoadingAnimationOnList()
                onReactToUserActive()
                return
            }
        }
    }


}