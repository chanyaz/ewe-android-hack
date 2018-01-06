package com.expedia.vm.traveler

class LastNameViewModel : BaseTravelerValidatorViewModel() {
    override fun isValid(): Boolean {
        return isRequiredNameValid(getText()) && getText().length >= 2
    }
}
