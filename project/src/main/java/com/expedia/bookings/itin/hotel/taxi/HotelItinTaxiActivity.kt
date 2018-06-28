package com.expedia.bookings.itin.hotel.taxi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.itin.common.ItinRepo
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.scopes.HotelItinTaxiViewModelScope
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.util.notNullAndObservable

open class HotelItinTaxiActivity : AppCompatActivity() {

    companion object : Intentable {
        private const val ID_EXTRA = "ITINID"

        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinTaxiActivity::class.java)
            i.putExtra(ID_EXTRA, id)
            return i
        }
    }

    val navigationButton by bindView<ImageView>(R.id.close_image_button)

    val localizedLocationNameTextView by bindView<TextView>(R.id.localized_location_name)
    val nonLocalizedLocationNameTextView by bindView<TextView>(R.id.non_localized_location_name)

    val localizedAddressTextView by bindView<TextView>(R.id.localized_location_address)
    val nonLocalizedAddressTextView by bindView<TextView>(R.id.non_localized_location_address)

    open val repo: ItinRepoInterface by lazy {
        val jsonUtil = Ui.getApplication(this).appComponent().jsonUtilProvider()
        ItinRepo(intent.getStringExtra(ID_EXTRA), jsonUtil, ItineraryManager.getInstance().syncFinishObservable)
    }

    var viewModel: HotelItinTaxiViewModel<HotelItinTaxiViewModelScope> by notNullAndObservable { vm ->
        vm.localizedAddressSubject.subscribeTextAndVisibility(localizedAddressTextView)
        vm.localizedLocationNameSubject.subscribeTextAndVisibility(localizedLocationNameTextView)
        vm.nonLocalizedAddressSubject.subscribeTextAndVisibility(nonLocalizedAddressTextView)
        vm.nonLocalizedLocationNameSubject.subscribeTextAndVisibility(nonLocalizedLocationNameTextView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itin_taxi)
        val scope = HotelItinTaxiViewModelScope(repo, this)
        viewModel = HotelItinTaxiViewModel(scope)
        repo.invalidDataSubject.subscribe {
            finish()
        }
        navigationButton.setOnClickListener {
            finish()
        }
    }
}
