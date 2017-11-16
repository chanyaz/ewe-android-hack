package com.expedia.ui.javademo;

/**
 * Created by nbirla on 15/11/17.
 */

public class FeedItem<D> {

    enum ExpandState {
        NONE, EXPANDED, EXPANDING, COLLAPSING, COLLAPSED
    }

    private String id;
    private String priorityType;
    private String viewType;
    private D d;

    private ExpandState state = ExpandState.COLLAPSED;

    public FeedItem(String viewType, D d){
        this.viewType = viewType;
        this.d = d;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPriorityType() {
        return priorityType;
    }

    public void setPriorityType(String priorityType) {
        this.priorityType = priorityType;
    }

    public String getViewType() {
        return viewType;
    }

    public D getBindingData() {
        return d;
    }

    public ExpandState getExpandState() {
        return state;
    }

    public void setExpandState(ExpandState expandState) {
        this.state = expandState;
    }
}
