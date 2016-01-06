package com.expedia.bookings.presenter.packages

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.hotels.Hotel
import com.expedia.bookings.data.packages.PackageOffersResponse
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.util.endlessObserver
import com.expedia.vm.PackageHotelResultsViewModel
import rx.Observer
import javax.inject.Inject

public class PackageHotelPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    lateinit var packageServices: PackageServices
        @Inject set

    val resultsPresenter: PackageHotelResultsPresenter by bindView(R.id.hotel_results_presenter)

    init {
        Ui.getApplication(context).packageComponent().inject(this)
        View.inflate(getContext(), R.layout.package_hotel_presenter, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        resultsPresenter.viewmodel = PackageHotelResultsViewModel(context)
        resultsPresenter.viewmodel.paramsSubject.onNext(Db.getPackageParams())
        resultsPresenter.viewmodel.resultsSubject.onNext(Db.getPackageResponse())
        resultsPresenter.hotelSelectedSubject.subscribe(hotelSelectedObserver)
    }

    val hotelSelectedObserver: Observer<Hotel> = endlessObserver { hotel ->
        getDetails(hotel.packageOfferModel.piid, Db.getPackageParams().checkIn.toString(), Db.getPackageParams().checkOut.toString())
    }

    private fun getDetails(piid: String, checkIn: String, checkOut: String) {
        packageServices.hotelOffer(piid, checkIn, checkOut, getOfferObserver())
    }

    fun getOfferObserver() :  Observer<PackageOffersResponse> {
        return object : Observer<PackageOffersResponse> {
            override fun onNext(response: PackageOffersResponse) {
                println("offers success, Hotels:" + response.packageHotelOffers.size);
            }

            override fun onCompleted() {
                println("offers completed")
            }

            override fun onError(e: Throwable?) {
                println("offers error: " + e?.message)
            }
        }
    }

}
