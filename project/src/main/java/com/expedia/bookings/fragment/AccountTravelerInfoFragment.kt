package com.expedia.bookings.fragment
import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import android.support.v7.widget.Toolbar
import com.expedia.bookings.data.Db
import com.expedia.bookings.utils.ArrowXDrawableUtil


class AccountTravelerInfoFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_traveler_info, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupListeners()
    }

    private fun setupToolbar() {
        val toolbar : Toolbar = view.findViewById(R.id.toolbar) as Toolbar
        val userName = Db.getUser()?.primaryTraveler?.fullName ?: ""
        toolbar.title = userName
        toolbar.setNavigationOnClickListener { (context as Activity).onBackPressed() }
    }

    private fun setupListeners() {
        val infoBtn = activity.findViewById(R.id.personal_info)
        infoBtn.setOnClickListener {
            val transaction = fragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, AccountPersonalInfoFragment())
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }
}

