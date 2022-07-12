package com.iceberg.password_manager;

public class Password {

    private String pwID;
    private String itemName;
    private String email;
    private String password;

    public Password(String pwID, String itemName, String email, String password) {
        this.pwID = pwID;
        this.itemName = itemName;
        this.email = email;
        this.password = password;
    }

    public String getpwID() {
        return pwID;
    }

    public void setpwID(String pwID) {
        this.pwID = pwID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
