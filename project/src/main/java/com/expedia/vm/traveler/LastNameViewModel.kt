package com.expedia.vm.traveler

import android.content.Context

class LastNameViewModel(context: Context) : BaseTravelerValidatorViewModel() {
    override fun isValid(): Boolean {
        return isRequiredNameValid(getText()) && getText().length >= 2
    }
}