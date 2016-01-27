package com.expedia.bookings.presenter.packages

import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import com.expedia.bookings.R
import com.expedia.bookings.data.Codes
import com.expedia.bookings.data.LineOfBusiness
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Constants
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.BaseCheckoutPresenter
import com.expedia.bookings.widget.CheckoutToolbar
import com.expedia.bookings.widget.PackageBundleHotelWidget
import com.expedia.bookings.widget.TextView
import com.expedia.ui.FlightPackageActivity
import com.expedia.util.notNullAndObservable
import com.expedia.util.subscribeText
import com.expedia.vm.BaseCheckoutViewModel
import com.expedia.vm.BundleHotelViewModel
import com.expedia.vm.BundleOverviewViewModel
import com.expedia.vm.CheckoutToolbarViewModel

public class BundleOverviewPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {
    val ANIMATION_DURATION = 450L

    val toolbar: CheckoutToolbar by bindView(R.id.checkout_toolbar)
    val bundleContainer: ScrollView by bindView(R.id.bundle_container)
    val checkoutContainer: LinearLayout by bindView(R.id.checkout_container)
    val checkoutPresenter: BaseCheckoutPresenter by bindView(R.id.checkout_presenter)
    val handle: CardView by bindView(R.id.handle)
    val chevron: View by bindView(R.id.chevron)
    val checkoutButton: Button by bindView(R.id.checkout_button)
    val bundleHotelWidget: PackageBundleHotelWidget by bindView(R.id.packageBundleWidget)
    val flightLoadingBar: ProgressBar by bindView(R.id.flight_loading_bar)
    val selectDepartureButton: CardView by bindView(R.id.flight_departure_card_view)
    val selectArrivalButton: CardView by  bindView(R.id.flight_arrival_card_view)
    val destinationText: TextView by bindView(R.id.flight_departure_card_view_text)
    val arrivalText: TextView by bindView(R.id.flight_arrival_card_view_text)

    var viewModel: BundleOverviewViewModel by notNullAndObservable { vm ->
        vm.hotelParamsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(true)
        }
        vm.hotelResultsObservable.subscribe {
            bundleHotelWidget.viewModel.showLoadingStateObservable.onNext(false)
        }
        vm.flightParamsObservable.subscribe {
            selectDepartureButton.isEnabled = false
            selectArrivalButton.isEnabled = false
            flightLoadingBar.visibility = View.VISIBLE
        }
        vm.flightResultsObservable.subscribe {
            selectDepartureButton.isEnabled = true
            selectArrivalButton.isEnabled = true
            flightLoadingBar.visibility = View.GONE
        }
        vm.destinationTextObservable.subscribeText(destinationText)
        vm.arrivalTextObservable.subscribeText(arrivalText)
    }

    init {
        View.inflate(context, R.layout.bundle_overview, this)
        bundleHotelWidget.viewModel = BundleHotelViewModel(context)
        checkoutPresenter.viewModel = BaseCheckoutViewModel(context)
        checkoutPresenter.viewModel.lineOfBusiness.onNext(LineOfBusiness.HOTELSV2)
        checkoutPresenter.viewModel.creditCardRequired.onNext(true)
        checkoutPresenter.travelerWidget.mToolbarListener = toolbar
        checkoutPresenter.paymentWidget.mToolbarListener = toolbar
        toolbar.viewModel = CheckoutToolbarViewModel(context)
        toolbar.viewModel.nextClicked.subscribe {
            checkoutPresenter.expandedView?.setNextFocus()
        }
        toolbar.viewModel.doneClicked.subscribe {
            checkoutPresenter.expandedView?.onMenuButtonPressed()
            Ui.hideKeyboard(this)
        }

        val statusBarHeight = Ui.getStatusBarHeight(getContext())
        if (statusBarHeight > 0) {
            val color = ContextCompat.getColor(context, R.color.packages_primary_color)
            val statusBar = Ui.setUpStatusBar(getContext(), toolbar, bundleContainer, color)
            addView(statusBar)
        }
        val padding = Ui.getToolbarSize(context) + statusBarHeight
        checkoutContainer.setPadding(0, padding, 0, 0)

        selectDepartureButton.isEnabled = false
        selectArrivalButton.isEnabled = false
        selectDepartureButton.setOnClickListener {
            openFlightsForDeparture()
        }
        selectArrivalButton.setOnClickListener {
            openFlightsForArrival()
        }
        checkoutButton.setOnClickListener {
            show(checkoutPresenter)
            checkoutPresenter.show(BaseCheckoutPresenter.CheckoutDefault(), FLAG_CLEAR_BACKSTACK)
        }

        //calculates the difference for rotating the chevron and translating the checkout presenter
        handle.setOnTouchListener(object : View.OnTouchListener {
            internal var originY: Float = 0.toFloat()
            internal var doneForNow: Boolean = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    (MotionEvent.ACTION_DOWN) -> {
                        // this could probs break it cause multitouch
                        doneForNow = false
                        originY = event.rawY
                    }
                    (MotionEvent.ACTION_UP) -> {
                        originY = 0f
                        doneForNow = false
                    }
                    (MotionEvent.ACTION_MOVE) -> if (!doneForNow) {
                        val diff = event.rawY - originY
                        if (rotateChevron(Math.max(diff, 0f))) {
                            doneForNow = true
                        }
                    }
                }
                return true
            }
        })
    }

    //Either shows the bundle overview or the checkout presenter based on distance/rotation
    private fun rotateChevron(distance: Float): Boolean {
        val distanceGoal = 300f
        if (distance > distanceGoal) {
            show(BundleDefault(), FLAG_CLEAR_BACKSTACK)
            return true
        } else {
            checkoutContainer.translationY = distance
            chevron.rotation = distance / distanceGoal * (-90)
            return false
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        addDefaultTransition(defaultTransition)
        addTransition(checkoutTransition)
        show(BundleDefault())
    }

    val defaultTransition = object : Presenter.DefaultTransition(BundleDefault::class.java.name) {
        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
        }
    }

    val checkoutTransition = object : Presenter.Transition(BundleDefault::class.java, BaseCheckoutPresenter::class.java) {
        override fun startTransition(forward: Boolean) {
            checkoutButton.visibility = if (forward) View.GONE else View.VISIBLE
            bundleContainer.visibility = View.VISIBLE
            checkoutContainer.visibility = View.VISIBLE
        }

        override fun updateTransition(f: Float, forward: Boolean) {
            bundleContainer.translationY = if (forward)  f * -bundleContainer.height.toFloat() else (1 - f) * bundleContainer.height.toFloat()
            checkoutContainer.translationY = if (forward)  (f - 1) * -checkoutContainer.height.toFloat() else f * checkoutContainer.height.toFloat()
        }

        override fun endTransition(forward: Boolean) {
            super.endTransition(forward)
        }

        override fun finalizeTransition(forward: Boolean) {
            super.finalizeTransition(forward)
        }
    }

    fun openFlightsForDeparture() {
        val intent = Intent(context, FlightPackageActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.PACKAGE_FLIGHT_DEPARTURE_REQUEST_CODE, null)
    }

    fun openFlightsForArrival() {
        val intent = Intent(context, FlightPackageActivity::class.java)
        (context as AppCompatActivity).startActivityForResult(intent, Constants.PACKAGE_FLIGHT_ARRIVAL_REQUEST_CODE, null)
    }

    override fun back(): Boolean {
        bundleHotelWidget.collapseSelectedHotel()
        return super.back()
    }

    class BundleDefault
}