package com.expedia.bookings.activity

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.expedia.bookings.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlacePicker
import java.util.Date


/**
 * Created by nbirla on 03/05/18.
 */
class MockLocationActivity: Activity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    protected var mGoogleApiClient: GoogleApiClient? = null
    val PLACE_PICKER_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mock_location)

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient()
        if (!mGoogleApiClient!!.isConnecting() || !mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.connect()
        }
    }

    fun mapButtonHandler(v: View){

        val location = Location("network")
        location.setLatitude(findViewById<EditText>(R.id.lat).text.toString().toDouble())
        location.setLongitude(findViewById<EditText>(R.id.lng).text.toString().toDouble())
        location.setTime(Date().getTime())
        location.setAccuracy(3.0f)
        location.setElapsedRealtimeNanos(System.nanoTime())

        LocationServices.FusedLocationApi.setMockMode(mGoogleApiClient, true)

        LocationServices.FusedLocationApi.setMockLocation(mGoogleApiClient, location)
    }

    @Synchronized protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onConnected(connectionHint: Bundle?) {

    }

    override fun onConnectionFailed(result: ConnectionResult) {
        // Do something with result.getErrorCode());
    }

    override fun onConnectionSuspended(cause: Int) {
        mGoogleApiClient!!.connect()
    }

}
