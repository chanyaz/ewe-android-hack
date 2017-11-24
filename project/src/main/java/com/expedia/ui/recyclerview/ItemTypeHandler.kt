package com.expedia.ui.recyclerview

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.bookings.R
import com.expedia.ui.javademo.interfaces.RecyclerViewContract
import com.expedia.ui.recyclerview.ItemTypeHandler.ItemViewType
import com.expedia.ui.recyclerview.viewholders.DateVH
import com.expedia.ui.recyclerview.viewholders.DescVH
import com.expedia.ui.recyclerview.viewholders.EmptyItemVH
import com.expedia.ui.recyclerview.viewholders.HeaderVH


/**
 * Created by nbirla on 15/11/17.
 */
class ItemTypeHandler {

    enum class ItemViewType(val id: Int) {

        NONE(-1), DATEVH(0), HEADERVH(1), DESCVH(2);

        companion object {
            fun getItemViewType(name: String): ItemViewType {
                if (TextUtils.isEmpty(name)) return NONE
                var type = NONE

                try {
                    type = ItemViewType.valueOf(name.toUpperCase())
                } catch (e: IllegalArgumentException) {
                }

                return type
            }

            fun getViewType(type: String): Int {
                val itemType: ItemViewType

                if (TextUtils.isEmpty(type)) {
                    itemType = ItemViewType.NONE

                } else {
                    itemType = ItemViewType.getItemViewType(type)
                }

                return itemType.getItemId()
            }

            fun getItemViewType(id: Int): ItemViewType {
                for (type in ItemViewType.values()) {
                    if (id == type.id) return type
                }
                return NONE
            }
        }

        fun getItemId(): Int {
            return id
        }
    }

    companion object {

        val CONTRACT = object : com.expedia.ui.recyclerview.interfaces.RecyclerViewContract{
            override fun createHolder(inflater: LayoutInflater, parent: ViewGroup, type: Int): ItemVH<*> {
                val itemViewType = ItemViewType.getItemViewType(type)
                var itemHolder: ItemVH<*>

                when (itemViewType) {
                    ItemViewType.DATEVH -> {
                        itemHolder = DateVH(inflater.inflate(R.layout.row_item_date, parent, false));
                    }
                    ItemViewType.HEADERVH -> {
                        itemHolder = HeaderVH(inflater.inflate(R.layout.row_item_header, parent, false));
                    }
                    ItemViewType.DESCVH -> {
                        itemHolder = DescVH(inflater.inflate(R.layout.row_item, parent, false));
                    }
                    else -> {
                        itemHolder = EmptyItemVH(View(inflater.context))
                    }
                }

                return itemHolder
            }

            override fun getViewType(type: String): Int {
                return ItemViewType.getViewType(type)
            }

        }
    }

}