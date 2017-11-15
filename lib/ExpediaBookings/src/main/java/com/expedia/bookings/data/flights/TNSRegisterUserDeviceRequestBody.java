package com.expedia.bookings.data.flights;

import java.util.List;

/**
 * Created by napandey on 11/15/17.
 */
public class TNSRegisterUserDeviceRequestBody {

    private String activityId;
    private Courier courier;
    private List<Flight> flights = null;
    private User user;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Courier getCourier() {
        return courier;
    }

    public void setCourier(Courier courier) {
        this.courier = courier;
    }

    public List<Flight> getFlights() {
        return flights;
    }

    public void setFlights(List<Flight> flights) {
        this.flights = flights;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}