package com.expedia.bookings.data.pos

import org.json.JSONObject

class PointOfSaleLocale(private val data: JSONObject) {

    /** The locale identifier (e.g., "es_AR") for this locale */
    val localeIdentifier: String? by lazy {
        data.optString("localeIdentifier", null)
    }

    /** The url leading to the booking support part of the website (e.g. expedia.com/service) */
    val bookingSupportUrl: String? by lazy {
        data.optString("bookingSupportURL", null)
    }

    /** A locale specific phone number, takes precedence over the POS supportNumber */
    val supportNumber: String? by lazy {
        data.optString("localeSpecificSupportPhoneNumber", null)
    }

    /** The url for please to be downloading this app */
    val appInfoUrl: String? by lazy {
        data.optString("appInfoURL", null)
    }

    /** The url for just the website */
    val websiteUrl: String? by lazy {
        data.optString("websiteURL", null)
    }

    /** The url for just the account page */
    val accountPageUrl: String? by lazy {
        data.optString("accountURL", null)
    }

    /** The url for travel insurance. Not present for all POS */
    val insuranceUrl: String? by lazy {
        data.optString("insuranceURL", null)
    }

    /** The rules & restrictions disclaimer for every hotel booking */
    val hotelBookingStatement: String? by lazy { data.optString("hotelBookingStatement", null) }

    /** The rules & restrictions disclaimer for every packages booking */
    val packagesBookingStatement: String? by lazy { data.optString("packagesBookingStatement", null) }

    /** Insurance Statement for legal information page*/
    val insuranceStatement: String? by lazy { data.optString("insuranceStatement", null) }

    /** The URL for Terms and Conditions for this POS */
    val termsAndConditionsUrl: String? by lazy { data.optString("termsAndConditionsURL", null) }

    /** The URL for Loyalty Terms and Conditions for this POS */
    val loyaltyTermsAndConditionsUrl: String? by lazy { data.optString("loyaltyTermsAndConditionsURL", null) }

    /** The URL for Help Url for Airlines Additional Fee Based On Payment Method for this POS */
    val termsOfBookingUrl: String? by lazy { data.optString("termsOfBookingURL", null) }

    /** The URL for Terms of Booking for this POS (see GB) */
    val privacyPolicyUrl: String? by lazy { data.optString("privacyPolicyURL", null) }

    /** The language code that this locale associates with */
    val languageCode: String? by lazy {
        data.optString("languageCode", null)
    }

    /** The language identifier linked to this locale (linked to language code) */
    val languageId by lazy { data.optInt("languageIdentifier") }

    /** directly gives the forgot_password Url for the POS */
    val forgotPasswordUrl: String? by lazy { data.optString("forgotPasswordURL", null) }

    /** Account creation marketing text */
    val marketingText: String by lazy { data.optString("createAccountMarketingText") }

    /** Loyalty Rewards Info URL */
    val rewardsInfoURL: String by lazy { data.optString("rewardsInfoURL") }

    /** Rails Rules and Restrictions URL */
    val railsRulesAndRestrictionsURL: String by lazy { data.optString("railsRulesAndRestrictionsURL") }

    /** Rails National Rail Conditions of Travel URL */
    val railsNationalRailConditionsOfTravelURL: String by lazy { data.optString("railsNationalRailConditionsOfTravelURL") }

    /** Rails Supplier Terms and Conditions URL */
    val railsSupplierTermsAndConditionsURL: String by lazy { data.optString("railsSupplierTermsAndConditionsURL") }

    /** Rails Terms of Use URL */
    val railsTermOfUseURL: String by lazy { data.optString("railsTermOfUseURL") }

    /** Rails Privacy Policy URL */
    val railsPrivacyPolicyURL: String by lazy { data.optString("railsPrivacyPolicyURL") }

    /** Rails Payment and Ticket Delivery Fees URL */
    val railsPaymentAndTicketDeliveryFeesURL: String by lazy { data.optString("railsPaymentAndTicketDeliveryFeesURL") }

    /** Hotel Results Sort FAQ URL */
    val hotelResultsSortFaqUrl: String by lazy { data.optString("resultsSortFAQLegalLink") }

    /** Cars Tab Web View URL */
    val carsTabWebViewURL: String by lazy { data.optString("carsTabWebViewURL") }
}
