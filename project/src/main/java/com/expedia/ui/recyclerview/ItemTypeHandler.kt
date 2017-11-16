package com.expedia.ui.recyclerview

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.expedia.ui.javademo.interfaces.RecyclerViewContract
import com.expedia.ui.recyclerview.ItemTypeHandler.ItemViewType
import com.expedia.ui.recyclerview.viewholders.EmptyItemVH


/**
 * Created by nbirla on 15/11/17.
 */
class ItemTypeHandler {

    enum class ItemViewType(val id: Int) {

        NONE(-1);

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

    class Contract : com.expedia.ui.recyclerview.interfaces.RecyclerViewContract {

        override fun createHolder(inflater: LayoutInflater, parent: ViewGroup, type: Int): ItemVH<Any> {
            val itemViewType = ItemViewType.getItemViewType(type)
            var itemHolder: ItemVH<*>

            when(itemViewType){

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