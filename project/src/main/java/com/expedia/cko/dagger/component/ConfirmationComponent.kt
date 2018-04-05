package com.expedia.cko.dagger.component

import com.expedia.cko.activity.ConfirmationActivity
import com.expedia.cko.widget.HeaderWidget
import com.expedia.cko.widget.ItineraryWidget

interface ConfirmationComponent {
    fun inject(confirmationActivity: ConfirmationActivity)
    fun inject(headerWidget: HeaderWidget)
    fun inject(itineraryWidget: ItineraryWidget)
}
