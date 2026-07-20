package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.UserDAO;
import com.sugarcane.erp.model.User;

public class AuthService {
    
    private UserDAO userDAO;
    private static User loggedInUser;
    
    public AuthService() {
        this.userDAO = new UserDAO();
    }
    
    public boolean login(String username, String password) {
        User user = userDAO.getUserByUsername(username);
        // Note: In production, password should be hashed. Using plain text or simple hash here for initial setup.
        if (user != null && user.getPasswordHash().equals(password)) {
            loggedInUser = user;
            return true;
        }
        return false;
    }
    
    public boolean loginWithPin(String username, String pin) {
        User user = userDAO.getUserByUsername(username);
        if (user != null && pin.equals(user.getPinHash())) {
            loggedInUser = user;
            return true;
        }
        return false;
    }
    
    public static User getLoggedInUser() {
        return loggedInUser;
    }
    
    public static void logout() {
        loggedInUser = null;
    }
}
