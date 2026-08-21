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
    
    public boolean resetPassword(String username, String pin, String newPassword) {
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return false;
        }
        // Match user's PIN or master recovery PIN (e.g. 1234 or proprietor mobile numbers)
        boolean isPinValid = (user.getPinHash() != null && user.getPinHash().equals(pin)) 
                             || "1234".equals(pin) 
                             || "7588237123".equals(pin) 
                             || "9763948154".equals(pin);
        if (isPinValid) {
            return userDAO.updatePassword(username, newPassword);
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
