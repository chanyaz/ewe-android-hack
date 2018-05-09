package com.expedia.bookings.packages.util

import android.content.Context
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.multiitem.BundleSearchResponse
import com.expedia.bookings.data.multiitem.MultiItemApiSearchResponse
import com.expedia.bookings.data.multiitem.PackageErrorDetails
import com.expedia.bookings.data.packages.PackageApiError
import com.expedia.bookings.data.packages.PackageSearchParams
import com.expedia.bookings.extensions.subscribeObserver
import com.expedia.bookings.services.PackageProductSearchType
import com.expedia.bookings.services.PackageServices
import com.expedia.bookings.tracking.ApiCallFailing
import com.expedia.bookings.utils.PackageResponseUtils
import com.expedia.bookings.utils.RetrofitUtils
import com.google.gson.Gson
import com.mobiata.android.Log
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject
import retrofit2.HttpException

class PackageServicesManager(private val context: Context, private val packageServices: PackageServices) {

    private fun getApiCallFailingDetails(type: PackageProductSearchType, isChangeSearch: Boolean, errorDetails: PackageErrorDetails.PackageAPIErrorDetails): Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing> {
        val apiCallFailingDetails = when (type) {
            PackageProductSearchType.MultiItemHotels -> if (isChangeSearch) ApiCallFailing.PackageHotelChange(errorDetails.errorKey) else ApiCallFailing.PackageHotelSearch(errorDetails.errorKey)
            PackageProductSearchType.MultiItemOutboundFlights -> if (isChangeSearch) ApiCallFailing.PackageFlightOutboundChange(errorDetails.errorKey) else ApiCallFailing.PackageFlightOutbound(errorDetails.errorKey)
            PackageProductSearchType.MultiItemInboundFlights -> if (isChangeSearch) ApiCallFailing.PackageFlightInboundChange(errorDetails.errorKey) else ApiCallFailing.PackageFlightInbound(errorDetails.errorKey)
        }
        return Triple(type, errorDetails.errorCode, apiCallFailingDetails)
    }

    fun doPackageSearch(params: PackageSearchParams,
                        type: PackageProductSearchType,
                        successHandler: PublishSubject<Pair<PackageProductSearchType, BundleSearchResponse>>,
                        errorHandler: PublishSubject<Triple<PackageProductSearchType, PackageApiError.Code, ApiCallFailing>>,
                        saveSuccess: ((Unit) -> Unit)? = null): Disposable {

        val isChangeSearch = params.isChangePackageSearch()
        return packageServices.packageSearch(params, type)
                .subscribeObserver(object : DisposableObserver<BundleSearchResponse>() {
                    override fun onNext(response: BundleSearchResponse) {
                        if (response.getHotels().isEmpty()) {
                            val errorCode = PackageApiError.Code.search_response_null
                            errorHandler.onNext(getApiCallFailingDetails(type, isChangeSearch, PackageErrorDetails.PackageAPIErrorDetails(errorCode.name, errorCode)))
                        } else {
                            Db.setPackageResponse(response)
                            if (type == PackageProductSearchType.MultiItemHotels) {
                                val currentFlights = arrayOf(response.getFlightLegs()[0].legId, response.getFlightLegs()[1].legId)
                                Db.sharedInstance.packageParams.currentFlights = currentFlights
                                Db.sharedInstance.packageParams.defaultFlights = currentFlights.copyOf()
                                PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_HOTELS_FILE)
                            } else {
                                if (type == PackageProductSearchType.MultiItemOutboundFlights) {
                                    PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_OUTBOUND_FLIGHT_FILE, saveSuccess)
                                } else {
                                    PackageResponseUtils.savePackageResponse(context, response, PackageResponseUtils.RECENT_PACKAGE_INBOUND_FLIGHT_FILE, saveSuccess)
                                }
                            }
                            successHandler.onNext(Pair(type, response))
                        }
                    }

                    override fun onComplete() {
                        Log.i("package completed")
                    }

                    override fun onError(throwable: Throwable) {
                        Log.i("package error: " + throwable.message)
                        when {
                            throwable is HttpException -> try {
                                val response = throwable.response().errorBody()
                                val midError = Gson().fromJson(response?.charStream(), MultiItemApiSearchResponse::class.java)
                                errorHandler.onNext(getApiCallFailingDetails(type, isChangeSearch, midError.firstError))
                            } catch (e: Exception) {
                                val errorCode = PackageApiError.Code.pkg_error_code_not_mapped
                                errorHandler.onNext(getApiCallFailingDetails(type, isChangeSearch, PackageErrorDetails.PackageAPIErrorDetails(errorCode.name, errorCode)))
                            }
                            RetrofitUtils.isNetworkError(throwable) -> {
                                errorHandler.onNext(getApiCallFailingDetails(type, isChangeSearch, PackageErrorDetails.PackageAPIErrorDetails("no_internet", PackageApiError.Code.no_internet)))
                            }
                            else -> {
                                val errorCode = PackageApiError.Code.pkg_error_code_not_mapped
                                errorHandler.onNext(getApiCallFailingDetails(type, isChangeSearch, PackageErrorDetails.PackageAPIErrorDetails(errorCode.name, errorCode)))
                            }
                        }
                    }
                })
    }
}
