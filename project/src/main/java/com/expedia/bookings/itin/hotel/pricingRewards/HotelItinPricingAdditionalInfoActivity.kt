package com.expedia.bookings.itin.hotel.pricingRewards

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.expedia.bookings.R
import com.expedia.bookings.data.trips.ItineraryManager
import com.expedia.bookings.extensions.LiveDataObserver
import com.expedia.bookings.itin.common.ItinPricingAdditionalInfoView
import com.expedia.bookings.itin.hotel.repositories.ItinHotelRepo
import com.expedia.bookings.itin.scopes.HotelItinPricingAdditionalInfoScope
import com.expedia.bookings.itin.tripstore.utils.IJsonToItinUtil
import com.expedia.bookings.itin.utils.Intentable
import com.expedia.bookings.itin.utils.StringSource
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

class HotelItinPricingAdditionalInfoActivity : AppCompatActivity() {

    companion object : Intentable {
        private const val ID_EXTRA = "ITINID"

        override fun createIntent(context: Context, id: String): Intent {
            val i = Intent(context, HotelItinPricingAdditionalInfoActivity::class.java)
            i.putExtra(ID_EXTRA, id)
            return i
        }
    }

    val additionalInfoView by bindView<ItinPricingAdditionalInfoView>(R.id.itin_addtional_info_view)
    lateinit var jsonUtil: IJsonToItinUtil
    lateinit var hotelRepo: ItinHotelRepo
    lateinit var stringProvider: StringSource
    val itineraryManager: ItineraryManager = ItineraryManager.getInstance()
    val invalidDataObserver = LiveDataObserver<Unit> {
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_itin_additional_info)
        Ui.getApplication(this).defaultTripComponents()

        stringProvider = Ui.getApplication(this).appComponent().stringProvider()
        jsonUtil = Ui.getApplication(this).tripComponent().jsonUtilProvider()
        hotelRepo = ItinHotelRepo(intent.getStringExtra(ID_EXTRA), jsonUtil, itineraryManager.syncFinishObservable)

        val pricingAdditionalInfoScope = HotelItinPricingAdditionalInfoScope(hotelRepo, stringProvider, this)
        val pricingAdditionInfoViewModel = HotelItinPricingAdditionalInfoViewModel(pricingAdditionalInfoScope)
        additionalInfoView.viewModel = pricingAdditionInfoViewModel

        additionalInfoView.toolbarViewModel.navigationBackPressedSubject.subscribe {
            finish()
        }
        hotelRepo.liveDataInvalidItin.observe(this, invalidDataObserver)
    }

    override fun finish() {
        super.finish()
        hotelRepo.dispose()
        overridePendingTransition(R.anim.slide_in_left_complete, R.anim.slide_out_right_no_fill_after)
    }
}
