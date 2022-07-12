package com.iceberg.password_manager;

public class Device {

    private String android_id;
    private boolean approved;
    private byte[] extraData;
    private String make;
    private String model;
    private String nickname;

    public Device(String android_id, boolean approved, byte[] extraData, String make, String model, String nickname) {
        this.android_id = android_id;
        this.approved = approved;
        this.extraData = extraData;
        this.make = make;
        this.model = model;
        this.nickname = nickname;
    }

    public Device(String android_id, boolean approved, String make, String model, String nickname) {
        this.android_id = android_id;
        this.approved = approved;
        this.make = make;
        this.model = model;
        this.nickname = nickname;
    }

    public String getAndroid_id() {
        return android_id;
    }

    public void setAndroid_id(String android_id) {
        this.android_id = android_id;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public byte[] getExtraData() {
        return extraData;
    }

    public void setExtraData(byte[] extraData) {
        this.extraData = extraData;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
