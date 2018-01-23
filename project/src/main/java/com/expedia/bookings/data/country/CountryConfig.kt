package com.expedia.bookings.data.country

import android.content.res.AssetManager
import com.expedia.bookings.utils.Constants
import com.google.gson.Gson
import java.io.IOException
import java.util.AbstractCollection

class CountryConfig {

    lateinit var billingCountryConfigs: HashMap<String, BillingAddressCountryConfig>
    lateinit var countries: HashMap<String, HashMap<String, String>>

    class BillingAddressCountryConfig {
        var postalCodeRequired: Boolean = true
        var stateRequired = StateRequired.REQUIRED
    }

    enum class StateRequired {
        REQUIRED,
        OPTIONAL,
        NOT_REQUIRED
    }

    companion object {

        lateinit var countryConfig: CountryConfig

        @JvmStatic fun loadCountryConfigs(assetManager: AssetManager) {
            countryConfig = Gson().fromJson(loadJSONFromAsset(assetManager), CountryConfig::class.java)
        }

        fun loadJSONFromAsset(assetManager: AssetManager): String? {
            var json: String? = null
            try {
                val open = assetManager.open("ExpediaSharedData/country/CountryConfig.json")
                val size = open.available()
                val buffer = ByteArray(size)
                open.read(buffer)
                open.close()
                json = String(buffer)
            } catch (ex: IOException) {
                ex.printStackTrace()
                return null
            }

            return json
        }

        fun getCountryConfigId(countryCode: String): String {
            if (!countryConfig.countries.contains(countryCode)) {
                return Constants.DEFAULT_HIDE_FIELDS_CONFIG_ID
            }
            return (countryConfig.countries[countryCode]?.values as AbstractCollection).toArray()[0].toString()
        }
    }
}
