package com.expedia.bookings.marketing.carnival

import com.expedia.bookings.marketing.carnival.model.CarnivalMessage
import com.expedia.bookings.utils.Constants

class FullPageDealViewModel(carnivalMessage: CarnivalMessage) {

    val dealTitle = carnivalMessage.attributes?.get(Constants.CARNIVAL_TITLE) ?: ""
    val dealInstructions = carnivalMessage.attributes?.get(Constants.CARNIVAL_DEAL_INSTRUCTIONS)
    val promoCodeText = carnivalMessage.attributes?.get(Constants.CARNIVAL_PROMO_CODE_TEXT)
    val detailsTitle = carnivalMessage.attributes?.get(Constants.CARNIVAL_DETAILS_TITLE)
    val detailsDescription = carnivalMessage.attributes?.get(Constants.CARNIVAL_DETAILS_DESCRIPTION)
    val termsTitle = carnivalMessage.attributes?.get(Constants.CARNIVAL_TERMS_TITLE)
    val termsDescription = carnivalMessage.attributes?.get(Constants.CARNIVAL_TERMS_DESCRIPTION)
    val shopButtonText = carnivalMessage.attributes?.get(Constants.CARNIVAL_SHOP_BUTTON_TITLE)
    val deeplink = carnivalMessage.attributes?.get(Constants.CARNIVAL_DEEPLINK)
    val shopDealsButtonLabel = carnivalMessage.attributes?.get(Constants.CARNIVAL_IN_APP_BUTTON1_LABEL)
    val cancelDealsButtonLabel = carnivalMessage.attributes?.get(Constants.CARNIVAL_IN_APP_BUTTON2_LABEL)
    val imageUrl = carnivalMessage.imageURL
    val title = carnivalMessage.title
    val text = carnivalMessage.text
}
