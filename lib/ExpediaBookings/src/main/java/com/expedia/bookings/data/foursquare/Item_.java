
package com.expedia.bookings.data.foursquare;

import java.io.Serializable;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item_ implements Serializable {

    @SerializedName("reasons")
    @Expose
    private Reasons reasons;
    @SerializedName("venue")
    @Expose
    private Venue venue;
    @SerializedName("tips")
    @Expose
    private List<Tip> tips = null;
    @SerializedName("referralId")
    @Expose
    private String referralId;

    public Reasons getReasons() {
        return reasons;
    }

    public void setReasons(Reasons reasons) {
        this.reasons = reasons;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public List<Tip> getTips() {
        return tips;
    }

    public void setTips(List<Tip> tips) {
        this.tips = tips;
    }

    public String getReferralId() {
        return referralId;
    }

    public void setReferralId(String referralId) {
        this.referralId = referralId;
    }

}
