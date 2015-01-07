package com.expedia.bookings.data.cars;

import com.expedia.bookings.data.SuggestionV2;

import org.joda.time.DateTime;

public class CarSearchParams {

    private DateTime startTime;
    private DateTime endTime;

    private SuggestionV2 origin;
    private SuggestionV2 destination;

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public SuggestionV2 getOrigin() {
        return origin;
    }

    public void setOrigin(SuggestionV2 origin) {
        this.origin = origin;
    }

    public SuggestionV2 getDestination() {
        return destination;
    }

    public void setDestination(SuggestionV2 destination) {
        this.destination = destination;
    }
}
