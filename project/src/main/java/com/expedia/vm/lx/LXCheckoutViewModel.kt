package com.expedia.vm.lx

import android.content.Context
import com.expedia.bookings.data.Money
import com.expedia.bookings.utils.Ui
import com.expedia.vm.AbstractCheckoutViewModel

class LXCheckoutViewModel(context: Context) : AbstractCheckoutViewModel(context) {

    override fun injectComponents() {
        Ui.getApplication(context).lxComponent().inject(this)
    }

    override fun getTripId(): String {
        //TODO
        return ""
    }

}