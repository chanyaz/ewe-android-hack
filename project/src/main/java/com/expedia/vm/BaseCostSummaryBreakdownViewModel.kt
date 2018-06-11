package com.expedia.vm

import android.content.Context
import android.support.annotation.ColorInt
import com.expedia.bookings.utils.FontCache
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

abstract class BaseCostSummaryBreakdownViewModel(val context: Context) {
    val iconVisibilityObservable = PublishSubject.create<Boolean>()
    val addRows = BehaviorSubject.create<List<CostSummaryBreakdownRow>>()
    val priceSummaryContainerDescription = PublishSubject.create<String>()

    abstract fun trackBreakDownClicked()

    class CostSummaryBreakdownRow(val title: String?, val cost: String?, @ColorInt val titleColor: Int?, @ColorInt val costColor: Int?, @ColorInt val separatorColor: Int?, var titleTypeface: FontCache.Font?, var costTypeface: FontCache.Font?, val separator: Boolean, val strikeThrough: Boolean, val titleTextSize: Float?, val costTextSize: Float?) {
        class Builder {
            var title: String? = null
            var cost: String? = null
            @ColorInt
            var titleColor: Int? = null
            @ColorInt
            var costColor: Int? = null
            @ColorInt
            var separatorColor: Int? = null
            var titleTypeface: FontCache.Font? = null
            var costTypeface: FontCache.Font? = null
            var separator = false
            var strikeThrough = false
            var titleTextSize: Float? = null
            var costTextSize: Float? = null

            fun separator(): CostSummaryBreakdownRow {
                this.separator = true
                return build()
            }

            fun strikeThrough(strikeThrough: Boolean): Builder {
                this.strikeThrough = strikeThrough
                return this
            }

            fun titleTextSize(titleTextSize: Float): Builder {
                this.titleTextSize = titleTextSize
                return this
            }

            fun costTextSize(costTextSize: Float): Builder {
                this.costTextSize = costTextSize
                return this
            }

            fun title(title: String?): Builder {
                this.title = title
                if (title.isNullOrEmpty()) {
                    throw IllegalArgumentException("Title can not be null or empty")
                }
                return this
            }

            fun cost(cost: String?): Builder {
                this.cost = cost
                if (cost.isNullOrEmpty()) {
                    throw IllegalArgumentException("Cost can not be null or empty")
                }
                return this
            }

            fun color(@ColorInt color: Int): Builder {
                titleColor(color)
                costColor(color)
                return this
            }

            fun titleColor(@ColorInt titleColor: Int): Builder {
                this.titleColor = titleColor
                return this
            }

            fun costColor(@ColorInt costColor: Int): Builder {
                this.costColor = costColor
                return this
            }

            fun separatorColor(@ColorInt separatorColor: Int): Builder {
                this.separatorColor = separatorColor
                return this
            }

            fun typeface(typeface: FontCache.Font?): Builder {
                titleTypeface(typeface)
                costTypeface(typeface)
                return this
            }

            fun titleTypeface(titleTypeface: FontCache.Font?): Builder {
                this.titleTypeface = titleTypeface
                return this
            }

            fun costTypeface(costTypeface: FontCache.Font?): Builder {
                this.costTypeface = costTypeface
                return this
            }

            fun build(): CostSummaryBreakdownRow {
                return CostSummaryBreakdownRow(title, cost, titleColor, costColor, separatorColor, titleTypeface, costTypeface, separator, strikeThrough, titleTextSize, costTextSize)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CostSummaryBreakdownRow) return false

            if (title != other.title) return false
            if (cost != other.cost) return false
            if (titleColor != other.titleColor) return false
            if (costColor != other.costColor) return false
            if (separatorColor != other.separatorColor) return false
            if (titleTypeface != other.titleTypeface) return false
            if (costTypeface != other.costTypeface) return false
            if (separator != other.separator) return false
            if (strikeThrough != other.strikeThrough) return false
            if (titleTextSize != other.titleTextSize) return false
            if (costTextSize != other.costTextSize) return false

            return true
        }

        override fun hashCode(): Int {
            var result = title?.hashCode() ?: 0
            result = 31 * result + (cost?.hashCode() ?: 0)
            result = 31 * result + (titleColor ?: 0)
            result = 31 * result + (costColor ?: 0)
            result = 31 * result + (separatorColor ?: 0)
            result = 31 * result + (titleTypeface?.hashCode() ?: 0)
            result = 31 * result + (costTypeface?.hashCode() ?: 0)
            result = 31 * result + separator.hashCode()
            result = 31 * result + (strikeThrough.hashCode())
            result = 31 * result + (titleTextSize?.hashCode() ?: 0)
            result = 31 * result + (costTextSize?.hashCode() ?: 0)
            return result
        }
    }
}
