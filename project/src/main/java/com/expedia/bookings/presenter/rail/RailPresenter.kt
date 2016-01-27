package com.expedia.bookings.presenter.rail;

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.expedia.bookings.R
import com.expedia.bookings.presenter.LeftToRightTransition
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView

public class RailPresenter(context: Context, attrs: AttributeSet) : Presenter(context, attrs) {

    val searchPresenter: RailSearchPresenter by bindView(R.id.widget_rail_search_presenter)
    val resultsPresenter: RailResultsPresenter by bindView(R.id.widget_rail_results_presenter)
    val detailsPresenter: RailDetailsPresenter by bindView(R.id.widget_rail_details_presenter)
    val checkoutPresenter: RailCheckoutPresenter by bindView(R.id.widget_rail_checkout_presenter)

    private val searchToResults = LeftToRightTransition(this, RailSearchPresenter::class.java, RailResultsPresenter::class.java)
    private val resultsToDetails = LeftToRightTransition(this, RailResultsPresenter::class.java, RailDetailsPresenter::class.java)
    private val detailsToCheckout = LeftToRightTransition(this, RailDetailsPresenter::class.java, RailCheckoutPresenter::class.java)
    private val checkoutToSearch = LeftToRightTransition(this, RailCheckoutPresenter::class.java, RailSearchPresenter::class.java)

    init {
        Ui.getApplication(getContext()).railComponent().inject(this)
        View.inflate(context, R.layout.rail_presenter, this)
        addTransition(searchToResults)
        addTransition(resultsToDetails)
        addTransition(detailsToCheckout)
        addTransition(checkoutToSearch)

        searchPresenter.setOnClickListener { transitionToResults() }
        resultsPresenter.setOnClickListener { transitionToDetails() }
        detailsPresenter.setOnClickListener { transitionToCheckout() }
        checkoutPresenter.setOnClickListener { transitionToSearch() } //todo - should show confirmation
        show(searchPresenter)
    }

    private fun transitionToSearch() {
        show(searchPresenter)
    }

    private fun transitionToResults() {
        show(resultsPresenter)
    }

    private fun transitionToDetails() {
        show(detailsPresenter)
    }

    private fun transitionToCheckout() {
        show(checkoutPresenter)
    }
}