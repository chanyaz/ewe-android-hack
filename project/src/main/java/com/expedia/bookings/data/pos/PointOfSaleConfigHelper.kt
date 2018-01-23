package com.expedia.bookings.data.pos

import android.content.res.AssetManager
import java.io.InputStream

open class PointOfSaleConfigHelper(private val assetManager: AssetManager, private val posConfigFilename: String) {

    open fun openPointOfSaleConfiguration(): InputStream {
        return assetManager.open(posConfigFilename)
    }

    open fun openExpediaSuggestSupportedLocalesConfig(): InputStream {
        return assetManager.open("ExpediaSharedData/ExpediaSuggestSupportedLocales.json")
    }

    open fun openPaymentPostalCodeOptionalCountriesConfiguration(): InputStream {
        return assetManager.open("ExpediaSharedData/ExpediaPaymentPostalCodeOptionalCountries.json")
    }
}
