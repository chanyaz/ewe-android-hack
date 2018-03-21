package com.expedia.bookings.customerfirst

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.widget.TextView
import com.expedia.bookings.R
import com.expedia.bookings.customerfirst.model.CustomerFirstSupportModel
import com.expedia.bookings.customerfirst.vm.CustomerFirstSupportViewModel

class CustomerFirstSupportAdapter(private val customerFirstSupportViewModel: CustomerFirstSupportViewModel) : RecyclerView.Adapter<CustomerFirstSupportAdapter.CustomerFirstSupportViewHolder>() {

    private var customerFirstSupportModel: List<CustomerFirstSupportModel> = ArrayList<CustomerFirstSupportModel>()

    init {
        customerFirstSupportViewModel.refreshCustomerSupportSubject.subscribe {
            customerFirstSupportModel = it
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): CustomerFirstSupportViewHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.customer_first_support_row, parent, false)
        return CustomerFirstSupportViewHolder(view, customerFirstSupportViewModel)
    }

    override fun getItemCount(): Int {
        return customerFirstSupportModel.size
    }

    override fun onBindViewHolder(holder: CustomerFirstSupportViewHolder?, position: Int) {
        holder?.bind(customerFirstSupportModel[position])
    }

    class CustomerFirstSupportViewHolder(itemView: View, val viewModel: CustomerFirstSupportViewModel) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        val textView by lazy {
            itemView.findViewById<TextView>(R.id.customer_first_row_title)
        }

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(customerFirstSupportModel: CustomerFirstSupportModel) {
            textView.setCompoundDrawablesWithIntrinsicBounds(customerFirstSupportModel.iconResId, 0, 0, 0)
            textView.text = textView.context.getString(customerFirstSupportModel.titleResId)
            textView.tag = customerFirstSupportModel.ordinal
        }

        override fun onClick(view: View) {
            val customerFirstSupportModel = CustomerFirstSupportModel.values()[view.tag as Int]
            viewModel.customerFirstSupportObservable.onNext(customerFirstSupportModel)
        }
    }
}
