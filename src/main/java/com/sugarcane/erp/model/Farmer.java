package com.sugarcane.erp.model;

public class Farmer {
    private int id;
    private String name;
    private String mobile;
    private String village;
    private String taluka;
    private String district;
    private String address;
    private String aadharNumber;
    private String bankDetails;
    private double openingBalance;
    private String remarks;
    private String status;

    public Farmer() {}

    public Farmer(int id, String name, String mobile, String village, String taluka, String district, 
                  String address, String aadharNumber, String bankDetails, double openingBalance, 
                  String remarks, String status) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.village = village;
        this.taluka = taluka;
        this.district = district;
        this.address = address;
        this.aadharNumber = aadharNumber;
        this.bankDetails = bankDetails;
        this.openingBalance = openingBalance;
        this.remarks = remarks;
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

    public String getTaluka() { return taluka; }
    public void setTaluka(String taluka) { this.taluka = taluka; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getAadharNumber() { return aadharNumber; }
    public void setAadharNumber(String aadharNumber) { this.aadharNumber = aadharNumber; }

    public String getBankDetails() { return bankDetails; }
    public void setBankDetails(String bankDetails) { this.bankDetails = bankDetails; }

    public double getOpeningBalance() { return openingBalance; }
    public void setOpeningBalance(double openingBalance) { this.openingBalance = openingBalance; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
