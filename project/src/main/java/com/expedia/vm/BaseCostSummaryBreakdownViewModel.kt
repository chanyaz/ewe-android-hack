package com.expedia.vm

import android.content.Context
import rx.subjects.BehaviorSubject
import rx.subjects.PublishSubject

open class BaseCostSummaryBreakdownViewModel(val context: Context) {
    val iconVisibilityObservable = PublishSubject.create<Boolean>()
    val addRows = BehaviorSubject.create<List<CostSummaryBreakdown>>()

    data class CostSummaryBreakdown(val title: String, val color: Int?, val cost: String, val isDiscount: Boolean,
                               val isTotalDue: Boolean, val isTotalCost: Boolean, val isLine: Boolean) {

        class CostSummaryBuilder() {
            var title: String = ""
            var color: Int? = null
            var cost: String = ""
            var isDiscount = false
            var isTotalDue = false
            var isTotalCost = false
            var isLine = false

            fun title(title: String): CostSummaryBuilder {
                this.title = title
                return this
            }

            fun color(color: Int): CostSummaryBuilder {
                this.color = color
                return this
            }

            fun cost(cost: String): CostSummaryBuilder {
                this.cost = cost
                return this
            }

            fun discount(isDiscount: Boolean): CostSummaryBuilder {
                this.isDiscount = isDiscount
                return this
            }

            fun totalDue(isTotalDue: Boolean): CostSummaryBuilder {
                this.isTotalDue = isTotalDue
                return this
            }

            fun isLine(isLine: Boolean): CostSummaryBuilder {
                this.isLine = isLine
                return this
            }

            fun build(): CostSummaryBreakdown {
                var params = CostSummaryBreakdown(title, color, cost, isDiscount, isTotalDue, isTotalCost, isLine)
                return params
            }
        }
    }
}