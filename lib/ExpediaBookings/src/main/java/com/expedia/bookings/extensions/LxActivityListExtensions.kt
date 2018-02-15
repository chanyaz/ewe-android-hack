package com.expedia.bookings.extensions

import com.expedia.bookings.data.lx.LXActivity
import com.expedia.bookings.data.lx.LXSortFilterMetadata
import com.expedia.bookings.data.lx.LXSortType

fun List<LXActivity>.applySortFilter(lxCategoryMetadata: LXSortFilterMetadata): List<LXActivity> {

    // Activity name filter
    var activities = this.filter { it.title.contains(lxCategoryMetadata.filter, true) }
    // Sorting
    activities = when (lxCategoryMetadata.sort) {
        LXSortType.POPULARITY -> activities.sortedBy { it.popularityForClientSort }
        LXSortType.PRICE -> activities.sortedBy { it.price.amount.toInt() }
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
