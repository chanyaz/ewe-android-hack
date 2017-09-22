package com.expedia.vm.packages

import android.content.Context
import android.support.v4.content.ContextCompat
import com.expedia.bookings.R
import com.expedia.bookings.data.BaseSearchParams
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.data.flights.FlightLeg
import com.expedia.bookings.data.flights.FlightTripDetails
import com.expedia.bookings.data.pos.PointOfSale
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.FlightV2Utils
import com.expedia.bookings.utils.LocaleBasedDateFormatUtils
import com.expedia.bookings.utils.StrUtils
import com.expedia.bookings.utils.Ui
import com.squareup.phrase.Phrase
import org.joda.time.LocalDate
import org.joda.time.format.ISODateTimeFormat
import rx.Observable
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

class BundleFlightViewModel(val context: Context, val lob: LineOfBusiness) {
    val showLoadingStateObservable = PublishSubject.create<Boolean>()
    val selectedFlightObservable = PublishSubject.create<PackageSearchType>()
    val searchTypeStateObservable = BehaviorSubject.create<PackageSearchType>()
    val date = PublishSubject.create<LocalDate>()
    val guests = BehaviorSubject.create<Int>()
    val suggestion = BehaviorSubject.create<SuggestionV4>()
    val flight = BehaviorSubject.create<FlightLeg>()

    val flightsRowExpanded = PublishSubject.create<Unit>()

    //output
    val flightTextObservable = BehaviorSubject.create<String>()
    val travelInfoTextObservable = BehaviorSubject.create<String>()
    val flightDetailsIconObservable = BehaviorSubject.create<Boolean>()
    val flightSelectIconObservable = BehaviorSubject.create<Boolean>()
    val flightIconImageObservable = BehaviorSubject.create<Pair<Int, Int>>()
    val flightTextColorObservable = BehaviorSubject.create<Int>()
    val flightTravelInfoColorObservable = BehaviorSubject.create<Int>()
    val flightInfoContainerObservable = BehaviorSubject.create<Boolean>()
    val selectedFlightLegObservable = BehaviorSubject.create<FlightLeg>()
    val totalDurationObserver = BehaviorSubject.create<CharSequence>()
    val totalDurationContDescObserver = BehaviorSubject.create<String>()
    val searchParams = BehaviorSubject.create<BaseSearchParams>()
    val showRowContainerWithMoreInfo = BehaviorSubject.create<Boolean>(AbacusFeatureConfigManager.isUserBucketedForTest(AbacusUtils.EBAndroidAppFlightsMoreInfoOnOverview)
            && (lob == LineOfBusiness.FLIGHTS_V2))
    val updateUpsellClassPreference = PublishSubject.create<Pair<List<FlightTripDetails.SeatClassAndBookingCode>, Boolean>>()

    val showPaymentInfoLinkObservable = PublishSubject.create<Boolean>()
    val baggageInfoUrlSubject = PublishSubject.create<String>()
    val baggageInfoClickSubject = PublishSubject.create<Unit>()
    val paymentFeeInfoClickSubject = PublishSubject.create<Unit>()
    lateinit var baggageUrl : String

    init {
        Observable.combineLatest(searchTypeStateObservable, suggestion, date, guests, { searchType, suggestion, date, guests ->
            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatCityName(suggestion)))
                travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                        .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(date))
                        .put("travelers", StrUtils.formatTravelerString(context, guests))
                        .format()
                        .toString())
            } else {
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_icon, ContextCompat.getColor(context, R.color.package_bundle_icon_color)))
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatCityName(suggestion)))
                if (date != null) {
                    travelInfoTextObservable.onNext(Phrase.from(context, R.string.flight_toolbar_date_range_with_guests_TEMPLATE)
                            .put("date", LocaleBasedDateFormatUtils.localDateToMMMd(date))
                            .put("travelers", StrUtils.formatTravelerString(context, guests))
                            .format()
                            .toString())
                }
            }
            flightInfoContainerObservable.onNext(false)
            flightDetailsIconObservable.onNext(false)
            flightSelectIconObservable.onNext(false)
            flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
            flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.package_bundle_icon_color))
        }).subscribe()

        showLoadingStateObservable.subscribe { isShowing ->
            if (isShowing) {
                flightSelectIconObservable.onNext(false)
                flightDetailsIconObservable.onNext(false)
                travelInfoTextObservable.onNext("")
            } else {
                flightSelectIconObservable.onNext(true)
                flightTextColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
                flightTravelInfoColorObservable.onNext(Ui.obtainThemeColor(context, R.attr.primary_color))
            }
        }

        Observable.combineLatest(selectedFlightObservable, flight, suggestion, date, guests, { searchType, flight, suggestion, date, guests ->
            baggageUrl = flight.baggageFeesUrl
            val fmt = ISODateTimeFormat.dateTime()
            val localDate = LocalDate.parse(flight.departureDateTimeISO, fmt)
            flightSelectIconObservable.onNext(false)
            flightDetailsIconObservable.onNext(true)
            flightTextColorObservable.onNext(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_primary_text))
            flightTravelInfoColorObservable.onNext(ContextCompat.getColor(context, R.color.packages_bundle_overview_widgets_secondary_text))
            travelInfoTextObservable.onNext(context.getString(R.string.package_overview_flight_travel_info_TEMPLATE, LocaleBasedDateFormatUtils.localDateToMMMd(localDate),
                    FlightV2Utils.formatTimeShort(context, flight.departureDateTimeISO), StrUtils.formatTravelerString(context, guests)))

            if (searchType == PackageSearchType.OUTBOUND_FLIGHT) {
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(flight)))
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight1_checkmark_icon, 0))
            } else {
                flightTextObservable.onNext(context.getString(R.string.flight_to, StrUtils.formatAirportCodeCityName(flight)))
                flightIconImageObservable.onNext(Pair(R.drawable.packages_flight2_checkmark_icon, 0))
            }

            val totalDurationContentDescription = if (showRowContainerWithMoreInfo.value) {
                FlightV2Utils.getFlightLegDurationWithButtonInfoContentDescription(context, flight)
            } else {
                FlightV2Utils.getFlightLegDurationContentDescription(context, flight)
            }

            showPaymentInfoLinkObservable.onNext(flight.mayChargeObFees || PointOfSale.getPointOfSale().showAirlinePaymentMethodFeeLegalMessage())

            val e3EndpointUrl = Ui.getApplication(context).appComponent().endpointProvider().e3EndpointUrl
            baggageInfoClickSubject.subscribe {
                if (baggageUrl.contains("http")) {
                    baggageInfoUrlSubject.onNext(baggageUrl)
                } else {
                    baggageInfoUrlSubject.onNext(e3EndpointUrl + baggageUrl)
                }
            }

            totalDurationContDescObserver.onNext(totalDurationContentDescription)
            totalDurationObserver.onNext(FlightV2Utils.getStylizedFlightDurationString(context, flight, R.color.packages_total_duration_text))
            selectedFlightLegObservable.onNext(flight)
        }).subscribe()
    }
}