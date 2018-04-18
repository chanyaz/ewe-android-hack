package com.expedia.bookings.shared.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.R
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.text.HtmlCompat
import com.expedia.bookings.utils.FontCache
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseSuggestionViewModel(val context: Context) {

    // Outputs
    val titleObservable = BehaviorSubject.create<CharSequence>()
    val subtitleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()
    val suggestionLabelTitleObservable = PublishSubject.create<String>()
    val iconContentDescriptionObservable = PublishSubject.create<String>()
    val titleFontObservable = BehaviorSubject.create<FontCache.Font>()

    protected lateinit var suggestion: SuggestionV4
    protected var searchInfo: SearchInfo? = null

    protected abstract fun getTitle(): CharSequence
    protected abstract fun getSubTitle(): String

    open fun isIconContentDescriptionRequired() = false

    fun bind(suggestion: SuggestionV4) {
        this.suggestion = suggestion

        titleObservable.onNext(getTitle())
        subtitleObservable.onNext(getSubTitle())
        titleFontObservable.onNext(if (getSubTitle().isEmpty()) FontCache.Font.ROBOTO_REGULAR else FontCache.Font.ROBOTO_MEDIUM)
        isChildObservable.onNext(isChild(suggestion) && !suggestion.isHistoryItem)
        iconObservable.onNext(getIcon())
        if (isIconContentDescriptionRequired()) {
            iconContentDescriptionObservable.onNext(getIconContentDescription())
        } else {
            iconContentDescriptionObservable.onNext("")
        }
    }

    open fun getIcon(): Int {
        return when {
            suggestion.isHistoryItem -> R.drawable.recents
            suggestion.iconType == SuggestionV4.IconType.CURRENT_LOCATION_ICON -> R.drawable.ic_suggest_current_location
            else -> R.drawable.search_type_icon
        }
    }

    fun bind(searchInfo: SearchInfo) {
        this.searchInfo = searchInfo
        bind(searchInfo.destination)
    }

    fun bindLabel(label: String) {
        suggestionLabelTitleObservable.onNext(label)
    }

    protected fun getDisplayName(suggestion: SuggestionV4): String {
        return HtmlCompat.stripHtml(suggestion.regionNames.displayName)
    }

    protected fun getShortName(suggestion: SuggestionV4): String {
        return HtmlCompat.stripHtml(suggestion.regionNames.shortName)
    }

    @VisibleForTesting
    fun isChild(suggestion: SuggestionV4): Boolean {
        return suggestion.hierarchyInfo?.isChild ?: false
    }

    private fun getIconContentDescription(): String {
        return when (suggestion.type) {
            "HOTEL" -> "HOTEL_ICON"
            "AIRPORT" -> "AIRPORT_ICON"
            else -> suggestion.iconType.toString()
        }
    }
}
