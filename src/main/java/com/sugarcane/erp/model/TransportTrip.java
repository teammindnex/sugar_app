package com.sugarcane.erp.model;

import java.time.LocalDate;

public class TransportTrip {
    private int id;
    private int transportId;
    private String transportName; // Transient
    private LocalDate tripDate;
    private Integer farmerId;
    private Integer customerId;
    private String pickupLocation;
    private String destination;
    private double weight;
    private double tripCharge;
    private double diesel;
    private double toll;
    private double advance;
    private double balance;
    private String tripStatus;

    public TransportTrip() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTransportId() { return transportId; }
    public void setTransportId(int transportId) { this.transportId = transportId; }

    public String getTransportName() { return transportName; }
    public void setTransportName(String transportName) { this.transportName = transportName; }

    public LocalDate getTripDate() { return tripDate; }
    public void setTripDate(LocalDate tripDate) { this.tripDate = tripDate; }

    public Integer getFarmerId() { return farmerId; }
    public void setFarmerId(Integer farmerId) { this.farmerId = farmerId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getTripCharge() { return tripCharge; }
    public void setTripCharge(double tripCharge) { this.tripCharge = tripCharge; }

    public double getDiesel() { return diesel; }
    public void setDiesel(double diesel) { this.diesel = diesel; }

    public double getToll() { return toll; }
    public void setToll(double toll) { this.toll = toll; }

    public double getAdvance() { return advance; }
    public void setAdvance(double advance) { this.advance = advance; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getTripStatus() { return tripStatus; }
    public void setTripStatus(String tripStatus) { this.tripStatus = tripStatus; }
}
