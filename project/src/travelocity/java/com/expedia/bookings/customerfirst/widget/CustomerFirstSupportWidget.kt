package com.expedia.bookings.customerfirst.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.customerfirst.CustomerFirstSupportAdapter
import com.expedia.bookings.customerfirst.model.CustomerFirstSupportModel
import com.expedia.bookings.customerfirst.vm.CustomerFirstSupportViewModel
import com.expedia.bookings.presenter.Presenter
import com.expedia.bookings.utils.AccessibilityUtil
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.RecyclerDividerDecoration
import com.expedia.util.notNullAndObservable
import kotlin.properties.Delegates

class CustomerFirstSupportWidget(context: Context, attr: AttributeSet?) : Presenter(context, attr) {

    private val toolBar by bindView<Toolbar>(R.id.customer_first_support_toolbar)
    private val browseCardView by bindView<CardView>(R.id.browse_card_view)
    private val recyclerView by bindView<RecyclerView>(R.id.customer_first_support_recycler_view)
    private val helpTopicsTextView by bindView<TextView>(R.id.customer_first_row_title)

    private var adapter: CustomerFirstSupportAdapter by Delegates.notNull()

    var viewModel: CustomerFirstSupportViewModel by notNullAndObservable { vm ->
        browseCardView.setOnClickListener { vm.customerFirstSupportObservable.onNext(CustomerFirstSupportModel.HELP_TOPICS) }
        adapter = CustomerFirstSupportAdapter(vm)
        recyclerView.adapter = adapter
        vm.refreshCustomerSupportSubject.onNext(vm.getCustomerFirstSupportList())
    }

    init {
        View.inflate(context, R.layout.customer_first_support_widget, this)
        viewModel = CustomerFirstSupportViewModel(context)
        helpTopicsTextView.text = context.getString(CustomerFirstSupportModel.HELP_TOPICS.titleResId)
        helpTopicsTextView.setCompoundDrawablesWithIntrinsicBounds(CustomerFirstSupportModel.HELP_TOPICS.iconResId, 0, 0, 0)

        toolBar.setNavigationOnClickListener {
            (context as Activity).onBackPressed()
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        recyclerView.addItemDecoration(RecyclerDividerDecoration(context, 0, 0, 0, 0, 0, 0, true))
        recyclerView.layoutManager = LinearLayoutManager(context)
        AccessibilityUtil.setFocusToToolbarNavigationIcon(toolBar)
    }
}
