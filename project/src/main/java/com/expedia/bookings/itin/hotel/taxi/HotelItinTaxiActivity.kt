package com.expedia.bookings.itin.hotel.taxi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.itin.scopes.HotelItinTaxiObserverScope
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.utils.Ui

class HotelItinTaxiActivity: AppCompatActivity() {

    companion object : Intentable {
        private const val ID_EXTRA = "ITINID"

        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinTaxiActivity::class.java)
            i.putExtra(ID_EXTRA, id)
            return i
        }
    }

    val lifecycleObserver: HotelItinTaxiActivityLifecycleObserver<HotelItinTaxiObserverScope> by lazy {
        val jsonUtil = Ui.getApplication(this).tripComponent().jsonUtilProvider()
        val itinId = intent.getStringExtra(ID_EXTRA)
        val scope =  HotelItinTaxiObserverScope(jsonUtil, itinId, this)
        HotelItinTaxiActivityLifecycleObserver(scope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.lifecycle.addObserver(lifecycleObserver)
        lifecycleObserver.invalidSubject.subscribe {
            finish()
        }
    }

    override fun finish() {
        super.finish()
       // overridePendingTransition(1, R.anim.slide)
    }
}