package com.sugarcane.erp.model;

import java.time.LocalDate;

public class Worker {
    private int id;
    private String name;
    private String mobile;
    private String village;
    private String workType;
    private LocalDate joiningDate;
    private String status;

    public Worker() {}

    public Worker(int id, String name, String mobile, String village, String workType, LocalDate joiningDate, String status) {
        this.id = id;
        this.name = name;
        this.mobile = mobile;
        this.village = village;
        this.workType = workType;
        this.joiningDate = joiningDate;
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

    public String getWorkType() { return workType; }
    public void setWorkType(String workType) { this.workType = workType; }

    public LocalDate getJoiningDate() { return joiningDate; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
