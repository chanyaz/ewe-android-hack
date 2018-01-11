package com.expedia.bookings.meso.vm

import android.content.Context
import com.expedia.bookings.R
import com.expedia.bookings.meso.MesoAdResponseProvider
import com.expedia.bookings.meso.model.MesoAdResponse
import com.expedia.bookings.meso.model.MesoDestinationAdResponse
import com.expedia.util.Optional
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class MesoDestinationViewModel(private val context: Context) {

    var mesoDestinationAdResponse: MesoDestinationAdResponse? = null

    val title: String? by lazy { mesoDestinationAdResponse?.title }
    val description: String? by lazy { mesoDestinationAdResponse?.subtitle }
    val sponsoredText: String? by lazy { mesoDestinationAdResponse?.sponsoredText }
    val webviewUrl: String? by lazy { mesoDestinationAdResponse?.webviewUrl }
    val backgroundUrl: String? by lazy { mesoDestinationAdResponse?.backgroundUrl }
    val backgroundFallback: Int = R.color.gray600
    val backgroundPlaceHolder: Int = R.drawable.results_list_placeholder

    fun fetchDestinationMesoAd(adObserver: Observer<Optional<MesoDestinationAdResponse>>) {
        MesoAdResponseProvider.fetchDestinationMesoAd(context, getMesoDestinationSubject(adObserver))
    }

    private fun getMesoDestinationSubject(adObserver: Observer<Optional<MesoDestinationAdResponse>>): PublishSubject<MesoAdResponse> {
        val adSubject: PublishSubject<MesoAdResponse> = PublishSubject.create<MesoAdResponse>()

        adSubject.subscribe(object : Observer<MesoAdResponse> {
            override fun onSubscribe(d: Disposable) {
                adObserver.onSubscribe(d)
            }

            override fun onNext(mesoAdResponse: MesoAdResponse) {
                mesoDestinationAdResponse = mesoAdResponse.DestinationAdResponse
                adObserver.onNext(Optional(mesoDestinationAdResponse))
            }

            override fun onComplete() {
                adObserver.onComplete()
            }

            override fun onError(e: Throwable) {
                adObserver.onError(e)
            }
        })

        return adSubject
    }
}
