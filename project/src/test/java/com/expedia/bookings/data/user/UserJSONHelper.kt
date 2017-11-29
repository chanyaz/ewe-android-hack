package com.expedia.bookings.data.user

import com.expedia.bookings.data.LoyaltyMembershipTier
import com.expedia.bookings.data.Money
import com.expedia.bookings.test.robolectric.UserLoginTestUtil
import org.json.JSONArray
import org.json.JSONObject

class UserJSONHelper {
    companion object {
        val versionOneUserJSONObject: JSONObject
            get() {
                val jsonString = """
                    {
                        "version":1,
                        "storedCreditCards":$storedCreditCardsJSONArray,
                        "storedPointsCards":$storedPointsCardsJSONArray,
                        "associatedTravelers":[${UserLoginTestUtil.mockUser().primaryTraveler.toJson()}],
                        "loyaltyAccountNumber":"300",
                        "loyaltyMembershipInformation":$userLoyaltyMembershipInformationJSONObject
                    }
                    """.trimIndent()

                val jsonObject = JSONObject(jsonString)

                primaryTravelerVersionOneJSONObject.keys().forEach { jsonObject.put(it, primaryTravelerVersionOneJSONObject[it]) }

                return jsonObject
            }

        val versionTwoUserJSONObject: JSONObject
            get() {
                val jsonString = """
                    {
                        "version":2,
                        "primaryTraveler":$primaryTravelerVersionTwoJSONObject,
                        "storedCreditCards":$storedCreditCardsJSONArray,
                        "storedPointsCards":$storedPointsCardsJSONArray,
                        "associatedTravelers":[${UserLoginTestUtil.mockUser().primaryTraveler.toJson()}],
                        "loyaltyAccountNumber":"300",
                        "loyaltyMembershipInformation":$userLoyaltyMembershipInformationJSONObject
                    }
                    """.trimIndent()

                return JSONObject(jsonString)
            }

        private val primaryTravelerVersionOneJSONObject: JSONObject
            get() {
                val jsonString = """
                    {
                        "tuid":100,
                        "expUserId":200,
                        "firstName":"Paul",
                        "middleName":"Vincent",
                        "lastName":"Kite",
                        "email":"pkite@expedia.com",
                        "phoneNumbers":$phoneNumbersJSONArray,
                        "homeAddress":${homeAddressJSONObject(true)}
                    }
                    """.trimIndent()

                return JSONObject(jsonString)
            }

        private val primaryTravelerVersionTwoJSONObject: JSONObject
            get() {
                val jsonString = """
                    {
                        "tuid":100,
                        "expUserId":200,
                        "firstName":"Paul",
                        "middleName":"Vincent",
                        "lastName":"Kite",
                        "email":"pkite@expedia.com",
                        "phoneNumbers":$phoneNumbersJSONArray,
                        "homeAddress":${homeAddressJSONObject(false)}
                    }
                    """.trimIndent()

                return JSONObject(jsonString)
            }

        private val phoneNumbersJSONArray: JSONArray
            get() {
                val jsonString = """
                    [{
                        "number":"5555555555",
                        "category":"PRIMARY",
                        "countryCode":"US",
                        "countryName":"United States"

                    }]
                    """.trimIndent()

                return JSONArray(jsonString)
            }

        private fun homeAddressJSONObject(isVersionOne: Boolean): JSONObject {
            val address = "\"114 Sansome St\""
            val jsonString = """
                {
                    "city":"San Francisco",
                    "postalCode":"94104",
                    "${if (isVersionOne) "countryAlpha3Code" else "countryCode"}":"USA",
                    ${if (isVersionOne) "\"firstAddressLine\":$address" else "\"streetAddress\":[$address]"}
                }
                """.trimIndent()

            return JSONObject(jsonString)
        }

        private val storedCreditCardsJSONArray: JSONArray
            get() {
                val jsonString = """
                    [{
                        "creditCardType":"CARD_VISA",
                        "description":"Visa",
                        "paymentsInstrumentsId":"1",
                        "isGoogleWallet":false,
                        "cardNumber":"4444444444444448",
                        "isSelectable":true,
                        "nameOnCard":"Paul Kite",
                        "expired":false
                    }]
                    """.trimIndent()

                return JSONArray(jsonString)
            }

        private val storedPointsCardsJSONArray: JSONArray
            get() {
                val jsonString = """
                    [{
                        "creditCardType":"CARD_VISA",
                        "description":"Visa",
                        "paymentsInstrumentsId":"1"
                    }]
                    """.trimIndent()

                return JSONArray(jsonString)
            }

        val userLoyaltyMembershipInformationJSONObject: JSONObject
            get() {
                val membershipTierValue = LoyaltyMembershipTier.TOP.toApiValue()
                val monetaryValue = UserLoyaltyMembershipInformation.LoyaltyMonetaryValue(Money(100, "USD"))
                val monetaryValueJSON = monetaryValue.toJson().toString()
                val jsonString = """
                    {
                        "loyaltyPointsAvailable":10000.0,
                        "loyaltyPointsPending":1000.0,
                        "bookingCurrency":"USD",
                        "isAllowedToShopWithPoints":true,
                        "loyaltyMemebershipActive":true,
                        ${if (membershipTierValue != null) "\"membershipTierName\":\"$membershipTierValue\"," else ""}
                        "loyaltyMonetaryValue":$monetaryValueJSON
                    }
                    """.trimIndent()

                return JSONObject(jsonString)
            }
    }
}