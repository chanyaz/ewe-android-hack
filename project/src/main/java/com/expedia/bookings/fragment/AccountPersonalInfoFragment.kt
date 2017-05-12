package com.expedia.bookings.fragment
import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.SpannableStringBuilder
import com.expedia.bookings.data.Db
import com.expedia.bookings.data.Traveler
import com.expedia.bookings.utils.ArrowXDrawableUtil
import com.expedia.bookings.utils.Ui
import com.expedia.bookings.utils.bindView
import com.expedia.bookings.widget.TelephoneSpinner
import com.expedia.bookings.widget.accessibility.AccessibleEditText
import com.expedia.bookings.widget.accessibility.AccessibleEditTextForSpinner
import com.expedia.bookings.widget.traveler.NameEntryView
import com.expedia.bookings.widget.traveler.PhoneEntryView
import com.expedia.bookings.widget.traveler.TSAEntryView
import com.expedia.bookings.widget.traveler.TravelerEditText
import com.expedia.vm.traveler.TravelerNameViewModel
import com.expedia.vm.traveler.TravelerPhoneViewModel
import com.expedia.vm.traveler.TravelerTSAViewModel


class AccountPersonalInfoFragment : Fragment() {

    val toolbar: Toolbar by bindView(R.id.toolbar)
    lateinit var traveler: Traveler

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_account_personal_info, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        traveler = Db.getUser().primaryTraveler
        setupToolbar()
        setupName()
        setupTSA()
        setupPhone()
        setupAddress()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { (context as Activity).onBackPressed() }
    }

    private fun setupName() {
        val nameEntry = view?.findViewById(R.id.name_entry_widget) as NameEntryView
        val vm = TravelerNameViewModel(context)
        vm.updateTravelerName(traveler.name)
        nameEntry.viewModel = vm

        val firstName = view?.findViewById(R.id.first_name_input) as TravelerEditText
        val middleName = view?.findViewById(R.id.middle_name_input) as TravelerEditText
        val lastName = view?.findViewById(R.id.last_name_input) as TravelerEditText
        firstName.setEnabled(false)
        middleName.setEnabled(false)
        lastName.setEnabled(false)
    }

    private fun setupTSA() {
        Ui.getApplication(context).defaultTravelerComponent()
        val tsaEntry = view?.findViewById(R.id.tsa_entry_widget) as TSAEntryView
        val vm = TravelerTSAViewModel(traveler, context)
        tsaEntry.viewModel = vm
        tsaEntry.setEnabled(false)

        val birthdate = view?.findViewById(R.id.edit_birth_date_text_btn) as TravelerEditText
        val gender = view?.findViewById(R.id.edit_gender_btn) as TravelerEditText
        birthdate.setEnabled(false)
        gender.setEnabled(false)
    }

    private fun setupPhone() {
        val phoneEntry = view?.findViewById(R.id.phone_entry_widget) as PhoneEntryView
        val vm = TravelerPhoneViewModel(context)
        if (traveler.primaryPhoneNumber != null) {
            vm.updatePhone(traveler.primaryPhoneNumber)
            phoneEntry.viewModel = vm
        }

        val phoneCountryCode = view?.findViewById(R.id.material_edit_phone_number_country_code) as AccessibleEditTextForSpinner
        val phoneNumber = view?.findViewById(R.id.edit_phone_number) as TravelerEditText
        phoneCountryCode.setEnabled(false)
        phoneNumber.setEnabled(false)
    }

    private fun setupAddress() {
        val addressLine1 = view?.findViewById(R.id.edit_address_line_one) as AccessibleEditText
        val addressLine2 = view?.findViewById(R.id.edit_address_line_two) as AccessibleEditText
        val addressCity = view?.findViewById(R.id.edit_address_city) as AccessibleEditText
        val addressState = view?.findViewById(R.id.edit_address_state) as AccessibleEditText
        val addressCountry = view?.findViewById(R.id.material_edit_country) as AccessibleEditText
        val addressPostalCode = view?.findViewById(R.id.edit_address_postal_code) as AccessibleEditText

        if (traveler.homeAddress != null) {
            addressLine1.text = SpannableStringBuilder(traveler.homeAddress.streetAddressLine1)
            addressLine2.text = SpannableStringBuilder(traveler.homeAddress.streetAddressLine2)
            addressCity.text = SpannableStringBuilder(traveler.homeAddress.city)
            addressState.text = SpannableStringBuilder(traveler.homeAddress.stateCode)
            addressCountry.text = SpannableStringBuilder(traveler.homeAddress.countryCode)
            addressPostalCode.text = SpannableStringBuilder(traveler.homeAddress.postalCode)
        }

        addressLine1.setEnabled(false)
        addressLine2.setEnabled(false)
        addressCity.setEnabled(false)
        addressState.setEnabled(false)
        addressCountry.setEnabled(false)
        addressPostalCode.setEnabled(false)
    }
}