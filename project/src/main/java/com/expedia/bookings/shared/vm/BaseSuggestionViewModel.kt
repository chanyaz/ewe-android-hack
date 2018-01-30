package com.expedia.bookings.shared.vm

import android.support.annotation.VisibleForTesting
import com.expedia.bookings.data.SuggestionV4
import com.expedia.bookings.text.HtmlCompat
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseSuggestionViewModel() {

    // Outputs
    val titleObservable = BehaviorSubject.create<String>()
    val subtitleObservable = BehaviorSubject.create<String>()
    val isChildObservable = BehaviorSubject.create<Boolean>()
    val iconObservable = BehaviorSubject.create<Int>()
    val suggestionLabelTitleObservable = PublishSubject.create<String>()

    private lateinit var suggestion: SuggestionV4

    protected abstract fun getTitle(suggestion: SuggestionV4): String
    protected abstract fun getSubTitle(suggestion: SuggestionV4): String
    protected abstract fun getIcon(suggestion: SuggestionV4): Int

    fun bind(suggestion: SuggestionV4) {
        this.suggestion = suggestion

        titleObservable.onNext(getTitle(suggestion))
        subtitleObservable.onNext(getSubTitle(suggestion))
        isChildObservable.onNext(isChild(suggestion) && !suggestion.isHistoryItem)
        iconObservable.onNext(getIcon(suggestion))
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
}
