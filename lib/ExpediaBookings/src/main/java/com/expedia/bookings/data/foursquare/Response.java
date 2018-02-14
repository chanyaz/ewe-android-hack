
package com.expedia.bookings.data.foursquare;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response {

    @SerializedName("suggestedFilters")
    @Expose
    private SuggestedFilters suggestedFilters;
    @SerializedName("warning")
    @Expose
    private Warning warning;
    @SerializedName("headerLocation")
    @Expose
    private String headerLocation;
    @SerializedName("headerFullLocation")
    @Expose
    private String headerFullLocation;
    @SerializedName("headerLocationGranularity")
    @Expose
    private String headerLocationGranularity;
    @SerializedName("totalResults")
    @Expose
    private Integer totalResults;
    @SerializedName("suggestedBounds")
    @Expose
    private SuggestedBounds suggestedBounds;
    @SerializedName("groups")
    @Expose
    private List<Group> groups = null;
    @SerializedName("photos")
    @Expose
    private Photos photos;

    public void setPhotos(Photos photos) {
        this.photos = photos;
    }

    public Photos getPhotos() {
        return photos;
    }

    public SuggestedFilters getSuggestedFilters() {
        return suggestedFilters;
    }

    public void setSuggestedFilters(SuggestedFilters suggestedFilters) {
        this.suggestedFilters = suggestedFilters;
    }

    public Warning getWarning() {
        return warning;
    }

    public void setWarning(Warning warning) {
        this.warning = warning;
    }

    public String getHeaderLocation() {
        return headerLocation;
    }

    public void setHeaderLocation(String headerLocation) {
        this.headerLocation = headerLocation;
    }

    public String getHeaderFullLocation() {
        return headerFullLocation;
    }

    public void setHeaderFullLocation(String headerFullLocation) {
        this.headerFullLocation = headerFullLocation;
    }

    public String getHeaderLocationGranularity() {
        return headerLocationGranularity;
    }

    public void setHeaderLocationGranularity(String headerLocationGranularity) {
        this.headerLocationGranularity = headerLocationGranularity;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public SuggestedBounds getSuggestedBounds() {
        return suggestedBounds;
    }

    public void setSuggestedBounds(SuggestedBounds suggestedBounds) {
        this.suggestedBounds = suggestedBounds;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

}
