package com.blooddonation.finder.models;

public class BloodRequest {
    private String requestId;
    private String requesterId;
    private String requesterName;
    private String requesterPhone;
    private String bloodGroup;
    private String city;
    private double latitude;
    private double longitude;
    private String hospitalName;
    private String urgency; // CRITICAL, URGENT, NORMAL
    private String status;  // OPEN, ACCEPTED, COMPLETED, CANCELLED
    private long timestamp;
    private String acceptedDonorId;
    private String notes;

    public BloodRequest() {}

    public BloodRequest(String requesterId, String requesterName, String requesterPhone,
                        String bloodGroup, String city, double latitude, double longitude,
                        String hospitalName, String urgency) {
        this.requesterId = requesterId;
        this.requesterName = requesterName;
        this.requesterPhone = requesterPhone;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hospitalName = hospitalName;
        this.urgency = urgency;
        this.status = "OPEN";
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getRequesterId() { return requesterId; }
    public void setRequesterId(String requesterId) { this.requesterId = requesterId; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public String getRequesterPhone() { return requesterPhone; }
    public void setRequesterPhone(String requesterPhone) { this.requesterPhone = requesterPhone; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getUrgency() { return urgency; }
    public void setUrgency(String urgency) { this.urgency = urgency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getAcceptedDonorId() { return acceptedDonorId; }
    public void setAcceptedDonorId(String acceptedDonorId) { this.acceptedDonorId = acceptedDonorId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
