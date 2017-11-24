package com.expedia.ui.recyclerview

/**
 * Created by nbirla on 15/11/17.
 */
class FeedItem<D>(val viewType: String, val d: D) {

    enum class ExpandState {
        NONE, EXPANDED, EXPANDING, COLLAPSING, COLLAPSED
    }

    constructor(feeditem :FeedItem<D>) : this(feeditem.viewType, feeditem.getBindingData()) {
        this.state = feeditem.state

        if(feeditem.id != null) {
            this.id = feeditem.id
        }

        if(feeditem.priorityType != null){
            this.priorityType = feeditem.priorityType
        }
    }

    private var id: String? = null
    private var priorityType: String? = null

    private var state : ExpandState = ExpandState.COLLAPSED

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