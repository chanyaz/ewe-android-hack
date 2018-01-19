package com.expedia.testutils.builder

import com.expedia.bookings.data.GaiaSuggestion

class TestGaiaSuggestionBuilder {
    private val suggestion = GaiaSuggestion()

    fun gaiaId(gaiaId: String): TestGaiaSuggestionBuilder {
        suggestion.gaiaID = gaiaId
        return this
    }

    fun type(type: String): TestGaiaSuggestionBuilder {
        suggestion.type = type
        return this
    }

    fun position(lat: Double, lng: Double): TestGaiaSuggestionBuilder {
        val position = GaiaSuggestion.Position("Point", arrayOf(lat, lng))
        suggestion.position = position
        return this
    }

    fun localizedName(id: Int, shortName: String, fullName: String,
                      friendlyName: String): TestGaiaSuggestionBuilder {
        val localizedNames = arrayOf(GaiaSuggestion.LocalizedName(id, shortName, fullName,
                friendlyName))
        suggestion.localizedNames = localizedNames
        return this
    }

    fun country(name: String, code: String?): TestGaiaSuggestionBuilder {
        val country = GaiaSuggestion.Country(name, code)
        suggestion.country = country
        return this
    }

    fun name(name: String): TestGaiaSuggestionBuilder {
        suggestion.name = name
        return this
    }

    fun build() : GaiaSuggestion {
        return suggestion
    }
}
