package com.expedia.bookings.presenter

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import javax.inject.Inject

class ActivityLifeCycleCallBacks: Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(p0: Activity?) {
    }

    override fun onActivityResumed(p0: Activity?) {
    }

    override fun onActivityStarted(p0: Activity?) {

    }

    override fun onActivityDestroyed(p0: Activity?) {

    }

    override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {

    }

    override fun onActivityStopped(p0: Activity?) {

    }

    private val LINE_OF_BUSINESS = "lineOfBusiness"


    fun getComponentType(lineOfBusiness: String): ComponentType {
        val componentType = when (lineOfBusiness) {
            LineOfBusiness.FLIGHTS.toString() -> ComponentType.FLIGHTS
            LineOfBusiness.FLIGHTS_V2.toString() -> ComponentType.FLIGHTS
            LineOfBusiness.HOTELS.toString() -> ComponentType.HOTELS
            LineOfBusiness.CARS.toString() -> ComponentType.CARS
            LineOfBusiness.LX.toString() -> ComponentType.LX
            LineOfBusiness.PACKAGES.toString() -> ComponentType.PACKAGES
            else -> throw UnsupportedOperationException()
        }
        return componentType
    }

    override fun onActivityCreated(activity: Activity?, intent: Bundle?) {
        if (activity is TravelersInjectable) {
            val lineOfBusiness = activity.intent.getStringExtra(LINE_OF_BUSINESS)
            Ui.getApplication(activity).getTravelerActivityComponent(getComponentType(lineOfBusiness)).inject(activity)
        }
    }

}

interface TravelersInjectable {

}

class TravelersActivity : AppCompatActivity(), TravelersInjectable {

    val numberOfTravelers by bindView<TextView>(R.id.number_of_travelers)
    val title by bindView<TextView>(R.id.title)

    lateinit var travelersViewModel: ITravelersViewModel
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travelers)

        travelersViewModel.travelers.observe(this, Observer {
            it?.let {
                numberOfTravelers.text = "Number of travelers: " + it.size
            }
        })

        title.text = travelersViewModel.travelersConfig.displayTitle
    }
}
