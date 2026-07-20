package com.sugarcane.erp.model;

public class Customer {
    private int id;
    private String name;
    private String mobile;
    private String village;
    private String address;
    private String gst;
    private double openingBalance;
    private String status;

    public Customer() {}

    public Customer(int id, String name, String mobile, String village, String address, 
                    String gst, double openingBalance, String status) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.village = village;
        this.address = address;
        this.gst = gst;
        this.openingBalance = openingBalance;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGst() { return gst; }
    public void setGst(String gst) { this.gst = gst; }

    public double getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(double openingBalance) { this.openingBalance = openingBalance; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
