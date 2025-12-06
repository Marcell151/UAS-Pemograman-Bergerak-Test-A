package com.example.kantinkampus;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SESSION MANAGER - Updated for Dual Role System
 * Manages login sessions for both Seller and Buyer
 */
public class SessionManager {
    private static final String PREF_NAME = "KantinKampusSession_v3";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole"; // 'seller' or 'buyer'
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_ID_NUMBER = "userIdNumber"; // Kartu Usaha or NIM/NIP
    private static final String KEY_USER_TYPE = "userType"; // 'mahasiswa'/'dosen'/'staff' or null

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(User user) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, user.getId());
        editor.putString(KEY_USER_EMAIL, user.getEmail());
        editor.putString(KEY_USER_NAME, user.getName());
        editor.putString(KEY_USER_ROLE, user.getRole());
        editor.putString(KEY_USER_PHONE, user.getPhone());
        editor.putString(KEY_USER_ID_NUMBER, user.getNimNip());
        editor.putString(KEY_USER_TYPE, user.getType());
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * Get current user details
     */
    public User getUserDetails() {
        if (!isLoggedIn()) {
            return null;
        }

        User user = new User();
        user.setId(sharedPreferences.getInt(KEY_USER_ID, -1));
        user.setEmail(sharedPreferences.getString(KEY_USER_EMAIL, ""));
        user.setName(sharedPreferences.getString(KEY_USER_NAME, ""));
        user.setRole(sharedPreferences.getString(KEY_USER_ROLE, ""));
        user.setPhone(sharedPreferences.getString(KEY_USER_PHONE, ""));
        user.setNimNip(sharedPreferences.getString(KEY_USER_ID_NUMBER, ""));
        user.setType(sharedPreferences.getString(KEY_USER_TYPE, ""));
        return user;
    }

    /**
     * Get user ID
     */
    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    /**
     * Get user name
     */
    public String getUserName() {
        return sharedPreferences.getString(KEY_USER_NAME, "");
    }

    /**
     * Get user role
     */
    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "");
    }

    /**
     * Get user type
     */
    public String getUserType() {
        return sharedPreferences.getString(KEY_USER_TYPE, "");
    }

    /**
     * Check if user is seller
     */
    public boolean isSeller() {
        return "seller".equals(getUserRole());
    }

    /**
     * Check if user is buyer
     */
    public boolean isBuyer() {
        return "buyer".equals(getUserRole());
    }

    /**
     * Check if buyer is mahasiswa
     */
    public boolean isMahasiswa() {
        return isBuyer() && "mahasiswa".equals(getUserType());
    }

    /**
     * Check if buyer is dosen
     */
    public boolean isDosen() {
        return isBuyer() && "dosen".equals(getUserType());
    }

    /**
     * Check if buyer is staff
     */
    public boolean isStaff() {
        return isBuyer() && "staff".equals(getUserType());
    }

    /**
     * Logout user
     */
    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    /**
     * Update user name
     */
    public void updateUserName(String name) {
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * Update user phone
     */
    public void updateUserPhone(String phone) {
        editor.putString(KEY_USER_PHONE, phone);
        editor.apply();
    }

    /**
     * Get user role display text
     */
    public String getRoleDisplayText() {
        if (isSeller()) {
            return "Penjual";
        } else if (isMahasiswa()) {
            return "Mahasiswa";
        } else if (isDosen()) {
            return "Dosen";
        } else if (isStaff()) {
            return "Staff";
        }
        return "User";
    }

    /**
     * Get greeting message
     */
    public String getGreetingMessage() {
        String name = getUserName();
        String role = getRoleDisplayText();
        return "Halo, " + name + " (" + role + ")! 👋";
    }
}