package com.expedia.vm.launch

interface GenericViewModel {
    val firstLine: String
    val secondLine: String
    val buttonOneLabel: String
    val buttonTwoLabel: String
}

data class SignInPlaceHolderViewModel(override val firstLine: String, override val secondLine: String, override val buttonOneLabel: String, override val buttonTwoLabel: String): GenericViewModel
data class PopularHotelsTonightViewModel(val background: Int, val firstLine: String, val secondLine: String)
data class ContinueBookingHolderViewModel(val firstLine: String, val secondLine: String)
