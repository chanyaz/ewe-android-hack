package com.expedia.bookings.presenter.flight

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.expedia.bookings.R
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.extensions.subscribeOnClick
import com.expedia.bookings.extensions.subscribeText
import com.expedia.bookings.extensions.subscribeTextAndVisibility
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.utils.AnimUtils
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TextView
import com.expedia.bookings.widget.packages.InboundFlightWidget
import com.expedia.bookings.widget.packages.OutboundFlightWidget
import com.expedia.util.notNullAndObservable
import com.expedia.vm.packages.BundleFlightViewModel
import com.expedia.vm.packages.FlightOverviewSummaryViewModel
import com.squareup.phrase.Phrase
import io.reactivex.subjects.PublishSubject

class FlightSummaryWidget(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    val outboundFlightTitle: TextView by bindView(R.id.outbound_flight_title)
    val inboundFlightTitle: TextView by bindView(R.id.inbound_flight_title)
    val outboundFlightWidget: OutboundFlightWidget by bindView(R.id.package_bundle_outbound_flight_widget)
    val inboundFlightWidget: InboundFlightWidget by bindView(R.id.package_bundle_inbound_flight_widget)
    val freeCancellationInfoTextView: TextView by bindView(R.id.free_cancellation_info)
    val freeCancellationMoreInfoTextView: TextView by bindView(R.id.free_cancellation_more_info)
    val freeCancellationMoreInfoIcon: android.widget.ImageView by bindView(R.id.free_cancellation_more_info_icon)
    val freeCancellationInfoContainer: LinearLayout by bindView(R.id.free_cancellation_layout)
    val splitTicketBaggageFeesTextView: android.widget.TextView by bindView(R.id.split_ticket_baggage_fee_links)
    val splitTicketInfoContainer: View by bindView(R.id.split_ticket_info_container)
    val scrollSpaceView: View by bindView(R.id.scroll_space_flight)
    val airlineFeeWarningTextView: android.widget.TextView by bindView(R.id.airline_fee_warning_text)
    val basicEconomyMessageTextView: View by bindView(R.id.basic_economy_info)
    val evolableTermsConditionTextView: android.widget.TextView by bindView(R.id.evolable_terms_condition_text)

    val basicEconomyInfoClickedSubject: PublishSubject<Unit> = PublishSubject.create<Unit>()

    init {
        View.inflate(context, R.layout.flight_summary, this)
        orientation = VERTICAL
        outboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        inboundFlightWidget.viewModel = BundleFlightViewModel(context, LineOfBusiness.FLIGHTS_V2)
        outboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight1_icon)
        inboundFlightWidget.flightIcon.setImageResource(R.drawable.packages_flight2_icon)
        val shouldShowSeatingAndCabinClass = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsSeatClassAndBookingCode)
        outboundFlightWidget.showFlightCabinClass = shouldShowSeatingAndCabinClass
        inboundFlightWidget.showFlightCabinClass = shouldShowSeatingAndCabinClass
        basicEconomyMessageTextView.subscribeOnClick(basicEconomyInfoClickedSubject)
    }

    var viewmodel: FlightOverviewSummaryViewModel by notNullAndObservable { vm ->
        vm.outboundFlightTitle.subscribeText(outboundFlightTitle)
        vm.inboundFlightTitle.subscribeTextAndVisibility(inboundFlightTitle)
        vm.outboundLeg.subscribe(outboundFlightWidget.viewModel.flight)

        vm.outboundBundleWidgetClassObservable.subscribe(outboundFlightWidget.viewModel.updateUpsellClassPreference)
        vm.inboundBundleWidgetClassObservable.subscribe(inboundFlightWidget.viewModel.updateUpsellClassPreference)
        vm.outboundBundleBaggageUrlSubject.subscribe {
            outboundFlightWidget.viewModel.baggageUrl = it
        }
        vm.inboundBundleBaggageUrlSubject.subscribe {
            inboundFlightWidget.viewModel.baggageUrl = it
        }

        vm.updatedInboundFlightLegSubject.subscribe {
            inboundFlightWidget.viewModel.updatedFlightLeg = it
        }

        vm.updatedOutboundFlightLegSubject.subscribe {
            outboundFlightWidget.viewModel.updatedFlightLeg = it
        }

        freeCancellationInfoContainer.subscribeOnClick(vm.freeCancellationInfoClickSubject)
        vm.freeCancellationInfoSubject.subscribe {
            if (it) {
                freeCancellationMoreInfoTextView.visibility = View.VISIBLE
                AnimUtils.rotate(freeCancellationMoreInfoIcon)
                freeCancellationInfoContainer.contentDescription =
                        Phrase.from(context, R.string.bundle_overview_free_canellation_expanded_button_description_TEMPLATE)
                                .put("rowtitle", freeCancellationInfoTextView.text.toString())
                                .put("rowdescription", freeCancellationMoreInfoTextView.text.toString()).format().toString()
            } else {
                freeCancellationMoreInfoTextView.visibility = View.GONE
                AnimUtils.reverseRotate(freeCancellationMoreInfoIcon)
                freeCancellationInfoContainer.contentDescription =
                        Phrase.from(context, R.string.bundle_overview_free_canellation_collapsed_button_description_TEMPLATE)
                                .put("rowdescription", freeCancellationInfoTextView.text.toString()).format().toString()
            }
            freeCancellationInfoContainer.announceForAccessibility(freeCancellationInfoContainer.contentDescription)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        splitTicketBaggageFeesTextView.movementMethod = LinkMovementMethod.getInstance()
    }

    fun collapseFlightWidgets() {
        inboundFlightWidget.backButtonPressed()
        outboundFlightWidget.backButtonPressed()
    }
}
