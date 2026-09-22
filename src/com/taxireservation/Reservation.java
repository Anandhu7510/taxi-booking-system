package com.taxireservation;

/**
 * Simple POJO representing a booking / reservation.
 */
public class Reservation {
    private int id;
    private String customerName;
    private String customerPhone;
    private String pickupLocation;
    private String dropLocation;
    private String dateTime;
    private String cabType;
    private String driverName;
    private double fare;
    private String status; // Pending, Ongoing, Completed, Cancelled

    public Reservation(int id, String customerName, String customerPhone, String pickupLocation,
                        String dropLocation, String dateTime, String cabType, String driverName,
                        double fare, String status) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.dateTime = dateTime;
        this.cabType = cabType;
        this.driverName = driverName;
        this.fare = fare;
        this.status = status;
    }

    public int getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getPickupLocation() { return pickupLocation; }
    public String getDropLocation() { return dropLocation; }
    public String getDateTime() { return dateTime; }
    public String getCabType() { return cabType; }
    public String getDriverName() { return driverName; }
    public double getFare() { return fare; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
