package com.expedia.bookings.extensions

import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXSortFilterMetadata
import com.expedia.bookings.data.lx.LXSortType
import java.util.Collections
import java.util.Comparator

fun List<LXActivity>.applySortFilter(lxCategoryMetadata: LXSortFilterMetadata, isMipEnabled: Boolean, isModEnabled: Boolean): List<LXActivity> {

    // Activity name filter
    var activities = this.filter { it.title.contains(lxCategoryMetadata.filter, true) }
    // Sorting
    activities = when (lxCategoryMetadata.sort) {
        LXSortType.POPULARITY -> activities.sortedBy { it.popularityForClientSort }
        LXSortType.PRICE -> getSortedActivityList(activities, isMipEnabled, isModEnabled)
        null -> activities
    }

    val filteredSet = LinkedHashSet<LXActivity>()
    for (i in activities.indices) {
        for (filterCategory in lxCategoryMetadata.lxCategoryMetadataMap.entries) {
            val innerLxCategoryMetadata = filterCategory.value
            val lxCategoryMetadataKey = filterCategory.key
            if (innerLxCategoryMetadata.checked) {
                if (activities[i].categories.contains(lxCategoryMetadataKey)) {
                    filteredSet.add(activities[i])
                }
            }
        }
    }
    return if (filteredSet.size > 0 || lxCategoryMetadata.lxCategoryMetadataMap.size > 0) {
        filteredSet.toList()
    } else {
        activities
    }
}

fun getSortedActivityList(activities: List<LXActivity>, isMipEnabled: Boolean, isModEnabled: Boolean): List<LXActivity> {
    Collections.sort<LXActivity>(activities, object : Comparator<LXActivity> {
        override fun compare(lhs: LXActivity, rhs: LXActivity): Int {
            val leftMoney = lhs.getActivityPriceForSorting(isMipEnabled, isModEnabled)
            val rightMoney = rhs.getActivityPriceForSorting(isMipEnabled, isModEnabled)
            return leftMoney.compareTo(rightMoney)
        }
    })
    return activities
}
