
package com.expedia.bookings.data.foursquare;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Photos {

    @SerializedName("count")
    @Expose
    private Integer count;
    @SerializedName("groups")
    @Expose
    private List<Object> groups = null;
    @SerializedName("items")
    @Expose
    private List<Items> items = null;

    public void setItems(List<Items> items) {
        this.items = items;
    }

    public List<Items> getItems() {
        return items;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<Object> getGroups() {
        return groups;
    }

    public void setGroups(List<Object> groups) {
        this.groups = groups;
    }



}
