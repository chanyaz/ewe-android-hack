package com.expedia.bookings.itin.services


import android.content.Context
import android.text.TextUtils
import com.expedia.bookings.BuildConfig
import com.expedia.bookings.data.Courier
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.TNSUser
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.data.user.UserSource
import com.expedia.bookings.notification.GCMRegistrationKeeper
import com.expedia.bookings.services.TNSServices
import com.expedia.bookings.utils.UniqueIdentifierHelper

import rx.subjects.PublishSubject

class FlightRegistrationHandler(val context: Context,
                                val tnsServices: TNSServices,
                                val userDetail: UserSource) {

    val userLoginStateChanged = PublishSubject.create<Boolean>()


    fun setup() {
        getCourier()?.let {
            tnsServices.registerForUserDevice(getUser(), it)
        }

        userLoginStateChanged.subscribe { state ->
            getCourier()?.let {
                if (state) {
                    tnsServices.registerForUserDevice(getUser(), it)
                } else {
                    tnsServices.deregisterDevice(it)
                }
            }
        }
    }

    private fun getUser(): TNSUser {
        val siteId: Int = PointOfSale.getPointOfSale().siteId
        val guid = Db.sharedInstance.abacusGuid
        return TNSUser(siteId.toString(), userDetail.tuid.toString(), userDetail.expUserId.toString(), guid)
    }

    private fun getCourier(): Courier? {
        val regId = GCMRegistrationKeeper.getInstance(context).getRegistrationId(context)
        val langId = PointOfSale.getPointOfSale().dualLanguageId
        if (!TextUtils.isEmpty(regId)) {
            return Courier("gcm", langId.toString(), BuildConfig.APPLICATION_ID, regId, UniqueIdentifierHelper.getID(context))
        }
        return null
    }
}
