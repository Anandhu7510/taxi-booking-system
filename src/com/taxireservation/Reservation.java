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
    private String serviceType;
    private String cabType;
    private String customerVehicle;
    private String driverName;
    private double fare;
    private String status; // Pending, Ongoing, Completed, Cancelled

    /**
     * Backward-compatible constructor for the original Taxi + Driver flow.
     */
    public Reservation(int id, String customerName, String customerPhone, String pickupLocation,
                       String dropLocation, String dateTime, String cabType, String driverName,
                       double fare, String status) {
        this(id, customerName, customerPhone, pickupLocation, dropLocation, dateTime,
                "Taxi + Driver", cabType, "", driverName, fare, status);
    }

    public Reservation(int id, String customerName, String customerPhone, String pickupLocation,
                       String dropLocation, String dateTime, String serviceType, String cabType,
                       String customerVehicle, String driverName, double fare, String status) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.pickupLocation = pickupLocation;
        this.dropLocation = dropLocation;
        this.dateTime = dateTime;
        this.serviceType = serviceType;
        this.cabType = cabType;
        this.customerVehicle = customerVehicle;
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
    public String getServiceType() { return serviceType; }
    public String getCabType() { return cabType; }
    public String getCustomerVehicle() { return customerVehicle; }
    public String getDriverName() { return driverName; }
    public double getFare() { return fare; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isDriverOnly() {
        return "Driver Only (Own Vehicle)".equalsIgnoreCase(serviceType);
    }

    public String getVehicleDisplay() {
        return isDriverOnly() ? customerVehicle : cabType;
    }
}
