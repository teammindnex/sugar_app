package com.sugarcane.erp.model;

public class Transport {
    private int id;
    private String transportName;
    private String vehicleNo;
    private String driverName;
    private String driverMobile;
    private String status;

    public Transport() {}

    public Transport(int id, String transportName, String vehicleNo, String driverName, String driverMobile, String status) {
        this.id = id;
        this.transportName = transportName;
        this.vehicleNo = vehicleNo;
        this.driverName = driverName;
        this.driverMobile = driverMobile;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTransportName() { return transportName; }
    public void setTransportName(String transportName) { this.transportName = transportName; }

    public String getVehicleNo() { return vehicleNo; }
    public void setVehicleNo(String vehicleNo) { this.vehicleNo = vehicleNo; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverMobile() { return driverMobile; }
    public void setDriverMobile(String driverMobile) { this.driverMobile = driverMobile; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
