package com.expedia.bookings.activity

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TNSRegisterDeviceResponse
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.itin.tripstore.utils.JsonToItinUtil
import com.expedia.bookings.itin.tripstore.utils.TripsJsonFileUtils
import com.expedia.bookings.itin.utils.NotificationScheduler
import com.expedia.bookings.notification.GCMRegistrationKeeper
import com.expedia.bookings.utils.FileReadUtils
import com.expedia.bookings.utils.Ui
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

class TNSTestAdapter(val context: Context, val loadingIndicatorSubject: PublishSubject<Pair<Boolean, String>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val textView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.text_label, parent, false) as TextView
        textView.textSize = 25f
        textView.setPadding(10, 10, 10, 10)
        return TNSViewHolder(textView, constructNotificationScheduler(), loadingIndicatorSubject)
    }

    override fun getItemCount(): Int {
        return TNSServices.values().size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as TNSViewHolder).textView.text = TNSServices.values()[position].toString()
    }

    private fun constructTNSService(): com.expedia.bookings.services.TNSServices {
        val endPoint = Ui.getApplication(context).appComponent().endpointProvider()
        val okHTTPClient = Ui.getApplication(context).appComponent().okHttpClient()
        val interceptor = Ui.getApplication(context).appComponent().userAgentInterceptor()
        return com.expedia.bookings.services.TNSServices(endPoint.tnsEndpoint, okHTTPClient, listOf(interceptor), AndroidSchedulers.mainThread(), Schedulers.io())
    }

    private fun constructNotificationScheduler(): NotificationScheduler {
        val userStateManager = Ui.getApplication(context).appComponent().userStateManager()
        val tripsJsonFileUtils = TripsJsonFileUtils(context.getDir("TRIPS_JSON_STORE", Context.MODE_PRIVATE))
        addDataToFileDirectory(tripsJsonFileUtils)
        return NotificationScheduler(context, Db.sharedInstance, com.expedia.bookings.notification.NotificationManager(context), userStateManager, constructTNSService(), GCMRegistrationKeeper.getInstance(context), PointOfSale.getPointOfSale(), JsonToItinUtil(tripsJsonFileUtils))
    }

    private fun addDataToFileDirectory(tripsJsonFileUtils: TripsJsonFileUtils) {
        val flightJsonString = FileReadUtils.getJsonStringFromFile(context, "api/trips/flight_trip_details.json")
        tripsJsonFileUtils.writeTripToFile("api/trips/activity_trip_details.json", updateFlightJsonDates(flightJsonString))
    }

    private fun updateFlightJsonDates(jsonString: String): String {
        val jsonObject = JsonParser().parse(jsonString) as JsonObject
        val segmentObject = (jsonObject.getAsJsonObject("responseData").getAsJsonArray("flights")[0].asJsonObject).getAsJsonArray("legs")[0].asJsonObject.getAsJsonArray("segments")[0].asJsonObject
        val departureTimeObject = segmentObject.getAsJsonObject("departureTime")
        val arrivalTimeObject = segmentObject.getAsJsonObject("arrivalTime")
        val departureDate = org.joda.time.DateTime.now().toString()
        departureTimeObject.remove("raw")
        arrivalTimeObject.remove("raw")
        departureTimeObject.addProperty("raw", departureDate)
        arrivalTimeObject.addProperty("raw", departureDate)
        return jsonObject.toString()
    }
}

class TNSViewHolder(val textView: TextView, notificationScheduler: NotificationScheduler, val loadingIndicatorSubject: PublishSubject<Pair<Boolean, String>>) : RecyclerView.ViewHolder(textView), View.OnClickListener {
    var viewModel: TNSViewHolderViewModel = TNSViewHolderViewModel(notificationScheduler, loadingIndicatorSubject)

    init {
        itemView.setOnClickListener(this)
    }

    class TNSViewHolderViewModel(val notificationScheduler: NotificationScheduler, val loadingIndicatorSubject: PublishSubject<Pair<Boolean, String>>) {
        val flightRegistrationSubject = PublishSubject.create<Unit>()
        val flightStatsCallSuccess = PublishSubject.create<Unit>()
        val flightRegistrationObserver = object : Observer<TNSRegisterDeviceResponse> {
            override fun onSubscribe(d: Disposable) = Unit

            override fun onComplete() = Unit

            override fun onError(e: Throwable) = Unit

            override fun onNext(tnsRegisterDeviceResponse: TNSRegisterDeviceResponse) {
                flightRegistrationSubject.onNext(Unit)
            }
        }

        val flightStatsObserver = object : Observer<TNSRegisterDeviceResponse> {
            override fun onSubscribe(d: Disposable) = Unit

            override fun onComplete() = Unit

            override fun onError(e: Throwable) = Unit

            override fun onNext(tnsRegisterDeviceResponse: TNSRegisterDeviceResponse) {
                flightStatsCallSuccess.onNext(Unit)
            }
        }

        init {
            flightRegistrationSubject.subscribe {
                loadingIndicatorSubject.onNext(Pair(true, "Performing Flight stats callback"))
                notificationScheduler.testFlightStatsCallback(FileReadUtils.getJsonStringFromFile(notificationScheduler.context, "api/trips/tns_notification_payload.json"), flightStatsObserver)
            }
            flightStatsCallSuccess.subscribe {
                loadingIndicatorSubject.onNext(Pair(false, ""))
                notificationScheduler.testFlightStatsCallback(FileReadUtils.getJsonStringFromFile(notificationScheduler.context, "api/trips/tns_notification_payload.json"), flightStatsObserver)
            }
        }

        fun registerForFlight() {
            notificationScheduler.registerForPushNotifications(flightRegistrationObserver)
        }
    }

    override fun onClick(v: View?) {
        loadingIndicatorSubject.onNext(Pair(true, "Performing flight Registration"))
        viewModel.registerForFlight()
    }
}

enum class TNSServices {
    FlightCancelled
}
