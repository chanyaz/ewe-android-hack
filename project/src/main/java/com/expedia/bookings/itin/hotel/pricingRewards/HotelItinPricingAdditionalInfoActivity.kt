package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.itin.common.ItinPricingAdditionalInfoView
import com.expedia.bookings.itin.common.ItinRepo
import com.expedia.bookings.itin.common.ItinRepoInterface
import com.expedia.bookings.itin.scopes.HotelItinPricingAdditionalInfoScope
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import javax.inject.Inject

class HotelItinPricingAdditionalInfoActivity : AppCompatActivity() {

    companion object : Intentable {
        private const val ID_EXTRA = "ITINID"

        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinPricingAdditionalInfoActivity::class.java)
            i.putExtra(ID_EXTRA, id)
            return i
        }
    }

    lateinit var jsonUtil: IJsonToItinUtil
        @Inject set
    lateinit var stringProvider: StringSource
        @Inject set

    val additionalInfoView by bindView<ItinPricingAdditionalInfoView>(R.id.itin_addtional_info_view)
    lateinit var repo: ItinRepoInterface
    val itineraryManager: ItineraryManager = ItineraryManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itin_additional_info)
        Ui.getApplication(this).defaultTripComponents()
        Ui.getApplication(this).tripComponent().inject(this)

        repo = ItinRepo(intent.getStringExtra(ID_EXTRA), jsonUtil, itineraryManager.syncFinishObservable)

        val pricingAdditionalInfoScope = HotelItinPricingAdditionalInfoScope(repo, stringProvider, this)
        val pricingAdditionInfoViewModel = HotelItinPricingAdditionalInfoViewModel(pricingAdditionalInfoScope)
        additionalInfoView.viewModel = pricingAdditionInfoViewModel

        additionalInfoView.toolbarViewModel.navigationBackPressedSubject.subscribe {
            finish()
        }
        repo.invalidDataSubject.subscribe {
            finish()
        }
    }

    override fun finish() {
        super.finish()
        repo.dispose()
    }
}
