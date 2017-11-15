package com.expedia.bookings.data.flights;

public class Flight {

    private String airline;
    private String arrival_date;
    private String departureDay;
    private String departure_date;
    private String destination;
    private String flightUuid;
    private String flight_no;
    private String origin;

    public void setAirline(String airline) {
        this.airline = airline;
    }


    public void setArrivalDate(String arrivalDate) {
        this.arrival_date = arrivalDate;
    }

    public void setDepartureDay(String departureDay) {
        this.departureDay = departureDay;
    }

    public void setDepartureDate(String departureDate) {
        this.departure_date = departureDate;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setFlightUuid(String flightUuid) {
        this.flightUuid = flightUuid;
    }

    public void setFlightNo(String flightNo) {
        this.flight_no = flightNo;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

}
