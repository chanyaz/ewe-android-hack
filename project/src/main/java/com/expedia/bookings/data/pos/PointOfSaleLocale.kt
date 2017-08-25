package com.expedia.bookings.data.pos

import org.json.JSONObject

class PointOfSaleLocale(private val data: JSONObject) {

    /** The locale identifier (e.g., "es_AR") for this locale */
    val localeIdentifier by lazy {
        data.optString("localeIdentifier", null)
    }

    /** The url leading to the booking support part of the website (e.g. expedia.com/service) */
    val bookingSupportUrl by lazy {
        data.optString("bookingSupportURL", null)
    }

    /** A locale specific phone number, takes precedence over the POS supportNumber */
    val supportNumber by lazy {
        data.optString("localeSpecificSupportPhoneNumber", null)
    }

    /** The url for please to be downloading this app */
    val appInfoUrl by lazy {
        data.optString("appInfoURL", null)
    }

    /** The url for just the website */
    val websiteUrl by lazy {
        data.optString("websiteURL", null)
    }

    /** The url for travel insurance. Not present for all POS */
    val insuranceUrl by lazy {
        data.optString("insuranceURL", null)
    }

    /** The url for the best price guarantee policy (if available in the POS) */
    val bestPriceGuaranteePolicyUrl by lazy { data.optString("bestPriceGuaranteePolicyURL", null) }

    /** The rules & restrictions disclaimer for every hotel booking */
    val hotelBookingStatement by lazy { data.optString("hotelBookingStatement", null) }

    /** The rules & restrictions disclaimer for every flight booking */
    val flightBookingStatement by lazy { data.optString("flightBookingStatement", null) }

    /** The rules & restrictions disclaimer for every packages booking */
    val packagesBookingStatement by lazy { data.optString("packagesBookingStatement", null) ?: flightBookingStatement }

    /** Insurance Statement for legal information page*/
    val insuranceStatement by lazy { data.optString("insuranceStatement", null) }

    /** The URL for Terms and Conditions for this POS */
    val termsAndConditionsUrl by lazy { data.optString("termsAndConditionsURL", null) }

    /** The URL for Loyalty Terms and Conditions for this POS */
    val loyaltyTermsAndConditionsUrl by lazy { data.optString("loyaltyTermsAndConditionsURL", null) }

    /** The URL for Help Url for Airlines Additional Fee Based On Payment Method for this POS */
    val termsOfBookingUrl by lazy { data.optString("termsOfBookingURL", null) }

    /** The URL for Terms of Booking for this POS (see GB) */
    val privacyPolicyUrl by lazy { data.optString("privacyPolicyURL", null) }

    /** The language code that this locale associates with */
    val languageCode by lazy {
        if (data.optString("languageCode", null) != "zh-hant") data.optString("languageCode", null) else "zh"
    }

    /** The language identifier linked to this locale (linked to language code) */
    val languageId by lazy { data.optInt("languageIdentifier") }

    /** directly gives the forgot_password Url for the POS */
    val forgotPasswordUrl by lazy { data.optString("forgotPasswordURL", null) }

    /** Account creation marketing text */
    val marketingText by lazy { data.optString("createAccountMarketingText") }

    /** Loyalty Rewards Info URL */
    val RewardsInfoURL by lazy { data.optString("rewardsInfoURL") }

    /** Rails Rules and Restrictions URL */
    val railsRulesAndRestrictionsURL by lazy { data.optString("railsRulesAndRestrictionsURL") }

    /** Rails National Rail Conditions of Travel URL */
    val railsNationalRailConditionsOfTravelURL by lazy { data.optString("railsNationalRailConditionsOfTravelURL") }

    /** Rails Supplier Terms and Conditions URL */
    val railsSupplierTermsAndConditionsURL by lazy { data.optString("railsSupplierTermsAndConditionsURL") }

    /** Rails Terms of Use URL */
    val railsTermOfUseURL by lazy { data.optString("railsTermOfUseURL") }

    /** Rails Privacy Policy URL */
    val railsPrivacyPolicyURL by lazy { data.optString("railsPrivacyPolicyURL") }

    /** Rails Payment and Ticket Delivery Fees URL */
    val railsPaymentAndTicketDeliveryFeesURL by lazy { data.optString("railsPaymentAndTicketDeliveryFeesURL") }

    /** Hotel Results Sort FAQ URL */
    val hotelResultsSortFaqUrl by lazy { data.optString("resultsSortFAQLegalLink") }

    /** Cars Tab Web View URL */
    val carsTabWebViewURL by lazy { data.optString("carsTabWebViewURL") }
}