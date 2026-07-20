package com.sugarcane.erp.model;

public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String pinHash;
    private String role;

    public User() {}

    public User(int id, String username, String passwordHash, String pinHash, String role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.pinHash = pinHash;
        this.role = role;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
