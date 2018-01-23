package com.expedia.bookings.dagger

import android.content.Context
import com.expedia.bookings.utils.Ui
import java.lang.ref.WeakReference

class HotelComponentInjector() {
    private var hotelComponentReference: WeakReference<HotelComponent>? = null

    fun inject(context: Context) {
        val application = Ui.getApplication(context)
        application.defaultHotelComponents()
        application.defaultTravelerComponent()
        hotelComponentReference = WeakReference(application.hotelComponent())
    }

    fun clear(context: Context) {
        hotelComponentReference?.get()?.let { hotelComponent ->
            val application = Ui.getApplication(context)
            if (hotelComponent == application.hotelComponent()) {
                application.setHotelComponent(null)
            }
        }
    }
}
