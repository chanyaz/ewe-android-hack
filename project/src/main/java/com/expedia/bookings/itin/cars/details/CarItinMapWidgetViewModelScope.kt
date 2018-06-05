package com.expedia.bookings.itin.cars.details

import android.arch.lifecycle.LifecycleOwner
import com.expedia.bookings.itin.cars.ItinCarRepoInterface
import com.expedia.bookings.itin.scopes.HasCarRepo
import com.expedia.bookings.itin.scopes.HasLifecycleOwner
import com.expedia.bookings.itin.scopes.HasPhoneHandler
import com.expedia.bookings.itin.scopes.HasStringProvider
import com.expedia.bookings.itin.scopes.HasToaster
import com.expedia.bookings.itin.scopes.HasTripsTracking
import com.expedia.bookings.itin.utils.IPhoneHandler
import com.expedia.bookings.itin.utils.IToaster
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.tracking.ITripsTracking

data class CarItinMapWidgetViewModelScope(override val strings: StringSource,
                                          override val tripsTracking: ITripsTracking,
                                          override val lifecycleOwner: LifecycleOwner,
                                          override val itinCarRepo: ItinCarRepoInterface,
                                          override val toaster: IToaster,
                                          override val phoneHandler: IPhoneHandler) : HasCarRepo, HasLifecycleOwner, HasTripsTracking, HasToaster, HasStringProvider, HasPhoneHandler
