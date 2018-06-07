package com.expedia.bookings.activity

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.expedia.bookings.R
import javax.inject.Inject

class TNSTestAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val textView = LayoutInflater.from(parent!!.context)
                .inflate(R.layout.text_label, parent, false) as TextView
        textView.textSize = 25f
        textView.setPadding(10, 10, 10, 10);
        return TNSViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return TNSServices.values().size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        (holder as TNSViewHolder).textView.text = TNSServices.values()[position].toString()
    }

    class TNSViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView), View.OnClickListener  {

        var viewModel: TNSViewHolderViewModel = TNSViewHolderViewModel()

        class TNSViewHolderViewModel {
            @Inject
            var tnsService: com.expedia.bookings.services.TNSServices? = null
        }

        override fun onClick(v: View?) {
            //Show Loading Indicator
        }

    }

}

enum class TNSServices {
    FlightCancelled
}
