package com.expedia.bookings.shared.vm

import android.content.Context
import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.data.travelgraph.SearchInfo
import com.expedia.bookings.text.HtmlCompat
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseSuggestionViewModel(val context: Context) {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val subtitleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()
    val suggestionLabelTitleObservable = PublishSubject.create<String>()
    val iconContentDescriptionObservable = PublishSubject.create<String>()

    protected lateinit var suggestion: SuggestionV4
    protected var searchInfo: SearchInfo? = null

    protected abstract fun getTitle(): String
    protected abstract fun getSubTitle(): String
    protected abstract fun getIcon(): Int

    fun bind(suggestion: SuggestionV4) {
        this.suggestion = suggestion

        titleObservable.onNext(getTitle())
        subtitleObservable.onNext(getSubTitle())
        isChildObservable.onNext(isChild(suggestion) && !suggestion.isHistoryItem)
        iconObservable.onNext(getIcon())
        iconContentDescriptionObservable.onNext(getIconContentDescription())
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
