package com.expedia.bookings.flights.vm

import android.content.Context
import android.support.v4.content.ContextCompat
import android.text.Spanned
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.abacus.AbacusUtils
import com.expedia.bookings.featureconfig.AbacusFeatureConfigManager
import com.expedia.bookings.shared.vm.BaseSuggestionViewModel
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.SuggestionStrUtils

class FlightsSuggestionViewModel(context: Context) : BaseSuggestionViewModel(context) {
    private val essBoldRegex = Regex("<B>(.*?)</B>")
    private val boldWithBlackFontTagStart = "<font color='${ContextCompat.getColor(context, R.color.black)}'>"
    private val fontTagEnd = "</font>"

    private val isUserBucketedInHightlightContent = AbacusFeatureConfigManager.isBucketedForTest(context, AbacusUtils.EBAndroidAppFlightsContentHighlightInTypeahead)

    override fun getTitle(): CharSequence {
        val isChildSuggestion = isChild(suggestion)
        return if (isChildSuggestion && suggestion.isHistoryItem) {
            SuggestionStrUtils.formatDashWithoutSpace(getShortName(suggestion))
        } else if (isUserBucketedInHightlightContent && !suggestion.isHistoryItem) {
            getTitleWithHighlights(suggestion)
        } else {
            getDisplayName(suggestion)
        }
    }

    override fun getSubTitle() = ""

    private fun getTitleWithHighlights(suggestion: SuggestionV4): Spanned {
        val text = suggestion.regionNames.displayName.replace(essBoldRegex, { match ->
            "$boldWithBlackFontTagStart${match.value}$fontTagEnd"
        })
        return HtmlCompat.fromHtml(text)
    }
}
