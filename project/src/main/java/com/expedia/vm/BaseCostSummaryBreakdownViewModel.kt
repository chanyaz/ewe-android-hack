package com.expedia.vm

import android.content.Context
import android.support.annotation.ColorInt
import com.expedia.bookings.utils.FontCache
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

abstract class BaseCostSummaryBreakdownViewModel(val context: Context) {
    val iconVisibilityObservable = PublishSubject.create<Boolean>()
    val addRows = BehaviorSubject.create<List<CostSummaryBreakdownRow>>()

    abstract fun trackBreakDownClicked()

    class CostSummaryBreakdownRow(val title: String?, val cost: String?, @ColorInt val color: Int?, var typeface: FontCache.Font?, val separator: Boolean) {
        class Builder {
            var title: String? = null
            var cost: String? = null
            @ColorInt var color: Int? = null
            var typeface: FontCache.Font? = null
            var separator = false

            fun separator(): CostSummaryBreakdownRow {
                this.separator = true
                return build()
            }

            fun title(title: String): Builder {
                this.title = title
                if (title.isBlank()) {
                    throw IllegalArgumentException("Title can not be null or empty")
                }
                return this
            }

            fun cost(cost: String): Builder {
                this.cost = cost
                if (cost.isBlank()) {
                    throw IllegalArgumentException("Cost can not be null or empty")
                }
                return this
            }

            fun color(@ColorInt color: Int): Builder {
                this.color = color
                return this
            }

            fun typeface(typeface: FontCache.Font?): Builder {
                this.typeface = typeface
                return this
            }

            fun build(): CostSummaryBreakdownRow {
                return CostSummaryBreakdownRow(title, cost, color, typeface, separator)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CostSummaryBreakdownRow) return false

            if (title != other.title) return false
            if (cost != other.cost) return false
            if (color != other.color) return false
            if (typeface != other.typeface) return false
            if (separator != other.separator) return false

            return true
        }

        override fun hashCode(): Int {
            var result = title?.hashCode() ?: 0
            result = 31 * result + (cost?.hashCode() ?: 0)
            result = 31 * result + (color ?: 0)
            result = 31 * result + (typeface?.hashCode() ?: 0)
            result = 31 * result + separator.hashCode()
            return result
        }
    }
}