package com.expedia.bookings.data.lx

import java.util.Comparator

    class LXCategoriesComparator : Comparator<LXCategoryMetadata> {
    override fun compare(categoryOne: LXCategoryMetadata, categoryTwo: LXCategoryMetadata): Int {
        return categoryOne.sortOrder.compareTo(categoryTwo.sortOrder)
    }
}
