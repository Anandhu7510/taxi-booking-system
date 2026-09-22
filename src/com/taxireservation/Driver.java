package com.taxireservation;

/**
 * Simple POJO representing a Driver / Vehicle record.
 */
public class Driver {
    private int id;
    private String name;
    private String phone;
    private String licenseNo;
    private String vehicleNumber;
    private String vehicleType; // Mini, Sedan, SUV, Luxury
    private String status;      // Available, On Trip, Offline

    public Driver(int id, String name, String phone, String licenseNo,
                  String vehicleNumber, String vehicleType, String status) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.licenseNo = licenseNo;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLicenseNo() { return licenseNo; }
    public void setLicenseNo(String licenseNo) { this.licenseNo = licenseNo; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() { return name + " (" + vehicleNumber + ")"; }
}
