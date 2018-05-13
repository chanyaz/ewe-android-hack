package com.expedia.bookings.marketing.carnival.model

import android.os.Parcelable
import com.carnival.sdk.Message
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import java.util.HashMap

@Parcelize
class CarnivalMessage(val imageURL: String?, var title: String?, var attributes: HashMap<String, String>?, var text: String?) : Parcelable {

    @IgnoredOnParcel
    var messageData: Message? = null
}
