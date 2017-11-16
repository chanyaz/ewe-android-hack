package com.expedia.ui.recyclerview

/**
 * Created by nbirla on 15/11/17.
 */
class FeedItem<D>(val viewType: String, val d: D) {

    enum class ExpandState {
        NONE, EXPANDED, EXPANDING, COLLAPSING, COLLAPSED
    }

    private var id: String? = null
    private var priorityType: String? = null

    var state = ExpandState.COLLAPSED

    fun getId(): String? {
        return id
    }

    fun setId(id: String) {
        this.id = id
    }

    fun getBindingData(): D {
        return d
    }

    fun getFeedItemViewType(): String {
        return viewType
    }

    fun getPriorityType(): String? {
        return priorityType
    }

    fun setPriorityType(priorityType: String) {
        this.priorityType = priorityType
    }

    fun getExpandState(): ExpandState {
        return state
    }

    fun setExpandState(state: ExpandState) {
        this.state = state
    }
}